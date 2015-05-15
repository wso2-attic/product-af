/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.appfactory.integration.test.utils.external;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


/**
 * This class is use as a http client
 *
 */
public class HttpHandler {


    private static final Log log = LogFactory.getLog(HttpHandler.class);

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                if (hostname.equals("localhost"))
                    return true;
                return false;
            }
        });
    }

    /**
     * This method is use get a html file for given url
     *
     * @param url
     *          Web page url
     *
     * @return response
     *
     * @throws  java.io.IOException
     *            Throws this when failed to retrieve web page
     */
    public static String getHtml(String url,Map<String,String> headers) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        System.out.println("-------------------------------------------------------"+url);
        final SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        sslContext.init(null,null,null);

        SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, sf);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(content));
        StringBuffer responseBuffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            responseBuffer.append(line);
        }
        return responseBuffer.toString();
    }


    /**
     * This method is used to retrieve the redirection location from response header
     * as the request will results in either 301 or 302 status code.
     * @param url
     * @return
     * @throws IOException
     */
    public static String getRedirectionUrl(String url,Map<String,String> headers) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        sslContext.init(null,null,null);

        SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, sf);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = httpclient.execute(httpPost);

        final int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY ){
            for(Header header:response.getAllHeaders()){
            if(!header.getName().equals(HTTPConstants.HEADER_CONTENT_LENGTH))
                headers.put(header.getName(),header.getValue());
            }
            return response.getFirstHeader(HTTPConstants.HEADER_LOCATION).getValue();
        }
        return null;
    }

    /**
     * This method is use get a html file for given url
     *
     * @param url
     *          Web page url
     *
     * @return response
     *
     * @throws  java.io.IOException
     *            Throws this when failed to retrieve web page
     */


    /**
     * This method is used to do a https post request
     *
     * @param url
     *           request url
     *
     *
     * @return response
     *
     * @throws  java.io.IOException
     *             - Throws this when failed to fulfill a https post request
     */
    public Header doPostHttps(String url,Map<String,String> headers)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        System.out.print("---------------------"+url);

        final SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        sslContext.init(null,null,null);

        SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, sf);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        HttpPost httpPost = new HttpPost(url);
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }
        HttpResponse response = httpclient.execute(httpPost);
        return response.getFirstHeader("Set-Cookie");
    }

    /**
     * This method is used to do a http post request
     *
     * @param url
     *            request url
     *
     * @param payload
     *            Content of the post request
     *
     * @param sessionId
     *            sessionId for authentication
     *
     * @param contentType
     *            content type of the post request
     *
     * @return response
     *
     * @throws  java.io.IOException
     *             - Throws this when failed to fulfill a http post request
     */
    public String doPostHttp(String url, String payload, String sessionId, String contentType)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        sslContext.init(null,null,null);

        SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Scheme sch = new Scheme("https", 443, sf);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        //con.setRequestProperty("User-Agent", USER_AGENT);
        if (!sessionId.equals("") && !sessionId.equals("none")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + sessionId);
        }
        con.setRequestProperty("Content-Type", contentType);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if (sessionId.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                return session_id;
            } else if (sessionId.equals("appmSamlSsoTokenId")) {
                return con.getHeaderField("Set-Cookie").split(";")[0].split("=")[1];
            } else if (sessionId.equals("header")) {
                return con.getHeaderField("Set-Cookie").split("=")[1].split(";")[0];
            } else {
                return response.toString();
            }
        }
        return null;
    }

    /**
     * This method is used to do a http put request
     *
     * @param url
     *          request url
     *
     * @param sessionId
     *          sessionId for authentication
     *
     * @return response
     *
     * @throws  java.io.IOException
     *             - Throws this when failed to fulfill a http put request
     */
    public String doPut(String url, String sessionId) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        StringBuilder result = new StringBuilder();
        HttpPut putRequest = new HttpPut(url);
        putRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        putRequest.addHeader("Accept-Language", "en-US,en;q=0.5");
        putRequest.addHeader("Cookie", "JSESSIONID=" + sessionId);
        putRequest.addHeader("Accept-Encoding", "gzip, deflate");
        HttpResponse response = httpClient.execute(putRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatusLine().getStatusCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (response.getEntity().getContent())));
        String output;
        while ((output = br.readLine()) != null) {
            result.append(output);
        }
        return result.toString();
    }


    /**
     * This method is used to do a http get request
     *
     * @param url
     *          request url
     *
     * @param trackingCode
     *          tracking code of the web application
     *
     * @param appmSamlSsoTokenId
     *          appmSamlSsoTokenId id of the web application
     *
     * @param refer
     *          web page url
     *
     * @return response
     *
     * @throws  java.io.IOException
     *             Throws this when failed to fulfill a http get request
     */
    public String doGet(String url, String trackingCode, String appmSamlSsoTokenId, String refer) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        //add request header
        if (trackingCode.equals("")) {
            con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.setRequestProperty("Cookie", "JSESSIONID=" + appmSamlSsoTokenId);
        } else {
            con.setRequestProperty("Cookie", "appmSamlSsoTokenId=" + appmSamlSsoTokenId);
            con.setRequestProperty("trackingCode", trackingCode);
            con.setRequestProperty("Referer", refer);
        }
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
