include('/jagg/jagg.jag');

var url_prefix = context.get(ISSUE_TRACKER_URL)+context.get(DOMAIN);
var log = new Log();
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

var getIssueByProjectKey = function (projectKey) {
    var url  = url_prefix+"/issue/"+projectKey;
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
        }, 'text');
    return result;
}

var editIssue = function (issueKey, jsonString){
    var user = context.get(LOGGED_IN_USER);
    var jsonObj = parse(jsonString);
    jsonObj.reporter =  user;
    var proj = new Object();
    proj.issue=jsonObj;
    jsonString = stringify(proj);
    log.info("Edit issue : " + jsonString);
    var result;
        var url = url_prefix+"/issue/"+issueKey;
        result = post(url, jsonString, {
            "Content-Type": "application/json"
        }, 'json');
    return result;
}
/*8
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
     return stringify(result.data.searchResponse);
};
*/
var getIssusesForDataTable = function (searchType, searchValue) {
    var jsonObj = {};
    jsonObj.searchType = searchType;
    jsonObj.searchValue = searchValue;
    var json = {};
    json.searchBean = jsonObj;
    var url  = url_prefix+"/issue/search";
    var jsonString = stringify(json);
    result = post(url, jsonString, {"Content-Type": "application/json"}, 'json');
    var dataarray = [];
    dataarray = result.data.searchResponse;
    if(dataarray.length==0){
    return null;
    }
    for(i=0;i<dataarray.length;i++){
    var issueObj = {};
    var issueNewVal = "<a href=\"\/issuetracker\/issue\/get?issuePkey=" + dataarray[i]['issuePkey'] + "&appkey=" + dataarray[i]['projectKey'] + "\">" + dataarray[i]['issuePkey'] + "<\/a>";
    dataarray[i]['edit'] = "<a href=\"\/issuetracker\/issue\/edit?issuePkey=" + dataarray[i]['issuePkey'] + "&projectKey=" + dataarray[i]['projectKey'] + "&appkey=" + dataarray[i]['projectKey'] + "\"><i class=\'fa fa-edit\'><\/i><\/a>";
    dataarray[i]['comment'] = "<a href=\"\/issuetracker\/issue\/get?issuePkey=" + dataarray[i]['issuePkey'] + "&appkey=" + dataarray[i]['projectKey'] + "\"><i class=\'fa fa-comment\'><\/i><\/a>";
    dataarray[i]['issuePkey'] = issueNewVal;
    }
    return stringify(dataarray);
};
