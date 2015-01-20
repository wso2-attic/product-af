/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.appfactory.ext.jndi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.ext.Util;
import org.wso2.carbon.base.CarbonContextHolderBase;
import org.wso2.carbon.base.UnloadTenantTask;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.event.EventContext;
import javax.naming.event.EventDirContext;
import javax.naming.event.NamingListener;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;
import javax.naming.spi.NamingManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements JNDI Context per deployed application(Eg Webapp).It is creating a sub Context within tenant's sub Context
 * Most of the logic are borrowed from CarbonInitialJNDIContext
 */
public class ApplicationAwareCarbonInitialJNDIContext implements EventDirContext, LdapContext {
    private static final Log log = LogFactory.getLog
            (ApplicationAwareCarbonInitialJNDIContext.class);

    private Context initialContext;

    public Map<String, Context> getContextCache() {
        return contextCache;
    }

    private Map<String, Context> contextCache =
            Collections.synchronizedMap(new HashMap<String, Context>());
    private static ContextCleanupTask contextCleanupTask;
    private static List<String> superTenantOnlyUrlContextSchemes;
    private static List<String> allTenantUrlContextSchemes;

    static {
        contextCleanupTask = new ContextCleanupTask();
        CarbonContextHolderBase.getCurrentCarbonContextHolderBase().registerUnloadTenantTask
                (contextCleanupTask);

        superTenantOnlyUrlContextSchemes = Arrays.asList(
                CarbonUtils.getServerConfiguration().getProperties(
                        "JNDI.Restrictions.SuperTenantOnly.UrlContexts.UrlContext.Scheme"));
        allTenantUrlContextSchemes = Arrays.asList(
                CarbonUtils.getServerConfiguration().getProperties(
                        "JNDI.Restrictions.AllTenants.UrlContexts.UrlContext.Scheme"));
    }

    public ApplicationAwareCarbonInitialJNDIContext(Context initialContext) throws NamingException {
        this.initialContext = initialContext;
    }

    private static String getScheme(String url) {
        if (null == url) {
            return null;
        }
        int colPos = url.indexOf(':');
        if (colPos < 0) {
            return null;
        }
        String scheme = url.substring(0, colPos);
        char c;
        boolean inCharSet;
        for (int i = 0; i < scheme.length(); i++) {
            c = scheme.charAt(i);
            inCharSet = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9') || c == '+' || c == '.'
                    || c == '-' || c == '_';
            if (!inCharSet) {
                return null;
            }
        }
        return scheme;
    }

    private Context getInitialContext(Name name) {
        return getInitialContext(name.get(0));
    }

    private Context getInitialContext() {
        return getInitialContext((String) null);
    }

    private boolean isBaseContextRequested() {

        try {
            String baseContextRequested = (String) this.initialContext.getEnvironment().
                    get(CarbonConstants.REQUEST_BASE_CONTEXT);
            if (baseContextRequested != null && baseContextRequested.equals("true")) {
                return true;
            }
        } catch (NamingException e) {
            log.warn(
                    "An error occurred while retrieving environment properties from initial context.",
                    e);
        }

        return false;
    }

