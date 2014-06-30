package com.example.allapps;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.glass.app.Card;

/*The following class chooses a meal to display based on the current time. If the date of use is not during the regular school year, it will notify
 * the user that the menu is unavailable because The Fresh Food Co. does not publish its menu during the summer months. Otherwise, if the current
 * time is before 11AM (or the user's chosen breakfast time) the Breakfast meal ID (1) will be sent to the DisplayMenuActivity. If the current
 * time is after the user's chosen breakfast time but before the user's chosen lunch time (the default of which is 3PM), then the Lunch meal ID (16)
 * will be sent to the DisplayMenuActivity. If the current time is after the user's chosen lunch time but before the user's chosen dinner time (the 
 * default of which is 8PM), then the Dinner meal ID (17) will be sent to the DisplayMenuActivity. If the current day is a weekend, then the times
 * for breakfast and lunch will not be checked (because they are not served) in exchange for brunch.
 * 
 * Dates for the start/end of the school year are hardcoded within the code and will need to be updated manually.
 * 
 * DATE FORMAT
 * (YYYY, MM, DD)
 * with January being "0"
 * startOfYear = (2014, 7, 25) August 25, 2014
 * endOfYear = (2014, 4, 16) May 16, 2014
 * 
 */
public class MealOptionsActivity extends Activity 
{
	//Card to display the lack of a menu, if necessary
	Card newCard;
	//Create Calendar instance for the current time
	Calendar now = Calendar.getInstance();
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
	
	//Meal IDs
	private static final String breakfastId = "1";
	private static final String brunchId = "639";
	private static final String lunchId = "16";
	private static final String dinnerId = "17";
	
	//String to contain mealId
	String id = "1"; //Default is 1 (breakfast)
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Log.d("MealOptionsActivity", "called");
		
		mealTimes=getSharedPreferences("Menu Times", Context.MODE_PRIVATE);
		
		//Set values for the start/end of the school year
		endOfYear.set(2014, 4, 16);
		startOfYear.set(2014, 7, 25);
		//Set fake current date for testing
		now.set(2014, 4, 5);
	
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
		
		//If menu is unavailable, notify the user.
		if(!menuAvailable())
		{
			newCard = new Card(this);
			newCard.setText("Fresh Menu is unavailable in summer months.");
			newCard.setFootnote("Sorry :(");
			setContentView(newCard.getView());
		}
		//If the current date is during the regular school year, call setMeal method to determine which meal menu should be
		//displayed
		else
		{
			//Call setMeal to determine which menu should be displayed
			setMeal();
			//Call startDisplay to call DisplayMenuActivity using the set mealId.
			startDisplay();
		}
		
	}
	
	//This method compares the current date to the start/end of the school year. If the date is not during the school year, it sets
	//the boolean variable "available" to false, to signify that the Fresh Menu is not available online. Otherwise, "available"
	//remains true. It then returns "available."
	public boolean menuAvailable()
	{
		//If the current date is not during the regular school year, notify the user that the menu is unavailable.
		if(now.after(endOfYear)&&now.before(startOfYear))
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
			
		}
		
	}
	
	//This method 
	public void startDisplay()
	{
		Intent intent = new Intent(this, DisplayMenuActivity.class);
		intent.putExtra("Meal Id", id);
		startActivity(intent);
	}
		

	//Method to set a resulting action for when the trackpad is tapped.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		 switch(keyCode)
		 {
		    case KeyEvent.KEYCODE_DPAD_CENTER:
		    case KeyEvent.KEYCODE_ENTER:
		         
		    	//ACTION HERE
		    	//Open the options menu when tapped, but only when the menu is available
		    	if(available==true)
		    	{
		    	openOptionsMenu();
		    	}
		         
		    return true;
		         
		    default:
		    	return super.onKeyDown(keyCode, event);
		            
		  }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		return super.onOptionsItemSelected(item);
	}


}
