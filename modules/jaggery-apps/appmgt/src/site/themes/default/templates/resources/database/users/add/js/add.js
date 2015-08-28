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
var redirectPage = "editdatabase.jag";
var invalidUNPattern = "Invalid Username - Use maximum 7 characters.Use both letters and numbers, special characters (such as _).";
var usernamePattern = /^[a-zA-Z0-9_]{1,7}$/;
// page initialization
$(document).ready(function () {
    $("#add-user").prop("disabled", true);
    //add show /hide option on user passsword field
    $('input[type=password]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fa fa-eye"></i> </span>');
    $('.hide-pass').click(function () {
        if ($(this).find('i').hasClass("fa-eye-slash")) {
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass("fa-eye-slash");
        } else {
            $(this).find('i').addClass("fa-eye-slash");
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
        }

    });

    $('.password-generator').click(function () {
        $('#password-modal').modal('show');
    });


    "use strict";
    var options = {};
    options.ui = {
        showStatus: true,
        showErrors: true,
        showProgressBar: true,
        showVerdictsInsideProgressBar: true,
        viewports: {
            progress: ".strength-meter"
        },
        showPopover: true
    };

    $('#password').pwstrength(options);
    $('#password').on('show.bs.popover', function () {
        $.fn.popover.Constructor.DEFAULTS.placement = 'right';
    });

    $.validator.addMethod("validatePattern", validatePattern, invalidUNPattern);
    var addDbUserForm = $("#addDbUserForm");
    addDbUserForm.validate(getValidationOptions());     // adding form validation options

    addDbUserForm.on('focusout keyup blur', function () { // fires on every keyup & blur
        if ($('#username').val() && $('#password').val() && $('#password-confirm').val()) {// checks form for validity
            $("#add-user").prop("disabled", false);     // disables button
        } else {
            $("#add-user").prop("disabled", true);      // enables button
        }
    });

    addDbUserForm.ajaxForm({
        complete: function (result, status) { // on complete
            $("#add-user").prop("disabled", true);
            if (status == "error") {
                jagg.message({
                    content: "Error occurred while adding database user",
                    type: 'error',
                    id: 'myuniqeid'
                });
            } else {
                jagg.message({
                    content: "Successfully added user "+$('#username').val(),
                    type: 'success',
                    id: 'myuniqeid'
                });
                var redirectionPageParams = redirectPage+ "?applicationName=" + applicationName + "&applicationKey="
                                    +applicationKey + "&dbName=" + dbName+"&environment="+environment;
                window.location.replace(redirectionPageParams);
            }
        }
     });
});

function submitForm() {
    $("#add-user").prop("disabled", true);
    var addDbUserForm = $("#addDbUserForm");
    var validator = addDbUserForm.validate();
    var formValidated = validator.form();
    if (formValidated) {
        addDbUserForm.submit();
    }
}

function getValidationOptions(){
    return {
        rules: {
            "username": {
                required: true,
                validatePattern:true
            },
            password: {
                required: true
            },
            "password-confirm": {equalTo: '#password'}
        },
        messages: {
            "password-confirm": {
                equalTo: "The password and its confirm are not the same"
            }
        },
        onkeyup: function (event, validator) {
            return false;
        },
        showErrors: function (event, validator) {
            // Disable add user button if the form is not valid
            if (this.numberOfInvalids() > 0) {
                $("#add-user").prop("disabled", true);
            }
            this.defaultShowErrors();
        },
        errorPlacement: function (error, element) {
            if ($(element).hasClass("eye-icon")) {
                error.insertAfter($(element).parent().find('span.hide-pass'));
            } else {
                error.insertAfter(element);
            }
        }
    };
}

function validatePattern(userNameVal) {
    var validation = true;
    var username = userNameVal.trim();
    if (!username) {
        validation=false;
    }
    if (username.length > 0 && !usernamePattern.test(username)) {
        validation=false;
    }
    return validation;
}