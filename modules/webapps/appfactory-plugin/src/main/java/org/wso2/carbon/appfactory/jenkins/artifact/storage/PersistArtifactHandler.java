/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appfactory.jenkins.artifact.storage;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of FileCallable to copy the artifact from
 * workspace to persistent storage at jenkins side
 */
public class PersistArtifactHandler implements FileCallable<Boolean> {

    private PersistArtifactHandlerConfig artifactHandlerConfig;

    public PersistArtifactHandler(PersistArtifactHandlerConfig artifactHandlerConfig) {
        this.artifactHandlerConfig = artifactHandlerConfig;
    }

    /**
     * This method will be invoked when we pass an object of this class to build.getWorkspace().act
     * method. Here the build is an AbstractBuild
     * @param file the workspace directory of the built job
     * @param channel
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Boolean invoke(File file, VirtualChannel channel)
            throws IOException, InterruptedException {
        String[] ext = {this.artifactHandlerConfig.getApplicationArtifactExtension()};
        if (file != null) {
            File target = (File) FileUtils.listFiles(file, ext, true).iterator().next();
            //there should be only one target
            //taking the first one,ignoring rest
            if (target != null) {
                if (new File(artifactHandlerConfig.getPersistentStoragePath()).exists()) {
                    //artifact will be stored at <storage-path>/<job-name>/<tag-Name>/artifact
                    File persistArtifact = new File(artifactHandlerConfig.
                                                    getPersistentStoragePath() + File.separator +
                                                    artifactHandlerConfig.getJobName() +
                                                    File.separator +
                                                    artifactHandlerConfig.getTagName());
                    //noinspection ResultOfMethodCallIgnored
                    persistArtifact.mkdirs();
                    copyArtifact(target.getAbsolutePath(), persistArtifact.getAbsolutePath() +
                                                           File.separator + target.getName());
                }
            }
        }
        return true;
    }

    private void copyArtifact(String source, String destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buffer = new byte[1024];

        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.close();
    }
}
    
