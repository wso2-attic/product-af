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


// side pane animation
$('.side-pane-trigger').click(function(){
    var rightPane = $('.right-pane');
    var leftPane = $('.left-pane');
    if (rightPane.hasClass('visible')){
        rightPane.animate({"left":"0em"}, "slow").removeClass('visible');
        leftPane.animate({"left":"-18em"}, "slow");
        $(this).find('i').removeClass('fa-arrow-left').addClass('fa-reorder');
    } else {
        rightPane.animate({"left":"18em"}, "slow").addClass('visible');
        leftPane.animate({"left":"0em"}, "slow");
        $(this).find('i').removeClass('fa-reorder').addClass('fa-arrow-left');
    }
});
    
$('input[type=password]').after('<span class="hide-pass"><i class="fa fa-eye"></i> </span>');
//password show hide characters
$('.hide-pass').click(function(){
    if($(this).find('i').hasClass("fa-eye-slash")){
        $(this).parent().find('input[data-schemaformat=password]').attr('type', 'password');
        $(this).find('i').removeClass( "fa-eye-slash" );
    } else {
        $(this).find('i').addClass( "fa-eye-slash" );
        $(this).parent().find('input[data-schemaformat=password]').attr('type', 'text');
    }
});

//password generator modal
$('.password-generator').click(function(){
    $('#password-modal').modal('show');
});

//password strength meter    
$(function () {
    $("#password")
        .popover({ title: 'Password Meter', content: "Password must meet the following requirements:" })
        .blur(function () {
            $(this).popover('hide');
        })
        .data('bs.popover')
        .tip()
        .addClass('password-meter');
});

// add new database 
function addNewDatabase(){
    var dbName = $("#database-name").val().trim();
    if(dbName==null || dbName == "" || dbName == undefined){
        jagg.message({content:'Database name field cannot be empty',type:'error'});
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
