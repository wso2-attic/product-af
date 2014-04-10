include('/jagg/jagg.jag');
var url_prefix = context.get(ISSUE_TRACKER_URL)+context.get(DOMAIN)+"/user";

var getUsersOfDomain = function () {
    var url  = url_prefix;
    var users = get(url, {} ,"json");
    return users.data.user;
};

