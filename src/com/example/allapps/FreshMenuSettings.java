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

public class FreshMenuSettings extends Activity 
{

	List<Card> allCards;
	Card newCard;
	CardScrollView optionScroll;
	OptionCardScrollAdapter optionAdapter;
	SharedPreferences mealTimes;
	SharedPreferences.Editor editor;
	String key="";
	private final int SPEECH_REQUEST = 0;
	String newHour="";
	String newMin="";
	boolean am=true;
	int count=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		allCards = new ArrayList<Card>();
		optionScroll = new CardScrollView(this);
		optionAdapter = new OptionCardScrollAdapter();
		mealTimes= getSharedPreferences("Menu Times", Context.MODE_PRIVATE);
		editor= mealTimes.edit();
		
		newCard = new Card(this);
		newCard.setText("Set Breakfast Cut-Off Time");
		newCard.setFootnote("Display breakfast menu until this time");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Set Brunch Cut-Off Time");
		newCard.setFootnote("Display breakfast menu until this time (weekends only)");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Set Lunch Cut-Off Time");
		newCard.setFootnote("Display lunch menu until this time");
		allCards.add(newCard);
		
		newCard = new Card(this);
		newCard.setText("Set Dinner Cut-Off Time");
		newCard.setFootnote("Display dinner menu until this time");
		allCards.add(newCard);
		
		optionScroll.setAdapter(optionAdapter);
		//Set listener for the CardScrollView to generate an action when the user selects a card.
		optionScroll.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
			{
				switch(position)
				{
				case 0:
					key="Breakfast";
					break;
				case 1:
					key="Brunch";
					break;
				case 2:
					key="Lunch";
					break;
				case 3:
					key="Dinner";
					break;
				};
				String prompt="Select hour.";
				displaySpeechRecognizer(prompt);
					
			}
		});
		optionScroll.activate();
		
		setContentView(optionScroll);

	}
	
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
	           switch(count)
	           {
	           case 1:
	        	   newHour=spokenText;
	        	   displaySpeechRecognizer("Select minute.");
	        	   break;
	           case 2:
	        	   newMin=spokenText;
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

	private class OptionCardScrollAdapter extends CardScrollAdapter 
	 {

	     	@Override
	        public int getPosition(Object item) 
	     	{
	            return allCards.indexOf(item);
	        }

	        @Override
	        public int getCount() 
	        {
	            return allCards.size();
	        }

	        @Override
	        public Object getItem(int position) 
	        {
	            return allCards.get(position);
	        }

	        @Override
	        public int getViewTypeCount() 
	        {
	            return Card.getViewTypeCount();
	        }

	        @Override
	        public int getItemViewType(int position)
	        {
	            return allCards.get(position).getItemViewType();
	        }

	        @Override
	        public View getView(int position, View convertView,
	                ViewGroup parent) 
	        {
	            return  allCards.get(position).getView(convertView, parent);
	        }
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
			    	if(count==3)
			    	{
			    		Intent intent = new Intent(this, DisplayMenuActivity.class);
			    		startActivity(intent);
			    	}
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