    /**
     * Create new sub context within tenant context and return
     *
     * @param name Context name
     * @return Application sub context
     */
    protected Context getInitialContext(String name) {

        /**
         * If environment is requesting a base context return the
         * base context.
         */

        if (isBaseContextRequested()) {
            return initialContext;
        }

        Context base = null;
        String scheme = null;
        if (name != null) {
            // If the name has components
            scheme = getScheme(name);
            if (scheme != null) {
                if (getContextCache().containsKey(scheme)) {
                    base = getContextCache().get(scheme);
                } else {
                    try {
                        Context urlContext = NamingManager.getURLContext(scheme,
                                initialContext.getEnvironment());
                        if (urlContext != null) {
                            getContextCache().put(scheme, urlContext);
                            base = urlContext;
                        }
                    } catch (NamingException ignored) {
                        // If we are unable to find the context, we use the default context.
                        if (log.isDebugEnabled()) {
                            log.debug("If we are unable to find the context, we use the default context.", ignored);
                        }
                    }
                }
            }
        }
        if (base == null) {
            base = initialContext;
            scheme = null;
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!isSubTenant(tenantId)) {
            return base;
        } else if (scheme != null) {
            if (allTenantUrlContextSchemes.contains(scheme)) {
                return base;
            } else if (superTenantOnlyUrlContextSchemes.contains(scheme)) {
                throw new SecurityException("Tenants are not allowed to use JNDI contexts " +
                        "with scheme: " + scheme);
            }
        }
        String tenantContextName = Integer.toString(tenantId);
        String applicationContextName = Util.getCurrentApplicationContextName();
        Context subContext;
        try {
            subContext = (Context) base.lookup(tenantContextName);
            if (subContext != null) {
                return returnApplicationContext(subContext, applicationContextName, false, tenantId);
            }
        } catch (NamingException ignored) {
            // Depending on the JNDI Initial Context factory, the above operation may or may not
            // throw an exception. But, since we don't mind the exception, we can ignore it.
            if (log.isDebugEnabled()) {
                log.debug(ignored);
            }

        }
        try {
            log.debug("Creating Sub-Context: " + tenantContextName);
            subContext = base.createSubcontext(tenantContextName);
            subContext = returnApplicationContext(subContext, applicationContextName, true, tenantId);
            contextCleanupTask.register(tenantId, subContext);
            if (subContext == null) {
                throw new RuntimeException("Initial context was not created for tenant: " +
                        tenantId);
            }
            return subContext;
        } catch (NamingException e) {
            throw new RuntimeException("An error occurred while creating the initial context " +
                    "for tenant: " + tenantId, e);
        }
    }

