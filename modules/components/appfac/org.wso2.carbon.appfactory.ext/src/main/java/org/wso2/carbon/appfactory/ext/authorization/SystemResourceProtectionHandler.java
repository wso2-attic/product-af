/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.appfactory.ext.authorization;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This registry handle implementation is used to secure registry resources that should be accessed only by stems bundles.
 * If web applications or services tried to access system registry resources, and those artifacts are not signed by trusted certificate,
 * security exceptions are thrown.
 * <p/>
 * Sample registry handler configuration is as below.
 * <handler class="org.wso2.carbon.appfactory.ext.authorization.SystemResourceProtectionHandler">
 * <filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher">
 * <property name="pattern">/_system/governance/trunk/.*</property>
 * </filter>
 * </handler>
 */
public class SystemResourceProtectionHandler extends Handler {
    private static final Log log = LogFactory.getLog(SystemResourceProtectionHandler.class);

    private void checkRequestIsFromTenantCode() throws RegistryException {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new AppFactorySecurityPermission("RegistryPermission"));
        }
    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.move(requestContext);
    }

    @Override
    public Resource get(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.get(requestContext);
    }

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.put(requestContext);
    }

    @Override
    public void importResource(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.importResource(requestContext);
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.delete(requestContext);
    }

    @Override
    public String copy(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.copy(requestContext);
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.rename(requestContext);
    }

    @Override
    public void putChild(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.putChild(requestContext);
    }

    @Override
    public void importChild(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.importChild(requestContext);
    }

    @Override
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.invokeAspect(requestContext);
    }

    @Override
    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.restoreVersion(requestContext);
    }

    @Override
    public void createVersion(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.createVersion(requestContext);
    }

    @Override
    public String[] getVersions(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.getVersions(requestContext);
    }

    @Override
    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.executeQuery(requestContext);
    }

    @Override
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.searchContent(requestContext);
    }

    @Override
    public void dump(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.dump(requestContext);
    }

    @Override
    public void restore(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        super.restore(requestContext);
    }

    @Override
    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        checkRequestIsFromTenantCode();
        return super.resourceExists(requestContext);
    }
}
