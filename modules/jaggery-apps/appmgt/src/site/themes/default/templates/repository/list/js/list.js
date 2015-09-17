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

$(document).ready(function() {
    getForkedRepoData();

if( 3 === getBrowserId()) {
    $("#copytoClipboardMaster").on("click",function(){
        $(this).copyToClipboard("#masterRepo");
    });
    $("#copytoClipboardForked").on("click",function(){
        $(this).copyToClipboard("#forkedRepo");
    }); 
}else {
    $("#copytoClipboardMaster").remove();
    $("#masterRepo").focus(function(){ $(this).select(); }).mouseup(function (e) {e.preventDefault(); });
    $("#copytoClipboardForked").remove();
    $("#forkedRepo").focus(function(){ $(this).select(); }).mouseup(function (e) {e.preventDefault(); }); 
}

$(".check-your-code").on("click",function(){
    var url = gitBaseUrl + "summary/?=" + tenantDomain + "/" + applicationKey + ".git";
    var win = window.open(url, '_blank');
    win.focus();
});

$("#fork").on("click",function(){
    $('.btn-fork-code').loadingButton({
        action: "show",
        type: "small"
    });
    jagg.post("../blocks/reposBuilds/set/ajax/set.jag", {
            action:"createFork",
            applicationKey:applicationKey,
            userNameArray:userName,
            type:"git",
            version:"trunk"
        }, function (result) {
            jagg.message({content: "Application forked successfully.", type: 'success', id:'notification'});
            getForkedRepoData();
            drawTabLinks();
        }, function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({
                    content:"Error while forking repository.",
                    type:'error'
                });
            }
    });
});


    /**
* Identify the browser
*/
function getBrowserId () {
        var aKeys = ["MSIE", "Firefox", "Safari", "Chrome", "Opera"],
            sUsrAg = navigator.userAgent, nIdx = aKeys.length - 1;
        for (nIdx; nIdx > -1 && sUsrAg.indexOf(aKeys[nIdx]) === -1; nIdx--);
        return nIdx
}
    
/**
* Show/hide fork button and forked repo div according to forked repo data.
* Forked repo URL is set here.
*/
function drawForkedRepo(data) {
    var dataJson = JSON.parse(data);
    if(!jQuery.isEmptyObject(dataJson)) {
        $('#fork').hide();
        $('#arrow-down').show();
        $('#forkedRepoDiv').show();
        $('#forkedRepo').val(dataJson.trunk.version.repoURL);
    }else {
        $('#fork').show();
        $('#arrow-down').hide();
        $('#forkedRepoDiv').hide();
    }    
}    
    
/**
* Getting forked repository data, if theres no forked repo
* empty object is received as result
* Received data will be passed to drawForkedRepo()
*/
function getForkedRepoData() {
        jagg.post("../blocks/reposBuilds/list/ajax/list.jag", {
            action: "getbuildandrepodataforkedrepo",
            buildableforstage:"true",
            metaDataNeed:"false",
            applicationKey: applicationKey,
            userName:userName,
            isRoleBasedPermissionAllowed:"false"
        }, function (result) {
            drawForkedRepo(result);
        }, function (jqXHR, textStatus, errorThrown) {
              jagg.message({content: "Error occurred while getting forked repo data.", type: 'error', id:'notification'});
        });
}

});
