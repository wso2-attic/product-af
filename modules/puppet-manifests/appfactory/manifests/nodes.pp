stage { 'configure': require => Stage['main'] }
stage { 'deploy': require => Stage['configure'] }

node basenode {

  $package_repo       = 'http://192.168.18.250'
  $depsync_svn_repo   = 'https://svn.wso2.example.com/wso2/repo/'
  $local_package_dir  = '/mnt/packs'
  $deploy_new_packs   = 'true'
  $puppet_username    = $puppet_un

# The user’s password, in encrypted format
  $puppet_password   = $encrtpted_pw

  $owner				      = $puppet_username
  $group              = $puppet_username
######### port offsets(for devsetup) ###########

  $af_offset        = 0
  $bam_offset       = 3
  $jppserver_offset = 57
  $bps_offset       = 100
  $apim_offset      = 6
  $ss_offset        = 40
  $ues_offset       = 11
  $mb_offset        = 300
  $gitblit_offset   = 0
  $s2gitbit_offset  = 1
  $dev_paas_offset  = 20
  $test_paas_offset = 21
  $prod_paas_offset = 22
  $dev_greg_offset  = 30
  $test_greg_offset = 31
  $prod_greg_offset = 32

######## Ports #######
  $af_port          = 9443 + $af_offset
  $is_port          = $af_port
  $bps_port         = 9443 + $bps_offset
  $bam_port         = 9443 + $bam_offset
  $bam_tcp_port     = 7611 + $bam_offset
  $jenkins_port_old     = 9443 + $jppserver_offset
  $mb_port          = 9443 + $mb_offset
  $mb_tcp_port      = 5672 + $mb_offset
  $ues_port         = 9443 + $ues_offset
  $as_dev_port      = '9443'
  $as_test_port     = '9443'
  $as_prod_port     = '9443'
  $gitblit_port     = 8443 + $gitblit_offset
  $issue_port       = 9443 + $af_offset
  $ss_port          = 9443 + $ss_offset
  $apim_port        = 9443 + $apim_offset
  $task_port         = $af_port

  $dev_paas_port    = 9443 + $dev_paas_offset
  $dev_paas_mb_port = 61616 + $dev_paas_offset

  $test_paas_port   = 9443 + $test_paas_offset
  $test_paas_mb_port = 61616 + $test_paas_offset

  $prod_paas_port   = 9443 + $prod_paas_offset
  $prod_paas_mb_port = 61616 + $prod_paas_offset

  $dev_greg_port    = 9443 + $dev_greg_offset
  $test_greg_port    = 9443 + $test_greg_offset
  $prod_greg_port    = 9443 + $prod_greg_offset

  $ipaddress = $appfac_ip
  $mysql_server_1 = $ipaddress

# Jenkins Configs
  $jenkins_keystore_name = "/mnt/${ipaddress}/jenkins/security/wso2carbon.jks"
  $jenkins_keystore_password = 'wso2carbon'
  $jenkins_log_file_location = "/mnt/${ipaddress}/jenkins/logs"
  $jenkins_package_name = 'jenkins.war'
  $jenkins_domain = 'jenkins'
  $jenkins_port = '9500'
  $jenkins_home = "/mnt/${ipaddress}/jenkins/jenkins_home"
  $clientTrustStore_location = "/mnt/${ipaddress}/jenkins/security/client-truststore.jks"
  $clientrustStore_password = 'wso2carbon'
  $keyStore_location = "/mnt/${ipaddress}/jenkins/security"
  $pre_conf_mvn_repo = "/mnt/${ipaddress}/jenkins/preMvnRepo"
  $keyStorePassword =  'wso2carbon'
  $jenkins_admin_username   = "jenkinssystemadmin"
  $jenkins_admin_pasword    = "password"
  $jenkins_storagePath = "${jenkins_home}/jobs/\$TENANT_IDENTIFIER/storage"
  $jenkins_tempPath = "${jenkins_home}/jobs/\$TENANT_IDENTIFIER/temp"
  $jenkins_admin_fullname = 'jenkins admin'
  $jenkins_admin_api_token = 'm440RGgFw5VpPUCFZ6L1yICUCv2IhwAqTfY27R1GCsdXIvN5A2bfHn0tpSbbcrDi'
  $jenkins_admin_password_hash = '#jbcrypt:$2a$10$WuhaeQqp36TXkTbUWZLxiOUkfJabKS1Ex4tFNqoRlzpeXhK7hY3am'
  $jenkins_admin_email = 'jenkinsadmin@cloud.com'



############## Stratos DBS for Dev Setup #########################

##PPAAS - Docker
  $ppaas_registry_db_schema  = "ppaasregistry"
  $ppaas_userstore           = "ppaasuserstore"
  $ppaas_config_db_schema    = "PPAAS_CONFIG_DB"
  $ppaas_registry_db         = [$mysql_server_1,$ppaas_registry_db_schema,'root','root']
  $ppaas_userstore_db        = [$mysql_server_1, $ppaas_userstore,'root','root']
  $ppaas_config_db           = [$mysql_server_1, $ppaas_config_db_schema,'root','root']

## Dev ##
 # $dev_registry_db_schema  = "devregistry"
 # $dev_userstore           = "devuserstore"
  $dev_config_db_schema    = "DEV_CONFIG_DB"
 # $devpaas_database       = [$mysql_server_1,$dev_registry_db_schema,'root','root']
 # $dev_userstore_db         = [$mysql_server_1, $dev_userstore,'root','root']
  $dev_config_db         = [$mysql_server_1, $dev_config_db_schema,'root','root']

## Test ##
 # $test_registry_db_schema  = "testregistry"
 # $test_userstore           = "testuserstore"
  $test_config_db_schema    = "TEST_CONFIG_DB"
 # $testpaas_database       = [$mysql_server_1,$test_registry_db_schema,'root','root']
 #$test_userstore_db         = [$mysql_server_1, $test_userstore,'root','root']
  $test_config_db         = [$mysql_server_1, $test_config_db_schema,'root','root']

## Prod ##
  #$prod_registry_db_schema  = "prodregistry"
  #$prod_userstore           = "produserstore"
  $prod_config_db_schema    = "PROD_CONFIG_DB"
  #$prodpaas_database       = [$mysql_server_1,$prod_registry_db_schema,'root','root']
  #$prod_userstore_db         = [$mysql_server_1, $prod_userstore,'root','root']
  $prod_config_db         = [$mysql_server_1, $prod_config_db_schema,'root','root']

  $domain         = 'appfactory.private.wso2.com'
  $hosts_mapping  = [
    "192.168.18.250,puppet.${domain}",
    "$ipaddress,localhost",
    "$ipaddress,mysql1.${domain}",
    "$ipaddress,mysql2.${domain}",
    "$ipaddress,appfactoryelb.${domain}",
    "$ipaddress,ldap.${domain}",
    "$ipaddress,identity.${domain}",
    "$ipaddress,cloudmgt.${domain}",
    "$ipaddress,issuetracker.${domain}",
    "$ipaddress,ues.${domain}",
    "$ipaddress,apps.${domain}",
    "$ipaddress,${domain}",
    "$ipaddress,messaging.${domain}",
    "$ipaddress,process.${domain}",
    "$ipaddress,jenkins.${domain}",
    "$ipaddress,storage.${domain}",
    "$ipaddress,git.${domain}",
    "$ipaddress,s2git.${domain}",
    "$ipaddress,dashboards.${domain}",
    "$ipaddress,keymanager.apimanager.${domain}",
    "$ipaddress,gateway.apimanager.${domain}",
    "$ipaddress,apimanager.${domain}",
    "$ipaddress,bam.${domain}",
    "$ipaddress,receiver1.${domain}",
    "$ipaddress,node0.cassandra.${domain}",
    "$ipaddress,hadoop0.${domain}",
    "$ipaddress,sc.dev.${domain}",
    "$ipaddress,sc.test.${domain}",
    "$ipaddress,sc.prod.${domain}",
    "$ipaddress,sc.${domain}",
    "$ipaddress,paas.${domain}",
    "$ipaddress,cc.stratos.apache.org",
    "$ipaddress,as.stratos.apache.org",
    "$ipaddress,autoscaler.stratos.apache.org",
    "192.168.18.242,appserver.dev.${domain}",
    "192.168.18.244,appserver.test.${domain}",
    "192.168.18.246,appserver.${domain}",
    "$ipaddress,mysql-dev-01.${domain}",
    "$ipaddress,mysql-test-01.${domain}",
    "$ipaddress,mysql-prod-01.${domain}",
    "$ipaddress,gregserver.dev.${domain}",
    "$ipaddress,gregserver.test.${domain}",
    "$ipaddress,gregserver.prod.${domain}"
  ]

  include 'wso2base'
}

