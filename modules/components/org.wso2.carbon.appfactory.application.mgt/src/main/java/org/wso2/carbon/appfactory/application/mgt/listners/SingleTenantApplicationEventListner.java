package org.wso2.carbon.appfactory.application.mgt.listners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.util.Util;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.beans.RuntimeBean;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.core.ApplicationEventsHandler;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.JDBCAppVersionDAO;
import org.wso2.carbon.appfactory.core.dto.UserInfo;
import org.wso2.carbon.appfactory.core.dto.Version;
import org.wso2.carbon.appfactory.core.runtime.RuntimeManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryProvider;
import org.wso2.carbon.appfactory.s4.integration.StratosRestService;
import org.wso2.carbon.appfactory.s4.integration.utils.CloudUtils;
import org.wso2.carbon.user.api.UserStoreException;


public class SingleTenantApplicationEventListner extends ApplicationEventsHandler {
    private static Log log = LogFactory.getLog(SingleTenantApplicationEventListner.class);

    public SingleTenantApplicationEventListner(String identifier, int priority) {
        super(identifier, priority);
    }

    @Override
    public void onCreation(org.wso2.carbon.appfactory.core.dto.Application application, String userName,
                           String tenantDomain, boolean isUploadableAppType) throws AppFactoryException {

    }

    /**
     * Undeploy and delete the stratos applications created for this appfactory application
     */
    public void onDeletion(org.wso2.carbon.appfactory.core.dto.Application application, String userName,
                           String tenantDomain) throws AppFactoryException {
        AppFactoryConfiguration appfactoryConfiguration = AppFactoryUtil.getAppfactoryConfiguration();

        String stratosServerURL = appfactoryConfiguration.getFirstProperty(
                AppFactoryConstants.DEPLOYMENT_ENVIRONMENT + AppFactoryConstants.DOT_SEPERATOR +
                AppFactoryConstants.ApplicationStage.DEVELOPMENT+ AppFactoryConstants.DOT_SEPERATOR +
                AppFactoryConstants.TENANT_MGT_URL);

        String  tenantUsername = application.getOwner();
        StratosRestService restService = StratosRestService.getInstance(stratosServerURL, tenantUsername);

        int tenantId = -1;
        try {
            tenantId = Util.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new AppFactoryException("Error while getting tenantId for domain " + tenantDomain  , e);
        }

        ApplicationTypeBean applicationTypeBean = ApplicationTypeManager.getInstance()
                .getApplicationTypeBean(application.getType());
        if (applicationTypeBean == null) {
            throw new AppFactoryException(
                    "Application Type details cannot be found for Artifact Type : " + application.getType()
                    + ", application id" + application.getId() + " for tenant domain: " + tenantDomain);
        }

        String runtimeNameForAppType = applicationTypeBean.getRuntimes()[0];
        RuntimeBean runtimeBean = RuntimeManager.getInstance().getRuntimeBean(runtimeNameForAppType);

        RepositoryProvider repositoryProvider = org.wso2.carbon.appfactory.repository.mgt.internal.Util.
                                      getRepositoryProvider(application.getRepositoryType());
        String paasRepositoryUrlPattern = runtimeBean.getPaasRepositoryURLPattern();

        JDBCAppVersionDAO appVersionDAO = JDBCAppVersionDAO.getInstance();
        String[] versions = appVersionDAO.getAllVersionNamesOfApplication(application.getId());

        String stratosApplicationId;
        //Iterating through app versions and deleting stratos resources
        for (String version : versions) {
            String stage = appVersionDAO.getAppVersionStage(application.getId(),version);
            stratosApplicationId = CloudUtils.generateUniqueStratosApplicationId(tenantId, application.getId(), version,
                                                                                 stage);
            restService.undeployApplication(stratosApplicationId);
            //TODO :
            // Have to wait till application get undeployed
            // Stratos is going to make undeploy a synchronous call or will provide a certial wait time
            // Before deletion
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Sleep thread got interrupted before deleting stratos application");
            }
            restService.deleteApplication(stratosApplicationId);

            try {
                repositoryProvider.deleteStratosArtifactRepository(CloudUtils.generateSingleTenantArtifactRepositoryName(
                        paasRepositoryUrlPattern, stage, version, stratosApplicationId,tenantId));
            } catch (RepositoryMgtException e) {
               log.error("Error while deleting stratos repository for application " + application.getId()
                         + " version :" + version);
                //No need to throw since this is an artifact repo
            }
        }
    }

    @Override
    public void onUserAddition(org.wso2.carbon.appfactory.core.dto.Application application, UserInfo user,
                               String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onUserDeletion(org.wso2.carbon.appfactory.core.dto.Application application, UserInfo user,
                               String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onUserUpdate(org.wso2.carbon.appfactory.core.dto.Application application, UserInfo user,
                             String tenantDomain) throws AppFactoryException {

    }

    @Override
    public void onRevoke(org.wso2.carbon.appfactory.core.dto.Application application, String tenantDomain)
            throws AppFactoryException {

    }

    @Override
    public void onVersionCreation(org.wso2.carbon.appfactory.core.dto.Application application, Version source,
                                  Version target, String tenantDomain,
                                  String userName) throws AppFactoryException {
    }

    @Override
    public void onFork(org.wso2.carbon.appfactory.core.dto.Application application, String userName,
                       String tenantDomain, String version,
                       String[] forkedUsers) throws AppFactoryException {

    }

    @Override
    public void onLifeCycleStageChange(org.wso2.carbon.appfactory.core.dto.Application application, Version version,
                                       String previosStage, String nextStage,
                                       String tenantDomain) throws AppFactoryException {

    }

    @Override
    public boolean hasExecuted(org.wso2.carbon.appfactory.core.dto.Application application, String userName,
                               String tenantDomain)
            throws AppFactoryException {
        return false;
    }

}
