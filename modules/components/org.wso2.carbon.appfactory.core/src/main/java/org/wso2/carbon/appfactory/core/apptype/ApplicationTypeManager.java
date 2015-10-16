/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.apptype;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfigurationBuilder;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manage Application type information
 * Singleton class
 */
public class ApplicationTypeManager {
	private static final Log log = LogFactory.getLog(ApplicationTypeManager.class);
	private Map<String, ApplicationTypeBean> applicationTypeBeanMap = new HashMap<String, ApplicationTypeBean>();
	private static final ApplicationTypeManager applicationTypeManager = new ApplicationTypeManager();

	/**
	 * Constructor
	 */
	private ApplicationTypeManager() {
	}

	/**
	 * Getter for the application type bean for the specific type
	 * @param applicationType The type of the application. Ex:- war,jaxrs, jaxws
	 * @return ApplicationTypeBean
	 */
	public ApplicationTypeBean getApplicationTypeBean(String applicationType) {
		return getApplicationTypeBeanMap().get(applicationType);
	}

	/**
	 * Returns the current instance
	 * @return ApplicationTypeBean
	 * @throws AppFactoryException
	 */
	public static ApplicationTypeManager getInstance() throws AppFactoryException {
		return applicationTypeManager;
	}

	/**
	 * Add new app type from a apptype.xml
	 *
	 * @param apptype     apptype configuration file
	 * @param buildConfig build job configuration file
	 * @throws AppFactoryException
	 */
	public void addAppType(File apptype, File buildConfig) throws AppFactoryException, FileNotFoundException {

		Map<String, String> appTypeConfig = new AppFactoryConfigurationBuilder(apptype.getAbsolutePath())
				.loadConfigurationFile();
		OMElement buildTemplate;
		StAXOMBuilder builder;
		try {
			if (!buildConfig.exists()) {
				//get the default config
				if (log.isDebugEnabled()) {
					log.debug("getting the default jenkins-config since custom config is not available for apptype:" +
					          apptype.getName());
				}
				InputStream inputStream =
						this.getClass().getResourceAsStream(File.separator + AppFactoryConstants.JENKINS_JOB_CONFIG);
				builder = new StAXOMBuilder(inputStream);
				buildTemplate = builder.getDocumentElement();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("getting the custom jenkins-config since it is provided for apptype:"
					          + apptype.getName());
				}
				InputStream inputStream = new FileInputStream(buildConfig);
				builder = new StAXOMBuilder(inputStream);
				buildTemplate = builder.getDocumentElement();
			}
		} catch (XMLStreamException e) {
			String msg = "Error while reading apptype: " + apptype.getName();
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}
		initAppTypeFromConfig(appTypeConfig, buildTemplate);
	}

	/**
	 * Initialize the bean from the configuration
	 *
	 * @param config map of name value pairs from the content of the apptype.xml
	 * @param buildJob OMElement of the build job
	 * @throws AppFactoryException
	 */
	private void initAppTypeFromConfig(Map<String, String> config, OMElement buildJob) throws AppFactoryException {

		String type = config.get(AppFactoryConstants.APPLICATION_TYPE_CONFIG);
		Properties properties = new Properties();

		for (Map.Entry entry : config.entrySet()){
			if(entry.getValue() != null) {
				properties.put(entry.getKey(), entry.getValue());
			} else {
				log.warn("Property is not available in apptype configuration : " + entry.getKey());
			}
		}

		ApplicationTypeBean applicationTypeBean;
		String className = properties.getProperty(ApplicationTypeConstants.PROCESSOR_CLASS_NAME);
		try {
			Class<ApplicationTypeManager> clazz =
					(Class<ApplicationTypeManager>) applicationTypeManager.getClass()
					                                       .getClassLoader()
					                                       .loadClass(className);
			Constructor constructor = clazz.getConstructor(String.class);
			ApplicationTypeProcessor applicationTypeProcessor =
					(ApplicationTypeProcessor) constructor.newInstance(type);
			applicationTypeBean = new ApplicationTypeBean();
			applicationTypeBean.setApplicationTypeName(type);
            // Null checking in optional parameters.
            if (properties.getProperty(ApplicationTypeConstants.BUILDABLE) != null) {
                applicationTypeBean.setBuildable(Boolean.parseBoolean(properties.getProperty(
                        ApplicationTypeConstants.BUILDABLE)));
            }
			if(properties.getProperty(AppFactoryConstants.RUNTIMES) == null){
				throw new AppFactoryException("Cannot find the Runtimes for the Application Type :"+type);
			}
			applicationTypeBean.setRuntimes(properties.getProperty(AppFactoryConstants.RUNTIMES).
							split(AppFactoryConstants.PROPERTY_VALUE_SEPERATOR));
			applicationTypeBean.setDeployerClassName(properties.getProperty(AppFactoryConstants.DEPLOYER_CLASSNAME));
			applicationTypeBean.setUndeployerClassName(properties.getProperty(AppFactoryConstants.UNDEPLOYER_CLASSNAME));
			applicationTypeBean.setBuildJobTemplate(properties.getProperty(ApplicationTypeConstants.BUILD_JOB_TEMPLATE));
			applicationTypeBean.setComment(properties.getProperty(ApplicationTypeConstants.COMMENT));
            applicationTypeBean.setDescription(properties.getProperty(ApplicationTypeConstants.DESCRIPTION));
            applicationTypeBean.setDisplayName(properties.getProperty(ApplicationTypeConstants.DISPLAY_NAME));
            applicationTypeBean.setEnabled(properties.getProperty(ApplicationTypeConstants.ENABLE));
            if (properties.getProperty(ApplicationTypeConstants.EXTENSION) != null) {
                applicationTypeBean.setExtension(properties.getProperty(ApplicationTypeConstants.EXTENSION));
            }
            if (properties.getProperty(ApplicationTypeConstants.IS_UPLOADABLE_APP_TYPE) != null) {
                applicationTypeBean.setIsUploadableAppType(Boolean.parseBoolean(properties.getProperty(
                        ApplicationTypeConstants.IS_UPLOADABLE_APP_TYPE)));
            }
            applicationTypeBean.setServerDeploymentPath(properties.getProperty(
                    ApplicationTypeConstants.SERVER_DEPLOYMENT_PATHS));
            applicationTypeBean.setLaunchURLPattern(properties.getProperty(ApplicationTypeConstants.LAUNCH_URL_PATTERN));
            if (properties.getProperty(ApplicationTypeConstants.IS_ALLOW_DOMAIN_MAPPING) != null) {
                applicationTypeBean.setIsAllowDomainMapping(Boolean.parseBoolean(properties.getProperty(
                        ApplicationTypeConstants.IS_ALLOW_DOMAIN_MAPPING)));
            }
            if (properties.getProperty(ApplicationTypeConstants.IS_CODE_EDITOR_SUPPORTED) != null) {
                applicationTypeBean.setIsCodeEditorSupported(Boolean.parseBoolean(properties.getProperty(
                        ApplicationTypeConstants.IS_CODE_EDITOR_SUPPORTED)));
            }
			if(properties.getProperty(ApplicationTypeConstants.PERSIST_APPLICATION_ENDPOINT_METADATA) != null){
				applicationTypeBean.setPersistApplicationEndPointMetaData(
						Boolean.parseBoolean(properties.getProperty(
								ApplicationTypeConstants.PERSIST_APPLICATION_ENDPOINT_METADATA)));
			}
			applicationTypeBean.setJenkinsJobConfig(buildJob);
            applicationTypeBean.setExecutionType(properties.getProperty(ApplicationTypeConstants.EXECUTION_TYPE));
            applicationTypeBean.setIconColorClass(properties.getProperty(ApplicationTypeConstants.ICON_COLOR_CLASS));
            applicationTypeBean.setIconImageClass(properties.getProperty(ApplicationTypeConstants.ICON_IMAGE_CLASS));
			applicationTypeBean.setInitialDeployerClassName(properties.getProperty(ApplicationTypeConstants.INITIAL_DEPLOYER_CLASS_NAME));
            // We set the order here. This is used when displaying the apps in the UI
            // If there are no values for this, we give Integer.MAX_VALUE as the display order.
            // If there is an error in parsing the integer value, we should not stop the deployment.
            // Hence assigning Integer.MAX_VALUE if there were any exceptions
            if (properties.getProperty(ApplicationTypeConstants.DISPLAY_ORDER) != null) {
                try {
                    applicationTypeBean.setDisplayOrder(Integer.parseInt(properties.getProperty(
                            ApplicationTypeConstants.DISPLAY_ORDER)));
                } catch (NumberFormatException e) {
                    String msg = "Error in parsing the display order for apptype " + type;
                    log.error(msg, e);
                    applicationTypeBean.setDisplayOrder(Integer.MAX_VALUE);
                }
            } else {
                applicationTypeBean.setDisplayOrder(Integer.MAX_VALUE);
            }
            applicationTypeBean.setProperties(properties);
			applicationTypeProcessor.setProperties(properties);
			applicationTypeBean.setProcessor(applicationTypeProcessor);
			applicationTypeManager.getApplicationTypeBeanMap().put(type, applicationTypeBean);
		} catch (ClassNotFoundException e) {
			String msg = "Processor class " + className + " not found";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (NoSuchMethodException e) {
			String msg = "Processor class " + className + " not contains no-argument constructor";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (InvocationTargetException e) {
			String msg = "Error in invoking constructor of Processor " + className;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (InstantiationException e) {
			String msg = "Error in creating Processor object of " + className;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (IllegalAccessException e) {
			String msg = "Error in creating Processor object " + className;
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}

	}

	public Map<String, ApplicationTypeBean> getApplicationTypeBeanMap() {
		return applicationTypeBeanMap;
	}

	public void setApplicationTypeBeanMap(
			Map<String, ApplicationTypeBean> applicationTypeBeanMap) {
		this.applicationTypeBeanMap = applicationTypeBeanMap;
	}
}