node confignode inherits basenode  {

  $packages = ['lsof','unzip','sysstat','telnet', 'git', 'less', 'tree']

# Service subdomains
  $wso2_env_domain      = $domain
  $af_subdomain         = 'apps'
  $as_subdomain         = 'appserver'
  $greg_subdomain       = 'gregserver'
  $as_dev_subdomain     = 'dev'
  $as_test_subdomain    = 'test'
  $as_prod_subdomain    = 'prod'
  $ss_subdomain         = 'storage'
  $is_subdomain         = $af_subdomain
  $ts_subdomain         = 'task'
  $am_subdomain         = 'apimanager'
  $mb_subdomain         = 'messaging'
  $bam_subdomain        = 'bam'
  $store_subdomain      = 'store'
  $pubstore_subdomain   = 'pubstore'
  $bps_subdomain        = 'process'
  $gateway_subdomain    = 'gateway'
  $publisher_subdomain  = 'publisher'
  $keymanager_subdomain = 'keymanager'
  $governance_subdomain = 'governance'
  $jenkins_subdomain    = 'jenkins'
  $cloudmgt_subdomain   = $af_subdomain
  $issue_subdomain      = 'issue-tracker'
  $analyzer_subdomain   = 'analyzer'
  $esb_subdomain        = 'esb'
  $task_subdomain       = $af_subdomain
  $ues_subdomain        = 'dashboards'

  $management_subdomain = 'management'

# MySQL server configuration details
  $mysql_port           = '3306'
  $max_connections      = '100000'
  $max_active           = '200'
  $max_wait             = '360000'
  $mysql_driver_file    = 'mysql-connector-java-5.1.29-bin.jar'

  $mysql_server_1       = "mysql1.${domain}"
  $mysql_server_2       = "mysql2.${domain}"

  $rss_dev_server_01    = "mysql-dev-01.${domain}"
  $rss_test_server_01   = "mysql-test-01.${domain}"
  $rss_prod_server_01   = "mysql-prod-01.${domain}"


# axis2 details
  $hazelcastClusteringAgentEnable = 'false'



# Database details

#Config database names
  $af_config_database_name           = 'dbAfConfig'
  $identity_config_database_name     = 'dbIdentityConfig'
  $cloud_mgt_config_database_name    = 'dbCloudMgtConfig'
  $bam_config_database_name          = 'dbBamConfig'
  $bps_config_database_name          = 'dbBpsConfig'
  $rss_mgt_config_database_name      = 'dbStorageServerConfig'
  $ts_config_database_name           = 'dbTaskConfig'
  $ues_config_database_name          = 'dbUesConfig'
  $apim_config_database_name         = 'dbAPIMConfig'

# config databases
  $config_database              = [$mysql_server_2,'','root','root']

  $af_config_database           = [$mysql_server_2,$af_config_database_name,'root','root']
  $identity_config_database     = [$mysql_server_2,$identity_config_database_name,'root','root']
  $cloud_mgt_config_database    = [$mysql_server_2,$cloud_mgt_config_database_name,'root','root']
  $bam_config_database          = [$mysql_server_2,$bam_config_database_name,'root','root']
  $bps_config_database          = [$mysql_server_2,$bps_config_database_name,'root','root']
  $rss_mgt_config_database      = [$mysql_server_2,$rss_mgt_config_database_name,'root','root']
  $ts_config_database           = [$mysql_server_2,$ts_config_database_name,'root','root']
  $ues_config_database          = [$mysql_server_2,$ues_config_database_name,'root','root']
  $apimgt_config_database       = [$mysql_server_1,$apim_config_database_name,'root','root']

# registry databases
  $userstore_database       = [$mysql_server_1,'dbUserstore','root','root']
  $registry_database        = [$mysql_server_1,'dbGovernanceCloud','root','root']
  $identity_database        = [$mysql_server_1,'dbIdentity','root','root']
  $cloud_mgt_database       = [$mysql_server_1,'dbCloudMgt','root','root']
  $issuetracker_database    = [$mysql_server_1,'dbIssueTracker','root','root']
  $af_database              = [$mysql_server_1,'afdb', 'root', 'root']
  $bps_database             = [$mysql_server_1,'dbBps','root','root'] # This is the BPS database
  $rss_mgt_database         = [$mysql_server_1,'dbRssMgt','root','root']

#API Mgt databases
  $apimgt_database          = [$mysql_server_1,'dbApiMgt','ApiMgtUser','root']
  $apistats_database        = [$mysql_server_1,'dbApiStats','ApiStatsUser','root']


# RSS databases
# RSS database user details
  $rss_database_user        = 'cloudrssadmin'
  $rss_database_password    = 'password'

  $rss_dev_database_01      = [$rss_dev_server_01,'',$rss_database_user,$rss_database_password]
  $rss_test_database_01     = [$rss_test_server_01,'',$rss_database_user,$rss_database_password]
  $rss_prod_database_01     = [$rss_prod_server_01,'',$rss_database_user,$rss_database_password]

# BAM stat DB
  $dbApimStats              = 'dbApimStats'
  $dbAfStats                = 'dbAfStats'
  $dbLoginAnalytics         = 'dbLoginAnalytics'
  $bamStatUser              = 'BamStatUser'
  $bamStatUserPw            = 'password'
  $bam_apim_stat_db         = [$mysql_server_1,$dbApimStats,$bamStatUser,$bamStatUserPw]
  $bam_af_stat_db           = [$mysql_server_1,$dbAfStats,$bamStatUser,$bamStatUserPw]
  $login_analytics_db       = [$mysql_server_1,$dbLoginAnalytics,$bamStatUser,$bamStatUserPw]

# User MGT
  $usrmgt_username        = 'admin'
  $usrmgt_password        = 'admin'
  $bps_user               = 'bps'
  $bps_password           = 'root'
  $cloud_mgt_user         = 'cloud'
  $cloud_mgt_password     = 'root'
  $issue_tracker_password = 'root'
  $issue_tracker_version  = "2.2.0-SNAPSHOT"

  $bpel_mysql_username   = 's2user'
  $bpel_mysql_password   = 'password'

# Deployment Synchronizer
  $repository_type       = 'svn'
  $svn_user              = 'wso2'
  $svn_password          = 'password'

# Auto-scaler
  $keystore_password        = 'wso2carbon'
  $auto_scaler_epr          = 'http://xxx:9863/services/AutoscalerService/'
  $auto_scaler_task_interval= '60000'
  $server_startup_delay     = '180000'

## Git related
  $git_version            ='1.4.1'
  $git_home               ="/mnt/git/gitblit-${git_version}"
  $source_control_git_admin_username = "gitblitsystemadmin"
  $source_control_git_admin_password = "admin"

  $gitblit_realm_userService = "org.wso2.carbon.appfactory.gitblit.oauth.oauth2.AppFactoryGitBlitAuthenticationProvider"
  $gitblit_realm_authenticationProvider = "org.wso2.carbon.appfactory.gitblit.oauth.oauth2.AppFactoryGitBlitAuthenticationProvider"

## Appfactory
  $af_db_name         = 'WSO2AppfactoryDB'
  $adminuser_name       = $usrmgt_username
  $adminuser_passwd     = $usrmgt_password
  $app_creation_delay   = '10000'
  $af_registration_link = "https://${af_subdomain}.${wso2_env_domain}:${af_port}/cloudmgt/site/pages/register.jag"
  $af_identity_provider = "https://${af_subdomain}.${wso2_env_domain}/samlsso"
  $af_keystore_passwd   = 'wso2carbon'
  $af_keystore_alias    = 'wso2carbon'
  $archetype_version    = "2.2.0-SNAPSHOT"

  $af_keystore          = 'repository/resources/security/wso2carbon.jks'
  $jenkins_resource_uid = 'admin'
  $jenkins_apitoken     = '11111'
  $redmine_admin_uname  = 'admin'
  $redmine_admin_passwd = 'admin'
  $jpadb_user           = 'jpadb'
  $jpadb_passwd         = 'password'
  $truststore_passwd    = 'wso2carbon'
  $s2_enabled           = true

#Not used. check and remove
################################################
  $as_cartridge_dev_alias     = 'appserverdev'
  $as_cartridge_dev_type      = 'appserverdev'
  $as_cartridge_test_alias    = 'appservertest'
  $as_cartridge_test_type     = 'appservertest'
  $cartridge_staging_alias    = 'asstaging'
  $cartridge_staging_type     = 'asstaging'
  $cartridge_prod_alias       = 'asprod'
  $cartridge_prod_type        = 'asprod'
###################################################

  $appfactory_tenant_mgt_service_epr= "sc.${wso2_env_domain}"
  $jenkins_admin_user         = "jenkinssystemadmin"
  $jenkins_admin_password     = "password"

#AF stratos cartridge information
  $deployment_policy    = "af-deployment"
  $autoscale_policy     = "economy"

  $appserver_cartridge_alias_prefix      = "as"
  $appserver_cartridge_type_prefix       = "${dev_id}as"

#Dev
  $dev_cartridge_alias      = "asdevelopment"
  $dev_cartridge_type       = "${dev_id}asdevelopment"
  $php_dev_cartridge_alias  = "{@appName}"
  $php_dev_cartridge_type  = "php"

#Test
  $test_cartridge_alias     = "astesting"
  $test_cartridge_type      = "${dev_id}astesting"
  $php_test_cartridge_alias  = "{@appName}"
  $php_test_cartridge_type  = "php"

#Prod
  $prod_cartridge_alias     = "asproduction"
  $prod_cartridge_type      = "${dev_id}asproduction"
  $php_prod_cartridge_alias  = "{@appName}"
  $php_prod_cartridge_type  = "php"

# AF domain mapping
  $allow_dev_domainmapping  = 'false'
  $allow_test_domainmapping = 'false'
  $allow_prod_domainmapping = 'false'

  $domainmapping_stratos_dev_url  = 'http://dev.lb.cloudapps.com'
  $domainmapping_stratos_test_url = 'http://test.lb.cloudapps.com'
  $domainmapping_stratos_prod_url = 'http://prod.lb.cloudapps.com'

  $domainmapping_domain         = 'example.com'
  $domainmapping_allowed_stage  = 'Production'
  $stratos_lb_url             = 'test.lb.cloudtest.com'
  $domain_mapping_default_context = 'webapps/defaultmapping'

  $enable_route53               = 'false'
  $route53_access_key_id        = 'xxxxxxxxxxxxxxxxxxxxxxxx'
  $route53_secret_access_key    = 'xxxxxxxxxxxxxxxxxxxxxxxx'
  $route53_hosted_zone_id       = 'xxxxxxxxxxxxxxxxxxxxxxxx'

# API Manager
$am_nio_http_port     = "8286"
$am_nio_https_port    = "8249"
$apimgt_db_user       = "apimgt"
$apimgt_db_passwd     = "root"
$apimgt_port          = "9443" # we put this because for minimal deployment we use an standalone api manager
$apimgt_http_port     = "9769" # we put this because for minimal deployment we use an standalone api manager

# Cassandra details
  $css0_subdomain       = "node0.cassandra"
  $css1_subdomain       = "node1.cassandra"
  $css2_subdomain       = "node2.cassandra"
  $cassandra_cluster    = ["${css0_subdomain}.${wso2_env_domain}"]
  $css_cluster_name     = "AdminCluster"
  $css_port_unoffset    = 9160
  $css_port             = $css_port_unoffset + $bam_offset
  $css_mgt_port         = 9443 + $bam_offset
  $cassandra_username   = $usrmgt_username
  $cassandra_password   = $usrmgt_password
  $css_replication_factor = "3"
  $hive_database        = "dbHiveMeta"
  $hive_user            = "HiveMetaUser"
  $hive_password        = "password"
  $hive_db         = [$mysql_server_1,$hive_database,$hive_user,$hive_password]
  $task_server_count   = 2
  $task_server_mode     = "AUTO"

  $hector_nodes         = ["${css0_subdomain}.${wso2_env_domain}:${css_port}"]
  $hadoop_embedded_local_mode =  true
# LDAP settings
  $app_base               = 'dc=appfactory,dc=wso2,dc=com'
  $usrmgt_connectionname  = "uid=admin,ou=system"
  $usrmgt_connectionpasswd= "admin"
  $usrmgt_class           = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager"
  $usrmgt_RO_class        = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager"
  $usrmgt_usb             = "ou=Users,${app_base}"
  $usrmgt_unsf            = "(&amp;(objectClass=person)(uid=?))"
  $usrmgt_una             = "uid"
  $usrmgt_gsb             = "ou=Groups,${app_base}"
  $usrmgt_udnp            = "uid={0},ou=Users,${app_base}"
  $usrmgt_eoc             = "wso2Person"
  $usrmgt_unf             = "(objectClass=person)"

#Embedded LDAP settings
  $default_partition_realm = "appfactory.wso2.com"

#Tenant Manager
  $tenant_manager_class       = "org.wso2.carbon.user.core.tenant.CommonHybridLDAPTenantManager"
  $tenant_realm_builder_class = "org.wso2.carbon.user.core.config.multitenancy.CommonLDAPRealmConfigBuilder"

# LOGEVENT configurations
  $receiver_url            = "receiver.${wso2_env_domain}"
  $receiver1_url            = "receiver1.${wso2_env_domain}"
  $receiver2_url            = "receiver2.${wso2_env_domain}"
  $receiver3_url            = "receiver3.${wso2_env_domain}"
  $receiver_port            = '7611'
  $receiver_secure_port     = '7711'
  $receiver_username        = $usrmgt_username
  $receiver_password        = $usrmgt_password

# This user name and password is used in messageBroker andes-virtualhosts.xml to connect to cassandra cluser.
# MB will be using its own cassandra.
# For dev setups we should be using a single cassandra cluster.
  $message_broker_username  = $usrmgt_username
  $message_broker_password  = $usrmgt_password


# Server details for billing
  $time_zone            = "GMT-8:00"

# s2gitblit confiugeration
  $s2gitbit_http_port   = "0"
  $s2gitbit_https_port  = 8443 + $s2gitbit_offset
  $s2gitblit_server     = "s2git.${wso2_env_domain}"
  $s2gitblit_https_port_enabled =false
  $adc_server_domain    = "sc.${wso2_env_domain}"
  $stratos_dev_server   = "sc.dev.${wso2_env_domain}"
  $stratos_test_server  = "sc.test.${wso2_env_domain}"
  $stratos_prod_server  = "sc.prod.${wso2_env_domain}"
  $stratos_dev_server_port = $dev_paas_port
  $stratos_test_server_port = $test_paas_port
  $stratos_prod_server_port = $prod_paas_port
  $adc_server_port      = '9463'
  $s2gitblit_server_port= '8444'
  $s2git_adminusername  = 'admin'
  $s2git_adminpassword  = 'admin'


# Datasource Passwords
  $carbon_db_password   = 'wso2carbon'

# AM Master Datasource Passwords
  $reg_db_password              = "password"
  $am_db_password               = "password"
  $am_config_registry_password  = "amconfi123"
  $am_stat_db_password          = "wso2carbon"

# Datasource Passwords
  $apistore_keystore_password     = "wso2carbon"
  $apistore_keystore_alias        = "wso2carbon"
  $apipublisher_keystore_password = "wso2carbon"
  $apipublisher_keystore_alias    = "wso2carbon"

## Catalina Thread configrations
## AM
  $am_accept_thread_count = "2"
  $am_max_threads         = "250"
  $am_min_spare_threads   = "50"
## CG
  $cg_accept_thread_count = "200"
  $cg_max_threads         = "250"
  $cg_min_spare_threads   = "250"
## Manager
  $mgr_accept_thread_count= "200"
  $mgr_max_threads        = "250"
  $mgr_min_spare_threads  = "250"
## CEP
  $cep_accept_thread_count= "200"
  $cep_max_threads        = "250"
  $cep_min_spare_threads  = "250"
## MB
  $mb_accept_thread_count = "200"
  $mb_max_threads         = "250"
  $mb_min_spare_threads   = "250"
## AS
  $as_accept_thread_count = "2"
  $as_max_threads         = "750"
  $as_min_spare_threads   = "150"
## GREG
  $greg_accept_thread_count = "2"
  $greg_max_threads         = "750"
  $greg_min_spare_threads   = "150"
## SS
  $ss_accept_thread_count = "2"
  $ss_max_threads         = "250"
  $ss_min_spare_threads   = "50"

## Hadoop details
  $hdfs_port              = "9000"
  $hdfs_job_tracker_port  = "9001"
  $hdfs_url               = "hadoop0"
  $hadoop1_subdomain      = "hadoop1"
  $hadoop2_subdomain      = "hadoop2"
  $dfs_replication        = "1"
  $hadoop_heapsize        = "1024"

## Storage Server
  $ss_host                = "storage.${wso2_env_domain}"
  $ss_port                = "9483"

## mail server configurations
  $mailto_smtp_from   = $domain
  $mailto_smtp_user   = 'user'
  $mailto_smtp_passwd = 'password'
  $mailto_smtp_host   = 'mail.${domain}'
  $mailto_smtp_port   = '25'
  $mailto_smtp_tls    = 'false'
  $mailto_smtp_auth   = 'true'

## Private paas configurations
  $stratos_version    = "4.1.1"
  $stratos_domain     = "paas.${wso2_env_domain}"

#Private paas IAAS configuration
$iaas                 = "kubernetes"
$iaas_region          = "RegionOne"
$iaas_cartridge_image = "34dd924f-ef3d-49ae-884a-4784f0330f1b"
$iaas_instance_flavour= "7"


  $os_identity          = "openstackDemo:admin"
  $os_credentials       = "password"
  $os_jclouds_endpoint  = "http://appfactorycloud.private.wso2.com:5000/v2.0"
  $os_keypair_name      = "appfackey"
  $os_security_groups   = "default"

#Private paas mysql configuration
  $ppaas_mysql_host     = $ipaddress
  $ppaas_mysql_port     = "3306"
  $ppaas_mysql_uname    = "root"
  $ppaas_mysql_password = "root"

#### private paas application server node configs ##########
#### [<as_stage> , <server_key>, <as_stage_prefix>, <mysql_server> <mb_ip> <mb_port> <userstore> <registry>]
  #$ppaas_as_nodes = {
  #  "$dev_cartridge_type"  => ['Development','DEV_AS','dev', $ipaddress, $ipaddress,$dev_paas_mb_port, $dev_userstore, $dev_registry_db_schema],
  #  "$test_cartridge_type" => ['Testing','TEST_AS','test', $ipaddress, $ipaddress, $test_paas_mb_port, $test_userstore, $test_registry_db_schema],
  #  "$prod_cartridge_type" => ['Production','PROD_AS','prod', $ipaddress, $ipaddress, $prod_paas_mb_port, $prod_userstore, $prod_registry_db_schema]
  #}


#Private paas basenode configs
  $ppaas_mb_ip            = $ipaddress #Need to configure for each environment
  $ppaas_cep_ip           = $ipaddress #Need to configure for each environment
  $ppaas_cep_user_name    = 'admin'
  $ppaas_cep_password     = 'password'
  $ppaas_idp_hostname		  = "${is_subdomain}.${domain}"
  $ppaas_appfactory_host	= "${af_subdomain}.${domain}"
  $ppaas_s2git_server		  = $s2gitblit_server
  $ppaas_s2git_server_port= $s2gitblit_server_port
  $ppaas_s2git_username	  = $s2git_adminusername
  $ppaas_s2git_password	  = $s2git_adminpassword

# Private paas puppet-master configurations
  $ppaas_puppetmaster_ip = $ipaddress
  $ppaas_puppetmaster_host = "puppet"

##AS
  $carbonDatasourceRepositoryClass='org.wso2.carbon.appfactory.ext.datasource.ApplicationAwareDataSourceRepository'
  $defaultInitialContextFactory='org.wso2.carbon.appfactory.ext.jndi.ApplicationAwareCarbonJavaURLContextFactory'
  $carbonInitialJNDIContextFactory='org.wso2.carbon.appfactory.ext.jndi.ApplicationAwareCarbonInitialJNDIContextFactory'
}


