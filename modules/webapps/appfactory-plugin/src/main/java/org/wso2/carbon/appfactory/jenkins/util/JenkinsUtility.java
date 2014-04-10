package org.wso2.carbon.appfactory.jenkins.util;

public class JenkinsUtility {
	public static String getJobName(String applicationId, String version) {
        // Job name will be '<ApplicationId>-<version>-default'
        return applicationId.concat("-").concat(version).concat("-").concat("default");
    }
	public static String getApplicationId(String jobName) {
        // Job name will be '<ApplicationId>-<version>-default'
		String[] jobValues = jobName.split("-");
        return jobValues[0];
    }
	public static String getVersion(String jobName) {
        // Job name will be '<ApplicationId>-<version>-default'
		String[] jobValues = jobName.split("-");
        return jobValues[1];
    }
}
