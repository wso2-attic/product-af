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

import org.apache.naming.SelectorContext;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Represents the sub Contexts created per application within a tenant
 * TODO:There is a issue in looking up common resources in tenant Context.Need to fix it properly
 */
public class ApplicationSelectorContext extends SelectorContext {
    //Application level context
    private Context applicationInitialContext;
    //Tenant  level context
    private Context parentInitialContext;

    public ApplicationSelectorContext(Hashtable<String, Object> env, boolean initialContext, Context tenantInitialContext, Context applicationContext) {
        super(env, initialContext);
        this.applicationInitialContext = applicationContext;
        this.parentInitialContext = tenantInitialContext;
    }

    public Object lookup(Name name) throws NamingException {

        //Overrides lookup and lookupLink methods
        //Fist looking up in tomcat level JNDI context
        //If it is not available in tomcat level JNDI context
        //then perform lookup in carbon JNDI context  of application
        try {
            return super.lookup(name);
        } catch (NamingException ex) {
      /*      try {*/
            return applicationInitialContext.lookup(name);
           /* } catch (NamingException e) {
                //finally lookup common resources at tenant level
                return this.parentInitialContext.lookup(name);
            }*/
        }
    }

    public Object lookup(String name) throws NamingException {
        try {
            return super.lookup(name);
        } catch (NamingException ex) {
       /*     try {*/
            return applicationInitialContext.lookup(name);
           /* } catch (NamingException e) {
                return this.parentInitialContext.lookup(name);
            }*/
        }
    }

    public Object lookupLink(Name name) throws NamingException {
        try {
            return super.lookupLink(name);
        } catch (NamingException ex) {
          /*  try {*/
            return applicationInitialContext.lookup(name);
           /* } catch (NamingException e) {
                return this.parentInitialContext.lookup(name);
            }*/
        }
    }

    public Object lookupLink(String name) throws NamingException {
        try {
            return super.lookupLink(name);
        } catch (NamingException ex) {
         /*   try {*/
            return applicationInitialContext.lookup(name);
         /*   } catch (NamingException e) {
                return this.parentInitialContext.lookup(name);
            }*/
        }
    }
}
