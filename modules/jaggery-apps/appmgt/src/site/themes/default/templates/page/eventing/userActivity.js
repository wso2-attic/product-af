/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

var events = [];
 
function addUserActivity(item, action, appName, appKey, appVersion) {
	var event={};
    event.appName = appName;
    event.appKey = appKey;
    event.appVersion = appVersion;
    event.timestamp = "generate-todo"; //TODO
    event.item = item;
    event.action = action;
    events[events.length] = event;
}

function publishEvents(item, pageUnload, appName, appKey, appVersion) {
    if(pageUnload) {
    	addUserActivity(item, "page-unload", appName, appKey, appVersion);
    } else {
    	addUserActivity(item, "same-page", appName, appKey, appVersion);
    }
    
    var copied = events;
    events = [];

    alert("publishing ******** copied " + copied.length);
    
    jagg.post("../blocks/events/publish/ajax/publish.jag", {
                    action:"userActivity",
                    events:JSON.stringify(copied)
            }, function (result) {
            }, function (jqXHR, textStatus, errorThrown) {
            });
    
    if (!pageUnload) {
        setTimeout(function() {
                publishEvents(item, false);
        } , 15000);
    }
}