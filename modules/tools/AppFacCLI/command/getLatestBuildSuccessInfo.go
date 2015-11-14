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
package command

import (
	"io/ioutil"
	"net/http"
	"github.com/codegangsta/cli"
	"encoding/json"
	"github.com/Dilhasha/AppFacCLI/cli/formats"
	"fmt"
)

/* BuildSuccessInfo is the implementation of the command to display the last build success details of an application. */

type BuildSuccessInfo struct {
	Url string
}

func NewBuildSuccessInfo(url string) (cmd BuildSuccessInfo) {
	return BuildSuccessInfo{
		Url : url,
	}
}

/* Returns metadata for build success information.*/
func (buildSuccessInfo BuildSuccessInfo)Metadata() CommandMetadata{
	return CommandMetadata{
		Name : "getBuildAndDeployStatusForVersion",
		Description : "get last build success details of a particular version of an application",
		ShortName : "bs",
		Usage :"get build success info",
		Url : buildSuccessInfo.Url,
		SkipFlagParsing : false,
		Flags : []cli.Flag{
			cli.StringFlag{Name: "-a", Usage: "applicationKey"},
			cli.StringFlag{Name: "-v", Usage: "version"},
			cli.StringFlag{Name: "-c", Usage: "cookie"},
		},
	}
}

/* Run calls the Run function of CommandConfigs and verifies the response from that call.*/
func(buildSuccessInfo BuildSuccessInfo) Run(configs CommandConfigs)(bool,string){
	var resp *http.Response
	resp = configs.Run()
	//if request did not fail
	if(resp != nil){
		defer resp.Body.Close()
	}else{
		//exit the cli
		return true, ""
	}
	body, _ := ioutil.ReadAll(resp.Body)
	if (resp.Status == "200 OK") {
		bodyString := string(body)
		var errorFormat formats.ErrorFormat
		var buildSuccessFormat formats.BuildSuccessFormat

		err := json.Unmarshal([]byte(bodyString), &errorFormat)
		if (err == nil) {
				if (errorFormat.ErrorCode == http.StatusUnauthorized) {
					fmt.Println("Your session has expired.Please login and try again!")
					return false , configs.Cookie
				}else{
					err = json.Unmarshal([]byte(bodyString), &buildSuccessFormat)
					if (err == nil) {
						fmt.Println("Build ID is: ",buildSuccessFormat.BuildId)
						fmt.Println("Build status is: ",buildSuccessFormat.BuildStatus)
						fmt.Println("Deployed Id is: ",buildSuccessFormat.DeployedId)
					}
				}
		}
	}
	return true , configs.Cookie
}
