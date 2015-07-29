// global data store
var currentVersion = null;
var isInit = true;
var devStudioLink = "http://wso2.com/more-downloads/developer-studio/";
var versionChangeEventAdded = false;

// page initialization
$(document).ready(function() {
    // set current version
    setCurrentVersion();
    // initialize page and handlers
    initPageView();
    // load initial data to the page
    loadTeamInfo();
    loadAppInfoFromServer(currentVersion);
});

// set default app version
function setCurrentVersion() {
    var appType = applicationInfo.type;
    var version = "trunk";
    if (appType && appType.indexOf("Uploaded") >= 0) {
        version = "1.0.0";
    }
    currentVersion = version;
}

// wrapping functions
function initPageView() {
    loadAppIcon(applicationInfo.key);
}

// Icon initialization
function loadAppIcon(appKey) {
    jagg.post("../blocks/application/get/ajax/list.jag", {
        action: "isAppIconAvailable",
        applicationKey: appKey
    },

    function (result) {
        if(result == 101) {
            // Application icon is not available, set the default
            $(".app-icon").attr('src', servicePath  + '/site/themes/default/assets/img/app_icon.png');
            console.info("101");
        } else {
            $(".app-icon").attr('src', iconUrl);
        }
    }, function (jqXHR, textStatus, errorThrown) {
        console.log("Could not load the application icon!");
    });


    // add upload app icon listener
    $("#change_app_icon").change(function(event) {
       submitChangeAppIcon(this);
    });

}

// load team information
function loadTeamInfo() {
    jagg.post("../blocks/application/user/get/ajax/list.jag", {
            action:"getUsersOfApplication",
            applicationKey:applicationInfo.key
    },function (result) {
        var teamMemberCount = 0;
        if(result) {
            var applicationUserList = JSON.parse(result);
            teamMemberCount = applicationUserList.length;
            $("#teamCount").html(teamMemberCount);
        }
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content:'Could not load team information!', type:'error', id:'notification' });
    });
}

// load main application data from server side
function loadAppInfoFromServer(version) {
    // show loading image
    $('.loader').loading('show');
    $('.loading-cover').overlay('show');

    jagg.post("../blocks/application/get/ajax/list.jag", {
          action:"getAppVersionsInStages",
          applicationKey: applicationInfo.key,
          userName: $("#userName").attr('value')
    },function (result) {
            var resultData = jQuery.parseJSON(result);
            if (resultData.length > 0) {
                // get the relevant application info object
                // since this always gives only one element array, take the first element
                var appInfo = resultData[0];
                var currentAppInfo = filterAppVersionInfo(appInfo, version);

                // load application version specific data
                // note : need to hide overlay in the final ajax call's callback function
                if (currentAppInfo) {
                    loadLifeCycleManagementInfo(currentAppInfo);
                    loadRepoAndBuildsInfo(currentAppInfo);

                    // Asyn calls
                    loadLaunchInfo(appInfo, currentAppInfo);
                    loadIssuesInfo(version);
                    loadDatabaseInfo(currentAppInfo);
                }
            }
      },function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content:'Could not load Application information', type:'error', id:'notification' });
            }
      });
}

// filter application data by version
function filterAppVersionInfo(appInfo, version) {
    var appVersionInfo = null;
    if (!appInfo || !version) {
        return appVersionInfo;
    }
    for (var i in appInfo.versions) {
        var versionInfo = appInfo.versions[i];
        if (versionInfo.version == version) {
            appVersionInfo = versionInfo;
            break;
        }
    }
    return appVersionInfo;
}

// load database information
function loadDatabaseInfo(appVersionInfo) {
    jagg.post("../blocks/resources/database/add/ajax/add.jag", {
            action : "getDatabasesForStage",
            applicationKey : applicationInfo.key,
            stage : appVersionInfo.stage
    },function (result) {
        var databaseList = JSON.parse(result);
        databaseCount = databaseList.length;
        $("#databaseCount").html(databaseCount);
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content:'Could not load database information!', type:'error', id:'notification' });
    });
}


// load launch url information
function loadLaunchInfo(appInfo, currentAppInfo) {

    var versionOptionListHtml = "";

    for (var i in appInfo.versions) {
        var versionInfo = appInfo.versions[i];
        versionOptionListHtml += "<option>";
        versionOptionListHtml += versionInfo.version;
        versionOptionListHtml += "</option>";
    }
    $("#appVersionList").html(versionOptionListHtml);
    $('#appVersionList').val(currentAppInfo.version);

    // set launch app url
    loadLaunchUrl(currentAppInfo.version, currentAppInfo.stage);

    if (!versionChangeEventAdded) {
        $('#btn-launchApp').click(function() {
            var appUrl = $('#btn-launchApp').attr("url");
            var newWindow = window.open('','_blank');
            newWindow.location = appUrl;
        });

        // add listener for cloud envy
        $('#createCodeEnvyUrl').click(function() {
            if(!isCodeEditorSupported) {
                jagg.message({content: "Code editor not supported for the " + applicationInfo.type + " application type!", type: 'error', id:'message_id'});
            } else {
                createCodeEnvyUrl(currentAppInfo.repoURL);
            }
        });

        // add listener for developer studio
        $('#localIde').click(function() {
            var newWindow = window.open('','_blank');
            newWindow.location = devStudioLink;
        });

        $("#appVersionList").change(function() {
            // reload page info for the selected version
            currentVersion = this.value;
            loadAppInfoFromServer(currentVersion);
        });
        versionChangeEventAdded = true;
    }

}

