package org.wso2.carbon.appfactory.jenkins;

public class Constants {

    public static final String APPLICATION_TYPE_WAR = "war";
    public static final String APPLICATION_TYPE_CAR = "car";
    public static final String APPLICATION_TYPE_ZIP = "zip";
    public static final String APPLICATION_TYPE_JAXWS = "jaxws";
    public static final String APPLICATION_TYPE_JAXRS = "jaxrs";
    public static final String APPLICATION_TYPE_JAGGERY = "jaggery";
    public static final String APPLICATION_TYPE_DBS = "dbs";
    public static final String APPLICATION_TYPE_BPEL = "bpel";
    public static final String APPLICATION_TYPE_PHP = "php";
    public static final String APPLICATION_TYPE_ESB = "esb";
    public static final String APPLICATION_TYPE_XML = "xml";

    public static final String APPLICATION_ID = "applicationId";
    public static final String JOB_NAME = "jobName";
    public static final String TAG_NAME = "tagName";
    public static final String DEPLOY_STAGE = "deployStage";
    public static final String ARTIFACT_TYPE = "artifactType";
    public static final String DEPLOYMENT_SERVER_URLS = "DeploymentServerURL";
    public static final String ESBDEPLOYMENT_SERVER_URLS = "ESBDeploymentServerURL";
    public static final String DEPLOY_ACTION = "deployAction";
    
    public static final String JENKINS_ADMIN_USERNAME_PATH = "JenkinsServerAdminUsername";
    public static final String JENKINS_ADMIN_PASSWORD_PATH = "JenkinsServerAdminPassword";
    
    public static final String JENKINS_HOME = "JENKINS_HOME";
    public static final String JOB_CONFIG_XPATH = "/*/publishers/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager/applicationArtifactExtension";
    
}
