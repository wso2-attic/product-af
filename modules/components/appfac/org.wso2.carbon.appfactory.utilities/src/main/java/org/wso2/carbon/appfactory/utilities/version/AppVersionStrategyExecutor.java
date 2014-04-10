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

package org.wso2.carbon.appfactory.utilities.version;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Iterator;
import java.util.List;


/**
 * This class is to do the version of the different type of application that
 * support in AppFactory
 */
public class AppVersionStrategyExecutor {
    public static final Log log = LogFactory.getLog(AppVersionStrategyExecutor.class);

    /**
     * Common method to do the version for every type of projects
     *
     * @param targetVersion   next version of the repository
     * @param workDir         temp location that use to store the repository.
     * @param applicationType type of the application ex: war,car, dbs
     * @return
     */
    public boolean doVersion(String applicationId, String currentVersion, String targetVersion, File workDir, String applicationType) {

        if (AppFactoryConstants.FILE_TYPE_DBS.equals(applicationType)) {
            return doVersionForDBS(targetVersion, workDir);
        } else if (AppFactoryConstants.FILE_TYPE_BPEL.equals(applicationType)) {
            return doVersionForBPEL(applicationId, targetVersion, workDir);
        } else if (AppFactoryConstants.FILE_TYPE_ESB.equals(applicationType)) {
            return doVersionForESB(currentVersion, targetVersion, workDir);
        } else if (AppFactoryConstants.FILE_TYPE_PHP.equals(applicationType)) {
            return doVersionForPHP(targetVersion, workDir);
        } else {
            return doVersionForMVN(targetVersion, workDir);
        }
    }


    public static boolean doVersionForESB(String currentVersion, String targetVersion, File workDir) {
        //doVersionOnSynapseXML(currentVersion,targetVersion, workDir);
        return true;
    }

    /**
     * Version of DataService
     *
     * @param targetVersion
     * @param workDir
     * @return
     */

    public static boolean doVersionForPHP(String targetVersion, File workDir) {
        for (File file : workDir.listFiles()) {
            if (file.isDirectory() && file.getName().contains("-")) {
                String newName = changeFileName(file.getName(), targetVersion);
                File newFile =
                        new File(file.getAbsolutePath().replace(file.getName(),
                                newName));
                file.renameTo(newFile);
            }
        }
        return true;
    }

    /**
     * Version of DataService
     *
     * @param targetVersion
     * @param workDir
     * @return
     */
    public static boolean doVersionForDBS(String targetVersion, File workDir) {

        try {
            String[] fileExtension = {"dbs"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir, fileExtension, true);

            for (File file : fileList) {

                if (file.getName().endsWith(".dbs")) {
                    FileInputStream stream = new FileInputStream(file);
                    String xmlContent = IOUtils.toString(stream);
                    OMElement configXMLFile = AXIOMUtil.stringToOM(xmlContent);

                    String str = configXMLFile.getAttribute(new QName("name")).getAttributeValue();

                    String newName = changeFileName(str, targetVersion);
                    configXMLFile.getAttribute(new QName("name")).setAttributeValue(newName);

                    if (stream != null) {
                        stream.close();
                    }

                    String newFileName = newName + ".dbs";

                    File newFile =
                            new File(file.getAbsolutePath().replace(file.getName(),
                                    newFileName));
                    file.renameTo(newFile);

                    FileWriter writer = new FileWriter(newFile);
                    writer.write(configXMLFile.toString());
                    writer.close();

                    return true;

                }
            }
        } catch (Exception e) {
            String errorMsg = "Error in process of version in DataService : " + e.getMessage();
            log.error(errorMsg, e);

        }
        return false;
    }

    /**
     * Version of BPEL
     *
     * @param targetVersion
     * @param workDir
     * @return
     */
    public static boolean doVersionForBPEL(String applicationId, String targetVersion, File workDir) {
        doVersionOnBPEL(applicationId, targetVersion, workDir);
        return true;
    }

