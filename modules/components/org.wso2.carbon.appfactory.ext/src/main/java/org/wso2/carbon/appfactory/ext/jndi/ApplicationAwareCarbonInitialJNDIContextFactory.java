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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

/**
 * JNDI InitialContextFactory that builds {@link ApplicationAwareCarbonInitialJNDIContext}
 * Configure this class in Carbon.xml to enable application aware JNDI Context as follows
 * <p/>
 * <JNDI>
 * <CarbonInitialJNDIContextFactory>org.wso2.carbon.appfactory.ext.jndi
 * .ApplicationAwareCarbonInitialJNDIContextFactory
 * </CarbonInitialJNDIContextFactory>
 * </JNDI>
 */
public class ApplicationAwareCarbonInitialJNDIContextFactory implements InitialContextFactory {

    private InitialContextFactory factory;

    public ApplicationAwareCarbonInitialJNDIContextFactory(InitialContextFactory factory) {
        this.factory = factory;
    }

    public Context getInitialContext(Hashtable<?, ?> h) throws NamingException {
        return new ApplicationAwareCarbonInitialJNDIContext(factory.getInitialContext(h));
    }
}
