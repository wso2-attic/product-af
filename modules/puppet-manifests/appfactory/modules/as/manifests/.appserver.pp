class wso2::appserver ( $version, 
		   $offset=0, 
		   $tribes_port=4000, 
		   $config_db=as_config, 
		   $appserver_domain,
		   $maintenance_mode=true, 
		   $depsync=false, 
		   $sub_cluster_domain=mgt,
		   $owner=root,
		   $group=root,
		   $target="/mnt" ) {
	
	$deployment_code	= "appserver"

	$stratos_version 	= $version
	$service_code 		= "appserver"
	$carbon_home		= "${target}/wso2as-${stratos_version}"


        $service_templates      = [
                                        "conf/axis2/axis2.xml",
                                        "conf/carbon.xml",
                                        "conf/datasources.properties",
                                        "conf/registry.xml",
                                        "conf/tomcat/catalina-server.xml",
                                        "conf/user-mgt.xml",
                                        "conf/tenant-mgt.xml",
                                        "conf/issuetracker/issuetracker.xml",
					"conf/datasources/as-datasources.xml",
				#	"deployment/server/jaggeryapps/cloudmgt/site/conf/cloud-mgt.xml",                                
				]

        $common_templates      =  [	
                                        "conf/appfactory/appfactory.xml",
                                        "conf/datasources/master-datasources.xml",
					"conf/log4j.properties",
				]
	$appconfig_templates   =["deployment/server/jaggeryapps/cloudmgt/site/conf/cloud-mgt.xml"]


	tag ($service_code)

        define pushtemplates ( $directory, $target ) {
        
                file { "${target}/repository/${name}":
                        owner   => $owner,
                        group   => $group,
                        mode    => 0755,
                        content => template("${directory}/${name}.erb"),
                        ensure  => present,
                }
        }

	cleandeployment { $deployment_code:
		mode		=> $maintenance_mode,
                target          => $carbon_home,
	}

	initializedeployment { $deployment_code:
		repo		=> $package_repo,
		version         => $stratos_version,
		mode		=> $maintenance_mode,
		service		=> "as",
		local_dir       => $local_package_dir,
		owner		=> $owner,
		target   	=> $target,
		require		=> WSO2::CleanDeployment[$deployment_code],
	}

	deployservice { $deployment_code:
		service		=> $service_code,	
		version		=> $version,
		security	=> "true",
		owner		=> $owner,
		group		=> $group,
		target		=> $carbon_home,
		require		=> WSO2::InitializeDeployment[$deployment_code],
	}

	if $sub_cluster_domain == "worker" {
		createworker { $deployment_code:
			target	=> $carbon_home,
			require	=> WSO2::DeployService[$deployment_code],
		}
	}	
	
	pushtemplates { 
		$service_templates: 
		target		=> $carbon_home,
		directory 	=> "${service_code}/${version}",
		require 	=> [WSO2::DeployService[$deployment_code],];

		$common_templates:
		target          => $carbon_home,
                directory       => "commons",
		require 	=> WSO2::DeployService[$deployment_code],
	}

	startservice { $deployment_code:
		owner		=> $owner,
                target          => $carbon_home,
		require		=> [ WSO2::InitializeDeployment[$deployment_code],
				     WSO2::DeployService[$deployment_code],
				     PushTemplates[$service_templates],
				     PushTemplates[$common_templates], 
				   ],
	}
	exec { 'wait_for_server_start' :
  		require => [WSO2::StartService[$deployment_code]],
  		command => "sleep 30",
  		path => "/usr/bin:/bin",
	}
	pushtemplates { $appconfig_templates:
		target          => "${carbon_home}",
                directory       => "${service_code}/${version}",
                require         => [Exec['wait_for_server_start']];

		
	}
	
}

