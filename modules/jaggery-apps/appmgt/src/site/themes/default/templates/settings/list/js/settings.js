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
    var generalForm = $("#generalForm");
    if(isUpdateInfoAllowed && generalForm){
        $("#updateGeneralInfo").prop("disabled", true);
        $.validator.addMethod("validateInputForAlphanumericAndSpaces", validateInputForAlphanumericAndSpaces);
        generalForm.validate(getGeneralFormValidationOptions());    // adding form validation options
        $("#description").on('focusout keyup blur', function () {   // fires on every keyup & blur
            var validator = generalForm.validate();
            validator.form();
        });

        generalForm.ajaxForm({
            complete: function (result, status) { // on complete
                if (status == "error") {
                    jagg.message({
                        content: result.responseText,
                        type: 'error'
                    });
                } else {
                    jagg.message({
                        content: "Successfully updated the application information",
                        type: 'success'
                    });
                    populateGeneralForm();
                }
            }
        });
    }
});

/**
 * Code snippet form UX team
 */
function formatState (state) {
    if (!state.id) { return state.text; }
    var $state;
    if(state.element.attributes['data-icon']){
        var $state = $(
                '<span><i class="fa '+ state.element.attributes['data-icon'].value.toLowerCase() +'"></i>&nbsp;&nbsp;'
                + state.text + '</span>'
        );
    }else{
        var $state = $(
                '<span><i class="fa '+state.id.toLowerCase() +'"></i>&nbsp;&nbsp;'
                + state.text + '</span>'
        );
    }

    return $state;

}

function populateGeneralForm(){
    //TODO: Once UX is finalized implement this
}

function validateInputForAlphanumericAndSpaces(inputValue){
    return !/[^a-zA-Z0-9\-\.,'"":;_+=(){}\[\] /]/.test(inputValue);
}

function submitGeneralForm() {
    if (isUpdateInfoAllowed) {
        $("#updateGeneralInfo").prop("disabled", true);
        var generalForm = $("#generalForm");
        var validator = generalForm.validate();
        var formValidated = validator.form();
        if (formValidated) {
            generalForm.submit();
        }
    } else {
        jagg.message({
            content:"You don't have permissions to update the application information",
            type:'error'
        });
    }
}

function deleteApplication(){
    if(isDeleteAllowed){
        jagg.message({
            content: "Application deletion started",
            type: 'success'
        });
        $("#deleteApplication").prop("disabled", true);
        jagg.post("../blocks/application/delete/ajax/delete.jag", {
            appKey:applicationKey
        }, function (result) {
            jagg.message({
                content:'Successfully deleted the application',
                type:'success'
            });
            window.location.replace("index.jag");
        },function (jqXHR, textStatus, errorThrown) {
            jagg.message({
                content:'Error occurred while deleting application',
                type:'error'
            });
            window.location.replace("index.jag");
        });
    } else {
        jagg.message({
            content:"You don't have permissions to delete the application",
            type:'error'
        });
    }
}

function clearUploadIconField() {
    document.getElementById("icon").value = "";
}

/**
 * Validation options for general form
 */
function getGeneralFormValidationOptions(){
    return {
        rules: {
            "appIcon": {
                require_from_group: [1, '.general-group']
            },
            description: {
                require_from_group: [1, '.general-group'],
                maxlength: 1000,
                validateInputForAlphanumericAndSpaces: true
            }
        },
        messages: {
            "description": {
                validateInputForAlphanumericAndSpaces: "Invalid input for description field!. Only alphanumeric and spaces are allowed."
            }
        },
        onsubmit: false,    // Since we are handling on submit validation on click event of the "Update" button,
                            // here we disabled the form validation on submit
        showErrors: function (event, validator) {
            // Disable update button if the form is not valid
            if (this.numberOfInvalids() > 0) {
                $("#updateGeneralInfo").prop("disabled", true);
            } else {
                $("#updateGeneralInfo").prop("disabled", false);
            }
            this.defaultShowErrors();
        },
        errorPlacement: function (error, element) {
            if ($(element).is($("#appIcon"))) {
                error.insertAfter($(element).parent().closest('div'));
            } else {
                error.insertAfter(element);
            }
        }
    };
}
