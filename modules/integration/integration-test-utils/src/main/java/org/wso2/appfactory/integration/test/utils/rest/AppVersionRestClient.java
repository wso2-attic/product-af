package org.wso2.appfactory.integration.test.utils.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by punnadi on 4/28/15.
 */
public class AppVersionRestClient extends BaseRestClient {
    /**
     * Construct authenticates REST client to invoke appmgt functions
     *
     * @param backEndUrl backend url
     * @param username   username
     * @param password   password
     * @throws Exception
     */
    public AppVersionRestClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    /**
     * Create new application version
     * @param applicationKey application key
     * @param srcVersion     source version
     * @param targetVersion  target version
     */
    public void createVersion(String applicationKey, String srcVersion, String targetVersion) throws Exception {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("applicationKey", applicationKey);
        msgBody.put("srcVersion", srcVersion);
        msgBody.put("targetVersion", targetVersion);
        msgBody.put("lifecycleName", "Development");
        doPostRequest(APPMGT_LIFECYCLE_ADD, generateMsgBody(msgBody));
    }

}
