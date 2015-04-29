package org.wso2.appfactory.integration.test.utils.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Datasource related stub
 */
public class DatasourceRestClient extends BaseRestClient {

    public static final String DATASOURCE_NAME = "datasourceName";
    public static final String RSS_INSTANCE_NAME = "rssInstanceName";
    public static final String URL = "url";
    public static final String DATASOURCE_DESCRIPTION = "datasourceDescription";
    public static final String DRIVER_NAME = "driverName";
    public static final String DB_USERNAME = "dbUsername";
    public static final String DB_PASSWORD = "dbPassword";
    public static final String COPY_TO_ALL = "copyToAll";

    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public DatasourceRestClient(String backEndUrl, String username, String password) throws Exception {
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
     * @throws Exception
     */
    public void createDatasource(String dsName, String stage, String dbUrl, String dbDescription, String driver,
                                 String dbUserName, String password, boolean copyToAll) throws Exception {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put(DATASOURCE_NAME, dsName);
        msgBody.put(RSS_INSTANCE_NAME, stage);
        msgBody.put(COPY_TO_ALL, Boolean.toString(copyToAll));
        msgBody.put(URL, dbUrl);
        msgBody.put(DATASOURCE_DESCRIPTION, dbDescription);
        msgBody.put(DRIVER_NAME, driver);
        msgBody.put(DB_USERNAME, dbUserName);
        msgBody.put(DB_PASSWORD, password);
        doPostRequest(APPMGT_DATASOURCE_ADD, msgBody);

    }

}
