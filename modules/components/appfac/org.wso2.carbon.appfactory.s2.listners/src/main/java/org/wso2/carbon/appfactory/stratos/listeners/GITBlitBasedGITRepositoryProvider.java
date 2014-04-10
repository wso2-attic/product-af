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

package org.wso2.carbon.appfactory.stratos.listeners;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.IOException;

/**
 * GITBlit specific repository manager implementation for git
 */
public class GITBlitBasedGITRepositoryProvider implements RepositoryProvider {
    private static final Log log = LogFactory.getLog(GITBlitBasedGITRepositoryProvider.class);

    public static final String REPO_TYPE = "git";

    private String baseUrl;
    private String adminUsername;
    private String adminPassword;
    private String repoName;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String createRepository() throws AppFactoryException {
        String repoCreateUrl = baseUrl + "rpc?req=CREATE_REPOSITORY&name="
                               + repoName;

        //Create the gitblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = repoName;
        //authenticated users can clone, push and view the repository
        model.accessRestriction = com.gitblit.Constants.AccessRestrictionType.VIEW;
        try {
            boolean created = RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
                    adminPassword.toCharArray());
            if (created) {
                String url = getAppRepositoryURL();
                log.info("Created repository url " + url);

                return url;
            }else {
                String msg="Repository is not created for "+ repoName + " due to remote server error";
                log.error(msg);
                throw new AppFactoryException(msg);
            }
        } catch (IOException e) {
            String msg="Repository is not created for "+ repoName + " due to "+e.getLocalizedMessage();
            log.error(msg,e);
            throw new AppFactoryException(msg,e);
        }
    }

    public String getAppRepositoryURL() {
        return baseUrl + REPO_TYPE + "/" + repoName + ".git";
    }
}
