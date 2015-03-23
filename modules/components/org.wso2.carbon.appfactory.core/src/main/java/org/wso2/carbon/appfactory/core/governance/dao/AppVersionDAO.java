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

package org.wso2.carbon.appfactory.core.governance.dao;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.util.GovernanceUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * Contains the CRUD operations done on Resources created via appversion.rxt Meta Data Model
 */
public class AppVersionDAO {
    private static AppVersionDAO appVersionDAO = new AppVersionDAO();

    private AppVersionDAO() {
    }

    public static AppVersionDAO getInstance() {
        return appVersionDAO;
    }

    public String addArtifact(String info, String lifecycleAttribute) throws AppFactoryException {
        try {
            UserRegistry userRegistry = GovernanceUtil.getUserRegistry();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLStreamReader reader = null;

            reader = factory.createXMLStreamReader(new StringReader(info));


            GenericArtifactManager manager = new GenericArtifactManager(userRegistry, "appversion");
            GenericArtifact artifact = manager.newGovernanceArtifact(
                    new StAXOMBuilder(reader).getDocumentElement());

            // want to save original content, so set content here
            artifact.setContent(info.getBytes());

            manager.addGenericArtifact(artifact);
            if (lifecycleAttribute != null) {
                String lifecycle = artifact.getAttribute(lifecycleAttribute);
                if (lifecycle != null) {
                    artifact.attachLifecycle(lifecycle);
                }
            }
            return "/_system/governance"+ artifact.getPath();
        } catch (Exception e) {
            String errorMsg = "Error adding artifact";
            throw new AppFactoryException(errorMsg, e);
        }
    }
}

