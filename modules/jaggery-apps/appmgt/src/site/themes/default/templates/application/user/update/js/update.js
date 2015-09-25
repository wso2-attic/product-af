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
 * /
 */
var organizationType = 'AF';
var orgIdentifier = organizationType+"_";
var redirectPage = "application-team.jag";

$(document).ready(function () {
    $.fn.select2.amd.require(['select2/selection/search'], function (Search) {
        var oldRemoveChoice = Search.prototype.searchRemoveChoice;
        var roleElement = $('.role');

        Search.prototype.searchRemoveChoice = function () {
            oldRemoveChoice.apply(this, arguments);
            this.$search.val('');
        };

        //Roles select2 component initialization
        roleElement.select2({
            data : getFormattedAllRoleList(allRoleList),
            placeholder:'Select Role',
            templateSelection : roleResult,
            escapeMarkup:function(m){
                return m;
            }
        });

        //Pre Select values for already assigned roles
        setCurrentRoles(currentUser.currentRoles, roleElement);
    });

    $('#update-roles').click(function () {
        var updatedRoles = getUpdatedRoles();
        updateUserRole(currentUser.userName, updatedRoles.newRoles, updatedRoles.deletedRoles);
    });

    $('#role').change(function () {
        var updatedRoles = getUpdatedRoles();
        if (updatedRoles.deletedRoles.length || updatedRoles.newRoles.length) {
            $('#update-roles').prop("disabled", false); // Disable update-roles btn if roles are not changed
        } else {
            $('#update-roles').prop("disabled", true);
        }
    });
});

/**
 * Formats all role list as required for the select2 lib
 * @param allRolesList
 * @returns {*}
 * ex:
 * [{
 *    id : 'AF-Admin',
 *    roleDisplayName : 'Admin',
 *    orgType : 'AF',
 *    text : 'Admin'
 *},
 *{
 *    id : 'AF-User',
 *    roleDisplayName : 'User',
 *    orgType : 'AF',
 *    text : 'User'
 *},
 *{
 *    id : 'AF-App-Owner',
 *    roleDisplayName : 'App Owner',
 *    orgType : 'AF',
 *    text : 'App Owner'
 *}]
 */
function getFormattedAllRoleList(allRolesList) {
    return [{
        id : 'AF-Admin',
        text : organizationType,
        children: $.map(allRolesList, function (role) {
            return {
                id: getFormattedIdentifier(role["role"]),
                role: role["role"],
                roleDisplayName: role["displayName"],
                orgType: organizationType,
                text: role["displayName"]
            };
        })
    }]
}

/**
 * get identifier to select2 element by role id
 * @param roleId
 * @returns {string}
 */
function getFormattedIdentifier(roleId){
    return orgIdentifier + roleId;
}


//Roles template generator
function roleResult (result) {
    if(result.loading != undefined && result.loading){
        return result.text;
    }
    return "<div class='role_type' " + result.orgType + "><span class='role_type_tag'>"  + result.roleDisplayName +  "</span></div>"
}

function setCurrentRoles(currentRoleList, roleElement){
    var formattedCurrentRoles = $.map(currentRoleList, function (role) {
        return getFormattedIdentifier(role);
    });
    roleElement.val(formattedCurrentRoles).trigger("change");

}

/**
 * Return removed and new roles of the current user
 *
 * @returns {{deletedRoles: (Array), newRoles: (Array)}}
 * ex: {
 *      deletedRoles: ["qa"],
 *      newRoles: ["owner","devops"]
 * }
 */
function getUpdatedRoles() {
    var roles = $('#role').select2('data');
    var selectedRoles = $.map(roles, function (role) {
        return role.role;
    });
    return {
        deletedRoles: $(currentUser.currentRoles).not(selectedRoles).get(),
        newRoles: $(selectedRoles).not(currentUser.currentRoles).get()
    }
}

function updateUserRole(userName, rolesToAdd, rolesToDelete){
    jagg.post("../blocks/tenant/users/update/ajax/update.jag", {
        action:"updateUserRoles",
        userName: userName,
        rolesToAdd:JSON.stringify(rolesToAdd),
        rolesToDelete: JSON.stringify(rolesToDelete)
    }, function () {
        $('#update-roles').loadingButton({action : "hide"});
        jagg.message({
            content:'Successfully updated the user roles',
            type: 'success'
        });
        var redirectionPageParams = redirectPage+ "?applicationName=" + applicationName
                                    + "&applicationKey=" +applicationKey;
        window.location.replace(redirectionPageParams);
    }, function (jqXHR) {
        $('#update-roles').loadingButton({action : "hide"});
        jagg.message({
            content:jqXHR.responseText,
            type: 'error'
        });
    });

}