##############################
###Cleaning Dev Setup node ###
##############################

node /clean.devsetup/ inherits confignode {

  $databases = [ 'afdb', $af_config_database_name, 'dbUserstore', 'dbGovernanceCloud', 'userstore', 'registry',
    'bps_jpa_db', 'rss_mgt', 'af_config', 'identity_config', 'cloud_mgt_config','dbBps','dbRssMgt',
    'dbApiMgt','dbApiStats', 'identity', 'cloud_mgt', 'issuetracker', 'appfactory',
    'bam_config', 'bps_config', 'rss_mgt_config', 'ts_config', 'ues_config', 'apim_config','dbIssueTracker',
    'apimdb', 'StratosStats', 'config', 'sm_config', 'as_config','dbIdentity','dbCloudMgt',
    $bps_config_database_name,$identity_config_database_name,$cloud_mgt_config_database_name,
    $bam_config_database_name,$rss_mgt_config_database_name,
    $ts_config_database_name,$ues_config_database_name,$apim_config_database_name,
    $dbApimStats,$dbAfStats,$dbLoginAnalytics,$hive_database,
    #$dev_registry_db_schema, $dev_userstore,
    $dev_config_db_schema,
    #$test_registry_db_schema,$test_userstore,
    $test_config_db_schema,
    #$prod_registry_db_schema,$prod_userstore,
    $prod_config_db_schema,
    $ppaas_registry_db_schema,$ppaas_userstore, $ppaas_config_db_schema
  ]

  class {"mysql::clean":
    databases => $databases,
  }

}

