/*
 * Copyright 2014 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.core.sql;

/**
 * Contains constants related to JDBC Operations used by {@link org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO}
 */
public class SQLParameterConstants {
    public static final String COLUMN_NAME_CLUSTER_ID = "CLUSTER_ID";
    public static final String COLUMN_NAME_APPLICATION_NAME = "APPLICATION_NAME";
    public static final String COLUMN_NAME_LB_CLUSTER_ID = "LB_CLUSTER_ID";
    public static final String COLUMN_NAME_ACTIVE_IP = "ACTIVE_IP";
    public static final String COLUMN_NAME_ID = "ID";
    public static final String COLUMN_NAME_VERSION_NAME = "VERSION_NAME";
    public static final String COLUMN_NAME_STAGE = "STAGE";
    public static final String COLUMN_NAME_PROMOTE_STATUS = "PROMOTE_STATUS";
    public static final String COLUMN_NAME_APPLICATION_KEY = "APPLICATION_KEY";
    public static final String COLUMN_NAME_STATUS = "STATUS";
    public static final String COLUMN_NAME_LAST_DEPLOY = "LAST_DEPLOY";
    public static final String COLUMN_NAME_LAST_DEPLOY_STATUS = "LAST_DEPLOY_STATUS";
    public static final String COLUMN_NAME_LAST_DEPLOY_TIME = "LAST_DEPLOY_TIME";
    public static final String COLUMN_NAME_LAST_BUILD = "LAST_BUILD";
    public static final String COLUMN_NAME_LAST_BUILD_STATUS = "LAST_BUILD_STATUS";
    public static final String COLUMN_NAME_LAST_BUILD_TIME = "LAST_BUILD_TIME";
    public static final String COLUMN_NAME_CURRENT_BUILD = "CURRENT_BUILD";
    public static final String COLUMN_NAME_BRANCH_COUNT = "BRANCH_COUNT";
    public static final String VERSION_TRUNK = "trunk";
    public static final String VERSION_1_0_0 = "1.0.0";
}