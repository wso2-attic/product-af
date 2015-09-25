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

$(document).ready(function () {
//select 2
    $('select').select2(); //select2 init for stages dropdown
    var $select = $('#user-name-select.select2')
            .select2({
                         placeholder: "Enter username or select existing",
                         multiple: true,
                         maximumInputLength: 5,
                         tags: true,
                         selectOnBlur: true,
                         createSearchChoice: function (term, data) {
                             if ($(data).filter(function () {
                                         return this.text.localeCompare(term) === 0;
                                     }).length === 0) {
                                 return {
                                     id: term,
                                     text: term
                                 };
                             }
                         },
                         createTag: function (tag) {
                             return {
                                 id: tag.term,
                                 text: tag.term,
                                 isNew: true
                             };
                         }
                     });
    $select.on("select2:select", function (e) {
        var l = $select.select2('data');
        if (e.params.data.isNew != undefined && e.params.data.isNew) {
            if (l.length > 1) {
                $("#user-name-select.select2 [value='" + e.params.data.id + "']").remove();
                $('#user-name-select.select2').trigger('change');
            }
        } else {
            if (l.length > 1) {
                $('#user-name-select.select2 option[value="' + e.params.data.id + '"]:selected').removeAttr('selected');
                $select.trigger('change');
            }
        }
    });
    $select.on("select2:close", function (e) {
        var values = $select.select2('data');
    });

    getExistingUsersForSelectedStage();

    $('#stage').on("select2:select", function (e) {
        getExistingUsersForSelectedStage();
    });

    /**
     * According to the selected stage, users available will be set and listed in users dropdown
     */
    function getExistingUsersForSelectedStage() {
        var dbUsersJsonArray = JSON.parse(dbUsers);
        var dbUsersInStage = [];
        for (var i in dbUsersJsonArray) {
            var user = dbUsersJsonArray[i];
            var id = 0;
            if (user.rssInstanceName == $('#stage').val()) {
                var exUser = {};
                exUser.text = user.name;
                exUser.existing = true;
                exUser.id = id;
                id++;
                dbUsersInStage.push(exUser);
            }
        }
        setExistingUsers(dbUsersInStage);
    }

    /**
     * Filling data to user-name-select dropdown
     * @param data = [{id:0, text: "Admin"}, {id:1, text: "Root"}, {id:2, text: "User"}];
     */
    function setExistingUsers(data) {
        $select.empty();
        $select.trigger('change');
        $select = $('#user-name-select.select2')
                .select2({
                             placeholder: "Enter username or select existing",
                             data: data,
                             multiple: true,
                             maximumInputLength: 5,
                             tags: true,
                             selectOnBlur: true,
                             createSearchChoice: function (term, data) {
                                 if ($(data).filter(function () {
                                             return this.text.localeCompare(term) === 0;
                                         }).length === 0) {
                                     return {
                                         id: term,
                                         text: term
                                     };
                                 }
                             },
                             createTag: function (tag) {
                                 return {
                                     id: tag.term,
                                     text: tag.term,
                                     isNew: true
                                 };
                             }
                         });
    }

//compare password and confirm password
    $("#password-confirm").on('focusout  blur', function () {
        if ($('#password').val().trim() != $('#password-confirm').val().trim()) {
            jagg.message({content: 'Password and confirm password fields does not match', type: 'error'});
        }
    });
    /*
     var dbNameLengthErrorShowed = false;
     var userNameLengthErrorShowed = false;
     $('.form-control').on('focusout keyup blur', function() {
     if ($('#database-name').val() && $('#user-name').val() && $('#password').val() && $('#password-confirm').val()) {
     $("#add-database").prop("disabled", false);
     } else {
     $("#add-database").prop("disabled", true);
     }
     if ($('#database-name').val().length > 4) {
     if (!dbNameLengthErrorShowed) {
     jagg.message({
     content: 'Maximum lenght of database name is 5 characters',
     type: 'warning'
     });
     dbNameLengthErrorShowed = true;
     }
     } else {
     dbNameLengthErrorShowed = false;
     }

     if ($('#user-name-select').val().length > 4) {
     if (!userNameLengthErrorShowed) {
     jagg.message({
     content: 'Maximum lenght of user name is 5 characters',
     type: 'warning'
     });
     userNameLengthErrorShowed = true;
     }
     } else {
     userNameLengthErrorShowed = false;
     }
     });
     */

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
        if (a.match(/.[!,@,#,$,%,^,&,*,?,_,~,-,(,)]/)) {
            $("#spchar").removeClass("invalid").addClass("valid");
            $("#spchar .status_icon").html(valid);
            score++;
        } else {
            $("#spchar").removeClass("valid").addClass("invalid");
            $("#spchar .status_icon").html(invalid);
        }
        if (a.length > 0) {
            //show strength text
            $("#passwordDescription").text(desc[score]);
            // show indicator
            $("#passwordStrength").removeClass().addClass("strength" + score);
        } else {
            $("#passwordDescription").text("Password not entered");
            $("#passwordStrength").removeClass().addClass("strength" + score);
        }
    });
    $("#password").popover({
                               title: 'Password strength meter',
                               html: true,
                               content: $("#password_strength_wrap").html(),
                               placement: 'top',
                               trigger: 'focus keypress'
                           });
    $("#password").blur(function () {
        $(".password_strength_meter .popover").popover("hide");
    });
    //password generator
    $('.password-generator')
            .pGenerator({
                            'bind': 'click',
                            'passwordElement': '#password',
                            'displayElement': '#password-confirm',
                            'passwordLength': 10,
                            'uppercase': true,
                            'lowercase': true,
                            'numbers': true,
                            'specialChars': true,
                            'onPasswordGenerated': function (generatedPassword) {
                                generatedPassword = 'Your password has been generated : ' + generatedPassword;
                                $(".password-generator").attr('data-original-title', generatedPassword)
                                        .tooltip('show', {placement: 'right'});
                                $("#password").trigger('focus');
                                if (!$(highPass).find('i').hasClass("fa-eye-slash")) {
                                    $(highPass).click();
                                }
                            }
                        });

});

/**
 *  Adding new database
 */
function addNewDatabase() {
    $("#add-database").loadingButton('show');
    var isBasic = false; // isBasic variable defines whether the attaching an existing user or a new user.
    if ($('#user-name-select').select2('data')[0].isNew) {
        isBasic = true; // attaching a new user
    }
    jagg.post("../blocks/resources/database/add/ajax/add.jag", {
        action: "createDatabaseAndAttachUser",
        applicationKey: appKey,
        databaseName: $("#database-name").val().trim(),
        databaseServerInstanceName: $("#stage option:selected").val(),
        isBasic: isBasic,
        customPassword: $('#password').val().trim(),
        userName: $('#user-name-select').select2('data')[0].text,
        templateName: null,
        copyToAll: false,
        createDatasource: false,
        databaseDescription: $("#description").val().trim()
    }, function (result) {
        result = $.trim(result);
        if (result == 'success') {
            window.location.href = "databases.jag?applicationName=" + appName + "&applicationKey=" + appKey;
        } else {
            jagg.message({content: 'Error occured while creating database!', type: 'error', id: 'databasecreation'});
        }
    }, function (jqXHR, textStatus, errorThrown) {
        jagg.message({content: 'Error occured while creating database!', type: 'error', id: 'databasecreation'});
    });
    $("#add-database").loadingButton('hide');
}
