package org.appfac.jaxws.sample;

import javax.jws.WebService;

@WebService
public interface JaxWebService{

	/** This is a sample web service operation */
	public String hello(String txt);	

}