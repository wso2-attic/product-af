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
	"github.com/Dilhasha/AppFacCLI/cli/session"
	"github.com/Dilhasha/AppFacCLI/cli/urls"
	"fmt"
	"strings"
	"strconv"
	"os"
)

/* BuildApp is the implementation of the command to build an app and wait to display build logs of a given application */
type BuildApp struct {
	Url string
}

func NewBuildApp(url string) (cmd BuildApp) {
	return BuildApp{
		Url : url,
	}
}

/* Returns metadata for triggering a build.*/
func (buildApp BuildApp)Metadata() CommandMetadata{
	return CommandMetadata{
		Name : "triggerBuild",
		Description : "trigger a build for an app, wait until its success and display build logs",
		ShortName : "tb",
		Usage : "triggering a build",
		Url : buildApp.Url,
		SkipFlagParsing : false,
		Flags : []cli.Flag{
			cli.StringFlag{Name: "-a", Usage: "applicationKey"},
			cli.StringFlag{Name: "-v", Usage: "version"},
			cli.StringFlag{Name: "-c", Usage: "cookie"},
		},
	}
}

/* Run calls the Run function of CommandConfigs and verifies the response from that call.*/
func(buildApp BuildApp) Run(configs CommandConfigs)(bool,string){

	//Construct query for creating an artifact
	query := configs.Query
	query = strings.Replace(query, "triggerBuild" , "createArtifact" , 1)
	configs.Query = query

	response := configs.Run()
	//if request did not fail
	if(response != nil){
		defer response.Body.Close()
	}else{
		//exit the cli
		return true, ""
	}
	body, _ := ioutil.ReadAll(response.Body)

	bodyStr := string(body)
	var errorFormat formats.ErrorFormat

	err := json.Unmarshal([]byte(bodyStr), &errorFormat)

	//waitFlag := false
	if (err == nil) {
		if (errorFormat.ErrorCode == http.StatusUnauthorized) {
				fmt.Println("Your session has expired. Please login and try again!")
				return false , configs.Cookie
		}
	}
	//Ask whether user wants to wait
	fmt.Println("Build has been triggered...")
	fmt.Println("Do you want to wait until build success? ( Y - Yes , N - No )")
	fmt.Scanf("%s", &bodyStr)
	//User chooses to wait
	if(bodyStr == "Y" || bodyStr == "YES"){
		fmt.Println("waiting...")
		//Construct query for getting last build success id
		query := configs.Query
		query = strings.Replace(query, "createArtifact" , "getBuildAndDeployStatusForVersion" , 1)
		configs.Query = query
		configs.Url = urls.GetUrls().GetBuildSuccessInfo
		continueFlag,buildId := checkBuildId(configs)
		id := buildId
		//If the build has not finished yet
		for(continueFlag){
			continueFlag,buildId = checkBuildId(configs)
			if(buildId != id){
				//New build is successful
				break
			}
		}
		if(buildId == id +1){
			//Build is successful
			fmt.Println("\nThe build has been successful. Displaying build logs below..\n")
			query := configs.Query
			query = strings.Replace(query, "getBuildAndDeployStatusForVersion" , "printBuildLogs" , 1)
			query = strings.Replace(query, "version" , "applicationVersion" , 1)

			//append build id and tenant domain
			query = query + "&lastBuildId=" + strconv.FormatInt(buildId , 10) + "&tenantDomain=" + getTenantDomain()
			configs.Query = query
			configs.Url = urls.GetUrls().PrintLogs
			printLogs := NewPrintLogs(urls.GetUrls().PrintLogs)
			return printLogs.Run(configs)

		}else if(!continueFlag){ //Error occurred while performing request
			return false, configs.Cookie
		}

	}
	return true,configs.Cookie
}


/* checkBuildId checks for the build id and returns the success of the check and the build id*/
func checkBuildId(configs CommandConfigs)(bool,int64) {
	var resp *http.Response
	resp = configs.Run()
	//if request did not fail
	if(resp != nil){
		defer resp.Body.Close()
	}else{
		//exit the cli
		return false, -1
	}
	body, _ := ioutil.ReadAll(resp.Body)
	if (resp.Status == "200 OK") {
		bodyString := string(body)
		var errorFormat formats.ErrorFormat
		var buildSuccessFormat formats.BuildSuccessFormat

		err := json.Unmarshal([]byte(bodyString), &errorFormat)
		if (err == nil) {
			if (errorFormat.ErrorCode == http.StatusUnauthorized) {
				fmt.Println("Your session has expired. Please login and try again!")
				return false, -1
			}else {
				err = json.Unmarshal([]byte(bodyString), &buildSuccessFormat)
				if (err == nil) {
					return true, buildSuccessFormat.BuildId
				}
			}
		}
	}
	return false,-1
}

/* getTenantDomain extracts tenant domain from username and returns it*/
func getTenantDomain() string{
	//Read username from session file and split the tenant domain
	//If session file exists
	filename := "session.txt"
	sessionObject := session.NewSession()
	if _, err := os.Stat(filename); err == nil {
		//Read session data
		data, err := ioutil.ReadFile(filename)
		if err != nil {
			panic(err)
		}
		//Get session data into a session object
		err = json.Unmarshal(data, &sessionObject)
		if (err != nil) {
			fmt.Println("Error occured while getting stored session.")
		}
		arrays := strings.Split(sessionObject.UserName,"@")
		return arrays[len(arrays) - 1]
	}
	return ""
}
