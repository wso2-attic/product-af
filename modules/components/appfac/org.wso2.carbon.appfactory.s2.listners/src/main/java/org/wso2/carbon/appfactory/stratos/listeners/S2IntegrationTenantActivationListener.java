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

package org.wso2.carbon.appfactory.stratos.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
@Deprecated
public class S2IntegrationTenantActivationListener implements TenantMgtListener{
    private static final Log log = LogFactory.getLog(S2IntegrationTenantActivationListener.class);

    private int priority = 5;
    private static SubscriptionManagerClient subscriptionManagerClient;

    public S2IntegrationTenantActivationListener(int priority) {
        this.priority = priority;
        //identifier = "s2";
    }

//    @Override
//    public void onCreation(Application application,String userName, String tenantDomain) throws AppFactoryException {
//        if(subscriptionManagerClient == null){
//            subscriptionManagerClient = new SubscriptionManagerClient();
//        }
//
//        RealmService realmService = Util.getRealmService();
//        TenantManager tenantManager = realmService.getTenantManager();
//
//        try {
//            int tenantID = tenantManager.getTenantId(tenantDomain);
//
//            if(log.isDebugEnabled()){
//                log.debug("Tenant domain : " + tenantDomain);
//            }
//
//            subscriptionManagerClient.subscribe(application.getId(), application.getType(),tenantDomain,tenantID);
//        } catch (AppFactoryException e) {
//            String msg = "Unable to subscribe to the S2 production instance";
//            log.error(msg,e);
//            throw new AppFactoryException(msg,e);
//        } catch (UserStoreException e) {
//            String msg = "Unable to get tenant id for domain : " + tenantDomain;
//            log.error(msg,e);
//            throw new AppFactoryException(msg,e);
//        }
//
//    }
//
//    @Override
//    public void onUserAddition(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onUserDeletion(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onUserUpdate(Application application, UserInfo user, String tenantDomain) throws AppFactoryException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onRevoke(Application application, String tenantDomain) throws AppFactoryException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onVersionCreation(Application application, Version source, Version target, String tenantDomain) throws AppFactoryException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void onLifeCycleStageChange(Application application, Version version, String previosStage, String nextStage, String tenantDomain) throws AppFactoryException {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public int getPriority() {
//        return priority;
//    }

    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
         if(subscriptionManagerClient == null){
            subscriptionManagerClient = new SubscriptionManagerClient();
        }
        try {
            subscriptionManagerClient.subscribe(tenantInfoBean.getTenantDomain(),tenantInfoBean.getTenantId());
        } catch (AppFactoryException e) {
        	String msg = "subscription failed";
            log.error(msg,e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onTenantRename(int i, String s, String s1) throws StratosException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onTenantInitialActivation(int i) throws StratosException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onTenantActivation(int i) throws StratosException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onTenantDeactivation(int i) throws StratosException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getListenerOrder() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
