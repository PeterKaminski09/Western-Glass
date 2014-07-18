package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.os.Build;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

/**
 * MealPlans is the activity which takes Logan's first version of the mealplans application and puts it onto
 * the glass. 
 * @author peterkaminski
 *
 */
public class MealPlans extends Activity
{
   //Progress Bar to display while the menu is loading.
   ProgressBar downloadBar;
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_meal_plans);

      //Call the asynctask upon creation
     new MealPlansTask().execute();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {

      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.meal_plans, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.action_settings)
      {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
   
   
   private class MealPlansTask extends AsyncTask<Void, Void, DiningInformation>
   {

      @Override
      protected void onPreExecute()
      {  
         //Executed before the thread begins
         super.onPreExecute();
         //Set the content to our generic loading screen
         setContentView(R.layout.better_launch);
         downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
         //Start the downloadBar
         downloadBar.setVisibility(0);
         
      }

      //This calls Logan's code and retrieves the latest information regarding the mealplan usage for the user
      @Override
      protected DiningInformation doInBackground(Void... voids)
      {
         DiningInformation diningInformation = new DiningInformation();
         return diningInformation;
      }

      //After execution we need to update the UI and create the cards that will be in our scroll view. 
      @Override
      protected final void onPostExecute(DiningInformation dining)
      {
         //Create a card and set the text of it to contain the mealplan info in its toString representation. 
         Card card = new Card(MealPlans.this);
         card.setText(dining.prettyRepresentation());
         //Set the content view to the card. 
         setContentView(card.getView());
         //Stop the download bar. 
         downloadBar.setVisibility(4);
      }

   }


}
