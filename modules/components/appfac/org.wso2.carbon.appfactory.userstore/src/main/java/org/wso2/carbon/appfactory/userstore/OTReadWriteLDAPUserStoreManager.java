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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.userstore.internal.OTLDAPUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.JNDIUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
/*
    This class is used to convert incoming email address into uid and convert results back to email.
    Consider that, userName === email
 */
public class OTReadWriteLDAPUserStoreManager extends ReadWriteLDAPUserStoreManager {
    private static Log log = LogFactory.getLog(OTReadWriteLDAPUserStoreManager.class);

    public OTReadWriteLDAPUserStoreManager(RealmConfiguration realmConfig,
                                           Map<String, Object> properties,
                                           ClaimManager claimManager,
                                           ProfileConfigurationManager profileManager,
                                           UserRealm realm, Integer tenantId)
            throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {
        return super.doAuthenticate(doConvert(userName), credential);
    }

    @Override
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        super.doUpdateRoleListOfUser(doConvert(userName), deletedRoles, newRoles);
    }

    @Override
    public void doUpdateUserListOfRole(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        super.doUpdateUserListOfRole(doConvert(userName), deletedRoles, newRoles);
    }

    @Override
    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        return super.getRoleListOfUser(doConvert(userName));
    }

    @Override
    public int getUserId(String userName) throws UserStoreException {
        return super.getUserId(doConvert(userName));
    }

    @Override
    public boolean isExistingUser(String userName) throws UserStoreException {
        return super.isExistingUser(doConvert(userName));
    }

    @Override
    public boolean isUserInRole(String userDN, SearchResult groupEntry)
            throws UserStoreException {
        return super.isUserInRole(doConvert(userDN), groupEntry);
    }

    private String doConvert(String email) throws UserStoreException {
        if (email == null) {
            throw new UserStoreException("User name can not be null.");
        }
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        return OTLDAPUtil.getUserIdFromEmail(email, this.connectionSource, searchBase);
    }
    @Override
    public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {
        String[] userNames = new String[0];

        if (maxItemLimit == 0) {
            return userNames;
        }

        int givenMax = Integer.parseInt(realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));

        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setCountLimit(maxItemLimit);

        if (filter.contains("?") || filter.contains("**")) {
            throw new UserStoreException(
                    "Invalid character sequence entered for user serch. Please enter valid sequence.");
        }

        StringBuffer searchFilter = null;
        searchFilter = new StringBuffer(
                realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_LIST_FILTER));
        String searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);

        String userNameProperty = realmConfig
                .getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
        StringBuffer buff = new StringBuffer();
        buff.append("(&").append(searchFilter).append("(").append(userNameProperty).append("=")
                .append(filter).append("))");

        String serviceNameAttribute = "sn";
        String mailAttribute ="mail";
        String returnedAtts[] = { userNameProperty, serviceNameAttribute, mailAttribute };

        searchCtls.setReturningAttributes(returnedAtts);
        DirContext dirContext = null;
        NamingEnumeration<SearchResult> answer = null;
        String[] allUserNames = null;
        try {
            dirContext = connectionSource.getContext();
            answer = dirContext.search(searchBase, buff.toString(), searchCtls);
            List<String> list = new ArrayList<String>();
            int i = 0;
            while (answer.hasMoreElements() && i < maxItemLimit) {
                SearchResult sr = (SearchResult) answer.next();
                if (sr.getAttributes() != null) {
                    Attribute attr = sr.getAttributes().get(mailAttribute);

                    /*
                     * If this is a service principle, just ignore and iterate rest of the array.
                     * The entity is a service if value of surname is Service
                     */
                    Attribute attrSurname = sr.getAttributes().get(serviceNameAttribute);

                    if (attrSurname != null) {
                        String serviceName = (String) attrSurname.get();
                        if (serviceName != null
                                && serviceName.equals(LDAPConstants.SERVER_PRINCIPAL_ATTRIBUTE_VALUE)) {
                            continue;
                        }
                    }

                    if (attr != null) {
                        String name = (String) attr.get();
                        //append the domain if exist
                        String domain = userRealm.getRealmConfiguration().getUserStoreProperty(
                                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                        if (domain != null) {
                            domain = domain + "/";
                            name = domain + name;
                        }
                        list.add(name);
                        i++;
                    }
                }
            }
            userNames = list.toArray(new String[list.size()]);
            //get secondary user lists
            UserStoreManager secUserManager = this.getSecondaryUserStoreManager();
            if (secUserManager != null) {
                String[] secUserNames = secUserManager.listUsers(filter, maxItemLimit);
                allUserNames = UserCoreUtil.combineArrays(userNames, secUserNames);
            } else {
                allUserNames = userNames;
            }
            Arrays.sort(allUserNames);
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }
        return allUserNames;
    }
}
