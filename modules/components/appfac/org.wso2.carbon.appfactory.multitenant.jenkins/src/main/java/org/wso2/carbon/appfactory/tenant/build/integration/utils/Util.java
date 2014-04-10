/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appfactory.tenant.build.integration.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Common class to define utility methods.
 */
public class Util {

	private static Log log = LogFactory.getLog(Util.class);

	public static String getCarbonResourcesPath() {
		return CarbonUtils.getCarbonHome() + File.separator + "repository"
				+ File.separator + "resources";
	}

	public static void createFile(File file, String content) throws IOException {
		FileUtils.writeStringToFile(file, content);
	}

	public static String getCarbonTenantPath() {
		return CarbonUtils.getCarbonHome() + File.separator + "repository"
				+ File.separator + "tenants";
	}

	/**
	 * Serializes the given {@link OMElement} to a specified file path. If
	 * entire file path doesn't exists it will be created.
	 * 
	 * @param filePath
	 *            the destination file
	 * @param xmlContent
	 *            Xml content to be written
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public static void writeToFilePath(String filePath, OMElement xmlContent)
			throws XMLStreamException, IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			if (!file.getParentFile().mkdirs()) {
				throw new IOException("Unable to create directories in path : "
						+ filePath);

			}

			file.createNewFile();

		} else {
			log.warn("configuratoin file :" + file.getAbsolutePath()
					+ "already exists");
		}

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		xmlContent.serialize(fileOutputStream);
		fileOutputStream.flush();
		fileOutputStream.close();
	}

	/**
	 * Returns the immediate child with given local name or creates if the
	 * specified child doesn't exist.
	 * 
	 * @param childNodeName
	 *            Name of the child node.
	 * @param parent
	 *            the parent
	 * @return an {@link OMElement}
	 */
	public static OMElement findOrAddChild(String childNodeName,
			OMElement parent) {

		OMElement childEle = null;
		Iterator<OMElement> iterator = parent
				.getChildrenWithLocalName(childNodeName);
		if (iterator.hasNext()) {
			childEle = iterator.next();
		} else {
			childEle = parent.getOMFactory().createOMElement(childNodeName,
					parent.getNamespace(), parent);
		}
		return childEle;

	}
	
	/**
	 * Unzip a zipfile to a destination specified. N
	 * 
	 * @param zipFilename
	 *            input zip file
	 * @param destDirname
	 *            destination directory
	 * @throws IOException
	 *             an error
	 */

	public static void unzip(String zipFilename, String destDirname)
			throws IOException {

		File distinationDir = new File(destDirname);
		if (!distinationDir.isDirectory()) {

			if (!distinationDir.mkdirs()) {

				throw new IOException("Unable to create output directory :"
						+ distinationDir);

			}
		}

		ZipFile zipFile = new ZipFile(zipFilename);
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File entryDestination = new File(distinationDir,
						entry.getName());
				if (!entry.isDirectory()) {
					entryDestination.getParentFile().mkdirs(); // TO ensure that parent path already exists.
					InputStream in = zipFile.getInputStream(entry);
					OutputStream out = new FileOutputStream(entryDestination);
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);

				} else {
					if (!entryDestination.mkdirs()) {
						log.warn("Couldn't create the directory :"
								+ entryDestination.getAbsolutePath());
					}
				}
			}

		} finally {
			zipFile.close();
		}

	}

}
