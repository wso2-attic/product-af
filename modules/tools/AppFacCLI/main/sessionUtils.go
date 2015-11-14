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
	"github.com/Dilhasha/AppFacCLI/cli/session"
	"os"
	"io"
	"encoding/json"
	"fmt"
)

//setSession returns a session object with userName set.
func setSessionUserName(flags []string, flagValues []string)(session.Session){
	for n := 0; n < len(flags); n++ {
		//If userName is available in flagValues set it in session
		if(flags[n]=="-u"){
			return session.Session{flagValues[n], ""}
		}
	}
	return session.Session{"",""}
}

//writeSession writes the session object to a file and returns whether it is successful.
func writeSession(sessionObject session.Session)bool{
	var n int
	var s []byte
	file, err := os.Create(filename)
	if err != nil {
		fmt.Println(err)
		return false
	}
	s, err = json.Marshal(sessionObject)
	if err != nil {
		fmt.Println(err)
		return false
	}
	n, err = io.WriteString(file, string(s))
	if err != nil{
		fmt.Println(n, err)
		return false
	}
	file.Close()
	return true
}

//checkIfInSession returns whether the user already logged in and returns the cookie for session.
func checkIfInSession(flag string,sessionObject session.Session)(bool,string){
	if(flag=="-u"){
		return true, sessionObject.UserName
	}else if(flag=="-c"){
		return true, sessionObject.Cookie
	}
	return false, ""
}
