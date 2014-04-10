$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "composite.jag?action=getResourceCount&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        var response = jQuery.parseJSON(data.text);
        $("#datasourcecount").append(response.datasourcecount);
        $("#apicount").append(response.apicount);
        $("#propertycount").append(response.resourcecount);
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
