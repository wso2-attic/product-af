/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.common.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.appfactory.common.*;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;

/**
 * Main responsibility of this class is to build the Appfactory configuration.
 * Further, this also encapsulates utility methods related to configuration values.
 * <p/>
 * TODO: FIXME: Rename this class to AppfactoryConfigUtils and may be remove code
 * that doesn't fall into this class responsibility to somewhere else.
 */
public class AppFactoryUtil {
    private static final Log log = LogFactory.getLog(AppFactoryUtil.class);

    private static AppFactoryConfiguration appFactoryConfig = null;

    private AppFactoryUtil() throws AppFactoryException {

    }

    public static File getApplicationWorkDirectory(String applicationId, String version, String revision)
            throws AppFactoryException {

        File tempDir = new File(CarbonUtils.getTmpDir() + File.separator + applicationId);
        return tempDir;
    }

    public static AppFactoryConfiguration getAppfactoryConfiguration() throws AppFactoryException {

        if (appFactoryConfig == null) {

            loadAppFactoryConfiguration();
        }
        return appFactoryConfig;
    }

    private static void loadAppFactoryConfiguration() throws AppFactoryException {
        String fileLocation =
                new StringBuilder().append(CarbonUtils.getCarbonConfigDirPath()).append(File.separator)
                        .append(AppFactoryConstants.CONFIG_FOLDER).append(File.separator)
                        .append(AppFactoryConstants.CONFIG_FILE_NAME).toString();

        appFactoryConfig = new AppFactoryConfigurationBuilder(fileLocation).buildAppFactoryConfiguration();
    }

    /**
     * TODO: FIXME: This method doesn't belong here. ( suggestion move to a different place)
     *
     * @param applicationId
     * @param version
     * @param revision
     * @param epr
     * @param payload
     */
    public static void sendNotification(final String applicationId, final String version, final String revision,
                                        final String epr, final OMElement payload) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
                try {
                    ServiceClient client = new ServiceClient();
                    client.getOptions().setTo(new EndpointReference(epr));
                    CarbonUtils.setBasicAccessSecurityHeaders(getAdminUsername(), getAdminPassword(), client);
                    client.sendRobust(payload);
                } catch (AxisFault e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static String getAdminUsername() {
        return appFactoryConfig.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME);
    }

