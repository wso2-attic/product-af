package org.wso2.carbon.appfactory.core;

import java.util.Map;

import org.wso2.carbon.appfactory.common.AppFactoryException;

public interface Deployer {

    public void deployTaggedArtifact(Map<String,String[]> requestParameters) throws Exception;

    public void deployLatestSuccessArtifact(Map<String,String[]> requestParameters) throws Exception;
    
    public void deployPromotedArtifact(Map<String,String[]> requestParameters) throws Exception;

    public void unDeployArtifact(Map<String,String[]> requestParameters) throws Exception;
    
    public void handleException(String msg, Exception e) throws AppFactoryException ;
    
}
