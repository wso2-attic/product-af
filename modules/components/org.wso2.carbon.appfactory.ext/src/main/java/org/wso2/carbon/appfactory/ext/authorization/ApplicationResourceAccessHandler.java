/*
 * Copyright 2005-2013 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.appfactory.ext.authorization;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.appfactory.ext.Util;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * This registry handle implementation is used to isolate application resources; to make sure resources in
 * one application is not visible to other application users.
 * Example violation is as below,
 * Registry systemGovRegistry = carbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE);
 * Resource resource = registry.get(resource path to another application resources);
 * <p/>
 * Sample registry handler configuration is as below.
 * <handler class="org.wso2.carbon.appfactory.ext.authorization.ApplicationResourceAccessHandler">
 * <filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher">
 * <property name="pattern">/_system/governance/dependencies/.*</property>
 * </filter>
 * </handler>
 */
public class ApplicationResourceAccessHandler extends Handler {
    private static final Log log = LogFactory.getLog(ApplicationResourceAccessHandler.class);

    /**
     * Check if current request can access the registry resource for given action such as GET,PUT,DELETE
     *
     * @param requestContext - information about current registry access request
     * @param action         - registry access action;ex.GET,PUT,DELETE
     * @return true if current access request is allowed, false otherwise.
     */
    public boolean accessApplication(RequestContext requestContext, String action) {
        String resourcePath = requestContext.getResourcePath().getPath();
        // if resource access request is made to access a resource under same application resource storage path, allow it.
        boolean canAccess = Util.pathContainsCurrentArtifactName(resourcePath);

        if (!canAccess) {
            canAccess = AppFactoryUtil.checkAuthorizationForUser(resourcePath, action);
        }
        return canAccess;
    }

    @Override
    public String move(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            return super.move(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public Resource get(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            return super.get(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }

    }

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            super.put(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void importResource(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            super.importResource(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.DELETE)) {
            super.delete(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public String copy(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            return super.copy(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public String rename(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            return super.rename(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void putChild(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            super.putChild(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void importChild(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            super.importChild(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void invokeAspect(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            super.invokeAspect(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void restoreVersion(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            super.restoreVersion(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void createVersion(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            super.createVersion(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public String[] getVersions(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            return super.getVersions(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public Collection executeQuery(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            return super.executeQuery(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public Collection searchContent(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            return super.searchContent(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void dump(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            super.dump(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public void restore(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.PUT)) {
            super.restore(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }

    @Override
    public boolean resourceExists(RequestContext requestContext) throws RegistryException {
        if (accessApplication(requestContext, ActionConstants.GET)) {
            return super.resourceExists(requestContext);
        } else {
            // no need to log the errors, it is logged by registry implementation itself.
            if (requestContext.getResourcePath() != null) {
                throw new RegistryException("Illegal access attempt on registry resource:" +
                        requestContext.getResourcePath().getPath());
            } else {
                throw new RegistryException("Illegal access attempt on registry resource");
            }
        }
    }
}
