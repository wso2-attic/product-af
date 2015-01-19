/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.utilities.sts;

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.Token;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.client.STSClient;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.SP11Constants;
import org.opensaml.saml2.core.Assertion;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.utils.CarbonUtils;

/*
 * This class will use sts service and generate a token
 */
public class STSUtil {

	private static Log log = LogFactory.getLog(STSUtil.class);

	/*
	 * Get STS token and encode and return
	 */
	public static String getEncodedTokenFromSTS(String id, String username, String password) throws AppFactoryException{		

		String encodedToken = null;
		try{
			String policyPath = getPolicyPath();
			
			//Get tenant domain and tenant id from the carbon context
			String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
                String warnMsg = "Can not get a token because provided tenant domain:" + tenantDomain + " is invalid with tenantId:"+tenantId;
                if (log.isDebugEnabled()) {
                    log.debug(warnMsg);
                }
                throw new AppFactoryException(warnMsg);
            }

			//Generate sts endpoint of the related tenant
			String stsEPR = getSTSUrlForTenant(tenantDomain, tenantId);

			//Get issuer and assertion consumer url from appfactory.xml
			String issuer = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AppFactoryConstants.SSO_NAME).toString();
			String assertionConsumerUrl = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
					(AppFactoryConstants.SSO_ASSERTION_CONSUMER_URL).toString();

			//get token from sts service
			Assertion assertion = getSTSToken(id, assertionConsumerUrl, username, password, policyPath, stsEPR, issuer);

