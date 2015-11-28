


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
package urls


var baseUrl string

type Urls struct {
	Login string
	ListApps string
	ListVersions string
	CreateApp string
	Logout string
	GetAppInfo string
	CreateArtifact string
	GetBuildSuccessInfo string
	PrintLogs string
	DeleteApp string
}

//getUrls returns a Urls object with urls for each api cal.
func GetUrls()Urls{
	return Urls{
		Login :baseUrl+"/appmgt/site/blocks/user/login/ajax/login.jag",
		ListApps :baseUrl+"/appmgt/site/blocks/application/get/ajax/list.jag",
		ListVersions : baseUrl+"/appmgt/site/blocks/application/get/ajax/list.jag",
		CreateApp : baseUrl+"/appmgt/site/blocks/application/add/ajax/add.jag",
		Logout :baseUrl+"/appmgt/site/blocks/user/logout/ajax/logout.jag",
		GetAppInfo : baseUrl+"/appmgt/site/blocks/application/get/ajax/list.jag",
		CreateArtifact :baseUrl+"/appmgt/site/blocks/reposBuilds/add/ajax/add.jag",
		GetBuildSuccessInfo : baseUrl+"/appmgt/site/blocks/reposBuilds/list/ajax/list.jag",
		PrintLogs : baseUrl+"/appmgt/site/blocks/reposBuilds/get/ajax/get.jag",
		DeleteApp : baseUrl+"/appmgt/site/blocks/application/delete/ajax/delete.jag",
	}
}

func SetBaseUrl(base string){
	baseUrl=  base
}

func BaseUrl() string{
	return baseUrl
}

