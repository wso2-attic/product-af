package org.wso2.carbon.appfactory.git;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold axis2 configuration context
 */
public class ContextHolder {
    private static final Logger log = LoggerFactory.getLogger(ContextHolder.class);
    private static ContextHolder holder;
    private ConfigurationContext configurationContext;
    private ApplicationCache cache;
    private  UserPasswordCache passwordCache;

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }
    public ApplicationCache getCache(){
        return cache;
    }
    public UserPasswordCache getUserPasswordCache(){
        return passwordCache;
    }
    public static ContextHolder getHolder(GitBlitConfiguration configuration) {
        if(holder==null){
            holder=new ContextHolder();
            try {
                log.info("Creating Default Axis2 ConfigurationContext");
                holder.configurationContext= ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            } catch (AxisFault fault) {
                log.error("Error occurred while initializing  ConfigurationContext", fault);
            }
            holder.cache=new ApplicationCache(configuration);
            holder.passwordCache=new UserPasswordCache(configuration);
        }

        return holder;
    }



    private ContextHolder() {
    }

}
