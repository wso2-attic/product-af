var multitenancy = {};

(function (multitenancy) {
   var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext,
       context = PrivilegedCarbonContext.getThreadLocalCarbonContext(),
       Class = java.lang.Class,
       realmService = context.getOSGiService(Class.forName('org.wso2.carbon.user.core.service.RealmService')),
       tenantManager = realmService.getTenantManager();
    /*usage multitenancy.executeAsTenant(tenantDomain,function(arg1,arg2){return},arg1,arg2);*/
    multitenancy.getPrivilegedCarbonContext = function () {
            return PrivilegedCarbonContext;
    };
    multitenancy.getTenantManager = function (){
            return tenantManager;
    };
}(multitenancy));
