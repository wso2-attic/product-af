package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by muthulee on 4/29/15.
 */
public class APIMIntegrationTestClient extends BaseRestClient {

	//src/site/blocks
	//resources/apis/add/ajax/add.jag - createApplication
	//resources/apis/add/block.jag
	//resources/apis/get/ajax/get.jag -getAPIsOfApp
	//resources/apis/get/block.jag
	//resources/apis/key/ajax/key.jag - getSavedKeys, keysExistsInAllStages


	public APIMIntegrationTestClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
	}

	public void createApplication() throws Exception {
		Map<String, String> msgBodyMap = new HashMap<String, String>();
		msgBodyMap.put("action", "createApplication");
		msgBodyMap.put("applicationKey", "thisappp");
		msgBodyMap.put("username", "thisapp");
		HttpResponse response = super.doPostRequest("resources/apis/add/ajax/add.jag", msgBodyMap);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			//TODO
			return;
		} else {
			throw new AFIntegrationTestException("GetAppInfo failed " + response.getData());
		}
	}

}
