include('/jagg/jagg.jag');
var url_prefix = context.get(ISSUE_TRACKER_URL)+context.get(DOMAIN)+"/project/";
var log = new Log();
var getProjectsOfDomain = function () {
    var url  = url_prefix;
    var projects = get(url, {} ,"json");
    if(request.getParameter("appkey")){
        for (var project in projects.data.project){
            if (projects.data.project[project].key == request.getParameter("appkey")){
                var newProjects = [];
                newProjects.push(projects.data.project[project]);
                return newProjects;
            }
        }
    }
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
