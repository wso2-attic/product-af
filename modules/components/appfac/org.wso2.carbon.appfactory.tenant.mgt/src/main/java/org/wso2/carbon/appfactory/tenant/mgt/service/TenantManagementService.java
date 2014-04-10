package org.wso2.carbon.appfactory.tenant.mgt.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.bam.integration.BamDataPublisher;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.TenantUserEventListner;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean;
import org.wso2.carbon.appfactory.tenant.mgt.util.AppFactoryTenantMgtUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.xsom.impl.scd.Iterators.Map;

public class TenantManagementService extends AbstractAdmin {
	private static final String CLAIMS_FIRSTLOGIN = "http://wso2.org/claims/firstlogin";
    private static Log log = LogFactory.getLog(TenantManagementService.class);


	/**
	 * Update roles of the user
	 * 
	 * @param userName
	 *            user to update
	 * @param rolesToBeAdded
	 *            roles to add
	 * @param rolesToBeRemoved
	 *            roles to delete
	 * @return
	 * @throws TenantManagementException
	 * @throws UserStoreException
	 * @throws AppFactoryException
	 */
	private boolean updateRolesOfUser(String userName, String[] rolesToBeAdded,
			String[] rolesToBeRemoved) throws UserStoreException {
		CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();

		UserStoreManager userStoreManager = AppFactoryTenantMgtUtil
				.getRealmService().getTenantUserRealm(tenantId)
				.getUserStoreManager();
		userStoreManager.updateRoleListOfUser(userName, rolesToBeRemoved,
				rolesToBeAdded);
		return true;

	}

