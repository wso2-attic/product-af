include('/jagg/jagg.jag');
var url_prefix = context.get(ISSUE_TRACKER_URL)+context.get(DOMAIN)+"/project/";

var getProjectsOfDomain = function () {
    var url  = url_prefix;
    var projects = get(url, {} ,"json");
    return projects.data.project;
};

var getAllVersionOfProject = function ( projectKey){
    var url  = url_prefix+projectKey+"/version";
    var project = get(url,{},"json");
    return project.data.version;
}

var getProjectByKey = function (projectKey) {
    var url  = url_prefix+projectKey;
    var project = get(url, {} ,"json");
    return project.data.project;
};
