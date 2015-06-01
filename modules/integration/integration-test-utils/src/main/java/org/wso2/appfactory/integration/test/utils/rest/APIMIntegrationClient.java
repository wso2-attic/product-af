/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appfactory.integration.test.utils.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.appfactory.integration.test.utils.AFIntegrationTestException;
import org.wso2.appfactory.integration.test.utils.external.HttpHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Rest client for handle api invocations for apps
 */
public class APIMIntegrationClient extends BaseClient {
    private static final Log log = LogFactory.getLog(APIMIntegrationClient.class);
    private static final String ACTION = "action";
    private static final String APPLICATION_KEY="applicationKey";
    private static final String APP_OWNER="appowner";
    private static final String USER_NAME="username";
    private static final String ISSYNC = "isSync";
    private static final String USER = "userName";
    public static String samlRequest=null;
    private Header cookie=null;
    private static SSLContext sslContext =null;
    private static HttpClient httpclient=null;

    public APIMIntegrationClient(String backEndUrl, String username, String password) throws Exception {
		super(backEndUrl, username, password);
    }

    @Override
    protected void login(String userName, String password) throws Exception {
        super.login(userName,password);
        retrieveSAMLToken(userName, password);
    }

    private void retrieveSAMLToken(String userName, String password) throws KeyManagementException,
            NoSuchAlgorithmException, IOException {
        String ssoUrl = getBackEndUrl()+ "samlsso";
        String url = getBackEndUrl()+ "commonauth";
        String webAppurl = getBackEndUrl() + "appmgt/site/pages/index.jag";
        String loginHtmlPage = null;
        String commonAuthUrl=null;
        String responceHtml = null;
        HttpHandler httpHandler = new HttpHandler();
        Map<String,String> headers=getRequestHeaders();
        try {
            loginHtmlPage = httpHandler.getHtml(webAppurl,headers );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
            Document html = Jsoup.parse(loginHtmlPage);
            Element samlRequestElement = html.select("input[name=SAMLRequest]").first();
            samlRequest = samlRequestElement.val();
            Element relayStateElement = html.select("input[name=RelayState]").first();
            String relayState = relayStateElement.val();
            Element ssoAuthSessionIDElement = html.select("input[name=SSOAuthSessionID]").first();
            String ssoAuthSessionID = ssoAuthSessionIDElement.val();
            samlRequest = samlRequest.replace("+","%2B");
            samlRequest = samlRequest.replace("=","%3D");

        try {
            commonAuthUrl = httpHandler.getRedirectionUrl(ssoUrl + "?SAMLRequest=" + samlRequest + "&RelayState=" + relayState + "&SSOAuthSessionID="
                    + ssoAuthSessionID,headers);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        String val = commonAuthUrl.split("sessionDataKey=")[1].split("&")[0];
            cookie = httpHandler.doPostHttps(url+"?sessionDataKey="+val+"&username="+userName+"&password="+password,headers);
            }
    /**
     *
     * @param parameters Map<String, String> parameters
     * @param path path
     * @return value of data element of the response
     * @throws Exception
     */
    public HttpPost getHttpClient(Map<String, String> parameters,String path) throws NoSuchAlgorithmException,
            KeyManagementException, UnsupportedEncodingException {
        sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        sslContext.init(null,null,null);
        SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, sf);
        httpclient = new DefaultHttpClient();
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        HttpPost post = new HttpPost(getBackEndUrl()+ path);
        post.setHeader(cookie);

        Map<String,String> headers=getRequestHeaders();
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            post.setHeader(entry.getKey(), entry.getValue());
        }
        // add headers
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        for(Map.Entry<String, String> entry:parameters.entrySet()){
            urlParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        return post;

    }

    /**
	 *
	 * @param applicationKey applicationKey
	 * @param userName userName
     * @return value of data element of the response
     * @throws Exception
	 */
	public boolean createApplication(String action,String applicationKey, String userName) throws Exception {
        // add header
        Map<String, String> entry =new HashMap<String,String>();
        entry.put(ACTION, action);
        entry.put(APPLICATION_KEY,applicationKey);
        entry.put(USER_NAME,userName);
        entry.put("SAML_TOKEN",samlRequest);

        HttpPost post=getHttpClient(entry,CREAT_APP_URL);
        HttpResponse response = (HttpResponse) httpclient.execute(post);

        /* process response */
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try {
                String responseString = response.getData();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(responseString);
                boolean addIssueResponse = jsonElement.getAsBoolean();
                if(addIssueResponse)
                    return true;
            }catch(Exception e){
                return false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
        return false;
    }
    /**
     *
     * @param applicationKey applicationKey
     * @param appOwner appOwner
     * @throws Exception
     * @return value of data element of the response
     * @throws Exception
     */
    public boolean getAPIsOfApp(String action,String applicationKey,String appOwner ) throws Exception {
        // add header
        Map<String, String> entry =new HashMap<String,String>();
        entry.put(ACTION, "getAPIsOfApp");
        entry.put(APPLICATION_KEY,applicationKey);
        entry.put(APP_OWNER,appOwner);
        entry.put("SAML_TOKEN",samlRequest);
        HttpPost post = getHttpClient(entry,GETAPIS_URL);

        HttpResponse response = (HttpResponse) httpclient.execute(post);
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try {
                String responseString = response.getData();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(responseString);
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if(jsonArray.size() > 0){
                    return true;
                }else{
                    return  false;
                }
            }catch (Exception e){
            }

        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());

        }
        return false;
    }
    /**
     *
     * @param applicationKey applicationKey
     * @param appOwner appOwner
     * @throws Exception
     * @return value of data element of the response
     * @throws Exception
     */
    public boolean getSavedKeys(String action,String applicationKey,String appOwner) throws Exception {
        // add header
        Map<String, String> entry =new HashMap<String,String>();
        entry.put(ACTION,action);
        entry.put(APPLICATION_KEY,applicationKey);
        entry.put(USER,appOwner);
        entry.put(ISSYNC,"true");
        entry.put("SAML_TOKEN",samlRequest);
        HttpPost post = getHttpClient(entry,GETSAVEDKEYS_URL);

        HttpResponse response = (HttpResponse) httpclient.execute(post);
        /* process response */
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try {
                String responseString = response.getData();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(responseString);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if(!jsonObject.get("keyName").equals(null))
                    return true;
            }catch(Exception e){
                return false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
        return false;
    }
    /**
     *
     * @param applicationId applicationId
     * @param userName userName
     * @throws Exception
     * @return value of data element of the response
     * @throws Exception
     */
    public boolean keysExistsInAllStages(String action,String applicationId,String userName,String IsSync) throws Exception {
        // add header
        Map<String, String> entry =new HashMap<String,String>();
        entry.put(ACTION, action);
        entry.put(APPLICATION_KEY,applicationId);
        entry.put(USER,userName);
        entry.put(ISSYNC,IsSync);
        entry.put("SAML_TOKEN",samlRequest);
        HttpPost post = getHttpClient(entry,KEYEXISTS_URL);

        HttpResponse response = (HttpResponse) httpclient.execute(post);
            /* process response */
        if (response.getResponseCode() == HttpStatus.SC_OK) {
            try {
                String responseString = response.getData();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(responseString);
                boolean iskeysExistsInAllStages = jsonElement.getAsBoolean();
                if (iskeysExistsInAllStages) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            throw new AFIntegrationTestException("" + response.getResponseCode() + response.getData());
        }
    }

}
