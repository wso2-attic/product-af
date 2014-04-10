/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.core.build;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.Statistic;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;

public class ContinuousIntegrationStatisticsService {

    public Statistic[] getGlobalStatistics(String[][] parameters) throws AppFactoryException{

//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        Statistic[] stats = null;
        if (ServiceHolder.getContinuousIntegrationSystemDriver() != null) {
            Map<String, String> paramMap = convertToMap(parameters);
            stats =
                    ServiceHolder.getContinuousIntegrationSystemDriver()
                                 .getGlobalStatistics(paramMap, tenantDomain);
        } else {
            stats = new Statistic[0];
        }

        return stats;
    }

    public Statistic[] getApplicationStatistics(String applicationId, String[][] parameters) throws AppFactoryException{
        Statistic[] stats = null;

//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();


        if (ServiceHolder.getContinuousIntegrationSystemDriver() != null) {
            Map<String, String> paramMap = convertToMap(parameters);
            stats =
                    ServiceHolder.getContinuousIntegrationSystemDriver()
                                 .getApplicationStatistics(applicationId, paramMap, tenantDomain);
        } else {
            stats = new Statistic[0];
        }

        return stats;
    }
    
    public String getJsonTree(String jobName,String treeStructure)throws AppFactoryException{
        String buildsInfo= "{}";

//        Getting the tenant domain from CarbonContext
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        if(ServiceHolder.getContinuousIntegrationSystemDriver()!=null){
            buildsInfo= ServiceHolder.getContinuousIntegrationSystemDriver().getJsonTree(jobName ,treeStructure,
                    tenantDomain);
        }
        return buildsInfo;
    }

    private Map<String, String> convertToMap(String[][] parameters) {
        Map<String, String> paramMap = new HashMap<String, String>();
        for (String[] strings : parameters) {
            if (strings != null && strings.length == 2) {
                paramMap.put(strings[0], strings[1]);
            }
        }
        return paramMap;
    }
}
