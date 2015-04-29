package org.wso2.appfactory.integration.test.utils.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AppFactoryIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * REST client for build and repo rest apis
 */
public class BuildRepoRestClient extends BaseRestClient {

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
	public BuildRepoRestClient(String backEndUrl, String username, String password) throws Exception {
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
	 */
	public void deployArtifact(String applicationKey, String stage, String version, String tagName,
	                                   String deployAction) throws Exception {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "deployArtifact");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("stage", stage);
		msgBody.put("version", version);
		msgBody.put("tagName", tagName);
		msgBody.put("deployAction", deployAction);
		HttpResponse response = doPostRequest(REPOANDBUILD_ADD_ENDPOINT, msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			return;
		} else {
			throw new AppFactoryIntegrationTestException("Deploy Artifact failed " + response.getData());
		}
	}

	/**
	 * Rest call to get build logs url
	 * @param applicationKey application key
	 * @param applicationVersion application version
	 * @param lastBuildNo last build no
	 * @return response returned by rest endpoint
	 * @throws Exception
	 */
	public String getBuildLogsUrl(String userName, String applicationKey, String applicationVersion, String lastBuildNo)
			throws Exception {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getBuildLogsUrl");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("applicationVersion", applicationVersion);
		msgBody.put("lastBuildNo", lastBuildNo);
		HttpResponse response = doPostRequest(REPOANDBUILD_GET_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			return response.getData();
		} else {
			throw new AppFactoryIntegrationTestException("Get build log url failed. " + response.getData());
		}
	}

	/**
	 * Rest call to get build info by application id
	 * @param applicationKey application key
	 * @return response returned by rest endpoint
	 * @throws Exception
	 */
	public JsonObject buildInfoByAppId(String applicationKey) throws Exception {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "buildinfobyappid");
		msgBody.put("applicationKey", applicationKey);
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			String responseJSON = response.getData();
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(responseJSON);
			JsonObject dataobject = jsonElement.getAsJsonObject();
			return dataobject;
		} else {
			throw new AppFactoryIntegrationTestException("Get build info by application id is failed. " + response.getData());
		}
	}

	/**
	 * Rest call to get build and repo data
	 * @param applicationKey application key
	 * @param isRoleBasedPermissionAllowed is role based permission allowed
	 * @param metaDataNeed is meta data needed
	 * @param buildableforstage is buildable for the stage
	 * @return response returned by rest endpoint
	 * @throws Exception
	 */
	public JsonObject getBuildAndRepoData(String applicationKey, String userName, String isRoleBasedPermissionAllowed,
	                                  String metaDataNeed, String buildableforstage) throws Exception{
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
			JsonObject dataobject = jsonElement.getAsJsonObject();
			return dataobject;
		} else {
			throw new AppFactoryIntegrationTestException("Get build and repo data by application id is failed. " + response.getData());
		}
	}

	/**
	 * Rest call for get jenkins url
	 * @return jenkins url for tenant
	 * @throws Exception
	 */
	public String getJenkinsURL() throws Exception{
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getJenkinsURL");
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			return response.getData();
		} else {
			throw new AppFactoryIntegrationTestException("Get jenkins url is failed. " + response.getData());
		}
	}

	/**
	 * Rest call for create codenvy url
	 * @param applicationKey application key
	 * @param gitURL git url
	 * @param version version
	 * @param appType application type
	 * @return codenvy url
	 * @throws Exception
	 */
	public String createCodeEnvyUrl(String applicationKey, String gitURL, String version, String appType)
			throws Exception {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "createCodeEnvyUrl");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("gitURL", gitURL);
		msgBody.put("version", version);
		msgBody.put("appType", appType);
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			return response.getData();
		} else {
			throw new AppFactoryIntegrationTestException("Create codenvy url is failed. " + response.getData());
		}
	}

	/**
	 * Rest call for get build and repo data of forked repo
	 * @param applicationKey application key
	 * @param isRoleBasedPermissionAllowed is role based permission allowed
	 * @param metaDataNeed is meta data needed
	 * @param buildableforstage is buildable
	 * @return build and repo data of forked repo
	 * @throws Exception
	 */
	public JsonObject getBuildAndRepoDataForkedRepo(String applicationKey, String userName,
	                                            String isRoleBasedPermissionAllowed, String metaDataNeed,
	                                            String buildableforstage) throws Exception {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getbuildandrepodataforkedrepo");
		msgBody.put("applicationKey", applicationKey);
		msgBody.put("isRoleBasedPermissionAllowed", isRoleBasedPermissionAllowed);
		msgBody.put("metaDataNeed", metaDataNeed);
		msgBody.put("buildableforstage", buildableforstage);
		HttpResponse response = doPostRequest(REPOANDBUILD_LIST_ENDPOINT , msgBody);
		if (response.getResponseCode() == HttpStatus.SC_OK) {
			String responseJSON = response.getData();
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(responseJSON);
			JsonObject dataobject = jsonElement.getAsJsonObject();
			return dataobject;
		} else {
			throw new AppFactoryIntegrationTestException("Get build and repo data for forked repo is failed. " + response.getData());
		}
	}

	/**
	 * Rest call to get build and deploy status for version
	 * @param applicationKey application key
	 * @param version version
	 * @return build and deploy status for version
	 * @throws Exception
	 */
	public JsonObject getBuildAndDeployStatusForVersion(String applicationKey, String version) throws Exception {
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
			throw new AppFactoryIntegrationTestException("Get build and deploy status for version is failed. " + response.getData());
		}
	}

	/**
	 * Rest call to get build and repo data for version
	 * @param applicationKey application key
	 * @param version version
	 * @return Build and repo data for version
	 * @throws Exception
	 */
	public JsonObject getBuildAndRepoDataForVersion(String applicationKey, String version) throws Exception {
		Map<String, String> msgBody = new HashMap<String, String>();
		msgBody.put("action", "getBuildAndRepoDataForVersion");
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
			throw new AppFactoryIntegrationTestException("Get build and repo data for version is failed. " + response.getData());
		}
	}

	/**
	 * Rest call to get build deployment configs
	 * @param applicationKey application key
	 * @param version version
	 * @param autoBuild auto build
	 * @param autoDep auto deployment
	 * @return Build deployment configs
	 * @throws Exception
	 */
	public JsonObject setBuildDelopymentConfigs(String applicationKey, String version, String autoBuild, String autoDep) throws Exception {
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
			return dataobject;
		} else {
			throw new AppFactoryIntegrationTestException("Set build deployment configs is failed. " + response.getData());
		}
	}

	/**
	 * Rest call to create fork
	 * @param applicationKey application key
	 * @param userNameArray username list comma seperated
	 * @param type type
	 * @param version version
	 * @return Is fork created
	 * @throws Exception
	 */
	public boolean createFork(String applicationKey, String userNameArray, String type, String version) throws Exception {
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
			throw new AppFactoryIntegrationTestException("Create fork is failed. " + response.getData());
		}
	}

	/**
	 * Rest call to create fork branch
	 * @param applicationKey application key
	 * @param userNameArray username list comma seperated
	 * @param type type
	 * @param version version
	 * @return Is fork created
	 * @throws Exception
	 */
	public boolean createForkBranch(String applicationKey, String userNameArray, String type, String version) throws Exception {
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
			throw new AppFactoryIntegrationTestException("Create fork branch is failed. " + response.getData());
		}
	}

}