    private Context returnApplicationContext(Context subContext, String applicationContextName,
                                             boolean isCreateOnly, int tenantID) {
        Context applicationContext = subContext;
        if (applicationContextName != null && !applicationContextName.isEmpty()) {
            if (!isCreateOnly) {
                try {
                    applicationContext = (Context) subContext.lookup(applicationContextName);
                    return applicationContext;
                } catch (NamingException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("ApplicationContext is not created yet.Creating new ApplicationContext " +
                                "" + applicationContextName + " for tenant " + tenantID);
                    }
                }
            }
            try {
                applicationContext = subContext.createSubcontext(applicationContextName);
            } catch (NamingException e) {
                throw new RuntimeException("An error occurred while creating the initial context " +
                        "for application: " + applicationContextName + " for tenant " + tenantID, e);
            }
        }
        return applicationContext;
    }

    private static class ContextCleanupTask implements UnloadTenantTask<Context> {

        private Map<Integer, ArrayList<Context>> contexts
                = new ConcurrentHashMap<Integer, ArrayList<Context>>();

        public void register(int tenantId, Context context) {
            ArrayList<Context> list = contexts.get(tenantId);
            if (list == null) {
                list = new ArrayList<Context>();
                list.add(context);
                contexts.put(tenantId, list);
            } else if (!list.contains(context)) {
                list.add(context);
            }
        }

        public void cleanup(int tenantId) {
            ArrayList<Context> list = contexts.remove(tenantId);
            // We need to close the context in a LIFO fashion.
            if (list != null) {
                Collections.reverse(list);
                for (Context context : list) {
                    try {
                        context.close();
                        if (log.isDebugEnabled()) {
                            log.debug("Closing " + context.getNameInNamespace() + " of " + tenantId);
                        }
                    } catch (NamingException ignore) {
                        // We are not worried about the exception thrown here, as we are simply
                        // doing a routine cleanup.
                        if (log.isDebugEnabled()) {
                            log.debug("Exception while outine cleanup", ignore);
                        }
                    }
                }
                list.clear();
            }
        }
    }

    public Object lookup(String s) throws NamingException {
        return getInitialContext(s).lookup(s);
    }

    public Object lookup(Name name) throws NamingException {
        return getInitialContext(name).lookup(name);
    }

    public void bind(String s, Object o) throws NamingException {
        getInitialContext(s).bind(s, o);
    }

    public void bind(Name name, Object o) throws NamingException {
        getInitialContext(name).bind(name, o);
    }

    public void rebind(String s, Object o) throws NamingException {
        getInitialContext(s).rebind(s, o);
    }

    public void rebind(Name name, Object o) throws NamingException {
        getInitialContext(name).rebind(name, o);
    }

    public void unbind(String s) throws NamingException {
        getInitialContext(s).unbind(s);
    }

    public void unbind(Name name) throws NamingException {
        getInitialContext(name).unbind(name);
    }

    public void rename(String s, String s1) throws NamingException {
        getInitialContext(s).rename(s, s1);
    }

    public void rename(Name name, Name name1) throws NamingException {
        getInitialContext(name).rename(name, name1);
    }

    public NamingEnumeration<NameClassPair> list(String s) throws NamingException {
        return getInitialContext(s).list(s);
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return getInitialContext(name).list(name);
    }

    public NamingEnumeration<Binding> listBindings(String s) throws NamingException {
        return getInitialContext(s).listBindings(s);
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return getInitialContext(name).listBindings(name);
    }

    public void destroySubcontext(String s) throws NamingException {
        getInitialContext(s).destroySubcontext(s);
    }

    public void destroySubcontext(Name name) throws NamingException {
        getInitialContext(name).destroySubcontext(name);
    }

    public Context createSubcontext(String s) throws NamingException {
        return getInitialContext(s).createSubcontext(s);
    }

    public Context createSubcontext(Name name) throws NamingException {
        return getInitialContext(name).createSubcontext(name);
    }

    public Object lookupLink(String s) throws NamingException {
        return getInitialContext(s).lookupLink(s);
    }

    public Object lookupLink(Name name) throws NamingException {
        return getInitialContext(name).lookupLink(name);
    }

    public NameParser getNameParser(String s) throws NamingException {
        return getInitialContext(s).getNameParser(s);
    }

    public NameParser getNameParser(Name name) throws NamingException {
        return getInitialContext(name).getNameParser(name);
    }

    public String composeName(String s, String s1) throws NamingException {
        return getInitialContext(s).composeName(s, s1);
    }

    public Name composeName(Name name, Name name1) throws NamingException {
        return getInitialContext(name).composeName(name, name1);
    }

    public Object addToEnvironment(String s, Object o) throws NamingException {
        return getInitialContext().addToEnvironment(s, o);
    }

    public Object removeFromEnvironment(String s) throws NamingException {
        return getInitialContext().removeFromEnvironment(s);
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        if (isSubTenant(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()
        )) {
            throw new NamingException("Tenants cannot retrieve the environment.");
        }
        return getInitialContext().getEnvironment();
    }

    public void close() throws NamingException {
        if (isSubTenant(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()) &&
                !isBaseContextRequested()) {
            CarbonUtils.checkSecurity();
        }

        Context ctx = this.getInitialContext();
            /* the below condition is there, because of a bug in Tomcat JNDI context close method,
             * see org.apache.naming.NamingContext#close() */
        if (!ctx.getClass().getName().equals("org.apache.naming.SelectorContext")) {
            ctx.close();
        }
    }

    public String getNameInNamespace() throws NamingException {
        return getInitialContext().getNameInNamespace();
    }

    public int hashCode() {
        return initialContext.hashCode();
    }

    public boolean equals(Object o) {
        return (o instanceof ApplicationAwareCarbonInitialJNDIContext) && initialContext.equals(o);
    }

    ////////////////////////////////////////////////////////
    // Methods Required by a DirContext
    ////////////////////////////////////////////////////////

    private DirContext getDirectoryContext(Name name) throws NamingException {
        return getDirectoryContext(name.get(0));
    }

    private DirContext getDirectoryContext() throws NamingException {
        return getDirectoryContext((String) null);
    }

    private DirContext getDirectoryContext(String name) throws NamingException {
        Context initialContext = getInitialContext(name);
        if (initialContext instanceof DirContext) {
            return (DirContext) initialContext;
        }
        throw new NamingException("The given Context is not an instance of "
                + DirContext.class.getName());
    }

    public Attributes getAttributes(Name name) throws NamingException {
        return getDirectoryContext(name).getAttributes(name);
    }

    public Attributes getAttributes(String s) throws NamingException {
        return getDirectoryContext(s).getAttributes(s);
    }

    public Attributes getAttributes(Name name, String[] strings) throws NamingException {
        return getDirectoryContext(name).getAttributes(name, strings);
    }

    public Attributes getAttributes(String s, String[] strings)
            throws NamingException {
        return getDirectoryContext(s).getAttributes(s, strings);
    }

    public void modifyAttributes(Name name, int i, Attributes attributes)
            throws NamingException {
        getDirectoryContext(name).modifyAttributes(name, i, attributes);
    }

    public void modifyAttributes(String s, int i, Attributes attributes)
            throws NamingException {
        getDirectoryContext(s).modifyAttributes(s, i, attributes);
    }

    public void modifyAttributes(Name name, ModificationItem[] modificationItems)
            throws NamingException {
        getDirectoryContext(name).modifyAttributes(name, modificationItems);
    }

    public void modifyAttributes(String s, ModificationItem[] modificationItems)
            throws NamingException {
        getDirectoryContext(s).modifyAttributes(s, modificationItems);
    }

    public void bind(Name name, Object o, Attributes attributes) throws NamingException {
        getDirectoryContext(name).bind(name, o, attributes);
    }

    public void bind(String s, Object o, Attributes attributes) throws NamingException {
        getDirectoryContext(s).bind(s, o, attributes);
    }

    public void rebind(Name name, Object o, Attributes attributes) throws NamingException {
        getDirectoryContext(name).rebind(name, o, attributes);
    }

    public void rebind(String s, Object o, Attributes attributes) throws NamingException {
        getDirectoryContext(s).rebind(s, o, attributes);
    }

    public DirContext createSubcontext(Name name, Attributes attributes)
            throws NamingException {
        return getDirectoryContext(name).createSubcontext(name, attributes);
    }

    public DirContext createSubcontext(String s, Attributes attributes)
            throws NamingException {
        return getDirectoryContext(s).createSubcontext(s, attributes);
    }

    public DirContext getSchema(Name name) throws NamingException {
        return getDirectoryContext(name).getSchema(name);
    }

    public DirContext getSchema(String s) throws NamingException {
        return getDirectoryContext(s).getSchema(s);
    }

    public DirContext getSchemaClassDefinition(Name name) throws NamingException {
        return getDirectoryContext(name).getSchemaClassDefinition(name);
    }

    public DirContext getSchemaClassDefinition(String s) throws NamingException {
        return getDirectoryContext(s).getSchemaClassDefinition(s);
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes attributes,
                                                  String[] strings) throws NamingException {
        return getDirectoryContext(name).search(name, attributes, strings);
    }

    public NamingEnumeration<SearchResult> search(String s, Attributes attributes,
                                                  String[] strings)
            throws NamingException {
        return getDirectoryContext(s).search(s, attributes, strings);
    }

    public NamingEnumeration<SearchResult> search(Name name, Attributes attributes)
            throws NamingException {
        return getDirectoryContext(name).search(name, attributes);
    }

    public NamingEnumeration<SearchResult> search(String s, Attributes attributes)
            throws NamingException {
        return getDirectoryContext(s).search(s, attributes);
    }

    public NamingEnumeration<SearchResult> search(Name name, String filter,
                                                  SearchControls searchControls)
            throws NamingException {
        return getDirectoryContext(name).search(name, filter, searchControls);
    }

    public NamingEnumeration<SearchResult> search(String s, String filter,
                                                  SearchControls searchControls)
            throws NamingException {
        return getDirectoryContext(s).search(s, filter, searchControls);
    }

    public NamingEnumeration<SearchResult> search(Name name, String filter, Object[] objects,
                                                  SearchControls searchControls)
            throws NamingException {
        return getDirectoryContext(name).search(name, filter, objects, searchControls);
    }

    public NamingEnumeration<SearchResult> search(String s, String filter, Object[] objects,
                                                  SearchControls searchControls)
            throws NamingException {
        return getDirectoryContext(s).search(s, filter, objects, searchControls);
    }

    ////////////////////////////////////////////////////////
    // Methods Required by a LdapContext
    ////////////////////////////////////////////////////////

    private LdapContext getLdapContext() throws NamingException {
        DirContext dirContext = getDirectoryContext();
        if (dirContext instanceof EventContext) {
            return (LdapContext) dirContext;
        }
        throw new NamingException("The given Context is not an instance of "
                + LdapContext.class.getName());
    }

    public ExtendedResponse extendedOperation(ExtendedRequest extendedRequest)
            throws NamingException {
        return getLdapContext().extendedOperation(extendedRequest);
    }

    public LdapContext newInstance(Control[] controls) throws NamingException {
        return getLdapContext().newInstance(controls);
    }

    public void reconnect(Control[] controls) throws NamingException {
        getLdapContext().reconnect(controls);
    }

    public Control[] getConnectControls() throws NamingException {
        return getLdapContext().getConnectControls();
    }

    public void setRequestControls(Control[] controls) throws NamingException {
        getLdapContext().setRequestControls(controls);
    }

    public Control[] getRequestControls() throws NamingException {
        return getLdapContext().getRequestControls();
    }

    public Control[] getResponseControls() throws NamingException {
        return getLdapContext().getResponseControls();
    }

    ////////////////////////////////////////////////////////
    // Methods Required by a EventContext
    ////////////////////////////////////////////////////////

    private EventContext getEventContext(Name name) throws NamingException {
        return getEventContext(name.get(0));
    }

    private EventContext getEventContext() throws NamingException {
        return getEventContext((String) null);
    }

    private EventContext getEventContext(String name) throws NamingException {
        Context initialContext = getInitialContext(name);
        if (initialContext instanceof EventContext) {
            return (EventContext) initialContext;
        }
        throw new NamingException("The given Context is not an instance of "
                + EventContext.class.getName());
    }

    public void addNamingListener(Name name, int i, NamingListener namingListener)
            throws NamingException {
        CarbonUtils.checkSecurity();
        getEventContext(name).addNamingListener(name, i, namingListener);
    }

    public void addNamingListener(String s, int i, NamingListener namingListener)
            throws NamingException {
        CarbonUtils.checkSecurity();
        getEventContext(s).addNamingListener(s, i, namingListener);
    }

    public void removeNamingListener(NamingListener namingListener) throws NamingException {
        CarbonUtils.checkSecurity();
        getEventContext().removeNamingListener(namingListener);
    }

    public boolean targetMustExist() throws NamingException {
        return getEventContext().targetMustExist();
    }

    ////////////////////////////////////////////////////////
    // Methods Required by a EventDirContext
    ////////////////////////////////////////////////////////

    private EventDirContext getEventDirContext(Name name) throws NamingException {
        return getEventDirContext(name.get(0));
    }

    private EventDirContext getEventDirContext(String name) throws NamingException {
        EventContext eventContext = getEventContext(name);
        if (eventContext instanceof EventDirContext) {
            return (EventDirContext) eventContext;
        }
        throw new NamingException("The given Context is not an instance of "
                + EventDirContext.class.getName());
    }

    public void addNamingListener(Name name, String filter, SearchControls searchControls,
                                  NamingListener namingListener) throws NamingException {
        CarbonUtils.checkSecurity();
        getEventDirContext(name)
                .addNamingListener(name, filter, searchControls, namingListener);
    }

    public void addNamingListener(String s, String filter, SearchControls searchControls,
                                  NamingListener namingListener) throws NamingException {
        CarbonUtils.checkSecurity();
        getEventDirContext(s).addNamingListener(s, filter, searchControls, namingListener);
    }

    public void addNamingListener(Name name, String filter, Object[] objects,
                                  SearchControls searchControls, NamingListener namingListener)
            throws NamingException {
        CarbonUtils.checkSecurity();
        getEventDirContext(name).addNamingListener(name, filter, objects, searchControls,
                namingListener);
    }

    public void addNamingListener(String s, String filter, Object[] objects,
                                  SearchControls searchControls, NamingListener namingListener)
            throws NamingException {
        CarbonUtils.checkSecurity();
        getEventDirContext(s).addNamingListener(s, filter, objects, searchControls,
                namingListener);
    }

    private static boolean isSubTenant(int tenantId) {
        return (tenantId != MultitenantConstants.SUPER_TENANT_ID &&
                tenantId != MultitenantConstants.INVALID_TENANT_ID);
    }
}
