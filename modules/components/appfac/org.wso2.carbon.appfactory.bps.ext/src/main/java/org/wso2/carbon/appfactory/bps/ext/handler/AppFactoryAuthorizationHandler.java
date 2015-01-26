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
package org.wso2.carbon.appfactory.bps.ext.handler;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Axis 2 Handler to set authorization headers needed from appfactory
 */
public class AppFactoryAuthorizationHandler extends AbstractHandler {

	private static Log log = LogFactory.getLog(AppFactoryAuthorizationHandler.class);
	private static final String USERNAME_URL = "http://handler.authorization.appfactory.carbon.wso2.org";

	@Override
	public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
		if (log.isDebugEnabled()) {
			log.debug("Invoking AppFactoryAuthorizationHandler.");
		}
		SOAPHeader header = messageContext.getEnvelope().getHeader();
		if (header != null) {
			@SuppressWarnings("unchecked")
			ArrayList<SOAPHeaderBlock> usernameHeaders =
					header.getHeaderBlocksWithNSURI(USERNAME_URL);
			//If no headers are there we are not doing anything
			//If there are more than one header then aborting
			if (usernameHeaders == null || usernameHeaders.size() == 0) {
				return InvocationResponse.CONTINUE;
			} else if (usernameHeaders.size() != 1) {
				String msg = "There are more than one username headers. Aborting";
				AxisFault axisFault = new AxisFault(msg);
				log.error(msg, axisFault);
				throw axisFault;
			}

			SOAPHeaderBlock block = usernameHeaders.get(0);
			String userName = block.getText();

			String authHeader = null;
			try {
				authHeader = AppFactoryUtil.getAuthHeader(userName);
			} catch (AppFactoryException e) {
				String msg = "Error while generating auth header. Aborting";
				AxisFault axisFault = new AxisFault(msg);
				log.error(msg, axisFault);
				throw axisFault;
			}
			List<Header> headers = new ArrayList<Header>();

			Header authorizationHeader = new Header(HTTPConstants.HEADER_AUTHORIZATION, authHeader);
			headers.add(authorizationHeader);


			messageContext.getOptions().setProperty(HTTPConstants.HTTP_HEADERS, headers);
		}
		return InvocationResponse.CONTINUE;
	}
}
