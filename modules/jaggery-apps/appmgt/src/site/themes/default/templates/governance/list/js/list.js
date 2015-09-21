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
    getAppVersionInfo();
    $('.tab-pane').each(function () {
        var $this = $(this);
        $this.find('.progress-bar-indicator').text($('.strikethrough:checked').length + '/' + $(this).find('.strikethrough').length);
        $(this).on('click', '.strikethrough', function () {
            var checkEl = $this.find('.strikethrough');
            var checkedEl = $this.find('.strikethrough:checked');
            drawProgress($this, checkEl.length, checkedEl.length);
            checkListItemChecked(checkEl);
        });
    });

    $('.ecdate').daterangepicker({
                                     locale: {
                                         format: 'YYYY-MM-DD'
                                     },
                                     "singleDatePicker": true,
                                 }, function (start, end, label) {
        var days = moment().diff(start, 'days'); // current date - selected date -> if old date is seleted this will give positive value
        if (days > 0) {
            isSelectedDateValid = false;
            jagg.message({content: "You selected an old date, please re select an upcoming date", type: 'warning'});
        } else {
            isSelectedDateValid = true;
        }
    });

    $('.datepicker-container .fw-calendar').click(function () {
        $(this).parents('.datepicker-container').find('.ecdate').data('daterangepicker').toggle();
    });

    $('.ecdate').change(function () {
        if (isSelectedDateValid) {
            updateETA($(this).val());
        }
    });


    $('#promote-to-testing-btn').click(function () {
        $('#promote-to-testing-btn').loadingButton('show');
        doLifeCycleAction("Promote", "[true,true,true]", ""); // TODO : get 2nd parameter  dynamically
        $('#promote-to-testing-btn').loadingButton('hide');
    });

    $('#promote-to-production-btn').click(function () {
        $('#promote-to-production-btn').loadingButton('show');
        doLifeCycleAction("Promote", "[true,true]", ""); // TODO : get 2nd parameter  dynamically
        $('#promote-to-production-btn').loadingButton('hide');
    });

    $('#retire-btn').click(function () {
        $('#retire-btn').loadingButton('show');
        doLifeCycleAction("Retire", "[]", "");
        $('#retire-btn').loadingButton('hide');
    });

    $('.demote-button').click(function () {
        $('.demote-button').loadingButton('show');
        var modalElement = $('#demote-confirm-modal');
        modalElement.modal({show: true});
        $('#demote-confirm').on('click',function(e){
            var demoteReasonTxt = $('textarea#demote-reason-text').val();
            doLifeCycleAction("Demote", "[]", demoteReasonTxt);
            modalElement.modal('hide');
            $('textarea#demote-reason-text').val("");
        });
        $('.demote-button').loadingButton('hide');
    });

    /**
    * Draw progress bar and set values
    */
    function drawProgress(tabPaneElement, allCheckListItemCount, checkedCheckListItemCount) {
        var percentage = 0;
        var perElementPercentage = 100 / allCheckListItemCount;
        for(var i=0; i<checkedCheckListItemCount; i++) {
              percentage += perElementPercentage;
        }
        if(percentage == 100 && hasPromotePermissions[currentStage]) {
                tabPaneElement.find('.promote-button').prop('disabled', false);
        } else {
                tabPaneElement.find('.promote-button').prop('disabled', true);
        }
        tabPaneElement.find('.progress-bar').css('width', percentage + '%').attr('aria-valuenow', percentage);
        tabPaneElement.find('.progress-bar-indicator').text(checkedCheckListItemCount + '/' + allCheckListItemCount);
    }

    /**
     * When estimated time is changed.This will do the update.
     * @param etaTo
     */
    function updateETA(etaTo) {
        jagg.post("../blocks/eta/set/ajax/set.jag", {
            action: "set",
            applicationKey: applicationKey,
            stage: currentStage,
            version: selectedVersion,
            user: userName,
            etaFrom: "0000-00-00",
            etaTo: etaTo
        }, function (result) {
            var parsedResult = JSON.parse(result);
            if (!parsedResult.error) {
                jagg.message({content: "Estimated completion date updated successfully", type: 'success'});
            } else {
                jagg.message({content: parsedResult.message, type: 'error'});
            }
        }, function (jqXHR, textStatus, errorThrown) {
            jagg.message({content: "Error occurred while updating estimated completion time", type: 'error'});
        });

    }

    /**
     * Retrieve eta from backend and update the estimated time field calendar text.
     */
    function retrieveETA() {
        jagg.post("../blocks/eta/get/ajax/get.jag", {
            action: "get",
            applicationKey: applicationKey,
            stage: currentStage,
            version: selectedVersion,
            user: userName
        }, function (result) {
            var resultJson = JSON.parse(result);
            if (!resultJson.error && resultJson.eta.etaTo.length > 0) {
                $('.ecdate').val(resultJson.eta.etaTo);
            } else {
                $('.ecdate').val("YYYY-MM-DD");
            }
        }, function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content: "Error occurred while retireving ETA", type: 'success'});
            }
        });
    }


    /**
     *  Promote / Demote / Retire action
     * @param actionName
     * @param checkItems
     * @param comment
     */
    function doLifeCycleAction(actionName, checkItems, comment) {
        jagg.post("../blocks/lifecycle/add/ajax/add.jag", {
            action: actionName,
            applicationKey: applicationKey,
            stageName: currentStage,
            version: selectedVersion,
            checkItems: checkItems,
            tagName: "",
            comment: comment,
            userName: userName
        }, function (result) {
            var parsedResult = JSON.parse(result);
            if (parsedResult.error == true) {
                jagg.message({content: parsedResult.message, type: 'error'});
            } else {
                jagg.message({
                                 content: "Successfully submitted request for the operation - " + actionName,
                                 type: 'success'
                             });
                var newStage;
                if (actionName == "Promote") {
                    newStage = nextStage[currentStage];
                } else if (actionName == "Demote") {
                    newStage = previousStage[currentStage];
                }
                else if (actionName == 'Retire') {
                    newStage = currentStage;
                }
                currentStage = newStage;
                updateSessionNewStage(newStage);
                if (actionName == "Promote") {
                    deployAndUpdatePromoteStatus(newStage);
                }
                getAppVersionInfo();
                drawTabLinks();
            }
        }, function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content: "Error occurred while performing the governance operation", type: 'error'});
            }
        });

    }

    /**
     * This is to update the changed stage in block layer
     * @param newStage - new stage after promote / demote
     */
    function updateSessionNewStage(newStage) {
        jagg.post("../blocks/lifecycle/add/ajax/add.jag", {
            action: "updateSessionInfo",
            stageName: newStage
        }, function (result) {
            //We ignore the result here
        }, function (jqXHR, textStatus, errorThrown) {
            //We ignore the errors here
        });
    }

    /**
     * Once app is promoted this will be called. This will deploy the app and update the promote status.
     * @param newStage - stage that app is promoted to
     */
    function deployAndUpdatePromoteStatus(newStage) {
        jagg.post("../blocks/lifecycle/add/ajax/add.jag", {
            action: "deployAndUpdatePromoteStatus",
            applicationKey: applicationKey,
            deployAction: "",
            stage: newStage,
            tagName: "",
            version: selectedVersion
        }, function (result) {
            jagg.message({
                             content: "Deployment has been submitted successfully for the next stage " + newStage,
                             type: 'success'
                         });
        }, function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({
                                 content: "Error occurred while deploying the artifact in next stage " + newStage,
                                 type: 'error'
                             });
            }
        });
    }

    /**
     * This will be triggered at checklist item checked/unchecked event.
     * @param allCheckBoxItems - all check list item checkbox elements
     */
    function checkListItemChecked(allCheckBoxItems) {
        var itemArray = new Array(allCheckBoxItems.length);
        for (var i in allCheckBoxItems) {
            var checkBoxItem = allCheckBoxItems[i];
            itemArray[checkBoxItem.value] = checkBoxItem.checked;
        }
        jagg.post("../blocks/lifecycle/add/ajax/add.jag", {
            action: "invokeUpdateLifeCycleCheckList",
            applicationKey: applicationKey,
            stageName: currentStage,
            version: selectedVersion,
            checkItems: JSON.stringify(itemArray)
        }, function (result) {
            var resultJson = JSON.parse(result);
            if (resultJson.error) {
                jagg.message({content: resultJson.message, type: 'error'});
            } else {
                jagg.message({content: "Lifecycle check list item change updated", type: 'success'});
            }
        }, function (jqXHR, textStatus, errorThrown) {
        });
    }
    
    /**
     * Activate the tab and its elements according to current stage of the version of the application
     * and permissions of the user.
     */
    function setElementsVisible(isAppRetired) {
            if(!isAppRetired) {
            $('#app-retired-text').hide();
            // active only the current stage tab, meanwhile check the visibility permission
            if (hasVisibilityPermissions[currentStage]) {
                $('.nav-tabs').show();
                $('.tab-content').show();
                $('.nav-tabs a[href="#' + currentStage.toLowerCase() + '"]').tab('show');
            }
            // disable promote button (once all the checkboxes are checked this will become active)
            $('.promote-button').prop('disabled', true);
            // disable demote button if user doesnt have demote permissions
            if (!hasDemotePermissions[currentStage]) {
                $('.demote-button').prop('disabled', true);
            }
            if (!hasRetirePermission) {
                $('#retire-btn').prop('disabled', true);
            }
        } else {
             $('#app-retired-text').show();
        }
    
    }

    /**
     * Draw the markup of checklist items
     */
    function drawCheckListItems() {
        var isAppRetired;
        jagg.post("../blocks/lifecycle/add/ajax/add.jag", {
            action: "getCheckListItemsOfAppVersion",
            applicationKey: applicationKey,
            version: selectedVersion
        }, function (result) {
            var resultJson = JSON.parse(result);
            // This is to check whether app is retired, if app is retired there are no checklist items
            if(resultJson.length ==0) {
                // app retired
                isAppRetired = true;
            } else {
                isAppRetired = false;
                // app is not retired
            }
            setElementsVisible(isAppRetired);
            var disabled = "";
            if(!hasPromotePermissions[currentStage]) {
                disabled = " disabled";
            }
            if (!resultJson.error && resultJson.length > 0) {
                var checkBoxes = "";
                var checkedItemsCount = 0;
                for (var i in resultJson) {
                    var checkListItemName = resultJson[i].name;
                    var checked = "";
                    if (resultJson[i].value == "true") {
                        checkedItemsCount += 1;
                        checked = "checked";
                    }
                    checkBoxes += '<div class="checkbox"><label><input type="checkbox" class="strikethrough custom-checkbox" value="' + i + '" ' + checked + disabled +'><span>' + resultJson[i].name + '</span></label></div>'
                }
                drawProgress($('#' + resultJson[0].status.toLowerCase()), resultJson.length, checkedItemsCount);
                $('#' + resultJson[0].status.toLowerCase()).find('.checkboxes').html(checkBoxes);
            }
            if(!resultJson[0] || currentStage == resultJson[0].status) {
                // governance operation successfull
                $('.loader').loading('hide');
                $('.loading-cover').overlay('hide'); 
            } else {
                if(retryCount < 3) {
                    setTimeout(function() {
                        drawCheckListItems();
                        retryCount++;
                    }, 5000);
                } else {
                    jagg.message({content: 'Theres a problem is completing lifecycle operation, please try again later', type: 'warning'});
                }
            }
            
        }, function (jqXHR, textStatus, errorThrown) {
        });
    }

    /**
     * Get app version info
     */
    function getAppVersionInfo() {
        // displaying loader
        $('.loader').loading('show');
        $('.loading-cover').overlay('show');
        // disable all tabs
        $('.nav-tabs a').prop('disabled', true);
        // hide tab panel
        $('.nav-tabs').hide();
        $('.tab-content').hide();
        
        jagg.post("../blocks/application/get/ajax/list.jag", {
            action: "getAppVersionAllInfoByVersion",
            applicationKey: applicationKey,
            userName: userName,
            version: selectedVersion
        }, function (result) {
            var appInfo = jQuery.parseJSON(result);
            if (appInfo) {
                    currentStage = appInfo.versionInfo.stage;
                    loadLifeCycleEventHistory();
                    retrieveETA();
                    drawCheckListItems();
            }
        }, function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status != 0) {
                jagg.message({content: 'Could not load Application information', type: 'error', id: 'notification'});
            }
        });
    }

    /**
     * This will retrieve the lifecycle history and draw the markup
     */
    function loadLifeCycleEventHistory() {
        jagg.post("../blocks/lifecycle/get/ajax/get.jag", {
            action: "getLifeCycleHistoryForApplication",
            userName: userName,
            applicationKey: applicationKey,
            version: selectedVersion,
            stageName: currentStage
        }, function (result) {
            var resultJson = JSON.parse(result);
            for (var i in resultJson) {
                var historyEvent = resultJson[i];
                if (historyEvent.item.action) {
                    $('#history-events-table tbody').append(' <tr><td><span class="table-notification-msg table-noti-success"></span><i class="fw fw-ok table-notification-i noti-success"></i></td><td>' +
                                                            historyEvent.item.timestamp + '</td><td>Application ' + historyEvent.item.action + 'd - ' + historyEvent.item.state +
                                                            ' to ' + historyEvent.item.targetState + '</td><td>By ' + historyEvent.item.user + '</td></tr>');
                    $('.lifecycle-event-history').show();
                }
            }

        }, function (jqXHR, textStatus, errorThrown) {
            jagg.message({content: 'Could not retrieve lifecycle history.', type: 'error', id: 'history'});
        });
    }
});
