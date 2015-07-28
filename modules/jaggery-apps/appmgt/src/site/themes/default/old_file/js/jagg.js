var jagg = jagg || {};
var messageTimer;
(function () {
    jagg.post = function(url, data, callback, error) {
        return jQuery.ajax({
                               type:"POST",
                               url:url,
                               data:data,
                               async:true,
                               cache:false,
                               success:callback,
                               error:error
        });
    };

    jagg.syncPost = function(url, data, callback, type) {
        return jQuery.ajax({
                               type: "POST",
                               url: url,
                               data: data,
                               async:false,
                               success: callback,
                               dataType:"json"
        });
    };

    jagg.sessionExpired = function (){
        var sessionExpired = false;
        jagg.syncPost("/site/blocks/user/login/ajax/sessionCheck.jag", { action:"sessionCheck" },
                 function (result) {
                     if(result!=null){
                         if (result.message == "timeout") {
                             sessionExpired = true;
                         }
                     }
                 }, "json");
        return sessionExpired;
    };

    jagg.sessionAwareJS = function(params){


        if(jagg.sessionExpired()){
            if(params.e != undefined){  //Canceling the href call
                if ( params.e.preventDefault ) {
                    params.e.preventDefault();

                // otherwise set the returnValue property of the original event to false (IE)
                } else {
                    params.e.returnValue = false;
                }
            }

            jagg.showLogin(params);
        }else if(params.callback != undefined && typeof params.callback == "function"){
             params.callback();
         }
    };


    /*
    usage
    Show info dialog
    jagg.message({type:'success',content:'Your Message'});

    Show warning
    jagg.message({content:'foo',type:'warning' });

    Show error dialog
    jagg.message({content:'foo',type:'error' });

    Any message type can have callback with a button
    cbk         = callback method
    cbkBtnText  = text to display with the button
    jagg.message({content:'foo',type:'error' });

    Showing custom message with custom content and custom call backs

     var $domContent = $('<div><button class="customButton">Custom Button</button></div>');
     $('.customButton',$domContent).click(function(){alert('do something here..')});
         jagg.message({
         type:'custom',
         content:$domContent
     });


     Hiding all messages
     jagg.removeMessage();

    Targeting a specific message
     jagg.message({content:'foo',type:'error',id:'uniqueId' });

     jagg.removeMessage('uniqueId');


    */


    jagg.message = function(params){
        // Included noty plugin implementation
        var allowedType = ["alert", "success", "error", "warning", "information", "confirm"];
        if(allowedType.indexOf(params.type) < 0){
            params.type = "information"
        }
        return noty({
                 theme: 'wso2',
                 layout: 'top',
                 type: params.type,
                 text: params.content,
                 animation: {
                     open: {height: 'toggle'}, // jQuery animate function property object
                     close: {height: 'toggle'}, // jQuery animate function property object
                     easing: 'swing', // easing
                     speed: 500 // opening & closing animation speed
                 }
             });
    };

    jagg.removeMessage = function(id){
        // TODO: close based on the id
        // store the returned objects id from the jagg.message and passed it to this function
        $.noty.closeAll();
    };
    //jagg.popMessage({type:'confirm',title:'Model Title',content:'Model Content',okCallback:function(){alert('do this when ok')},cancelCallback:function(){alert('do this when cancel')}});

    //jagg.popMessage({content:'Message'});

    jagg.popMessage = function(params){
        $('.qtip').remove();
        var $modelObject = $('<div class="modal_content">'+
            '<div class="message_body error_details">'+
            '<dl>'+
                '<dt class="modal_msg"></dt>'+
                '<dd class="img">'+
                    '<span class="icon-stack">'+
                        '<i class="icon-circle icon-stack-base"></i>'+
                        '<i class="icon-exclamation"></i>'+
                    '</span>'+
                '</dd>'+
                '<dd id="contentRowContainer"></dd>'+
            '</dl>'+
            '</div>'+
        '<div class="message_action" id="buttonRowContainer">'+
        '</div>'+
        '</div>');
        var $buttonRow = '';
        var $contentRow = '';
        if(params.type == "confirm"){
            //show ok and cancel buttons and register callbacks for each.
            $buttonRow = $('<ul class="inline_list">'+
                                '<li class="inline_item"><a href="#" class="btn sub big modal_cancel" >Cancel</a></li>'+
                                '<li class="inline_item"><a href="#" class="btn main big modal_action">OK</a></li>'+
                            '</ul>');

            $('.modal_action',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            $('.modal_cancel',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            if(typeof params.okCallback == 'function'){
                $('.modal_action',$buttonRow).click(params.okCallback);
            }
            if(typeof params.cancelCallback == 'function'){
                $('.modal_cancel',$buttonRow).click(params.cancelCallback);
            }
        }

        if(params.type == "ok"){
            //show ok and cancel buttons and register callbacks for each.
            $buttonRow = $('<ul class="inline_list">'+
                '<li class="inline_item"><a href="#" class="btn main big modal_action">Ok</a></li>'+
                '</ul>');

            $('.modal_action',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            if(typeof params.okCallback == 'function'){
                $('.modal_action',$buttonRow).click(params.okCallback);
            }

            $('.modal_msg',$modelObject ).remove();
            $('.img',$modelObject ).remove();
        }


        if( (typeof $buttonRow) == "object"){
            $('#buttonRowContainer',$modelObject).append($buttonRow);
        }else{
            $('#buttonRowContainer',$modelObject).html($buttonRow);
        }

        if( (typeof params.content == "object")){
            $('#contentRowContainer',$modelObject).append(params.content);
        }else{
            $('#contentRowContainer',$modelObject).html(params.content);
        }



        //text(params.btnText);
        $('.modal_msg',$modelObject).html(params.title);

        $('#qtip').qtip(
            {
                content: {
                    text: function(api){
                        return $modelObject;
                    },
                    prerender: true
                },

                position: {
                    my: 'center',
                    at: 'center',
                    target: $(window)
                },
                show: {
                    ready: true,
                    solo: true,
                    modal: {
                        blur: true,
                        escape: true
                    },
                    when: false
                },

                hide: true,
                style: {
                    classes: 'modal_box',
                    widget: false,
                    def: false
                }
            });

    };



		var e = jQuery.Event("keyup"); // or keypress/keydown
		e.keyCode = 27; // for Esc
		$(document).trigger(e); // trigger it on document


	$(document).keyup(function(e) {
		if (e.keyCode == 27) { // Esc
			jagg.removeMessage();
		}
	});
	
	jagg.getConvertedVersion=function(version){
	    return version.replace(/\./g,'_');
	};

}());