################################
###Backing up Dev Setup node ###
################################
node /backup.devsetup/ inherits confignode {

  $databases = [ 'afdb', $af_config_database_name, 'dbUserstore', 'dbGovernanceCloud', 'userstore', 'registry',
    'bps_jpa_db', 'rss_mgt', 'af_config', 'identity_config', 'cloud_mgt_config','dbBps','dbRssMgt',
    'dbApiMgt','dbApiStats', 'identity', 'cloud_mgt', 'issuetracker', 'appfactory',
    'bam_config', 'bps_config', 'rss_mgt_config', 'ts_config', 'ues_config', 'apim_config','dbIssueTracker',
    'apimdb', 'StratosStats', 'config', 'sm_config', 'as_config','dbIdentity','dbCloudMgt',
    $bps_config_database_name,$identity_config_database_name,$cloud_mgt_config_database_name,
    $bam_config_database_name,$rss_mgt_config_database_name,
    $ts_config_database_name,$ues_config_database_name,$apim_config_database_name,
    $dbApimStats,$dbAfStats,$dbLoginAnalytics,$hive_database,
    #$dev_registry_db_schema, $dev_userstore,
    $dev_config_db_schema,
    #$test_registry_db_schema,$test_userstore,
    $test_config_db_schema,
    #$prod_registry_db_schema,$prod_userstore,
    $prod_config_db_schema,
    $ppaas_registry_db_schema,$ppaas_userstore, $ppaas_config_db_schema
  ]

  class {"mysql::backup":
    databases => $databases,
    dump_dir => '/mnt/backups/mysqldump'
  }

}


