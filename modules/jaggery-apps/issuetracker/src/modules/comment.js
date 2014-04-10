include('/jagg/jagg.jag');
var url_prefix = context.get(ISSUE_TRACKER_URL)+context.get(DOMAIN);

var addComment = function (issueKey, jsonString){
    var user = context.get(LOGGED_IN_USER);
    var jsonObj = parse(jsonString);
    jsonObj.creator =  user;
    var proj = new Object();
    proj.comment=jsonObj;
    jsonString = stringify(proj);
    var result;
    var url  = url_prefix+"/issue/"+issueKey+"/comment";
    result = post(url, jsonString, {
        "Content-Type": "application/json"
    }, 'json');
    return result;
}

var editComment = function (issueKey, commentId, jsonString){
    var user = context.get(LOGGED_IN_USER);
    var jsonObj = parse(jsonString);
    jsonObj.creator =  user;
    var proj = new Object();
    proj.comment=jsonObj;
    jsonString = stringify(proj);
    var result;
    var url  = url_prefix + "/issue/"+issueKey+"/comment/"+commentId;
    result = post(url, jsonString, {
        "Content-Type": "application/json"
    }, 'json');
    return result;
}

var deleteComment = function (issueKey, commentId){
    var result;
    var url  = url_prefix+"/issue/"+issueKey+"/comment/"+commentId;
    var data = {};
    var headers = {};
    result = del(url, data, 'json');
    return result;
}