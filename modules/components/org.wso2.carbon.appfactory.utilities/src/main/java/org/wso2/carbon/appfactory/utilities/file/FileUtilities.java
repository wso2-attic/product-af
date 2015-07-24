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

package org.wso2.carbon.appfactory.utilities.file;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for file operations in AF
 * The file operations that's used commonly in AF which cannot be found in common File Utils are implemented here
 */
public class FileUtilities {

	public static final Log log = LogFactory.getLog(FileUtilities.class);

	/**
	 * Search for the file name recursively inside a folder
	 *
	 * @param baseDir working directory
	 * @param fileName name of the file to search
	 * @param resultFileList list to insert files found
	 */
	public static void searchFiles(File baseDir, String fileName, List<File> resultFileList){
		for (File file : baseDir.listFiles()) {
			if (file.isDirectory()) {
				searchFiles(file, fileName, resultFileList);
			}
			if (fileName.equals(file.getName())) {
				resultFileList.add(file);
			}
		}
	}

	/**
	 * Search xml files in which given namespaces are defined.
	 *
	 * @param baseDir working directory
	 * @param subFolder optional param to include specific locations
	 * @param namespace namespace to search
	 * @param resultFileList list to insert files found
	 * @throws AppFactoryException
	 */
	public static void searchFiles(File baseDir, String subFolder, OMNamespace namespace, List<File> resultFileList)
			throws AppFactoryException {
		String[] fileExtension = { AppFactoryConstants.XML_EXTENSION};
		List<File> fileList = (List<File>) FileUtils.listFiles(baseDir, fileExtension, true);
		for (File file : fileList) {
			if(subFolder != null && !file.getAbsolutePath().contains(subFolder)){
				continue;
			}
			OMElement element = loadXML(file);
			Iterator<OMNamespace> namespaceIterator = element.getAllDeclaredNamespaces();
			while (namespaceIterator.hasNext()){
				if(namespace.getNamespaceURI().equals(namespaceIterator.next().getNamespaceURI())){
					resultFileList.add(file);
					break;
				}
			}
		}
	}

	/**
	 * Delete target folders inside a given location recursively
	 *
	 * @param baseDir base directory
	 */
	public static void deleteTargetFolders(File baseDir){
		List<File> targetFileList = new ArrayList<File>();
		FileUtilities.searchFiles(baseDir, AppFactoryConstants.DEFAULT_TARGET_FOLDER, targetFileList);
		for (File file : targetFileList) {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				String errorMsg = "Error while deleting target folders : " + e.getMessage();
				log.error(errorMsg, e);
			}
		}
	}

	/**
	 * Load xml from a file
	 *
	 * @param configFile file location
	 * @return loaded root OMElement
	 * @throws AppFactoryException
	 */
	public static OMElement loadXML(File configFile) throws AppFactoryException {

	    InputStream inputStream = null;
	    OMElement configXMLFile = null;
	    try {
	        inputStream = new FileInputStream(configFile);
	        String xmlContent = IOUtils.toString(inputStream);
	        configXMLFile = AXIOMUtil.stringToOM(xmlContent);
	    } catch (IOException e) {
	        String msg = "Unable to read the file " + configFile.getName();
	        AppVersionStrategyExecutor.log.error(msg, e);
	        throw new AppFactoryException(msg, e);
	    } catch (XMLStreamException e) {
	        String msg = "Error in parsing " + configFile.getName();
	        AppVersionStrategyExecutor.log.error(msg, e);
	        throw new AppFactoryException(msg, e);
	    } finally {
	        try {
	            if (inputStream != null) {
	                inputStream.close();
	            }
	        } catch (IOException e) {
	            String msg = "Error in closing stream ";
	            AppVersionStrategyExecutor.log.error(msg, e);
	        }
	    }
	    return configXMLFile;
	}

	/**
	 * Write OMElement to a file
	 * @param element OMElement to write
	 * @param fileName file name to write
	 * @return isSuccessful
	 * @throws AppFactoryException
	 */
	public static boolean writeXMLToFile(OMElement element, String fileName) throws AppFactoryException {
        File destinationFile = new File(fileName);
        FileWriter writer = null;
        try {
            writer = new FileWriter(destinationFile);
            element.serialize(writer);
        } catch (IOException e) {
            String msg = "Error in writing to " + fileName;
            AppVersionStrategyExecutor.log.error(msg, e);
            throw new AppFactoryException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error in parsing " + fileName;
            AppVersionStrategyExecutor.log.error(msg, e);
            throw new AppFactoryException(msg, e);
        }
        return true;
    }
}
