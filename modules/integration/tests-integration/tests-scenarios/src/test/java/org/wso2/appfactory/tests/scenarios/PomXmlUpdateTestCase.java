/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appfactory.tests.scenarios;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AppFactoryIntegrationTest;
import org.wso2.carbon.appfactory.repository.mgt.client.AppfactoryRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.GitAgent;
import org.wso2.carbon.appfactory.repository.mgt.git.GitRepositoryClient;
import org.wso2.carbon.appfactory.repository.mgt.git.JGitAgent;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * This test case cover below scenarios on updating root pom.xml of an application
 * 1. update version, groupId, packaging, name
 * 2. update dependencies(positive test case)
 * 3. replace root pom.xml
 * 4. delete root pom.xml
 */
public class PomXmlUpdateTestCase extends AppFactoryIntegrationTest {
    private static final Log log = LogFactory.getLog(PomXmlUpdateTestCase.class);

    private static AppfactoryRepositoryClient client;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        initWithTenantAndApplicationCreation();

        // initiate repository client
        GitAgent gitAgent = new JGitAgent();
        client = new GitRepositoryClient(gitAgent);
        client.init(getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIIN) + "@" + getRandomTenantDomain(),
                getPropertyValue(AFConstants.DEFAULT_TENANT_ADMIN_PASSWORD));

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Modify groupId, packaging, version, name in root pom.xml")
    public void modifyPOMWithInvalidEntries() throws Exception {
        // working directory
        File workDir = new File(CarbonUtils.getTmpDir() + "/" + UUID.randomUUID());
        // construct repo url for default application
        String repoURL = getRepoUrl(getRandomTenantDomain(), getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        boolean result = client.retireveMetadata(repoURL, false, workDir);
        Assert.assertEquals(result, true, "Failed to clone.");

        updatePOM("100", "None", "None", "org.test.failure", null, workDir);
        client.commitLocally("Committing POM file updates.", true, workDir);
        boolean pushSucceeded = client.pushLocalCommits(repoURL, "master", workDir);
        Assert.assertEquals(pushSucceeded, false, "Able to commit and push pom updates.");

        // clean temp directory
        FileUtils.forceDelete(workDir);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Add new dependency in root pom.xml")
    public void modifyPOMWithValidEntries() throws Exception {
        // working directory
        File workDir = new File(CarbonUtils.getTmpDir() + "/" + UUID.randomUUID());
        // construct repo url for default application
        String repoURL =  getRepoUrl(getRandomTenantDomain(), getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        boolean result = client.retireveMetadata(repoURL, false, workDir);
        Assert.assertEquals(result, true, "Failed to clone.");

        Dependency carbonCore = new Dependency();
        carbonCore.setGroupId("org.wso2.carbon");
        carbonCore.setArtifactId("org.wso2.carbon.core");
        carbonCore.setVersion("4.2.0");
        updatePOM(null, null, null, null, carbonCore, workDir);
        client.commitLocally("Committing POM file with new dependency.", true, workDir);
        boolean pushSucceeded = client.pushLocalCommits(repoURL, "master", workDir);
        Assert.assertEquals(pushSucceeded, true, "Failed to commit and push dependency updates.");

        // clean temp directory
        FileUtils.forceDelete(workDir);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Delete root pom.xml")
    public void deletePOM() throws Exception {
        // working directory
        File workDir = new File(CarbonUtils.getTmpDir() + "/" + UUID.randomUUID());
        // construct repo url for default application
        String repoURL =  getRepoUrl(getRandomTenantDomain(), getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        boolean result = client.retireveMetadata(repoURL, false, workDir);
        Assert.assertEquals(result, true, "Failed to clone.");

        deletePOM(workDir);
        client.commitLocally("Committing root pom deletion", true, workDir);
        boolean pushSucceeded = client.pushLocalCommits(repoURL, "master", workDir);
        Assert.assertEquals(pushSucceeded, false, "Able to delete and push pom.");

        // clean temp directory
        FileUtils.forceDelete(workDir);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
    @Test(description = "Replace root pom.xml")
    public void replacePOM() throws Exception {
        // working directory
        File workDir = new File(CarbonUtils.getTmpDir() + "/" + UUID.randomUUID());
        // construct repo url for default application
        String repoURL =  getRepoUrl(getRandomTenantDomain(), getPropertyValue(AFConstants.DEFAULT_APP_APP_KEY));
        boolean result = client.retireveMetadata(repoURL, false, workDir);
        Assert.assertEquals(result, true, "Failed to clone.");

        // get newPOM file from artifacts.
        File newPOM = new File(FrameworkPathUtil.getSystemResourceLocation() + "/artifacts/pom.xml");
        if (!newPOM.exists()) {
            boolean created = newPOM.createNewFile();
            if (!created) {
                throw new Exception("Failed to create file:" + newPOM.getAbsolutePath());
            }
        }

        //replace oldPOM file with newPOM file.
        FileUtils.copyFileToDirectory(newPOM, workDir);
        client.commitLocally("Committing root pom replacement", true, workDir);
        boolean pushSucceeded = client.pushLocalCommits(repoURL, "master", workDir);
        Assert.assertEquals(pushSucceeded, false, "Able to replace and push root pom.");

        // clean temp directory
        FileUtils.forceDelete(workDir);
    }

    public String getRepoUrl(String tenantDomain, String appKey) throws XPathExpressionException {
        return  getPropertyValue(AFConstants.URLS_GIT) + "git" + "/" + tenantDomain + "/" + appKey + ".git";
    }

    public static void updatePOM(String version, String name, String packaging, String groupId,
                                 Dependency dependency, File workDir) throws IOException, XmlPullParserException {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model;

        String[] fileExtension = {"xml"};

        Collection files = FileUtils.listFiles(workDir, fileExtension, true);
        for (Object fileObj : files) {
            File file = (File) fileObj;
            if (file.getName().equals("pom.xml")) {
                FileInputStream stream = null;
                try {
                    stream = new FileInputStream(file);
                    model = mavenXpp3Reader.read(stream);
                    if (StringUtils.isNotEmpty(version)) {
                        model.setVersion(version);
                    }
                    if (StringUtils.isNotEmpty(name)) {
                        model.setName(name);
                    }
                    if (StringUtils.isNotEmpty(packaging)) {
                        model.setPackaging(packaging);
                    }
                    if (StringUtils.isNotEmpty(groupId)) {
                        model.setGroupId(groupId);
                    }
                    if (dependency != null) {
                        model.addDependency(dependency);
                    }

                    MavenXpp3Writer writer = new MavenXpp3Writer();
                    writer.write(new FileWriter(file), model);

                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            log.warn("Failed to close file input stream.", e);
                        }
                    }
                }
            }
        }

    }

    public static void deletePOM(File workDir) throws IOException, XmlPullParserException {
        String[] fileExtension = {"xml"};
        Collection files = FileUtils.listFiles(workDir, fileExtension, true);
        for (Object fileObj : files) {
            File file = (File) fileObj;
            if (file.getName().equals("pom.xml")) {
                FileUtils.forceDelete(file);
            }
        }

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
