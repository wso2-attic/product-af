$( document ).ready(function() {
//select 2
$('select').select2();


//add show /hide option on user passsword field
    $('input[type=password]').after('<span class="hide-pass" title="Show/Hide Password"><i class="fa fa-eye"></i> </span>');
    $('.hide-pass').click(function(){
        if($(this).find('i').hasClass("fa-eye-slash")){
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
            $(this).find('i').removeClass( "fa-eye-slash" );
        }else{
            $(this).find('i').addClass( "fa-eye-slash" );
            $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
        }
    });
    $('.password-generator').click(function(){
        $('#password-modal').modal('show');
    });
    "use strict";
    var options = {};
    options.ui = {
        showStatus: true,
        showErrors: true,
        showProgressBar: true,
        showVerdictsInsideProgressBar: true,
        viewports:{
          progress: ".strength-meter"
        },
        showPopover: true,
    };
    $('#password').pwstrength(options);
    $('#password').on('show.bs.popover', function () {
        $.fn.popover.Constructor.DEFAULTS.placement = 'right';
    });

});

// add new database
function addNewDatabase(){
    if(validateAddNewDatabaseFileds()) {
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
function validateAddNewDatabaseFileds() {
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
