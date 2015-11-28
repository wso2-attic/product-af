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
	"strings"
	"io/ioutil"
	"github.com/codegangsta/cli"
	"net/http"
)

/* Login is the implementation of the command to log into a user account in app factory */
type Login struct {
	Url string
}

func NewLogin(url string) (cmd Login) {
	return Login{
		Url : url,
	}
}

/* Returns metadata for login.*/
func (login Login)Metadata() CommandMetadata{
	return CommandMetadata{
		Name : "login",
		Description : "Login to app factory",
		ShortName : "l",
		Usage : "login",
		Url : login.Url,
		SkipFlagParsing : true,
		Flags : []cli.Flag{
			cli.StringFlag{Name:"-u",Usage:"userName"},
			cli.StringFlag{Name: "-p", Usage: "password"},
		},
	}
}

/* Run calls the Run function of CommandConfigs and verifies the response from that call.*/
func(login Login) Run(configs CommandConfigs)(bool , string){
	resp := configs.Run()
	//if request did not fail
	if(resp != nil){
		defer resp.Body.Close()
	}else{
		//exit the cli
		return true, ""
	}
	if(resp.StatusCode == http.StatusOK){
		body, _ := ioutil.ReadAll(resp.Body)
		bodyString := string(body)
		if(strings.Contains(bodyString, "true")){
			fmt.Println("You have Successfully logged in.")
			cookie := strings.Split(resp.Header.Get("Set-Cookie"),";")
			configs.Cookie = cookie[0]

		}else{
			fmt.Println("Authorization failed. Please try again!")
			return false , configs.Cookie
		}
	}
	return true,configs.Cookie
}

