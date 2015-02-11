/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.repository.provider.common;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.repository.mgt.BranchingStrategy;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.provider.common.bean.Permission;
import org.wso2.carbon.appfactory.repository.provider.common.bean.Repository;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * Repository Provider to be used by different SCM providers
 */
public abstract class AbstractRepositoryProvider implements RepositoryProvider {

    public static final Log log = LogFactory.getLog(AbstractRepositoryProvider.class);
    public AppFactoryConfiguration config;
    private BranchingStrategy branchingStrategy;
    protected AppfactoryRepositoryClient appfactoryRepositoryClient;
    //rest URIs
    public static final String REST_BASE_URI = "/api/rest";
    public static final String REST_CREATE_REPOSITORY_URI = "/repositories";

    //repositories xml elements
    public static final String REPOSITORY_XML_ROOT_ELEMENT = "repositories";
    public static final String REPOSITORY_NAME_ELEMENT = "name";
    public static final String REPOSITORY_TYPE_ELEMENT = "type";
    public static final String REPOSITORY_URL_ELEMENT = "url";

    //permission xml elements
    public static final String PERMISSION_XML_ROOT_ELEMENT = "permissions";
    public static final String PERMISSION_TYPE_ELEMENT = "type";
    public static final String PERMISSION_NAME_ELEMENT = "name";
    public static final String PERMISSION_GROUP_PERMISSION_ELEMENT = "groupPermission";

    //    These constants should be taken from AppfactoryConstants.
    private static final String REPOSITORY_PROVIDER_CONFIG_PREFIX = "RepositoryProviderConfig.";
    private static final String SCM_ADMIN_USER_NAME_SUFFIX = ".Property.AdminUserName";
    private static final String SCM_ADMIN_PASSWORD_SUFFIX = ".Property.AdminPassword";


    protected HttpClient getClient() {
        HttpClient client = new HttpClient();
        String userName = config.getFirstProperty((AppFactoryConstants.SCM_ADMIN_NAME).
                replace("{@type}", getType()));
        String password = config.getFirstProperty((AppFactoryConstants.SCM_ADMIN_PASSWORD).
                replace("{@type}", getType()));
        AuthScope authScope = AuthScope.ANY;
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        client.getState().setCredentials(authScope, credentials);
        return client;
    }

    protected String getServerURL() {
        return config.getFirstProperty((AppFactoryConstants.SCM_SERVER_URL).replace("{@type}", getType()));
    }

    @Override
    public void setConfiguration(AppFactoryConfiguration configuration) {
        this.config = configuration;
    }

    public AppFactoryConfiguration getConfig() {
        return config;
    }

    protected byte[] getRepositoryAsString(Repository repo) throws RepositoryMgtException {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement repository = factory.createOMElement(REPOSITORY_XML_ROOT_ELEMENT, null);
        OMElement name = factory.createOMElement(REPOSITORY_NAME_ELEMENT, null);
        name.setText(repo.getName());
        repository.addChild(name);
        OMElement type = factory.createOMElement(REPOSITORY_TYPE_ELEMENT, null);
        type.setText(repo.getType());
        repository.addChild(type);

        for (Permission perm : repo.getPermissions()) {

            OMElement permission = factory.createOMElement(PERMISSION_XML_ROOT_ELEMENT, null);
            OMElement groupPermission = factory.createOMElement(PERMISSION_GROUP_PERMISSION_ELEMENT, null);
            groupPermission.setText(String.valueOf(perm.getGroupPermission()));
            permission.addChild(groupPermission);

            OMElement permName = factory.createOMElement(PERMISSION_NAME_ELEMENT, null);
            permName.setText(perm.getName());
            OMElement permType = factory.createOMElement(PERMISSION_TYPE_ELEMENT, null);
            permType.setText(perm.getType().toString());

            permission.addChild(permName);
            permission.addChild(permType);
            repository.addChild(permission);
        }
        StringWriter writer = new StringWriter();
        try {
            repository.serialize(writer);
        } catch (XMLStreamException e) {
            String msg = "Error while serializing the payload";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                String msg = "Error while closing the reader";
                log.error(msg, e);
            }
        }
        return writer.toString().getBytes();
    }

    protected Repository getRepositoryFromStream(InputStream responseBodyAsStream)
            throws RepositoryMgtException {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;
        Repository repository = new Repository();
        try {
            reader = xif.createXMLStreamReader(responseBodyAsStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement rootElement = builder.getDocumentElement();
            if (REPOSITORY_XML_ROOT_ELEMENT.equals(rootElement.getLocalName())) {
                Iterator elements = rootElement.getChildElements();
                while (elements.hasNext()) {
                    Object object = elements.next();
                    if (object instanceof OMElement) {
                        OMElement element = (OMElement) object;
                        if (REPOSITORY_URL_ELEMENT.equals(element.getLocalName())) {
                            repository.setUrl(element.getText());
                            break;
                        }
                    }
                }
            } else {
                String msg = "In the payload no repository information is found";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (XMLStreamException e) {
            String msg = "Error while reading the stream";
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                String msg = "Error while serializing the payload";
                log.error(msg, e);
            }
        }
        return repository;
    }


    /**
     * Right now 3 of the 4 configurations use the same pattern to describe the repository provider config
     * The user name and the password properties are "AdminUserName" and "AdminPassword" respectively
     * For those kinds of configurations, this method will initialize the client.
     * If someone changes those property names, then they should override this method in their class.
     */
    @Override
    public AppfactoryRepositoryClient getRepositoryClient() throws RepositoryMgtException {
        String userName = config.getFirstProperty((AppFactoryConstants.SCM_ADMIN_NAME).
                replace("{@type}", getType()));
        String password = config.getFirstProperty((AppFactoryConstants.SCM_ADMIN_PASSWORD).
                replace("{@type}", getType()));
        appfactoryRepositoryClient.init(userName, password);
        return this.appfactoryRepositoryClient;
    }

    @Override
    public BranchingStrategy getBranchingStrategy() {
        return this.branchingStrategy;
    }

    @Override
    public void setAppfactoryRepositoryClient(AppfactoryRepositoryClient client) {
        this.appfactoryRepositoryClient = client;
    }

    @Override
    public void setBranchingStrategy(BranchingStrategy branchingStrategy) {
        this.branchingStrategy = branchingStrategy;
    }

    @Override
    public void provisionUser(String applicationKey, String username)
            throws RepositoryMgtException {
    }

    abstract protected String getType();
}
