package org.wso2.carbon.issue.tracker.util;

import org.apache.log4j.Logger;

public class IssueUtils {

    private static Logger logger = Logger.getLogger(TenantUtils.class);

    /**
     * returns the project key given the issue key
     *
     * @param tenantDomain the tenant domain
     * @return the tenant id
     * @throws IssueTrackerException an error while accessing the user store.
     */
    public static String getProjectKey(String issueKey) {

        String splitResults[] = issueKey.split("-");

        return splitResults[0];
    }

}