			//encode the token before return
			encodedToken = SAMLSSOUtil.encode(SAMLSSOUtil.marshall(assertion));
		}catch(Exception e){
			handleException("Error while creating a response over sts token", e);
		}
		return encodedToken;

	}
	
	/*
	 * Get sts service url for the tenant
	 */
	public static String getSTSUrlForTenant(String tenantDomain, int tenantID) throws AppFactoryException{
		String stsEPR = null;
		try{		
			if (tenantID != MultitenantConstants.SUPER_TENANT_ID) {
				stsEPR = new StringBuilder().append(AppFactoryUtil.getAppfactoryConfiguration().
						getFirstProperty(AppFactoryConstants.STS_EPR_SERVICES_LOCATION))
						.append((AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
								(AppFactoryConstants.STS_EPR_TENANT_TEMPLATE)
								.replace(AppFactoryConstants.STS_EPR_TENANT_TEMPLA_TENANT_DOMAIN_VALUE, tenantDomain)))
								.append(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
										(AppFactoryConstants.STS_EPR_SERVICE_NAME)).toString();
			} else {
				stsEPR = new StringBuilder().append(AppFactoryUtil.getAppfactoryConfiguration().
						getFirstProperty(AppFactoryConstants.STS_EPR_SERVICES_LOCATION))
						.append(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
								(AppFactoryConstants.STS_EPR_SERVICE_NAME)).toString();
			}
		} catch(Exception e){
			handleException("Building STS epr failed ", e);
		}
		return stsEPR;
	}

	/*
	 * Generate sts token
	 */
	public static Assertion getSTSToken(String id, String assertionConsumerUrl, String username, String password, String policyPath, 
			String stsEPR, String relyingPartyEPR) throws AppFactoryException{
		Assertion assertion = null;
		try{//
			STSClient client = createSTSClient(username);
			Token responseToken;
			Policy stsPolicy = loadPolicy(policyPath);
			Options options = new Options();
			options.setUserName(username);
			options.setPassword(password);
			client.setOptions(options);
			stsPolicy.addAssertion(new RampartConfig());
			//service policy is null since we dont use any trust10 policies
			responseToken = client.requestSecurityToken(null, stsEPR, stsPolicy, relyingPartyEPR);
			assertion = (Assertion) SAMLSSOUtil.unmarshall(responseToken.getToken().toString());	
		} catch (Exception e){
			handleException("Error in requesting security token ", e);
		}
		return assertion;

	}

	/*
	 * Create an STS client
	 */
	public static STSClient createSTSClient(String username) throws AppFactoryException{

		STSClient stsClient = null;
		try{
			ConfigurationContext configCtx = ServiceReferenceHolder.getInstance().getConfigContextService().getClientConfigContext();
			stsClient = new STSClient(configCtx);
			stsClient.setRstTemplate(getRSTTemplate());
			stsClient.setAction(TrustUtil.getActionValue(RahasConstants.VERSION_05_02,
					RahasConstants.RST_ACTION_ISSUE));
		} catch (Exception e){
			handleException("Failed creating a sts client ", e);
		}
		return stsClient;

	}

	/*
	 * Generate the path to policy file
	 */
	public static String getPolicyPath() throws AppFactoryException{

		return new StringBuilder().append(CarbonUtils.getCarbonConfigDirPath())
				.append(File.separator)
				.append(AppFactoryConstants.CONFIG_FOLDER)
				.append(File.separator)
				.append(AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty
						(AppFactoryConstants.STS_POLICY_FILE)).toString();

	}

	/*
	 * Load the policy using policy file
	 */
	public static Policy loadPolicy(String policyPath) throws AppFactoryException {

		Policy policy = null;
		try {
			StAXOMBuilder omBuilder = new StAXOMBuilder(policyPath);
			policy = PolicyEngine.getPolicy(omBuilder.getDocumentElement());
		} catch (Exception e) {
			handleException("Failed loading policy ", e);
		}
		return policy;

	}

	/*
	 * Create a rst template
	 */
	private static OMElement getRSTTemplate() throws AppFactoryException {

		OMFactory omFac = OMAbstractFactory.getOMFactory();
		OMElement element = omFac.createOMElement(SP11Constants.REQUEST_SECURITY_TOKEN_TEMPLATE);

		String tokenType = AppFactoryConstants.SAML_TOKEN_TYPE;

		try{
			if (AppFactoryConstants.SAML_TOKEN_TYPE_20.equals(tokenType)) {
				TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, element).setText(
						RahasConstants.TOK_TYPE_SAML_20);
			} else if (AppFactoryConstants.SAML_TOKEN_TYPE_11.equals(tokenType)) {
				TrustUtil.createTokenTypeElement(RahasConstants.VERSION_05_02, element).setText(
						RahasConstants.TOK_TYPE_SAML_10);
			}
		} catch (Exception e){
			handleException("Failed create token element ", e);
		}

		String subjectConfirmationMethod = AppFactoryUtil.getAppfactoryConfiguration().
				getFirstProperty(AppFactoryConstants.SUBJECT_CONFIRMATION_METHOD);
		String claimDialect = 
				AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AppFactoryConstants.CLAIM_DIALECT);
		String[] claimUris = 
				AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AppFactoryConstants.CLAIM_URIS).split(",");

		try{
			if (AppFactoryConstants.SUBJECT_CONFIRMATION_BEARER.equals(subjectConfirmationMethod)) {
				TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, element,
						RahasConstants.KEY_TYPE_BEARER);
			} else if (AppFactoryConstants.SUBJECT_CONFIRMATION_HOLDER_OF_KEY
					.equals(subjectConfirmationMethod)) {
				TrustUtil.createKeyTypeElement(RahasConstants.VERSION_05_02, element,
						RahasConstants.KEY_TYPE_SYMM_KEY);
			}
		} catch (Exception e){
			handleException("Failed create key element ", e);
		}

		try{
			OMElement claimElement = TrustUtil.createClaims(RahasConstants.VERSION_05_02, element,claimDialect);
			addClaimType(claimElement, claimUris);
		} catch (Exception e){
			handleException("Failed addig claims ", e);
		}
		return element;

	}

	/*
	 * Add claim values to rst template
	 */
	private static void addClaimType(OMElement parent, String[] claimUris) throws AppFactoryException {

		OMElement element = null;
		for (String attr : claimUris) {
			element = parent.getOMFactory()
					.createOMElement(
							new QName(AppFactoryUtil.getAppfactoryConfiguration().
									getFirstProperty(AppFactoryConstants.CLIAM_NAMESPACE),
									AppFactoryConstants.CLIAM_TYPE_NAME, AppFactoryConstants.CLIAM_TYPE_VALUE), parent);
			element.addAttribute(parent.getOMFactory().createOMAttribute("Uri", null, attr));
		}

	}

	private static void handleException(String msg, Exception e) throws AppFactoryException{
		log.error(msg, e);
		throw new AppFactoryException(msg,e);
	}
}
