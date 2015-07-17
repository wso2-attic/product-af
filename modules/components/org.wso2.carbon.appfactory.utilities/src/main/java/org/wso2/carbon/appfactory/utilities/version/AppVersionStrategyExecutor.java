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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.file.FileUtilities;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is to do the version of the different type of application that
 * support in AppFactory
 */
public class AppVersionStrategyExecutor {
    public static final Log log = LogFactory.getLog(AppVersionStrategyExecutor.class);


    public static boolean doVersionForESB(String currentVersion, String targetVersion, File workDir) {
        doVersionOnSynapseXML(currentVersion,targetVersion, workDir);
        return true;
    }

    /**
     * Version of applications which do not use a pom file
     *
     * @param targetVersion
     * @param workDir
     * @return
     */

    public static boolean doVersionForGenericApplicationType(String targetVersion, File workDir) {
        for (File file : workDir.listFiles()) {
            if (file.isDirectory() && file.getName().contains("-")) {
                String newName = changeArtifactName(file.getName(), targetVersion);
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

                    String newName = changeArtifactName(str, targetVersion);
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

	/**
	 * Version of generic multi module mvn mvn type project
	 *
	 * @param targetVersion value of the version we are going to create
	 * @param workDir current working directory
	 */
	public static void doVersionForMultiModuleMVN(String targetVersion, File workDir) {
		ArrayList<String> artifactIds = new ArrayList<String>();
		try {
			List<File> pomFileList = new ArrayList<File>();
			FileUtilities.searchFiles(workDir, AppFactoryConstants.DEFAULT_POM_FILE, pomFileList);
			getArtifactIdsFromPOMFiles(pomFileList, artifactIds);
			changeVersionsInPomFiles(targetVersion, artifactIds, pomFileList);
		} catch (Exception e) {
			String errorMsg = "Error in process of version in multi module Mvn project : " + e.getMessage();
			log.error(errorMsg, e);
		}
	}

	/**
	 * Change versions in pom files
	 *
	 * @param targetVersion name of the version we are going to create
	 * @param artifactIds list of artifact ids
	 * @param pomFileList list of pom files
	 * @throws AppFactoryException
	 */
	private static void changeVersionsInPomFiles(String targetVersion, List<String> artifactIds, List<File> pomFileList)
			throws AppFactoryException {
		try {
			MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
			for (File file : pomFileList) {
				FileInputStream stream = new FileInputStream(file);
				Model model = mavenXpp3Reader.read(stream);
				model.setVersion(targetVersion);
				validatingParentArtifactVersion(targetVersion, model, artifactIds);
				validateDependentArtifactVersions(targetVersion, model, artifactIds);
				if (stream != null) {
					stream.close();
				}
				MavenXpp3Writer writer = new MavenXpp3Writer();
				writer.write(new FileWriter(file), model);
			}
		} catch (FileNotFoundException e) {
			String msg = "Error while reading / writing pom files : " + e.getMessage();
			throw new AppFactoryException(msg, e);
		} catch (XmlPullParserException e) {
			String msg = "Error while parsing pom files : " + e.getMessage();
			throw new AppFactoryException(msg, e);
		} catch (IOException e) {
			String msg = "Error while reading / writing pom files : " + e.getMessage();
			throw new AppFactoryException(msg, e);
		}
	}

	/**
	 * validate the versions of dependents in a maven model and correct them
	 *
	 * @param targetVersion name of the version we are creating
	 * @param model maven model
	 * @param artifactIds list of artifact ids in the project
	 */
	private static void validateDependentArtifactVersions(String targetVersion, Model model, List<String> artifactIds) {
		List<Dependency> dependencies = model.getDependencies();
		for (Dependency dependency : dependencies) {
			if(artifactIds.contains(dependency.getArtifactId())){
				dependency.setVersion(targetVersion);
			}
		}
	}

	/**
	 * validate the versions of parent in a maven model and correct them
	 *
	 * @param targetVersion name of the version we are creating
	 * @param model maven model
	 * @param artifactIds list of artifact ids in the project
	 */
	private static void validatingParentArtifactVersion(String targetVersion, Model model, List<String> artifactIds) {
		Parent parentPom = model.getParent();
		if(parentPom != null && artifactIds.contains(parentPom.getArtifactId())) {
			parentPom.setVersion(targetVersion);
		}
	}

	/**
	 * Get artifact ids from list of pom files
	 *
	 * @param pomFileList list of pom files
	 * @param artifactIds list of artifact ids
	 * @throws AppFactoryException
	 */
	private static void getArtifactIdsFromPOMFiles(List<File> pomFileList, List<String> artifactIds)
			throws AppFactoryException {
		MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
		try {
			for (File file : pomFileList) {
				FileInputStream stream = new FileInputStream(file);
				Model model = mavenXpp3Reader.read(stream);
				artifactIds.add(model.getArtifactId());
				if (stream != null) {
					stream.close();
				}
			}
		} catch (FileNotFoundException e) {
			String msg = "Error while reading pom files : " + e.getMessage();
			throw new AppFactoryException(msg, e);
		} catch (XmlPullParserException e) {
			String msg = "Error while parsing pom files : " + e.getMessage();
			throw new AppFactoryException(msg, e);
		} catch (IOException e) {
			String msg = "Error while reading pom files : " + e.getMessage();
			throw new AppFactoryException(msg, e);
		}
	}

	/**
	 * Do versioning for the car artifacts (artifact.xmls and synapse.xmls)
	 *
	 * @param targetVersion name of the version we are going to create
	 * @param workDir current working directory
	 * @throws AppFactoryException
	 */
	public static void doVersionCarArtifacts(String targetVersion, File workDir) throws AppFactoryException{
		List<File> artifactList = new ArrayList<File>();
		List<String> synapseArtifacts = new ArrayList<String>();
		FileUtilities.searchFiles(workDir, AppFactoryConstants.CAR_ARTIFACT_CONFIGURATION, artifactList);
		List<File> pomFileList = new ArrayList<File>();
		FileUtilities.searchFiles(workDir, AppFactoryConstants.DEFAULT_POM_FILE, pomFileList);

		getAllSynapseArtifactsAndVersionAllArtifacts(targetVersion, synapseArtifacts, artifactList);

		List<File> synapseConfigs = new ArrayList<File>();
		FileUtilities.searchFiles(workDir, AppFactoryConstants.CAR_ARTIFACT_SYNAPSE_CONFIG_STORE_LOCATION,
		                          new OMNamespaceImpl(AppFactoryConstants.DEFAULT_SYNAPSE_NAMESPACE,
		                                              AppFactoryConstants.DEFAULT_SYNAPSE_NAMESPACE_PREFIX),
		                          synapseConfigs);

		//version all synapse configs in the project
		renameSynapseArtifactsInFiles(targetVersion, synapseArtifacts, synapseConfigs, true);
		renameSynapseArtifactsInFiles(targetVersion, synapseArtifacts, pomFileList, false);
		renameSynapseArtifactsInFiles(targetVersion, synapseArtifacts, pomFileList, false);
	}

	/**
	 * Changes the versions in all artifact definition files and will add synapse artifact names to the  input param
	 * synapseArtifacts
	 *
	 * @param targetVersion name of the version we are going to create
	 * @param synapseArtifacts List of names of synapse artifacts
	 * @param artifactList CAR artifact definition file list
	 * @throws AppFactoryException
	 */
	private static void getAllSynapseArtifactsAndVersionAllArtifacts(String targetVersion,
	                                                                 List<String> synapseArtifacts,
	                                                                 List<File> artifactList)
			throws AppFactoryException {
		for (File artifactConfiguration : artifactList) {
			OMElement artifacts = FileUtilities.loadXML(artifactConfiguration);
			Iterator artifactIterator =
					artifacts.getChildrenWithName(new QName(AppFactoryConstants.CAR_ARTIFACT_CONFIGURATION_ARTIFACT));
			while (artifactIterator.hasNext()){
				OMElement artifact = (OMElement) artifactIterator.next();
				artifact.getAttribute(new QName(AppFactoryConstants.CAR_ARTIFACT_CONFIGURATION_QNAME_VERSION))
				        .setAttributeValue(targetVersion);
				//If this is a synapse artifact
				if (artifact.getAttribute(new QName(AppFactoryConstants.CAR_ARTIFACT_CONFIGURATION_QNAME_TYPE))
				            .getAttributeValue().startsWith(AppFactoryConstants.CAR_ARTIFACT_CONFIGURATION_TYPE_SYNAPSE)){
					OMAttribute name = artifact.getAttribute(new QName(AppFactoryConstants.CAR_ARTIFACT_CONFIGURATION_QNAME_NAME));
					if (!synapseArtifacts.contains(name.getAttributeValue())) {
						// Add synapse artifact name to the list
						synapseArtifacts.add(name.getAttributeValue());
					}
				}
			}
			FileUtilities.writeXMLToFile(artifacts, artifactConfiguration.getAbsolutePath());
		}
	}

	/**
	 * Rename all synapse artifact names in the project while versioning since they include the version in their name
	 *
	 * @param targetVersion name of the version we are going to create
	 * @param synapseArtifacts List of names of synapse artifacts
	 * @param Files List of files to change content
	 * @param renameFiles is rename the file name is required (This will be true only for the synapse config files)
	 * @throws AppFactoryException
	 */
	private static void renameSynapseArtifactsInFiles(String targetVersion, List<String> synapseArtifacts,
	                                                  List<File> Files, boolean renameFiles) throws AppFactoryException {
		try {
			for (File file : Files) {
				String content = FileUtils.readFileToString(file);
				for (String synapseArtifact : synapseArtifacts) {
					content = content.replaceAll(synapseArtifact, changeArtifactName(synapseArtifact, targetVersion));
				}
				FileUtils.writeStringToFile(file, content);
				if(renameFiles) {
					String newArtifactFileName = file.getParentFile().getAbsolutePath() + File.separator +
					                             changeArtifactName(file.getName(), targetVersion) +
					                             AppFactoryConstants.FILENAME_EXTENSION_SEPERATOR +
					                             AppFactoryConstants.XML_EXTENSION;
					FileUtils.moveFile(file, new File(newArtifactFileName));
				}
			}
		} catch (IOException e) {
			String errorMsg = "Error in versioning synapse configs : " + e.getMessage();
			log.error(errorMsg, e);
			throw new AppFactoryException(errorMsg, e);
		}
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
                    OMElement deployXml = FileUtilities.loadXML(file);
                    Iterator processIterator = deployXml.getChildrenWithName(new QName("process"));
                    while (processIterator.hasNext()) {
                        OMElement processElement = (OMElement) processIterator.next();
                        if (processElement.getQName().getLocalPart().equals("process")) {
                            String processName = processElement.getAttribute(new QName("name")).getAttributeValue();
                            processElement.getAttribute(new QName("name")).setAttributeValue(changeArtifactName(
		                            processName, targetVersion));
                            Iterator provideIterator = processElement.getChildrenWithName(new QName("provide"));
                            while (provideIterator.hasNext()) {
                                OMElement provideElement = (OMElement) provideIterator.next();
                                Iterator serviceIterator = provideElement.getChildrenWithName((new QName("service")));
                                while (serviceIterator.hasNext()) {
                                    OMElement serviceElement = (OMElement) serviceIterator.next();
                                    if (serviceElement.getQName().getLocalPart().equals("service")) {
                                        String oldName1 = serviceElement.getAttribute(new QName("name")).getAttributeValue();
                                        serviceElement.getAttribute(new QName("name")).setAttributeValue(
		                                        changeArtifactName(
				                                        oldName1, targetVersion));
                                    }
                                }
                            }
                        }
                    }
                    FileUtilities.writeXMLToFile(deployXml, file.getAbsolutePath());
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
                OMElement wsdl = FileUtilities.loadXML(file);
                Iterator wsdlIterator = wsdl.getChildElements();
                while (wsdlIterator.hasNext()) {
                    OMElement wsdlElement = (OMElement) wsdlIterator.next();
                    if (wsdlElement.getQName().getLocalPart().equals("service")) {
                        String serviceName = wsdlElement.getAttribute(new QName("name")).getAttributeValue();
                        if (serviceName.startsWith(applicationId)){
                        	wsdlElement.getAttribute(new QName("name")).setAttributeValue(changeArtifactName(
			                        serviceName, targetVersion));
                        }
                    }
                }
                FileUtilities.writeXMLToFile(wsdl, file.getAbsolutePath());
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
                OMElement bpelProcess = FileUtilities.loadXML(file);
                String processName = bpelProcess.getAttribute(new QName("name")).getAttributeValue();
                bpelProcess.getAttribute(new QName("name")).setAttributeValue(changeArtifactName(processName,
                                                                                                 targetVersion));
                FileUtilities.writeXMLToFile(bpelProcess, file.getAbsolutePath());
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

	private static String changeArtifactName(String name, String changedVersion) {

        String applicationName = name;
        if (name.lastIndexOf(AppFactoryConstants.APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR) != -1) {
            applicationName = name.substring(0, name.lastIndexOf(AppFactoryConstants.APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR));
        }
        String newArtifactName = applicationName + AppFactoryConstants.APPFACTORY_ARTIFACT_NAME_VERSION_SEPERATOR + changedVersion;
        return newArtifactName;
    }


    private static void doVersionOnSynapseXML(String currentVersion, String targetVersion, File workDir) {

        if (!(currentVersion.equals("trunk") && currentVersion.equals("1.0.0"))) {
            currentVersion = "SNAPSHOT";
        }

        try {
            String[] fileExtension = {"xml"};
            List<File> fileList = (List<File>) FileUtils.listFiles(workDir,
                    fileExtension, true);
            for (File file : fileList) {
                if (file.getName().equals("synapse.xml")) {
                    OMElement deployXml = FileUtilities.loadXML(file);
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
                    FileUtilities.writeXMLToFile(deployXml, file.getAbsolutePath());
                }

            }
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

}
