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
package org.wso2.carbon.appfactory.utilities.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.utilities.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.CarbonConfigurationContextFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter class to make the Appfactory application as the root application in
 * his domain.
 * When the request is made to ROOT ("/") then this filter gets activated and
 * redirects to the appfactory home page.
 * Configuration for this filter should be available at carbon/web.xml.
 *
 * @author shamika
 */
public class RootContextRedirectFilter implements Filter {

    Log log =
            LogFactory.getLog(org.wso2.carbon.appfactory.utilities.filters.RootContextRedirectFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException,
            ServletException {
        if (!(response instanceof HttpServletResponse)) {
            return;
        }
        String appContext =
                ServiceReferenceHolder.getInstance()
                        .getAppFactoryConfiguration()
                        .getFirstProperty(AppFactoryConstants.APPFACTORY_WEB_CONTEXT_ROOT);

        if (appContext != null && !appContext.isEmpty()) {
            log.info("ROOT / context is called. Redirecting to App Factory home page with context - " +
                    appContext);
            ((HttpServletResponse) response).sendRedirect(appContext);
        } else {
            log.debug("No appfactory root context is defined in appfactory.xml. Request will be redirected to carbon console");
            RequestDispatcher requestDispatcher =
                    request.getRequestDispatcher(CarbonConfigurationContextFactory.getConfigurationContext()
                            .getContextRoot());
            requestDispatcher.forward(request, response);
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
