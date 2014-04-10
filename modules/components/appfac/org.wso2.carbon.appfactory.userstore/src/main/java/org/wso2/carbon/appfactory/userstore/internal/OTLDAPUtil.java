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

package org.wso2.carbon.appfactory.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.util.JNDIUtil;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

public class OTLDAPUtil {
    private static OTUserIdCache otUserIdCache = OTUserIdCache.getOTUserIdCache();
    private static OTEmailCache otEmailCache = OTEmailCache.getOTEmailCache();
    private static Log log = LogFactory.getLog(OTLDAPUtil.class);

    public static String getUserIdFromEmail(String email, LDAPConnectionContext connectionSource,
                                            String userSearchBase) throws UserStoreException {
        // if it is not an email, just return it as the uid.
        if (!email.contains("@")) {
            return email;
        }
        // check from cache
        String userId = otUserIdCache.getValueFromCache(email);
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }

        // check from ldap and update the cache
        StringBuffer buff = new StringBuffer();
        buff.append("(&(objectClass=inetOrgPerson)(mail=").append(email).append("))");
        if (log.isDebugEnabled()) {
            log.debug("Searching for " + buff.toString());
        }
        DirContext dirContext = connectionSource.getContext();
        NamingEnumeration<SearchResult> answer = null;
        try {
            String name = null;
            answer = searchForUser(buff.toString(), null, dirContext, userSearchBase);
            int count = 0;
            SearchResult userObj = null;
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                if (count > 0) {
                    log.error("More than one user exist for the same name");
                }
                count++;
                userObj = sr;
            }
            if (userObj != null) {
                name = userObj.getName();
                if (name != null) {
                    name = name.replaceFirst("uid=", "");
                }
            }
            otUserIdCache.addToCache(email, name);
            return name;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

    }
    public static String getEmailFromUserId(String uid, LDAPConnectionContext connectionSource,
                                            String userSearchBase) throws UserStoreException {
        
        // check from cache
        String email = otEmailCache.getValueFromCache(uid);
        if (email != null && !email.isEmpty()) {
            return email;
        }

        // check from ldap and update the cache
        StringBuffer buff = new StringBuffer();
        buff.append("(&(objectClass=inetOrgPerson)(uid=").append(uid).append("))");
        if (log.isDebugEnabled()) {
            log.debug("Searching for " + buff.toString());
        }
        DirContext dirContext = connectionSource.getContext();
        NamingEnumeration<SearchResult> answer = null;
        try {
            String[] returnedAttributes = {"mail"};
            answer = searchForUser(buff.toString(), returnedAttributes, dirContext, userSearchBase);
            int count = 0;
            SearchResult userObj = null;
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                if (count > 0) {
                    log.error("More than one user exist for the same name");
                }
                count++;
                userObj = sr;
            }
            if (userObj != null) {
                
                Attributes attributes = userObj.getAttributes();
               Attribute mailAttribute = attributes.get("mail");
                if (mailAttribute != null) {
                    email = mailAttribute.getID();
                }
            }
            otEmailCache.addToCache(uid, email);
            return email;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            JNDIUtil.closeNamingEnumeration(answer);
            JNDIUtil.closeContext(dirContext);
        }

    }

    public static NamingEnumeration<SearchResult> searchForUser(String searchFilter,
                                                                String[] returnedAtts,
                                                                DirContext dirContext,
                                                                String userSearchBase)
            throws UserStoreException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        if (returnedAtts != null && returnedAtts.length > 0) {
            searchCtls.setReturningAttributes(returnedAtts);
        }
        try {
            return dirContext.search(userSearchBase, searchFilter, searchCtls);
        } catch (NamingException e) {
            log.error("Search failed.", e);
            throw new UserStoreException(e.getMessage());
        }
    }
}