/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * 
 */

$(document).ready(function () {
    initializeUserActivity("logs", applicationKey, applicationName);
    setVersions($('#stage-download'));
    setVersions($('#stage-view'));
    
    // Loading version values according to the selected stage
    $(".stage").on("change", function() {
        var selectedStageElement = $(this);
        setVersions(selectedStageElement);
    });

    // Hiding loaded log container when changing the new versions
    $("#version-view").on("change", function() {
        $('.log-container').hide();
    });


    /**
    * Set versions according to the selected stage
    */
    function setVersions(selectedStageElement) {
        var selectedStage = selectedStageElement.val();
        var appVersionsInfoArray = JSON.parse(appVersionsInfo);
        var versionsData = []; 
        var currentVersionElement = selectedStageElement.parents('.tab-pane').find('.version');
        currentVersionElement.select2().enable(true);
        $('#view-log').enable(true);
        $('#download-log').enable(true);
        for(var i in appVersionsInfoArray) {
            if(selectedStage == appVersionsInfoArray[i].stage) {
                var versionsInStage = appVersionsInfoArray[i].versions;
                for(var j in versionsInStage) {
                    var select2DataObject = {}; 
                    select2DataObject.id = versionsInStage[j];
                    select2DataObject.text = versionsInStage[j];
                    versionsData.push(select2DataObject);
                }   
            }   
        }
        if(versionsData.length == 0) {
            var select2DataObject = {}; 
            select2DataObject.id = null;
            select2DataObject.text = "No versions available";
            versionsData.push(select2DataObject);
            currentVersionElement.select2().enable(false);
            $('#view-log').enable(false);
            $('#download-log').enable(false);
        }   

        currentVersionElement.empty();
        currentVersionElement.trigger('change');
        currentVersionElement.select2({data: versionsData});
    }

    // Initialzing select2 drop downs
    $('#recent .select2').select2({
        templateResult: formatState,
        templateSelection :formatState,
        width: '100%'
    });

    $('#archived .select2').select2({
        templateResult: formatState,
        templateSelection :formatState,
        width: '100%'
    });

    function formatState (state) {
        if (!state.id) { return state.text; }
        var $state;
        if(state.element.attributes['data-icon']){
            var $state = $(
                    '<span><i class="fa '+ state.element.attributes['data-icon'].value.toLowerCase() +'"></i>&nbsp;&nbsp;'
                    + state.text + '</span>'
            );
        }else{
            var $state = $(
                    '<span><i class="fa '+state.id.toLowerCase() +'"></i>&nbsp;&nbsp;'
                    + state.text + '</span>'
            );
        }
        return $state;
    };
    
    $('#view-log').click(function(){
        $('#view-log').loadingButton({action : "show"});
        var selectedStage = $('#stage-view').val();
        var selectedVersion = $('#version-view').val();
        if(selectedStage && selectedVersion) {
            jagg.post("../blocks/logdownload/ajax/logdownload.jag", {
                action:"downloadLogFile",
                applicationKey:applicationKey,
                applicationStage:selectedStage,
                applicationVersion:selectedVersion,
                downloadFile:"false",
                pageNumber : 1
            },function (result) {
                 if(!result) {
                     $('#view-logs-content').html(result);
                     $('.log-container').show();
                 } else {
                     jagg.message({content: "No logs available for version" + selectedVersion + " in stage " + selectedStage + " .", type: 'error', id:'view_log'});
                 }
            },function (jqXHR, textStatus, errorThrown) {
                jagg.message({content: "Error occurred while loading the logs for version" + selectedVersion + " .", type: 'error', id:'view_log'});
            });        
        }
        $('#view-log').loadingButton({action : "hide"});
      });
    
    $('#download-log').click(function(){
        $('#download-log').loadingButton({action : "show"});
        var selectedDate = $('#date').val();
        var selectedStage = $('#stage-download').val();
        var selectedVersion = $('#version-download').val();
        if(selectedDate) {
            jagg.post("../blocks/logdownload/ajax/logdownload.jag", {
                action:"downloadLogFile",
                applicationKey:applicationKey,
                applicationStage:selectedStage,
                applicationVersion:selectedVersion,
                date:selectedDate,
                downloadFile:"true"
            },function (result) {
                var resultJson = JSON.parse(result);
                if(resultJson.error == true) {
                    jagg.message({content: "No archived logs available to download on " + selectedDate + " .", type: 'warning', id:'download_log'});
                }
            },function (jqXHR, textStatus, errorThrown) {
                jagg.message({content: "Error occurred while loading the logs for version" + selectedVersion + " .", type: 'error', id:'view_log'});
            }); 
        }
         $('#download-log').loadingButton({action : "hide"});
        
    });
    
    $('#date').daterangepicker({
        "singleDatePicker": true,
        "startDate": "08/19/2015",
        "endDate": "08/21/2015"
    }, function(start, end, label) {
    });

    $('.datepicker-container .fw-calendar').click(function(){
        $('#date').data('daterangepicker').toggle();
    })
    
    
});




