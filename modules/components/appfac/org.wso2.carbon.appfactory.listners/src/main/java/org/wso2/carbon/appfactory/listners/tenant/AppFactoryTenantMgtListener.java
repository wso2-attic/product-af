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
package org.wso2.carbon.appfactory.listners.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.listners.util.Util;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.apache.stratos.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Calendar;

/**
 * Tenant activation listener for initializing cloud in different environments
 */
public class AppFactoryTenantMgtListener implements TenantMgtListener {
    private String ENVIRONMENT_MANAGER_URL = "";
    private String ENVIRONMENT = "ApplicationDeployment.DeploymentStage";
    private static final Log log = LogFactory.getLog(AppFactoryTenantMgtListener.class);

    @Override
    public void onTenantCreate(org.wso2.carbon.stratos.common.beans.TenantInfoBean tenantInfoBean) throws StratosException {
        AppFactoryConfiguration configuration = Util.getConfiguration();
        TenantInfoBean bean = getTenantInfoBean(tenantInfoBean);
        String[] stages = configuration.getProperties(ENVIRONMENT);
        for (String stage : stages) {
            try {
                callDoPostTenantActivation(stage, configuration, Util.getRealmService().getTenantManager().
                        getTenantId(tenantInfoBean.getTenantDomain()), getTenantInfoBean(tenantInfoBean));
            } catch (UserStoreException e) {
                String msg = "Error while getting tenantID";
                log.error(msg, e);
                throw new StratosException(msg, e);
            }
        }
    }

    private TenantInfoBean getTenantInfoBean(org.wso2.carbon.stratos.common.beans.TenantInfoBean tenantInfoBean) {
        TenantInfoBean bean = new TenantInfoBean();
        bean.setOriginatedService(tenantInfoBean.getOriginatedService());
        bean.setTenantId(tenantInfoBean.getTenantId());
        bean.setActive(tenantInfoBean.isActive());
        bean.setSuccessKey(tenantInfoBean.getSuccessKey());
        bean.setTenantDomain(tenantInfoBean.getTenantDomain());
        bean.setUsagePlan(tenantInfoBean.getUsagePlan());
        bean.setCreatedDate(Calendar.getInstance());
        return bean;
    }

    @Override
    public void onTenantUpdate(org.wso2.carbon.stratos.common.beans.TenantInfoBean tenantInfoBean) throws StratosException {
        //Do nothing
    }

    @Override
    public void onTenantRename(int i, String s, String s2) throws StratosException {
        //Do nothing
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        //Do nothing
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        //Do nothing
    }

    private void callDoPostTenantActivation(String stage, AppFactoryConfiguration configuration, int tenantId,
                                            TenantInfoBean bean) throws StratosException {
        String serverURL = configuration.getFirstProperty(ENVIRONMENT + "." + stage + "." + "TenantMgtUrl");
        try {
            AppFactoryTenantMgtServiceClient client = new AppFactoryTenantMgtServiceClient(serverURL,
                    configuration.getFirstProperty(AppFactoryConstants.SERVER_ADMIN_NAME), configuration.
                    getFirstProperty(AppFactoryConstants.SERVER_ADMIN_PASSWORD));
            TenantInfoBean tenantInfoBean = new TenantInfoBean();
            tenantInfoBean.setTenantId(tenantId);
            tenantInfoBean.setOriginatedService("WSO2 Stratos Manager");
            client.doPostTenantActivation(bean);
        } catch (Exception e) {
            String msg = "Error while getting doing PostTenantActivation";
            log.error(msg, e);
            throw new StratosException(msg, e);
        }
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        //Do nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s2) throws StratosException {
        //Do nothing
    }

    @Override
    public int getListenerOrder() {
        return 100;
    }

}
