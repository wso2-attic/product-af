$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "issues.jag?action=getIssueCountsByPriority&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        //data = [["Low", 2], ["Normal", 3], ["High ", 1], ["Urgent ", 1], ["Immediate ", 1]];
        var issuesByPriority = jQuery.parseJSON(data.text);
        var prioritiesChart = drawPieChart("prioritiesChart", issuesByPriority);
        window.console.info("by stage issues 2");
        //addTooltip(chart);
        addInfoPopup(prioritiesChart, "prioritiesChartInfo");
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
