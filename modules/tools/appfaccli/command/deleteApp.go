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
)

type AppDeletion struct {
	Url string
}

/* AppDeletion is the implementation of the command for a user to delete an app from app factory */

func NewAppDeletion(url string) (cmd AppDeletion) {
	return AppDeletion{
		Url : url,
	}
}


/* Returns metadata for app deletion*/
func (appDeletion AppDeletion)Metadata() CommandMetadata{
	return CommandMetadata{
		Name : "deleteApplication",
		Description : "Deletes an application of user",
		ShortName : "da",
		Usage : "delete app",
		Url : appDeletion.Url,
		SkipFlagParsing : false,
		Flags : []cli.Flag{
			cli.StringFlag{Name: "-a", Usage: "appKey"},
			cli.StringFlag{Name: "-u", Usage: "userName"},
			cli.StringFlag{Name: "-c", Usage: "cookie"},
		},
	}

}


/* Run calls the Run function of CommandConfigs and verifies the response from that call.*/
func(appDeletion AppDeletion) Run(configs CommandConfigs)(bool , string){
	//Send http request and get response
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
				fmt.Println(errorFormat.ErrorMessage)
				fmt.Println("Your session has expired.Please login and try again!")
				return false , configs.Cookie
			}
		}
		var successMessage formats.SuccessFormat
		err = json.Unmarshal([]byte(bodyString), &successMessage)
		if(err == nil){
			fmt.Println(successMessage.Message)
		}
	}
	return true,configs.Cookie
}
