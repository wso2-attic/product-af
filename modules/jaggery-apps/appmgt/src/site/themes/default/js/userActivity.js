var events = [];
var appName;
var appKey;
var appVersion = null;
var page;

var EVENT_PUBLISH_PERIOD = 180000;

function initializeUserActivity(currentPage, applicationKey, applicationName) {
	appKey = applicationKey;
	appName = applicationName;
	page = currentPage;
	addUserActivity(page, "load");
}

function addUserActivity(item, action) {
    var event={};
    if (!item) {
        item = 'noname';
    }
    event.item = item;
    event.action = action;
    event.timestamp = $.now();
    event.appName = appName;
    event.appKey = appKey;
    event.appVersion = appVersion;
    events[events.length] = event;
}

function publishEvents(pageUnload) {
    if(pageUnload) {
    	addUserActivity(page, "page-unload");
    } else {
    	addUserActivity(page, "same-page");
    }
    
    var copied = events;
    events = [];
    
    //console.log("************** Published events " + copied.length);
    
    jagg.syncPost("../blocks/events/publish/ajax/publish.jag", {
                    action:"userActivity",
                    events:JSON.stringify(copied)
            }, function (result) {
            }, function (jqXHR, textStatus, errorThrown) {
            });
    
    if (!pageUnload) {
        setTimeout(function() {
                publishEvents(false);
        } , EVENT_PUBLISH_PERIOD);
    }
    return;
}

var addClickEvents = function(e){
      //console.log(e.target);
      var target = $(e.target);
      if (target.is("input") || target.is("select") || target.is("textarea") || target.is("a") || target.is("button") || target.is("span")) {
    	  var item = target.attr("name") ? target.attr("name") : target.attr("id");
    	  if (!item) {
    		  item = 'noname';
    	  }
          item = "" + page + "-" + item;	  
    	  addUserActivity(item, "click");
      }
 }

  
$(document).ready(function($){
           
     $(document).click(addClickEvents);
      
  	 setTimeout(function() { 
         publishEvents(false);
	       } , EVENT_PUBLISH_PERIOD);
});


$(window).bind('beforeunload', function() {
	publishEvents(true);
});




