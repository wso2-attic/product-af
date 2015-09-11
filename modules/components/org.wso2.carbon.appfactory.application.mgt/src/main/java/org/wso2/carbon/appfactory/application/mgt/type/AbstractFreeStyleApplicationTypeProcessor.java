package org.wso2.carbon.appfactory.application.mgt.type;

import org.apache.axiom.om.OMElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.utilities.version.AppVersionStrategyExecutor;

import java.io.File;
import java.util.Map;

/**
 * Created by harsha on 2/15/15.
 */
public abstract class AbstractFreeStyleApplicationTypeProcessor extends AbstractApplicationTypeProcessor{
    private static final Log log = LogFactory.getLog(AbstractFreeStyleApplicationTypeProcessor.class);

    public AbstractFreeStyleApplicationTypeProcessor(String type) {
        super(type);
    }


    @Override
    public void doVersion(String applicationId, String targetVersion, String currentVersion, String workingDirectory)
            throws AppFactoryException {
        AppVersionStrategyExecutor.doVersionForGenericApplicationType(targetVersion, new File(workingDirectory));
    }

    @Override
    public OMElement configureBuildJob(OMElement jobConfigTemplate, Map<String, String> parameters,
                                       String projectType)
            throws AppFactoryException {
        if (jobConfigTemplate == null) {
            String msg =
                    "Class loader is unable to find the jenkins job configuration template  for "+ projectType;
            log.error(msg);
            throw new AppFactoryException(msg);
        }

        String artifactArchiver = null;
        Object hudsonArtifactArchiver = ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType)
                .getProperty(
                        AppFactoryConstants.HUDSON_ARTIFACT_ARCHIVER);
        if (hudsonArtifactArchiver != null) {
            artifactArchiver = hudsonArtifactArchiver.toString();
        }
        jobConfigTemplate = configureRepositoryData(jobConfigTemplate, parameters);

        // Support for post build listener residing in jenkins server
        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_EXTENSION_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPTYPE_EXTENSION));

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_ID_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPLICATION_ID));

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.PUBLISHERS_APPFACTORY_POST_BUILD_APP_VERSION_XPATH_SELECTOR,
                parameters.get(AppFactoryConstants.APPLICATION_VERSION));

        if (StringUtils.isNotBlank(artifactArchiver)) {
            setValueUsingXpath(jobConfigTemplate,
                    AppFactoryConstants.ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR,
                    artifactArchiver);
        }

        if (ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType).isUploadableAppType()) {
            setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.ARTIFACT_ARCHIVER_CONFIG_NAME_XAPTH_SELECTOR,
                    AppFactoryConstants.DEFAULT_ARTIFACT_NAME +
                            ApplicationTypeManager.getInstance().getApplicationTypeBean(projectType)
                                    .getExtension());

            String repositoryBranchName = parameters.get(AppFactoryConstants.APPLICATION_VERSION);
            if (AppFactoryConstants.INITIAL_UPLOADED_APP_VERSION.equals(repositoryBranchName)) {
                setValueUsingXpath(jobConfigTemplate, AppFactoryConstants.GIT_REPOSITORY_VERSION_XPATH_SELECTOR,
                        AppFactoryConstants.APPLICATION_VERSION_VALUE_FREESTYLE);
            }
        }

        setValueUsingXpath(jobConfigTemplate,
                AppFactoryConstants.APPLICATION_TRIGGER_PERIOD,
                parameters.get(AppFactoryConstants.APPLICATION_POLLING_PERIOD));

        return jobConfigTemplate;

    }
}
