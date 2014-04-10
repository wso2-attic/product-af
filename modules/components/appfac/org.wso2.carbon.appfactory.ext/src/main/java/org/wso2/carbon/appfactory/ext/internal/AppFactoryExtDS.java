package org.wso2.carbon.appfactory.ext.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.ext.listener.AppFactoryClaimManagerListener;
import org.wso2.carbon.appfactory.ext.listener.AppFactoryAuthorizationManagerListener;
import org.wso2.carbon.appfactory.ext.listener.AppFactoryUserOperationEventListener;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.ext.internal"
 * immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1" policy="dynamic"  bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="appfactory.configuration"
 * interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppFactoryConfiguration"
 * unbind="unsetAppFactoryConfiguration"
 */
public class AppFactoryExtDS {
    private static final Log log = LogFactory.getLog(AppFactoryExtDS.class);

    @SuppressWarnings("UnusedDeclaration")
    protected void activate(ComponentContext context) {

        context.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                new AppFactoryUserOperationEventListener(), null);

        context.getBundleContext().registerService(AuthorizationManagerListener.class.getName(),
                new AppFactoryAuthorizationManagerListener(), null);

        context.getBundleContext().registerService(ClaimManagerListener.class.getName(),
                new AppFactoryClaimManagerListener(), null);


        if (log.isDebugEnabled()) {
            log.debug("appfactory.ext service bundle is activated");
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("appfactory.ext service bundle is deactivated");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService configContextService) {
        ServiceHolder.getInstance().setConfigContextService(configContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configContextService) {
        ServiceHolder.getInstance().setConfigContextService(null);
    }

    protected void setRealmService(RealmService realmService) {
        ServiceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.getInstance().setRealmService(null);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration configuration) {
        ServiceHolder.getInstance().setAppFactoryConfiguration(configuration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration configuration) {
        ServiceHolder.getInstance().setAppFactoryConfiguration(null);
    }
}
