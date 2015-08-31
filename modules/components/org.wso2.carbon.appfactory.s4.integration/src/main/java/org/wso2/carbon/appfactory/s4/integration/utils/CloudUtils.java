/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.s4.integration.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.s4.integration.internal.ServiceReferenceHolder;

public class CloudUtils {
	private static final Log log = LogFactory.getLog(CloudUtils.class);

	public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	public static final String AWS_HOST = "route53.amazonaws.com";

	/**
	 * Get access key id from configs
	 * 
	 * @return
	 */
	public static String getAccessKeyId() {
		return ServiceReferenceHolder.getInstance().getAppFactoryConfiguration()
		                             .getFirstProperty("AWSRoute53AccessKeyId");
	}

	/**
	 * Get secret access key from configs
	 * 
	 * @return
	 */
	public static String getSecretAccessKey() {
		return ServiceHolder.getAppFactoryConfiguration()
		                    .getFirstProperty("AWSRoute53SecretAccessKey");
	}

	/**
	 * Get hosted zone id from configs
	 * 
	 * @return
	 */
	public static String getHostedZoneId() {
		return ServiceReferenceHolder.getInstance().getAppFactoryConfiguration()
		                             .getFirstProperty("AWSRoute53HostedZoneId");
	}

	/**
	 * 
	 * @param stage
	 * @return
	 */
	public static String getLBUrl() {
		return ServiceReferenceHolder.getInstance().getAppFactoryConfiguration()
		                             .getFirstProperty("StratosLBUrl");
	}

	/**
	 * 
	 * @param stage
	 * @return
	 */
	public static boolean isAWSRoute53Enabled() {
		return Boolean.parseBoolean(ServiceReferenceHolder.getInstance()
				                            .getAppFactoryConfiguration()
				                            .getFirstProperty("EnableAWSRoute53")
				                            .trim());
	}

	/**
	 * 
	 * @param request
	 * @throws AppFactoryException
	 */
	public static void sendRequest(String request) throws AppFactoryException {
		// create a post request to addAPI.
		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		String endPoint =
		                  "https://" + AWS_HOST + "/2013-04-01/hostedzone/" + getHostedZoneId() +
		                          "/rrset";
		PostMethod postMethod = new PostMethod(endPoint);

		try {
			String stringToSign = getGMTTime();
			postMethod.setRequestHeader("Content-Type", "text/xml");
			postMethod.setRequestHeader("Host", AWS_HOST);
			postMethod.setRequestHeader("x-amz-date", stringToSign);
			String authHeaderval =
			                       "AWS3-HTTPS AWSAccessKeyId=" + getAccessKeyId() + ",Algorithm=" +
			                               HMAC_SHA1_ALGORITHM + ",Signature=" +
			                               calculateRFC2104HMAC(stringToSign, getSecretAccessKey());
			postMethod.setRequestHeader("X-Amzn-Authorization", authHeaderval);

			postMethod.setRequestEntity(new StringRequestEntity(request, "application/json",
			                                                    "UTF-8"));
			int responseCode = 0;
			String responseString = null;

			responseCode = httpClient.executeMethod(postMethod);

			responseString = postMethod.getResponseBodyAsString();

			if (log.isDebugEnabled()) {
				log.debug(" AWSRoute53DomainNameService response id: " + responseCode +
				          " message:O " + responseString);
			}

		} catch (UnsupportedEncodingException e) {
			String msg =
			             "Error occured while invoking AWS Route API 53 to add/delete CNAME reocrds";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (HttpException e) {
			String msg =
			             "Error occured while invoking AWS Route API 53 to add/delete CNAME reocrds";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} catch (IOException e) {
			String msg =
			             "Error occured while invoking AWS Route API 53 to add/delete CNAME reocrds";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		} finally {
			postMethod.releaseConnection();
		}

	}

	/**
	 * Prepare the request
	 * 
	 * @param action
	 * @param name
	 * @param domains
	 * @return
	 */
	public static String prepareCNAMERecordsReq(String action, String name, String domain) {
		StringBuffer request =
		                       new StringBuffer(
		                                        "<ChangeResourceRecordSetsRequest xmlns=\"https://route53.amazonaws.com/doc/2013-04-01/\">"
		                                                + "<ChangeBatch>" + "<Changes>");
		request.append("<Change><Action>" + action + "</Action>" + "<ResourceRecordSet>" +
		               "<Name>" + domain + "</Name>" + "<Type>CNAME</Type>" + "<TTL>100000</TTL>" +
		               "<ResourceRecords>" + "<ResourceRecord>" + "<Value>" + name + "</Value>" +
		               "</ResourceRecord>" + "</ResourceRecords>" + "</ResourceRecordSet>" +
		               "</Change>");

		request.append("</Changes>" + "</ChangeBatch>" + "</ChangeResourceRecordSetsRequest>");
		return request.toString();
	}

	/**
	 * Computes RFC 2104-compliant HMAC signature.
	 * * @param data
	 * The data to be signed.
	 * 
	 * @param key
	 *            The signing key.
	 * @return
	 *         The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws AppFactoryException
	 * @throws java.security.SignatureException
	 *             when signature generation fails
	 */
	public static String calculateRFC2104HMAC(String stringToSign, String secrectAccessKey)
	                                                                                       throws AppFactoryException {

		// get an hmac_sha1 key from the raw key bytes
		SecretKeySpec signingKey =
		                           new SecretKeySpec(secrectAccessKey.getBytes(),
		                                             HMAC_SHA1_ALGORITHM);

		// get an hmac_sha1 Mac instance and initialize with the signing key
		Mac mac = null;
		try {
			mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
		} catch (NoSuchAlgorithmException e) {
			String msg = "Error occured while computing RFC 2104-compliant HMAC signature";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);

		} catch (InvalidKeyException e) {
			String msg = "Error occured while computing RFC 2104-compliant HMAC signature";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);

		}

