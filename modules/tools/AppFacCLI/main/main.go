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
	"github.com/Dilhasha/AppFacCLI/cli/command"
	"github.com/codegangsta/cli"
	"github.com/Dilhasha/AppFacCLI/cli/session"
	"github.com/Dilhasha/AppFacCLI/cli/help"
	"fmt"
	"io/ioutil"
	"encoding/json"
)

const (
	loginCommand = "login"
	//<TODO> move this to cache
	filename = "session.txt"
)
//main handles the flow of the CLI tool.
func main() {
	//Create basic cli tool
	app := cli.NewApp()
	app.Name = "appfac"
	app.Usage = "CLI Tool for WSO2 Appfactory"

	cmdFactory := command.NewFactory()
	var continueFlag bool
	var sessionObject session.Session
	var args[] string

	//command `appfac` without argument or help (h) flag
	if len(os.Args) == 1 || os.Args[1] == "help" || os.Args[1] == "h" {
		help.ToolHelp(cmdFactory)
	}else if command, ok := cmdFactory.CheckIfCommandExists(os.Args[1]); ok {
		args = os.Args[1:]
		if(command != loginCommand) {
			//If session file exists
			if _, err := os.Stat(filename); err == nil {
				//Read session data
				data, err := ioutil.ReadFile(filename)
				if err != nil {
					panic(err)
				}
				//Get session data into a session object
				err = json.Unmarshal(data, &sessionObject)
				if(err != nil){
					fmt.Println("Error occured while getting stored session.")
				}
				continueFlag = runCommand(command,args,sessionObject,cmdFactory)
			}else{
				fmt.Println("You must be logged into continue.")
				sessionObject = session.NewSession()
				continueFlag = runCommand(loginCommand,args,sessionObject,cmdFactory)
			}
		}else {
			sessionObject = session.NewSession()
			continueFlag = runCommand(command,args,sessionObject,cmdFactory)
		}
		for(!continueFlag){
			continueFlag = runCommand(loginCommand,args,sessionObject,cmdFactory)
		}
	}else{
		fmt.Println("'"+os.Args[1]+"' is not a valid command. See 'appfac help'")

	}

}