##############################
####### Database Setup #######
##############################

node /mysql/ inherits confignode{
#Can improve this when we change the data structure
#All registry related databases are created from here
  $database_configs = {userstore => {config => $userstore_database, script_name => "mysql"},
    registry =>  {config => $registry_database, script_name => "mysql"},
    identity =>  {config => $identity_database, script_name => "mysql"},
    cloud_mgt =>  {config => $cloud_mgt_database, script_name => "wso2_cloud_mgt"},
    issuetracker =>  {config => $issuetracker_database, script_name => "wso2_issue_tracker_mysql"},
    appfactory => {config => $af_database, script_name => "afdb_mysql"},
    bps_jpa_db =>  {config => $bps_database, script_name => "wso2_bps_jpadb_mysql"},
    rss_mgt =>  {config => $rss_mgt_database, script_name => "wso2_rss_mysql"},
    af_config =>  {config => $af_config_database, script_name => "mysql"},
    identity_config =>  {config => $identity_config_database, script_name => "mysql"},
    cloud_mgt_config =>  {config => $cloud_mgt_config_database, script_name => "mysql"},
    bam_config =>  {config => $bam_config_database, script_name => "mysql"},
    bps_config =>  {config => $bps_config_database, script_name => "mysql"},
    rss_mgt_config =>  {config => $rss_mgt_config_database, script_name => "mysql"},
    ts_config =>  {config => $ts_config_database, script_name => "mysql"},
    ues_config =>  {config => $ues_config_database, script_name => "mysql"},
    apim_config =>  {config => $apimgt_config_database, script_name => "mysql"},
    apimdb =>  {config => $apimgt_database, script_name => "apim-mysql"},
    #devregistry =>  {config => $devpaas_database, script_name => "mysql"},
    #devuser_store =>  {config => $dev_userstore_db, script_name =>"mysql" },
    devconfigdb =>  {config => $dev_config_db, script_name => "mysql"},
    #testregistry =>  {config => $testpaas_database, script_name => "mysql"},
    #testuser_store =>  {config => $test_userstore_db, script_name => "mysql"},
    testconfigdb =>  {config => $test_config_db, script_name => "mysql"},
    #prodregistry =>  {config => $prodpaas_database, script_name => "mysql"},
    #produser_store =>  {config => $prod_userstore_db, script_name => "mysql"},
    prodconfigdb =>  {config => $prod_config_db, script_name => "mysql"},
    ppaasregistry =>  {config => $ppaas_registry_db, script_name => "mysql"},
    ppaasuser_store =>  {config => $ppaas_userstore_db, script_name => "mysql"},
    ppaasconfigdb =>  {config => $ppaas_config_db, script_name => "mysql"},
    dbApimStats =>  {config => $bam_apim_stat_db, script_name => undef},
    dbAfStats =>  {config => $bam_af_stat_db, script_name => undef},
    dbLoginAnalytics =>  {config => $login_analytics_db, script_name => undef},
    dbHiveMeta =>  {config => $hive_db, script_name => undef}
  #apim_stat =>  {config => $apistats_database, script_name => undef}
  }
  class {"mysql::database":
    base_dir => "/mnt/${server_ip}",
    configs => $database_configs,
  }
}


