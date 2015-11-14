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
package help

import (
	"github.com/Dilhasha/AppFacCLI/cli/command"
	tablemanagement "github.com/buger/goterm"
	"fmt"
)

func ToolHelp(factory command.CommandFactory){
	fmt.Println("\n"+Bold("NAME")+" : appfac\n")
	fmt.Println(Bold("USAGE")+" : CLI Tool for WSO2 Appfactory\n")
	fmt.Println(Bold("VERSION")+" : 1.0.0\n")
	fmt.Println(Bold("COMMANDS")+" :\n")

	commands := tablemanagement.NewTable(0, 10, 5, ' ', 0)
	fmt.Fprintf(commands, "%s\t%s\t%s\n", "help","h","Shows help for appfac CLI tool")
	for _, command := range factory.CmdsByName {
		metadata := command.Metadata()
		fmt.Fprintf(commands, "%s\t%s\t%s\n", metadata.Name,metadata.ShortName,metadata.Description)
	}
	tablemanagement.Println(commands)
	tablemanagement.Flush()
}

func Bold(str string) string {
	return "\033[1m" + str + "\033[0m"
}
