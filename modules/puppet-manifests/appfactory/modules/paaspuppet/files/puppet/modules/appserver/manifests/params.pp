# ----------------------------------------------------------------------------
#  Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
# Class appserver::params
#
# This class manages appserver parameters
#
# Parameters:
#
# Usage: Uncomment the variable and assign a value to override the wso2.pp value
#
#

class appserver::params {
  #$package_repo         = 'http://ec2-54-251-18-7.ap-southeast-1.compute.amazonaws.com'
  $depsync_svn_repo     = 'https://svn.appfactory.domain.com/wso2/repo/'
  $local_package_dir    = '/mnt/packs'

  # Service subdomains
  $domain               = 'wso2.com'
  $as_subdomain         = 'appserver'
  $management_subdomain = 'management'

  $admin_username       = 'admin'
  $admin_password       = 'admin'

  # MySQL server configuration details
  #$mysql_server         = 'ec2-54-254-43-232.ap-southeast-1.compute.amazonaws.com'
  #$mysql_port           = '3306'
  #$max_connections      = '100000'
  #$max_active           = '150'
  #$max_wait             = '360000'

  # Database details
  $registry_user        = 'root'
  $registry_password    = 'root'
  #$registry_database    = 'registry'

  $userstore_user       = 'root'
  $userstore_password   = 'root'
  #$userstore_database   = 'userstore'

  # Depsync settings
  $svn_user             = 'wso2'
  $svn_password         = 'wso2123'

  # Auto-scaler
  $auto_scaler_epr      = 'http://xxx:9863/services/AutoscalerService/'

  #LDAP settings 
  $ldap_connection_uri      = 'ldap://ldap.appfactory.wso2.com:10389'
  $bind_dn                  = 'uid=admin,ou=system'
  $bind_dn_password         = 'admin'
  $user_search_base         = 'ou=system'
  $group_search_base        = 'ou=system'
  $sharedgroup_search_base  = 'ou=SharedGroups,dc=wso2,dc=org'
  


  $app_base               = 'dc=appfactory,dc=wso2,dc=com'
  $usrmgt_connectionname  = "uid=admin,ou=system"
  $usrmgt_connectionpasswd= "admin"
  $usrmgt_class           = "org.wso2.carbon.appfactory.userstore.AppFactoryCustomUserStoreManager"
  $usrmgt_RO_class        = "org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager"
  $usrmgt_usb             = "ou=Users,${app_base}"
  $usrmgt_unsf            = "(&amp;(objectClass=person)(uid=?))"
  $usrmgt_una             = "uid"
  $usrmgt_gsb             = "ou=Groups,${app_base}"
  $usrmgt_udnp            = "uid={0},ou=Users,${app_base}"
  $usrmgt_eoc             = "wso2Person"
  $usrmgt_unf             = "(objectClass=person)"



  #Proxy ports
  $http_proxy_port             = '80'
  $https_proxy_port             = '443'
}
