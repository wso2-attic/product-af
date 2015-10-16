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

// page initialization
$(document).ready(function () {
    populateTableData();

    if(pendingUsers.length > 0){
        $('.click_to_expand').click(function(e){
            e.preventDefault();
            $('.invitaiton_list').slideToggle("fast");
        });
    }

    var teamListBody = $('#teamlist tbody');
    teamListBody.on( 'click', '.editroles', function () {
        var table = $('#teamlist').DataTable();
        var userData = table.row( $(this).parents('tr') ).data();
        editRoles(userData);
    });

    teamListBody.on( 'click', '.delete', function () {
        var table = $('#teamlist').DataTable();
        var data = table.row( $(this).parents('tr') ).data();
        jagg.popMessage({
            content:"Are you sure you want to remove the user '"+data.email+"'?",
            okCallback:function(){
                deleteUser(data.userName);
            }
            }
        );
    });
});

function populateTableData(){
    $('#teamlist').DataTable(getDatatabelOptions());
}

function resendInvitation(email){
    jagg.post("../blocks/application/user/invite/ajax/invite.jag", {
            action:"resendInvitation",
            applicationKey:applicationKey,
            email:email
        },
        function () {
            jagg.message({
                content:'Successfully resent the invitation to user '+email,
                type: 'success'
            });
        },
        function () {
            jagg.message({
                content:'Error occurred while resending the invitation to user '+email,
                type: 'error'
            });
        }
    );
}

function getDatatabelOptions(){

    var options = {
        responsive: true,
        "orderCellsTop": true,
        'ajax': {
            "type"   : "POST",
            "url"    : "../blocks/application/user/list/ajax/list.jag",
            "data"   : function( d ) {
                d.action = "getTeamMembersOfApplication";
                d.applicationKey = applicationKey;
            }
        },
        "columns": [
            { "data": "email", "width": "10%","visible": false},
            { "data": "name" , "width": "15%"},
            { "data": "displayRoles" , "width": "40%"},
            { "data": "userName"},
            { "data": "roles"}
        ],
        "columnDefs": [
            {
                "targets": 0,
                "render": function ( data ) {
                    return "<a>"+data+"</a>";
                }
            },
            {
                "targets": 2,
                "render": function ( data, type, row ) {
                    var roleTag = "";
                    $.each(row.displayRoles, function(i,e){
                        roleTag += "<div class='role_type'><span class='role_type_tag'>"+ e.roleType +"</span></div>";
                    });
                    return roleTag;
                }
            },
            { "targets": "hidden-username", "visible": false, "class": "hide-column-data"}, // hide hidden-username
            { "targets": "hidden-roles", "visible": false, "class": "hide-column-data"}    // hide hidden-roles
        ]
    };

    if(hasTenantLevelUserMgtPermission){
        var editColumn = [
            { "data": null, "orderable": false, "width": "5%", "sClass" : "dt-body-center" }
        ];
        var editColumnDef = [
            {
                "targets": "af-edit-roles",
                "data": null,
                //"defaultContent": '<a class="editroles"><i class="fa fa-edit"></i></a>',
                "render": function ( data, type, row ) {
                    var editElement = "";

                    if( row.roles.indexOf("admin") == -1){
                        editElement = '<a class="editroles"><i class="fa fa-edit"></i></a>'
                    }
                    return editElement;
                }
            }
        ];
        options.columns = options.columns.concat(editColumn.slice());
        options.columnDefs = options.columnDefs.concat(editColumnDef.slice());
    }

    if(hasInviteUserPermission){
        var deleteColumn = [
            { "data": null, "orderable": false, "width": "5%", "sClass" : "dt-body-center" }
        ];
        var deleteColumnDef = [
            { "targets": "af-delete-users","data": null,
                "render": function ( data, type, row ) {
                    var deleteElement = "";
                    var escapedUserName = row.userName.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
                    var tenantLessLoggedInUser = loggedInUser.split('@')[0];

                    // Enforcing following restrictions as per previous implementation
                    // 1.) Application Creator cannot be deleted
                    // 2.) User cannot delete him self from the application
                    if( (escapedUserName != appCreator) && (escapedUserName != tenantLessLoggedInUser)){
                        deleteElement = "<a class='delete' href='#'><i class='fa  fa-trash'></i></a>"
                    }
                    return deleteElement;
                }
            }
        ];
        options.columns = options.columns.concat(deleteColumn.slice());
        options.columnDefs = options.columnDefs.concat(deleteColumnDef.slice());
    }
    return options;

}

function reloadDataTable() {
    var table = $('#teamlist').DataTable();
    table.ajax.reload();
}

function deleteUser(userName){
    jagg.post("../blocks/application/user/update/ajax/update.jag", {
        action:"removeUserFromApplication",
        applicationKey:applicationKey,
        users:userName
    },
    function () {
        window.setTimeout(function () {
            jagg.message({
                content:'Successfully removed the user '+userName,
                type: 'success'
            });
            reloadDataTable();
        }, 300);
    },
    function () {
        jagg.message({
            content:'Error occurred while removing user '+userName,
            type: 'error'
        });
        reloadDataTable();
    });
}

function getCurrentAfRolesFromDisplayRoles(displayRoles){
    return $.map(displayRoles, function(displayRole) {
        return displayRole.role;
    });
}

function editRoles(userData){
    var simplifiedUserObj = {
        email : userData.email,
        name : userData.name,
        userName : userData.userName,
        currentRoles :getCurrentAfRolesFromDisplayRoles(userData.displayRoles)
    };
    var tempForm =
            $('<form action="' + editRolesUrl + '" method="post">' +
                "<input type='text' name='currentUser' id='currentUser' value='" + JSON.stringify(simplifiedUserObj) + "' />" +
            '</form>');
    $('body').append(tempForm);
    tempForm.submit();
}