    /**
     * Version of generic mvn type project
     *
     * @param targetVersion
     * @param workDir
     * @return
     */
    public static boolean doVersionForMVN(String targetVersion, File workDir) {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model;
        try {
            String[] fileExtension = {"xml"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir, fileExtension, true);
            for (File file : fileList) {

                if (file.getName().equals("pom.xml")) {
                    FileInputStream stream = new FileInputStream(file);
                    model = mavenXpp3Reader.read(stream);
                    model.setVersion(targetVersion);
                    if (stream != null) {
                        stream.close();
                    }
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    writer.write(new FileWriter(file), model);
                    return true;
                }
            }
        } catch (Exception e) {
            String errorMsg = "Error in process of version in common Mvn project : " + e.getMessage();
            log.error(errorMsg, e);
        }
        return false;
    }

    public static void doVersionOnBPEL(String applicationId, String targetVersion, File workDir) {
        //change in pom.xml file
        doVersionOnPOM(targetVersion, workDir);
        //change in .bpel file
        doVersionOnBPELFile(targetVersion, workDir);
        //change in .wsdl file
        doVersionOnWSDLFile(applicationId, targetVersion, workDir);
        //change in deploy.xml file
        doVersionOnDeployXML(targetVersion, workDir);
    }

    private static void doVersionOnDeployXML(String targetVersion, File workDir) {
        try {
            String[] fileExtension = {"xml"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir,
                    fileExtension, true);
            for (File file : fileList) {
                if (file.getName().equals("deploy.xml")) {
                    OMElement deployXml = loadXML(file);
                    Iterator processIterator = deployXml.getChildrenWithName(new QName("process"));
                    while (processIterator.hasNext()) {
                        OMElement processElement = (OMElement) processIterator.next();
                        if (processElement.getQName().getLocalPart().equals("process")) {
                            String processName = processElement.getAttribute(new QName("name")).getAttributeValue();
                            processElement.getAttribute(new QName("name")).setAttributeValue(changeFileName(processName, targetVersion));
                            Iterator provideIterator = processElement.getChildrenWithName(new QName("provide"));
                            while (provideIterator.hasNext()) {
                                OMElement provideElement = (OMElement) provideIterator.next();
                                Iterator serviceIterator = provideElement.getChildrenWithName((new QName("service")));
                                while (serviceIterator.hasNext()) {
                                    OMElement serviceElement = (OMElement) serviceIterator.next();
                                    if (serviceElement.getQName().getLocalPart().equals("service")) {
                                        String oldName1 = serviceElement.getAttribute(new QName("name")).getAttributeValue();
                                        serviceElement.getAttribute(new QName("name")).setAttributeValue(changeFileName(oldName1, targetVersion));
                                    }
                                }
                            }
                        }
                    }
                    writeXMLToFile(deployXml, file.getAbsolutePath());
                }

            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    private static void doVersionOnWSDLFile(String applicationId, String targetVersion, File workDir) {
        try {
            String[] fileExtension = {"wsdl"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir,
                    fileExtension, true);
            for (File file : fileList) {
                OMElement wsdl = loadXML(file);
                Iterator wsdlIterator = wsdl.getChildElements();
                while (wsdlIterator.hasNext()) {
                    OMElement wsdlElement = (OMElement) wsdlIterator.next();
                    if (wsdlElement.getQName().getLocalPart().equals("service")) {
                        String serviceName = wsdlElement.getAttribute(new QName("name")).getAttributeValue();
                        if (serviceName.startsWith(applicationId)){
                        	wsdlElement.getAttribute(new QName("name")).setAttributeValue(changeFileName(serviceName, targetVersion));
                        }
                    }
                }
                writeXMLToFile(wsdl, file.getAbsolutePath());
            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    private static void doVersionOnBPELFile(String targetVersion, File workDir) {
        try {
            String[] fileExtension = {"bpel"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir,
                    fileExtension, true);
            for (File file : fileList) {
                OMElement bpelProcess = loadXML(file);
                String processName = bpelProcess.getAttribute(new QName("name")).getAttributeValue();
                bpelProcess.getAttribute(new QName("name")).setAttributeValue(changeFileName(processName, targetVersion));
                writeXMLToFile(bpelProcess, file.getAbsolutePath());
            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    private static void doVersionOnPOM(String targetVersion, File workDir) {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model;

        try {
            String[] fileExtension = {"xml"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir,
                    fileExtension, true);

            for (File file : fileList) {

                if (file.getName().equals("pom.xml")) {
                    FileInputStream stream = new FileInputStream(file);
                    model = mavenXpp3Reader.read(stream);
                    model.setVersion(targetVersion);
                    if (stream != null) {
                        stream.close();
                    }
                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    writer.write(new FileWriter(file), model);
                }
            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    private static OMElement loadXML(File configFile) throws AppFactoryException {

        InputStream inputStream = null;
        OMElement configXMLFile = null;
        try {
            inputStream = new FileInputStream(configFile);
            String xmlContent = IOUtils.toString(inputStream);
            configXMLFile = AXIOMUtil.stringToOM(xmlContent);
        } catch (IOException e) {
            String msg = "Unable to read the file " + configFile.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error in parsing " + configFile.getName();
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String msg = "Error in closing stream ";
                log.error(msg, e);
            }
        }
        return configXMLFile;
    }

    private static boolean writeXMLToFile(OMElement element, String fileName) throws AppFactoryException {
        File destinationFile = new File(fileName);
        FileWriter writer = null;
        try {
            writer = new FileWriter(destinationFile);
            element.serialize(writer);
        } catch (IOException e) {
            String msg = "Error in writing to " + fileName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error in parsing " + fileName;
            log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }

    private static String changeFileName(String name, String changedVersion) {

        String applicationName = name;
        if (name.lastIndexOf("-") != -1) {
            applicationName = name.substring(0, name.lastIndexOf("-"));
        }
        String newFileName = applicationName + "-" + changedVersion;
        return newFileName;
    }


    private void doVersionOnSynapseXML(String currentVersion, String targetVersion, File workDir) {

        if (currentVersion.equals("trunk")) {
            currentVersion = "SNAPSHOT";
        }

        try {
            String[] fileExtension = {"xml"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir,
                    fileExtension, true);
            for (File file : fileList) {
                if (file.getName().equals("synapse.xml")) {
                    OMElement deployXml = loadXML(file);
                    Iterator proxyItemIterator = deployXml.getChildrenWithName(new QName("proxy"));

                    while (proxyItemIterator.hasNext()) {
                        OMElement processElement = (OMElement) proxyItemIterator.next();
                        if (processElement.getAttribute(new QName("name")) != null && processElement.getAttribute(new QName("name")).getAttributeValue().endsWith(currentVersion)) {
                            //OMElement cloneElement = processElement.cloneOMElement();
                            String name = processElement.getAttribute(new QName("name")).getAttributeValue();
                            String newName = name.replace(currentVersion, targetVersion);
                            processElement.getAttribute(new QName("name")).setAttributeValue(newName);
                            //processElement.getParent().addChild(cloneElement);
                            break;
                        }
                    }
                    Iterator sequenceItemIterator = deployXml.getChildrenWithName(new QName("sequence"));
                    while (sequenceItemIterator.hasNext()) {
                        OMElement processElement = (OMElement) sequenceItemIterator.next();
                        if (processElement.getAttribute(new QName("name")) != null && processElement.getAttribute(new QName("name")).getAttributeValue().endsWith(currentVersion)) {
                            //OMElement cloneElement = processElement.cloneOMElement();
                            String name = processElement.getAttribute(new QName("name")).getAttributeValue();
                            String newName = name.replace(currentVersion, targetVersion);
                            processElement.getAttribute(new QName("name")).setAttributeValue(newName);
                            //processElement.getParent().addChild(cloneElement);
                            break;
                        }
                    }
                    writeXMLToFile(deployXml, file.getAbsolutePath());
                }

            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

}
