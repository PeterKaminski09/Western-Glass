package com.example.allapps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;

/*
 * This class creates and displays the main menu for the WKU Glass app. The main menu is displayed using a card scroll view and
 * features a welcome screen card, as well as a card for each individual section of the app. The sections can be accessed with a
 * tap from the user on the desired activity's card.
 * 
 * Code written and commented by Lydia Buzzard and Peter Kaminski
 */

public class MainActivity extends Activity
{

   // GLOBAL variable
   public static List<String> info = new ArrayList<String>();
   long startTime, endTime;

   //Card for the welcome screen
   Card mainCard;
   //Context of the application
   Context context = this;
   //Card list to hold all options
   List<Card> options;
   //Scroll view to display all options
   CardScrollView menuScroll;
   //Scroll adapter to supply scroll view with the card list
   ScrollAdapter menuAdapter;

   String activity;

   // Shared preferences class variables
   private SharedPreferences prefs;
   SharedPreferences.Editor editor;
   Map<String, Integer> map = new HashMap<String, Integer>();
   ValueComparator bvc = new ValueComparator(map);
   TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
   private int labCount, directionsCount, menuCount, openCount, newsCount,
         eventCount, mealCount;
   
   //Text Label
   TextView text;
   ProgressBar progressBar;

   // Keys for default preference values
   private final String LAB = "Lab Vacancies";
   private final String DIRECTIONS = "Campus Directions";
   private final String MENU = "Fresh Menu";
   private final String OPEN = "Open Now";
   private final String NEWS = "Campus News";
   private final String EVENT = "Campus Events";
   private final String MEALPLAN = "Meal Plans";

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      startTime = System.currentTimeMillis();
      super.onCreate(savedInstanceState);

      options = new ArrayList<Card>();
      mainCard = new Card(this);

      // Set text for the welcome screen/card
      mainCard.setText("Welcome to the WKU Glass App");
      mainCard.setFootnote("Swipe for options, tap to reset user preferences.");

      // Add Welcome screen to the card list
      options.add(mainCard);

      // Initiate the shared preferences
      prefs = this.getSharedPreferences("com.example.allapps",
            Context.MODE_PRIVATE);
      editor = prefs.edit();
      // Find the user's preferences for display order.
      findCounts();
      // Now set the cards based on the counts
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
            Intent intent;
            String type = options.get(position).getText().toString();
            // Now run through the available options and start an event
            // task based on what was clicked.

