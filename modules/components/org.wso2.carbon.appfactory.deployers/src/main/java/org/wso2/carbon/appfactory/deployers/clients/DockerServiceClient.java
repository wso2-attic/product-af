/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */


package org.wso2.carbon.appfactory.deployers.clients;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import java.io.File;

/**
 * This docker client is used to call local docker deamon REST api and execute docker commands with java docker client
 */
public class DockerServiceClient {
    private static final Log log = LogFactory.getLog(DockerServiceClient.class);

    private DockerClient dockerClient;

    public DockerServiceClient(String registryApiUrl) {
        DockerClientConfig.DockerClientConfigBuilder builder =
                DockerClientConfig.createDefaultConfigBuilder().withServerAddress(registryApiUrl);
        DockerCmdExecFactory dockerCmdExecFactory  = DockerClientBuilder.getDefaultDockerCmdExecFactory();
        dockerClient = DockerClientBuilder.getInstance(builder).withDockerCmdExecFactory(dockerCmdExecFactory).build();
    }


    public void buildDockerImage(File dockerFile, String imageTagName) throws AppFactoryException {
        try {
            dockerClient.buildImageCmd(dockerFile).withTag(imageTagName).exec(new BuildImageResultCallback().awaitCompletion());
        } catch (InterruptedException e) {
            String msg = "Error while building docker image for image : " + imageTagName;
            log.error(msg, e);
            throw new AppFactoryException(e);

        }
    }

    public void pushDockerImage(String imageTagName) throws AppFactoryException {
        try {
            dockerClient.pushImageCmd(imageTagName).exec(new PushImageResultCallback().awaitCompletion());
        } catch (InterruptedException e) {
            String msg = "Error while pushing docker image for image : " + imageTagName;
            log.error(msg, e);
            throw new AppFactoryException(e);
        }
    }

}
