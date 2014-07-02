package com.example.allapps;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask that sends all information regarding the microinteractions to the webserver. 
 * @author peterkaminski
 *
 */
public class SendInfoToServerTask extends AsyncTask<Void, Void, String>
{

   //This access the WordPress api and finds information about the top 5 posted articles, storing them as article objects
   @Override
   protected String doInBackground(Void...voids)
   {    //Create a new post example
      WritePostExample http = new WritePostExample();
      
      try
      {
         //Pick which type of information to send and send the post. 
         if(DisplayMenuActivity.info.size() > 0)
         {
         
         http.setInfo(DisplayMenuActivity.info);
         http.sendPost();
         }
         if(TodaysEventsActivity.info.size() > 0)
         {
            http.setInfo(TodaysEventsActivity.info);
            http.sendPost();
         }
         if(ShowVacancyActivity.info.size() > 0)
         {
            http.setInfo(TodaysEventsActivity.info);
            http.sendPost();
         }
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return "String";
   }

   // After execution we need to update the UI and create the cards that will be
   // in our scroll view.
   @Override
   protected final void onPostExecute(String dining)
   {

   }

}

