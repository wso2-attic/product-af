var configs = require('/config.json');


var server = require('store').server;
server.init(configs);

var user = require('store').user;
user.init(configs);

//addRxtConfigs(tenantId);
var event = require('event');

event.on('tenantCreate', function (tenantId) {
    var role, roles,
       carbon = require('carbon'),
       mod = require('store'),
       server = mod.server,
       system = server.systemRegistry(tenantId),
       um = server.userManager(tenantId);
    system.put(options.tenantConfigs, {
       content: JSON.stringify(configs),
       mediaType: 'application/json'
    });
    roles = configs.roles;
    for (role in roles) {
       if (roles.hasOwnProperty(role)) {
           if (um.roleExists(role)) {
           um.authorizeRole(role, roles[role]);
           } else {
           um.addRole(role, [], roles[role]);
           }
       }
    }
});

