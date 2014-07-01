package com.example.allapps;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WritePostExample {
   
   private final String USER_AGENT = "Mozilla/5.0";
   protected List<String> info = new ArrayList<String> ();

   public void setInfo(List<String> list){
      this.info = list;
   }

   // HTTP POST request
   protected void sendPost() throws Exception {

       // String url = "https://selfsolve.apple.com/wcResults.do";
       String url = "http://projectglass.netii.net/savecrashinfo.php";

       URL obj = new URL(url);
       HttpURLConnection con = (HttpURLConnection) obj.openConnection();

       //add reuqest header
       con.setRequestMethod("POST");
       con.setRequestProperty("User-Agent", USER_AGENT);
       con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

       // build parameters list
       StringBuilder result = new StringBuilder();
       for (String param : info) {
           
           if (result.length() == 0) {
               result.append(param);
           } else {
               result.append("&" + param);
           }
           
           info.remove(param);
       }
       
       String params = result.toString();;
       
       con.setRequestProperty("Content-Length",
               Integer.toString(params.length()));


   
       // String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

       // Send post request
       con.setDoOutput(true);
       DataOutputStream wr = new DataOutputStream(con.getOutputStream());
       wr.writeBytes(params);
       wr.flush();
       wr.close();

       int responseCode = con.getResponseCode();

       BufferedReader in = new BufferedReader(
               new InputStreamReader(con.getInputStream()));
       String inputLine;
       StringBuffer response = new StringBuffer();

       while ((inputLine = in.readLine()) != null) {
           response.append(inputLine);
       }
       in.close();
   }

}