    public static String getAdminPassword() {
        return appFactoryConfig.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD);
    }

    public static String getAdminEmail() {
        return appFactoryConfig.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_EMAIL);
    }

    public static String getMessageBrokerConnectionURL() {
        return appFactoryConfig.getFirstProperty(AppFactoryConstants.MESSAGE_BROKER_CONNECTION_URL);
    }


    public static Set<RoleBean> getRolePermissionConfigurations(String rolePermissionConfigPath, String defaultUser)
            throws AppFactoryException {
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        AppFactoryConfiguration configuration = getAppfactoryConfiguration();
        String[] roles = configuration.getProperties(rolePermissionConfigPath);
        if (roles == null || roles.length == 0) {
            log.warn("No roles permissions are configured for " + rolePermissionConfigPath + " path in appfactory.xml");
        } else {
            for (String role : roles) {
                String permissionIdString =
                        configuration.getFirstProperty(rolePermissionConfigPath + "." + role +
                                ".Permission");
                String[] permissionIds = permissionIdString.split(",");
                RoleBean roleBean = new RoleBean(role.trim());
                roleBean.addUser(defaultUser);
                for (String permissionId : permissionIds) {
                    permissionId = permissionId.trim();
                    boolean isDeniedPermission = permissionId.startsWith(AppFactoryConstants.DENY);
                    if (isDeniedPermission) {
                        permissionId = permissionId.substring(AppFactoryConstants.DENY.length(), permissionId.length());
                    }

                    String[] resourceAndActionParts = permissionId.split(":");
                    if (resourceAndActionParts.length == 2) {
                        Permission permission =
                                new Permission(
                                        resourceAndActionParts[0],
                                        replaceRegistryPermissionAction(resourceAndActionParts[1]));
                        roleBean.addPermission(permission, !isDeniedPermission);

                    } else if (resourceAndActionParts.length == 1) {
                        Permission permission =
                                new Permission(resourceAndActionParts[0],
                                        CarbonConstants.UI_PERMISSION_ACTION);
                        roleBean.addPermission(permission, !isDeniedPermission);
                    }
                }
                roleBeanList.add(roleBean);
            }
        }

        return roleBeanList;
    }

    /**
     * This is to replace registry action constants with short action names, to avoid urls as action
     *
     * @param action - REGISTRY_GET,REGISTRY_PUT,REGISTRY_DELETE or any other action
     * @return - replaced permission action for REGISTRY_ACTION
     */
    private static String replaceRegistryPermissionAction(String action) {
        if (AppFactoryConstants.REGISTRY_GET.equals(action)) {
            return ActionConstants.GET;
        } else if (AppFactoryConstants.REGISTRY_PUT.equals(action)) {
            return ActionConstants.PUT;
        } else if (AppFactoryConstants.REGISTRY_DELETE.equals(action)) {
            return ActionConstants.DELETE;
        } else {
            return action;
        }
    }

    public static void addRolePermissions(UserStoreManager userStoreManager, AuthorizationManager authorizationManager,
                                          Set<RoleBean> roleBeanList) throws UserStoreException {
        for (RoleBean roleBean : roleBeanList) {
            if (!userStoreManager.isExistingRole(roleBean.getRoleName())) {
                // add role and authorize given authorized permission list
                userStoreManager.addRole(roleBean.getRoleName(),
                        roleBean.getUsers().toArray(new String[roleBean.getUsers().size()]),
                        roleBean.getPermissions(true)
                                .toArray(new Permission[roleBean.getPermissions(true).size()]));
                if (log.isDebugEnabled()) {
                    StringBuilder permissionLog = new StringBuilder("Role:" + roleBean.getRoleName() + " is added with below permissions;");
                    List<Permission> permissions = roleBean.getPermissions(true);
                    for (Permission permission : permissions) {
                        permissionLog.append("resource:").append(permission.getResourceId()).append(" action:").append(permission.getAction()).append("\n");
                    }
                    log.debug(permissionLog.toString());
                }
            } else {
                // authorize given authorized permission list
                for (Permission permission : roleBean.getPermissions(true)) {
                    if (!authorizationManager.isRoleAuthorized(roleBean.getRoleName(), permission.getResourceId(),
                            permission.getAction())) {
                        authorizationManager.authorizeRole(roleBean.getRoleName(), permission.getResourceId(),
                                permission.getAction());
                        if (log.isDebugEnabled()) {
                            StringBuilder permissionLog = new StringBuilder("Role:" + roleBean.getRoleName() + " is authorized with permission;\n");
                            permissionLog.append("resource:").append(permission.getResourceId()).append(" action:").append(permission.getAction()).append("\n");
                            log.debug(permissionLog.toString());
                        }
                    }
                }
            }

            // deny given denied permission list
            for (Permission permission : roleBean.getPermissions(false)) {
                authorizationManager.denyRole(roleBean.getRoleName(), permission.getResourceId(),
                        permission.getAction());
                if (log.isDebugEnabled()) {
                    StringBuilder permissionLog = new StringBuilder("Role:" + roleBean.getRoleName() + " is denied with permissions;\n");
                    permissionLog.append("resource:").append(permission.getResourceId()).append(" action:").append(permission.getAction()).append("\n");
                    log.debug(permissionLog.toString());
                }
            }
        }

    }

    public static boolean checkAuthorizationForUser(String resource, String action) {
        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (currentUser != null) {
            try {
                AuthorizationManager authorizationManager =
                        CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                .getAuthorizationManager();
                return authorizationManager.isUserAuthorized(currentUser, resource, action);
            } catch (UserStoreException e) {
                log.warn("Error occurred when checking authorization", e);
                return false;
            }
        }

        return false;
    }

    public static boolean isAppRole(String role) {
        return role.startsWith(AppFactoryConstants.APP_ROLE_PREFIX);
    }

    public static String getRoleNameForApplication(String applicationKey) {
        return AppFactoryConstants.APP_ROLE_PREFIX + applicationKey;
    }

    public static String getAppkeyFromPerAppRoleName(String roleName) throws AppFactoryException {
        if (roleName != null && isAppRole(roleName)) {
            return roleName.replaceFirst(AppFactoryConstants.APP_ROLE_PREFIX, "");
        } else {
            String errorMsg = "Given role:" + roleName + " is not an unique application role given for each application.";
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

    }

    /**
     * <p>
     * Returns the previous life cycle stage given the current.
     * </p>
     * <b>NOTE: </b> This method relies on having following configuration in
     * appfactory.xml as oppose to considering how the governance life cycle is
     * configured
     * ( i.e. APPFACTORY_HOME/repository/resources/lifecycles).
     *
     * @param currentLifeCycleStage A valid stage name
     * @return The previous stage
     * @throws AppFactoryException an error
     */
    public static String getPreviousLifeCycleStage(String currentLifeCycleStage) throws AppFactoryException {
        return getAppfactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage." +
                currentLifeCycleStage + ".Demote.TargetStage");
    }

    /**
     * <p>
     * Returns the next life cycle stage given the current.
     * </p>
     * <p/>
     * * <b>NOTE: </b> This method relies on having following configuration in
     * appfactory.xml as oppose to considering how the governance life cycle is
     * configured
     * ( i.e. APPFACTORY_HOME/repository/resources/lifecycles/configurations.xml).
     *
     * @param currentLifeCycleStage A valid stage name
     * @return The next stage
     * @throws AppFactoryException an error
     */
    public static String getNextLifeCycleStage(String currentLifeCycleStage) throws AppFactoryException {
        return getAppfactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage." +
                currentLifeCycleStage + ".Promote.TargetStage");

    }

    /**
     * Returns true if the specified life cycle stage has no previous stage.
     *
     * @param lifeCycleStage Name of the stage
     * @return true if stage is an initial, false otherwise
     * @throws AppFactoryException an error
     */
    public static boolean isInitialLifeCycleStage(String lifeCycleStage) throws AppFactoryException {
        return getPreviousLifeCycleStage(lifeCycleStage) == null;
    }

    /**
     * Returns life cycle stages that have no previous stage. (e.g.
     * Development). All most all of the time this will return an array of
     * one element ( since AF will only have one initial stage).
     *
     * @return array of Strings with development stages
     * @throws AppFactoryException
     */
    public static String[] getInitialLifeCycleStages() throws AppFactoryException {

        String[] stages = getAppfactoryConfiguration().getProperties("ApplicationDeployment.DeploymentStage");
        List<String> initialStages = new ArrayList<String>(stages.length);

        for (String stg : stages) {
            if (isInitialLifeCycleStage(stg)) {
                initialStages.add(stg);
            }
        }

        return initialStages.toArray(new String[0]);
    }

	public static void setAuthHeaders(ServiceClient serviceClient, String username) throws AppFactoryException {
		// Set authorization header to service client
		List headerList = new ArrayList();
		Header header = new Header();
		header.setName(HTTPConstants.HEADER_AUTHORIZATION);
		header.setValue(getAuthHeader(username));
		headerList.add(header);
		serviceClient.getOptions().setProperty(HTTPConstants.HTTP_HEADERS, headerList);
	}

	public static String getAuthHeader(String username) throws AppFactoryException {

		//Get the filesystem keystore default primary certificate
		KeyStoreManager keyStoreManager;
		keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
		try {
			keyStoreManager.getDefaultPrimaryCertificate();
			JWSSigner signer = new RSASSASigner((RSAPrivateKey) keyStoreManager.getDefaultPrivateKey());
			JWTClaimsSet claimsSet = new JWTClaimsSet();
			claimsSet.setClaim(AppFactoryConstants.SIGNED_JWT_AUTH_USERNAME, username);
			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
			signedJWT.sign(signer);

			// generate authorization header value
			return "Bearer " + Base64Utils.encode(signedJWT.serialize().getBytes());
		} catch (SignatureException e) {
			String msg = "Failed to sign with signature instance";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (Exception e) {
			String msg = "Failed to get primary default certificate";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
	}



    /**
     * Get base access urls of remote servers for each stages configured in appfactory.xml
     *
     * @return stage, remote server url
     * @throws org.wso2.carbon.appfactory.common.AppFactoryException
     *          if failed to get appfactory.xml configuration details.
     */
    public static Map<String, String> getBaseAccessURLs() throws AppFactoryException {
        // this method can be improved if we need more information about environments. we have to introduce a bean
        Map<String, String> environmentDetails = new HashMap<String, String>();
        try {
            AppFactoryConfiguration appFactoryConfiguration = getAppfactoryConfiguration();
            String[] stages = appFactoryConfiguration.getProperties("ApplicationDeployment.DeploymentStage");
            if (stages != null) {
                for (String stage : stages) {
                    String baseAccessURL = appFactoryConfiguration.getFirstProperty("ApplicationDeployment.DeploymentStage." + stage + ".TenantMgtUrl");
                    environmentDetails.put(stage, baseAccessURL);
                }
            }
        } catch (AppFactoryException e) {
            log.error("Failed to get runtime environmental details.", e);
            throw new AppFactoryException("Failed to get runtime environmental details.", e);
        }
        return environmentDetails;
    }
}
