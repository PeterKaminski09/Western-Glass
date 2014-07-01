package com.example.allapps;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DiningInformation
{

   private List<String> cookies;
   private HttpsURLConnection conn;
   private String endOfURL;
   private String mealPlans;
   private String diningDollars;
   private String bigRedDollars;
   private String mealPlanDollars;

   private final String USER_AGENT = "Mozilla/5.0";
   
   public DiningInformation()
   {
         String url = "https://wku.managemyid.com/reference.dca?cdx=login";
         String manageId = "https://wku.managemyid.com";
         
         
         // make sure cookies is turn on
         CookieHandler.setDefault(new CookieManager());

        
        try
        {
           // 1. Send a "GET" request, so that you can extract the form's data.
           String page = GetPageContent(url);
           String postParams = getFormParams(page,
                   "Peter.Kaminski09@gmail.com", "Gatton101");

           // 2. Construct above post's content and then send a POST request for
           // authentication
           sendPost(url, postParams);
           
           //Create the new full url 
           manageId = manageId.concat(endOfURL);

           // 3. success then go to manageId.
           String result;
           result = GetPageContent(manageId);
           //The following code in this method sorts through the html code to find the information we need
           //Later I will replace the + "some number" with a better form of dynamic parsing but for now this works
           //Meal Plans extraction
           int posBeforeMeals = result.indexOf("<td>Meals</td>");
           mealPlans = result.substring(posBeforeMeals + 34, posBeforeMeals + 36);
           if (mealPlans.substring(1).equals("<")){
               mealPlans = mealPlans.substring(0,1);
           }
           
           //Dining Dollars extraction
           int posBeforeDiningDollars = result.indexOf("<td>Dining Dollars</td>");
           diningDollars = result.substring(posBeforeDiningDollars + 43, posBeforeDiningDollars + 49);
           if (diningDollars.substring(diningDollars.length()-1).equals("<")){
               diningDollars = diningDollars.substring(0, diningDollars.length()-1);
           }
           
           //Big Red Dollars extraction
           int posBeforeBigRedDollars = result.indexOf("<td>Big Red Dollars</td>");
           bigRedDollars = result.substring(posBeforeBigRedDollars + 44, posBeforeBigRedDollars + 50);
           if (bigRedDollars.substring(bigRedDollars.length()-1).equals("<")){
               bigRedDollars = bigRedDollars.substring(0, bigRedDollars.length()-1);
           }
           
           //Meal Plan Dollars extraction
           int posBeforeMealPlanDollars = result.indexOf("<td>Meal Plan Dollars</td>");
           mealPlanDollars = result.substring(posBeforeMealPlanDollars + 46, posBeforeMealPlanDollars + 52);
           if (mealPlanDollars.substring(mealPlanDollars.length()-1).equals("<")){
               mealPlanDollars = mealPlanDollars.substring(0, mealPlanDollars.length()-1);
           }
           
        }
        catch (Exception e)
        {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
         
        
      
   }
   
 //This method is used to post our signin data into the fields of the login screen and then to receive the new webpage 
   //data in the form of an ending of a URL embedded into the rest of the html code for the page
   private void sendPost(String url, String postParams) throws Exception {

       URL obj = new URL(url);
       conn = (HttpsURLConnection) obj.openConnection();

       // Acts like a browser
       conn.setUseCaches(false);
       conn.setRequestMethod("POST");
       //Difference from orig below
       conn.setRequestProperty("Host", "wku.managemyid.com");
       conn.setRequestProperty("User-Agent", USER_AGENT);
       conn.setRequestProperty("Accept",
               "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
       conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
       if (cookies != null) {
           for (String cookie : this.cookies) {
               conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
           }
       }
       conn.setRequestProperty("Connection", "keep-alive");
       //What screen are you coming from? (below)
       conn.setRequestProperty("Referer",
               "https://wku.managemyid.com/reference.dca?cdx=login");
       conn.setRequestProperty("Content-Type",
               "application/x-www-form-urlencoded");
       conn.setRequestProperty("Content-Length",
               Integer.toString(postParams.length()));

       conn.setDoOutput(true);
       conn.setDoInput(true);

       // Send post request
       DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
       wr.writeBytes(postParams);
       wr.flush();
       wr.close();

       int responseCode = conn.getResponseCode();
       BufferedReader in = new BufferedReader(new InputStreamReader(
               conn.getInputStream()));
       String inputLine;
       StringBuffer response = new StringBuffer();

       while ((inputLine = in.readLine()) != null) {
           response.append(inputLine);
       }
       in.close();
       
       //New code below
       
       Document doc = Jsoup.parse(response.toString());
       Element name = doc.getElementById("contentNarrow");
       
       Elements inputElements = name.getElementsByTag("meta");
       for (Element inputElement : inputElements) {
           String key = inputElement.attr("content");
           int pos = key.indexOf("URL=");
           endOfURL = key.substring(pos+4);
       }

   }

   private String GetPageContent(String url) throws Exception {

       URL obj = new URL(url);
       conn = (HttpsURLConnection) obj.openConnection();

       // default is GET
       conn.setRequestMethod("GET");

       conn.setUseCaches(false);

       // act like a browser
       conn.setRequestProperty("User-Agent", USER_AGENT);
       conn.setRequestProperty("Accept",
               "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
       conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
       if (cookies != null) {
           for (String cookie : this.cookies) {
               conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
           }
       }
       int responseCode = conn.getResponseCode();
       BufferedReader in = new BufferedReader(new InputStreamReader(
               conn.getInputStream()));
       String inputLine;
       StringBuffer response = new StringBuffer();

       while ((inputLine = in.readLine()) != null) {
           response.append(inputLine);
       }
       in.close();

       // Get the response cookies
       setCookies(conn.getHeaderFields().get("Set-Cookie"));

       return response.toString();

   }

   public String getFormParams(String html, String username, String password)
           throws UnsupportedEncodingException {

       Document doc = Jsoup.parse(html);
       
       // Google form id
       // NodeList loginform = (NodeList)
       // doc.getElementsByAttributeValue("action",
       // "/reference.dca?cdx=login");
       Elements links = doc.select("form");
       Elements inputElements = links.get(0).getElementsByTag("input");
       List<String> paramList = new ArrayList<String>();
       for (Element inputElement : inputElements) {
           String key = inputElement.attr("name");
           String value = inputElement.attr("value");
           
           if (key.equals("LoginID"))
               value = username;
           else if (key.equals("LoginPWD"))
               value = password;
           paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
       }
       
       // build parameters list
       StringBuilder result = new StringBuilder();
       for (String param : paramList) {
           if (result.length() == 0) {
               result.append(param);
           } else {
               result.append("&" + param);
           }
       }
       return result.toString();
   }

   public List<String> getCookies() {
       return cookies;
   }

   public void setCookies(List<String> cookies) {
       this.cookies = cookies;
   }
   
   public String getDiningDollars(){
      return this.diningDollars;
   }
   public String getBigRedDollars(){
      return this.bigRedDollars;
   }
   public String getMealPlanDollars(){
      return this.mealPlanDollars;
   }
   
   public String getMealPlans(){
      return this.mealPlans;
   }
   
   //This method gives us a good way to visualize what information we have attained from this class. 
   public String prettyRepresentation(){
      return String.format("Meal plans:%s\nMeal plan dollars:%s\nBig Red dollars:%s\n", this.mealPlans, this.mealPlanDollars, this.bigRedDollars);
   }

}
