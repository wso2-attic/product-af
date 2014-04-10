package org.wso2.carbon.cloudmgt.users.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloudmgt.common.CloudConstants;
import org.wso2.carbon.cloudmgt.users.beans.RoleBean;
import org.wso2.carbon.cloudmgt.users.service.UserManagementException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.securevault.SecretManagerInitializer;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

/**
 * Util class for building app factory configuration
 */
public class AppFactoryConfigReader {
	private static final Log log = LogFactory.getLog(AppFactoryConfigReader.class);
	private static SecretResolver secretResolver;
	private static Map<String, List<String>> configurationMap = new HashMap<String, List<String>>();
	private static AppFactoryConfiguration appFactoryConfig = null;

	private AppFactoryConfigReader() throws UserManagementException {
		loadAppFactoryConfiguration();
	}

	public static File getApplicationWorkDirectory(String applicationId, String version, String revision)
	                                                                                                     throws UserManagementException {

		File tempDir = new File(CarbonUtils.getTmpDir() + File.separator + applicationId);
		return tempDir;
	}

	public static AppFactoryConfiguration getAppfactoryConfiguration() throws UserManagementException {
		if (appFactoryConfig == null) {
			loadAppFactoryConfiguration();
		}
		return appFactoryConfig;
	}

	private static void loadAppFactoryConfiguration() throws UserManagementException {
		OMElement appFactoryElement = loadAppFactoryXML();

		// Initialize secure vault
		SecretManagerInitializer secretManagerInitializer = new SecretManagerInitializer();
		secretManagerInitializer.init();
		secretResolver = SecretResolverFactory.create(appFactoryElement, true);

		if (!CloudConstants.CONFIG_NAMESPACE.equals(appFactoryElement.getNamespace().getNamespaceURI())) {
			String message =
			                 "AppFactory namespace is invalid. Expected [" + CloudConstants.CONFIG_NAMESPACE +
			                         "], received [" + appFactoryElement.getNamespace() + "]";
			log.error(message);
			throw new UserManagementException(message);
		}

		Stack<String> nameStack = new Stack<String>();
		readChildElements(appFactoryElement, nameStack);

		appFactoryConfig = new AppFactoryConfiguration(configurationMap);
	}

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

	private static OMElement loadAppFactoryXML() throws UserManagementException {
		String fileLocation =
		                      new StringBuilder().append(CarbonUtils.getCarbonConfigDirPath()).append(File.separator)
		                                         .append(CloudConstants.CONFIG_FOLDER).append(File.separator)
		                                         .append(CloudConstants.CONFIG_FILE_NAME).toString();

		File configFile = new File(fileLocation);
		InputStream inputStream = null;
		OMElement configXMLFile = null;
		try {
			inputStream = new FileInputStream(configFile);
			String xmlContent = IOUtils.toString(inputStream);
			configXMLFile = AXIOMUtil.stringToOM(xmlContent);
		} catch (IOException e) {
			String msg = "Unable to read the file " + CloudConstants.CONFIG_FILE_NAME + " at " + fileLocation;
			log.error(msg, e);
			throw new UserManagementException(msg, e);
		} catch (XMLStreamException e) {
			String msg = "Error in parsing " + CloudConstants.CONFIG_FILE_NAME;
			log.error(msg, e);
			throw new UserManagementException(msg, e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				String msg = "Error in closing stream ";
				log.error(msg, e);
			}
		}
		return configXMLFile;
	}

