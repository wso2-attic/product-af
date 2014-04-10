package org.wso2.appfactory.dynamicslave;

import hudson.model.Hudson;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton client class used to call remote API of a jenkins master
 */
public class JenkinsClient {
    private static final Logger LOGGER = Logger.getLogger(JenkinsClient.class.getName());
    public static final String MASTER_URL = "masterURL";
    public static final String SLAVE_NAME = "slaveName";

    private String url;
    private static JenkinsClient jenkinsClient;
    private HttpClient httpClient;


    public JenkinsClient(String username, String password, String url) {
        httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        httpClient.getState()
                .setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));
        httpClient.getParams().setAuthenticationPreemptive(true);
        this.url = url;
        LOGGER.log(Level.FINEST, "Creating a client for " + url);
    }

    public static JenkinsClient getClientInstance() {
        if (JenkinsClient.jenkinsClient == null) {
            DynamicSlaveCloud cloud = Hudson.getInstance().clouds.get(DynamicSlaveCloud.class);
            JenkinsClient.jenkinsClient = new JenkinsClient(cloud.getMasterUsername(),
                    cloud.getMasterPassword(), cloud.getMasterURL());
        }

        return JenkinsClient.jenkinsClient;

    }

    public static String getJobTemplate(Map<String, String> parameters) {
        InputStream inputStream = JenkinsClient.class.getResourceAsStream("/job.xml");

        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
        } catch (IOException e) {
            String msg = "Error while reading the raw template(job.xml) from class path";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new RuntimeException(msg, e);
        }
        String theTemplate = writer.toString();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "The template with parameters " + theTemplate);
        }
        for (String value : parameters.keySet().toArray(new String[parameters.keySet().size()])) {

            theTemplate = theTemplate.replace("${" + value + "}", parameters.get(value));

        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "The template with values " + theTemplate);
        }
        return theTemplate;

    }

    public String get(String path, Map<String, String> query) {
        GetMethod getMethod = createGetMethod(path, getNameValuePairFromMap(query));
        int statusCode;
        String response = null;
        try {
            httpClient.executeMethod(getMethod);
        } catch (IOException e) {
            LOGGER.info("Invocation failed for " + getMethod.getPath());
            return null;
        } finally {
            getMethod.releaseConnection();
        }
        try {
            statusCode = getMethod.getStatusCode();
            if (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES) {
                LOGGER.info("Invocation returned " + statusCode + " for " + getMethod.getPath());
            } else {
                response = getMethod.getResponseBodyAsString();
                LOGGER.info("Sent get successfully " + getMethod.getPath());
            }

        } catch (IOException e) {
            LOGGER.info("Error while getting the response " + getMethod.getPath());
            return null;
        }
        return response;
    }

    private GetMethod createGetMethod(String path, NameValuePair[] queryParameters) {
        GetMethod method = new GetMethod(this.url + path);
        if (queryParameters != null) {
            method.setQueryString(queryParameters);
        }
        return method;
    }

    public String post(String path) {
        PostMethod postMethod = createPost(path, null, null, null);
        int statusCode = 0;
        String response = null;
        try {
            statusCode = httpClient.executeMethod(postMethod);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while calling the backed for " + postMethod.getPath(), e);
            return null;
        }
        try {
            if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY && (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES)) {
                LOGGER.info("Invocation returned " + statusCode + " for " + postMethod.getPath());
                return null;
            } else {

                response = postMethod.getResponseBodyAsString();
                LOGGER.info("Sent post successfully " + postMethod.getPath());
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while getting the response for " + postMethod.getPath(), e);
            return null;
        } finally {
            postMethod.releaseConnection();
        }
        return response;
    }

    public String post_xml(String path, String xml, Map<String, String> params) {
        PostMethod postMethod = null;
        int statusCode = 0;
        String response = null;
        NameValuePair[] nameValuePairs = getNameValuePairFromMap(params);
        StringRequestEntity stringRequestEntity = null;
        try {
            stringRequestEntity = xml != null ? new StringRequestEntity(xml,
                    "text/xml", "utf-8") : null;
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Error while getting the request parameters for " + postMethod.getPath(), e);
            return null;
        }

        postMethod = createPost(path, nameValuePairs, stringRequestEntity, null);

        try {
            statusCode = httpClient.executeMethod(postMethod);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while calling the backed for " + postMethod.getPath(), e);
            return null;
        }
        try {
            if (statusCode != HttpStatus.SC_MOVED_TEMPORARILY && (statusCode < HttpStatus.SC_OK || statusCode >= HttpStatus.SC_MULTIPLE_CHOICES)) {
                LOGGER.info("Invocation returned " + statusCode + " for " + postMethod.getPath());
                return null;
            } else {

                response = postMethod.getResponseBodyAsString();
                LOGGER.info("Sent post successfully " + postMethod.getPath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error while getting the response for " + postMethod.getPath(), e);
            return null;
        } finally {
            postMethod.releaseConnection();
        }
        return response;
    }

    private NameValuePair[] getNameValuePairFromMap(Map<String, String> params) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String name : params.keySet()) {
            nameValuePairs.add(new NameValuePair(name, params.get(name)));

        }
        return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
    }

    private PostMethod createPost(String urlFragment, NameValuePair[] queryParameters,
                                  RequestEntity requestEntity, NameValuePair[] postParameters) {


        PostMethod post = new PostMethod(this.url + urlFragment);

        post.setDoAuthentication(true);


        if (queryParameters != null) {
            post.setQueryString(queryParameters);
        }

        if (requestEntity != null) {
            post.setRequestEntity(requestEntity);
        }

        if (postParameters != null) {
            post.addParameters(postParameters);
        }

        return post;
    }
}
