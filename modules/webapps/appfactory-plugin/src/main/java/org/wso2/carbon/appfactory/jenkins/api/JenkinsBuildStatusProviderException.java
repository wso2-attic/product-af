package org.wso2.carbon.appfactory.jenkins.api;

import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProviderException;

public class JenkinsBuildStatusProviderException extends BuildStatusProviderException {

	private static final long serialVersionUID = 1L;

	public static final int INVALID_API = 100;

	public static final int INVALID_RESPONSE = 101;

	private int code = 0;

	public JenkinsBuildStatusProviderException(String msg) {
		super(msg);
	}

	public JenkinsBuildStatusProviderException(String msg, int code) {
		super(msg);
		this.code = code;
	}

	public int getCode(){
		return this.code;
	}

}
