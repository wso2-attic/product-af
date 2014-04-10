$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "application.jag?action=getVersionsInLifecycleStages&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        //data = [["Development", 16.0], ["Testing", 2.0], ["Staging", 1.0], ["Production", 1.0]];
        var applicationsByStage = jQuery.parseJSON(data.text);
        var stagesChart = drawPieChart("stagesChart", applicationsByStage);
        window.console.info("applications by stage");
        //addTooltip(chart);
        addInfoPopup(stagesChart, "info");
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
