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
	"fmt"
	"io/ioutil"
	"net/http"
	"encoding/json"
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/formats"
	"github.com/codegangsta/cli"
	tm "github.com/buger/goterm"
)

/* VersionList is the implementation of the command to display details of available versions of a given application of app factory user */
type VersionsList struct {
	Url string
}

func NewVersionsList(url string) (cmd VersionsList) {
	return VersionsList{
		Url : url,
	}
}

/* Returns metadata for listing versions.*/
func (versionsList VersionsList)Metadata() CommandMetadata{
	return CommandMetadata{
		Name : "getAppVersionsInStage",
		Description : "Lists versions of an application in a stage",
		ShortName : "lv",
		Usage : "list versions",
		SkipFlagParsing : false,
		Url : versionsList.Url,
		Flags : []cli.Flag{
			cli.StringFlag{Name: "-u", Usage: "userName"},
			cli.StringFlag{Name: "-s", Usage: "stageName"},
			cli.StringFlag{Name: "-a", Usage: "applicationKey"},
			cli.StringFlag{Name: "-c", Usage: "cookie"},
		},
	}
}

/* Run calls the Run function of CommandConfigs and verifies the response from that call.*/
func(versionsList VersionsList) Run(configs CommandConfigs)(bool,string){
	resp := configs.Run()
	//if request did not fail
	if(resp != nil){
		defer resp.Body.Close()
	}else{
		//exit the cli
		return true, ""
	}
	body, _ := ioutil.ReadAll(resp.Body)

	if (resp.StatusCode == http.StatusOK) {
		bodyString := string(body)
		var errorFormat formats.ErrorFormat
		err := json.Unmarshal([]byte(bodyString), &errorFormat)
		if (err == nil) {
			if (errorFormat.ErrorCode == http.StatusUnauthorized) {
				fmt.Println("Your session has expired.Please login and try again!")
				return false , configs.Cookie
			}
		}else{
			var appVersions []formats.AppVersion
			err := json.Unmarshal([]byte(bodyString), &appVersions)
			if(err == nil){
				fmt.Println("\nApplication has ", len(appVersions[0].Versions)," versions. Details of versions are as follows.\n")
				for _, appVersion := range appVersions {
					versions := appVersion.Versions
					totals := tm.NewTable(0, 10, 5, ' ', 0)
					fmt.Fprintf(totals, "Version\tAutoDeploy\tStage\tRepoURL\n")
					fmt.Fprintf(totals, "-------\t---------\t-----\t-----------\n")

					for _, version := range versions{
						fmt.Fprintf(totals, "%s\t%s\t%s\t%s\n", version.Version,version.AutoDeployment,version.Stage,version.RepoURL)
					}
					tm.Println(totals)
					tm.Flush()
				}
			}
		}
	}
	return true,configs.Cookie
}
