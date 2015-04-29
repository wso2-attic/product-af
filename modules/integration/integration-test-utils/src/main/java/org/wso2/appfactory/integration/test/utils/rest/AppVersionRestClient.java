package org.wso2.appfactory.integration.test.utils.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Application version stub
 */
public class AppVersionRestClient extends BaseRestClient {

    public static final String APPLICATION_KEY = "applicationKey";
    public static final String SRC_VERSION = "srcVersion";
    public static final String TARGET_VERSION = "targetVersion";
    public static final String LIFECYCLE_NAME = "lifecycleName";
    public static final String LIFECYCLE = "ApplicationLifecycle";
    private static final String REQUEST_KEY_ACTION ="Action" ;
    public static final String INVOKE_DO_VERSION = "invokeDoVersion";

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
        msgBody.put(APPLICATION_KEY, applicationKey);
        msgBody.put(SRC_VERSION, srcVersion);
        msgBody.put(TARGET_VERSION, targetVersion);
        msgBody.put(LIFECYCLE_NAME, LIFECYCLE);
        msgBody.put(REQUEST_KEY_ACTION, INVOKE_DO_VERSION);
        doPostRequest(APPMGT_LIFECYCLE_ADD, msgBody);
    }
}
