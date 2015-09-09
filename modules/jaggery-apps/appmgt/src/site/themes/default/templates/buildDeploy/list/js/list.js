var lastBuildId = 0;
var timer = null;
var handlerAdded = false;

// initialize the page when document is ready
$(document).ready(function() {
    init();
});

var init = function() {
    getBuildAndDeploymentInfo(isForkInfo);
    getBuildHistory(isForkInfo);
}

var getBuildHistory = function (isForkData) {
    jagg.post("../blocks/buildDeploy/list/ajax/list.jag", {
          action:"getBuildHistory",
          applicationKey: applicationKey,
          applicationVersion: currentVersion,
          forkedRepository: isForkData,
          tenantDomain:tenantDomain
    },function (result) {
        if(isJsonString(result)) {
            var buildHistory = jQuery.parseJSON(result);
            drawBuildHistory(buildHistory);
            addBuildLogHandlers(buildHistory, isForkData);
        }
      },function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content:'Could not build history information', type:'error', id:'notification' });
            }
      });
}

var drawBuildHistory = function(buildHistory) {
    if(buildHistory && buildHistory.builds) {
        var builds = buildHistory.builds;
        var maxBuildId = getMaxBuildId(builds);
        if(maxBuildId === 0 || (maxBuildId > lastBuildId)) {
            lastBuildId = maxBuildId;
            $('#buildHistoryTbl').empty();
            for(var key in builds) {
                var buildInfo = builds[key];
                if(buildInfo) {
                    var date = new Date(parseInt(buildInfo.timestamp));
                    var message = "";
                    message += "<tr>";
                    message += "<td><span class='table-notification-msg";
                    if("SUCCESS" === buildInfo.result) {
                        message += " table-noti-success";
                    } else if("FAILURE" === buildInfo.result) {
                        message += " table-noti-error";
                    } else {
                        message += " table-noti-default";
                    }
                    message += " '></span>";
                    message += "Build   ";
                    message += buildInfo.id;
                    message += "</td>";
                    message += "<td>";
                    message += date.toDateString();
                    message += "</td>";
                    message += "<td><a href='#modal-one' data-toggle='modal' data-target='#modal-one' id='build-log-" + buildInfo.id + "'>Build Logs</a>";
                    message += "</td>";
                    message += "</tr>";

                   // set message
                   $('#buildHistoryTbl').append(message);
                }
            }
            clearTimeout(timer);
        } else {
            poolUntilBuildTriggers(init);
        }
    }
}

var addBuildLogHandlers = function(buildHistory, isForkData) {
    if(buildHistory && buildHistory.builds) {
        var builds = buildHistory.builds;
         for(var i=0; i<builds.length ; i++) {
            var buildInfo = builds[i];
            addBuildLogHandler("build-log-" + buildInfo.id, buildInfo.id, isForkData);
        }
    }
}

var addBuildLogHandler = function(elementId, buildId, isForkData) {
    $("#" + elementId).click(function(event) {
        jagg.post("../blocks/buildDeploy/list/ajax/list.jag", {
             action : "printBuildLogs",
             tenantDomain : tenantDomain,
             applicationKey : applicationKey,
             applicationVersion : currentVersion,
             lastBuildId : buildId,
             forkedRepository : isForkData
        }, function (result) {
            $('#build_logs').html(result);
        },
        function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0){}
        });
    });
}

var getBuildAndDeploymentInfo = function(isForkInfo) {
    jagg.post("../blocks/buildDeploy/list/ajax/list.jag", {
        action: "getBuildAndRepoDataForVersion",
        applicationKey:applicationKey,
        version:currentVersion,
        userName: userName
    }, function (result) {
        result = jQuery.parseJSON(result);
        if(result && result.length > 0) {
            // assume that only one object comes always
            var appInfo = result[0];
            var versionInfo = filterVersionInfo(appInfo.versions);

            addBuildNDeployListeners(versionInfo, isForkInfo);
            if(!isForkInfo || isForkInfo == "null") {
                drawDeployedStatus(versionInfo, isForkInfo);
            }

        }
    },
    function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while getting the build and deploy status data", type: 'error', id:'notification'});
    });
}

var addBuildNDeployListeners = function (versionInfo, isForkInfo) {
    if(versionInfo) {
        var repoFrom = "original";
        if(isForkInfo) {
            repoFrom = "fork";
        }
        if(!handlerAdded) {
            // add build button listener
            $("#buildBtn").click(function(event) {
                doBuild(applicationKey, " ", versionInfo.stage, " ", currentVersion, versionInfo.isAutoDeploy, repoFrom);
            });

            // add deploy button listener
            $("#deployBtn").click(function(event) {
                doDeploy(applicationKey, "deploy", versionInfo.stage, "", currentVersion);
            });
            handlerAdded = true;
        }
    }
}

var filterVersionInfo = function(versionData) {
    var result = null;
    if(versionData) {
        for(var key in versionData) {
            var versionInfo = versionData[key];
            if(versionInfo && versionInfo.version === currentVersion) {
                result = versionInfo;
                break;
            }
        }
    }
    return result;
}


var drawDeployedStatus = function(buildInfo, isForkInfo) {
    var message = "";
    if (buildInfo && buildInfo.deployedBuildId && buildInfo.stage) {
        message += "Build ";
        message += buildInfo.deployedBuildId;
        message += " deployed to ";
        message += buildInfo.stage;
        message += " stage";
    }
    $('#buildStatus').html(message);
}

var doBuild = function(applicationKey, revision, stage, tagName, version, autoDeploy, repoFrom) {
    jagg.post("../blocks/buildDeploy/add/ajax/add.jag", {
        action: "createArtifact",
        applicationKey: applicationKey,
        revision:revision,
        stage:stage,
        tagName:tagName,
        version:version,
        doDeploy:autoDeploy,
        repoFrom:repoFrom
    },function (result) {
        jagg.message({
            content: "The build has been triggered successfully",
            type: 'success',
            id:'message_id_success'
        });
        poolUntilBuildTriggers(init);
    },
    function (jqXHR, textStatus, errorThrown) {});
}


var doDeploy = function(applicationKey, deployAction, stage, tagName, version) {
    jagg.post("../blocks/buildDeploy/add/ajax/add.jag", {
                action: "deployArtifact",
                applicationKey: applicationKey,
                deployAction:deployAction,
                stage:stage,
                tagName:tagName,
                version:version
    }, function (result) {
        jagg.message({
            content: "Deployment has been submitted successfully - Refresh the page in few seconds.",
            type: 'success',
            id:'message_id_success'
        });
        refreshBuildStatus(getBuildAndDeploymentInfo, isForkInfo);
    },

    function (jqXHR, textStatus, errorThrown) {
        if (jqXHR.status != 0) {
            jagg.message({
                content: "Error occurred while deploying the artifact.",
                type: 'error',
                id:'message_id'
            });
        }
    });
}

var isJsonString = function(value) {
    try {
        JSON.parse(value);
    } catch (e) {
        return false;
    }
    return true;
}

var refreshBuildStatus = function(callback) {
    var timer = setTimeout(callback, 10000);
}

var getMaxBuildId = function(builds) {
    var maxId = 0;
    if(builds) {
        for(var i=0; i<builds.length; i++) {
            var build = builds[i];
            if(build) {
                var buildId = parseInt(build.id);
                if(buildId && (buildId > maxId)) {
                    maxId = buildId;
                }
            }
        }
    }
    return maxId;
}

var poolUntilBuildTriggers = function(callback) {
    clearTimeout(timer);
    timer = setTimeout(callback, 3000);
}