package org.wso2.carbon.appfactory.jenkins.artifact.storage;

import hudson.Plugin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.jenkins.Constants;
import org.wso2.carbon.appfactory.jenkins.util.JenkinsUtility;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import java.io.IOException;
import java.util.Map;

@ExportedBean
public class AppfactoryArtifactStoragePlugin extends Plugin {

    private static final Log log = LogFactory.getLog(AppfactoryArtifactStoragePlugin.class);
    /**
     * This method serves the requests coming under <jenkins-url>/plugin/<plugin-name>
     * @param req request
     * @param rsp response
     * @throws IOException
     * @throws ServletException
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {

//        First we check the action.
//        The action get tag names does not depend on the deployer.
        String action = req.getRestOfPath();
        if ("/getTagNamesOfPersistedArtifacts".equals(action)) {

            Utils.getTagNamesOfPersistedArtifacts(req, rsp);
        }else{
//        First we check what is the class that we need to invoke
        	
        	String jobName = req.getParameter("jobName");
        	
        	String applicationId = JenkinsUtility.getApplicationId(jobName);
        	String version = JenkinsUtility.getVersion(jobName);
        	
        	String stage = req.getParameter("deployStage");
        	
        	String appType = null;
			try {
				appType = Utils.getJobConfigElement(jobName);
			} catch (Exception e) {
				String msg = "Error while reading the apptype from job config";
        		log.error(msg, e);
        		throw new ServletException(msg , e);
			}
        	
        	String className = null;
        	try {
			
			  className = Utils.getDeployerClassName(stage, appType);
				
			} catch (AppFactoryException e) {
				String msg = "Error while reading the class name from the config";
        		log.error(msg, e);
        		throw new ServletException(msg , e);
			}

            Deployer deployer;

            try {
                ClassLoader loader = getClass().getClassLoader();
                Class<?> customCodeClass = Class.forName(className, true, loader);
                deployer = (Deployer) customCodeClass.newInstance();
                
                Map<String, String[]> map = Utils.getParameterMapFromRequest(req) ;
                map.put(AppFactoryConstants.APPLICATION_ID, new String[]{applicationId});
                map.put(AppFactoryConstants.APPLICATION_VERSION , new String[]{version});

                //        log.info(" action : "+action);

                if ("/deployLatestSuccessArtifact".equals(action)) {
                    deployer.deployLatestSuccessArtifact(map);
                } else if("/deployPromotedArtifact".equals(action)) {
                    deployer.deployPromotedArtifact(map);
                } else {
                    rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    throw new ServletException("Invalid action");
                }
            } catch (ClassNotFoundException e) {
//                log.error(e);
                throw new ServletException(e);
            } catch (InstantiationException e) {
//                log.error(e);
                throw new ServletException(e);
            } catch (IllegalAccessException e) {
//                log.error(e);
                throw new ServletException(e);
            } catch (Exception e) {
//                log.error(e);
                throw new ServletException(e);
            }


        }
    }
}
