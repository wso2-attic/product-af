# ------------------------------------------------------------------------
#
# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------


from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
import subprocess
import os
import psutil

class WSO2ASStartupHandler(ICartridgeAgentPlugin):
    log = LogFactory().get_log(__name__)
    log.info("Disabling git ssl verification...")
    os.environ["GIT_SSL_NO_VERIFY"] = "1"

    def run_plugin(self, values):
        WSO2ASStartupHandler.log.info("Reading port mappings...")
        port_mappings_str = values["PORT_MAPPINGS"].replace("'", "")

        http_proxy_port = None
        https_proxy_port = None
        pt_http_port = None
        pt_https_port = None

        # port mappings format: """NAME:mgt-console|PROTOCOL:https|PORT:30000|PROXY_PORT:9443;
        #                          NAME:pt-http|PROTOCOL:http|PORT:30001|PROXY_PORT:7280;
        #                          NAME:pt-https|PROTOCOL:https|PORT:4502|PROXY_PORT:7243"""

        WSO2ASStartupHandler.log.info("Port mappings: %s" % port_mappings_str)
        if port_mappings_str is not None:

            port_mappings_array = port_mappings_str.split(";")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    WSO2ASStartupHandler.log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]

                    proxy_port = name_value_array[3].split(":")[1]
                    # If PROXY_PORT is not set,
                    if proxy_port == '0':
                        proxy_port = name_value_array[2].split(":")[1]
                    if name == "http-9763" and protocol == "http":
                        http_proxy_port = proxy_port
                    if name == "http-9443" and protocol == "https":
                        https_proxy_port = proxy_port

        WSO2ASStartupHandler.log.info("Catalina http proxy port: %s" % http_proxy_port)
        WSO2ASStartupHandler.log.info("Catalina https proxy por: %s" % https_proxy_port)

        if http_proxy_port is not None:
            command = "sed -i \"s/^#CONFIG_PARAM_HTTP_PROXY_PORT.*/CONFIG_PARAM_HTTP_PROXY_PORT = %s/g\" %s" % (http_proxy_port, "/opt/ppaas-configurator-4.1.0-SNAPSHOT/template-modules/wso2as-5.2.1/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            WSO2ASStartupHandler.log.info("Successfully updated catalina http proxy port: %s in AS template module" % http_proxy_port)

        if http_proxy_port is not None:
            command = "sed -i \"s/^#CONFIG_PARAM_HTTPS_PROXY_PORT.*/CONFIG_PARAM_HTTPS_PROXY_PORT = %s/g\" %s" % (https_proxy_port, "/opt/ppaas-configurator-4.1.0-SNAPSHOT/template-modules/wso2as-5.2.1/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            WSO2ASStartupHandler.log.info("Successfully updated catalina https proxy port: %s in AS template module" % https_proxy_port)


        # configure server
        WSO2ASStartupHandler.log.info("Configuring WSO2 AS...")
        config_command = "python /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("WSO2 AS configured successfully")

        # start server
        WSO2ASStartupHandler.log.info("Starting WSO2 AS...")

        start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.debug("WSO2 AS started successfully")

        s2gitDomain = values.get("S2GIT_DOMAIN")
        s2gitIP = values.get("S2GIT_IP")
        entry_command = "echo '"+ s2gitIP + " "+  s2gitDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("S2git host entry added successfully")

        bamDomain = values.get("BAM_DOMAIN")
        bamIP = values.get("BAM_IP")
        entry_command = "echo '"+ bamIP + " "+  bamDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("BAM host entry added successfully")

        thriftDomain = values.get("THRIFT_DOMAIN")
        thriftIP = values.get("THRIFT_IP")
        entry_command = "echo '"+ thriftIP + " "+  thriftDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("Thrift host entry added successfully")

        msgDomain = values.get("MESSAGING_DOMAIN")
        msgIP = values.get("MESSAGING_IP")
        entry_command = "echo '"+ msgIP + " "+  msgDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("Messaging host entry added successfully")

        socialDomain = values.get("SOCIAL_DOMAIN")
        socialIP = values.get("SOCIAL_IP")
        entry_command = "echo '"+ socialIP + " "+  socialDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("Social host entry added successfully")

        lbDomain = values.get("LB_DOMAIN")
        lbIP = values.get("LB_IP")
        entry_command = "echo '"+ lbIP + " "+  lbDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("LB host entry added successfully")

        mysqlDomain = values.get("MYSQL_DOMAIN")
        mysqlIP = values.get("MYSQL_IP")
        entry_command = "echo '"+ mysqlIP + " "+  mysqlDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("Mysql host entry added successfully")

        appfacDomain = values.get("APPFAC_DOMAIN")
        appfacIP = values.get("APPFAC_IP")
        entry_command = "echo '"+ appfacIP + " "+  appfacDomain + "' >> /etc/hosts"
        env_var = os.environ.copy()
        p = subprocess.Popen(entry_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ASStartupHandler.log.info("App Factory host entry added successfully")