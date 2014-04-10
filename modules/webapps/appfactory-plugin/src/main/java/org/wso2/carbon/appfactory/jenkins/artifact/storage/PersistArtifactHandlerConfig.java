package org.wso2.carbon.appfactory.jenkins.artifact.storage;

/**
 * Config object used to pass configs to PersistArtifactHandler
 * .
 */
public class PersistArtifactHandlerConfig {

    private String jobName;
    private String applicationArtifactExtension;
    private String tagName;
    private String persistentStoragePath;

    public String getApplicationArtifactExtension() {
        return applicationArtifactExtension;
    }

    public void setApplicationArtifactExtension(String applicationArtifactExtension) {
        this.applicationArtifactExtension = applicationArtifactExtension;
    }

    public String getPersistentStoragePath() {
        return persistentStoragePath;
    }

    public void setPersistentStoragePath(String persistentStoragePath) {
        this.persistentStoragePath = persistentStoragePath;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