##############################
###### Production Setup ######
##############################

node /appfactoryelb/ inherits confignode {
  $server_ip= $ipaddress

  class {"elb":
  #service,tenant range,worker/mgt/all,group management port
    services           =>  [
      "apps,*,*,4010,,,base",
      "process,*,*,4020,,,base",
      "jenkins,*,*,4030,,,base",
      "storage,*,mgt,4040,,,base",
      "identity,*,*,4050,,,base",
      "monitor,*,*,4060,,,base"
    ],
    version            => "2.1.0",
    maintenance_mode   => true,
    auto_scaler        => false,
    auto_failover      => false,
    owner              => "root",
    group              => "root",
    cluster_domain     => "elb",
    elbtype		         => "appfactory",
    localmember_host   => "appfactoryelb.${wso2_env_domain}",
  #members            => {"appfactoryelb2.${wso2_env_domain}" => '4000'},
    target             => "/mnt/${server_ip}",
    stage              => "deploy",
  }
}



node /identity/ inherits confignode {
  $server_ip= $ipaddress

  class {"identity":
    version            => "4.5.0",
    offset             => 0,
    localmember_port   => 4000,
    clustering         => 'false',
    maintenance_mode   => 'refresh',
    owner              => $owner,
    group              => $owner,
    sub_cluster_domain => "mgt",
    config_db          => $identity_config_database_name,
    members            => {
      "appfactoryelb.${wso2_env_domain}" => '4050'
    },
    target             => "/mnt/${server_ip}",
    stage              => "deploy",
    dep_sync_enabled   => "false",
    logging_enabled    => "false"
  }
}

