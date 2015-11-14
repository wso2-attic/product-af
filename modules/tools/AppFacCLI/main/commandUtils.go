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

package main

import (
	"github.com/Dilhasha/AppFacCLI/cli/command"
	"github.com/codegangsta/cli"
	"github.com/Dilhasha/AppFacCLI/cli/password"
	"github.com/Dilhasha/AppFacCLI/cli/session"
	"github.com/Dilhasha/AppFacCLI/cli/help"
	"fmt"
)

//getFlagValues returns the list of requirements needed to run a command.
func getFlagValues(command command.Command, cmdFlags []string, sessionObject session.Session, args []string) ([]string) {
	var i = 0
	//If flag parsing is disabled
	if (command.Metadata().SkipFlagParsing) {
		flags := command.Metadata().Flags
		// Prompt for values for each flag
		flagValues := make([]string, len(flags), len(flags))
		for n := 0 ; n < len(flags); n++ {
			if flag , ok := flags[n].(cli.StringFlag); ok {
				if (flag.Usage != "password") {
					fmt.Print(flag.Usage + " > ")
					fmt.Scanf("%s", &flagValues[i])
					if (flag.Usage == "userName") {
						sessionObject.UserName = flagValues[i]
					}
					i++
				}else {
					flagValues[i] = password.AskForPassword("Password")
					i++
				}
			}
		}
		return flagValues
	}else {
		isMatch, flagValues := matchArgAndFlags(cmdFlags, args[1:], sessionObject)
		if (isMatch) {
			return flagValues
		}else {
			return nil
		}
	}

}

//matchArgAndFlags matches the flags against user arguments and data available in session.
func matchArgAndFlags(flags []string, args []string, sessionObject session.Session) (bool, []string) {

	var i = 0
	var flagValues = make([]string, len(flags), len(flags))
Loop:
for _, flag := range flags {
	//Checks if flag value is present in user arguments
	containsFlag, index := checkIfArgsContainsFlag(flag, args)
	//Checks if flag value is present in session object
	inSession, val := checkIfInSession(flag, sessionObject)
	if (containsFlag) {
		flagValues[i] = args[index]
		i++
		continue Loop
	}else if (inSession) {
		flagValues[i] = val
		i++
		continue Loop
	}else {
		return false, flagValues
	}
}
	return true, flagValues
}

//checkIfArgsContainsFlag checks whether a given flag is available in user arguments, if so returns index of flag value.
func checkIfArgsContainsFlag(flag string, args []string) (bool, int) {
	for n := 0 ; n < len(args); n++ {
		if (args[n] == flag) {
			//flag at n, so flag value at n+1
			return true, n+1
		}
	}
	return false, -1
}


func runCommand(commandName string , args []string, sessionObject session.Session, cmdFactory command.CommandFactory) (bool) {
	command := cmdFactory.CmdsByName[commandName]
	cmdFlags := cmdFactory.GetCommandFlags(command)
	flagValues := getFlagValues(command, cmdFlags, sessionObject, args)
	configs := cmdFactory.GetCommandConfigs(command, flagValues)
	if (configs.Url == "" && configs.Query == "" && configs.Cookie == "") {
		help.HelpTemplate(cmdFactory.CmdsByName[commandName].Metadata())
		return true
	}
	continueFlag, cookie := command.Run(configs)
	if (commandName == loginCommand && continueFlag) {
		//set session object username
		sessionObject = setSessionUserName(cmdFlags, flagValues)
		sessionObject.Cookie = cookie
		success := writeSession(sessionObject)
		if (success) {
			fmt.Println("Your session details are stored.")
		}else {
			fmt.Println("Error occured while storing session!")
		}
	}
	return continueFlag
}
