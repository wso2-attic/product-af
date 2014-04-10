$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "source.jag?action=topRepositoriesByLinesOfCode&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        var response = jQuery.parseJSON(data.text);
        for(var repository in response) {
        	var contentRepository = response[repository];
        	$("#content").append('<tr><td>' + contentRepository.name + '</td><td class="value-cell">' + 
        		contentRepository.linesOfCode + '</td></tr>');
        }
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
