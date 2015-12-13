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
	"testing"
	"net/http"
	"fmt"
	"crypto/tls"
	"bytes"
	"strings"
	"io/ioutil"
	"encoding/json"
)

/* This is an initial test to display how the tool works. */

func TestFunctionality(t *testing.T) {

	t.Log("Initial test to display how the tool works.")

	fmt.Println("Logging in to Appfactory set up at : https://apps.cloud.wso2.com with the username : appfactesting.gmail.com@af2241")

	//The username and password is configured
	var query = []byte("action=login&userName=appfactesting.gmail.com@af2241&password=abc123!@#")

	//Create a new request
	request, err := http.NewRequest("POST", "https://apps.cloud.wso2.com/appmgt/site/blocks/user/login/ajax/login.jag", bytes.NewBuffer(query))
	request.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	request.Header.Set("Cookie", "")

	//Disable security check
	transport := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true},}
	client := &http.Client{Transport: transport}

	//Send the request
	resp, err := client.Do(request)
	if err != nil {
		fmt.Println("Error while performing request : ", err)
	}

	if(resp.StatusCode == http.StatusOK){

		body, _ := ioutil.ReadAll(resp.Body)
		bodyString := string(body)
		if(strings.Contains(bodyString, "true")){
			fmt.Println("\nLogging is successfull.")
			cookie := strings.Split(resp.Header.Get("Set-Cookie"),";")

			//Get the application details for the user
			fmt.Println("\nGetting application details for the user")

			//query
			query = []byte("action=getApplicationsOfUser&userName=appfactesting.gmail.com@af2241")

			//Create a new request
			request, err = http.NewRequest("POST", "https://apps.cloud.wso2.com/appmgt/site/blocks/application/get/ajax/list.jag", bytes.NewBuffer(query))
			request.Header.Set("Content-Type", "application/x-www-form-urlencoded")
			request.Header.Set("Cookie", cookie[0])

			//Send the request
			resp, err = client.Do(request)

			bodyNew, _ := ioutil.ReadAll(resp.Body)
			if (resp.StatusCode == http.StatusOK) {

				bodyString = string(bodyNew)

				type ErrorFormat struct {
					ErrorCode int64 `json:",string"`
					ErrorMessage string
				}

				var errorFormat ErrorFormat
				err = json.Unmarshal([]byte(bodyString), &errorFormat)
				if (err == nil) {
					if (errorFormat.ErrorCode == http.StatusUnauthorized) {
						fmt.Println(errorFormat.ErrorMessage)
						fmt.Println("Your session has expired.Please login and try again!")

					}
				}else{

					type App struct {
						Name string
					}

					var apps []App
					err := json.Unmarshal([]byte(bodyString), &apps)
					if(err == nil){
						fmt.Println("\nYou have ", len(apps)," applications. Following is the list of applications:")
						for _, app := range apps {
							fmt.Println(app.Name)
						}
						fmt.Println()
					}
				}
			}
		}else{
			fmt.Println("Authorization failed. Please try again!")
		}
	}
}

