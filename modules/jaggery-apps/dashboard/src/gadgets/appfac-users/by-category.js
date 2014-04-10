$(function() {

	function getHtml() {   
		var parameters = {}; 
	    parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.HTML; 
	    var url = gadgetAPIContext() + "user.jag?action=getUserCountByRoles&id=" + Math.random(); 
	    gadgets.io.makeRequest(url, processResponse, parameters);
	};
	 
	function processResponse(response) { 
	
		var users = jQuery.parseJSON(response.data);
	    var html = '<table class="table"><tbody>';
	    for (var categoryKey in users) {
	        var category = users[categoryKey];
	        var icon = category.displayName.split(' ').join('') + "-icon";
	        html += '<tr><td><i class="' + icon + '"></i> ' + category.displayName + '</td>';
	        html += '<td> ' + category.count + '</td></tr>';
	
	    }
	    html += '</thead><tbody>';
	
	    document.getElementById('usersGadgetList').innerHTML = html;
	};
	 
	gadgets.util.registerOnLoadHandler(getHtml);

});