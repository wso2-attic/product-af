package org.wso2.carbon.appfactory.ext.internal;


import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.RoleBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.ext.Util;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.*;

/**
 * This class stores admin,hidden services deployed in current server and appfactory specific permissions and secured roles.
 */
public class AuthorizationMetaDataHolder {
    private static final Log log = LogFactory.getLog(AuthorizationMetaDataHolder.class);
    private HashMap<String, AxisService> services;
    private static AuthorizationMetaDataHolder instance = new AuthorizationMetaDataHolder();
    private Set<String> adminServices = null;
    private Set<String> hiddenServices = null;
    private Set<String> appFactoryPermissions = null;
    private Set<String> securedRoles = null;

    private AuthorizationMetaDataHolder() {
        this.services = ServiceHolder.getInstance().getConfigContextService().
                getServerConfigContext().getAxisConfiguration().getServices();
    }

    public static AuthorizationMetaDataHolder getInstance() {
        if (instance == null) {
            instance = new AuthorizationMetaDataHolder();
        }
        return instance;
    }

    public Set<String> getSecuredRoles() throws UserStoreException {
        if (securedRoles == null || securedRoles.isEmpty()) {
            synchronized (AuthorizationMetaDataHolder.class) {
                if (securedRoles == null || securedRoles.isEmpty()) {
                    AppFactoryConfiguration appFactoryConfiguration = ServiceHolder.getInstance().getAppFactoryConfiguration();

                    // get AppFactory specific tenant roles
                    String[] afSystemRoles = appFactoryConfiguration.getProperties(AppFactoryConstants.TENANT_ROLES_ROLE);
                    // get AppFactory specific default roles
                    String[] afDefaultRoles = appFactoryConfiguration.getProperties(AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE);
                    securedRoles = new HashSet<String>(Arrays.asList(afSystemRoles));
                    securedRoles.addAll(new HashSet<String>(Arrays.asList(afDefaultRoles)));

                    try {
                        UserRealm tenantUserRealm = ServiceHolder.getInstance().getRealmService().
                                getTenantUserRealm(CarbonContext.getThreadLocalCarbonContext().getTenantId());
                        // add tenant admin role
                        securedRoles.add(tenantUserRealm.getRealmConfiguration().getAdminRoleName());
                        // add everyone role
                        securedRoles.add(tenantUserRealm.getRealmConfiguration().getEveryOneRoleName());
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        String errorMsg = "Failed to get the tenant user realm of tenant:" +
                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                        log.error(errorMsg, e);
                        throw new UserStoreException(errorMsg, e);
                    }

                }
            }
        }

        return securedRoles;

    }

    public Set<String> getAdminServices() {
        if (adminServices == null || adminServices.isEmpty()) {
            synchronized (AuthorizationMetaDataHolder.class) {
                if (adminServices == null || adminServices.isEmpty()) {
                    adminServices = new HashSet<String>();
                    for (AxisService axisService : services.values()) {
                        if (SystemFilter.isAdminService(axisService)) {
                            adminServices.add(axisService.getName());
                        }
                    }
                }
            }
        }
        return adminServices;
    }

    public Set<String> getHiddenServices() {
        if (hiddenServices == null || hiddenServices.isEmpty()) {
            synchronized (AuthorizationMetaDataHolder.class) {
                if (hiddenServices == null || hiddenServices.isEmpty()) {
                    hiddenServices = new HashSet<String>();
                    for (AxisService axisService : services.values()) {
                        if (SystemFilter.isHiddenService(axisService)) {
                            hiddenServices.add(axisService.getName());
                        }
                    }
                }
            }
        }
        return hiddenServices;
    }

    public Set<String> getAppFactoryPermissions() throws AppFactoryException {
        if (appFactoryPermissions == null || appFactoryPermissions.isEmpty()) {
            synchronized (AuthorizationMetaDataHolder.class) {
                if (appFactoryPermissions == null || appFactoryPermissions.isEmpty()) {
                    appFactoryPermissions = new HashSet<String>();

                    String currentCloudStage = System.getProperty(AppFactoryConstants.CLOUD_STAGE);
                    if (currentCloudStage == null || currentCloudStage.isEmpty()) {
                        log.error(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
                        throw new AppFactoryException(AppFactoryConstants.CLOUD_STAGE + " system variable is not set.");
                    }
                    Set<RoleBean> roleBeans =
                            AppFactoryUtil.getRolePermissionConfigurations("ApplicationDeployment.DeploymentStage." +
                                    currentCloudStage + ".TenantRoles.Role", "");

                    for (RoleBean roleBean : roleBeans) {
                        List<Permission> permissions = roleBean.getPermissions(true);
                        permissions.addAll(roleBean.getPermissions(false));
                        for (Permission permission : permissions) {
                            appFactoryPermissions.add(permission.getResourceId());
                        }
                    }
                }
            }
        }
        return appFactoryPermissions;
    }
}
