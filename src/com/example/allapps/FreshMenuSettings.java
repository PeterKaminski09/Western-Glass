package com.example.allapps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
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
 * This class sets and stores user preferences for the Fresh Menu portion of the application. When microinteractions are enabled,
 * the user is shown the next available meal based on the current time in relation to set cut-off times for each meal. For
 * example, the default cut-off time for breakfast is 11AM. If the user accesses the Fresh Menu prior to 11AM, the breakfast
 * menu will be displayed because it is the next available meal. The user has the option to override this decision by tapping to 
 * view other options. In this class, the user is given the option to change these cut-off times using speech recognition. After
 * selecting a meal for which the cut-off time should be changed, the user will be lead through a series of prompts to obtain
 * the appropriate information. The prompts are as follows:
 * 
 * Select hour.
 * Select minute.
 * AM or PM?
 * 
 * If a problem is found with the user's responses, he/she will be invited to try speaking them again. These preferences are 
 * saved using a SharedPreferences object and are accessed by the DisplayMenuActivity class.
 * 
 * Code written and commented by Lydia Buzzard
 */
public class FreshMenuSettings extends Activity 
{
	//Card list to hold all settings options
	List<Card> allCards;
	//Card instance to add to above card list
	Card newCard;
	//Scroll view for the card list of options
	CardScrollView optionScroll;
	//Adapter to supply scroll view with card list
	ScrollAdapter optionAdapter;
	//SharedPreferences object for saving user input
	SharedPreferences mealTimes;
	//SharedPreferences object for recording user input
	SharedPreferences.Editor editor;
	//Key to set the preference to be changed.
	String key="";
	//Speech Request for Speech Recognizer
	private final int SPEECH_REQUEST = 0;
	//Strings to save the new hour/minute the user has spoken
	String newHour="", newMin="";
	//Boolean value set based on whether the user meant for the time to be AM or PM
	boolean am=true;
	//Count to record number of times the speech recognizer has been displayed
	int count=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Initialize all local variables
		allCards = new ArrayList<Card>();
		optionScroll = new CardScrollView(this);
		optionAdapter = new ScrollAdapter(allCards);
		mealTimes= getSharedPreferences("Menu Times", Context.MODE_PRIVATE);
		editor= mealTimes.edit();
		
