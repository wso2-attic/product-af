// initialize the page when document is ready
$(document).ready(function() {
    init();
});

var init = function() {
//    getAppVersionInfo();
    getBuildAndDeploymentInfo();
    getBuildHistory(false);
    // add element listeners
//    addListeners();
}

var getBuildHistory = function (isFork) {
    jagg.post("../blocks/buildDeploy/list/ajax/list.jag", {
          action:"getBuildHistory",
          applicationKey: applicationKey,
          applicationVersion: currentVersion,
          forkedRepository: isFork,
          tenantDomain:tenantDomain
    },function (result) {
            var buildHistory = jQuery.parseJSON(result);
            drawBuildHistory(buildHistory);
            addBuildLogHandlers(buildHistory, isFork);
      },function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content:'Could not build history information', type:'error', id:'notification' });
            }
      });
}

var drawBuildHistory = function(buildHistory) {
    if(buildHistory && buildHistory.builds) {
        var builds = buildHistory.builds;
        for(var key in builds) {
            var buildInfo = builds[key];
            if(buildInfo) {
                var date = new Date(parseInt(buildInfo.timestamp));
                var message = "";
                message += "<tr>";
                message += "<td><span class='table-notification-msg";
                if("SUCCESS" === buildInfo.result) {
                    message += " table-noti-success";
                } else if("FAIL" === buildInfo.result) {
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
    }
}

var addBuildLogHandlers = function(buildHistory, isFork) {
    if(buildHistory && buildHistory.builds) {
        var builds = buildHistory.builds;
         for(var i=0; i<builds.length ; i++) {
            var buildInfo = builds[i];
            addBuildLogHandler("build-log-" + buildInfo.id, buildInfo.id, isFork);
        }
    }
}

var addBuildLogHandler = function(elementId, buildId, isFork) {
    $("#" + elementId).click(function(event) {
        jagg.post("../blocks/buildDeploy/list/ajax/list.jag", {
             action : "printBuildLogs",
             tenantDomain : tenantDomain,
             applicationKey : applicationKey,
             applicationVersion : currentVersion,
             lastBuildId : buildId,
             forkedRepository : isFork
        }, function (result) {
            $('#build_logs').html(result);
        },
        function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0){}
        });
    });
}

var getAppVersionInfo = function() {
    jagg.post("../blocks/application/get/ajax/list.jag", {
          action:"getAppVersionAllInfoByVersion",
          applicationKey: applicationKey,
          userName: userName,
          version: currentVersion
    },function (result) {
            var appInfo = jQuery.parseJSON(result);

      },function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content:'Could not load Application information', type:'error', id:'notification' });
            }
      });
}

function getBuildAndDeploymentInfo() {
    jagg.post("../blocks/buildDeploy/list/ajax/list.jag", {
        action: "getBuildAndRepoDataForVersion",
        applicationKey:applicationKey,
        version:currentVersion,
        userName: $("#userName").attr('value')
    }, function (result) {
        result = jQuery.parseJSON(result);
        if(result && result.length > 0) {
            // assume that only one object comes always
            var appInfo = result[0];
            var versionInfo = filterVersionInfo(appInfo.versions);

            addBuildNDeployListeners(versionInfo);
            drawDeployedStatus(versionInfo);
        }
    },
    function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: "Error occurred while getting the build and deploy status data", type: 'error', id:'notification'});
    });
}

var addBuildNDeployListeners = function (versionInfo) {
    if(versionInfo) {
        // add build button listener
        $("#buildBtn").click(function(event) {
            doBuild(applicationKey, " ", versionInfo.stage, " ", currentVersion, versionInfo.isAutoDeploy, "original");
        });

        // add deploy button listener
        $("#deployBtn").click(function(event) {
            doDeploy(applicationKey, "deploy", versionInfo.stage, "", currentVersion);
        });
    }
}

function filterVersionInfo(versionData) {
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


function drawDeployedStatus(buildInfo) {
    var message = "";
    if (buildInfo) {
        message += "Build ";
        message += buildInfo.deployedBuildId;
        message += " deployed to ";
        message += buildInfo.stage;
        message += " stage";
    }
    $('#buildStatus').html(message);
}

function doBuild(applicationKey, revision, stage, tagName, version, autoDeploy, repoFrom) {
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
    },
    function (jqXHR, textStatus, errorThrown) {});
}


function doDeploy(applicationKey, deployAction, stage, tagName, version) {
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