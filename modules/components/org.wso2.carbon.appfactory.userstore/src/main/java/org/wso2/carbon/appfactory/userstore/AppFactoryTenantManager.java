/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.userstore;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.CommonHybridLDAPTenantManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.JNDIUtil;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.*;

/**
 * This class is the tenant manager for any external LDAP and based on the "ou" partitioning
 * per tenant under one DIT.
 */
public class AppFactoryTenantManager extends CommonHybridLDAPTenantManager {
    private static Log log = LogFactory.getLog(AppFactoryTenantManager.class);
    protected LDAPConnectionContext ldapConnectionSource;

    private TenantMgtConfiguration tenantMgtConfig = null;
    protected RealmConfiguration realmConfig = null;

    public AppFactoryTenantManager(OMElement omElement, Map<String, Object> properties)
            throws Exception {
        super(omElement, properties);
        tenantMgtConfig = (TenantMgtConfiguration) properties.get(
                UserCoreConstants.TENANT_MGT_CONFIGURATION);

        realmConfig = (RealmConfiguration) properties.get(UserCoreConstants.REALM_CONFIGURATION);
        this.ldapConnectionSource = (LDAPConnectionContext) properties.get(
                UserCoreConstants.LDAP_CONNECTION_SOURCE);

        if (ldapConnectionSource == null) {
        	ldapConnectionSource = new LDAPConnectionContext(realmConfig);
        }
        	
       try {
    	   ldapConnectionSource.getContext();
    	   log.info("LDAP connection created successfully in read-only mode");
       } catch (Exception e) {
    	   log.error(e.getMessage(), e);
               throw new UserStoreException(
                                            "Cannot create connection to Active directory server. Error message " +
                                                    e.getMessage());
       }
    }

    public AppFactoryTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    /**
     * Create a space for tenant in LDAP.
     *
     * @param orgName  Organization name.
     * @param tenant The tenant
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating.
     */
    @Override
    protected void createOrganizationalUnit(String orgName, Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        
        //e.g: ou=wso2.com
        String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
        createOrganizationalContext(partitionDN, orgName, initialDirContext);

        //create user store
        String organizationNameAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg:o=cse.org,dc=wso2,dc=com
        String dnOfOrganizationalContext = organizationNameAttribute + "=" + orgName + "," +
                partitionDN;
//        createOrganizationalSubContext(dnOfOrganizationalContext,
//                LDAPConstants.USER_CONTEXT_NAME, initialDirContext);

        //create group store
        createOrganizationalSubContext(dnOfOrganizationalContext,
                LDAPConstants.GROUP_CONTEXT_NAME, initialDirContext);

        //create admin entry
        String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg: ou=users,dc=wso2,dc=com
       String dnOfUserContext = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        String dnOfUserEntry = getAdminEntryDN(dnOfUserContext, tenant, initialDirContext);

        //create admin group if write ldap group is enabled
        if (("true").equals(realmConfig.getUserStoreProperty(
                LDAPConstants.WRITE_EXTERNAL_ROLES))) {
            //construct dn of group context: eg:ou=groups,o=cse.org,dc=wso2,dc=com
            String dnOfGroupContext = orgSubContextAttribute + "=" +
                    LDAPConstants.GROUP_CONTEXT_NAME + "," +
                    dnOfOrganizationalContext;
            
            createAdminGroup(dnOfGroupContext, dnOfUserEntry, initialDirContext);
        }
    }


    private String getAdminEntryDN(String dnOfUserContext, Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        String userDN = null;
        DirContext organizationalUsersContext = null;
        try {
            //get connection to tenant's user context
            organizationalUsersContext = (DirContext) initialDirContext.lookup(
                    dnOfUserContext);
            //read user name attribute in user-mgt.xml
            String userNameAttribute = realmConfig.getUserStoreProperty(
                    LDAPConstants.USER_NAME_ATTRIBUTE);

            String userRDN = userNameAttribute + "=" + tenant.getAdminName();
            //organizationalUsersContext.bind(userRDN, null, userAttributes);
            userDN = userRDN + "," + dnOfUserContext;
            //return (userRDN + dnOfUserContext);
        } catch (NamingException e) {
            String errorMsg = "Error occurred while creating Admin entry";
            log.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalUsersContext);
        }