//// load application launch url
function loadLaunchUrl(version, stage) {
    $('#btn-launchApp').attr('disabled','disabled');

    jagg.post("../blocks/application/get/ajax/list.jag", {
       action: "getMetaDataForAppVersion",
       applicationKey: applicationInfo.key,
       version: version,
       stage: stage,
       state: "started",
       type: applicationInfo.type
    }, function (result) {
        if(result) {
           var resJSON = jQuery.parseJSON(result);
           var appURL = "deployment in progress...";
           if(resJSON.url) {
               appURL = resJSON.url;
           }
            // display app url
            var repoUrlHtml = "<b>URL : </b>" + appURL;
            $("#app-version-url").html(repoUrlHtml);
            if(appURL.indexOf("progress"!= -1)){
                $('#version-url-link').attr({href:appURL});
                // set url to launch button
                $('#btn-launchApp').attr({url:appURL});
                $('#btn-launchApp').removeAttr('disabled');
                // create accept and deploy section
                showAcceptAndDeploy(appInfo, currentAppInfo, resJSON.url);
            }
        }
    }, function (jqXHR, textStatus, errorThrown) {
            // show error to the user
            var appURL = "deployment error";
            var repoUrlHtml = "<b>URL : </b>" + appURL;
            $("#app-version-url").html(repoUrlHtml);

            // log error
            jagg.message({content:'Could not load Application deployment information!', type:'error', id:'notification' });
    });
}


// load code envy editor
function createCodeEnvyUrl(gitURL) {
    jagg.post("../blocks/reposBuilds/list/ajax/list.jag", {
            action: "createCodeEnvyUrl",
            gitURL: gitURL,
            applicationKey: applicationInfo.key,
            appType: applicationInfo.type,
            version:currentVersion
        }, function (result) {
            if(result) {
                var newWindow = window.open('','_blank');
                newWindow.location = result;
            } else {
                jagg.message({content: "Failed creating the Codenvy workspace URL! ", type: 'error', id:'message_id'});
            }
        });
}

// load life cycle management information
function loadLifeCycleManagementInfo(appVersionInfo) {
    if(appVersionInfo) {
        var stage = appVersionInfo.appStage;
        $("#appStage").html(stage);
    }
}

// load repository and build information
function loadRepoAndBuildsInfo(appVersionInfo) {
    if (appVersionInfo) {
        var versionStage = appVersionInfo.stage;
        var lastBuildInfo = appVersionInfo.lastBuildResult;
        var buildSplitted = lastBuildInfo.split(' ');

        var buildNumber = 0;
        var buildStatus = "";

        if (buildSplitted.length > 1 && buildSplitted[1] != "null") {
            buildNumber = buildSplitted[1];
        }

        if (buildSplitted.length > 2 && buildSplitted[2] != "null") {
            buildStatus = buildSplitted[2];
        }

        // show data in the UI
        $("#lifecycle-mgt-main").html("Application <strong>" + applicationInfo.name + "</strong> is in <br> <strong>" +
            versionStage + "</strong> Stage");
        $("#success-and-fail-ids").html("Build " + buildNumber + " " + buildStatus);
    }
}

// load issue tracking information
function loadIssuesInfo() {
    jagg.post("../blocks/issuetracker/list/ajax/list.jag", {
        action:"getIssuesSummary",
        applicationKey:applicationInfo.key
    },function (result) {
        var resultJson = JSON.parse(result);
        var issueData = {'IMPROVEMENT':'0','NEW_FEATURE':'0','BUG':'0', 'TASK': '0'};
        for(var key in resultJson) {
            if (resultJson.hasOwnProperty(key) && key === currentVersion) {
                issueData = resultJson[currentVersion];
                break;
            }
        }

        // render issue data
        var issueSegment = formatCount(issueData.BUG);
        issueSegment += " Bugs<br>";
        issueSegment +=  formatCount(issueData.NEW_FEATURE);
        issueSegment += " Features<br>";
        issueSegment += formatCount(issueData.IMPROVEMENT);
        issueSegment += " Improvements<br>";
        issueSegment += formatCount(issueData.TASK);
        issueSegment += " Tasks";
        $("#issueCount").html(issueSegment);

        // hide loading image after loading all the version specific data
        $('.loader').loading('hide');
        $('.loading-cover').overlay('hide');
    },function (jqXHR, textStatus, errorThrown) {
        jagg.message({content:'Could not load Application issue information!', type:'error', id:'notification' });
    });
}

// Uploading application icon
function submitChangeAppIcon(newIconObj) {
    var validated = validateIconImage(newIconObj.value, newIconObj.files[0].size);
    if(validated) {
        $('#changeAppIcon').submit();
    }
}

function validateIconImage(filename, fileSize) {
    var ext = getFileExtension(filename);
    var extStatus = false;
    var fileSizeStatus = true;
    switch (ext.toLowerCase()) {
        case 'jpg':
        case 'jpeg':
        case 'gif':
        case 'bmp':
        case 'png':
            extStatus = true;
            break;
        default:
            jagg.message({content: "Invalid image selected for Application Icon - Select a valid image", type: 'error', id:'notification'});
            break;
        }

        if((fileSize/1024) > 51200 && extStatus == true) {
            fileSizeStatus = false;
            jagg.message({content: "Image file should be less than 5MB", type: 'error', id:'notification'});
        }
        if(extStatus == true && fileSizeStatus == true) {
             return true;
        }
    return false;
}

function getFileExtension(filename) {
    var parts = filename.split('.');
    return parts[parts.length - 1];
}

// Utility Functions Goes Here
// number formatting util function
function formatCount(count) {
   if(count) {
       return count;
   }
   return 0;
}
