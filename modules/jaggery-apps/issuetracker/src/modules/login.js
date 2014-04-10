var site = config = require('/tracker.json');
var security =require("sso");
var isLogged = function(){
    var ssoRelyingParty = new security.SSORelyingParty(site.ssoConfiguration.issuer);
    var sessionId = session.getId();
    var isAuthenticated = ssoRelyingParty.isSessionAuthenticated(sessionId);
    if(isAuthenticated){
        return true;
    } else {
        include('/login.jag');
        return false;
    }
}
