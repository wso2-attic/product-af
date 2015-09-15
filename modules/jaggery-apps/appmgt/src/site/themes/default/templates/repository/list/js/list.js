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

$("#copytoClipboardMaster").on("click",function(){
        $(this).copyToClipboard("#masterRepo");
});

$("#copytoClipboardForked").on("click",function(){
        $(this).copyToClipboard("#forkedRepo");
});

$("#fork").on("click",function(){
    $(this).loadingButton('show');   
    jagg.post("../blocks/reposBuilds/set/ajax/set.jag", {
            action:"createFork",
            applicationKey:applicationKey,
            userNameArray:userName,
            type:"git",
            version:"trunk"
        }, function (result) {
            jagg.message({content: "Application forked successfully.", type: 'success', id:'notification'});
            getForkedRepoData();
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
* Show/hide fork button and forked repo div according to forked repo data.
* Forked repo URL is set here.
*/
function drawForkedRepo(data) {
    var dataJson = JSON.parse(data);
    if(!jQuery.isEmptyObject(dataJson)) {
        $('#fork').hide();
        $('#forkedRepoDiv').show();
        $('#forkedRepo').val(dataJson.trunk.version.repoURL);
    }else {
        $('#fork').show();
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