        return userDN;
    }
    @Override
    public String[] getAllTenantDomainStrOfUser(String username)
            throws org.wso2.carbon.user.api.UserStoreException
    {
        String pattern = realmConfig.getUserStoreProperty("UserDNPattern");
        String userDN;
        if(pattern != null){
            //construct DN from User DN pattern configuration
            userDN = MessageFormat.format(pattern,username);
        } else {
           //search for user and get the DN
            userDN = getNameInSpaceForUserName(username);
        }
        //finally search for the above user DN in all Org context and return list of tenants domains
        return getTenantDomains(userDN);
    }

    protected String getNameInSpaceForUserName(String userName)
            throws UserStoreException
    {
        DirContext dirContext;
        String usernameSearchFilter = realmConfig.getUserStoreProperty("UserNameListFilter");
        String userNameProperty = realmConfig.getUserStoreProperty("UserNameAttribute");
        String searchFilter = getSearchFilter(usernameSearchFilter, userNameProperty, userName);
        if(log.isDebugEnabled()) {
            log.debug((new StringBuilder()).append("Searching for ").append(searchFilter).toString());
        }
        dirContext = ldapConnectionSource.getContext();
        NamingEnumeration answer = null;
        String userDn;
        try
        {
            String name = null;
            answer = searchForObject(searchFilter, null, dirContext, realmConfig.getUserStoreProperty("UserSearchBase"));
            int count = 0;
            SearchResult userObj;
            SearchResult sr;
            for(userObj = null; answer.hasMoreElements(); userObj = sr)
            {
                sr = (SearchResult)answer.next();
                if(count > 0){
                    log.error("More than one user exist for the same name");
                }
                count++;
            }

            if(userObj != null) {
                name = userObj.getNameInNamespace();
            }
            userDn = name;
        }
        catch(Exception e)
        {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
        finally
        {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        return userDn;
    }

    private String getSearchFilter(String searchFilter, String nameProperty, String name)
    {
        StringBuilder buff = new StringBuilder();
        buff.append("(&").append(searchFilter).append("(").append(nameProperty).append("=").append(name).append("))");
        return buff.toString();
    }

    protected String[] getTenantDomains(String userDN)
            throws UserStoreException
    {
        DirContext dirContext;
        String groupNameSearchFilter = realmConfig.getUserStoreProperty("GroupNameListFilter");
        String groupNameProperty = realmConfig.getUserStoreProperty("MembershipAttribute");
        String searchFilter = getSearchFilter(groupNameSearchFilter, groupNameProperty, userDN);
        Set<String> list = new HashSet<String>();
        if(log.isDebugEnabled()) {
            log.debug((new StringBuilder()).append("Searching for ").append(searchFilter).toString());
        }
        dirContext = ldapConnectionSource.getContext();
        NamingEnumeration answer = null;
        String domainsStrs[];
        try
        {
            String dn;
            String domain;
            answer = searchForObject(searchFilter, null, dirContext, tenantMgtConfig.getTenantStoreProperties().get("RootPartition"));
            while (answer.hasMoreElements())
            {
                SearchResult sr = (SearchResult)answer.next();
                dn = sr.getNameInNamespace();
                domain= getOrganizationalContextName(dn);
                if(domain!=null){
                list.add(domain);
                }
            }

            domainsStrs = list.toArray(new String[list.size()]);
        }
        catch(Exception e)
        {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        }
        finally
        {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

        return domainsStrs;


    }

    private String getOrganizationalContextName(String dn)
    {
        String rootLessDN = dn.split(tenantMgtConfig.getTenantStoreProperties().get("RootPartition"))[0];
        if(rootLessDN.split(",").length > 2)
        {
            String organizationalAndSubOrgContext = rootLessDN.split(",")[2];
            String orgContext = organizationalAndSubOrgContext.split(
                    (new StringBuilder()).append(tenantMgtConfig.getTenantStoreProperties().get("OrganizationalAttribute")).append("=").toString())[1];
            return orgContext;
        } else
        {
            return null;
        }
    }

    protected NamingEnumeration searchForObject(String searchFilter, String returnedAtts[], DirContext dirContext, String searchBase)
            throws UserStoreException
    {
        SearchControls searchCtls;
        searchCtls = new SearchControls();
        searchCtls.setSearchScope(2);
        if(returnedAtts != null && returnedAtts.length > 0)
            searchCtls.setReturningAttributes(returnedAtts);
        try {
            return dirContext.search(searchBase, searchFilter, searchCtls);
        } catch (NamingException e) {
            log.error("Search failed.", e);
            throw new UserStoreException(e.getMessage());
        }


    }
}
