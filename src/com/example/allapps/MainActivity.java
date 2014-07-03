package com.example.allapps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
/*
 * This class creates and displays the main menu for the WKU Glass app. The main menu is displayed using a card scroll view and
 * features a welcome screen card, as well as a card for each individual section of the app. The sections can be accessed with a
 * tap from the user on the desired activity's card.
 */
public class MainActivity extends Activity
{

   // GLOBAL variable
   public static List<String> info = new ArrayList<String>();
   long startTime, endTime;

   Card mainCard;
   Context context = this;
   List<Card> options;
   CardScrollView menuScroll;
   ScrollAdapter menuAdapter;

   String activity;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      startTime = System.currentTimeMillis();
      super.onCreate(savedInstanceState);

      options = new ArrayList<Card>();
      mainCard = new Card(this);

      //Set text for the welcome screen/card
      mainCard.setText("Welcome to the WKU Glass App");
      mainCard.setFootnote("Swipe for options.");

      //Add Welcome screen to the card list
      options.add(mainCard);
      addOptions();

      // Create new CardScrollView and Adapter, and activate the ScrollView
      menuScroll = new CardScrollView(this);
      menuAdapter = new ScrollAdapter(options);
      menuScroll.setAdapter(menuAdapter);
      menuScroll.activate();

      // Set the ContentView to show the Cards
      setContentView(menuScroll);

      // Set listener for the CardScrollView to generate an action when the user
      // selects a card.
      menuScroll.setOnItemClickListener(new OnItemClickListener()
      {
         @Override
         public void onItemClick(AdapterView<?> adapter, View v, int position,
               long id)
         {
            if (position == 1)
            {
               // Define the intent to show campus directions
               Intent intent = new Intent(MainActivity.this,
                     StartDirectionsActivity.class);
               startActivity(intent);
            }
            else if (position == 2)
            {
               // Define the intent to show campus news
               Intent intent = new Intent(MainActivity.this,
                     NewsArticleActivity.class);
               startActivity(intent);

            }
            else if (position == 3)
            {
               // Define the intent to show fresh menu
               Intent intent = new Intent(MainActivity.this,
                     DisplayMenuActivity.class);
               startActivity(intent);
            }
            else if (position == 4)
            {
               // Define the intent to show computer lab vacancies
               Intent intent = new Intent(MainActivity.this,
                     LabMenuActivity.class);
               startActivity(intent);

            }
            else if (position == 5)
            {
               // Define the intent to show what's open now
               Intent intent = new Intent(MainActivity.this,
                     FacilityHoursActivity.class);
               startActivity(intent);
            }
            else if (position == 6)
            {
               // Define the intent to show Campus Events
               Intent intent = new Intent(context, TodaysEventsActivity.class);
               startActivity(intent);
            }
            else if (position == 7){
               Intent intent = new Intent(context, MealPlans.class);
               startActivity(intent);
            }
//            endTime = System.currentTimeMillis();
//            String time = String.valueOf(endTime - startTime);
//            try
//            {
//               info.add(info.size() + "="
//                     + URLEncoder.encode("Main activity " + time, "UTF-8"));
//               new SendInfoToServerTask().execute();
//            }
//            catch (UnsupportedEncodingException e)
//            {
//               // TODO Auto-generated catch block
//               e.printStackTrace();
//            }
         }
      });

   }

   @Override
   protected void onResume()
   {
      onCreate(null);

      super.onResume();
   }

  

   // Add all menu options to the list of cards
   public void addOptions()
   {
      Card newCard = new Card(context);
      newCard.setText("Campus Directions");
      newCard.setFootnote("Tap to begin");
      options.add(newCard);

      newCard = new Card(context);
      newCard.setText("Campus News");
      newCard.setFootnote("Tap to begin");
      options.add(newCard);

      newCard = new Card(context);
      newCard.setText("Fresh Menu");
      newCard.setFootnote("Tap to begin.");
      options.add(newCard);

      newCard = new Card(context);
      newCard.setText("Lab Vacancies");
      newCard.setFootnote("Tap to begin.");
      options.add(newCard);

      newCard = new Card(context);
      newCard.setText("Open Now");
      newCard.setFootnote("Tap to begin.");
      options.add(newCard);

      newCard = new Card(context);
      newCard.setText("Campus Events");
      newCard.setFootnote("Tap to begin.");
      options.add(newCard);
      
      newCard = new Card(context);
      newCard.setText("Meal Plans");
      newCard.setFootnote("Tap to begin");
      options.add(newCard);

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {

      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
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

}
