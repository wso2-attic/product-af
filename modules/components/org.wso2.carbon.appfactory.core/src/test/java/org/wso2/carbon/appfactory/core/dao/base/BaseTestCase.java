/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.dao.base;

import junit.framework.TestCase;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConfigurationBuilder;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeBean;
import org.wso2.carbon.appfactory.core.apptype.ApplicationTypeManager;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dao.JDBCResourceDAO;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.AppFactoryDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBCApplicationDAO Tester.
 */
public class BaseTestCase extends TestCase {

    public static final String CARBON_HOME = "carbon.home";
    public static final String AFDB_CONFIGURATION_PATH = "AFDBConfigurationPath";
    public static JDBCApplicationDAO applicationDAO = null;
    public static JDBCResourceDAO resourceDAO = null;


    public BaseTestCase(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty(AFDB_CONFIGURATION_PATH);
        if (System.getProperty(CARBON_HOME) == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty(CARBON_HOME, file.getAbsolutePath());
            }
        }

        AppFactoryConfiguration appFactoryConfiguration = new AppFactoryConfigurationBuilder(dbConfigPath)
                .buildAppFactoryConfiguration();
        ServiceHolder.setAppFactoryConfiguration(appFactoryConfiguration);
        AppFactoryDBUtil.initializeDatasource();
        CarbonContext.getCurrentContext();

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                                                                                      .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        Map<String, ApplicationTypeBean> applicationTypeBeanMap = new HashMap<String, ApplicationTypeBean>();
        ApplicationTypeBean applicationTypeBean = new ApplicationTypeBean();
        applicationTypeBean.setIsUploadableAppType(false);
        applicationTypeBeanMap.put("Uploaded-App-Jax-WS", applicationTypeBean);
        ApplicationTypeManager.getInstance().setApplicationTypeBeanMap(applicationTypeBeanMap);

        applicationDAO = JDBCApplicationDAO.getInstance();
        resourceDAO = JDBCResourceDAO.getInstance();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        PrivilegedCarbonContext.destroyCurrentContext();
    }
}
