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
                 layout: 'topCenter',
                 type: params.type,
                 text: params.content,
                 timeout: '5000',
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


    jagg.removeMessageById = function(id) {
        if(id) {
            $.noty.close(id);
        }
    };

    jagg.popMessage = function(params){
        return noty({
                        theme: 'wso2',
                        layout: 'topCenter',
                        type: 'confirm',
                        closeWith: ['button','click'],
                        modal: (params.modalStatus ? params.modalStatus : false),
                        text: params.content ? params.content : 'Do you want to continue?',
                        buttons: [
                            {addClass: 'btn btn-primary', text: (params.okText ? params.okText : 'Ok'), onClick: function($noty) {
                                $noty.close();
                                if (isFunction(params.okCallback)) {
                                    params.okCallback();
                                }
                            }
                            },
                            {addClass: 'btn btn-default', text: 'Cancel', onClick: function($noty) {
                                $noty.close();
                                if (isFunction(params.cancelCallback)) {
                                    params.cancelCallback();
                                }

                            }
                            }
                        ],
                        animation: {
                            open: {height: 'toggle'}, // jQuery animate function property object
                            close: {height: 'toggle'}, // jQuery animate function property object
                            easing: 'swing', // easing
                            speed: 500 // opening & closing animation speed
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

/**
 * Check whether the {@code functionToCheck} is a function or not
 * @param functionToCheck
 * @returns {*|boolean}
 */
function isFunction(functionToCheck) {
    var getType = {};
    return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
}
