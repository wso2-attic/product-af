package org.wso2.appfactory.integration.test.utils.rest;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;



/**
 * Created by binalip91 on 5/5/15.
 */
public class HTTPHandler {
    public String doPostHttps(String url, String payload, String sessionId, String contentType)
            throws IOException {
        URL obj = new URL(url);
        System.out.println("SESSION  "+sessionId);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        if (!sessionId.equals("")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + sessionId);
        }
        if (!contentType.equals("")) {
            con.setRequestProperty("Content-Type", contentType);
        }
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if (sessionId.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                return session_id;
            } else if (sessionId.equals("header")) {
                return con.getHeaderField("Set-Cookie");
            }
            System.out.print("response  "+response.toString());
            return response.toString();

        }
        return null;
    }

    public String doPostHttp(String url, String payload, String sessionId, String contentType)
            throws IOException {
        URL obj = new URL(url);
        System.out.println("SESSION  "+sessionId);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        //con.setRequestProperty("User-Agent", USER_AGENT);
        if (!sessionId.equals("") && !sessionId.equals("none")) {
            con.setRequestProperty(
                    "Cookie", "JSESSIONID=" + sessionId);
        }
        con.setRequestProperty("Content-Type", contentType);
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println("response   "+response.toString());
            if (sessionId.equals("")) {
                String session_id = response.substring((response.lastIndexOf(":") + 3), (response.lastIndexOf("}") - 2));
                System.out.println(" response "+session_id);
                return session_id;
            } else if (sessionId.equals("appmSamlSsoTokenId")) {
                System.out.println(" response "+con.getHeaderField("Set-Cookie").split(";")[0].split("=")[1]);
                return con.getHeaderField("Set-Cookie").split(";")[0].split("=")[1];
            } else if (sessionId.equals("header")) {
                System.out.println(" response "+con.getHeaderField("Set-Cookie").split("=")[1].split(";")[0]);
                return con.getHeaderField("Set-Cookie").split("=")[1].split(";")[0];
            } else {
                System.out.println(" response "+response.toString());
                return response.toString();

            }
        }

        return null;
    }
}
