/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.application.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.eta.CalculatedETA;
import org.wso2.carbon.appfactory.application.mgt.eta.ETAValue;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class defines operations related to ETA (Estimated Time of Arrival)
 *
 */
public class ETAManagementService {

	private static Log log = LogFactory.getLog(ETAManagementService.class);

	/**
	 * Persist the given {@code etaFrom} and {@code etaTo} values with reference to the given {@code applicationKey, stage, version, user}.
	 * If there is an ETA record is already available, then update it with given information.
	 * Otherwise creates new ETA record.
	 * 
	 * @param applicationKey
	 * @param stage
	 * @param version
	 * @param user
	 * @param etaFrom
	 * @param etaTo
	 * @throws ApplicationManagementException
	 */
	public void publishSetETA(String applicationKey, String stage, String version, String user,
	                          String etaFrom, String etaTo) throws ApplicationManagementException {
		try {
            if(log.isDebugEnabled()){
               log.debug("PublishSetETA is invoked");
            }
			RxtManager rxtManager = new RxtManager();
			GenericArtifact artifact =
			                           rxtManager.getArtifact(getETAResourcePath(applicationKey,
			                                                                     stage, version,
			                                                                     user), "eta");
			Map<String, String> newValueMap =
			                                  createEtaValueMap(applicationKey, stage, version,
			                                                    user, etaFrom, etaTo);
			if (artifact == null) {
				log.info("No existing eta information is found. Creating new information....");

				rxtManager.addNewArtifact("eta", applicationKey, newValueMap);
			} else {
				log.info("Existing eta information is found. Updating Current information...");
				rxtManager.updateExistingArtifact(artifact, "eta", applicationKey, newValueMap);
			}
			log.debug("publishSetETA invocation is sucessful");
		} catch (AppFactoryException ex) {
			String errorMsg =
			                  " ErropublishGetETAValuePerUserr while persisting new ETA value " +
			                          ex.getMessage();
			log.error(errorMsg, ex);
			throw new ApplicationManagementException(errorMsg, ex);
		}
	}

	/**
	 * Retrieves ETA values related to given {@code applicationKey, stage, version, user}
	 * @param applicationKey
	 * @param stage
	 * @param version
	 * @param user
	 * @return {@link ETAValue}
	 * @throws ApplicationManagementException
	 */
	public ETAValue publishGetETAValuePerUser(String applicationKey, String stage, String version,
	                                          String user) throws ApplicationManagementException {
		try {
            if(log.isDebugEnabled()){
              log.debug("publishGetETAValuePerUser is invoked.");
            }

			RxtManager rxtManager = new RxtManager();
			GenericArtifact artifact =
			                           rxtManager.getArtifact(getETAResourcePath(applicationKey,
			                                                                     stage, version,
			                                                                     user), "eta");

			if (artifact == null) {
				log.info("No ETA information is available.");
				return new ETAValue(applicationKey, stage, version, user, "", "");
			}
			Map<String, String> artifactValues = rxtManager.readArtifact(artifact);
			return getETAValue(artifactValues);

		} catch (AppFactoryException ex) {
			String errorMsg = " Error while retrieving persisted ETA values " + ex.getMessage();
			log.error(errorMsg, ex);
			throw new ApplicationManagementException(errorMsg, ex);
		} catch (GovernanceException ex) {
			String errorMsg = " Error while reading persisted ETA values " + ex.getMessage();
			log.error(errorMsg, ex);
			throw new ApplicationManagementException(errorMsg, ex);
		}
	}

	/**
	 * Returns the registry resource path for ETA information. 
	 * @param applicationKey
	 * @param stage
	 * @param version
	 * @param user
	 * @return resource registy path String
	 */
	private String getETAResourcePath(String applicationKey, String stage, String version,
	                                  String user) {
		return AppFactoryConstants.REGISTRY_APPLICATION_PATH + RegistryConstants.PATH_SEPARATOR +
		       applicationKey + RegistryConstants.PATH_SEPARATOR + "eta" + RegistryConstants.PATH_SEPARATOR + stage +
		       RegistryConstants.PATH_SEPARATOR + version + RegistryConstants.PATH_SEPARATOR +
		       modifyUser(user) + RegistryConstants.PATH_SEPARATOR + "eta";
	}

