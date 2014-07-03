package com.example.allapps;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.glass.app.Card;
/*
 * This class enables the user to turn microinteractions on/off and is only utilized for testing purposes. The last setting 
 * selected is saved using a SharedPreferences object and then referenced throughout the application to determine whether or not
 * a microinteraction should be implemented.
 */
public class Microinteractions extends Activity 
{

	static boolean on = true;
	SharedPreferences micro;
	SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_microinteractions);
		
		micro = getSharedPreferences("Microinteractions", Context.MODE_PRIVATE);
		editor = micro.edit();
		//Display welcome card to the user
		Card newCard = new Card(this);
		newCard.setText("Tap to change Microinteraction settings");
		newCard.setFootnote(String.valueOf(on));
		setContentView(newCard.getView());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.microinteractions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//If the user turns microinteractions on, set boolean value of "on" to true and save the change.
		if (id == R.id.on) 
		{
			on = true;
			editor.putBoolean("Value", on);
			editor.commit();
			showCard();
			return true;
		}
		//If the user turns microinteractions off, set boolean value of "on" to false and save the change.
		else if (id ==R.id.off)
		{
			on = false;
			editor.putBoolean("Value", on);
			editor.commit();
			showCard();
			return true;
		}
		else
		{
		return super.onOptionsItemSelected(item);
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
				    openOptionsMenu();
				         
				default:
				    return super.onKeyDown(keyCode, event);
				            
			}
		}
		
		//updates the value of the card
		public void showCard(){
		    Card newCard = new Card(this);
	        newCard.setText("Tap to change Microinteraction settings");
	        newCard.setFootnote(String.valueOf(on));
	        setContentView(newCard.getView());
		}
	
}
