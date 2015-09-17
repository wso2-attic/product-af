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
*/


$(document).ready(function() {
var usersDatatable = {};
populateDatatable();

// select all check boxes for data and structure permission sections
$('.data-select-all input[type="checkbox"]').on("click",function() {
    $(".datacbx").prop('checked', $(this).prop('checked'));
});
$('.structure-select-all input[type="checkbox"]').on("click",function() {
    $(".structurecbx").prop('checked', $(this).prop('checked'));
});

$('.datacbx').on("click",function() {
    var classL = $('.datacbx').length;
    var checkedL = $('.datacbx:checked').length;
    if(classL == checkedL) {
        $('.data-select-all input[type="checkbox"]').prop('checked', true);
    } else {
        $('.data-select-all input[type="checkbox"]').prop('checked', false);
    }
});
$('.structurecbx').on("click",function() {
    var classL = $('.structurecbx').length;
    var checkedL = $('.structurecbx:checked').length;
    if(classL == checkedL) {
        $('.structure-select-all input[type="checkbox"]').prop('checked', true);
    } else {
        $('.structure-select-all input[type="checkbox"]').prop('checked', false);
    }
});

}); // end of document ready


/**
* Populate data table for databtase users
*/
function populateDatatable() {
  usersDatatable = $('.datatable table').dataTable({
            responsive: true,
            "orderCellsTop": true,
            'ajax': {
                "type"   : "POST",
                "url"    : '../blocks/resources/database/add/ajax/add.jag',
                "data"   : function( d ) {
                d.action = "getDatabaseUsersForDataTable";
                d.applicationKey = applicationKey;
                d.environment = environment;
                d.dbName = dbName;
                }
            },
            "columns": [
                { "data": "username", "width": "10%"},
                { "data": "attachuser" , "width": "5%","sClass" : "dt-body-center"},
                { "data": "priviledges" , "width": "40%"},
                { "data": "editpriviledges", "orderable": false, "width": "3%","sClass" : "dt-body-center"  },
                { "data": "trash", "orderable": false, "width": "3%", "sClass" : "dt-body-center" }
            ],
            "fnDrawCallback": function( oSettings ) {
                // onclick event of checkbox switch
                $('.switch input[type=checkbox]').on("click",function(){
                    var isChecked = $(this).is(":checked");
                    var currentId = $(this).attr("id");
                    var currentElement = $("#"+ currentId);
                    //getting the user name from checkbox id, removing 'chkbx_'
                    var userName = currentId.substr(7);
                    $('#modal-title').html('Select User Privilege for User: <i>' + userName + '</i>');
                    if(isChecked){
                        //clear all checked boxes
                        $('#priviledges_modal input:checkbox').prop('checked', false);

                        $('#priviledges_modal').modal({
                            show: true,
                            keyboard: false,
                            backdrop: 'static'
                        });
                        $('#privilege_edit_cancel').on('click', function (e) {
                            currentElement.removeAttr("checked");
                            userName = "";
                        });
                        $('#privilege_edit_save').on('click', function (e) {
                            if(priviledgeCheckboxValidation()){
                            var permissions = JSON.stringify(getCheckedPriviledgesAsJson());
                            attachUserWithPermissions(userName, permissions);
                            $('#priviledges_modal').modal('hide');
                            } else {
                                 jagg.message({content:'Select at least one priviledge before attaching user' , type:'error', id:'userattach_checkbox_validation'});
                            }
                            userName = "";
                        });
                     }else {
                        detachUserAndDropTemplate(userName);
                        currentElement.removeAttr("checked");
                        $("#priviledges_"+ userName).html("");
                    }

                });
            // on click event of edit
            $('.edit_priviledge').on("click",function(){
                var currentId = $(this).attr("id");
                //getting the user name from  id, removing 'edit_'
                var userName = currentId.substr(5);
                $('#modal-title').html('Select User Privilege for User: <i>' + userName + '</i>');
                markExistingPriviledges(userName);
                $('#priviledges_modal').modal({
                            show: true,
                            keyboard: false,
                            backdrop: 'static'
                });
                $('#privilege_edit_save').on('click', function (e) {
                    if(priviledgeCheckboxValidation()) {
                        var permissions = JSON.stringify(getCheckedPriviledgesAsJson());
                        editUserPriviledges(userName, permissions);
                        $('#priviledges_modal').modal('hide'); 
                    } else {
                        jagg.message({content:'Select at least one priviledge before attaching user' , type:'error', id:'userattach_checkbox_validation'});
                    }
                    userName = "";
                });
                $('#privilege_edit_cancel').on('click', function (e) {
                    userName = "";
                });
            });
            // on click event of delete 
            $('.delete_user').on("click",function(){
                var currentId = $(this).attr("id");
                //getting the user name from  id, removing 'delete_'
                var userName = currentId.substr(7);
                jagg.popMessage({type:'confirm',title:'Delete User',content:'Are you sure you want to delete the user ' + userName + ' ?', okCallback:function(){deleteUser(userName);;}, cancelCallback:function(){}});
            });
        } // end of call back function
    }); // end of datatable
}


