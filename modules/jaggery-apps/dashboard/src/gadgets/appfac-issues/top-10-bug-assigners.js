$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "issues.jag?action=getAssignerIssueCount&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        var response = jQuery.parseJSON(data.text);
        for(var index in response) {
        	var contentIssues = response[index];
        	$("#content").append('<tr><td>' + contentIssues.name + '</td><td class="value-cell">' + 
        		contentIssues.issueCount + '</td></tr>');
        }
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