		// compute the hmac on input data bytes
		byte[] rawHmac = mac.doFinal(stringToSign.getBytes());

		// base64-encode the hmac
		return new String(Base64.encodeBase64(rawHmac));

	}

	/**
	 * Get the current date from the Amazon Route 53 server
	 * 
	 * @return
	 * @throws AppFactoryException
	 */
	public static String getGMTTime() throws AppFactoryException {

		HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		GetMethod getMethod = new GetMethod("https://" + AWS_HOST + "/date");
		try {
			httpClient.executeMethod(getMethod);
		} catch (IOException e) {
			String msg =
			             "Error occured while retriving the current date from the Amazon Route 53 server";
			log.error(msg, e);
			throw new AppFactoryException(msg, e);
		}finally{
			getMethod.releaseConnection();
		}
		String date = getMethod.getResponseHeader("Date").getValue();
		return date;

	}

	/**
	 * Generate a UniqueId for single tenant applications using the following format
	 * {tenantId}-{applicationId}-{application-version}-{stage}
	 *
	 * @param tenantId
	 * @param applicationId
	 * @param version
	 * @return
	 */
	public static String generateUniqueStratosApplicationId(int tenantId, String applicationId, String version,
	                                                        String stage) {
		return tenantId + AppFactoryConstants.HYPHEN + applicationId + AppFactoryConstants.HYPHEN
		       + (version + "").replace(AppFactoryConstants.DOT, AppFactoryConstants.HYPHEN) + AppFactoryConstants.HYPHEN
		       + stage.toLowerCase();
	}

	/**
	 * Generate the Stratos artifact repository name
	 *
	 * @param paasRepositoryURLPattern Ex : {@stage}/tomcat
	 * @param stage
	 * @param version
	 * @param applicationId
	 * @param tenantId
	 * @return repository name
	 */
	public static String generateSingleTenantArtifactRepositoryName(String paasRepositoryURLPattern, String stage,
	                                                                String version, String applicationId,
	                                                                int tenantId) {
		//needs to replace dot(.) with minus(-) cause git doesn't allow
		version = version.replaceAll("\\.+", AppFactoryConstants.MINUS);
		String gitRepoName = AppFactoryConstants.URL_SEPERATOR + paasRepositoryURLPattern
		                     + AppFactoryConstants.URL_SEPERATOR + tenantId + AppFactoryConstants.URL_SEPERATOR
		                     + applicationId + AppFactoryConstants.MINUS + version
		                     + AppFactoryConstants.GIT_REPOSITORY_EXTENSION;
		gitRepoName = gitRepoName.replace(AppFactoryConstants.STAGE_PLACE_HOLDER, stage);
		return gitRepoName;
	}
}