## appfactory
node /appfactory/ inherits confignode {
include 'wso2base::maven'
$server_ip= $ipaddress

class { "appfactory":
  version            => "2.2.0-SNAPSHOT",
  offset             => 0,
  localmember_port   => 4000,
  clustering         => 'false',
  maintenance_mode   => 'refresh',
  owner              => $owner,
  group              => $group,
  sub_cluster_domain => "mgt",
  config_db          => $af_config_database_name,
  members            => {
  "appfactoryelb.${wso2_env_domain}" => '4010'
  },
  target             => "/mnt/${server_ip}/appfactory",
  stage              => "deploy",
  file_ignore_filter => "local",
  s2gitblit_server   => $s2gitblit_server
}
}


node /messagebroker/ inherits confignode {
  $server_ip= $ipaddress

  class { 'messagebroker':
    version            => '2.2.0',
    offset             => $mb_offset,
    maintenance_mode   => true,
    owner              => $owner,
    group              => $group,
    target             => "/mnt/${ipaddress}/mb",
    stage              => "deploy",
  }
}

node /jenkins/ inherits confignode {
  $server_ip= $ipaddress

  class { 'jenkins':
    user     => $owner,
    group    => $group,
    base_dir => "/mnt/${server_ip}",
  }


}


node /jppserver/ inherits confignode {
  include 'wso2base::maven'
  $server_ip= $ipaddress

  class { 'jppserver':
    version            => '5.2.1',
    owner              => $owner,
    group              => $group,
    maintenance_mode   => 'refresh',
    offset             => $jppserver_offset,
    target             => "/mnt/${server_ip}/buildserver",
    members            => {
      "appfactoryelb.${wso2_env_domain}" => '4030'
    }
  }
}

node /bps/ inherits confignode {
  $server_ip= $ipaddress

  class { 'bps':
    version             => '3.2.0',
    localmember_port    => 4000,
    owner               => $owner,
    group               => $group,
    config_db           => $bps_config_database_name,
    target              => "/mnt/${server_ip}/bps",
    stage               => "deploy",
    offset              => $bps_offset,
    members             => {
      "appfactoryelb.${wso2_env_domain}" => '4020'
    },
    dep_sync_enabled   => "false",
    logging_enabled    => "false",
    membership_scheme  => 'multicast'
  }
}

node /dashboards/ inherits confignode {
  $server_ip= $ipaddress

  class { "ues":
    version            => "1.1.0",
    offset             => $ues_offset,
    localmember_port   => 4000,
    clustering         => 'false',
    maintenance_mode   => true,
    owner              => $owner,
    group              => $group,
    sub_cluster_domain => "mgt",
    config_db          => $ues_config_database_name ,
    members            => false,
    target             => "/mnt/${server_ip}/ues",
    stage              => "deploy",
  }
}

