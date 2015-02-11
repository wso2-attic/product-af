/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.appfactory.s4.integration.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.s4.integration.DomainMapperEventHandler;
import org.wso2.carbon.appfactory.s4.integration.utils.CloudUtils;

public class AWSRoute53DomainNameService implements DomainMapperEventHandler<AWSRoute53DomainNameService> {
	private static final Log log = LogFactory.getLog(AWSRoute53DomainNameService.class);

	@Override
	public void onDomainMappingCreate(String domain) throws AppFactoryException {
		if (CloudUtils.isAWSRoute53Enabled()) {
			try {
				CloudUtils.sendRequest(CloudUtils.prepareCNAMERecordsReq("CREATE",
				                                                         CloudUtils.getLBUrl(),
				                                                         domain));
			} catch (AppFactoryException e) {
				String msg = "Error occured while creating CNAME records for domains " + domain;
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
		}

	}

	@Override
	public void OnDomainMappingDelete(String domain) throws AppFactoryException {
		if (CloudUtils.isAWSRoute53Enabled()) {
			try {
				CloudUtils.sendRequest(CloudUtils.prepareCNAMERecordsReq("DELETE",
				                                                         CloudUtils.getLBUrl(),
				                                                         domain));
			} catch (AppFactoryException e) {
				String msg = "Error occured deleting CNAME records for domains " + domain;
				log.error(msg, e);
				throw new AppFactoryException(msg, e);
			}
		}
	}

    @Override
    public int compareTo(AWSRoute53DomainNameService awsRoute53DomainNameService) {
        return this.hashCode();
    }


}
