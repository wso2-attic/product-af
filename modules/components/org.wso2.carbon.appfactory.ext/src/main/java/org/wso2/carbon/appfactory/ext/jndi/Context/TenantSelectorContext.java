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
package org.wso2.carbon.appfactory.ext.jndi.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.naming.SelectorContext;
import org.wso2.carbon.appfactory.ext.Util;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Represents each sub SelectorContexts created per tenants
 */
public class TenantSelectorContext extends SelectorContext {
    private static final Log log = LogFactory.getLog(TenantSelectorContext.class);
    private Context tenantInitialContext;

    public TenantSelectorContext(Hashtable<String, Object> env, boolean initialContext,
                                 Context carbonInitialContext) {
        super(env, initialContext);
        this.tenantInitialContext = carbonInitialContext;
    }

    public Object lookup(Name name) throws NamingException {


        if (isApplicationRequest(name)) {
            return getApplicationSelectorContext(name);
        }
        //Overrides lookup and lookupLink methods
        //Fist looking up in jndi level JNDI context
        //If it is not available in jndi level JNDI context
        //then perform lookup in carbon JNDI context  of tenant
        try {
            return super.lookup(name);
        } catch (NamingException ex) {
            //return carbonInitialContext.lookup(name);
            return tenantInitialContext.lookup(name);
        }
    }

    public Object lookup(String name) throws NamingException {


        if (isApplicationRequest(name)) {
            return getApplicationSelectorContext(name);
        }
        try {
            return super.lookup(name);
        } catch (NamingException ex) {
            return tenantInitialContext.lookup(name);
        }
    }

    public Object lookupLink(Name name) throws NamingException {


        if (isApplicationRequest(name)) {
            return getApplicationSelectorContext(name);
        }
        try {
            return super.lookupLink(name);
        } catch (NamingException ex) {
            return tenantInitialContext.lookup(name);
        }
    }

    public Object lookupLink(String name) throws NamingException {
        if (isApplicationRequest(name)) {
            return getApplicationSelectorContext(name);
        }
        try {
            return super.lookupLink(name);
        } catch (NamingException ex) {
            return tenantInitialContext.lookup(name);
        }
    }

    private Object getApplicationSelectorContext(Name name) throws NamingException {
        return getApplicationSelectorContext(name.get(0));
    }

    private Object getApplicationSelectorContext(String name) {

        Context applicationContext;
        try {
            applicationContext = (Context) tenantInitialContext.lookup(name);
        } catch (NamingException e) {
            //create one
            try {
                applicationContext = tenantInitialContext.createSubcontext(name);
            } catch (NamingException e1) {
                String msg = "Could not create application sub context for " + name;
                log.error(msg, e1);
                throw new RuntimeException(msg, e1);
            }
        }
        return new ApplicationSelectorContext(env, initialContext, this, applicationContext);

    }

    private boolean isApplicationRequest(Name name) {
        return isApplicationRequest(name.get(0));
    }

    private boolean isApplicationRequest(String name) {
        String currentApplication = Util.getCurrentApplicationContextName();
        if(name.startsWith(Util.JNDI_APPLICATION_SUB_CONTEXT_PREFIX)){
            if((currentApplication != null && !currentApplication.isEmpty() && name.equals
                    (currentApplication))){
                return true;
            } else {
                String msg = "Illegal access to " + name+" from "+Util.getCurrentArtifactName();
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }
        return false;
    }
}
