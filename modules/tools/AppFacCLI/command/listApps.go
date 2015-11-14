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
	"github.com/Dilhasha/AppFacCLI/cli/formats"
	"github.com/codegangsta/cli"
	tm "github.com/buger/goterm"
)

/* AppList is the implementation of the command to display details of available applications for app factory user */

type AppList struct {
	Url string
}


func NewAppList(url string) (cmd AppList) {
	return AppList{
		Url : url,
	}
}

/* Returns metadata for listing application details of a user.*/
func (appList AppList)Metadata() CommandMetadata{
	return CommandMetadata{
		Name : "getApplicationsOfUser",
		Description : "Lists applications of a user",
		ShortName : "la",
		Usage : "list apps",
		Url : appList.Url,
		SkipFlagParsing : false,
		Flags : []cli.Flag{
			cli.StringFlag{Name: "-u", Usage: "userName"},
			cli.StringFlag{Name: "-c", Usage: "cookie"},
		},
	}

}

/* Run calls the Run function of CommandConfigs and verifies the response from that call.*/
func(applist AppList) Run(configs CommandConfigs)(bool , string){
	resp := configs.Run()
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
		err := json.Unmarshal([]byte(bodyString), &errorFormat)
		if (err == nil) {
			if (errorFormat.ErrorCode == http.StatusUnauthorized) {
				fmt.Println(errorFormat.ErrorMessage)
				fmt.Println("Your session has expired.Please login and try again!")
				return false , configs.Cookie
			}
		}else{
			var apps []formats.AppFormat
			err := json.Unmarshal([]byte(bodyString), &apps)
			if(err == nil){
				fmt.Println("\nYou have ", len(apps)," applications. Details of applications are as follows.\n")
				totals := tm.NewTable(0, 10, 5, ' ', 0)
				fmt.Fprintf(totals, "Name\tKey\tType\n")
				fmt.Fprintf(totals, "-------\t------\t-----\n")
				for _, app := range apps {
					fmt.Fprintf(totals, "%s\t%s\t%s\n", app.Name,app.Key,app.Type)
				}
				tm.Println(totals)
				tm.Flush()
			}
		}
	}
	return true,configs.Cookie
}