		//Create a card for each possible meal to be changed
		newCard = new Card(this);
		newCard.setText("Set Breakfast Cut-Off Time");
		newCard.setFootnote("Display breakfast menu until this time");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Set Brunch Cut-Off Time");
		newCard.setFootnote("Display brunch menu until this time (weekends only)");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Set Lunch Cut-Off Time");
		newCard.setFootnote("Display lunch menu until this time");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Set Dinner Cut-Off Time");
		newCard.setFootnote("Display dinner menu until this time");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Delete saved preferences.");
		newCard.setFootnote("Restores all meal times to default settings.");
		allCards.add(newCard);
		//Set adapter for scroll view
		optionScroll.setAdapter(optionAdapter);
		//Set listener for the CardScrollView to generate an action when the user selects a card.
		optionScroll.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
			{
				//Set the preference key depending on what meal time the user would like to change
				switch(position)
				{
				case 0:
					key="Breakfast";
					startSpeech();
					break;
				case 1:
					key="Brunch";
					startSpeech();
					break;
				case 2:
					key="Lunch";
					startSpeech();
					break;
				case 3:
					key="Dinner";
					startSpeech();
					break;
				case 4:
					restoreDefault();
					break;
				};
				
					
			}
		});
		optionScroll.activate();
		//Display the scroll view to the user
		setContentView(optionScroll);

	}
	//This method begins the process of receiving information from the user with the speech recognizer
	public void startSpeech()
	{
		//Set the initial prompt
		String prompt="Select hour.";
		//Display speech recognizer
		displaySpeechRecognizer(prompt);
	}
	//This method clears all previously stored user preferences and allows the user to return to the Fresh Menu
	public void restoreDefault()
	{
		this.mealTimes.edit().clear().commit();
		newCard = new Card(this);
		newCard.setText("Default settings have been restored.");
		newCard.setFootnote("Tap to return to the Fresh Menu.");
		count = 3;
		setContentView(newCard.getView());
	}
	//This method displays a speech recognizer to the user to retrieve the new time information vocally.
	public void displaySpeechRecognizer(String prompt) 
	   {
		   Log.d("Message", "Got here.");
		   count++;
	       Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	       intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
	       startActivityForResult(intent, SPEECH_REQUEST);
	   }
	
	 @Override
	   protected void onActivityResult(int requestCode, int resultCode,
	           Intent data)
	   {
		   Log.d("Message", "Got here too");
		   super.onActivityResult(requestCode, resultCode, data);
	       if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) 
	       {
	           List<String> results = data.getStringArrayListExtra(
	                   RecognizerIntent.EXTRA_RESULTS);
	           String spokenText = results.get(0).toLowerCase();
	           //Update the speech recognizer prompt depending on the number of times it has been called previously. This is 
	           //tracked using the "count" variable.
	           switch(count)
	           {
	           case 1:
	        	   newHour=spokenText;
	        	   displaySpeechRecognizer("Select minute.");
	        	   break;
	           case 2:
	        	   newMin=spokenText;
	        	   //Ensure that the minute is formatted correctly
	        	   checkMin();
	        	   displaySpeechRecognizer("AM or PM?");
	        	   break;
	           case 3:
	        	   if(spokenText.contains("am"))
	        	   {
	        		   am=true;
	        	   formatTime();
	        	   }
	        	   else if(spokenText.contains("pm"))
	        	   {
	        		   am=false;
	        	   formatTime();
	        	   }
	        	   else
	        	   {
	        		   count=2;
	        		   displaySpeechRecognizer("AM or PM?");
	        	   }
	        	   break;
	        	   
	        	   
	           }
	           
	          
	       }
	       
	   }
	 //This method formats the user's choice for the new minute based on common speech patterns that may not be understood by
	 //Glass
	 public void checkMin()
	 {
		 if(newMin.equals("0"))
  	   	{
  		   newMin="00";
  	   	}
		 else if(newMin.contains("o"))
		 {
			 newMin.replaceAll("o", "0");
		 }
		 if(newMin.length()>2)
		 {
			 count=1;
			 displaySpeechRecognizer("Select minute.");
		 }
  	   
	 }
	 //This method formats the user's spoken time into that of a 24 hour clock that can be used by the program in determining which
	 //meal should be displayed next. If a problem is found with the user's responses, the user will be invited to enter the
	 //speech recognizer again.
	 public void formatTime()
	 {
		 if(((Integer.parseInt(newHour)<=12 && am) ||
				 (Integer.parseInt(newHour)<=24 && 
				 Integer.parseInt(newHour)!=0 &&!am)) &&
				 (Integer.parseInt(newMin)<=59 && Integer.parseInt(newMin)>=0))
		 {
			 editor.putInt(key+" Hour", Integer.parseInt(newHour));
			 editor.putInt(key+" Min", Integer.parseInt(newMin));
			 editor.commit();
			 
			 newCard.setText(key+" cut-off time is now set to "+newHour+":"+newMin);
			 newCard.setFootnote("Tap to go back to the Fresh menu.");
		 }
		 else if((Integer.parseInt(newHour)<=12 && !am)&&
				 (Integer.parseInt(newMin)<=59 && Integer.parseInt(newMin)>=0))
		 {
			 editor.putInt(key+" Hour", Integer.parseInt(newHour)+12);
			 editor.putInt(key+" Min", Integer.parseInt(newMin));
			 editor.commit();
			 
			 newCard.setText(key+" cut-off time is now set to "+Integer.toString(Integer.parseInt(newHour)+12)+":"+newMin);
			 newCard.setFootnote("Tap to go back to the Fresh menu.");
		 }
		 else
		 {
			 newCard.setText("Please enter a properly formatted time. Hours should be between 1 and 12. Minutes should be between 0 and 59.");
			 newCard.setFootnote("Tap to try again.");
			 count=4;
		 }
		 
		 setContentView(newCard.getView());
	 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fresh_menu_settings, menu);
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

	//Method to set a resulting action for when the trackpad is tapped.
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event)
		{
			 switch(keyCode)
			 {
			    case KeyEvent.KEYCODE_DPAD_CENTER:
			    case KeyEvent.KEYCODE_ENTER:
			         
			    	//ACTION HERE
			    	//If the user's answers were satisfactory, send user back to the Fresh Menu upon tapping.
			    	if(count==3)
			    	{
			    		Intent intent = new Intent(this, DisplayMenuActivity.class);
			    		startActivity(intent);
			    	}
			    	//If problems were found with the user's answers, send user back through speech recognition prompts upon
			    	//tapping.
			    	else if(count==4)
			    	{
			    		count=0;
			    		displaySpeechRecognizer("Select hour.");
			    	}
			         
			    return true;
			         
			    default:
			    	return super.onKeyDown(keyCode, event);
			            
			  }
		}
}
