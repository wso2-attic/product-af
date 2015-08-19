package org.wso2.carbon.appfactory.utilities.esb;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.RemoteRegistryService;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;

import java.io.File;
import java.io.IOException;

public class ESBApplicationListener extends ApplicationEventsHandler {

    private static final Log log = LogFactory.getLog(ESBApplicationListener.class);
	private static final String APPLICATION_TYPE_ESB = "esb-car";
	private static final String ENDPOINT_MEDIA_TYPE = "application/vnd.wso2.esb.endpoint";
	private static final String WSDL_MEDIA_TYPE = "WSDL";
	private static final String ECHO_WSDL_RESOURCE_NAME = "echo.wsdl";
	private static final String ECHO_ENDPOINT_RESOURCE_NAME = "EchoServiceEP.xml";
	private static final String ECHO_ENDPOINT_RESOURCE_DESCRIPTION = "ESB default echo service endpoint";
	private static final String ECHO_WSDL_RESOURCE_DESCRIPTION = "Echo wsdl to publish";
	private static final String PARAM_APP_STAGE = "{stage}";
	private static final String PARAM_APP_STAGE_NAME_SUFFIX = "StageParam";
	private static final String AF_START_STAGE = "StartStage";

	private RemoteRegistryService appfactoryRemoteRegistryService = null;
	private File echoEndpointFile = null;
	private File echoWsdlFile = null;

    public ESBApplicationListener(String identifier, int priority) {
        super(identifier, priority);
	    appfactoryRemoteRegistryService = ServiceReferenceHolder.getInstance().getAppfactoryRemoteRegistryService();
	    // Read files from resources of the component
	    ClassLoader classLoader = getClass().getClassLoader();
	    echoEndpointFile = new File(classLoader.getResource(ECHO_ENDPOINT_RESOURCE_NAME).getFile());
	    echoWsdlFile = new File(classLoader.getResource(ECHO_WSDL_RESOURCE_NAME).getFile());
    }

    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType)
		    throws AppFactoryException {
        if (APPLICATION_TYPE_ESB.equalsIgnoreCase(application.getType())) {

	        String serverURL = constructServerURL(application);

	        //Create resources
	        try {
		        appfactoryRemoteRegistryService.putRegistryProperty(serverURL, userName, application.getId(),
		                                                            ECHO_ENDPOINT_RESOURCE_NAME,
		                                                            FileUtils.readFileToString(echoEndpointFile),
		                                                            ECHO_ENDPOINT_RESOURCE_DESCRIPTION,
		                                                            ENDPOINT_MEDIA_TYPE, false);
	        } catch (IOException e) {
		        log.error("Reading endpoint file failed", e);
		        //Not throwing exceptions since this will only fail launching the sample application
	        }
	        try {
		        appfactoryRemoteRegistryService.putRegistryProperty(serverURL, userName, application.getId(),
		                                                            ECHO_WSDL_RESOURCE_NAME,
		                                                            FileUtils.readFileToString(echoWsdlFile),
		                                                            ECHO_WSDL_RESOURCE_DESCRIPTION,
		                                                            WSDL_MEDIA_TYPE, false);
	        } catch (IOException e) {
		        log.error("Reading wsdl file failed", e);
		        //Not throwing exceptions since this will only fail launching the sample application
	        }
        }
    }

	@Override
    public void onDeletion(Application application, String userName, String tenantDomain) throws AppFactoryException {
        if (APPLICATION_TYPE_ESB.equalsIgnoreCase(application.getType())) {

	        String serverURL = constructServerURL(application);
	        
	        //Sending the request with empty content for deletion
	        appfactoryRemoteRegistryService.deleteRegistryResource(serverURL, userName, application.getId(),
	                                                               ECHO_ENDPOINT_RESOURCE_NAME, "", "",
	                                                               ENDPOINT_MEDIA_TYPE, false);
	        appfactoryRemoteRegistryService.deleteRegistryResource(serverURL, userName, application.getId(),
	                                                               ECHO_WSDL_RESOURCE_NAME, "", "",
	                                                               WSDL_MEDIA_TYPE, false);
        }

    }

	private String constructServerURL(Application application) throws AppFactoryException {
		RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(
				ApplicationTypeManager.getInstance().getApplicationTypeBean(application.getType()).getRuntimes()[0]);
		String serverURL = runtimeBean.getServerURL();
		String urlStageValue = "";

		try {
			urlStageValue = runtimeBean.getProperty(AppFactoryUtil.getAppfactoryConfiguration().
					getFirstProperty(AF_START_STAGE) + PARAM_APP_STAGE_NAME_SUFFIX);
		} catch (Exception e){
			// no need to throw just log and continue
			log.warn("Error while getting the url stage value fo application:" + application.getId(), e);
		}

		if(urlStageValue == null){
			urlStageValue = "";
		}

		serverURL = serverURL.replace(PARAM_APP_STAGE, urlStageValue);
		return serverURL;
	}

    @Override
    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain,
                                  String userName) throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLifeCycleStageChange(Application application, Version version,
                                       String previosStage, String nextStage, String tenantDomain)
            throws AppFactoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasExecuted(Application application, String userName, String tenantDomain)
		    throws AppFactoryException {
	    //To change body of implemented methods use File | Settings | File Templates.
        return true;
    }

    @Override
    public void onFork(Application application, String userName, String tenantDomain, String version,
                       String[] forkedUsers) throws AppFactoryException {
	    //To change body of implemented methods use File | Settings | File Templates.
    }
}
