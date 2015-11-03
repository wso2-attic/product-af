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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.RemoteRegistryService;
import org.wso2.carbon.appfactory.core.dao.JDBCResourceDAO;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ESBApplicationListener extends ApplicationEventsHandler {

    private static final Log log = LogFactory.getLog(ESBApplicationListener.class);
	private static final String APPLICATION_TYPE_ESB = "esb-car";
	private static final String ENDPOINT_MEDIA_TYPE = "application/vnd.wso2.esb.endpoint";
	private static final String WSDL_MEDIA_TYPE = "WSDL";
	private static final String ECHO_WSDL_RESOURCE_NAME = "echo.wsdl";
	private static final String ECHO_ENDPOINT_RESOURCE_NAME = "EchoServiceEP.xml";
	private static final String ECHO_ENDPOINT_RESOURCE_DESCRIPTION = "ESB default echo service endpoint";
	private static final String ECHO_WSDL_RESOURCE_DESCRIPTION = "Echo wsdl to publish";
	private static final String ENDPOINT_RESOURCE_TYPE = "Registry";
	private static final String AF_START_STAGE = "StartStage";

	private RemoteRegistryService appfactoryRemoteRegistryService = null;
	private InputStream endpointInputStream = null;
	private InputStream wsdlInputStream = null;
	private JDBCResourceDAO resourceDAO = null;

    public ESBApplicationListener(String identifier, int priority) {
        super(identifier, priority);
	    appfactoryRemoteRegistryService = ServiceReferenceHolder.getInstance().getAppfactoryRemoteRegistryService();
	    resourceDAO = JDBCResourceDAO.getInstance();
    }

    @Override
    public void onCreation(Application application, String userName, String tenantDomain, boolean isUploadableAppType)
		    throws AppFactoryException {
        if (APPLICATION_TYPE_ESB.equalsIgnoreCase(application.getType())) {

	        // Read files from resources of the component
	        ClassLoader classLoader = getClass().getClassLoader();
	        endpointInputStream = classLoader.getResourceAsStream(ECHO_ENDPOINT_RESOURCE_NAME);
	        wsdlInputStream = classLoader.getResourceAsStream(ECHO_WSDL_RESOURCE_NAME);

	        String serverURL = constructServerURL(application);

		        appfactoryRemoteRegistryService.putRegistryProperty(serverURL, userName, application.getId(),
		                                                            ECHO_ENDPOINT_RESOURCE_NAME,
		                                                            getStringFromInputStream(endpointInputStream),
		                                                            ECHO_ENDPOINT_RESOURCE_DESCRIPTION,
		                                                            ENDPOINT_MEDIA_TYPE, false);

	            resourceDAO.addResource(application.getId(), ECHO_ENDPOINT_RESOURCE_NAME, ENDPOINT_RESOURCE_TYPE,
	                                    AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AF_START_STAGE),
	                                    ECHO_ENDPOINT_RESOURCE_DESCRIPTION);

		        appfactoryRemoteRegistryService.putRegistryProperty(serverURL, userName, application.getId(),
		                                                            ECHO_WSDL_RESOURCE_NAME,
		                                                            getStringFromInputStream(wsdlInputStream),
		                                                            ECHO_WSDL_RESOURCE_DESCRIPTION,
		                                                            WSDL_MEDIA_TYPE, false);
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

		String startStage = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AF_START_STAGE);
		String serverURL =
				AppFactoryUtil.getAppfactoryConfiguration().
						getFirstProperty("ApplicationDeployment.DeploymentStage." + startStage + ".GregServerURL");

		return serverURL;
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			log.error("Error occured while reading the esb apptype resources", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("Error occured while closing the buffered reader for esb apptype resources", e);
				}
			}
		}

		return sb.toString();

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
