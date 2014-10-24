package com.example.allapps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
/*
 * This class displays a list of currently open campus dining facilities based on operation hours found in the XML file at the 
 * following location: http://apps.wku.edu/iwku/maps/places/MainCampus_Places.xml
 * 
 * This is accomplished with an AsyncTask. Open locations are displayed in a CardScrollView. Selecting a location by tapping its 
 * respective card provides the user with directions to that location by calling the StartDirectionsActivity class. The class
 * does contain some hardcoded values for the start and end of the school year because dining location operation hours are 
 * irregular during summer months. These values will need to be updated with time. Current values are as follows:
 * 
 * startOfYear August 25, 2014 (2014, 7, 25)
 * endOfYear May 15, 2015 (2015, 4, 15)
 * 
 * The current date is also being hardcoded for testing purposes. This will not be so when the app is complete.
 * 
 * Code written and commented by Lydia Buzzard
*/
public class FacilityHoursActivity extends Activity 
{
	//Context of the class
	Context ref = this;
	
	//Get the current date and time for comparisons
	Calendar now = Calendar.getInstance();
	
	//Get start/end of school year dates for comparisons. These will be hardcoded later
	Calendar startOfYear = Calendar.getInstance();
	Calendar endOfYear = Calendar.getInstance();
	
	//Initialize Progress Bar to display while open restaurants are retrieved
	ProgressBar downloadBar;
	
	//List of Cards for displaying open restaurants
	List<CardBuilder> allCards;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Set start/end of year
		startOfYear.set(2014, 7, 25);
		endOfYear.set(2015, 4, 15);
		
