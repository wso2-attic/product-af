package org.wso2.carbon.cloudmgt.users.service;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloudmgt.common.CloudConstants;
import org.wso2.carbon.cloudmgt.users.beans.UserInfoBean;
import org.wso2.carbon.cloudmgt.users.util.UserMgtUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;


public class UserManagementService extends AbstractAdmin {
	private static final String CLAIMS_FIRSTLOGIN = "http://wso2.org/claims/firstlogin";
    private static Log log = LogFactory.getLog(UserManagementService.class);


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
	 * @throws UserManagementException
	 * @throws UserStoreException
	 * @throws AppFactoryException
	 */
	private boolean updateRolesOfUser(String userName, String[] rolesToBeAdded,
			String[] rolesToBeRemoved) throws UserStoreException {
		CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();

		UserStoreManager userStoreManager = UserMgtUtil
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
	 * @throws UserManagementException
	 * @throws AppFactoryException
	 */
	public boolean updateUserRoles(String userName, String[] rolesToBeAdded,
			String[] rolesToBeRemoved) throws UserManagementException
			 {
		try {
			boolean result = updateRolesOfUser(userName, rolesToBeAdded,
					rolesToBeRemoved);
			return result;
		} catch (UserStoreException e) {
			String msg = "User addition to tenant "
					+ CarbonContext.getThreadLocalCarbonContext().getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
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
	 * @throws UserManagementException
	 * @throws AppFactoryException 
	 */
	public boolean addUserRoles(String userName, String[] rolesToBeAdded)
			throws UserManagementException {
		try {
            // 222  add to tenant
			boolean result=updateRolesOfUser(userName, rolesToBeAdded, null);
			
			return result;
		} catch (UserStoreException e) {
			String msg = "User addition to tenant "
					+ CarbonContext.getThreadLocalCarbonContext().getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}

	}

	/**
	 * Remove special roles from the user, here everyone role is not removed
	 * 
	 * @param userName
	 *            user name of the user to remove
	 * @return
	 * @throws UserManagementException
	 * @throws AppFactoryException 
	 */
	public boolean removeUserFromTenant(String userName)
			throws UserManagementException {
		CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		UserStoreManager userStoreManager;
		try {
			userStoreManager = UserMgtUtil.getRealmService()
					.getTenantUserRealm(tenantId).getUserStoreManager();
			userStoreManager.deleteUser(userName);
           // updateBAMStats(userName, AppFactoryConstants.BAM_DELETE_DATA);

			return true;
		} catch (UserStoreException e) {
			String msg = "User deletion from tenant "
					+ currentContext.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * get all users of the tenant
	 * 
	 * @return UserInfoBean arrays is returned which contains all required data
	 *         such as Fname,Lname,email address
	 * @throws UserManagementException
	 */
	public UserInfoBean[] getUsersofTenant() throws UserManagementException {
		CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		HashMap<String, UserInfoBean> userMap = new HashMap<String, UserInfoBean>();
		try {
			UserStoreManager userStoreManager = UserMgtUtil
					.getRealmService().getTenantUserRealm(tenantId)
					.getUserStoreManager();
			String[] Roles = userStoreManager.getRoleNames();
			
			for (String role : Roles) {

				if (!UserMgtUtil.everyOneRoleName.equals(role)) {
					String[] users = userStoreManager.getUserListOfRole(role);
					for (String user : users) {

                        if (!userMap.containsKey(user) &&
                            !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(user)) {
                            userMap.put(user,
                                        UserMgtUtil.getUserInfoBean(user, tenantId));
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
			throw new UserManagementException(e.getMessage(), e);

		}

	}

	public String getUserEmail (String userName) throws UserStoreException, UserManagementException {
		UserInfoBean userInfo = getUserInfo(userName);
		if (userInfo.getEmail() != null) {
			return userInfo.getEmail() ;
		} else {
			int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
			userName = UserMgtUtil.getRealmService().getTenantManager().getTenant(tenantId).getAdminName();
			return getUserInfo(userName).getEmail();
		}
	}

	/**
	 * get information of a single user specified by the user name
	 * 
	 * @param userName
	 *            user to get the information
	 * @return
	 * @throws UserManagementException
	 */
	public UserInfoBean getUserInfo(String userName)
			throws UserManagementException {
		try {
		    CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
	        int tenantId = context.getTenantId();
			return UserMgtUtil.getUserInfoBean(userName, tenantId);
		} catch (UserManagementException e) {
			String msg = "Retrieving user of tenant "
					+ CarbonContext.getThreadLocalCarbonContext().getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(msg, e);
		}
	}
	
	public int getTenantId(String tenantDomain) throws UserManagementException{
	    try {
            return UserMgtUtil.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Retrieving tenant Id of tenant "
                    + tenantDomain
                    + " failed due to " + e.getMessage();
            log.error(msg);
            throw new UserManagementException(e.getMessage(), e);
        }
	}
	
    public boolean importUsersTotheTenant(String[] users, String defaultPassword)
                                                                                 throws UserManagementException {
        String[] defaultUserRoles =
                                 UserMgtUtil.getConfiguration()
                                                        .getProperties(CloudConstants.TENANT_ROLES_DEFAULT_USER_ROLE);
        CarbonContext threadLocalCarbonContext = CarbonContext.getThreadLocalCarbonContext();

        UserStoreManager userStoreManager = null;
        try {
            userStoreManager = threadLocalCarbonContext.getUserRealm().getUserStoreManager();
        } catch (UserStoreException e) {
            String msg =
                    "Importing users to tenant " + threadLocalCarbonContext.getTenantDomain() +
                            " failed due to " + e.getMessage();
            log.error(msg);
            throw new UserManagementException(e.getMessage(), e);
        }

        HashMap<String, String> claims=new HashMap<String, String>();
        claims.put(CLAIMS_FIRSTLOGIN, "true");
        StringBuilder failedUsers = null;
        for (String user : users) {
            try {
                userStoreManager.addUser(user, defaultPassword, defaultUserRoles,
                        claims, null, true);

             //   updateBAMStats(user, AppFactoryConstants.BAM_ADD_DATA);

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
            throw new UserManagementException(errorMsg);
        }

            return true;
    }


    /**
     * Update stats on BAM
     * @param userName user name
     * @param action action
     * @throws UserManagementException
//     */
//    private void updateBAMStats(String userName, String action) throws UserManagementException {
//        BamDataPublisher bamDataPublisher = new BamDataPublisher();
//        try {
//            bamDataPublisher.PublishTenantUserUpdateEvent("" + CarbonContext.getThreadLocalCarbonContext().
//                    getTenantId(), userName, action,
//                    System.currentTimeMillis());
//        } catch (Exception e) {
//            String msg = e.getMessage();
//            if ((AppFactoryConstants.BAM_DELETE_DATA).equals(action)) {
//                msg =
//                        "Failed to publish data to BAM on user delete event for tenant " +
//                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " due to " +
//                                e.getMessage();
//            } else if ((AppFactoryConstants.BAM_ADD_DATA).equals(action)) {
//                msg =
//                        "Failed to publish data to BAM on user add event for tenant " +
//                                CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " due to " +
//                                e.getMessage();
//
//            }
//
//            log.error(msg);
//            throw new UserManagementException(e.getMessage(), e);
//        }
//    }

}
