package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
/*
 * 
 * 
 * This class displays the Fresh Menu for the next available meal, decided by either the default times for each meal or by user
 * preferences. The default settings for weekdays are as follows: if the user accesses the menu before 11AM, the breakfast menu is 
 * displayed. If the user accesses the menu before 3PM but after 11AM, the lunch menu is displayed. If the user accesses the menu 
 * at any time after 3PM, the dinner menu is displayed. The default settings for weekends are as follows: if the user accesses the 
 * menu before 2PM, the brunch menu is displayed. If the user accesses the menu after 2PM, the dinner menu is displayed.
 * 
 * If the current date is during the summer, the class notifies the user (with a Card) that the Fresh Menu is unavailable during
 * summer months. Start/End dates of the school year must be hardcoded for each school year. As it stands, the current start/end
 * dates are 
 * 
 * startOfYear: August 25, 2014 (2014, 7, 25)
 * endOfYear: May 15, 2015 (2015, 4, 15)
 * 
 * 
 * Meal Ids must be added to the menu site URL in the createURL method. The meal Ids are as follows: breakfast = 1, brunch = 639,
 * lunch = 16, and dinner = 17. These have been hardcoded and can be found as static, final variables at the start of the class.
 * 
 * Code written and commented by Lydia Buzzard
 * 
 */
public class DisplayMenuActivity extends Activity 
{
	//This string will later contain the items on the Fresh Menu
	String menu;
	//List of Cards to display each station at Fresh
	List<CardBuilder> mCards = new ArrayList<CardBuilder>();
	//List of cards to display possible meal options, if microinteractions are turned off.
	List<CardBuilder> mealCards = new ArrayList<CardBuilder>();
	//Calendar object to determine the current time and date.
	Calendar now = Calendar.getInstance();
	
	//Card to display the lack of a menu, if necessary
	CardBuilder newCard;
	//Create Calendar instance for the start of the school year
	Calendar startOfYear = Calendar.getInstance();
	//Create Calendar instance for the end of the school year
	Calendar endOfYear = Calendar.getInstance();
	//Create Calendar instance for breakfast. A time will later be set for this Calendar object to be used in comparisons
	Calendar breakfast= Calendar.getInstance();
	//Create Calendar instance for lunch. A time will later be set for this Calendar object to be used in comparisons
	Calendar lunch = Calendar.getInstance();
	//Create Calendar instance for dinner. A time will later be set for this Calendar object to be used in comparisons
	Calendar dinner = Calendar.getInstance();
	//Create Calendar instance for brunch. A time will later be set for this Calendar object to be used in comparisons
	Calendar brunch = Calendar.getInstance();
	//Boolean set based on whether or not the menu is available
	boolean available=true;
	//SharedPreferences object for saving preferred meal times.
	SharedPreferences mealTimes;
	//SharedPreferences object for checking the current microinteraction settings
	SharedPreferences micro;
	//Boolean value to contain the microinteraction setting
	boolean microOn;
	
	//Meal IDs
	private static final String breakfastId = "1";
	private static final String brunchId = "639";
	private static final String lunchId = "16";
	private static final String dinnerId = "17";

	//String to contain the meal Id
	static String id="";
	int startTime, endTime, count=0;
	public static List<String> info = new ArrayList<String>();
	
	//Progress Bar to display while the menu is loading.
	ProgressBar downloadBar;