node /s2gitblit/ inherits confignode {
  $server_ip= $ipaddress

  class { 's2gitblit':
    version => $git_version,
    user    => $owner,
    group   =>  $group,
    base_dir => "/mnt/${server_ip}",
    server_ip => $ipaddress # If this is not in the same servers
  }
  class {'mysql':
    user      => $rss_database_user,
    password  => $rss_database_password
  }
}

node /gitblit/ inherits confignode {
  $server_ip= $ipaddress

  class { 'gitblit':
    version  => $git_version,
    user     => $owner,
    group    => $group,
    base_dir => "/mnt/${server_ip}",
  }
  class {'mysql':
    user      => $rss_database_user,
    password  => $rss_database_password
  }
}


node /storage/ inherits confignode {
  $server_ip= $ipaddress
  class {'mysql':
    user      => $rss_database_user,
    password  => $rss_database_password
  }

  class {"storage":
    version            => "1.1.0",
    offset             => $ss_offset,
    localmember_port   => 4000,
    clustering         => 'false',
    maintenance_mode   => true,
    owner              => $owner,
    group              => $group,
    sub_cluster_domain => "worker",
    config_db          => $rss_mgt_config_database_name,
    members            => {
      "appfactoryelb.${wso2_env_domain}" => '4040'
    },
    target             => "/mnt/${server_ip}/ss",
    stage              => "deploy",
  }

  Class["mysql"] ~> Class["storage"]
}



node /api-manager/ inherits confignode {
$server_ip= $ipaddress

class {"apimanager":
  version            => "1.9.0",
  offset             => $apim_offset,
  localmember_port   => 4000,
  clustering         => false,
  maintenance_mode   => true,
  owner              => $owner,
  group              => $group,
  sub_cluster_domain => "mgt",
  config_db          => 'dbAPIMConfig',
  members            => {
  },
  target             => "/mnt/${server_ip}/api-manager",
  stage              => "deploy",
  amtype             => "apimanager"
 }
}

node /dev_greg/ inherits confignode {
$server_ip= $ipaddress

class {"greg":
  version            => "4.6.0",
  offset             => $dev_greg_offset,
  localmember_port   => 4000,
  clustering         => false,
  maintenance_mode   => true,
  owner              => $owner,
  group              => $group,
  sub_cluster_domain => "mgt",
  registry_db_schema => $dev_registry_db_schema,
  user_store         => $dev_userstore,
  config_db_schema   => $dev_config_db_schema,
  stage_subdomain    => "dev",
  greg_stage         => "Development",
  members            => {},
  target             => "/mnt/${server_ip}/dev_greg",
  stage              => "deploy"
 }
}

node /test_greg/ inherits confignode {
$server_ip= $ipaddress

class {"greg":
  version            => "4.6.0",
  offset             => $test_greg_offset,
  localmember_port   => 4000,
  clustering         => false,
  maintenance_mode   => true,
  owner              => $owner,
  group              => $group,
  sub_cluster_domain => "mgt",
  registry_db_schema => $test_registry_db_schema,
  user_store         => $test_userstore,
  config_db_schema   => $test_config_db_schema,
  greg_stage         => "Testing",
  stage_subdomain    => "test",
  members            => {
  },
target             => "/mnt/${server_ip}/test_greg",
stage              => "deploy"
}
}

node /prod_greg/ inherits confignode {
$server_ip= $ipaddress

class {"greg":
  version            => "4.6.0",
  offset             => $prod_greg_offset,
  localmember_port   => 4000,
  clustering         => false,
  maintenance_mode   => true,
  owner              => $owner,
  group              => $group,
  sub_cluster_domain => "mgt",
  registry_db_schema => $prod_registry_db_schema,
  user_store         => $prod_userstore,
  config_db_schema   => $prod_config_db_schema,
  greg_stage         => "Production",
  stage_subdomain    => "prod",
  members            => {
  },
target             => "/mnt/${server_ip}/prod_greg",
stage              => "deploy"
}
}

node /bam/ inherits confignode {
  $server_ip = $ipaddress

  class { "bam":
    version            => "2.4.1",
    offset             => $bam_offset,
    hazelcast_port     => 4000,
    config_db          => $bam_config_database_name,
    maintenance_mode   => 'refresh',
    depsync            => true,
    sub_cluster_domain => "mgt",
    clustering         => false,
    cloud              => false,
    members            => { "appfactoryelb.${wso2_env_domain}" => '4060'
    },
    owner              => $owner,
    group              => $group,
    target             => "/mnt/${server_ip}/bam",
    membership_scheme  => 'multicast'
  }
}

node /paaspuppet/ inherits confignode {

  class { "paaspuppet":
    maintenance_mode   => true,
    owner              => root,
    group              => root,
    target             => "/etc/puppet",
    ppaas_mb_ip        => "puppet",
    ppaas_cep_ip       => "puppet",
    ppaas_mysql_host   => "puppet"
  }
}


node /ppaas/ inherits confignode {
  $server_ip = $ipaddress

  class { "privatepaas":
    iaas_provider      => $iaas,
    maintenance_mode   => 'refresh',
    owner              => $owner,
    group              => $group,
    target             => "/mnt/${server_ip}/ppaas",
    ppaas_mysql_host   => $ppaas_mysql_host,
    offset             => $dev_paas_offset,
    registry_db_schema => $ppaas_registry_db_schema,
    user_store         => $ppaas_userstore,
    config_db_schema   => $ppaas_config_db_schema
  }
}


#########################################
###### END of the production setup ######
#########################################
