$(document).ready(function() {
    // initialize page and handlers
    initPage();
    // load initial data to the page
    loadAppInfo();
    loadAppVersionInfo();
});

// wrapping functions goes here
function initPage() {
    loadAppIcon(applicationKey);
    addSidePaneClickHandlers();
}

// load common application info
function loadAppInfo () {
    loadTeamInfo();
    loadDatabaseInfo();
}

// load version specific application data
function loadAppVersionInfo() {
    // since this is the initial data loading, show data for trunk version
    loadAppInfoFromServer(applicationKey, "trunk");
}


// initialization function goes here
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
        }
        else {
            $(".app-icon").attr('src', iconUrl);
        }
    },
    function (jqXHR, textStatus, errorThrown) {
        // does nothing
    });
}

function addSidePaneClickHandlers() {
    $('.side-pane-trigger').click(function() {
        var rightPane = $('.right-pane');
        var leftPane = $('.left-pane');
        if (rightPane.hasClass('visible')){
            rightPane.animate({"left":"0em"}, "slow").removeClass('visible');
            leftPane.animate({"left":"-18em"}, "slow");
            $(this).find('i').removeClass('fa-arrow-left').addClass('fa-reorder');
        } else {
            rightPane.animate({"left":"18em"}, "slow").addClass('visible');
            leftPane.animate({"left":"0em"}, "slow");
            $(this).find('i').removeClass('fa-reorder').addClass('fa-arrow-left');
        }
    });

    $('.notification-pane-trigger').click(function() {
        var notificationPane = $('.notification-pane');
        if(notificationPane.hasClass('visible')) {
            notificationPane.animate({"right":"0em"}, "slow").removeClass('visible');
        } else {
            notificationPane.animate({"right":"-24em"}, "slow").addClass('visible');
        }
    });
}



// static data loading functions goes here
function loadTeamInfo() {
    // TODO
}

function loadDatabaseInfo() {
    // TODO

}


function loadAppInfoFromServer(applicationKey, version) {
    jagg.post("../blocks/application/get/ajax/list.jag", {
          action:"getAppVersionsInStages",
          applicationKey: applicationKey,
          userName: $("#userName").attr('value')
    },function (result) {
            var resultData = jQuery.parseJSON(result);
            if (resultData.length > 0) {
                // get the relevant application info object
                // since this always gives only one element array, take the first element
                var appInfo = resultData[0];
                var appVersionInfo = filterAppVersionInfo(appInfo, version);
                loadLaunchAppInfo(appVersionInfo);
                loadLifeCycleManagementInfo(appVersionInfo);
                loadReposAndBuildsInfo(appVersionInfo);
                loadIssuesInfo(version);
            }
      },function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content:'Could not load Application information', type:'error', id:'notification' });
            }
      });
}

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

// app version specific functions goes here
function loadLaunchAppInfo(appVersionInfo) {
    // TODO

}

function loadLifeCycleManagementInfo(appVersionInfo) {
    if(appVersionInfo) {
        var stage = appVersionInfo.appStage;
        $("#appStage").html(stage);
    }
}

function loadReposAndBuildsInfo(appVersionInfo) {
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
        $("#lifecycle-mgt-main").html("Application <strong>" + applicationName + "</strong> is in <br> <strong>" + versionStage + "</strong> Stage");
        $("#success-and-fail-ids").html("Build " + buildNumber + " " + buildStatus);
    }
}

function loadIssuesInfo(version) {
    // TODO
}