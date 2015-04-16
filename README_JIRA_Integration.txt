----------JIRA OAuth Integration-POC-----------------

***The components changed are:
	product-af/modules/components/appfac/org.wso2.carbon.appfactory.issuetracking
	appmgt

***Classes changed in org.wso2.carbon.appfactory.issuetracking

	* Changes added to pom.xml

		Added following dependencies:

		<dependency>
		   <groupId>net.oauth.core</groupId>
		   <artifactId>oauth</artifactId>
		   <version>20090617</version>
		</dependency>
		<dependency>
		   <groupId>net.oauth.core</groupId>
		   <artifactId>oauth-httpclient4</artifactId>
		   <version>20090617</version>
		</dependency>
		<dependency>
		   <groupId>com.google.common</groupId>
		   <artifactId>google-collect</artifactId>
		   <version>1.0</version>
		</dependency>

		Added net.oauth.* and com.google.common.collect.* as private-package as follows.

		<Private-Package>
		org.wso2.carbon.appfactory.issuetracking.internal.*,net.oauth.*,com.google.common.collect.*
		</Private-Package>

	* Changes added to internal/IssueTrackerServiceComponent.java

		bundleContext.registerService(IssueTrackerService.class.getName(), new IssueTrackerService(), null);


	* Changes added to service/IssueTrackerService.java

		Added following new methods,

		createJIRAOAuthClient
		setOAuthAuthorizationUrl
		setAccessToken
		getSummaryofIssues

	* New classes added are,

		AtlassianOAuthClient
		JIRAOAuthClient
		TokenSecretVerifierHolder

***Changes done in appmgt

	Changed modules/issuetracker/module.jag
	Added new modules/issuetracker/get/getIssueSummaries.jag
	Added new  site/blocks/issuetracker/list/ajax/getSummary.jag
	Added new css files elegantAero.css and hor_minimalist.css to site/themes/default/css/
	Changed site/themes/default/templates/issuetracker/list/template.jag
	Added /site/themes/default/templates/issuetracker/list/getUserData.jag




