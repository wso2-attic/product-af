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

 /* Package main handles the flow of CLI tool matching user arguments, session details to the required flags for eac command*/
package main

import (
	"os"
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/command"
	"github.com/codegangsta/cli"
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/session"
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/help"
	"github.com/wso2/product-af/modules/tools/AppFacCLI/cli/urls"
	"fmt"
	"io/ioutil"
	"io"
	"encoding/json"

)

const (
	loginCommand = "login"
	//<TODO> move this to cache
	sessionFilename = "session.txt"
	baseUrlFilename = "baseUrl.txt"

)
//main handles the flow of the CLI tool.
func main() {
	//Create basic cli tool
	app := cli.NewApp()
	app.Name = "appfac"
	app.Usage = "CLI Tool for WSO2 Appfactory"

	if len(os.Args) >1 && os.Args[1]==command.SetBaseUrlCommand{
		setBaseUrl()
	}

	baseUrl := readBaseUrl()
	urls.SetBaseUrl(baseUrl)
	cmdFactory := command.NewFactory()
	var continueFlag bool
	var sessionObject session.Session
	var args[] string

	//command `appfac` without argument or help (h) flag
	if len(os.Args) == 1 || os.Args[1] == "help" || os.Args[1] == "h" {
		help.ToolHelp(cmdFactory)
	}else if _command, ok := cmdFactory.CheckIfCommandExists(os.Args[1]); ok {
		args = os.Args[1:]
		if (_command == command.SetBaseUrlCommand){
			return
		}else if(_command != loginCommand) {
			//If session file exists
			if _, err := os.Stat(sessionFilename); err == nil {
				//Read session data
				data, err := ioutil.ReadFile(sessionFilename)
				if err != nil {
					panic(err)
				}
				//Get session data into a session object
				err = json.Unmarshal(data, &sessionObject)
				if(err != nil){
					fmt.Println("Error occured while getting stored session.")
				}
				continueFlag = runCommand(_command,args,sessionObject,cmdFactory)
			}else{
				fmt.Println("You must be logged into continue.")
				sessionObject = session.NewSession()
				continueFlag = runCommand(loginCommand,args,sessionObject,cmdFactory)
			}
		}else {
			sessionObject = session.NewSession()
			continueFlag = runCommand(_command,args,sessionObject,cmdFactory)
		}
		for(!continueFlag){
			continueFlag = runCommand(loginCommand,args,sessionObject,cmdFactory)
		}
	}else{
		fmt.Println("'"+os.Args[1]+"' is not a valid command. See 'appfac help'")
	}

}


//writeBaseUrl writes baseUrl to file.
func writeBaseUrl(base string)bool{
	file, err1 := os.Create(baseUrlFilename)
	if err1 != nil {
		fmt.Println(err1)
		return false
	}
	n, err := io.WriteString(file,base)
	if err != nil{
		fmt.Println(n, err)
		return false
	}
	file.Close()
	return true
}

func readBaseUrl() string{
	if _, err := os.Stat(baseUrlFilename); err == nil {
		//Read base Url
		data, err := ioutil.ReadFile(baseUrlFilename)
		if err != nil {
			panic(err)
		}
		return string(data)
	}else{
		fmt.Println("Base url value is not set. Please set it to continue.")
		setBaseUrl()
		data, err := ioutil.ReadFile(baseUrlFilename)
		if err != nil {
			panic(err)
		}
		return string(data)
	}
}

func setBaseUrl(){
	fmt.Print("baseURL > ")
	var base string
	fmt.Scanf("%s", &base)
	success:=writeBaseUrl(base)
	if(success){
		fmt.Println("Base Url is set successfully.")
	}else{
		fmt.Println("Error occurred while setting baseUrl.")
	}
}



