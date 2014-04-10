package org.wso2.carbon.appfactory.stratos.listeners;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.beans.TenantInfoBean;
import org.apache.stratos.common.exception.StratosException;
import org.apache.stratos.common.listeners.TenantMgtListener;
import org.apache.stratos.keystore.mgt.util.RegistryServiceHolder;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.security.SecurityConstants;



public class AppFactoryTenantKeyStoreMgtListener implements TenantMgtListener{

	private static Log log = LogFactory.getLog(AppFactoryTenantKeyStoreMgtListener.class);
    private static final int EXEC_ORDER = 10;
    
	@Override
	public void onTenantCreate(TenantInfoBean tenantBean) throws StratosException {
		try {
			
			String serverURL = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(AppFactoryConstants.APPFACTORY_SERVER_URL);
			
			//Login to the AF and get the cookie
			String cookie = authendicateToRemoteServer(serverURL, tenantBean.getAdmin() + "@" + tenantBean.getTenantDomain(), 
					tenantBean.getAdminPassword());
			
			//Build keystore paths
			String keyStoreName = tenantBean.getTenantDomain().trim().replace(".", "-") + ".jks";
			String keyStorePath = RegistryResources.SecurityManagement.KEY_STORES + "/" + keyStoreName;
			String pubKeyResourcePath = RegistryResources.SecurityManagement.TENANT_PUBKEY_RESOURCE;
			
			//Retrieve remote resource
			Resource remoteKeyStore = getRemoteResource(serverURL, cookie, keyStorePath);
			Resource remotePubKeyResource = getRemoteResource(serverURL, cookie, pubKeyResourcePath);
			
			//Copy resource to governance registry and add resource
			UserRegistry govRegistry = RegistryServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantBean.getTenantId());
			copyResource(RegistryResources.SecurityManagement.KEY_STORES + "/" + keyStoreName, remoteKeyStore, govRegistry);
			copyResource(RegistryResources.SecurityManagement.TENANT_PUBKEY_RESOURCE, remotePubKeyResource, govRegistry);
			govRegistry.addAssociation(RegistryResources.SecurityManagement.KEY_STORES + "/" + keyStoreName,
                    RegistryResources.SecurityManagement.TENANT_PUBKEY_RESOURCE,
                    SecurityConstants.ASSOCIATION_TENANT_KS_PUB_KEY);
			govRegistry.addAssociation(RegistryResources.SecurityManagement.KEY_STORES + "/" + keyStoreName,
                    RegistryResources.SecurityManagement.TENANT_PUBKEY_RESOURCE,
                    SecurityConstants.ASSOCIATION_TENANT_KS_PUB_KEY);
			
		} catch (AppFactoryException e) {
			String msg = "Error while reading configuration";
			log.error(msg, e);
		} catch (RegistryException e) {
			String msg = "Error while reading remote registry";
			log.error(msg, e);
		} catch (AxisFault e) {
			String msg = "Error while authendicating remote registry";
			log.error(msg, e);
		} catch (RemoteException e) {
			String msg = "Error while authendicating remote registry";
			log.error(msg, e);
		} catch (LoginAuthenticationExceptionException e) {
			String msg = "Error while authendicating remote registry";
			log.error(msg, e);
		} catch (MalformedURLException e) {
			String msg = "Error while generating server url";
			log.error(msg, e);
		}
		
	}
	
	@Override
	public int getListenerOrder() {
		return EXEC_ORDER;
	}

	@Override
	public void onSubscriptionPlanChange(int tenantId, String oldPlan, 
            String newPlan) throws StratosException {
		// It is not required to implement this method for keystore mgt. 		
	}

	@Override
	public void onTenantActivation(int tenantId) throws StratosException {
		// It is not required to implement this method for keystore mgt. 		
	}

	@Override
	public void onTenantDeactivation(int tenantId) throws StratosException {
		// It is not required to implement this method for keystore mgt. 		
	}

	@Override
	public void onTenantInitialActivation(int tenantId) throws StratosException {
		// It is not required to implement this method for keystore mgt. 		
	}

	@Override
	public void onTenantRename(int tenantId, String oldDomainName,
            String newDomainName) throws StratosException {
		// It is not required to implement this method for keystore mgt. 		
	}

	@Override
	public void onTenantUpdate(TenantInfoBean tenantBean) throws StratosException {
		// It is not required to implement this method for keystore mgt. 
		
	}

    @Override
    public void onTenantDelete(int i) {
        // It is not required to implement this method for keystore mgt.
    }

    private String authendicateToRemoteServer(String serverURL, String username, String password) throws RemoteException, MalformedURLException, LoginAuthenticationExceptionException{
		String cookie = "";
		String serviceEPR = serverURL + "AuthenticationAdmin";
		AuthenticationAdminStub authStub = new AuthenticationAdminStub(serviceEPR);
		ServiceClient client = authStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        boolean result = authStub.login(username, password, new URL(serviceEPR).getHost());
        if (result){
        	cookie = ((String) authStub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING));
        } else {
        	throw new LoginAuthenticationExceptionException("Login Failed");
        }
        return cookie;
	}
	
	private Resource getRemoteResource(String serverURL, String cookie, String resourcePath) throws RegistryException{		
		WSRegistryServiceClient wsclient =  new WSRegistryServiceClient(serverURL, cookie);	
		Resource resource = wsclient.get(RegistryUtils.getAbsolutePathToOriginal(resourcePath, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH));
		return resource;
	}
	
	private void copyResource(String path, Resource resource, UserRegistry govRegistry) throws RegistryException{
		if(govRegistry.resourceExists(path)){
			govRegistry.delete(path);
		}
		govRegistry.put(path, resource);
	}
		
}
