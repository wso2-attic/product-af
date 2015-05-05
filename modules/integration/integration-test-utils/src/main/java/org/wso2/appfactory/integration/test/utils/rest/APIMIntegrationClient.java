package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFConstants;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestUtils;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by muthulee on 4/29/15.
 */
public class APIMIntegrationClient extends BaseClient {

	//src/site/blocks
	//resources/apis/add/ajax/add.jag - createApplication
	//resources/apis/add/block.jag
	//resources/apis/get/ajax/get.jag -getAPIsOfApp
	//resources/apis/get/block.jag
	//resources/apis/key/ajax/key.jag - getSavedKeys, keysExistsInAllStages

    public static final String ACTION = "applicationKey";
    public static final String APPLICATION_KEY="applicationKey";
    public static final String APPLICATION_ID="applicationId";
    public static final String APP_OWNER="appowner";
    public static final String USER_NAME="userName";
    public static final String APPIMGT_URL_SURFIX="/store/";
    public static final String API_NAME="name";
    public static final String API_VERSION="version";
    public static final String API_PROVIDER="provider";
    public static final String API_STATUS="status";
    public static final String PUBLISH_TO_GATWAY="publishToGateway";
    public static final String API_ACTION="action";


	public APIMIntegrationClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
	}

	public void createApplication(String applicationKey,String userName) throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put(ACTION, "createApplication");
		msgBodyMap.put(APPLICATION_KEY, applicationKey);
		msgBodyMap.put(USER_NAME, userName);
		HttpResponse response = super.doPostRequest("resources/apis/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return;
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

    public void getAPIsOfApp(String applicationKey,String appOwner ) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, "getAPIsOfApp");
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(APP_OWNER, appOwner);
        HttpResponse response = super.doPostRequest("resources/apis/get/ajax/get.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
        }
    }
    public void getSavedKeys(String applicationKey,String appOwner) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, "getSavedKeys");
        msgBodyMap.put(APPLICATION_KEY, applicationKey);
        msgBodyMap.put(APP_OWNER, appOwner);
        HttpResponse response = super.doPostRequest("resources/apis/get/ajax/get.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
        }
    }
    public void keysExistsInAllStages(String applicationId,String userName) throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(ACTION, "keysExistsInAllStages");
        msgBodyMap.put(APPLICATION_ID, applicationId);
        msgBodyMap.put(USER_NAME, userName);
        HttpResponse response = super.doPostRequest("resources/apis/get/ajax/get.jag", msgBodyMap);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            //TODO
            return;
        } else {
            throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
        }
    }
    public  void  addPAI() throws Exception {
        Map<String, String> msgBodyMap = new HashMap<String, String>();
        msgBodyMap.put(API_NAME, "YoutubeFeeds");
        msgBodyMap.put(API_VERSION, "1.0");
        msgBodyMap.put(API_PROVIDER, "provider1");
        msgBodyMap.put(API_STATUS, "PUBLISHED");
        msgBodyMap.put(PUBLISH_TO_GATWAY, "true");
        msgBodyMap.put(API_ACTION, "updateStatus");

        String urlSuffix="9769/store/?tenant=binali.com";
        String postBody = generateMsgBody(msgBodyMap);

//        HttpResponse response=HttpRequestUtil.doPost(new URL(getBackEndUrl() + APPIMGT_URL_SURFIX + urlSuffix), postBody,
//                getRequestHeaders());
//        if (response.getResponseCode() == HttpStatus.SC_OK) {
//            //TODO
//            return;
//        } else {
//            throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
//        }
    }





}
