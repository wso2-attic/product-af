================================================================================
                        WSO2 App Factory 2.1.0
================================================================================

Welcome to WSO2 AppFactory 2.1.0 release


About AppFactory
=================================
WSO2 App Factory is a multi-tenant, elastic and self-service enterprise platform that enables multiple project teams to collaboratively create, run and manage enterprise applications. Combining complete application lifecycle management and Platform-as-a-Service (PaaS) capabilities, WSO2 App Factory makes it possible to develop, test, deploy to production and retire applications with a single click. It also provides an easy way to discover and consume apps and APIs through a user-friendly storefront.


Installation & Running
==================================
1. The latest distribution is available to download at https://svn.wso2.org/repos/wso2/scratch/appfactorycc_tags/2.1.0-beta/packs/
2. Puppet manifests for installing App Factory can be found at https://github.com/wso2/product-af/tree/2.1.0-Beta/modules/puppet-manifests
3. Latest documentation is at https://docs.wso2.com/display/AF210/WSO2+App+Factory+Documentation


WSO2 AppFactory 2.1.0 distribution directory structure
=============================================

	CARBON_HOME
        |-- bin <folder>
        |-- dbscripts <folder>
        |-- lib <folder>
        |-- repository <folder>
        |   |-- components <folder>
        |   |-- conf <folder>
        |   |-- data <folder>
        |   |-- database <folder>
        |   |-- deployment <folder>
        |   |-- lib <folder>
        |   |-- logs <folder>
        |   |-- resources <folder> 
        |   |   `-- security <folder>
        |   `-- tenants <folder>
        |-- tmp <folder>
        |-- LICENSE.txt <file>
        |-- INSTALL.txt <file>
        `-- README.txt <file>

    - bin
	  Contains various scripts .sh & .bat scripts

    - dbscripts
	  Contains all the database scripts 

	- lib
	  Contains the basic set of libraries required to startup AppFactory
	  in standalone mode
	
	- repository
	  The repository where services and modules deployed in WSO2 AppFactory
	  are stored. In addition to this the components directory inside the
	  repository directory contains the carbon runtime and the user added
	  jar files including mediators third party libraries and so on..

	- conf
	  Contains configuration files

	- database
      	  Contains the database
   
	- logs
	  Contains all log files created during execution

	- resources
	  Contains additional resources that may be required.

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 Appfactory is distributed.

    - INSTALL.txt
      	  This document will contain information on installing WSO2 AppFactory

	- README.txt
	  This document.

