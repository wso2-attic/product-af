var plot;
var chart;
/**
 * Comment
 * @param {String} chartDivId
 * @param {Array} data
 */
function drawPieChart(chartDivId, data) {
    chart = $("#" + chartDivId);
    plot = jQuery.jqplot(chartDivId, [data],
            {seriesColors: ["#ff5555", "#478ec7", "#d7561f", "#88a533", "#dee439", "#5bbc3c", "#5d82ab", "#f48129"],
                //title:'Sample Pie Chart',
                seriesDefaults: {
                    renderer: jQuery.jqplot.PieRenderer,
                    rendererOptions: {
                        startAngle: -90,
                        showDataLabels: true
                    }
                },
                legend: {show: true, location: 'e', textColor: '#ffffff', background: '#1C1C1C'},
                grid: {background: '#1C1C1C', borderColor: 'transparent', shadow: false, drawBorder: false, shadowColor: 'transparent'}
            }
    );
    return chart;
}

/**
 * Comment
 * @param {Object} chart
 */
function addTooltip(chart) {
    chart.bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, data) {
        var $this = $(this);
        $this.attr('title', data[0] + ":" + data[1]);
    });

    chart.bind('jqplotDataUnhighlight', function(ev, seriesIndex, pointIndex, data) {
        var $this = $(this);
        $this.attr('title', "");
    });
}

/**
 * Comment
 * @param {Object} chart
 * @param {String} infoDivId
 */
function addInfoPopup(chart, infoDivId) {
    chart.bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, data) {
        var htmlText = "<span style=\"color:" + plot.seriesColors[pointIndex] + "\">" + data[0] + " : " + data[1] + "</span>";
        $("#" + infoDivId).html(htmlText);

        var parentOffset = $(this).parent().offset();
        //or $(this).offset(); if you really just want the current element's offset
        var relX = ev.pageX - parentOffset.left;
        var relY = ev.pageY - parentOffset.top;

        $("#"+infoDivId).fadeIn('slow').css("top", relY).css("left", parseInt(relX + 20, 10));
//        $("#" + infoDivId).show().css("top", relY).css("left", parseInt(relX + 20, 10));

    });

    chart.bind('jqplotDataUnhighlight', function(ev, seriesIndex, pointIndex, data) {
//                $("#"+infoDivId).fadeOut();
        $("#" + infoDivId).hide();
    });
}
