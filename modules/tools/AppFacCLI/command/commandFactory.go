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
	"bytes"
	"github.com/codegangsta/cli"
	"github.com/Dilhasha/AppFacCLI/cli/urls"
)

const (
	//Keyword for starting a query
	queryStarter = "action"
	connector = "&"
	equator = "="
)

type CommandFactory struct {
	CmdsByName map[string]Command
}

/*GetByCmdName returns command given the command name or short name*/
func (factory CommandFactory) CheckIfCommandExists(cmdName string) (string,bool) {
	cmd , found := factory.CmdsByName[cmdName]
	if !found {
		for _, command := range factory.CmdsByName {
			//If command matches with the short name
			if command.Metadata().ShortName == cmdName {
				return command.Metadata().Name,true
			}
		}
		return "" , false
	}
	return cmd.Metadata().Name , true
}

/* NewFactory returns a new concreteFactory with with a map of commands.*/
func NewFactory() (factory CommandFactory) {
	//Get Urls
	urls := urls.GetUrls()
	//Create map of commands
	factory.CmdsByName = make(map[string]Command)
	factory.CmdsByName["login"] = NewLogin(urls.Login)
	factory.CmdsByName["getApplicationsOfUser"] = NewAppList(urls.ListApps)
	factory.CmdsByName["getAppVersionsInStage"] = NewVersionsList(urls.ListVersions)
	factory.CmdsByName["createNewApplication"] = NewAppCreation(urls.CreateApp)
	factory.CmdsByName["getAppInfo"] = NewAppInfo(urls.GetAppInfo)
	factory.CmdsByName["createArtifact"] = NewArtifact(urls.CreateArtifact)
	factory.CmdsByName["getBuildAndDeployStatusForVersion"] = NewBuildSuccessInfo(urls.GetBuildSuccessInfo)
	factory.CmdsByName["printBuildLogs"] = NewPrintLogs(urls.PrintLogs)
	factory.CmdsByName["triggerBuild"] = NewBuildApp(urls.CreateArtifact)
	factory.CmdsByName["logout"] = NewLogout(urls.Logout)
	factory.CmdsByName["deleteApplication"] = NewAppDeletion(urls.DeleteApp)
	return
}

/* GetCommandFlags converts flags into a list of strings.*/
func (factory CommandFactory) GetCommandFlags(command Command) []string {
	var flags []string
	for _, flag := range command.Metadata().Flags {
		switch flagType := flag.(type) {
		default:
		case cli.StringFlag:
			flags = append(flags, flagType.Name)
		}
	}
	return flags
}

/* GetCommandConfigs returns a CommandConfigs struct based on flags nd flag values.*/
func (factory CommandFactory) GetCommandConfigs(command Command , flagValues []string) CommandConfigs {
	var buffer bytes.Buffer
	flags := command.Metadata().Flags
	var cookie string

	buffer.WriteString(queryStarter + equator + command.Metadata().Name)

	if(len(flagValues) == 0){
		return CommandConfigs{
			Url:"",
			Query:"",
			Cookie:"",
		}
	}

	for n := 0 ; n < len(flags) ; n++ {
		//If flag is a string flag
		if flag, ok := flags[n].(cli.StringFlag); ok {
			if(flag.Usage == "cookie"){
				cookie = flagValues[n]
			}else{
				buffer.WriteString(connector + flag.Usage + equator)
				buffer.WriteString(flagValues[n])
			}
		}
	}
	query := buffer.String()

	return CommandConfigs{
		Url:command.Metadata().Url,
		Query:query,
		Cookie:cookie,
	}
}