	/**
	 * Creates {@link Map} of the ETA values from given values.
	 * @param applicationKey
	 * @param stage
	 * @param version
	 * @param user
	 * @param etaFrom
	 * @param etaTo
	 * @return {@link Map}
	 */
	private Map<String, String> createEtaValueMap(String applicationKey, String stage,
	                                              String version, String user, String etaFrom,
	                                              String etaTo) {

		Map<String, String> valueMap = new HashMap<String, String>();

		valueMap.put("eta_key", applicationKey);
		valueMap.put("eta_stage", stage);
		valueMap.put("eta_version", version);
		valueMap.put("eta_user", modifyUser(user));
		valueMap.put("eta_estimatedFrom", etaFrom);
		valueMap.put("eta_estimatedTo", etaTo);
		return valueMap;
	}

	/**
	 * Replaces invalid Registry characters of username with registry savable value.
	 * @param user
	 * @return savable string of username
	 */
	private String modifyUser(String user) {
		return user.replace("@", ":");
	}

	/**
	 * Creates {@link ETAValue} instance from given values {@link Map}
	 * @param artifactValues
	 * @return {@link ETAValue} instance.
	 * @throws GovernanceException
	 */
	private ETAValue getETAValue(Map<String, String> artifactValues) throws GovernanceException {

		String applicationKey = artifactValues.get("eta_key");
		String stage = artifactValues.get("eta_stage");
		String version = artifactValues.get("eta_version");
		String user = artifactValues.get("eta_user");
		String etaFrom = artifactValues.get("eta_estimatedFrom");
		String etaTo = artifactValues.get("eta_estimatedTo");

		return new ETAValue(applicationKey, stage, version, user, etaFrom, etaTo);
	}

	/**
	 * Calculates ETA information related to {@code applicationKey, stage, version} based on all the ETA values saved.
	 * @param applicationKey
	 * @param stage
	 * @param version
	 * @return {@link CalculatedETA} instance
	 * @throws ApplicationManagementException
	 * @throws ParseException
	 */
	public CalculatedETA getCalculatedETA(String applicationKey, String stage, String version)
	                                                                                          throws ApplicationManagementException,
	                                                                                          ParseException {
		RxtManager rxtManager = new RxtManager();
		try {

			List<GenericArtifact> etaArtifacts =
			                                     rxtManager.getETAArtifacts(applicationKey, stage,
			                                                                version);
			log.debug("Saved ETA values are retried for applicationKey " + applicationKey + ", stage " + stage + " version " + version);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
			Date startDate = null;
			Date endDate = null;

			for (GenericArtifact etaArtifact : etaArtifacts) {
				ETAValue etaValue = getETAValue(rxtManager.readArtifact(etaArtifact));
				Date tmpStartDate = dateFormat.parse(etaValue.getEtaFrom());

				if (startDate == null || (tmpStartDate.compareTo(startDate) < 0)) {
					startDate = tmpStartDate;
				}

				Date tmpEndDate = dateFormat.parse(etaValue.getEtaTo());
				if (endDate == null || (tmpEndDate.compareTo(endDate) > 0)) {
					endDate = tmpEndDate;
				}
			}

			String startDateStr = startDate == null ? "" : dateFormat.format(startDate);
			String endtDateStr = endDate == null ? "" : dateFormat.format(endDate);

			return new CalculatedETA(applicationKey, stage, version, startDateStr, endtDateStr);
		} catch (AppFactoryException ex) {
			String errorMsg =
			                  " Error Calculating ETA values for " + applicationKey + " version " +
			                          version;
			log.error(errorMsg, ex);
			throw new ApplicationManagementException(errorMsg, ex);
		} catch (GovernanceException ex) {
			String errorMsg = " Error while reading ETA artifact values " + ex.getMessage();
			log.error(errorMsg, ex);
			throw new ApplicationManagementException(errorMsg, ex);
		}
	}

}
