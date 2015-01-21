function getUser() {
    return require('store').server.current(session);
}

function isUserLoggedIn() {
    if (!getUser()) {
        response.sendRedirect('/CXODashboard/login');
    }else{
        return true;
    }
}

function getTenantID() {
    if (isUserLoggedIn()) {
        return getUser().tenantId;
    }

}

function getTenantDomain(){
    var carbon = require('carbon');
    var tenantId = getTenantID();
    var tenantDomain = carbon.server.tenantDomain({tenantId : tenantId});
    tenantDomain ='"'+tenantDomain+'"';
    return tenantDomain; 
}