	/**
	 * Service method to update roles of existing user in the tenant
	 * 
	 * @param userName
	 * @param rolesToBeAdded
	 * @param rolesToBeRemoved
	 * @return
	 * @throws TenantManagementException
	 * @throws AppFactoryException
	 */
	public boolean updateUserRoles(String userName, String[] rolesToBeAdded,
			String[] rolesToBeRemoved) throws TenantManagementException,
			AppFactoryException {
		try {
			boolean result = updateRolesOfUser(userName, rolesToBeAdded,
					rolesToBeRemoved);
			Iterator<TenantUserEventListner> tenantUserEventListners = AppFactoryTenantMgtUtil
					.getTenantUserEventListners().iterator();

			while (tenantUserEventListners.hasNext()) {
				TenantUserEventListner tl = tenantUserEventListners.next();
				tl.onUserUpdate(new UserInfo(userName, rolesToBeAdded),
						CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
			}
			return result;
		} catch (UserStoreException e) {
			String msg = "User addition to tenant "
					+ CarbonContext.getThreadLocalCarbonContext().getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new TenantManagementException(e.getMessage(), e);
		}

	}

	/**
	 * Add new roles to an existing user in the tenant
	 * 
	 * @param userName
	 *            user name of the user to add roles
	 * @param rolesToBeAdded
	 *            roles to add
	 * @return true if the operation is success
	 * @throws TenantManagementException
	 * @throws AppFactoryException 
	 */
	public boolean addUserRoles(String userName, String[] rolesToBeAdded)
			throws TenantManagementException, AppFactoryException {
		try {
            // 222  add to tenant
			boolean result=updateRolesOfUser(userName, rolesToBeAdded, null);
			Iterator<TenantUserEventListner> tenantUserEventListners = AppFactoryTenantMgtUtil
					.getTenantUserEventListners().iterator();

			while (tenantUserEventListners.hasNext()) {
				TenantUserEventListner tl = tenantUserEventListners.next();
				tl.onUserRoleAddition(new UserInfo(userName),
						CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
			}
			return result;
		} catch (UserStoreException e) {
			String msg = "User addition to tenant "
					+ CarbonContext.getThreadLocalCarbonContext().getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new TenantManagementException(e.getMessage(), e);
		}

	}

	/**
	 * Remove special roles from the user, here everyone role is not removed
	 * 
	 * @param userName
	 *            user name of the user to remove
	 * @return
	 * @throws TenantManagementException
	 * @throws AppFactoryException 
	 */
	public boolean removeUserFromTenant(String userName)
			throws TenantManagementException, AppFactoryException {
		CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		UserStoreManager userStoreManager;
		try {
			userStoreManager = AppFactoryTenantMgtUtil.getRealmService()
					.getTenantUserRealm(tenantId).getUserStoreManager();
			userStoreManager.deleteUser(userName);
		    Iterator<TenantUserEventListner> tenantUserEventListners = AppFactoryTenantMgtUtil
					.getTenantUserEventListners().iterator();

			while (tenantUserEventListners.hasNext()) {
				TenantUserEventListner tl = tenantUserEventListners.next();
				tl.onUserDeletion(new UserInfo(userName),
						CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
			}

            updateBAMStats(userName, AppFactoryConstants.BAM_DELETE_DATA);

			return true;
		} catch (UserStoreException e) {
			String msg = "User deletion from tenant "
					+ currentContext.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new TenantManagementException(e.getMessage(), e);
		}
	}

	/**
	 * get all users of the tenant
	 * 
	 * @return UserInfoBean arrays is returned which contains all required data
	 *         such as Fname,Lname,email address
	 * @throws TenantManagementException
	 */
	public UserInfoBean[] getUsersofTenant() throws TenantManagementException {
		CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		HashMap<String, UserInfoBean> userMap = new HashMap<String, UserInfoBean>();
		try {
			UserStoreManager userStoreManager = AppFactoryTenantMgtUtil
					.getRealmService().getTenantUserRealm(tenantId)
					.getUserStoreManager();
			String[] Roles = userStoreManager.getRoleNames();
			
			for (String role : Roles) {

				if (!AppFactoryTenantMgtUtil.everyOneRoleName.equals(role)) {
					String[] users = userStoreManager.getUserListOfRole(role);
					for (String user : users) {

                        if (!userMap.containsKey(user) &&
                            !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(user)) {
                            userMap.put(user,
                                        AppFactoryTenantMgtUtil.getUserInfoBean(user, tenantId));
                        }
					}
				}
			}
			Collection<UserInfoBean> userInfoBeans = userMap.values();
			return userInfoBeans
					.toArray(new UserInfoBean[userInfoBeans.size()]);

		} catch (UserStoreException e) {
			String msg = "Retrieving users of tenant "
					+ currentContext.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new TenantManagementException(e.getMessage(), e);

		}

	}

	

	/**
	 * get information of a single user specified by the user name
	 * 
	 * @param userName
	 *            user to get the information
	 * @return
	 * @throws TenantManagementException
	 */
	public UserInfoBean getUserInfo(String userName)
			throws TenantManagementException {
		try {
		    CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
	        int tenantId = context.getTenantId();
			return AppFactoryTenantMgtUtil.getUserInfoBean(userName, tenantId);
		} catch (TenantManagementException e) {
			String msg = "Retrieving user of tenant "
					+ CarbonContext.getThreadLocalCarbonContext().getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new TenantManagementException(msg, e);
		}
	}
	
	public int getTenantId(String tenantDomain) throws TenantManagementException{
	    try {
            return AppFactoryTenantMgtUtil.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Retrieving tenant Id of tenant "
                    + tenantDomain
                    + " failed due to " + e.getMessage();
            log.error(msg);
            throw new TenantManagementException(e.getMessage(), e);
        }
	}
	
    public boolean importUsersTotheTenant(String[] users, String defaultPassword)
                                                                                 throws TenantManagementException {
        String[] defaultUserRoles =
                                 AppFactoryTenantMgtUtil.getConfiguration()
                                                        .getProperties(AppFactoryConstants.TENANT_ROLES_DEFAULT_USER_ROLE);
        CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();

        UserStoreManager userStoreManager = null;
        try {
            userStoreManager = threadLocalCarbonContext.getUserRealm().getUserStoreManager();
        } catch (UserStoreException e) {
            String msg =
                    "Importing users to tenant " + threadLocalCarbonContext.getTenantDomain() +
                            " failed due to " + e.getMessage();
            log.error(msg);
            throw new TenantManagementException(e.getMessage(), e);
        }

        HashMap<String, String> claims=new HashMap<String, String>();
        claims.put(CLAIMS_FIRSTLOGIN, "true");
        StringBuilder failedUsers = null;
        for (String user : users) {
            try {
                userStoreManager.addUser(user, defaultPassword, defaultUserRoles,
                        claims, null, true);

                updateBAMStats(user, AppFactoryConstants.BAM_ADD_DATA);

            } catch (UserStoreException e) {
                if(failedUsers == null) {
                    failedUsers = new StringBuilder(user);
                } else {
                    failedUsers.append("," + user);
                }
                String msg =
                        "Importing users to tenant " + threadLocalCarbonContext.getTenantDomain() +
                                " failed due to " + e.getMessage();
                log.error(msg);
            }
        }

        if(failedUsers != null) {
            String errorMsg = "Error importing users: " + failedUsers.toString() + " to tenant " +
                    threadLocalCarbonContext.getTenantDomain();
            throw new TenantManagementException(errorMsg);
        }

            return true;
    }


    /**
     * Update stats on BAM
     * @param userName user name
     * @param action action
     * @throws TenantManagementException
     */
    private void updateBAMStats(String userName, String action) throws TenantManagementException {
        BamDataPublisher bamDataPublisher = new BamDataPublisher();
        try {
            bamDataPublisher.PublishTenantUserUpdateEvent("" + CarbonContext.getThreadLocalCarbonContext().
                    getTenantId(), userName, action,
                    System.currentTimeMillis());
        } catch (AppFactoryException e) {
            String msg = e.getMessage();
            if ((AppFactoryConstants.BAM_DELETE_DATA).equals(action)) {
                msg =
                        "Failed to publish data to BAM on user delete event for tenant " +
                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " due to " +
                                e.getMessage();
            } else if ((AppFactoryConstants.BAM_ADD_DATA).equals(action)) {
                msg =
                        "Failed to publish data to BAM on user add event for tenant " +
                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " due to " +
                                e.getMessage();

            }

            log.error(msg);
            throw new TenantManagementException(e.getMessage(), e);
        }
    }

}
