/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.helloapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Spring Controller Class
 */
@Controller
public class HelloWorldController {

	/**
	 * stores the string to be displayed specific to the methods being invoked
	 */
	String msg;

	/**
	 * Method called when the request comes in the form of "/"
	 */
	@RequestMapping(value = "/")
	public ModelAndView indexPageCallback() {

		//creating a new ModelAndView object and indicating the location of the view page
		ModelAndView mav = new ModelAndView("index");
		//the message to be passed
		msg = "Hello World!!";
	   /*adding msg object to the created ModelAndView object,
	    the message will be assigned to the msg parameter
       */
		mav.addObject("msg", msg);
		return mav;
	}

	/**
	 * Method called when the request comes in the form of "/home/{custom name}"
	 */
	@RequestMapping(value = "/home/{username}")
	public ModelAndView welcomePage(@PathVariable("username") String username) {

		ModelAndView mav = new ModelAndView("index");
		msg = "Welcome " + username + "!!";
		mav.addObject("msg", msg);
		return mav;
	}
}
