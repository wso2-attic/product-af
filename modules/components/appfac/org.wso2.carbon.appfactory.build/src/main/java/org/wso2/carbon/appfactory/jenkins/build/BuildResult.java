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

package org.wso2.carbon.appfactory.jenkins.build;

/**
 * This Enum defines all possible build results in Jenkins CI
 * 
 */
public enum BuildResult {

    Succesfull("SUCCESS", "Successful"), Aborted("ABORTED", "Aborted"), Failed("FAILURE", "Failed"),
    Unstable("UNSTABLE", "Unstable"), NotBuild("NOT_BUILT", "Not Build"),Building("BUILDING",
                                                                                        "Building");

    /**
     * The Id returned by the jenkins CI
     */
    private String id;

    /**
     * Display friendly name of the build result
     */
    private String name;

    private BuildResult(String id, String name) {
        this.id = id;
        this.name = name;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    /**
     * Returns the {@link BuildResult} matching the given string
     * 
     * @param value
     *            the {@link String} representation of the build result
     * @return matching {@link BuildResult} or null ( if match doesn't occur
     */
    public static BuildResult convert(String value) {
        for (BuildResult bs : BuildResult.values()) {
            if (bs.getId().equalsIgnoreCase(value)) {
                return bs;
            }
        }
        return null;
    }

}
