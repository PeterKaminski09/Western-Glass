package com.example.allapps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class TodaysEventsActivity extends Activity
{
   // For audio purposes
   private AudioManager mAudioManager;
   public static List<String> info = new ArrayList<String>();

   // Card scrolling objects
   private List<Card> mCards = new ArrayList<Card>();
   private CardScrollView mCardScrollView;
   private Context context = this;

   // CATEGORY IDS
   private final static int SPORTS = 314;
   private final static int ARTS = 315;
   private final static int CAMPUS = 313;
   private final static int STUDENT_ACTIVITIES = 307;
   private SharedPreferences prefs;
   SharedPreferences.Editor editor;
   boolean micros;

   // Keys for default preference values
   private final String SPORTS_KEY = "Athletic Events";
   private final String ARTS_KEY = "Fine Arts";
   private final String CAMPUS_KEY = "All Events";
   private final String STUDENT_KEY = "Student Activities";
   Map<String, Integer> map = new HashMap<String, Integer>();
   ValueComparator bvc = new ValueComparator(map);
   TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
   long startTime, endTime;

   private int sportCount, artCount, studentCount, campusCount;

   // UI Elements
   ProgressBar downloadBar;

   // Counter for preferences, 0 is sports, 1 is arts, 2 is student, 3 is campus
   int[] counts = new int[4];

   // Start the timer as the activity becomes available to the user.
   protected void onResume()
   {
      super.onResume();
      // Start the time as soon as the app launches.
      startTime = System.currentTimeMillis();
      
   }

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {

      super.onCreate(savedInstanceState);
      
      // Initiate the shared preferences
      prefs = this.getSharedPreferences("com.example.allapps",
            Context.MODE_PRIVATE);
      editor = prefs.edit();
      // Find the user's preferences for display order.
      findCounts();
      // Now set the cards based on the counts
      setCards();

      // Set up the audio and gestures
      mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      // For all the strings in the array, create a card based on the text from
      // the array.

      mCardScrollView = new CardScrollView(context);
      ScrollAdapter adapter = new ScrollAdapter(mCards);
      mCardScrollView
            .setOnItemClickListener(new AdapterView.OnItemClickListener()
            {

               @Override
               public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id)
               {
                  // Stop the time
                  endTime = System.currentTimeMillis();
                  // Find the time by subtracting
                  String time = String.valueOf(endTime - startTime);
                  try
                  {
                     info.add(info.size()
                           + "="
                           + URLEncoder.encode("Events Activity:  " + time
                                 + " milliseconds" + " Microinteractions:"
                                 + Microinteractions.on, "UTF-8"));
                     new SendInfoToServerTask().execute();
                  }
                  catch (UnsupportedEncodingException e)
                  {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                  // Play a sound effect
                  mAudioManager.playSoundEffect(Sounds.TAP);
                  // Find the type of card that was pressed;
                  String type = mCards.get(position).getText().toString();
                  // Now run through the available options and start an event
                  // task based on what was clicked.
                  switch (type)
                  {
                  case SPORTS_KEY:
                     new EventTask().execute(SPORTS);
                     sportCount++;
                     break;
                  case ARTS_KEY:
                     new EventTask().execute(ARTS);
                     artCount++;
                     break;
                  case STUDENT_KEY:
                     new EventTask().execute(STUDENT_ACTIVITIES);
                     studentCount++;
                     break;
                  case CAMPUS_KEY:
                     new EventTask().execute(CAMPUS);
                     campusCount++;
                     break;
                  default:
                     Log.i("DEFAULT REACHED", "DEFAULT");
                     new EventTask().execute(CAMPUS);
                  }

                  // Finish by updating the counts.
                  updateCounts();

               }

            });
      mCardScrollView.setAdapter(adapter);
      mCardScrollView.activate();

      // Update the view
      setContentView(mCardScrollView);

   }

   // This determines the current counts for the users recently searched events
   public void findCounts()
   {
      // Set the counts, based on user preferences.
      sportCount = Integer.parseInt(prefs.getString(SPORTS_KEY, "4"));
      artCount = Integer.parseInt(prefs.getString(ARTS_KEY, "3"));
      studentCount = Integer.parseInt(prefs.getString(STUDENT_KEY, "2"));
      campusCount = Integer.parseInt(prefs.getString(CAMPUS_KEY, "1"));

      // Put them into a map
      map.put(SPORTS_KEY, sportCount);
      map.put(ARTS_KEY, artCount);
      map.put(STUDENT_KEY, studentCount);
      map.put(CAMPUS_KEY, campusCount);

      // Sort the map by most used in a tree map
      sorted_map.putAll(map);
      Log.i("Click counts", sorted_map.toString());

   }

   // Update counts refreshes the user preferences.
   public void updateCounts()
   {
      // Make the changes to the user preferences
      editor.putString(SPORTS_KEY, String.valueOf(sportCount));
      editor.putString(ARTS_KEY, String.valueOf(artCount));
      editor.putString(STUDENT_KEY, String.valueOf(studentCount));
      editor.putString(CAMPUS_KEY, String.valueOf(campusCount));

      editor.commit();
   }

   // Set the cards in the card list to that of the most used, based on the
   // counts in the map.
   public void setCards()
   {

         for (int i = 0; i < 4; i++)
         {
            // For each element in the map, create a card based on the most used
            // option
            Card card = new Card(TodaysEventsActivity.this);

            switch (sorted_map.firstEntry().getKey())
            {
            case SPORTS_KEY:
               // For each card set the text, then clear the sorted map. Remove
               // the case just used, and then resort the map
               card.setText(SPORTS_KEY);
               sorted_map.clear();
               map.remove(SPORTS_KEY);
               sorted_map.putAll(map);
               break;
            case ARTS_KEY:
               card.setText(ARTS_KEY);
               sorted_map.clear();
               map.remove(ARTS_KEY);
               sorted_map.putAll(map);
               break;
            case STUDENT_KEY:
               card.setText(STUDENT_KEY);
               sorted_map.clear();
               map.remove(STUDENT_KEY);
               sorted_map.putAll(map);
               break;
            case CAMPUS_KEY:
               card.setText(CAMPUS_KEY);
               sorted_map.clear();
               map.remove(CAMPUS_KEY);
               sorted_map.putAll(map);
               break;

            }
            card.setFootnote("Tap for events");
            // Then add the card to the array list.
            mCards.add(card);

         }
      
   }
   
   private class EventTask extends AsyncTask<Integer, Void, ArrayList<Event>>
   {
      @Override
      protected void onPreExecute()
      {
         // Executed before the thread begins
         super.onPreExecute();
         setContentView(R.layout.better_launch);
         downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
         // Simulate starting the downloadBar
         downloadBar.setVisibility(0);
      }

      // This access the WordPress api and finds information about the top 5
      // posted articles, storing them as article objects
      @Override
      protected ArrayList<Event> doInBackground(Integer... integers)
      {
         EventParse event = new EventParse(integers[0]);
         return event.getEvents();
      }

      // After execution we need to update the UI and create the cards that will
      // be in our scroll view.
      @Override
      protected final void onPostExecute(ArrayList<Event> events)
      {

         // For all remaining events, run through the array list and add the
         // event to a string array.
         // This allows us to easily send the information to the showEvent
         // activity.
         ArrayList<String> extra_strings = new ArrayList<String>();
         for (Event event : events)
         {
            extra_strings.add(event.prettyRepresentation());
         }
         //Create a new intent and put the event strings into the intent, then start it. 
         Intent showEvents = new Intent(context, ShowEventsActivity.class);
         showEvents.putStringArrayListExtra("event_strings", extra_strings);
         // Dismiss the progress bar
         downloadBar.setVisibility(4);
         setContentView(mCardScrollView);
         startActivity(showEvents);

      }

   }

   // Find end time takes a string and returns a string that holds the time that
   // an event will end.
   // For instance, if an event is from 1:00am-2:00am, 2:00 will be returned.
   public String findEndTime(String time)
   {
      // Find the separator
      int firstTime = time.indexOf("-") + 1;
      // Make a substring without the start time
      String timeWithSuffix = time.substring(firstTime);
      // Remove the am/pm suffix
      // String timeWithoutSuffix = removeAlphaCharacters(timeWithSuffix);
      // Return the formatted string without any leading or trailing whitespace
      return timeWithSuffix.trim();

   }

   // RemoveAlphaCharacters is a method that takes a string and removes all the
   // characters from it that aren't numeric
   public String removeAlphaCharacters(String timeString)
   {
      // Regex is used to replace the am and pm in a time string
      return timeString.replaceAll("[a-z?]", "");
   }

   // CompareTimes takes a string of time and a calendar object. It then
   // compares the two times and return true if the event hasn't already
   // happened
   // False if the event has already happened today. This allows us to make a
   // microinteraction for our users. They will have no interest in going to
   // Events that already happened so this prevents them from having to scroll
   // through unnecessary cards.
   public boolean compareTimes(String time, Calendar cal)
   {
      Calendar event = Calendar.getInstance();
      DateFormat formatter = new SimpleDateFormat("h:mm a");

      try
      {
         // Create a date object from this with today's current values.
         Date date = formatter.parse(time);
         event.setTime(date);
         event.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
               cal.get(Calendar.DATE));

         if (cal.getTime().compareTo(event.getTime()) < 0)
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      catch (ParseException e)
      {
         // If there is a parse error, return false in general. This card must
         // have some funky date.
         e.printStackTrace();
         return false;
      }

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
