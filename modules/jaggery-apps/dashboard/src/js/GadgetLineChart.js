dojo.require('dojox.charting.Chart2D');
dojo.require('dojox.charting.widget.Chart2D');
dojo.require('dojox.charting.plot2d.Lines');
dojo.require('dojox.charting.plot2d.Markers');
dojo.require('dojox.charting.axis2d.Default');
dojo.require('dojox.charting.themes.Tom');
dojo.require('dojox.charting.widget.Legend');
dojo.require('dojox.charting.action2d.Tooltip');
dojo.require('dojox.charting.action2d.Magnify');
dojo.provide("GadgetLineChart");
dojo.declare("GadgetLineChart", null, {
    constructor : function(chartDivId,seriesTitle) {
        _chart = new dojox.charting.Chart2D(chartDivId);
        _legend = new dojox.charting.widget.Legend({
            chart : _chart, 
            horizontal: true
        }, chartDivId+"Legend");
        _seriesTitle=seriesTitle;

        console.info("line chart constructor");
        
    },
    postData : function(deal) {
        _chart.setTheme(dojox.charting.themes.Tom).addPlot('default', {
            type : 'Lines',
            labels : true,
            fontColor : "#000"
        }).addSeries(_seriesTitle, deal.data).render();
        
        //_chart.addPlot("default", {
        //    markers: true
        //});

        _chart.addPlot("default", {markers:true, styleFunc: function(item){
            if (item <= 150) {
                return {
                    fill : "red"
                };
            } else if (item > 150) {
                return {
                    fill : "green"
                };
            }
            return {};
        }});
        
        _chart.addAxis("x",deal.xValues);
        _chart.addAxis("y",deal.yValues);
        
        //_tip = new dojox.charting.action2d.Tooltip(_chart,"default");
        _mag = new dojox.charting.action2d.Magnify(_chart,"default");
        
        _chart.render();
        _legend.refresh();
    },
    updateData : function(deal) {
        
        _chart.updateSeries(_seriesTitle, deal).render();
        _legend.refresh();
    }
});