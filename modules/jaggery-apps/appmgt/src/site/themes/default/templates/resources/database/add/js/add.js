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

$( document ).ready(function() {
//select 2
$('select').select2();

//compare password and confirm password
    $("#password-confirm").focusout(function(){
        if($('#password').val().trim()!= $('#password-confirm').val().trim()) {
            jagg.message({content:'Password and confirm password fields does not match' , type:'error'});
        }
    });


//add show /hide option on user passsword field
    $('input[type=password]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fa fa-eye"></i> </span>');
    var highPass = $('.hide-pass');
    $('.hide-pass').click(function(){
        if($(this).find('i').hasClass("fa-eye-slash")){
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass( "fa-eye-slash" );
        }else{
            $(this).find('i').addClass( "fa-eye-slash" );
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

});

// add new database
function addNewDatabase(){
    if(validateFileds()) {
        jagg.post("../blocks/resources/database/add/ajax/add.jag", {
            action: "createDatabaseAndAttachUser",
            applicationKey: appKey,
            databaseName:$("#database-name").val().trim(),
            databaseServerInstanceName:$("#stage option:selected").val(),
            isBasic:true,
            customPassword:$('#password').val().trim(),
            userName:$("#user-name").val().trim(),
            templateName:null,
            copyToAll:false,
            createDatasource: false,
            databaseDescription:$("#description").val().trim()
        }, function (result) {
            result = $.trim(result);
            if(result=='success'){
                window.location.href="databases.jag?applicationName="+ appName +"&applicationKey=" + appKey;
            } else {
                jagg.message({content:'Error occured while creating database!' , type:'error', id:'databasecreation'});
            }
        },function (jqXHR, textStatus, errorThrown) {
            jagg.message({content:'Error occured while creating database!' , type:'error', id:'databasecreation'});
        });
    }
}

// validate user inputs in add new database fields
function validateFileds() {
    if(!$("#database-name").val().trim()) {
        jagg.message({content:'Database name field cannot be empty',type:'error'});
        return false;
    } else if(!$("#user-name").val().trim()) {
        jagg.message({content:'Default user name field cannot be empty',type:'error'});
        return false;
    } else if(!$('#password').val().trim()) {
        jagg.message({content:'Password field cannot be empty' , type:'error'});
        return false;
    } else if($('#password').val().trim()!= $('#password-confirm').val().trim()) {
            jagg.message({content:'Password and confirm password fields does not match' , type:'error'});
            return false;
    } else {
        return true;
    }
}
