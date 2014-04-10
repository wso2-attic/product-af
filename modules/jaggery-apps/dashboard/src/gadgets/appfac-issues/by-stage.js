$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "issues.jag?action=getIssueCountsByStatus&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        //data =[["New", 9], ["In-progress", 0], ["Resolved", 0], ["Feedback", 0], ["Closed", 1], ["Rejected", 0]] ;
        var issuesByStage = jQuery.parseJSON(data.text);
        var stagesChart = drawPieChart("stagesChart", issuesByStage);
        window.console.info("by stage issues 2");
        //addTooltip(chart);
        addInfoPopup(stagesChart, "info");
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
