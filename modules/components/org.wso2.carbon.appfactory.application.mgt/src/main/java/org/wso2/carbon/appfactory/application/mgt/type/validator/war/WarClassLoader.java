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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.jar.JarEntry;

/**
 * War class loader.
 */
public class WarClassLoader extends URLClassLoader {
    /**
     * war file to be used for class loading
     */
    private WarFile war;

    public WarClassLoader(WarFile war, ClassLoader classloader) throws IOException {
        super(new URL[0], classloader);
        this.war = war;
        addURLs();
    }

    /**
     * Add WEB-INF/classes and WEB-INF/lib/*.jar as extra classpath URLs
     *
     * @throws java.io.IOException            when an I/O error occurs extracting the jars from
     *                                        the war
     * @throws java.net.MalformedURLException if the jar: URL is not supported on the
     *                                        underlying platform
     */
    private void addURLs() throws IOException {
        File warFile = new File(war.getName());
        String CLASSES_DIR = "WEB-INF/classes/";
        String JAR_PREFIX = "jar:";
        String SEPARATOR = "!/";
        URL webInfClasses = new URL(JAR_PREFIX + warFile.toURI().toURL() + SEPARATOR
                                    + CLASSES_DIR);
        addURL(webInfClasses);
        Set<JarEntry> jars = war.getLibEntries();
        for (JarEntry entry : jars) {
            File jar = war.extract(entry);
            addURL(new URL(JAR_PREFIX + jar.toURI().toURL() + SEPARATOR));
            jar.deleteOnExit();
        }
    }
}