            if (position == 0)
            {
               Log.e("Resetting user prefs", "Reset initiated");
               resetUserPrefs();
               openOptionsMenu();
            }
            else
            {
               switch (type)
               {
               case LAB:
                  intent = new Intent(context, LabMenuActivity.class);
                  startActivity(intent);
                  labCount++;
                  break;
               case MEALPLAN:
                  intent = new Intent(context, MealPlans.class);
                  startActivity(intent);
                  mealCount++;
                  break;
               case EVENT:
                  intent = new Intent(context, TodaysEventsActivity.class);
                  startActivity(intent);
                  eventCount++;
                  break;
               case NEWS:
                  intent = new Intent(context, NewsArticleActivity.class);
                  startActivity(intent);
                  newsCount++;
                  break;
               case DIRECTIONS:
                  intent = new Intent(context, StartDirectionsActivity.class);
                  startActivity(intent);
                  directionsCount++;
                  break;
               case OPEN:
                  intent = new Intent(context, FacilityHoursActivity.class);
                  startActivity(intent);
                  openCount++;
                  break;
               case MENU:
                  intent = new Intent(context, DisplayMenuActivity.class);
                  startActivity(intent);
                  menuCount++;
                  break;

               }

               // Finish by updating the counts.
               updateCounts();

            }
         }
      });

   }

   // This determines the current counts for the users recently searched events
   public void findCounts()
   {
      // Set the counts, based on user preferences.
      labCount = Integer.parseInt(prefs.getString(LAB, "1"));
      menuCount = Integer.parseInt(prefs.getString(MENU, "1"));
      directionsCount = Integer.parseInt(prefs.getString(DIRECTIONS, "1"));
      eventCount = Integer.parseInt(prefs.getString(EVENT, "1"));
      mealCount = Integer.parseInt(prefs.getString(MEALPLAN, "1"));
      openCount = Integer.parseInt(prefs.getString(OPEN, "1"));
      newsCount = Integer.parseInt(prefs.getString(NEWS, "1"));

      // Put them into a map
      map.put(LAB, labCount);
      map.put(MENU, menuCount);
      map.put(DIRECTIONS, directionsCount);
      map.put(EVENT, eventCount);
      map.put(MEALPLAN, mealCount);
      map.put(OPEN, openCount);
      map.put(NEWS, newsCount);

      // Sort the map by most used in a tree map
      sorted_map.putAll(map);
      Log.i("Click counts", sorted_map.toString());

   }

   // Update counts refreshes the user preferences.
   public void updateCounts()
   {
      // Make the changes to the user preferences
      editor.putString(LAB, String.valueOf(labCount));
      editor.putString(MEALPLAN, String.valueOf(mealCount));
      editor.putString(DIRECTIONS, String.valueOf(directionsCount));
      editor.putString(NEWS, String.valueOf(newsCount));
      editor.putString(EVENT, String.valueOf(eventCount));
      editor.putString(MENU, String.valueOf(menuCount));
      editor.putString(OPEN, String.valueOf(openCount));

      editor.commit();
   }

   // restUserPrefs refreshes the user preferences.
   public void resetUserPrefs()
   {
      // Make the changes to the user preferences
      editor.putString(LAB, "1");
      editor.putString(MEALPLAN, "1");
      editor.putString(DIRECTIONS, "1");
      editor.putString(NEWS, "1");
      editor.putString(EVENT, "1");
      editor.putString(MENU, "1");
      editor.putString(OPEN, "1");

      editor.commit();
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
      for (int i = 0; i < 7; i++)
      {
         // For each element in the map, create a card based on the most used
         // option
         Card card = new Card(MainActivity.this);

         switch (sorted_map.firstEntry().getKey())
         {
         case LAB:
            // For each card set the text, then clear the sorted map. Remove
            // the case just used, and then resort the map
            card.setText(LAB);
            sorted_map.clear();
            map.remove(LAB);
            sorted_map.putAll(map);
            break;
         case MENU:
            card.setText(MENU);
            sorted_map.clear();
            map.remove(MENU);
            sorted_map.putAll(map);
            break;
         case EVENT:
            card.setText(EVENT);
            sorted_map.clear();
            map.remove(EVENT);
            sorted_map.putAll(map);
            break;
         case DIRECTIONS:
            card.setText(DIRECTIONS);
            sorted_map.clear();
            map.remove(DIRECTIONS);
            sorted_map.putAll(map);
            break;
         case OPEN:
            card.setText(OPEN);
            sorted_map.clear();
            map.remove(OPEN);
            sorted_map.putAll(map);
            break;
         case NEWS:
            card.setText(NEWS);
            sorted_map.clear();
            map.remove(NEWS);
            sorted_map.putAll(map);
            break;
         case MEALPLAN:
            card.setText(MEALPLAN);
            sorted_map.clear();
            map.remove(MEALPLAN);
            sorted_map.putAll(map);
            break;

         }
         card.setFootnote("Tap for events");
         // Then add the card to the array list.
         options.add(card);

      }

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

   // Compares the values within a Map to find the largest to the smallest and
   // sort
   class ValueComparator implements Comparator<String>
   {

      Map<String, Integer> base;

      public ValueComparator(Map<String, Integer> base)
      {
         this.base = base;
      }

      // Note: this comparator imposes orderings that are inconsistent with
      // equals.
      public int compare(String a, String b)
      {
         if (base.get(a) >= base.get(b))
         {
            return -1;
         }
         else
         {
            return 1;
         } // returning 0 would merge keys
      }
   }

}
