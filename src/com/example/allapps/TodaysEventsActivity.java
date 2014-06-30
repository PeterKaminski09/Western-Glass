package com.example.allapps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TodaysEventsActivity extends Activity
{
   // For audio purposes
   private AudioManager mAudioManager;

   // Card scrolling objects
   private List<Card> mCards = new ArrayList<Card>();
   // second list for the nested cards
   private List<Card> eventCards = new ArrayList<Card>();
   private CardScrollView mCardScrollView;
   private Context context = this;

   private ArrayList<Event> currentEvents = new ArrayList<Event>();

   // CATEGORY IDS
   private final static int SPORTS = 314;
   private final static int ARTS = 315;
   private final static int CAMPUS = 313;
   private final static int STUDENT_ACTIVITIES = 307;
   
   ProgressBar downloadBar;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      
      super.onCreate(savedInstanceState);
    //Set up the audio and gestures
      mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      
     
      // Create a sports card.
      Card sports = new Card(this);
      sports.setText("Athletic Events");
      mCards.add(sports);

      // Create an arts card
      Card arts = new Card(this);
      arts.setText("Fine Arts");
      mCards.add(arts);
      
      // Create a student activities card
      Card student = new Card(this);
      student.setText("Student Activities");
      mCards.add(student);
      
      // Create a card for campus events
      Card campus = new Card(this);
      campus.setText("Campus Events");
      mCards.add(campus);

      Card all = new Card(this);
      campus.setText("All Activities For Today");
      mCards.add(all);

      mCards.remove(mCards.size() - 1);
      
      mCardScrollView = new CardScrollView(context);
      ExampleCardScrollAdapter adapter = new ExampleCardScrollAdapter();
      mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {

               @Override
               public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id)
               {
                  mAudioManager.playSoundEffect(Sounds.TAP);
                  
                  switch(position)
                  {
                  case 0:
                     new EventTask().execute(SPORTS);
                     break;
                  case 1:
                     new EventTask().execute(ARTS);
                     break;
                  case 2: 
                     new EventTask().execute(STUDENT_ACTIVITIES);
                     break;
                  case 3: 
                     new EventTask().execute(CAMPUS);
                     break;
                  }

               }

            });
      mCardScrollView.setAdapter(adapter);
      mCardScrollView.activate();

      
      // Update the view
      setContentView(mCardScrollView);

   }

   
   // This is the generic adapter for card scrolling and can be found on
   // https://developers.google.com/glass/develop/gdk/ui-widgets

   private class ExampleCardScrollAdapter extends CardScrollAdapter
   {

      @Override
      public int getPosition(Object item)
      {
         return mCards.indexOf(item);
      }

      @Override
      public int getCount()
      {
         return mCards.size();
      }

      @Override
      public Object getItem(int position)
      {
         return mCards.get(position);
      }

      @Override
      public int getViewTypeCount()
      {
         return Card.getViewTypeCount();
      }

      @Override
      public int getItemViewType(int position)
      {
         return mCards.get(position).getItemViewType();
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
         return mCards.get(position).getView(convertView, parent);

      }
   }

   private class EventTask extends AsyncTask<Integer, Void, ArrayList<Event>>
   {
      @Override
      protected void onPreExecute()
      {  
         //Executed before the thread begins
         super.onPreExecute();
         setContentView(R.layout.better_launch);
         downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
         //Simulate starting the downloadBar
         downloadBar.setVisibility(0);
      }

      // This access the WordPress api and finds information about the top 5
      // posted articles, storing them as article objects
      @Override
      protected ArrayList<Event> doInBackground(Integer... integers)
      {
         Log.d("Event", String.valueOf(integers[0]));
         EventParse event = new EventParse(integers[0]);
         return event.getEvents();
      }

      // After execution we need to update the UI and create the cards that will
      // be in our scroll view.
      @Override
      protected final void onPostExecute(ArrayList<Event> events)
      {
         
//         
//         //Create an instance of a calendar. This will be the object we use to compare all date objects
//         Calendar cal = Calendar.getInstance();
//         //Run through all the events
//         for(int i = 0; i < events.size(); i++){
//   			//compareTimes returns false, remove the event from the array list
//   			if(!compareTimes(events.get(i).getTime(), cal)){
//   				events.remove(i);
//   			}
//   		}
         
         //For all remaining events, run through the array list and add the event to a string array.
         //This allows us to easily send the information to the showEvent activity. 
         ArrayList<String> extra_strings = new ArrayList<String>();
         for (Event event : events)
         {
            extra_strings.add(event.prettyRepresentation()); 
         }
         
         Intent showEvents = new Intent(context, ShowEventsActivity.class);
         showEvents.putStringArrayListExtra("event_strings", extra_strings);
         Log.i("Activity started", "STARTING");
         //Dismiss the progress bar
         downloadBar.setVisibility(4);
         setContentView(mCardScrollView);
         startActivity(showEvents);
         

      }

   }
   
 //Find end time takes a string and returns a string that holds the time that an event will end.
  	//For instance, if an event is from 1:00am-2:00am, 2:00 will be returned.
  	public String findEndTime(String time){
  		//Find the separator
  		int firstTime = time.indexOf("-") + 1;
  		//Make a substring without the start time
  		String timeWithSuffix = time.substring(firstTime);
  		//Remove the am/pm suffix
  		//String timeWithoutSuffix = removeAlphaCharacters(timeWithSuffix);
  		//Return the formatted string without any leading or trailing whitespace
  		return timeWithSuffix.trim();
  		
  	}
  	
  	//RemoveAlphaCharacters is a method that takes a string and removes all the characters from it that aren't numeric
  	public String removeAlphaCharacters(String timeString){
  		//Regex is used to replace the am and pm in a time string
  		return timeString.replaceAll("[a-z?]", "");
  	}
  	
  	//CompareTimes takes a string of time and a calendar object. It then compares the two times and return true if the event hasn't already happened
  	//False if the event has already happened today. This allows us to make a microinteraction for our users. They will have no interest in going to 
  	//Events that already happened so this prevents them from having to scroll through unnecessary cards. 
  	public boolean compareTimes(String time, Calendar cal){
  		Calendar event = Calendar.getInstance();
  		DateFormat formatter = new SimpleDateFormat("h:mm a");
  		
  		try {
  			//Create a date object from this with today's current values. 
  			Date date = formatter.parse(time);
  			event.setTime(date);
  			event.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
  			
  			if(cal.getTime().compareTo(event.getTime()) < 0){
  				return true;	
  			}
  			else{
  				return false;
  			}
  		}
  		catch (ParseException e) {
  			//If there is a parse error, return false in general. This card must have some funky date. 
  			e.printStackTrace();
  			return false;
  		}
  		
  	}


}
