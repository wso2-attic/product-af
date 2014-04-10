var USER = 'server.user';

var USER_REGISTRY = 'server.user.registry';

var USER_OPTIONS = 'server.user.options';

var USER_SPACE = 'server.user.space';

var USER_ROLE_PREFIX = 'private_';

var init = function (options) {
    var role, roles, user,
        server = require('/modules/server.js'),
        um = server.userManager();
    roles = options.roles;
    for (role in roles) {
        if (roles.hasOwnProperty(role)) {
            if (um.roleExists(role)) {
                um.authorizeRole(role, roles[role]);
            } else {
                um.addRole(role, [], roles[role]);
            }
        }
    }
    user = um.getUser(options.user.username);
    if (!user.hasRoles(options.userRoles)) {
        // PATCH
        // Comment out role add option
        // user.addRoles(options.userRoles);
        // END PATCH
    }
    application.put(USER_OPTIONS, options);
};

var options = function () {
    return application.get(USER_OPTIONS);
};

var login = function (username, password) {
    var user, perm, perms, actions, i, length,
        authorized = false,
        opts = options(),
        carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server(),
        um = server.userManager();
    if (!serv.authenticate(username, password)) {
        return false;
    }
    user = um.getUser(username);
    perms = opts.permissions.login;
    L1:
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    if (user.isAuthorized(perm, actions[i])) {
                        authorized = true;
                        break L1;
                    }
                }
            }
        }
    if (!authorized) {
        return false;
    }
    session.put(USER, new carbon.user.User(um, username));
    session.put(USER_REGISTRY, new carbon.registry.Registry(serv, {
        username: username,
        tenantId: carbon.server.tenantId()
    }));
    session.put(USER_SPACE, new carbon.user.Space(username, opts.userSpace.space, opts.userSpace.options));
    if (opts.login) {
        opts.login(user, password, session);
    }
    return true;
};

var isAuthorized = function (permission, action) {
    var user = current(),
        server = require('/modules/server.js'),
        um = server.userManager();
    user = user ? user.username : options().user.username;
    return um.getUser(user).isAuthorized(permission, action);
};

var userSpace = function () {
    try {
        return session.get(USER_SPACE);
    } catch (e) {
        return null;
    }
};

var userRegistry = function () {
    try {
        return session.get(USER_REGISTRY);
    } catch (e) {
        return null;
    }
};

var logout = function () {
    var opts = options(),
        user = current();
    if (opts.logout) {
        opts.logout(user, session);
    }
    session.remove(USER);
    session.remove(USER_SPACE);
    session.remove(USER_REGISTRY);
};

var userExists = function (username) {
    var server = require('/modules/server.js');
    return server.userManager().userExists(username);
};

var privateRole = function (username) {
    return USER_ROLE_PREFIX + username;
};

var register = function (username, password) {
    var user, role, id, perms, r, p,
        server = require('/modules/server.js'),
        um = server.userManager(),
        opts = options();
    um.addUser(username, password, opts.userRoles);
    user = um.getUser(username);
    role = privateRole(username);
    id = opts.userSpace.options.path + '/' + opts.userSpace.space;
    perms = {};
    perms[id] = [
        'http://www.wso2.org/projects/registry/actions/get',
        'http://www.wso2.org/projects/registry/actions/add',
        'http://www.wso2.org/projects/registry/actions/delete',
        'authorize'
    ];
    p = opts.permissions.login;
    for (r in p) {
        if (p.hasOwnProperty(r)) {
            perms[r] = p[r];
        }
    }
    um.addRole(role, [], perms);
    user.addRoles([role]);
    if (opts.register) {
        opts.register(user, password, session);
    }
    login(username, password);
};

/**
 * Returns the currently logged in user
 */
var current = function () {
    try {
        return session.get(USER);
    } catch (e) {
        return null;
    }
};

var loginWithSAML = function (username) {
    var user, perm, perms, actions, i, length,
        authorized = false,
        opts = options(),
        carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server();
        //um = server.userManager();

    // PATCH 
    // Fix the user retreive to be tenant aware...
    
    //user = um.getUser(username);
    var tenantUserName = carbon.server.userNamewithoutTenantDomain(username);
    var tenantDomain = carbon.server.tenantDomainFromUserName(username);
    var um = new carbon.user.UserManager(serv, tenantDomain);
    var user = um.getUser(tenantUserName);
    // END PATCH


    perms = opts.permissions.login;
    L1:
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    if (user.isAuthorized(perm, actions[i])) {
                        authorized = true;
                        break L1;
                    }
                }
            }
        }
    if (!authorized) {
        return false;
    }
    session.put(USER, new carbon.user.User(um, username));

    // PATCH 
    // Take tenant registry to store user perfs...

    session.put(USER_REGISTRY, new carbon.registry.Registry(serv, {
        username: tenantUserName,
        tenantId: um.tenantId
    }));

    opts.userSpace.options.username = tenantUserName;
    opts.userSpace.options.tenantDomain = tenantDomain;

    //session.put(USER_SPACE, new carbon.user.Space(tenantUserName, opts.userSpace.space, opts.userSpace.options));
    if (opts.login) {
        opts.login(user, "", session);
    }

    var permission = {};
    permission[opts.userSpace.options.path + '/' + tenantUserName ] = [
        carbon.registry.actions.GET,
        carbon.registry.actions.PUT,
        carbon.registry.actions.DELETE
    ];
    um.authorizeRole(privateRole(tenantUserName), permission);
    // END PATCH

    return true;
};


// PATCH 
// CHECK USER HAS THE GIVEN ROLE
var userHasRole = function (user, role) {

    var carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server();

    var username = user.username;
    var tenantUserName = carbon.server.userNamewithoutTenantDomain(username);
    var tenantDomain = carbon.server.tenantDomainFromUserName(username);
    var um = new carbon.user.UserManager(serv, tenantDomain);

    var roleArray = um.getRoleListOfUser(tenantUserName);
    for(var i=0; i< roleArray.length ; i++){
        if(roleArray[i]==role){
	 return true;
	}
	
    }

    return false;

}

// END PATCH
