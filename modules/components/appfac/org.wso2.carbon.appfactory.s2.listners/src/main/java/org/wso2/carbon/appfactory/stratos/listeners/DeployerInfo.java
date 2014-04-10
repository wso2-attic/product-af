package org.wso2.carbon.appfactory.stratos.listeners;

public class DeployerInfo {

    private String alias;
    private String cartridgeType;
    private String repoURL;
    private String dataCartridgeType;
    private String dataCartridgeAlias;
    private String policyName;
    private String endpoint;


    private String baseURL;
    private String className;
    private Class<?> repoProvider;
    private String adminUserName;
    private String adminPassword;
    private String repoPattern;

    private String appType;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public Class<?> getRepoProvider() {
        return repoProvider;
    }

    public void setRepoProvider(Class repoProvider) {
        this.repoProvider = repoProvider;
    }

    public String getRepoPattern() {
        return repoPattern;
    }

    public void setRepoPattern(String repoPattern) {
        this.repoPattern = repoPattern;
    }
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCartridgeType() {
        return cartridgeType;
    }

    public void setCartridgeType(String cartridgeType) {
        this.cartridgeType = cartridgeType;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public String getDataCartridgeType() {
        return dataCartridgeType;
    }

    public void setDataCartridgeType(String dataCartridgeType) {
        this.dataCartridgeType = dataCartridgeType;
    }

    public String getDataCartridgeAlias() {
        return dataCartridgeAlias;
    }

    public void setDataCartridgeAlias(String dataCartridgeAlias) {
        this.dataCartridgeAlias = dataCartridgeAlias;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
    
    
}