/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package org.wso2.carbon.appfactory.issuetracking.beans;


import org.wso2.carbon.appfactory.issuetracking.exception.IssueTrackerException;

/**
 * Takes a GenericIssue & converts a generic issue into a issue tracker specific issue.
 * <p/>
 * e.g. A GenericIssue containing generic data will be input to this converter, and in JIRA
 * this GenericIssue is converted to a JIRA specific issue.
 *
 * @param <T> The specific issue
 */
public interface IssueConverter<T> {

    T getSpecificIssue(GenericIssue genericIssue) throws IssueTrackerException;

}
