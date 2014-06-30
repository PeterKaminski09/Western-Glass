package com.example.allapps;

import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class SendInfoToServerTask extends AsyncTask<Void, Void, String>
{

   //This access the WordPress api and finds information about the top 5 posted articles, storing them as article objects
   @Override
   protected String doInBackground(Void...voids)
   {
      Log.i("AsyncTask", "Task started");
      WritePostExample http = new WritePostExample();
      http.setInfo(MainActivity.info);
      try
      {
         http.sendPost();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return "String";
   }

   //After execution we need to update the UI and create the cards that will be in our scroll view. 
   @Override
   protected final void onPostExecute(String dining)
   {
     
   }

}

