package org.wso2.appfactory.integration.test.utils.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import java.net.URL;

/**
 * Created by binalip91 on 4/30/15.
 */
public class APIRestClient extends BaseRestClient {

    public static final String ADDAPI_ACTION = "addAPI&name=YoutubeFeeds&visibility=public&version=1.0.0&description=Youtube " +
            "Live Feeds&endpointType=nonsecured&http_checked=http&https_checked=https&&wsdl=&tags=youtube,gdata," +
            "multimedia&tier=Silver&" +
            "thumbUrl=http://www.10bigideas.com.au/www/573/files/pf-thumbnail-youtube_logo.jpg&context=/youtube&" +
            "tiersCollection=Gold&resourceCount=0&resourceMethod-0=GET&resourceMethodAuthType-0=Application&" +
            "resourceMethodThrottlingTier-0=Unlimited&uriTemplate-0=/*";

    public static final String ENDPOINT_CONFIG="{\"production_endpoints\":{\"url\":\"http://gdata.youtube.com/feeds/api/standardfeeds\",\"config\":null},\"endpoint_type\":\"http\"}";
    public static final String APPMGT_URL_SURFIX="/publisher/site/blocks/item-add/ajax/add.jag";
    public static final String APPMGT_LOGGIN_URL_SURFIX="/publisher/site/blocks/user/login/ajax/login.jag";

    public APIRestClient(String backEndUrl, String username, String password) throws Exception {
        super(backEndUrl, username, password);
    }

    /**
     *@param userName
     * @param password
     * @throws Exception
     */

    public void addAPI(String userName,String password) throws Exception {
        logAPIM(userName,password);
        HttpResponse response = HttpRequestUtil.doPost(
                new URL(getBackEndUrl() + APPMGT_URL_SURFIX ),
                "action=" + ADDAPI_ACTION+ENDPOINT_CONFIG, getRequestHeaders());

        if (response.getResponseCode() == HttpStatus.SC_OK && response.getData().equals("true")) {
            checkErrors(response);
            //TODO
        } else {
            throw new AFIntegrationTestException("Add API failed " + response.getData());
        }
    }
    /**
     * This method will use to log in to APIM
     * @param userName
     * @param password
     * @return
     */
    public void logAPIM(String userName,String password) throws Exception {
        HttpResponse response = HttpRequestUtil.doPost(
                new URL(getBackEndUrl() + APPMGT_LOGGIN_URL_SURFIX ),
                "action=login&username="+userName+"&password=" + password, getRequestHeaders());

        if (response.getResponseCode() == HttpStatus.SC_OK && response.getData().equals("true")) {
            checkErrors(response);
            //TODO
        } else {
            throw new AFIntegrationTestException("Add API failed " + response.getData());
        }
    }


}

