/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

function drawTabLinks() {
    jagg.post("../blocks/tablinks/ajax/tablinks.jag", {
        action:"getAllowedTabLinks",
        applicationKey:applicationKey
    }, function (result) {
        var jsonResult = JSON.parse(result);
        $(".left-pane").html(constructTabLinksHTML(jsonResult));
    }, function (jqXHR, textStatus, errorThrown) {
        if (jqXHR.status != 0) {
            jagg.message({
                content:"Error while getting tab links.",
                type:'error'
            });
        }
    });
}
/*
* Permissions result is similar to below.
"isOverviewAllowed":isOverviewAllowed,
 "isTeamAllowed":isTeamAllowed,
 "isReposAndBuildsAllowed":isReposAndBuildsAllowed,
 "isResourcesAllowed":isResourcesAllowed,
 "isIssuesAllowed":isIssuesAllowed,
 "isLifecylceAllowed":isLifecylceAllowed,
 "isLogsAllowed":isLogsAllowed,
 "isAllowDomainMapping":isAllowDomainMapping,
 "isSettingsAllowed":isSettingsAllowed,
 "isForked":isForked
* */
function constructTabLinksHTML(permissions) {
    <!-- Below HTML content is duplicated in site/themes/default/templates/tablinks/template.jag page too because we need to redraw the tablinks using ajax in some cases.-->
    var content="<ul>";
    if (permissions.isOverviewAllowed) {
        content += "<li class='selected'>" + "                <a href="
            + "                '/appmgt/site/pages/application.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_appHome'><i class='fa fa-laptop'></i> Overview</a>"
            + "            </li>";
    }
    if (permissions.isTeamAllowed) {
        content += "<li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/application-team.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_userAdmin'><i class='fa fa-users'></i> Team</a>"
            + "            </li>";
    }
    if (permissions.isReposAndBuildsAllowed && !permissions.isUploadableAppType) {
        content += "            <li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/repository.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_buildRepo'><i class='fa fa-cogs'></i> Repository</a>"
            + "            </li>";
    }

    if (permissions.isReposAndBuildsAllowed || permissions.isUploadableAppType) {
        content += "<li class=''>";
        if(permissions.isUploadableAppType){
            content += "<a href='/appmgt/site/pages/uploadedVersions.jag?applicationName=" + applicationName + "&amp;applicationKey=" + applicationKey + "'>" +
                        "<i class='fw fw-deploy'></i> Deployments </a>";
        } else {
            content += "                <a href="
                + "                '/appmgt/site/pages/buildDeploy.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
                + "                id='menu_buildDeploy'><i class='fw fw-deploy'></i> Build &amp; Deploy</a>";
            if(permissions.isForked){
                content += "                <ul>" + "                    <li>" + "                        <a href="
                    + "                        '/appmgt/site/pages/buildDeploy.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"&amp;isForkInfo=true'"
                    + "                        id='menu_buildDeploy_fork'><i class='fa fa-code-fork'></i> My Fork</a>"
                    + "                    </li>" + "                </ul>";
            }
        }

    }

    // add database link
    content +=  "            <li class=''>" + "                <a href="
        + "                '/appmgt/site/pages/databases.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
        + "                id='menu_db'><i class='fa fa-database'></i> Databases</a>"
        + "            </li>";

    // runtime configs
    if(permissions.isResourcesAllowed){
        content += "            <li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/configureenvironment.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_dbAdmin'><i class='fa fa-wrench'></i> Runtime Configs</a>"
            + "            </li>";
    }
    if(permissions.isIssuesAllowed){
        content += "            <li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/issuetracker.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_trackIssues'><i class='fa fa-tags'></i> Issues</a>"
            + "            </li>";
    }
    if(permissions.isLifecylceAllowed && !permissions.isUploadableAppType){
        content +=  "            <li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/governance.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_governance'><i class='fw fw-lifecycle'></i> Lifecycle"
            + "                Management</a>" + "            </li>";
    }
    // runtime logs
    content +=  "            <li class=''>"
        + "                <a href="
        + "                '/appmgt/site/pages/server.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
        + "                id='menu_server'><i class='fa fa-hdd-o'></i> Runtime Logs</a>"
        + "            </li>";
    if(permissions.isAllowDomainMapping){
        content +=  "            <li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/customurl.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_server'><i class='fa fa-link'></i> Custom URL</a>"
            + "            </li>";
    }
    if(permissions.isSettingsAllowed){
        content +=  "            <li class=''>" + "                <a href="
            + "                '/appmgt/site/pages/settings.jag?applicationName="+applicationName+"&amp;applicationKey="+applicationKey+"'"
            + "                id='menu_server'><i class='fw fw-settings'></i> Settings</a>"
            + "            </li>";
    }
    content+=" </ul>";
    return content;
}