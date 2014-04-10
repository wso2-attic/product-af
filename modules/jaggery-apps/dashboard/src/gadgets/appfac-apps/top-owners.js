$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "application.jag?action=getTopAppOwners&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        //data = [["Development", 16.0], ["Testing", 2.0], ["Staging", 1.0], ["Production", 1.0]];
        var topOwners = jQuery.parseJSON(data.text);
        var stagesChart = drawPieChart("stagesChart", topOwners);
        window.console.info("applications by stage");
        //addTooltip(chart);
        addInfoPopup(stagesChart, "info");
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
