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
    var highPass = $('.hide-pass');
    $('.hide-pass').click(function () {
        if ($(this).find('i').hasClass("fa-eye-slash")) {
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass("fa-eye-slash");
        } else {
            $(this).find('i').addClass("fa-eye-slash");
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
        }

    });
    
    //password strength meter logic
    $("#password").on("focus keyup", function () {
        var score = 0;
        var a = $(this).val();
        var desc = new Array();
        // strength desc
        desc[0] = "Too short";
        desc[1] = "Weak";
        desc[2] = "Good";
        desc[3] = "Strong";
        desc[4] = "Best";
        // password length
        var valid = '<i class="fa fa-check"></i>';
        var invalid = '<i class="fa fa-times"></i>';
        if (a.length >= 6) {
            $("#length").removeClass("invalid").addClass("valid");
            $("#length .status_icon").html(valid);
            score++;
        } else {
            $("#length").removeClass("valid").addClass("invalid");
            $("#length .status_icon").html(invalid);
        }
        // at least 1 digit in password
        if (a.match(/\d/)) {
            $("#pnum").removeClass("invalid").addClass("valid");
            $("#pnum .status_icon").html(valid);
            score++;
        } else {
            $("#pnum").removeClass("valid").addClass("invalid");
            $("#pnum .status_icon").html(invalid);
        }
        // at least 1 capital & lower letter in password
        if (a.match(/[A-Z]/) && a.match(/[a-z]/)) {
            $("#capital").removeClass("invalid").addClass("valid");
            $("#capital .status_icon").html(valid);
            score++;
        } else {
            $("#capital").removeClass("valid").addClass("invalid");
            $("#capital .status_icon").html(invalid);
        }
        // at least 1 special character in password {
        if ( a.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/) ) {
                $("#spchar").removeClass("invalid").addClass("valid");
                $("#spchar .status_icon").html(valid);
                score++;
        } else {
                $("#spchar").removeClass("valid").addClass("invalid");
                $("#spchar .status_icon").html(invalid);
        }
        if(a.length > 0) {
                //show strength text
                $("#passwordDescription").text(desc[score]);
                // show indicator
                $("#passwordStrength").removeClass().addClass("strength"+score);
        } else {
                $("#passwordDescription").text("Password not entered");
                $("#passwordStrength").removeClass().addClass("strength"+score);
        }
    });
    $("#password").popover({ title: 'Password strength meter', html:true, content: $("#password_strength_wrap").html(), placement: 'top', trigger:'focus keypress' });
    $("#password").blur(function () {
        $(".password_strength_meter .popover").popover("hide");
    });
    //password generator
    $('.password-generator').pGenerator({
        'bind': 'click',
        'passwordElement': '#password',
        'displayElement': '#password-confirm',
        'passwordLength': 10,
        'uppercase': true,
        'lowercase': true,
        'numbers':   true,
        'specialChars': true,
        'onPasswordGenerated': function(generatedPassword) {
            generatedPassword = 'Your password has been generated : ' +generatedPassword;
            $(".password-generator").attr('data-original-title', generatedPassword)
              .tooltip('show',{ placement: 'right'});
            $( "#password" ).trigger('focus');
            if(!$(highPass).find('i').hasClass("fa-eye-slash")){
                $(highPass).click();
            }
        }
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
                    content: result.responseText,
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
    var addDbUserForm = $("#addDbUserForm");
    var validator = addDbUserForm.validate();
    var formValidated = validator.form();
    if (formValidated) {
        $("#add-user").loadingButton('show');
        addDbUserForm.submit();
        $("#add-user").loadingButton('hide');
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
        onsubmit: false,    // Since we are handling on submit validation on click event of the "Create" button,
                            // here we disabled the form validation on submit
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
