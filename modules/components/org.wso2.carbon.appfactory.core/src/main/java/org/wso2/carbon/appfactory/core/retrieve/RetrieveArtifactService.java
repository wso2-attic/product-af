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

package org.wso2.carbon.appfactory.core.retrieve;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

public class RetrieveArtifactService {
    private static final Log log = LogFactory.getLog(RetrieveArtifactService.class);

    /**
     *
     * @param applicationId Application ID
     * @param version Application Version
     * @param revision Application revision
     * @param tenantDomain Tenant domain of application
     * @return {@link DataHandler}
     */
    public DataHandler retrieveArtifact(String applicationId, String version, String revision, String tenantDomain) {
        String fileName = null;
        File file = null;
        ArtifactStorage storage = ServiceHolder.getArtifactStorage();

        try {
            file = storage.retrieveArtifact(applicationId, version, revision, tenantDomain);
            fileName = file.getAbsolutePath();

        } catch (AppFactoryException e) {
            e.printStackTrace();
        }

        FileDataSource dataSource = new FileDataSource(fileName);
        DataHandler fileDataHandler = new DataHandler(dataSource);
        return fileDataHandler;
    }

    /**
     *
     * @param applicationId Application ID
     * @param version Application Version
     * @param revision Application revision
     * @param tenantDomain Tenant domain of application
     * @return Arifact details
     */
    public String retrieveArtifactId(String applicationId, String version, String revision, String tenantDomain) {
        String fileName = null;
        String entryName = null;
        File file = null;
        ArtifactStorage storage = ServiceHolder.getArtifactStorage();
        String artifactDetails = null;


        try {
            file = storage.retrieveArtifact(applicationId, version, revision, tenantDomain);
              if (file == null) {
                return "Not Found";
            } else if ((file.getName()).endsWith(".war")) {
                String artifactVersion = (file.getName()).substring(file.getName().indexOf('-') + 1, file.getName().indexOf(".war"));
                String artifactName = file.getName().substring(0, (file.getName().indexOf('-')));
                artifactDetails = artifactName + '-' + artifactVersion;
                //System.out.print(artifactDetails);
                return artifactDetails;

            } else if ((file.getName()).endsWith(".car")){
                fileName = file.getAbsolutePath();
                FileInputStream fin = null;
                fin = new FileInputStream(fileName);

                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry zentry = null;

                while ((zentry = zin.getNextEntry()) != null) {
                    if (!(zentry.getName().equals("artifacts.xml"))) {

                        //  byte[] buf = new byte[1024];
                        entryName = zentry.getName();
                        log.info("Name of  Zip Entry : " + entryName);
                        String artifactVersion = entryName.substring(entryName.indexOf('_') + 1);
                        String artifactName = entryName.substring(0, (entryName.indexOf('_')));
                        zin.close();
                        fin.close();

                        artifactDetails = artifactName + '-' + artifactVersion;
                        return artifactDetails;
                    }

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AppFactoryException e) {
            e.printStackTrace();
        }
        return artifactDetails;

    }
}











