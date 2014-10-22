package com.example.allapps;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.android.glass.widget.CardBuilder;

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
         //First test using the cardbuilder
         CardBuilder cardBuild = new CardBuilder(MealPlans.this, CardBuilder.Layout.TEXT);
         cardBuild.setText(dining.prettyRepresentation());
         setContentView(cardBuild.getView());
         downloadBar.setVisibility(4);
         
         //Old shit
//         //Create a card and set the text of it to contain the mealplan info in its toString representation. 
//         Card card = new Card(MealPlans.this);
//         card.setText(dining.prettyRepresentation());
//         //Set the content view to the card. 
//         setContentView(card.getView());
//         //Stop the download bar. 
//         downloadBar.setVisibility(4);
      }

   }


}
