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
import java.util.List;

/**
 * War validator
 */
public class WarValidator {
    private String warFileName;
    private boolean validateServletClasses = true;
    private boolean validateServletJsp = true;

    public WarValidator() {
    }

    public WarValidator(String warFileName) {
        this.warFileName = warFileName;
    }

    public boolean isValidateServletClasses() {
        return validateServletClasses;
    }

    public void validateServletClasses(boolean validateServletClasses) {
        this.validateServletClasses = validateServletClasses;
    }

    public boolean isValidateServletJsp() {
        return validateServletJsp;
    }

    public void validateServletJsp(boolean validateServletJsp) {
        this.validateServletJsp = validateServletJsp;
    }

    public String getWarFileName() {
        return this.warFileName;
    }

    public void setWarFileName(String warFileName) {
        this.warFileName = warFileName;
    }


    public void execute() throws WarValidationException {
        if (this.getWarFileName() == null) {
            throw new NullPointerException("war file name should not be null");
        } else {
            this.validate();
        }
    }

    protected void validate() throws WarValidationException {
        validateFile();
        validateWarContents();
    }

    protected void validateFile() throws WarValidationException {
        File warFile = new File(this.getWarFileName());
        if (!warFile.exists()) {
            throw new WarValidationException("File does not exist");
        } else if (!warFile.canRead()) {
            throw new WarValidationException("File can\'t be read");
        }
    }

    protected void validateWarContents() throws WarValidationException {
        validateWebXml();
    }

    protected void validateWebXml() throws WarValidationException {
        try {
            WarFile war = new WarFile(this.getWarFileName());
            if (war.getWebXmlEntry() == null) {
                throw new WarValidationException("web.xml entry not found");
            }
            if (isValidateServletClasses()) {
                validateServlets(war);
            }
            if (isValidateServletJsp()) {
                validateJSPs(war);
            }
        } catch (IOException e) {
            throw new WarValidationException("Error opening war file for web.xml - possibly missing manifest", e);
        }
    }

    protected void validateJSPs(WarFile war) throws IOException, WarValidationException {
        List<String> Jsps = war.getJSPs();
        for (String jspFile : Jsps) {
            if (!war.hasFile(jspFile)) {
                throw new WarValidationException("JSP File: \'" + jspFile + "\' not found");
            }
        }
    }

    protected void validateServlets(WarFile war) throws IOException, WarValidationException {
        List<String> servlets = war.getServlets();
        WarClassLoader classLoader = new WarClassLoader(war, this.getClass().getClassLoader());
        for (String servletClassName : servlets) {
            validateClassUsingClassLoader(servletClassName, classLoader);
        }
    }

    protected void validateClassUsingClassLoader(String className, ClassLoader loader) throws WarValidationException {
        try {
            // TODO:
            // Class not found exception is thrown for "org.apache.cxf.transport.servlet.CXFServlet" uploadable jaxrs
            // app type.
            loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new WarValidationException("class (" + className + ") not found ", e);
        } catch (NoClassDefFoundError e) {
            throw new WarValidationException("class (" + className + ") was found, but a referenced class was " +
                                             "missing", e);
        }
    }

}
