function getUser() {
    return require('/modules/user.js').current();
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
        return getUser().um.tenantId;
    }

}

function getTenantDomain(){
    var carbon = require('carbon');
    var tenantDomain = carbon.server.tenantDomainFromUserName(session.get("server.user").username);
    tenantDomain ='"'+tenantDomain+'"';
    return tenantDomain; 
}
