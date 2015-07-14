package org.wso2.appfactory.integration.test.utils.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.json.JSONArray;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * REST client for build and repo rest apis
 */
public class BuildRepoClient extends BaseClient {

	private final static String REPOANDBUILD_ADD_ENDPOINT = "reposBuilds/add/ajax/add.jag";
    private final static String REPOANDBUILD_GET_ENDPOINT = "reposBuilds/get/ajax/get.jag";
    private final static String REPOANDBUILD_LIST_ENDPOINT = "reposBuilds/list/ajax/list.jag";
    private final static String REPOANDBUILD_SET_ENDPOINT = "reposBuilds/set/ajax/set.jag";


	/**
	 * Construct authenticates REST client to invoke appmgt functions
	 *
	 * @param backEndUrl backend url
	 * @param username   username
	 * @param password   password
	 * @throws Exception
	 */
	public BuildRepoClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
	}

	/**
	 * Rest call to deploy artifact
	 * @param applicationKey application key
	 * @param stage stage to deploy
	 * @param version version of application to deploy
	 * @param tagName tag name to deploy
	 * @param deployAction deploy action name
	 * @return http response object returns from rest endpoint
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
	 */
	public String deployArtifact(String applicationKey, String stage, String version, String tagName,
	                                   String deployAction) throws AFIntegrationTestException {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "deployArtifact");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("stage", stage);
		msgBody.put("version", version);
		msgBody.put("tagName", tagName);
		msgBody.put("deployAction", deployAction);
		HttpResponse response = doPostRequest(REPOANDBUILD_ADD_ENDPOINT, msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
            return  response.getData();
        } else {
			throw new AFIntegrationTestException("Deploy Artifact failed " +response.getResponseCode()+" " +
                    response.getData());
		}
	}

    /**
     * Rest call to create fork
     * @param applicationKey application key
     * @param userNameArray username list comma seperated
     * @param type type
     * @param version version
     * @return Is fork created
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    public boolean createFork(String applicationKey, String userNameArray, String type, String version) throws
            AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "createFork");
        msgBody.put("applicationKey", applicationKey);
        msgBody.put("userNameArray", userNameArray);
        msgBody.put("type", type);
        msgBody.put("version", version);
        HttpResponse response = doPostRequest(REPOANDBUILD_SET_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.parseBoolean(response.getData());
        } else {
            throw new AFIntegrationTestException("Create fork is failed. "+response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Rest call to create fork branch
     * @param applicationKey application key
     * @param userNameArray username list comma seperated
     * @param type type
     * @param version version
     * @return Is fork created
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    public boolean createForkBranch(String applicationKey, String userNameArray, String type, String version) throws
            AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "createForkBranch");
        msgBody.put("applicationKey", applicationKey);
        msgBody.put("userNameArray", userNameArray);
        msgBody.put("type", type);
        msgBody.put("version", version);
        HttpResponse response = doPostRequest(REPOANDBUILD_SET_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return Boolean.parseBoolean(response.getData());
        } else {
            throw new AFIntegrationTestException("Create fork branch is failed. " +response.getResponseCode()+" "
                    + response.getData());
        }
    }

    /**
     * Rest call to get build deployment configs
     * @param applicationKey application key
     * @param version version
     * @param autoBuild auto build
     * @param autoDep auto deployment
     * @return Build deployment configs
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    public JsonObject setBuildDelopymentConfigs(String applicationKey, String version, String autoBuild, String autoDep)
            throws AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "setBuildDelpymentConfigs");
        msgBody.put("applicationKey", applicationKey);
        msgBody.put("version", version);
        msgBody.put("autoBuild", autoBuild);
        msgBody.put("autoDep", autoDep);
        HttpResponse response = doPostRequest(REPOANDBUILD_SET_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseJSON = response.getData();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(responseJSON);
            JsonObject dataobject = jsonElement.getAsJsonObject();
            return  dataobject;
        } else {
            throw new AFIntegrationTestException("Set build deployment configs is failed. " +response.getResponseCode()
                    +" "+ response.getData());
        }
    }

    /**
     * Rest call for get build and repo data of forked repo
     * @param applicationKey application key
     * @param isRoleBasedPermissionAllowed is role based permission allowed
     * @param metaDataNeed is meta data needed
     * @param buildableforstage is buildable
     * @return build and repo data of forked repo
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    public JsonObject getBuildAndRepoDataForkedRepo(String applicationKey,
                                                String isRoleBasedPermissionAllowed, String metaDataNeed,
                                                String buildableforstage,String username)
            throws AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "getbuildandrepodataforkedrepo");
        msgBody.put("applicationKey", applicationKey);
        msgBody.put("isRoleBasedPermissionAllowed", isRoleBasedPermissionAllowed);
        msgBody.put("metaDataNeed", metaDataNeed);
        msgBody.put("buildableforstage", buildableforstage);
        msgBody.put("userName",username);
        HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseJSON = response.getData();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(responseJSON);
            JsonObject dataobject = jsonElement.getAsJsonObject();
            return dataobject;
        } else {
            throw new AFIntegrationTestException("Get build and repo data for forked repo is failed. "
                    +response.getResponseCode()+" " +response.getData());
        }
    }

    /**
     * Rest call to get build and repo data for version
     * @param applicationKey application key
     * @param version version
     * @param username username
     * @return Build and repo data for version
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    public JsonObject getBuildAndRepoDataForVersion(String applicationKey, String version, String username)
            throws AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "getBuildAndRepoDataForVersion");
        msgBody.put("applicationKey", applicationKey);
        msgBody.put("userName",username);
        msgBody.put("version", version);
        HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseJSON = response.getData();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(responseJSON);
            JsonArray dataArray = jsonElement.getAsJsonArray();
            JsonObject dataObject = dataArray.get(0).getAsJsonObject();
            return  dataObject;
        } else {
            throw new AFIntegrationTestException("Get build and repo data for version is failed. "+
                    response.getResponseCode()+" "+response.getData());
        }
    }

    /**
     * Rest call to get build info by application id
     * @param applicationKey application key
     * @return response returned by rest endpoint
     * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
     */
    public JsonArray buildInfoByAppId(String applicationKey) throws AFIntegrationTestException {
        Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "buildinfobyappid");
        msgBody.put("applicationKey", applicationKey);
        HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            String responseJSON = response.getData();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(responseJSON);
            JsonArray dataArray = jsonElement.getAsJsonArray();
            return dataArray;
        } else {
            throw new AFIntegrationTestException("Get build info by application id is failed. "
                    +response.getResponseCode()+" " + response.getData());
        }
    }
	/**
	 * Rest call to get build logs url
	 * @param applicationKey application key
	 * @param applicationVersion application version
	 * @param lastBuildNo last build no
	 * @return response returned by rest endpoint
	 * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
	 */
	public String getBuildLogsUrl(String applicationKey, String applicationVersion
            , String lastBuildNo)
			throws AFIntegrationTestException {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getBuildLogsUrl");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("applicationVersion", applicationVersion);
		msgBody.put("lastBuildNo", lastBuildNo);
		HttpResponse response = doPostRequest(REPOANDBUILD_GET_ENDPOINT , msgBody);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            return  response.getData();
		} else {
			throw new AFIntegrationTestException("Get build log url failed. "+response.getResponseCode()+" "
                    + response.getData());
		}
	}

    /**
	 * Rest call to get build and repo data
	 * @param applicationKey application key
	 * @param isRoleBasedPermissionAllowed is role based permission allowed
	 * @param metaDataNeed is meta data needed
	 * @param buildableforstage is buildable for the stage
	 * @return response returned by rest endpoint
	 * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
	 */
	public JsonArray getBuildAndRepoData(String applicationKey, String isRoleBasedPermissionAllowed,
                                         String metaDataNeed, String buildableforstage)
                                         throws AFIntegrationTestException{
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getbuildandrepodata");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("isRoleBasedPermissionAllowed", isRoleBasedPermissionAllowed);
		msgBody.put("metaDataNeed", metaDataNeed);
		msgBody.put("buildableforstage", buildableforstage);
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			String responseJSON = response.getData();
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(responseJSON);
			JsonArray dataobject = jsonElement.getAsJsonArray();
            return dataobject;
		} else {
			throw new AFIntegrationTestException("Get build and repo data by application id is failed. "
                    + response.getData());
		}
	}

	/**
	 * Rest call for get jenkins url
	 * @return jenkins url for tenant
	 * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
	 */
	public String getJenkinsURL() throws AFIntegrationTestException{
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getJenkinsURL");
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
            return  response.getData();
        } else {
			throw new AFIntegrationTestException("Get jenkins url is failed. " + response.getData());
		}
	}



	/**
	 * Rest call to get build and deploy status for version
	 * @param applicationKey application key
	 * @param version version
	 * @return build and deploy status for version
	 * @throws org.wso2.appfactory.integration.test.utils.AFIntegrationTestException
	 */
	public JsonObject getBuildAndDeployStatusForVersion(String applicationKey, String version)
            throws AFIntegrationTestException {
		Map<String, String> msgBody = new HashMap<String, String>();
        msgBody.put("action", "getBuildAndDeployStatusForVersion");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("version", version);
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			String responseJSON = response.getData();
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(responseJSON);
			JsonObject dataobject = jsonElement.getAsJsonObject();
            return dataobject;
        } else {
			throw new AFIntegrationTestException("Get build and deploy status for version is failed. "
                    + response.getData());
		}
	}



}
