$(function() {
    function getHtml() {
        var parameters = {};
        parameters[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
        var url = gadgetAPIContext() + "build.jag?action=getAllJenkinsBuildsStats&id=" + Math.random(); 
        gadgets.io.makeRequest(url, processResponse, parameters);
    };

    function processResponse(data) {
        //data =[["success", 4.0], ["failure", 2.0], ["aborted", 1.0]];
        var builds = jQuery.parseJSON(data.text);
        var buildsStatusChart = drawPieChart("buildsStatusChart", builds);
        //addTooltip(chart);
        addInfoPopup(buildsStatusChart, "buildsStatusChartInfo");
    };

    gadgets.util.registerOnLoadHandler(getHtml);

});
