package org.wso2.carbon.cloudmgt.users.util.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSPasswordCallback;
import org.wso2.carbon.cloudmgt.users.service.UserManagementException;
import org.wso2.carbon.cloudmgt.users.util.UserMgtUtil;

public class PWCBHandler implements CallbackHandler {

	private static final Log log =  LogFactory.getLog(PWCBHandler.class);
    public void handle(Callback[] callbacks) throws IOException,
            UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {
            WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
            String id = pwcb.getIdentifer();
            int usage = pwcb.getUsage();


            if (usage == WSPasswordCallback.USERNAME_TOKEN) {
                // Logic to get the password to build the username token
            	try {
					 if (UserMgtUtil.getAdminUsername().equals(id)) {
		                    pwcb.setPassword(UserMgtUtil.getAdminPassword());
		                } else {
		                    //not authenticated
		                }
				} catch (UserManagementException e) {
					log.error("Error handling password call back ",e);
				}
               
            }
        }
    }

}