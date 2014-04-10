/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.webdavsvn.svn.repository.provider;

/**
 *
 */
public class SSHClient {
    /*private static final Log log= LogFactory.getLog(SSHClient.class);
    private JSch shell;
    private Session session ;


    public SSHClient() {
        shell = new JSch();
    }

    public boolean connect(String username,String password,String hostName)
            throws RepositoryMgtException {
        // get a new session
        try {
            session = shell.getSession(username,hostName, 22);
        } catch (JSchException e) {
            //
        }
        session.setUserInfo(new SSHUserInfo(password));
        try {
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }
    public boolean disConnect(){
        session.disconnect();
        return true;
    }
    public boolean createRepositoryRemotely(String repositoryLocation,String sudoPassword,String username,String password,String host)
            throws RepositoryMgtException {
        connect(username,password,host);
        Channel channel = null;
        try {
            channel = session.openChannel("exec");
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ChannelExec channelExec= (ChannelExec) channel;
        channelExec.setCommand("sudo -S -p '' -u www-data "+"svnadmin create "+repositoryLocation+" \r\n");
        try {
            channelExec.connect();
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        DataInputStream dataIn = null;
        DataOutputStream dataOut=null;
        try {
            dataIn = new DataInputStream(channelExec.getInputStream());
            dataOut= new DataOutputStream(channelExec.getOutputStream());
            dataOut.write(sudoPassword.concat("\n").getBytes());
            dataOut.flush();
            byte[] tmp=new byte[1024];

                while(dataIn.available()>0){
                    int i=dataIn.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));

            }
           if(! channelExec.isClosed()){
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException ignore) {

               }
           }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                dataIn.close();
                dataOut.close();
                channelExec.disconnect();
                disConnect();
            } catch (IOException e) {

            }

        }






        return true;
    }
   // this class implements jsch UserInfo interface for passing password to the session
    static class SSHUserInfo implements com.jcraft.jsch.UserInfo {
        private String password;

        SSHUserInfo(String password) {
            this.password = password;
        }

        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return password;
        }

        public boolean promptPassword(String arg0) {
            return true;
        }

        public boolean promptPassphrase(String arg0) {
            return true;
        }

        public boolean promptYesNo(String arg0) {
            return true;
        }

        public void showMessage(String arg0) {
            System.out.println(arg0);
        }
    }*/
}
