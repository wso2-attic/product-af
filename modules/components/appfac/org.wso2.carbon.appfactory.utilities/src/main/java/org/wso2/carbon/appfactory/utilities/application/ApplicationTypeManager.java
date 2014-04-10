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

package org.wso2.carbon.appfactory.utilities.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ApplicationTypeProcessor;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manage Application type information
 */
public class ApplicationTypeManager {
	private static Log log = LogFactory.getLog(ApplicationTypeManager.class);
	private Map<String, ApplicationTypeProcessor> applicationTypeProcessorMap;
	private static ApplicationTypeManager applicationTypeManager;
	private AppFactoryConfiguration configuration;

	private ApplicationTypeManager(AppFactoryConfiguration config) throws AppFactoryException {
		applicationTypeProcessorMap = new HashMap<String, ApplicationTypeProcessor>();
		this.configuration = config;
	}

	public void registerNewApplicationType(String applicationType, ApplicationTypeProcessor processor) {
		applicationTypeProcessorMap.put(applicationType, processor);
	}

	public ApplicationTypeProcessor getApplicationTypeProcessor(String applicationType) {
		return applicationTypeProcessorMap.get(applicationType);
	}

	public static ApplicationTypeManager getInstance() throws AppFactoryException {
		if (applicationTypeManager == null) {
			AppFactoryConfiguration config = ServiceReferenceHolder.getInstance().getAppFactoryConfiguration();
			applicationTypeManager = new ApplicationTypeManager(config);
			init(applicationTypeManager);
		}
		return applicationTypeManager;
	}

	public static void init(ApplicationTypeManager manager) throws AppFactoryException {

		String[] applicationTypes = manager.configuration.getProperties(AppFactoryConstants.APPLICATION_TYPE_CONFIG);

		for (String type : applicationTypes) {
			Properties properties = new Properties();
			String propertyStrings[] =
			                           manager.configuration.getProperties(AppFactoryConstants.APPLICATION_TYPE_CONFIG +
			                                                               "." + type + ".Property");

			for (String propertyName : propertyStrings) {
				String propertyKey =
				                     AppFactoryConstants.APPLICATION_TYPE_CONFIG + "." + type + ".Property." +
				                             propertyName;
				String propertyValue = manager.configuration.getFirstProperty(propertyKey);
				if (propertyValue != null) {
					properties.setProperty(propertyName, propertyValue);
				} else {
					log.warn("Property is not available in appfactory.xml : " + propertyKey);
				}
			}

			ApplicationTypeProcessor processor;
			String className = (String) properties.get("ProcessorClassName");
			try {
				Class<ApplicationTypeManager> clazz =
				                                      (Class<ApplicationTypeManager>) manager.getClass()
				                                                                             .getClassLoader()
				                                                                             .loadClass(className);
				Constructor constructor = clazz.getConstructor();
				processor = (ApplicationTypeProcessor) constructor.newInstance();
				processor.setDisplayName((String) properties.get("DisplayName"));
				processor.setFileExtension((String) properties.get("Extension"));
				processor.setName(type);
				processor.setDescreption((String) properties.get("Description"));
				processor.setBuildJobTemplate((String) properties.get("BuildJobTemplate"));
				processor.setProperties(properties);
				manager.applicationTypeProcessorMap.put(type, processor);
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
	}
}
