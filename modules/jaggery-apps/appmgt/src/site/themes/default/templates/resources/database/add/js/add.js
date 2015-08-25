//select 2
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
};

//$('.select2').select2({
//        templateResult: formatState,
//        templateSelection :formatState
//});


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

// add new database 
function addNewDatabase(){
    var dbName = $("#database-name").val().trim();
    var defaultUserName = $("#user-name").val().trim();
    if(!dbName){
        jagg.message({content:'Database name field cannot be empty',type:'error'});
        return;
    }
    if(!defaultUserName){
        jagg.message({content:'Default user name field cannot be empty',type:'error'});
        return;
    }   
    jagg.post("../blocks/resources/database/add/ajax/add.jag", {
        action: "createDatabaseAndAttachUser",
        applicationKey: appKey,
        databaseName:dbName,
        databaseServerInstanceName:$("#stage option:selected").val(),
        isBasic:true,
        customPassword:$('#password').val().trim(),
        userName:$("#user-name").val().trim(),
        templateName:null,
        copyToAll:false,
        createDatasource: false,
        databaseDescription:$("#description").val().trim()
    }, function (result) {
        console.log("result : " + result);
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
