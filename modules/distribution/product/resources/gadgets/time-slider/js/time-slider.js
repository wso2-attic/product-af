var pref = new gadgets.Prefs();
var delay;
var chartData = [];
var options;
var period;
$(function () {

fetchSliderEndpoints();

});
var fetchSliderEndpoints = function (){
    var url = pref.getString("dataSource");
    $.ajax({
        url: url,
        type: "GET",
        dataType: "json",
        success: function (data) {
          drawSlider(data)

        }
    });

 }

var drawSlider = function (startDate) {

    var endDate = Math.floor(Date.now() / 1000);
    $("#Slider").slider(
        {
            from: startDate - 2629740,
            to: endDate + 2629740,
            dimension: '',
            scale: getScale(startDate, endDate),
            limits: false,
            calculate: function (value) {
                return  new Date(value * 1000).toDateString();
            },
            callback: function (value) {
                var result = [];
                result[0]= value.split(';')[0];
                result[1]= value.split(';')[1];
                gadgets.Hub.publish("timeSliderPub", result);
            }
        }
    )

   $("#Slider").slider('value', startDate, endDate);

}

function getScale(from, to) {
    var scaleArray = [];
    var scaleStart = parseInt(from) - 2629740;
    var scaleEnd = parseInt(to) + 2629740;
    for (i = scaleStart; i < scaleEnd ; i = i + 2629740) {
        scaleArray.push(new Date(i * 1000).toDateString());
    }
    return scaleArray;
}



