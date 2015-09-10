// global data store
var currentVersion = null;
var isInit = true;
var devStudioLink = "https://docs.wso2.com/display/AF210/Use+WSO2+Developer+Studio+to+Develop+an+Application";
var versionChangeEventAdded = false;
var timer = null;
var currentMessage = null;
var popoverAdded = false;

// page initialization
$(document).ready(function() {
    // show the application creation status as a notification
    if("created" == userAction) {
        showTopMessage("Application creation started.");
    }
    // set current version
    setDefaultVersion();
    // initialize page and handlers
    initPageView();
    // load initial data to the page
    loadTeamInfo();
    loadAppInfoFromServer(currentVersion);
});

// set default app version
function setDefaultVersion() {
    var appType = applicationInfo.type;
    currentVersion = "trunk";
    if (appType && appType.indexOf("Uploaded") >= 0) {
        currentVersion = "1.0.0";
    }
    if(previousVersion && previousVersion != null) {
        currentVersion = previousVersion;
    }
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
            $("#app-icon").attr('src', servicePath  + '/site/themes/default/images/dark-app-iconx256.jpg');
            console.info("101");
        } else {
            $("#app-icon").attr('src', iconUrl);
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
          action:"getAppVersionAllInfoByVersion",
          applicationKey: applicationInfo.key,
          userName: $("#userName").attr('value'),
          version: version
    },function (result) {
            var appInfo = jQuery.parseJSON(result);
            if (appInfo) {
                var currentAppInfo = appInfo.versionInfo;

                // load application version specific data
                // note : need to hide overlay in the final ajax call's callback function
                if (currentAppInfo) {
                    loadLifeCycleManagementInfo(currentAppInfo);
                    loadRepoAndBuildsInfo(currentAppInfo);

                    // Asyn calls
                    loadLaunchInfo(appInfo, currentAppInfo);
                    loadIssuesInfo(version);
                    loadDatabaseInfo(currentAppInfo);
                    addVersionCreationHandler();
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
        var version = appInfo.versions[i];
        versionOptionListHtml += "<li><a href='#'>";
        versionOptionListHtml += version;
        versionOptionListHtml += "</a></li>";
    }

    $("#appVersionList").html(versionOptionListHtml);
    $('#selected-version').html(currentAppInfo.version);


    // set launch app url
    loadLaunchUrl(appInfo, currentAppInfo);

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
        $('#btnEditCode').click(function() {
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

        versionChangeEventAdded = true;
    }

    $('#appVersionList li').click(function() {
        // reload page info for the selected version
        currentVersion = this.textContent;
        loadAppInfoFromServer(currentVersion);
    });

    // show hide accept and deploy handler
    showAcceptAndDeploy(appInfo, currentAppInfo);
}

//// load application launch url
var loadLaunchUrl = function(appInfo, currentAppInfo) {
    jagg.post("../blocks/application/get/ajax/list.jag", {
       action: "getMetaDataForAppVersion",
       applicationKey: applicationInfo.key,
       version: currentAppInfo.version,
       stage: currentAppInfo.stage,
       state: "started",
       type: applicationInfo.type
    }, function (result) {
        if(result) {
            var resJSON = jQuery.parseJSON(result);
            if(resJSON) {

                // display app url
                var repoUrlHtml = "<b>URL : </b>" + appURL;
                if("Success" == resJSON.status) {
                    var appURL = resJSON.url;
                    // display app url
                    var repoUrlHtml = "<b>URL : </b>" + appURL;
                    $("#app-version-url").html(repoUrlHtml);
                    $('#version-url-link').attr({href:appURL});
                    $('#btn-launchApp').attr({url:appURL});

                    // set url to launch button
                    $('#btn-launchApp').removeAttr('disabled');
                    $('#version-url-link').removeAttr('disabled');

                    // clear the timer if exist
                    clearTimeout(timer);
                    hideTopMessage();
                } else {
                   // remove status message
                   var repoUrlHtml = "<b>URL : </b>deployment in progress...";
                   $("#app-version-url").html(repoUrlHtml);
                   // disable links and buttons
                   $('#btn-launchApp').attr('disabled','disabled');
                   $('#version-url-link').attr('disabled','disabled');
                   // remove previous urls
                   $('#version-url-link').removeAttr('href');
                   $('#btn-launchApp').removeAttr('url');

                   // set the timer until the app get deployed
                   poolUntilAppDeploy(loadLaunchUrl, appInfo, currentAppInfo);
                }
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

// pool until the app is deployed
function poolUntilAppDeploy(callback, appInfo, currentAppInfo) {
    clearTimeout(timer);
    timer = setTimeout(callback, 5000, appInfo, currentAppInfo);
}

// load code envy editor
function createCodeEnvyUrl(gitURL) {
    var codeEnvyUrl = null;
    var newWindow = window.open('','_blank');
    jagg.post("../blocks/reposBuilds/list/ajax/list.jag", {
            action: "createCodeEnvyUrl",
            gitURL: gitURL,
            applicationKey: applicationInfo.key,
            appType: applicationInfo.type,
            version:currentVersion
        }, function (result) {
            codeEnvyUrl = result;
            if(codeEnvyUrl==="" || codeEnvyUrl==null){
                jagg.message({
                                 content: "Failed creating the Codenvy workspace URL!",
                                 type: 'error',
                                 id:'message_id'
                             });

            }else{
                newWindow.location = codeEnvyUrl;
            }
        });
}

// load life cycle management information
function loadLifeCycleManagementInfo(appVersionInfo) {
    if(appVersionInfo && !isEmpty(appVersionInfo)) {
        var stage = appVersionInfo.appStage;
        $("#appStage").html(stage);
    }
}

// load repository and build information
function loadRepoAndBuildsInfo(appVersionInfo) {
    if (appVersionInfo && !isEmpty(appVersionInfo)) {
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
        $("#lifecycle-mgt-main").html("Application <strong>" + applicationInfo.name + "</strong> is in <br> <strong>" + versionStage + "</strong> Stage");

        var buildStatusTag = "";
        if("successful" == buildStatus)  {
            buildStatusTag += "<i class='fa fa-smile-o'></li>";
        } else {
            buildStatusTag += "<i class='fa fa-frown-o'></li>";
        }
        buildStatusTag += " Build ";
        buildStatusTag += buildNumber;
        buildStatusTag += " ";
        buildStatusTag += buildStatus

        $("#success-and-fail-ids").html(buildStatusTag);
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
        var issueSegment = "<i class='fa fa-bug'></i> ";
        issueSegment += formatCount(issueData.BUG);
        issueSegment += " Bugs<br>";
        issueSegment += "<i class='fa fa-leaf'></i> ";
        issueSegment +=  formatCount(issueData.NEW_FEATURE);
        issueSegment += " Features<br>";
        issueSegment += "<i class='fa fa-line-chart'></i> ";
        issueSegment += formatCount(issueData.IMPROVEMENT);
        issueSegment += " Improvements<br>";
        issueSegment += "<i class='fa fa-paw'></i> ";
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

// check the file is an image file
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

// accept and deploy hide
function hideAcceptAndDeploy() {
    // hide the button by default
    $("#acceptDeployWrapper").hide();
}

// accept and deploy show
function showAcceptAndDeploy(appInfo, currentAppInfo) {
    var promoteStatus = currentAppInfo.promoteStatus;
    var pendingState = "pending";
    var deployAction = "deploy";
    var state = "started";
    var stage = currentAppInfo.stage;

    // hide the button by default
    hideAcceptAndDeploy();

    if(pendingState == promoteStatus && deploymentPermission[stage]) {
        addAcceptNDeployHandler(appInfo, currentAppInfo, deployAction, state);
    }
}

function addAcceptNDeployHandler(appInfo, currentAppInfo, deployAction, state) {
    $("#accepndeploy-button").off('click').click(function(event) {
        deployApp(currentAppInfo.version, currentAppInfo.stage, deployAction, state, appInfo.type, true);
    });
    $("#acceptDeployWrapper").show();
}


function addDeployHandler(appInfo, currentAppInfo, deployAction, state) {
    $("#accepndeploy-button").off('click').click(function(event) {
        deployApp(currentAppInfo.version, currentAppInfo.stage, deployAction, state, appInfo.type, false);
    });
    $("#acceptDeployWrapper").show();
}


function deployApp(version, stage, deployAction, state, type, isUpdateState) {
   jagg.post("../blocks/lifecycle/add/ajax/add.jag", {
           action: "copyNewDependenciesAndDeployArtifact",
           applicationKey: applicationInfo.key,
           deployAction:deployAction,
           stage:stage,
           tagName: "",
           version:version
       }, function (result) {
           jagg.message({content: "The Deployment is underway. Please wait and refresh page after few minutes.", type: 'success', id:'notification'});

           if (isUpdateState) {
                updateAppVersionPromoteStatus("", version, stage);
           }
           hideAcceptAndDeploy();
       }, function (jqXHR, textStatus, errorThrown) {
            jagg.message({content: "Error occurred while deploying the artifact.", type: 'error', id:'notification'});
       });
}

// update the app version promote status
function updateAppVersionPromoteStatus(nextState, version, nextStage) {
    jagg.post("../blocks/application/update/ajax/update.jag", {
       action: "updatePromoteStatus",
       applicationKey: applicationInfo.key,
       nextStage:nextStage,
       version:version,
       state:nextState
    }, function (result) {
        console.info(result);
    },
    function (jqXHR, textStatus, errorThrown) {
        console.log("Error while updating application promote status!");
    });
}

function showTopMessage(msg) {
    if(msg) {
        currentMessage = jagg.message({content:msg, type:'success', id:'notification' });
    }
}

function hideTopMessage() {
    if (currentMessage && currentMessage.options) {
        var id = currentMessage.options.id;
        jagg.removeMessageById(id);
    }
}

function addVersionCreationHandler() {
    // create the message
    var version = "";
    if("trunk" == currentVersion) {
        version = currentVersion;
    } else {
        version = "v ";
        version += currentVersion;
    }
    var message = "You are creating a new version from ";
    message += "<b>" + version + "</b>";
    message += " of your application";

    // create a popover
    $(".btn-create-version").popover("destroy");
    $(".btn-create-version").popover({
        title: message,
        html:true,
        content: $(".create-version-form-wrap").html(),
        placement: 'bottom'
    });

    // add button click handler
    $(".btn-create-version").on('shown.bs.popover', function() {
        $("#versionCreationBtn").click(function() {
            var newVersion = $("#new-version").val();
            createVersion(currentVersion, newVersion);
        });
    });
}

function createVersion(srcVersion, targetVersion) {
    if(validateVersion(targetVersion)) {
        // show message
        showVersionCreationMessage(srcVersion, targetVersion);
        $(".btn-create-version").popover("destroy");

        // create verion
        jagg.post("../blocks/reposBuilds/add/ajax/add.jag", {
            action: "invokeDoVersion",
            applicationKey: applicationInfo.key,
            srcVersion:srcVersion,
            targetVersion:targetVersion,
            lifecycleName:lifeCycleName
        }, function (result) {
            result = JSON.parse(result);
            if(result === true) {
                // load new data
                loadAppInfoFromServer(targetVersion);
            }
        }, function (jqXHR, textStatus, errorThrown) {
            jagg.message({
                content: "Error while creating version. Check the given version value and try again.",
                type: 'error'
            });
        });
    } else {
        jagg.removeMessage();
        jagg.message({
            content: "Invalid version number - Provide version number in format major.minor.patch",
            type: 'error',
            id:'reposBuild'
        });
    }
}

function showVersionCreationMessage(srcVersion, targetVersion) {
    // put a message to the user
    var message = "";
    message += "New version ";
    message == targetVersion;
    message += " has been created from ";
    message += srcVersion;
    message += " and the version has been set to ";
    message += targetVersion;
    jagg.message({content: message, type: 'success', id:'notification'});
}

function validateVersion(version) {
    var pattern = /^(\d{1,2}\.){2}(\d{1,5})$/;
    return pattern.test(version);
}

// Utility Functions Goes Here
// extract file extension
function getFileExtension(filename) {
    var parts = filename.split('.');
    return parts[parts.length - 1];
}

// number formatting util function
function formatCount(count) {
   if(count) {
       return count;
   }
   return 0;
}

function isEmpty(object) {
    if (!object) {
        return true;
    }
    if(Object.keys(object).length === 0) {
        return true;
    }
    return false;
}