	//OnResume retrieves the current time to be set as the "start" time of an interaction (testing only)
	protected void onResume(){
	   super.onResume();
	   if(count!=1)
	   {
		   startTime = (int)System.currentTimeMillis();
		   count=1;
	   }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//Calls onResume method if it has yet to be called (testing only)
		if(count==0)
		{
			onResume();
		}
		//Call setDates method to set values for start/end of year dates. Also, current date, for testing
		setDates();
		
		//Initialize the microinteraction settings
		micro = getSharedPreferences("Microinteractions", Context.MODE_PRIVATE);
		//Set microOn to the boolean value stored in micro. If no value is stored, set the default value to true.
		microOn = micro.getBoolean("Value", true);
		
		//If a meal Id was passed with the previous intent, display the meal it indicates
		try
		{
			//Get previous Intent
			Intent previous = getIntent();
			
			if(previous.getStringExtra("Meal Id") != null)
			{
			//Store Meal Id from previous intent into a String variable
			id = previous.getStringExtra("Meal Id");
			
			//Create the URL
			String urlString = createURL(dateString(), id);
			startDisplay(urlString);
			}
			//Make sure that this gets thrown to our catch block
			else
			{
			   throw new Exception();
			}
		}
		//If a meal Id was not passed with the previous intent, determine which meal (if any) should be displayed
		catch(Exception e)
		{
			
			//If no menu is available, notify the user with a card
			if(!menuAvailable())
			{
				newCard = new CardBuilder(this, CardBuilder.Layout.TEXT);
				newCard.setText("Fresh Menu is unavailable in summer months.");
				newCard.setFootnote("Sorry :(");
				setContentView(newCard.getView());
			}
			//If the menu is available, check first if microinteractions are enabled
			else
			{
				checkMicrointeractions();
			}
		}
		
	}
	//This method checks whether or not microinteractions are enabled using sharedPreferences
	public void checkMicrointeractions()
	{
	 //If microinteractions are enabled, set meal Id using user preferences.
       if(microOn)
       {
           //Check for/apply shared preferences
           getPreferences();
           //Set meal Id
           setMeal();
           
           
           //Create the URL
           String urlString = createURL(dateString(), id);
           startDisplay(urlString);
       }
       //If microinteractions are disabled, allow user to choose meal manually
       else
       {
          microOff();
       }
	}
	
	
	//This method creates and displays a cardScrollView so the user can select a meal option manually, for when microinteractions
	//are turned off
	public void microOff()
	{
		//get the current day of the week
		int day = now.get(Calendar.DAY_OF_WEEK), counter;
		//String array list to contain names of all available meals for the given day of the week
		ArrayList<String> meals = new ArrayList<String>();
		
		//If it is a weekday, add all meal names except for Brunch
		if(day>=2 && day<=6)
		{
			meals.add("Breakfast");
			meals.add("Lunch");
			meals.add("Dinner");
		}
		//If it is a weekend, only add brunch and dinner
		else
		{
			meals.add("Brunch");
			meals.add("Dinner");
		}
		//for each element in "meals," create a new card with the meal contained in that element as its text. Then add it to the
		//card list
		for(counter=0; counter<meals.size(); counter++)
		{
		   CardBuilder newCard = new CardBuilder(this, CardBuilder.Layout.TEXT);
			newCard.setText(meals.get(counter));
			newCard.setFootnote("Tap to view menu.");
			mealCards.add(newCard);
			
		}
		//Initalize a new card scroll view to display mealCards
		CardScrollView cardScroll = new CardScrollView(this);
		//Initalize a new scroll adapter that contains mealCards
		ScrollAdapter adapter = new ScrollAdapter(mealCards);
		//Set an onItemClickListener for the scroll view to react to taps
		cardScroll.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
			{
			   /**
			    * READ THIS. .getText() is now deprecated! 
			    */
				String name = mealCards.get(position).toString();
				      //mealCards.get(position).getText().toString();
				//When user taps, check which meal he/she has selected
				switch(name)
				{
				case "Breakfast":
					DisplayMenuActivity.id = "1";
					break;
				case "Brunch":
					DisplayMenuActivity.id="639";
					break;
				case "Lunch":
					DisplayMenuActivity.id="16";
					break;
				case "Dinner":
					DisplayMenuActivity.id="17";
					break;
				}
				//Call startDisplay and createURL to begin the AsyncTask to display the menu
				startDisplay(createURL(dateString(), DisplayMenuActivity.id));
			}
		});
		
		//Set the CardScrollView Adapter
		cardScroll.setAdapter(adapter);
		//Activate the CardScrollView
		cardScroll.activate();
		//Set the content view to the card scroll view
		setContentView(cardScroll);
	}
	
	//This method creates a date string using the current month, day, and year, and assembles them in a format that can be used
	//in the assembly of a menu URL.
	public String dateString()
	{
		//Get current month from the date
		int month = now.get(Calendar.MONTH)+1;
		//Get current day of month from date
		int day = now.get(Calendar.DAY_OF_MONTH);
		//Get current year from date
		int year = now.get(Calendar.YEAR);
	
	
		//Combine day, month, and year into a string that can be used in the menu URL
		String dateString = Integer.toString(month)+"_"+Integer.toString(day)+"_"+Integer.toString(year);
		
		return dateString;
	}
	
	//This method sets dates for the start/end of year as well as for the current date (only applicable during testing)
	public void setDates()
	{
		//Set values for the start/end of the school year
		endOfYear.set(2015, 4, 15);
		startOfYear.set(2014, 7, 25);
	}
	
	//This method retrieves user preferences set within the FreshMenuSettings activity. If no preferences have been set, default
	//values/times are used.
	public void getPreferences()
	{
		mealTimes=getSharedPreferences("Menu Times", Context.MODE_PRIVATE);
		
		//Set preferred breakfast time. If the user has not set a preferred time, set to default (11:00AM)
		breakfast.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), mealTimes.getInt("Breakfast Hour", 11), 
				mealTimes.getInt("Breakfast Min", 00));
		//Set preferred lunch time. If the user has not set a preferred time, set to default (3:00PM)
		lunch.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), mealTimes.getInt("Lunch Hour", 15), 
				mealTimes.getInt("Lunch Min", 00));
		//Set preferred dinner time. If the user has not set a preferred time, set to default (8:00PM)
		dinner.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), mealTimes.getInt("Dinner Hour", 20), 
				mealTimes.getInt("Dinner Min", 00));
		//Set preferred brunch time. If the user has not set a preferred time, set to default (2:00PM)
		brunch.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), mealTimes.getInt("Brunch Hour", 14), 
				mealTimes.getInt("Brunch Min", 00));
	}
	
	//This method compares the current date to the start/end of the school year. If the date is not during the school year, it sets
	//the boolean variable "available" to false, to signify that the Fresh Menu is not available online. Otherwise, "available"
	//is set to true. It then returns "available."
	public boolean menuAvailable()
	{
		//If the current date is not during the regular school year, notify the user that the menu is unavailable.
		if(now.after(startOfYear)&&now.before(endOfYear))
		{
			available=true;
		}
		else
		{
			available=false;
		}
		
		return available;
	}
	
	//This method compares the current day of the week to others to determine whether or not the current day is a weekday. If so,
	//the method then compares the current time with the set breakfast, lunch, and dinner times, and sets the meal Id accordingly.
	//If the current date is a weekend, the method then compares the current time with the set brunch and dinner times and sets the
	//meal Id accordingly.
	public void setMeal()
	{
		if(now.get(Calendar.DAY_OF_WEEK)>=2&&
				now.get(Calendar.DAY_OF_WEEK)<=6)
		{
		if(now.before(breakfast))
		{
			id = breakfastId;
		}
		else if(now.before(lunch))
		{
			id = lunchId;
		}
		else if(now.before(dinner))
		{
			id = dinnerId;
		}
		}
		else
		{
			
			if(now.before(brunch))
			{
				id = brunchId;
			}
			else if(now.before(dinner))
			{
				id = dinnerId;
			}
			else
			{
				id = dinnerId;
			}
			
		}
		
	}
	
	//This method calls the DownloadWebpageTask AsyncTask to download/display the menu using the URL passed from OnCreate
	public void startDisplay(String url)
	{
		
		new DownloadWebpageTask().execute(url);
	}
	
	//This method creates a URL string to display the Fresh menu given a date and meal Id in the form of string. It then returns 
	//the url as a String.
	public String createURL (String date, String mealId)
	{
		//Local variables
		String first="http://www.campusdish.com/en-US/CSMA/WesternKentucky/Menus/TheFreshFoodCompany.htm?LocationName=The%20Fresh%20Food%20Company&MealID=";
		String mid="&OrgID=157951&Date=";
		String last="&ShowPrice=False&ShowNutrition=True";
		
		//Define full url
		String fullUrl = first+mealId+mid+date+last;
		
		//Return full url
		return fullUrl;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		int weekday = now.get(Calendar.DAY_OF_WEEK);
		
		//If it is a weekday, display the weekday meal options (excludes brunch)
		if(weekday>=2 &&
				weekday<=6)
		{
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.meal_options_week, menu);
			return true;
		}
		//If it is a weekend, display the weekend meal options (includes brunch)
		else
		{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.meal_options, menu);
		return true;
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		switch(id)//Depending on what meal is being displayed, adjust the menu options. For example, if the breakfast menu is 
		//being displayed, breakfast should not appear as an option in the menu because it is already being shown to the user.
		{
		case "1": 
			menu.removeItem(R.id.breakfast);
			return true;
		case "639":
			menu.removeItem(R.id.brunch);
			return true;
		case "16":
			menu.removeItem(R.id.lunch);
			return true;
		case "17":
			menu.removeItem(R.id.dinner);
			return true;
		default:
			return super.onPrepareOptionsMenu(menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//Define the intent
		Intent intent = new Intent(this, DisplayMenuActivity.class);
		switch(id)
		{
		case R.id.breakfast:
			//Assign breakfast meal ID
			intent.putExtra("Meal Id","1");
			//Start the Activity
			startActivity(intent);
			return true;
		case R.id.brunch:
			//Assign brunch meal ID
			intent.putExtra("Meal Id", "639");
			//Start the Activity
			startActivity(intent);
			return true;
		case R.id.lunch:
			//Assign lunch meal ID
			intent.putExtra("Meal Id", "16");
			//Start the Activity
			startActivity(intent);
			return true;
		case R.id.dinner:
			//Assign dinner meal ID
			intent.putExtra("Meal Id", "17");
			//Start the Activity
			startActivity(intent);
			return true;
		
		case R.id.action_settings:
			intent = new Intent(this, FreshMenuSettings.class);
			startActivity(intent);
			return true;
		
		case R.id.wku_main_menu:
			intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			return true;
			
		};
		
		return super.onOptionsItemSelected(item);
	}
	
	//This class is an AsyncTask that downloads and displays the Fresh Menu for a given day/meal in the form of a CardScrollView
	private class DownloadWebpageTask extends AsyncTask<String,Void, ArrayList<String>>
	{
		//Initialize a card
		CardBuilder newCard;
		
		//This method is executed before doInBackground and displays a loading screen that remains visible until the menu is
		//ready.
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
		//This method contains the majority of the logic and computation of the class and downloads/stores the Fresh Menu. It
		//then passes these menu items in the form of a String ArrayList with each item in the ArrayList representing the menu
		//for a station at Fresh.
		@Override
		protected ArrayList<String> doInBackground(String...params)
		{
			ArrayList<String> allMenu = new ArrayList<String>();
			String menu="";
			try{
				//Create URL from url String and create connection
				URL newUrl = new URL(params[0]);
				HttpURLConnection conn =(HttpURLConnection) newUrl.openConnection();
				
				
				//For reading data from URLs
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer bufferReader = new StringBuffer();
				String inputLine;
							      
				//Run through the connection and store the file into the buffer reader. 
				while ((inputLine = in.readLine()) != null)
				bufferReader.append(inputLine+"\n");
				
				in.close();
				
				int currentIndex=0;
				String stationLabel="",item="";
				
				currentIndex=bufferReader.indexOf("menuBorder")+1;
				
				
				//Determine if any meal options are available for the given day/meal.
				if(bufferReader.indexOf("recipeLink")!=-1&&
						bufferReader.indexOf("menuTxt",currentIndex)<bufferReader.indexOf("menuBorder",currentIndex))
				{
					currentIndex=0;
					while(bufferReader.indexOf("ConceptTabText", currentIndex)!=-1)
					{
						//Get the station name from the string buffer
						currentIndex=bufferReader.indexOf("ConceptTabText")+16;
						bufferReader.delete(0,currentIndex);
						currentIndex= bufferReader.indexOf("</td>");
						stationLabel= bufferReader.substring(0,currentIndex);
						
						menu=stationLabel;
						
						currentIndex=bufferReader.indexOf("menuBorder",currentIndex)+1;
						
						while(bufferReader.indexOf("menuBorder", currentIndex)>bufferReader.indexOf("menuTxt",currentIndex))
						{
						//Get the available menu items for each station
						currentIndex=bufferReader.indexOf("recipeLink")+12;
						bufferReader.delete(0, currentIndex);
						currentIndex=bufferReader.indexOf("</a>");
						item=bufferReader.substring(0,currentIndex);
						
						//Remove HTML special characters from the item string
						item = StringEscapeUtils.unescapeHtml4(item).replaceAll(
			                     "[^\\x20-\\x7e]", "");
						item=item.toLowerCase();
						item="-"+item;
						
						//Add each item to the menu
						menu=menu+"\n"+item;
						};
						
						
						allMenu.add(menu);
						menu="";
					}
				}
				else
					menu = "No menu options are available for the day.";
					allMenu.add(menu);
				}
			
			catch(MalformedURLException d)
			{
				menu="Menu unavailable.";
				allMenu.add(menu);
			}
			catch(IOException e)
			{
				menu="Menu unavailable.";
				allMenu.add(menu);
			};
			
			//Remove any blank items (to prevent the creation of blank cards)
			if(allMenu.size()>1)
			allMenu.remove(allMenu.indexOf(""));
			
			return allMenu;
		}
		//This method is the last to be executed and formats the menu passed by doInBackground into a CardScrollView that is 
		//displayed to the user. In this method, the loading screen displayed in onPreExecute is removed. 
		@Override
		protected void onPostExecute (ArrayList<String> menu)
		{
			if(menu.size()>=1)
			{
				//Create a new Card
				newCard = new CardBuilder(DisplayMenuActivity.this, CardBuilder.Layout.TEXT);
				//Set the Info card depending on the meal ID
				switch(id)
				{
				case "1": newCard.setText("Today's Breakfast Menu");
				break;
				case "639": newCard.setText("Today's Brunch Menu");
				break;
				case "16": newCard.setText("Today's Lunch Menu");
				break;
				case "17": newCard.setText("Today's Dinner Menu");
				break;
				};
				newCard.setFootnote("Swipe to view or tap for options");
				//Add card to the list
				mCards.add(newCard);
				
				//For each station, add the text of that station's menu to a Card and add that card to the list.
				for(int counter=0; counter<menu.size(); counter++)
				{
					//Create new card
					newCard = new CardBuilder(DisplayMenuActivity.this, CardBuilder.Layout.TEXT);
					
					//Set text of the card to the menu items for that station
					newCard.setText(menu.get(counter));
					//Add card to the list
					mCards.add(newCard);
					
					
				};
			//Create a new CardScrollView object
			CardScrollView menuScroll = new CardScrollView(DisplayMenuActivity.this);
			
			//Set listener for the CardScrollView to generate an action when the user selects a card.
			menuScroll.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
				{
					//When user taps, open the options menu
					openOptionsMenu();
						
				}
			});
			//Create a new ScrollAdapter object
			ScrollAdapter menuAdapter = new ScrollAdapter(mCards);
			//Set the adapter of the CardScrollView to the previously created adapter
			menuScroll.setAdapter(menuAdapter);
			//Activate the ScrollView
			menuScroll.activate();
			
			//Set progress bard visibility to 4
			downloadBar.setVisibility(4);
			//Set the content view to the Scroll View
			setContentView(menuScroll);
			
			
			}
			//If there are no contents in the menu, state that the menu is unavailable
			else if(menu.size()==0)
			{
				//Create new card
				newCard = new CardBuilder(DisplayMenuActivity.this, CardBuilder.Layout.ALERT);
				//Notify the user that the menu is unavailable
				newCard.setText("Menu is not available.");
				//Remove loading screen
				downloadBar.setVisibility(4);
				//Display new card to the user
				setContentView(newCard.getView());
			};
			
			//Stop the time
	        endTime = (int) System.currentTimeMillis();
	        Log.d("End time",String.valueOf(endTime));
	        Log.d("Start time", String.valueOf(startTime));
	        //Find the time by subtracting
	        String time = String.valueOf(endTime - startTime);
	        Log.d("Microinteractions", String.valueOf(microOn));
	        Log.d("Fresh Menu time", time);
		}

	}
	
	
	}