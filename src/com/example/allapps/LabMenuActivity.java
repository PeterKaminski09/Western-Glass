package com.example.allapps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
/*
 * This class displays the computer lab vacancy main menu. The menu is contained in a card scroll view with a welcome card, a PC
 * card, and a Mac card. Selecting either the PC or Mac card with a tap will call the ShowVacancyActivity to display the chosen
 * computer type's vacancies to the user.
 * 
 * Code written and commented by Lydia Buzzard
 */
public class LabMenuActivity extends Activity 
{
	//Card for the welcome screen
	CardBuilder menuCard;
	//PC option card
	CardBuilder pc;
	//Mac option card
	CardBuilder mac;
	//List to contain welcome card and both options
	List<CardBuilder> cardList;
	//Scroll view to display card list
	CardScrollView mainMenu;
	//Adapter to supply scroll view with card list
	ScrollAdapter mainMenuAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Create a menu card
		menuCard = new CardBuilder(this, CardBuilder.Layout.TEXT);
		menuCard.setText("WKU Computer Lab Vacancies");
		menuCard.setFootnote("Swipe to select a computer option.");
		
		//Create a PC Card
		pc =new CardBuilder(this, CardBuilder.Layout.TEXT);
		pc.setText("PC");
		pc.setFootnote("Shows available Windows PCs");
		
		//Create a Mac Card
		mac = new CardBuilder(this, CardBuilder.Layout.TEXT);
		mac.setText("Mac");
		mac.setFootnote("Shows available Macintosh computers");
		
		//Create a new card list
		cardList = new ArrayList<CardBuilder>();
		//Add three previously created cards to the list
		cardList.add(menuCard);
		cardList.add(pc);
		cardList.add(mac);
		
		//Create a new CardScrollView to see other options
		mainMenu = new CardScrollView(this);
		
		//Create a CardScrollAdapter to make the above scroll view functional
		mainMenuAdapter = new ScrollAdapter(cardList);
		
		//Set listener for the CardScrollView to generate an action when the user selects a card.
		mainMenu.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
			{
				if(position==1)
				{
					//Define the intent
					Intent intent = new Intent(LabMenuActivity.this, ShowVacancyActivity.class);
					intent.putExtra("Type", "pc");
					startActivity(intent);
				}
				else if(position==2)
				{
					//Define the intent
					Intent intent = new Intent(LabMenuActivity.this, ShowVacancyActivity.class);
					intent.putExtra("Type","mac");
					startActivity(intent);
				}
					
			}
		});
		
		mainMenu.setAdapter(mainMenuAdapter);
		mainMenu.activate();
		
		//Sets the view to the main menu
		setContentView(mainMenu);
		

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
