# ----------------------------------------------------------------------------
#  Copyright 2005-2014 WSO2, Inc. http://www.wso2.org
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

1.) Edit the configuration parameters (config.properties) according to the requirments
2.) Run setup.sh as the root use


restart.sh
-----------

Use this script to restart the entire appfactory.
This will stop all the running appfactory processes and restart them


clean.sh
--------

Use this script to clean the entire appfactory setup

Warnig!! This will kill all the appfactory related processes and
delete the setup directory. Use this with extreme caution.

Stop.sh
-------
Use this script to stop entire apfactory. This will not kill other java processes 
in the machine instead it will kill all the appfactory related java processes