	private static void readChildElements(OMElement serverConfig, Stack<String> nameStack) {
		for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext();) {
			OMElement element = (OMElement) childElements.next();
			nameStack.push(element.getLocalName());

//			secureVaultResolve(element);

			String nameAttribute = element.getAttributeValue(new QName("name"));
			if (nameAttribute != null && nameAttribute.trim().length() != 0) {
				// We have some name attribute
				String key = getKey(nameStack);
				addToConfiguration(key, nameAttribute.trim());

				// all child element will be having this attribute as part of their name
				nameStack.push(nameAttribute.trim());
			}

			String enabledAttribute = element.getAttributeValue(new QName("enabled"));
			if (enabledAttribute != null && enabledAttribute.trim().length() != 0) {
				String key = getKey(nameStack) + ".Enabled";
				addToConfiguration(key, enabledAttribute.trim());

			}

			String text = element.getText();
			if (text != null && text.trim().length() != 0) {
				String key = getKey(nameStack);
				String value = replaceSystemProperty(text.trim());

				// Check wither the value is secured using secure valut
				if (isProtectedToken(key)) {
					value = getProtectedValue(key);
				}
				addToConfiguration(key, value);
			}
			readChildElements(element, nameStack);

			// If we had a named attribute, we have to pop that out
			if (nameAttribute != null && nameAttribute.trim().length() != 0) {
				nameStack.pop();
			}
			nameStack.pop();
		}
	}

	private static String getKey(Stack<String> nameStack) {
		StringBuffer key = new StringBuffer();
		for (int i = 0; i < nameStack.size(); i++) {
			String name = nameStack.elementAt(i);
			key.append(name).append(".");
		}
		key.deleteCharAt(key.lastIndexOf("."));

		return key.toString();
	}

	private static String replaceSystemProperty(String text) {
		int indexOfStartingChars = -1;
		int indexOfClosingBrace;

		// The following condition deals with properties.
		// Properties are specified as ${system.property},
		// and are assumed to be System properties
		while (indexOfStartingChars < text.indexOf("${") && (indexOfStartingChars = text.indexOf("${")) != -1 &&
		       (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a property used?

			// Get the system property name
			String sysProp = text.substring(indexOfStartingChars + 2, indexOfClosingBrace);

			// Resolve the system property name to a value
			String propValue = System.getProperty(sysProp);

			// If the system property is carbon home and is relative path,
			// we have to resolve it to absolute path
			if (sysProp.equals("carbon.home") && propValue != null && propValue.equals(".")) {
				propValue = new File(".").getAbsolutePath() + File.separator;
			}

			// Replace the system property with valid value
			if (propValue != null) {
				text = text.substring(0, indexOfStartingChars) + propValue + text.substring(indexOfClosingBrace + 1);
			}

		}
		return text;
	}

	private static boolean isProtectedToken(String key) {
		return secretResolver != null && secretResolver.isInitialized() &&
		       secretResolver.isTokenProtected("Carbon." + key);
	}

	private static String getProtectedValue(String key) {
		return secretResolver.resolve("Carbon." + key);
	}

	private static void addToConfiguration(String key, String value) {
		List<String> list = configurationMap.get(key);
		if (list == null) {
			list = new ArrayList<String>();
			list.add(value);
			configurationMap.put(key, list);
		} else {
			if (!list.contains(value)) {
				list.add(value);
			}
		}
	}

	public static String getAdminUsername() {
		return appFactoryConfig.getFirstProperty(CloudConstants.SERVER_ADMIN_NAME);
	}

	public static String getAdminPassword() {
		return appFactoryConfig.getFirstProperty(CloudConstants.SERVER_ADMIN_PASSWORD);
	}

//	private static void secureVaultResolve(OMElement element) {
//		String secretAliasAttr =
//		                         element.getAttributeValue(new QName(CloudConstants.SECURE_VAULT_NS,
//		                        		 CloudConstants.SECRET_ALIAS_ATTR_NAME));
//		if (secretAliasAttr != null) {
//			element.setText(loadFromSecureVault(secretAliasAttr));
//		}
//		// Iterator<OMElement> childEls = (Iterator<OMElement>) dbsElement.getChildElements();
//		// while (childEls.hasNext()) {
//		// this.secureVaultResolve(childEls.next());
//		// }
//	}
//
//	public static synchronized String loadFromSecureVault(String alias) {
//		if (secretResolver == null) {
//			secretResolver = SecretResolverFactory.create((OMElement) null, false);
//			secretResolver.init(AppFactoryCommonServiceComponent.getSecretCallbackHandlerService()
//			                                                    .getSecretCallbackHandler());
//		}
//		return secretResolver.resolve(alias);
//	}

	/**
	 * Check whether this artifact is buildable or non-buildable. It is defined
	 * in appfactory config file
	 *
	 * @param applicationType
	 * @return
	 * @throws AppFactoryException
	 */
	public static boolean isBuildable(String applicationType) throws UserManagementException {
		String appTypeBuildableElementKey =
				CloudConstants.APP_TYPE + "." + applicationType +
		                                            ".Property.Buildable";
		String appIsBuildalbe = appFactoryConfig.getFirstProperty(appTypeBuildableElementKey);
		if (appIsBuildalbe != null && !appIsBuildalbe.equals("")) {
			if (appIsBuildalbe.equalsIgnoreCase("true") || appIsBuildalbe.equalsIgnoreCase("yes")) {
				return true;
			} else if (appIsBuildalbe.equalsIgnoreCase("false") || appIsBuildalbe.equalsIgnoreCase("no")) {
				return false;
			}
			throw new UserManagementException("Invalid parameter value in appfactory.xml for buildable status of " +
			                              applicationType + " application type");
		}
		throw new UserManagementException("Buildable Parameter not defined or blank for " + applicationType +
		                              " application type");
	}

	public static Set<RoleBean> getRolePermissionConfigurations(String rolePermissionConfigPath, String defaultUser)
	                                                                                                                throws UserManagementException {
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
					boolean isDeniedPermission = permissionId.startsWith(CloudConstants.DENY);
					if (isDeniedPermission) {
						permissionId = permissionId.substring(CloudConstants.DENY.length(), permissionId.length());
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
	 * @param action
	 *            - REGISTRY_GET,REGISTRY_PUT,REGISTRY_DELETE or any other action
	 * @return - replaced permission action for REGISTRY_ACTION
	 */
	private static String replaceRegistryPermissionAction(String action) {
		if (CloudConstants.REGISTRY_GET.equals(action)) {
			return ActionConstants.GET;
		} else if (CloudConstants.REGISTRY_PUT.equals(action)) {
			return ActionConstants.PUT;
		} else if (CloudConstants.REGISTRY_DELETE.equals(action)) {
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
                if(log.isDebugEnabled()){
                    StringBuilder permissionLog = new StringBuilder("Role:"+roleBean.getRoleName() +" is added with below permissions;");
                    List<Permission> permissions = roleBean.getPermissions(true);
                    for(Permission permission:permissions){
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
                        if(log.isDebugEnabled()){
                            StringBuilder permissionLog = new StringBuilder("Role:"+roleBean.getRoleName() +" is authorized with permission;\n");
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
                if(log.isDebugEnabled()){
                    StringBuilder permissionLog = new StringBuilder("Role:"+roleBean.getRoleName() +" is denied with permissions;\n");
                    permissionLog.append("resource:").append(permission.getResourceId()).append(" action:").append(permission.getAction()).append("\n");
                    log.debug(permissionLog.toString());
                }
			}
		}

	}

	public static boolean checkAuthorizationForUser(String resource, String action) {
		String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
		// if thread local carbon context user is null, try to take from carbon context.
		if (currentUser == null) {
			currentUser = CarbonContext.getCurrentContext().getUsername();
		}
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
        return role.startsWith(CloudConstants.APP_ROLE_PREFIX);
    }

    public static String getRoleNameForApplication(String applicationKey) {
        return CloudConstants.APP_ROLE_PREFIX + applicationKey;
    }

    public static String getAppkeyFromPerAppRoleName(String roleName) throws UserManagementException {
        if (roleName != null && isAppRole(roleName)) {
            return roleName.replaceFirst(CloudConstants.APP_ROLE_PREFIX, "");
        } else {
            String errorMsg = "Given role:" + roleName + " is not an unique application role given for each application.";
            log.error(errorMsg);
            throw new UserManagementException(errorMsg);
        }

    }
}