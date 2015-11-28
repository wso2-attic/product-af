/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package command_test

import (
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/command"
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/urls"
	"testing"
)

/* The unit test for AppCreation. */

func TestNewAppCreation(t *testing.T) {
	t.Log("Testing creation of AppCreation object")
	url := urls.GetUrls()
	appCreation := command.NewAppCreation(url.CreateApp)

	if(appCreation.Url != url.CreateApp) {
		t.Errorf("Expected '%s', but it was '%s' instead.", url.CreateApp, appCreation.Url)
	}
}

func TestMetadata(t *testing.T){
	t.Log("Testing metadata")
	url := urls.GetUrls()
	appCreation := command.NewAppCreation(url.CreateApp)
	metadata := appCreation.Metadata()

	if(metadata.Name != "createNewApplication") {
		t.Errorf("Expected 'createNewApplication', but it was '%s' instead.", metadata.Name)
	}
}

func TestRun(t *testing.T){
	t.Log("Testing metadata")
	url := urls.GetUrls()
	appCreation := command.NewAppCreation(url.CreateApp)

	configs := command.CommandConfigs{
		Url : "https://apps.cloud.wso2.com/appmgt/site/blocks/application/add/ajax/add.jag",
		Query : "action=createNewApplication&userName=dilhasha.uom.gmail.com@uomcse&applicationKey=de&applicationName=De&applicationDescription=sh&applicationType=war&repositoryType=git",
		Cookie : "JSESSIONID=11450C5F38F9F167D55FF2F268436068",
	}

	success,cookie := appCreation.Run(configs)

	if(cookie != configs.Cookie) {
		t.Errorf("Expected '%s', but it was '%s' instead.", configs.Cookie , cookie)
	}
	if(success == false){
		t.Errorf("Run command failed for appCreation. Check cookie and whether app already exists.")
	}
}
