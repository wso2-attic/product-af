export GIT_SSL_NO_VERIFY=1
mkdir -p ${GITBLIT_HOME}/logs
nohup /opt/java/bin/java -d64 -server ${JAVA_PROXY_CONFIG} -Djava.awt.headless=true -Xms1024m -Xmx4096m -XX:MaxPermSize=1024m  -Djava.io.tmpdir="{GITBLIT_HOME}/tmp/" -jar gitblit.jar --httpsPort 443 --storePassword wso2carbon > ${GITBLIT_HOME}/logs/git.log 2>&1 &
