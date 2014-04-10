package org.wso2.carbon.issue.tracker.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.api.UserStoreException;

public class TenantUtils {

    private static Logger logger = Logger.getLogger(TenantUtils.class);

    /**
     * returns the tenant id given the tenant domain
     *
     * @param tenantDomain the tenant domain
     * @return the tenant id
     * @throws IssueTrackerException an error while accessing the user store.
     */
    public static int getTenantId(String tenantDomain) throws UserStoreException {
        UserRealmService realmService =
                (UserRealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getOSGiService(UserRealmService.class);

        int tenantId = 0;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Error occured while retrieving tenant id for :" + tenantDomain;
            logger.error(msg, e);
            throw e;
        }

        return tenantId;
    }

    /**
     * returns the tenant id given the tenant domain
     *
     * @param tenantDomain the tenant domain
     * @return the tenant id
     * @throws IssueTrackerException an error while accessing the user store.
     */
    public static String[] getUsersOfTenant(String tenantDomain) throws UserStoreException {
        UserRealmService realmService =
                (UserRealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getOSGiService(UserRealmService.class);

        int tenantId = 0;
        String[] userNames;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            userNames = realmService.getTenantUserRealm(tenantId).getUserStoreManager().listUsers("*",Integer.MAX_VALUE);

        } catch (UserStoreException e) {
            String msg = "Error occured while retrieving tenant id for :" + tenantDomain;
            logger.error(msg, e);
            throw e;
        }

        return userNames;
    }

}
