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
	"github.com/codegangsta/cli"
	tablemanagement "github.com/buger/goterm"
	"fmt"
)

func HelpTemplate(metadata command.CommandMetadata){
	commandHelp := tablemanagement.NewTable(0, 10, 5, ' ', 0)
	fmt.Fprintf(commandHelp, "%s\t%s\n", Bold("COMMAND"),metadata.Name)
	fmt.Fprintf(commandHelp, "%s\t%s\n", Bold("SHORTNAME"),metadata.ShortName)
	fmt.Fprintf(commandHelp, "%s\t%s\n", Bold("USAGE"),metadata.Usage)
	fmt.Fprintf(commandHelp, "%s\n", Bold("FLAGS"))
	for n := 0; n < len(metadata.Flags); n++ {
		if flag, ok := metadata.Flags[n].(cli.StringFlag); ok {
			if(flag.Name != "-u" && flag.Name != "-c"){
				fmt.Fprintf(commandHelp, "\t%s\t%s\n",flag.Name,flag.Usage)
			}
		}
	}
	tablemanagement.Println(commandHelp)
	tablemanagement.Flush()

}

