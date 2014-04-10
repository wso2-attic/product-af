var pref = new gadgets.Prefs();
gadgets.HubSettings.onConnect = function () {

    gadgets.Hub.subscribe("timeSliderPub", callback);
};

function callback(topic, obj, subscriberData) {
    getAllApps(obj);
}

	gadgets.util.registerOnLoadHandler(getAllApps);

	var selectedAppKey;

        function getAllApps(obj) {		
		var url = pref.getString("getAllAppsDataSource");
		var dataurl = getURL(obj,url);
		var appSelect = document.getElementById('D1');
		appSelect.options.length = 0;
		opt = document.createElement("option");
		opt.value = "";
		opt.textContent = "SELECT APPLICATION";
		appSelect.appendChild(opt);

	        $.ajax({
                    url: dataurl,

                    dataType: 'json',
                    //GET method is used
                    type: "POST",

                    async: false,

                    //pass the data
                    data: "",

                    //Do not cache the page
                    cache: false,

                    //success
                    success: function (html) {
			setDropDown(html);
			
                    }
                });

	}

	function setDropDown(html) {
		
		for(var i=0;i<html.length;i++){
		  var appObject = html[i];
		  for(var key in appObject){
        	    var attrName = key;
        	    var attrValue = appObject[key];

		    var select = document.getElementById("D1");
    		    opt = document.createElement("option");
		    opt.value = attrName;
		    opt.textContent = attrValue;
		    select.appendChild(opt);
        	  }
    		}
	}

	function selectApp(){
		var url = pref.getString("selectAppDataSource");
 		var versionSelect = document.getElementById('D2');
		versionSelect.options.length = 0;
		opt = document.createElement("option");
		opt.value = "";
		opt.textContent = "SELECT VERSION";
		versionSelect.appendChild(opt);
		
		var e = document.getElementById("D1");
		var appKey = e.options[e.selectedIndex].value;
		var respJson = null;
                $.ajax({
                    url: url+"?appKey="+appKey,

                    dataType: 'json',
                    //GET method is used
                    type: "POST",

                    async: false,

                    //pass the data
                    data: "",

                    //Do not cache the page
                    cache: false,

                    //success
                    success: function (html) {
			setVersionDropDown(html);
			
                    }
                });

	}	

	function setVersionDropDown(html){

                var select = document.getElementById("D2");

                for(var i=0;i<html.length;i++){
                  var appObject = html[i];
                  for(var key in appObject){
			opt = document.createElement("option");
                    var attrName = key;
                    var attrValue = appObject[key];

	            opt.value = attrValue;
                    opt.textContent = attrValue;
                    select.appendChild(opt);

                  }
                }

	}


	function selectVersion(){
		var url = pref.getString("selectVersionDataSource");
		var e = document.getElementById("D1");
		var appKey = e.options[e.selectedIndex].value;
		var e2 = document.getElementById("D2");
		var appVersion = e2.options[e2.selectedIndex].value;
                $.ajax({
                    url: url+"?appKey="+appKey+"&appVersion="+appVersion,

                    dataType: 'json',
                    //GET method is used
                    type: "POST",

                    async: false,

                    //pass the data
                    data: "",

                    //Do not cache the page
                    cache: false,

                    //success
                    success: function (html) {
                        createDataTable(html);
                        
                    }
                });

	}

	function getURL(obj, url){
		var dataurl;		
		if(obj){
			dataurl = url+"?from=" + obj[0]+ "&to=" + obj[1]
		}else{
			dataurl = url;
		}
		return dataurl;
	}

	function createDataTable(html){

                var outputDiv = document.getElementById("outputDiv"); 
                outputDiv.innerHTML = "";
                var tableHtml = '<table class="table table-striped  table-bordered">' +
				'<tr>'+
				'<th>ISSUE ID</th>' +
				'<th>ASSIGNEE</th>' +
                                '<th>PRIORITY</th>' +
                                '<th>REPORTER</th>' +
                                '<th>SEVERITY</th>' +
                                '<th>STATUS</th>' +
                                '<th>TYPE</th>' +
				'</tr>'

                for(var i=0;i<html.length;i++){

		  tableHtml += '<tr>';
                  var appObject = html[i];
                  for(var key in appObject){
                    var attrName = key;
                    var attrValue = appObject[key];
                    tableHtml += '<td>' + attrValue + '</td>'
                  }
		  tableHtml += '</tr>';
                }
                tableHtml += '</table>';
                outputDiv.innerHTML = tableHtml;

	}


