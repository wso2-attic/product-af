/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.appfactory.application.mgt.type.validator.war;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * War file
 */
public class WarFile extends JarFile {
    public static final String TMP_FOLDER_PREFIX = "maven";
    public static final String JSP_FILE = "jsp-file";
    private Document parsedWebXml;


    public WarFile(String name) throws IOException {
        super(name);
    }

    public JarEntry getWebXmlEntry() {
        String WEB_XML = "WEB-INF/web.xml";
        return getJarEntry(WEB_XML);
    }

    public List<String> getServlets() throws IOException {
        String SERVLET_CLASS = "servlet-class";
        List<String> servletList = new ArrayList<String>();
        if (this.getWebXmlEntry() != null) {
            Document webXml = getWebXml();
            NodeList servletClassNodes = webXml.getElementsByTagName(SERVLET_CLASS);
            int length = servletClassNodes.getLength();
            for (int i = 0; i < length; i++) {
                servletList.add(servletClassNodes.item(i).getFirstChild().getNodeValue().trim());
            }
        }
        return servletList;
    }

    public List<String> getJSPs() throws IOException {
        List<String> jspList = new ArrayList<String>();
        if (this.getWebXmlEntry() != null) {
            Document webXml = getWebXml();
            NodeList jspFileNodes = webXml.getElementsByTagName(JSP_FILE);
            int length = jspFileNodes.getLength();
            for (int i = 0; i < length; i++) {
                jspList.add(jspFileNodes.item(i).getFirstChild().getNodeValue().trim());
            }
        }
        return jspList;
    }

    public Set<JarEntry> getLibEntries() {
        String JAR = ".jar";
        String LIB = "WEB-INF/lib/";
        HashSet<JarEntry> libs = new HashSet<JarEntry>();
        Enumeration entries = entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            if (entry.getName().startsWith(LIB) && entry.getName().toLowerCase().endsWith(JAR)) {
                libs.add(entry);
            }
        }

        return libs;
    }

    public File extract(JarEntry entry) throws IOException {
        File tempFile = File.createTempFile(TMP_FOLDER_PREFIX, null);
        BufferedInputStream inStream = new BufferedInputStream(this.getInputStream(entry));
        BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        int status;
        while ((status = inStream.read()) != -1) {
            outStream.write(status);
        }
        outStream.close();
        inStream.close();
        return tempFile;
    }

    public boolean hasFile(String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName parameter can\'t be null");
        } else {
            String entryName = fileName;
            if (fileName.startsWith("/")) {
                entryName = fileName.substring(1);
            }
            return getJarEntry(entryName) != null;
        }
    }

    protected Document getWebXml() throws IOException {
        if (parsedWebXml == null) {
            parsedWebXml = loadWebXml();
        }
        return parsedWebXml;
    }

    private Document loadWebXml() throws IOException {
        if (this.getWebXmlEntry() == null) {
            throw new IOException("Attempted to get non-existent web.xml");
        } else {
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                return docBuilder.parse(getInputStream(getWebXmlEntry()));
            } catch (ParserConfigurationException e) {
                throw new IOException(e.getMessage());
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
        }
    }
}
