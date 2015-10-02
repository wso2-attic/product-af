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


FROM wso2/base-image:4.1.0
MAINTAINER dev@wso2.org

ENV JDK_VERSION 1.7.0_60
ENV TOMCAT_VERSION 7.0.63

# --------------------------------
# Copy scripts, packages & plugins
# --------------------------------
COPY scripts/init.sh /usr/local/bin/init.sh
COPY packages/apache-tomcat-${TOMCAT_VERSION}.zip /opt/

# ----------------------
# Install prerequisites
# ----------------------
ENV JAVA_HOME /opt/jdk${JDK_VERSION}

# ----------------------
# Install Tomcat
# ----------------------
WORKDIR /opt

RUN unzip /opt/apache-tomcat-${TOMCAT_VERSION}.zip -d /opt/ && \
    mv /opt/apache-tomcat-${TOMCAT_VERSION} /opt/tomcat && \
    rm -rf /opt/apache-tomcat-${TOMCAT_VERSION}.zip

ENV CATALINA_HOME /opt/tomcat

# -----------------------
# Add shell scripts
# -----------------------
ADD files/create-admin-user.sh /opt/create-admin-user.sh
RUN chmod +x /opt/create-admin-user.sh && \
    chmod +x ${CATALINA_HOME}/bin/startup.sh && \
    chmod +x ${CATALINA_HOME}/bin/catalina.sh && \
    bash /opt/create-admin-user.sh

ADD files/env /tmp/env
RUN chmod +x /tmp/env && \
    sleep 1 && \
    /tmp/env ${JAVA_HOME} ${CATALINA_HOME}

#------------------------
# Copy Tomcat related PCA plugins
#-----------------------
COPY packs/plugins ${PCA_HOME}/plugins


EXPOSE 8080

# -----------------------
# Define entry point
# -----------------------
ENTRYPOINT /usr/local/bin/init.sh >> /tmp/init.sh.log 2>&1 | /usr/sbin/sshd -D
