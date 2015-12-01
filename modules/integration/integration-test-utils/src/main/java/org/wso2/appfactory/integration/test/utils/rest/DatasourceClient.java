package org.wso2.appfactory.integration.test.utils.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONObject;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Datasource related stub
 */
public class DatasourceClient extends BaseClient {

    public static final String STAGE = "stage";
    public static final String APP_KEY = "applicationKey";
    private static final String DATASOURCE_NAME = "datasourceName";
    private static final String RSS_INSTANCE_NAME = "rssInstanceName";
    private static final String URL = "url";
    private static final String DATASOURCE_DESCRIPTION = "datasourceDescription";
    private static final String DRIVER_NAME = "driverName";
    private static final String DB_USERNAME = "dbUsername";
    private static final String DB_PASSWORD = "dbPassword";
    private static final String COPY_TO_ALL = "copyToAll";
    private static final String IS_EDIT = "isEdit";
    private static final String REQUEST_KEY_ACTION = "action";
    private static final String CREATE_DATASOURCE = "createDatasource";
    private static final String DELETE_DATASOURCE = "deleteDatasource";
    private static final String EDIT_DATASOURCE = "editDatasource";
    private static final String GET_DATASOURCE = "getDatasource";
    private static final String GET_ALL_DATASOURCES_INFO = "getAllDatasourcesInfo";

    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public DatasourceClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }


    /**
     * Create datasource in given environment; If copyToAll is true, create datasource in all the environments
     *
     * @param dsName
     * @param stage
     * @param dbUrl
     * @param dbDescription
     * @param driver
     * @param dbUserName
     * @param password
     * @param copyToAll
     * @param applicationKey
     * @throws Exception
     */
    public JSONObject createDatasource(String dsName, String stage, String dbUrl, String dbDescription, String driver,
                                       String dbUserName, String password, boolean copyToAll, String applicationKey)
            throws Exception {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(REQUEST_KEY_ACTION, CREATE_DATASOURCE);
        msgBody.put(DATASOURCE_NAME, dsName);
        msgBody.put(RSS_INSTANCE_NAME, stage);
        msgBody.put(APP_KEY, applicationKey);
        msgBody.put(COPY_TO_ALL, Boolean.toString(copyToAll));
        msgBody.put(URL, dbUrl);
        msgBody.put(DATASOURCE_DESCRIPTION, dbDescription);
        msgBody.put(DRIVER_NAME, driver);
        msgBody.put(DB_USERNAME, dbUserName);
        msgBody.put(DB_PASSWORD, password);
        HttpResponse response = doPostRequest(APPMGT_DATASOURCE_ADD, msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            checkErrors(response);
            return new JSONObject(response.getData().trim());
        } else {
            throw new AFIntegrationTestException("Error occurred while creating a new datasource :" +
                                                 response.getResponseCode() + response.getData().trim());
        }
    }

    /**
     * Delete given datasource
     *
     * @param dsName
     * @param stage
     * @param applicationKey
     * @throws Exception
     */
    public JSONObject deleteDatasource(String dsName, String stage, String applicationKey) throws
            Exception {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(REQUEST_KEY_ACTION, DELETE_DATASOURCE);
        msgBody.put(DATASOURCE_NAME, dsName);
        msgBody.put(STAGE, stage);
        msgBody.put(APP_KEY, applicationKey);
        HttpResponse response = doPostRequest(APPMGT_DATASOURCE_ADD, msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            checkErrors(response);
            return new JSONObject(response.getData().trim());
        } else {
            throw new AFIntegrationTestException("Error occurred while deleting a new datasource :" +
                                                 response.getResponseCode() + response.getData());
        }
    }

    /**
     * Edit existing datasource
     *
     * @param dsName
     * @param stage
     * @param dbUrl
     * @param dbDescription
     * @param driver
     * @param dbUserName
     * @param password
     * @param isEdit
     * @param applicationKey
     * @throws Exception
     */
    public JSONObject editDatasource(String dsName, String stage, String dbUrl, String dbDescription, String driver,
                               String dbUserName, String password, boolean isEdit, String applicationKey)
            throws Exception {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(REQUEST_KEY_ACTION, EDIT_DATASOURCE);
        msgBody.put(DATASOURCE_NAME, dsName);
        msgBody.put(RSS_INSTANCE_NAME, stage);
        msgBody.put(APP_KEY, applicationKey);
        msgBody.put(IS_EDIT, Boolean.toString(isEdit));
        msgBody.put(URL, dbUrl);
        msgBody.put(DATASOURCE_DESCRIPTION, dbDescription);
        msgBody.put(DRIVER_NAME, driver);
        msgBody.put(DB_USERNAME, dbUserName);
        msgBody.put(DB_PASSWORD, password);
        HttpResponse response = doPostRequest(APPMGT_DATASOURCE_ADD, msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            checkErrors(response);
            return new JSONObject(response.getData().trim());
        } else {
            throw new AFIntegrationTestException("Error occurred while updating existing datasource :" +
                                                 response.getResponseCode() + response.getData().trim());
        }
    }

    /**
     * Get all datasource information of a particular application
     *
     * @param applicationKey
     * @throws Exception
     */
    public void getAllDatasourcesInfo(String applicationKey) throws Exception {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(REQUEST_KEY_ACTION, EDIT_DATASOURCE);
        msgBody.put(APP_KEY, applicationKey);
        HttpResponse response = doPostRequest(APPMGT_DATASOURCE_GET, msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving existing datasource :" +
                                                 response.getResponseCode() + response.getData().trim());
        }
    }

    /**
     *  Get data source info url for  given stage
     * @param stage
     * @return
     * @throws AFIntegrationTestException
     */
    public String getDataSourceInfoUrl(String stage) throws AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(REQUEST_KEY_ACTION, "getDataSourceInfoUrl");
        msgBody.put("stage", stage);
        HttpResponse response = doPostRequest(APPMGT_DATASOURCE_GET, msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return response.getData().trim();
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving datasource info url for stage : " +
                                                 stage + response.getResponseCode() + response.getData().trim());
        }
    }

    /**
     *  Get data source
     * @param dsName
     * @param stage
     * @param applicationKey
     * @return
     * @throws AFIntegrationTestException
     */
    public JSONObject getDataSource(String dsName, String stage, String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(REQUEST_KEY_ACTION, "getDatasource");
        msgBody.put(DATASOURCE_NAME, dsName);
        msgBody.put(STAGE, stage);
        msgBody.put(APP_KEY, applicationKey);
        HttpResponse response = doPostRequest(APPMGT_DATASOURCE_GET, msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return new JSONObject(response.getData().trim());
        } else {
            throw new AFIntegrationTestException("Error occurred while retrieving datasource for stage : " +
                    stage + response.getResponseCode() + response.getData().trim());
        }
    }
}
