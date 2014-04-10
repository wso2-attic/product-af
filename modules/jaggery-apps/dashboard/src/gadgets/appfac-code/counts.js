var deal={
    "xValues":{
        "labels": [
            {value: 1, text: "2013-01-01"}, {value: 2, text: "2013-01-02"},
            {value: 3, text: "2013-01-03"}, {value: 4, text: "2013-01-04"},
            {value: 5, text: "2013-01-05"}, {value: 6, text: "2013-01-06"},
            {value: 7, text: "2013-01-07"}, {value: 8, text: "2013-01-08"},
            {value: 9, text: "2013-01-09"}, {value: 10, text: "2013-01-10"}
        ],
        "title":"Day",
        "titleOrientation":"away"
    },
    "yValues":{
        "title":"Lines count",
        "min": 0,
        "vertical": true, 
        "fixLower": "major", 
        "fixUpper": "major"
    },
    //"data":[100,110,90,70,300,320,270,80,200,90]
    "data":[
        {"text":0,"y":100},
        {"text":0,"y":110},
        {"text":1,"y":90},
        {"text":1,"y":70},
        {"text":0,"y":300},
        {"text":0,"y":320},
        {"text":0,"y":270},
        {"text":1,"y":80},
        {"text":0,"y":200},
        {"text":1,"y":90}
    ]
};


dojo.require("dojo.NodeList-manipulate");
dojo.require("GadgetLineChart");
dojo.addOnLoad(function() {
    var codeCountsChart = new GadgetLineChart('codeCountsChart','Lines of code');
    codeCountsChart.postData(deal);
});
