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
	"net/http"
	"crypto/tls"
	"bytes"
	"fmt"
)

type CommandConfigs struct {
	Url string
	Query string
	Cookie string
}

/* Run creates an http request and returns the response received.*/
func (configs CommandConfigs) Run() (*http.Response){
	var query = []byte(configs.Query)
	//Create a new request
	request, err := http.NewRequest("POST", configs.Url, bytes.NewBuffer(query))
	request.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	request.Header.Set("Cookie", configs.Cookie)
	//Disable security check
	transport := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true},}
	client := &http.Client{Transport: transport}
	//Send the request
	response, err := client.Do(request)
	if err != nil {
		fmt.Println("Error while performing request : ", err)
	}
	return response
}
