class wso2::appserver::dev ( $version, 
		   $offset=0, 
		   $tribes_port=4000, 
		   $config_db=as_dev_config, 
		   $datasource=WSO2ASDevConfigDB,
		   $maintenance_mode=true, 
		   $depsync=false, 
		   $sub_cluster_domain=mgt,
		   $owner=root,
		   $group=root,
		   $af_env,
		   $target="/mnt" ) {
	
	$deployment_code	= "appserver_dev"

	$stratos_version 	= $version
	$service_code 		= "as"
	$carbon_home		= "${target}/wso2${service_code}-${stratos_version}"


        $service_templates      = [
                                        "conf/axis2/axis2.xml",
                                        "conf/carbon.xml",
                                        "conf/datasources.properties",
                                        "conf/datasources/as-datasources.xml",
                                        "conf/registry.xml",
                                        "conf/tenant-mgt.xml",
                                        "conf/tomcat/catalina-server.xml",
					"conf/security/authenticators.xml",
					"conf/security/cipher-text.properties",
					"conf/security/secret-conf.properties",
                                        "conf/user-mgt.xml",
					"conf/etc/logging-config.xml",
					"conf/etc/bam.xml",
					"conf/multitenancy/usage-throttling-agent-config.xml",
                                ]

        $common_templates      =  [	
                                        "conf/datasources/master-datasources.xml",
					"conf/log4j.properties",
					"conf/appfactory/appfactory.xml",
				]


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
		service		=> $service_code,
		local_dir       => $local_package_dir,
		owner		=> $owner,
		target   	=> $target,
		require		=> WSO2::CleanDeployment[$deployment_code],
	}

	deployservice { $deployment_code:
		version         => $version,
		service		=> $service_code,	
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
	file{"${carbon_home}/repository/conf/appfactory":
		ensure =>directory,
		owner =>$owner,
		group=>$group,
		require =>  WSO2::InitializeDeployment[$deployment_code],
 	}	
	pushtemplates { 
		$service_templates: 
		target		=> $carbon_home,
		directory 	=> "${service_code}/${version}",
		require 	=> WSO2::DeployService[$deployment_code];

		$common_templates:
		target          => $carbon_home,
                directory       => "commons",
		require 	=> [WSO2::DeployService[$deployment_code],
				    File["${carbon_home}/repository/conf/appfactory"]],
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
}

