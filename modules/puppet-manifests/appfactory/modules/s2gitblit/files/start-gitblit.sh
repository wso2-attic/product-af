export GIT_SSL_NO_VERIFY=1
mkdir -p /mnt/s2gitblit/logs/
nohup /opt/java/bin/java -server ${JAVA_PROXY_CONFIG} -Djava.awt.headless=true -Xms1024m -Xmx4096m -XX:MaxPermSize=1024m  -Djava.io.tmpdir="/mnt/tmp/" -jar gitblit.jar --httpsPort 443 --storePassword gitblit > /mnt/s2gitblit/logs/s2gitblit.log 2>&1  &
