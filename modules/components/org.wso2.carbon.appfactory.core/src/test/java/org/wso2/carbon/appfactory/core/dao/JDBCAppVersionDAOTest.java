package org.wso2.carbon.appfactory.core.dao;

import org.wso2.carbon.appfactory.core.dao.base.BaseTestCase;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by punnadi on 4/16/15.
 */
public class JDBCAppVersionDAOTest extends BaseTestCase {
    public JDBCAppVersionDAOTest(String name) {
        super(name);
    }


    public void testAddApplicationVersion() throws Exception {
        Application application = new Application();
        application.setName("appx");
        application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
        application.setId("appx");
        application.setType("Uploaded-App-Jax-WS");

        applicationDAO.addApplication(application);

        Version version = new Version();
        version.setVersion("1.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setStage("Development");
        appVersionDAO.addVersion("appx", version);

        String[] applicationVersions = appVersionDAO.getAllVersionsOfApplication("appx");
        assertTrue( Arrays.asList(applicationVersions).contains("trunk"));
        assertTrue(Arrays.asList(applicationVersions).contains("1.0.0"));
        assertEquals(2, applicationDAO.getBranchCount("appx"));

        applicationDAO.deleteApplication("appx");
    }

    public void testGetApplicationVersion() throws Exception {

        Application application = new Application();
        application.setName("appversion");
        application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
        application.setId("appversion");
        application.setType("Uploaded-App-Jax-WS");
        applicationDAO.addApplication(application);

        Version version = new Version();
        version.setVersion("1.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setStage("Development");
        appVersionDAO.addVersion("appversion", version);

        Version addedVersion = appVersionDAO.getApplicationVersion("appversion", "1.0.0");
        assertEquals("1.0.0", addedVersion.getVersion());
        assertEquals("Development", addedVersion.getStage());

        applicationDAO.deleteApplication("appversion");
    }

    public void testUpdateApplicationVersionStage() throws Exception {

        Application application = new Application();
        application.setName("appstage");
        application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
        application.setId("appstage");
        application.setType("Uploaded-App-Jax-WS");
        applicationDAO.addApplication(application);

        Version version = new Version();
        version.setVersion("2.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setStage("Development");
        appVersionDAO.addVersion("appstage", version);

        boolean updated = appVersionDAO.updateStageOfVersion("appstage", "2.0.0", "Testing");
        assertTrue(updated);
        Version addedVersion = appVersionDAO.getApplicationVersion("appstage", "2.0.0");
        assertEquals("Testing", addedVersion.getStage());

        applicationDAO.deleteApplication("appstage");
    }

    public void testUpdateApplicationVersionPromoteState() throws Exception {

        Application application = new Application();
        application.setName("apppromote");
        application.setApplicationCreationStatus(Constants.ApplicationCreationStatus.PENDING);
        application.setId("apppromote");
        application.setType("Uploaded-App-Jax-WS");
        applicationDAO.addApplication(application);

        Version version = new Version();
        version.setVersion("2.0.0");
        version.setPromoteStatus("SUCCESS");
        version.setStage("Development");
        appVersionDAO.addVersion("apppromote", version);

        boolean updated = appVersionDAO.updateStageOfVersion("apppromote", "2.0.0", "Testing");
        appVersionDAO.updatePromoteStatusOfVersion("apppromote", "2.0.0", "");
        Version addedVersion = appVersionDAO.getApplicationVersion("apppromote", "2.0.0");
        assertEquals("", addedVersion.getPromoteStatus());

        applicationDAO.deleteApplication("apppromote");
    }

}
