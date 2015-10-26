package org.wso2.carbon.appfactory.lifecycle.mgt.internal;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.lifecycle.mgt.service.LifecycleManagementService;
import org.wso2.carbon.appfactory.lifecycle.mgt.service.LifecycleManagementServiceImpl;
import org.wso2.carbon.governance.lcm.services.LifeCycleManagementService;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="appfactory.lifecycle" immediate="true"
 * @scr.reference name="registry.service"
 * interface=
 * "org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="lifecycle.service"
 * interface="org.wso2.carbon.governance.lcm.services.LifeCycleManagementService"
 * ardinality="1..1" policy="dynamic" bind="setLifecycleService"
 * unbind="unsetLifecycleService"
 */
public class LifecycleServiceComponent {

    private static final Log log = LogFactory.getLog(LifecycleServiceComponent.class);
    private static TenantRegistryLoader tenantRegistryLoader;
    private static RegistryService registryService;
    private static RealmService realmService;
    private static LifeCycleManagementService lifeCycleManagementService;

    @SuppressWarnings("UnusedDeclaration") protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        try {
            CommonUtil.getRegistryService();

            //            LifecycleManagementService lifeCycleManagementService =
            //                    new LifecycleManagementServiceImpl();
            bundleContext
                    .registerService(LifecycleManagementService.class.getName(), new LifecycleManagementServiceImpl(),
                            null);
            if (log.isDebugEnabled()) {
                log.debug("Appfactory lifecycle bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating appfactory configuration", e);
        }
    }

    @SuppressWarnings("UnusedDeclaration") protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is deactivated");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry org.wso2.carbon.appfactory.lifecycle.mgt.service initialized");
        }
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    protected void setLifecycleService(LifeCycleManagementService lifeCycleManagementService) {
        this.lifeCycleManagementService = lifeCycleManagementService;
    }

    protected void unsetLifecycleService(LifeCycleManagementService lifeCycleManagementService) {
        this.lifeCycleManagementService = null;
    }
}