		//If the current date falls during the school year, start AsyncTask to find open restaurants
		if(now.after(startOfYear) && now.before(endOfYear))
		{
			//Start openNow AsyncTask
			new openNow().execute();
		}
		//Otherwise, if the current date is not during the school year, notify the user that the action cannot be completed
		else
		{
			//Create new card
			Card newCard = new Card(ref);
			//Set text of card to notify the user that open restaurants cannot be found
			newCard.setText("Restaurant hours are unavailable during summer months.");
			//Apologize to the user
			newCard.setFootnote("Sorry :(");
			//Display the card
			setContentView(newCard.getView());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.facility_hours, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class openNow extends AsyncTask<Void, Void, String[]>
	{
		//Local variables
		String name="", timeString="";
		ArrayList<String> allOpen = new ArrayList<String>();
		int weekday = now.get(Calendar.DAY_OF_WEEK), index=0, start=0, stop=0, openHr=0, closeHr, openMin, closeMin, currentHr,
				currentMin;
		Date currentTime = now.getTime();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		String time= timeFormat.format(currentTime);
		
		//onPreExecute method is called as the AsyncTask begins. This displays a loading screen.
		protected void onPreExecute()
		{
			//Executed before the thread begins
	         super.onPreExecute();
	         setContentView(R.layout.better_launch);
	         downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
	         //Simulate starting the downloadBar
	         downloadBar.setVisibility(0);
		}
		protected String[]  doInBackground(Void...voids)
		{
			
			try
			{
				//Create URL from url String and create connection
				URL newUrl = new URL("http://apps.wku.edu/iwku/maps/places/MainCampus_Places.xml");
				HttpURLConnection conn =(HttpURLConnection) newUrl.openConnection();
				
				//For reading data from URLs
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer bufferReader = new StringBuffer();
				String inputLine;
							      
				//Run through the connection and store the file into the buffer reader. 
				while ((inputLine = in.readLine()) != null)
				bufferReader.append(inputLine+"\n");
				
				in.close();
				
				//Sets the index to that of the first category, which is dining.
				index = bufferReader.indexOf("<category>");
				
				//Set index to that of the next place/dining location
				index = bufferReader.indexOf("<place>", index);
				
				if(weekday >= 2 &&
						weekday <= 5)
				{
					
					while(index < bufferReader.indexOf("</category>"))
					{
						//Set the index to that of the current location's name
						start = bufferReader.indexOf("<name>", index)+6;
						//Set index to the end of the name tag
						stop = bufferReader.indexOf("</name>", start);
						//Set the name of the current location to what is contained in the name tag
						name = bufferReader.substring(start, stop);
						index = stop;
						
						//Set index to that of the current location's schedule
						index = bufferReader.indexOf("<schedule>", index);
						//Set the index to that of the current location's Monday schedule
						start = bufferReader.indexOf("<monday>", index)+8;
						//Set index to the end of the Monday tag
						stop = bufferReader.indexOf("</monday>", start);
						//Set the timeString of the current location to what is contained in the Monday tag
						timeString = bufferReader.substring(start, stop);
						index = stop;
						
						//Set index to that of the next place/dining location
						index = bufferReader.indexOf("<place>", index);
					
						compareTimes();
					}
				}
				
				else if(weekday ==6)
				{
					while(index < bufferReader.indexOf("</category>"))
					{
					
						//Set the index to that of the current location's name
						start = bufferReader.indexOf("<name>", index)+6;
						//Set index to the end of the name tag
						stop = bufferReader.indexOf("</name>", start);
						//Set the name of the current location to what is contained in the name tag
						name = bufferReader.substring(start, stop);
						index = stop;
						
						//Set index to that of the current location's schedule
						index = bufferReader.indexOf("<schedule>", index);
						//Set the index to that of the current location's Friday schedule
						start = bufferReader.indexOf("<friday>", index)+8;
						//Set index to the end of the Monday tag
						stop = bufferReader.indexOf("</friday>", start);
						//Set the timeString of the current location to what is contained in the Friday tag
						timeString = bufferReader.substring(start, stop);
						index = stop;
						
						//Set index to that of the next place/dining location
						index = bufferReader.indexOf("<place>", index);
						
						compareTimes();
					}
				}
				
				else if(weekday == 7)
				{
					while(index < bufferReader.indexOf("</category>"))
					{
						//Set the index to that of the current location's name
						start = bufferReader.indexOf("<name>", index)+6;
						//Set index to the end of the name tag
						stop = bufferReader.indexOf("</name>", start);
						//Set the name of the current location to what is contained in the name tag
						name = bufferReader.substring(start, stop);
						index = stop;
						
						if(bufferReader.indexOf("<saturday>", index)<bufferReader.indexOf("</saturday>", index))
						{
						//Set index to that of the current location's schedule
						index = bufferReader.indexOf("<schedule>", index);
						//Set the index to that of the current location's Monday schedule
						start = bufferReader.indexOf("<saturday>", index)+10;
						//Set index to the end of the Monday tag
						stop = bufferReader.indexOf("</saturday>", start);
						//Set the timeString of the current location to what is contained in the Saturday tag
						timeString = bufferReader.substring(start, stop);
						index = stop;
						}
						
						//Set index to that of the next place/dining location
						index = bufferReader.indexOf("<place>", index);
						
						compareTimes();
						
					}
				}
				
				else if(weekday == 1)
				{
					while(index < bufferReader.indexOf("</category>"))
					{
				
						//Set the index to that of the current location's name
						start = bufferReader.indexOf("<name>", index)+6;
						//Set index to the end of the name tag
						stop = bufferReader.indexOf("</name>", start);
						//Set the name of the current location to what is contained in the name tag
						name = bufferReader.substring(start, stop);
						index = stop;
						
						//Set index to that of the current location's schedule
						index = bufferReader.indexOf("<schedule>", index);
						//Set the index to that of the current location's Monday schedule
						start = bufferReader.indexOf("<sunday>", index)+8;
						//Set index to the end of the Sunday tag
						stop = bufferReader.indexOf("</sunday>", start);
						//Set the timeString of the current location to what is contained in the Sunday tag
						timeString = bufferReader.substring(start, stop);
						index = stop;
						
						//Set index to that of the next place/dining location
						index = bufferReader.indexOf("<place>", index);
						
						compareTimes();
				
					}
				}
			
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			//Convert the ArrayList to a String[]
			String[] openList = new String[1];
			openList = allOpen.toArray(openList);
			//Return the String[]
			return openList;
		}
		//This method compares the current time with the operation hours of a location. If the location is currently open, it will
		//be added to the array of open locations.
		public void compareTimes()
		{
			//Retrieve current hour and current minute from "time" String
			currentHr = Integer.valueOf(time.substring(0, time.indexOf(":")));
			currentMin = Integer.valueOf(time.substring(time.indexOf(":")+1, time.length()-1));
			
			try
			{
				//Assign open/close hours and minutes
				openHr = Integer.valueOf(timeString.substring(0, timeString.indexOf(":")));
				openMin = Integer.valueOf(timeString.substring(timeString.indexOf(":")+1, timeString.indexOf("am")));
				closeHr = Integer.valueOf((timeString.substring(timeString.indexOf("-")+1, timeString.indexOf(":", 
						timeString.indexOf("-")))).trim())+12;
				closeMin = Integer.valueOf((timeString.substring(timeString.indexOf(":", timeString.indexOf("-"))+1, 
						timeString.indexOf("pm"))).trim());
				
				//Compare current time with that of the facility's open/close times
				if((currentHr > openHr && currentHr < closeHr) ||
						(currentHr == openHr && currentMin >= openMin) ||
						(currentHr == closeHr && currentMin < closeMin))
				{
					//If the current time is between the facility's open and close times, add the facility to the allOpen
					//ArrayList
					allOpen.add(name);
				}
			}
			catch(Exception e)
			{
				if(timeString.contains("|"))
				{
					//Assign open/close hours and minutes
					openHr = Integer.valueOf(timeString.substring(0, timeString.indexOf(":")));
					openMin = Integer.valueOf(timeString.substring(timeString.indexOf(":")+1, timeString.indexOf("am")));
					closeHr = Integer.valueOf((timeString.substring(timeString.indexOf("-")+1, timeString.indexOf(":", 
							timeString.indexOf("-")))).trim())+12;
					closeMin = Integer.valueOf((timeString.substring(timeString.indexOf(":", timeString.indexOf("-"))+1, 
							timeString.indexOf("pm"))).trim());
					
					//Compare current time with that of the facility's open/close times
					if((currentHr > openHr && currentHr < closeHr) ||
							(currentHr == openHr && currentMin >= openMin) ||
							(currentHr == closeHr && currentMin < closeMin))
					{
						//If the current time is between the facility's open and close times, add the facility to the allOpen
						//ArrayList
						allOpen.add(name);
					}
					
					index = timeString.indexOf("|")+1;
					
					//Assign open/close hours and minutes
					openHr = Integer.valueOf(timeString.substring(0, timeString.indexOf(":", index)));
					openMin = Integer.valueOf(timeString.substring(timeString.indexOf(":", index)+1, timeString.indexOf("am", 
							index)));
					closeHr = Integer.valueOf((timeString.substring(timeString.indexOf("-", index)+1, timeString.indexOf(":", 
							timeString.indexOf("-", index)))).trim())+12;
					closeMin = Integer.valueOf((timeString.substring(timeString.indexOf(":", timeString.indexOf("-", index))+1, 
							timeString.indexOf("pm", index))).trim());
					
					//Compare current time with that of the facility's open/close times
					if((currentHr > openHr && currentHr < closeHr) ||
							(currentHr == openHr && currentMin >= openMin) ||
							(currentHr == closeHr && currentMin < closeMin))
					{
						//If the current time is between the facility's open and close times, add the facility to the allOpen
						//ArrayList
						allOpen.add(name);
					}
					
				}
			}
		}
		
		protected void onPostExecute(String[] open)
		{
			//Local variables
			CardBuilder newCard;
			allCards = new ArrayList<CardBuilder>();
			CardScrollView scroll = new CardScrollView(ref);
			ScrollAdapter adapter = new ScrollAdapter(allCards);
			
			//If any locations are open, add them to the list
			if(open.length!=0)
			{
				
				for(String name : open)
				{
					if(name != null && name != "")
					{
						//Create new Card object
						newCard = new CardBuilder(ref, CardBuilder.Layout.TEXT);
						//Set text of card to the name of the restaurant
						newCard.setText(name);
						//Set footnote to notify user of a possible interaction (tapping for directions
						newCard.setFootnote("Tap for directions.");
						//Add newly created card to the card list
						allCards.add(newCard);
					}
				}
				//Set onItemClickListener to start Directions to a location when a card is selected
				scroll.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
					{
					   //Another instance of getText() being replaced
						//Get text of card to set as location title
						String location=(String) allCards.get(position).toString();
						Log.i("LOCATION", location);
						//Create new intent calling the StartDirectionsActivity class
						Intent intent = new Intent(ref, StartDirectionsActivity.class);
						//Attach the location title as an Extra with the intent
						intent.putExtra("Place", location);
						//Start the intent
						startActivity(intent);
					}
				});
				//Set adapter of the scroll view to the OpenCardScrollAdapter
				scroll.setAdapter(adapter);
				//Activate the CardScrollView
				scroll.activate();
				//Set content view to the scroll view
				setContentView(scroll);
			}
			//If no locations are open, create a card that notifies the user
			else
			{
				newCard = new Card(ref);
				//Set card text to tell user that no locations are open
				newCard.setText("No locations are currently open.");
				//Apologize to user in footnote
				newCard.setFootnote("Sorry :(");
				//Display the card
				setContentView(newCard.getView());
			}
			
			
			
		}
	}

}