/**
* Detach a user and drop priviledge template of a user
*/
function detachUserAndDropTemplate(userName){
    // detach user from database
    jagg.post("../blocks/resources/database/add/ajax/add.jag", {
            action:"detachUser",
            applicationKey:applicationKey,
            databaseName:dbName,
            dbServerInstanceName:environment,
            username:userName
    }, function (result) {
        if(result) {
            jagg.message({content:'User ' + userName +' detached from database ' + dbName , type:'success', id:'userdetach'});
        }
    },function (jqXHR, textStatus, errorThrown) {
            jagg.message({content:'Error occured while detaching the user from database' , type:'error', id:'userdetach'});
    });
    
    // drop template
    jagg.post("../blocks/resources/database/templates/ajax/list.jag", {
            action:"dropTemplate",
            applicationKey:applicationKey,
            templateName:applicationKey + '_' + dbName + '_' + userName,
            environment:environment
    }, function (result) {
            jagg.message({content:'Privilegdes removed for User ' + userName +' of database ' + dbName , type:'success', id:'userdetach'});
    
    },function (jqXHR, textStatus, errorThrown) {
            jagg.message({content:'Error occured while detaching the user from database' , type:'error', id:'userdetach'});
    });
}

/**
* Attach a user to a database with selected permissions
*/
function attachUserWithPermissions(userName, permissionsJson) {
    if(userName && permissionsJson) {
        jagg.post("../blocks/resources/database/add/ajax/add.jag", {
            action:"attachUserWithPermissions",
            applicationKey:applicationKey,
            databaseName:dbName,
            dbServerInstanceName:environment,
            userName:userName,
            permissions:permissionsJson 
        },function (result) {
                if(result){
                    usersDatatable.api().ajax.reload();
                }
        },function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status != 0){
                    jagg.message({type:'error',content:'User ' + userName + ' attaching failed for database ' + dbName, id:'attach_user'});
                }
        });
    }
}

/**
* Create a json object of priviledges with its checked/unchecked value
*/
function getCheckedPriviledgesAsJson() {
    var priviledgeCheckboxes = document.getElementsByName('chkbx_priviledge');
    if(priviledgeCheckboxes.length == 0) {
        jagg.message({type:'error',content:'Please select one or more priviledges', id:'get_priviledges'});
        return ;
    }
    var allPriviledges = {};
    for (var i=0; i<priviledgeCheckboxes.length; i++) {
        var priviledgeId = priviledgeCheckboxes[i].id;
        if (priviledgeCheckboxes[i].checked) {
            allPriviledges[priviledgeId] = "Y";
        } else {
             allPriviledges[priviledgeId] = "N";
        }
    }
    return allPriviledges;
}

/**
* Check whether at least one priviledge is selected before attaching user
*/
function priviledgeCheckboxValidation() {
    var checkboxes=document.getElementsByName('chkbx_priviledge');
    var atLeastOneChecked=false;
    for(var i=0,l=checkboxes.length;i<l;i++)
    {
        if(checkboxes[i].checked)
        {
            atLeastOneChecked=true;
            break;
        }
    }
    return atLeastOneChecked;
}

/**
* Update priviledges of a user attached to a database
*/
function editUserPriviledges(userName, permissionsJson) {
        jagg.post("../blocks/resources/database/add/ajax/add.jag", {
            action:"editUserPermissions",
            applicationKey:applicationKey,
            databaseName:dbName,
            rssInstanceName:environment,
            userName:userName,
            permissions:permissionsJson 
        },function (result) {
            usersDatatable.api().ajax.reload();
        },function (jqXHR, textStatus, errorThrown) {
                if (jqXHR.status != 0){
                    jagg.message({type:'error',content:'User ' + userName + ' attaching failed for database ' + dbName, id:'attach_user'});
                }
        });
}

/**
 * Delete database user
*/
function deleteUser(userName) {
        jagg.post("../blocks/resources/database/users/list/ajax/list.jag", {
            action:"deleteUser",
            applicationKey:applicationKey,
            name:userName,
            rssInstanceName:environment
        },function (result) {
            usersDatatable.api().ajax.reload();
        },function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content:'Error occurred while deleting user: ' + userName + 'User already attached to a database.',type:'error', id:'dbusercreation' });
            }
        });
}
/**
* When edit priviledges is clicked existing priviledges should be shown in the modal.
*/
function markExistingPriviledges(userName) {
    //clear all checked boxes
    $('#priviledges_modal input:checkbox').prop('checked', false);

    var priviledgesString = $('#priviledges_' + userName).html();
    var priviledgeArray = priviledgesString.split(", ");
    for(var i in priviledgeArray) {
        var privText = priviledgeArray[i];
        $('#' + priviledgeEnum[privText]).prop('checked',true);
    }
}


/**
* Javascript enum for database priviledges
*/
var priviledgeEnum = Object.freeze({
    "SELECT" :  "selectPriv",
    "INSERT" : "insertPriv",
    "UPDATE" : "updatePriv",
    "DELETE" : "deletePriv",
    "CREATE" : "createPriv",
    "DROP" : "dropPriv",
    "GRANT" : "grantPriv",
    "REFERENCES" : "referencesPriv",
    "INDEX" : "indexPriv",
    "ALTER" : "alterPriv",
    "CREATE TEMP TABLE" : "createTmpTablePriv",
    "LOCK TABLES" : "lockTablesPriv",
    "CREATE VIEW" : "createViewPriv",
    "SHOW VIEW" : "showViewPriv",
    "CREATE ROUTINE" : "createRoutinePriv",
    "ALTER ROUTINE" : "alterRoutinePriv",
    "EXECUTE" : "executePriv",
    "EVENT" : "eventPriv",
    "TRIGGER" : "triggerPriv"

});



