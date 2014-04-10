include('/jagg/jagg.jag');

var url_prefix = context.get(ISSUE_TRACKER_URL)+context.get(DOMAIN);

var getAllIssue = function (projectId) {
    if(projectId === undefined || projectId === null){
        projectId = 0;
    }
    var url  = url_prefix+"/project/"+projectId+"/issue";
    var data = {  };
    var issues = get(url, {} ,"json");
    return issues;
};

var getIssueByKey = function (issueKey) {
    var url  = url_prefix+"/issue/"+issueKey;
    var result = get(url, {} ,"json");
    var user = context.get(LOGGED_IN_USER);
    var commentList = result.data.issueResponse.comments;
    for(var i in commentList){
        if(user == commentList[i].creator) {
            commentList[i].isOwner=true
        } else
            commentList[i].isOwner=false
    }
    return result.data.issueResponse;
};

var addIssue = function (projectKey, jsonString){
    var user = context.get(LOGGED_IN_USER);
    var jsonObj = parse(jsonString);
    jsonObj.reporter =  user;
    var proj = new Object();
    proj.issue=jsonObj;
    jsonString = stringify(proj);
    var result;
    var url = url_prefix+"/project/"+projectKey+"/issue";

    result = post(url, jsonString, {
            "Content-Type": "application/json"
        }, 'json');
    return result;
}

var editIssue = function (issueKey, jsonString){
    var user = context.get(LOGGED_IN_USER);
    var jsonObj = parse(jsonString);
    jsonObj.reporter =  user;
    var proj = new Object();
    proj.issue=jsonObj;
    jsonString = stringify(proj);
    var result;
        var url = url_prefix+"/issue/"+issueKey;
        result = post(url, jsonString, {
            "Content-Type": "application/json"
        }, 'json');
    return result;
}

var searchIssue = function (searchType, searchValue) {
    var jsonObj = new Object();
    jsonObj.searchType = searchType;
    jsonObj.searchValue = searchValue;
    var json = new Object();
    json.searchBean = jsonObj;
    var url  = url_prefix+"/issue/search";
    var jsonString = stringify(json);
    result = post(url, jsonString, {
        "Content-Type": "application/json"
    }, 'json');
    return result.data.searchResponse;
};