package com.example.allapps;

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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class CampusHoursActivity extends Activity 
{
	Calendar endOfSummer;
	Calendar startOfSummer;
	Calendar now;
	Context context = this;
	List<Card> allCards;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Create new card to display while the rest of the app is loading
		Card loadCard = new Card(context);
		loadCard.setText("Open Now list is loading");
		loadCard.setFootnote("Chill for a sec.");
		setContentView(loadCard.getView());
		
		now=Calendar.getInstance();
		endOfSummer=Calendar.getInstance();
		startOfSummer=Calendar.getInstance();
		
		//Set dates for the start and end of summer
		endOfSummer.set(2014, 7, 15);
		startOfSummer.set(2014, 5, 2);
		
		//Set fake date for "now" for testing
		//now.set(2014, 4, 6);
		
		//If the current date is in the summer, find open locations using summer restaurant hours
		if(now.after(startOfSummer) && now.before(endOfSummer))
		{
			new findOpenSummer().execute();
			//openWebPage("http://www.campusdish.com/NR/rdonlyres/CDB1AFAE-EE51-4437-80AE-D12B9500F731/0/SUMMER2014HOP.pdf");
		}
		//If the current date is during the school year, find open locations using regular hours
		else
		{
		new findOpenNow().execute();
		}
	}
	
	/*
	 * PDF doesn't display
	public void openWebPage(String url) 
	{
	    Uri webpage = Uri.parse(url);
	    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
	    if (intent.resolveActivity(getPackageManager()) != null) 
	    {
	        startActivity(intent);
	    }
	}
	*/

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
	
	//AsyncTask that is started to find open locations during the school year
	private class findOpenNow extends AsyncTask<Void, Void, String[]>
	{
		protected String[] doInBackground(Void...params)
		{
			//Local variables
			ArrayList<Facility> allFacilities= new ArrayList<Facility>();
			ArrayList<String> allOpen = new ArrayList<String>();
			int weekday = now.get(Calendar.DAY_OF_WEEK);
			Date currentTime = now.getTime();
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			String time= timeFormat.format(currentTime);
			
			Log.d("Weekday", Integer.toString(weekday));
			
			//Monday through Thursday location hours
			if(weekday>=2&&
					weekday<=5)
			{
				//Initialize facilities with open/close times and add them to the arrayList
				
				Facility fresh = new Facility("The Fresh Food Company","07:00","20:00");
				allFacilities.add(fresh);
				
				//Starbucks has the same open/close times as fresh;
				Facility starbucks = new Facility("Starbucks", fresh.getOpen(), fresh.getClose());
				allFacilities.add(starbucks);
				
				//Da Vinci's has the same opening time as Fresh.
				Facility davincis = new Facility("Da Vinci's", fresh.getOpen(), "16:00");
				allFacilities.add(davincis);
				
				//YoBlendz has the same close time as Fresh
				Facility yoblendz = new Facility("YoBlendz", "11:00", fresh.getClose());
				allFacilities.add(yoblendz);
				
				Facility topper = new Facility("Topper Cafe","10:30","24:00");
				allFacilities.add(topper);
				
				//Garrett has the same open time as Fresh.
				Facility garrett = new Facility("Garrett Food Court",fresh.getOpen(),"15:30");
				allFacilities.add(garrett);
				
				Facility tower = new Facility("Tower Food Court","07:30","23:00");
				allFacilities.add(tower);
				
				//Java City has the same open time as Fresh.
				Facility java = new Facility("Java City",fresh.getOpen(),"22:00");
				allFacilities.add(java);
				
				//Einstein Bros. has the same open time as Fresh.
				Facility einsteins = new Facility("Einstein Bros. Bagels",fresh.getOpen(),"17:00");
				allFacilities.add(einsteins);
				
				Facility gSubway = new Facility("Subway @ Garrett","10:00","20:00");
				allFacilities.add(gSubway);
				
				//Subway at Bates has the same close time as Topper Cafe
				Facility bSubway = new Facility("Subway @ Bates","08:00",topper.getClose());
				allFacilities.add(bSubway);
				
				//P.O.D. has the same open time as Tower Food Court and the same close time as Topper Cafe
				Facility pod = new Facility("P.O.D. @ Bates",tower.getOpen(),topper.getClose());
				allFacilities.add(pod);
				
				//Pit Stop Convenience has the same hours as the Tower Food Court
				Facility pitStop = new Facility("Pit Stop Convenience Store", tower.getOpen(), tower.getClose());
				allFacilities.add(pitStop);
				
				//Panda Express has the same open time as Topper Cafe and the same close time as Fresh.
				Facility panda = new Facility("Panda Express",topper.getOpen(),fresh.getClose());
				allFacilities.add(panda);
				
				Log.d("All Facilities Size", Integer.toString(allFacilities.size())); //Size is correct (14)
				
				
			}
			//Friday location hours
			else if(weekday==6)
			{
				//Initialize facilities with open/close times and add them to the arrayList
				
				Facility fresh = new Facility("The Fresh Food Company","07:00","20:00");
				allFacilities.add(fresh);
				
				//Starbucks has the same open time as fresh;
				Facility starbucks = new Facility("Starbucks", fresh.getOpen(), "16:00");
				allFacilities.add(starbucks);
				
				//Da Vinci's has the same opening time as Fresh.
				Facility davincis = new Facility("Da Vinci's", fresh.getOpen(), "15:00");
				allFacilities.add(davincis);
				
				//YoBlendz has the same close time as Starbucks
				Facility yoblendz = new Facility("YoBlendz", "11:00", starbucks.getClose());
				allFacilities.add(yoblendz);
				
				Facility topper = new Facility("Topper Cafe","10:30","22:00");
				allFacilities.add(topper);
				
				//Garrett has the same open time as Fresh.
				Facility garrett = new Facility("Garrett Food Court",fresh.getOpen(),"15:30");
				allFacilities.add(garrett);
				
				Facility tower = new Facility("Tower Food Court","07:30","17:00");
				allFacilities.add(tower);
				
				//Java City has the same open time as Fresh and the same close time as Da Vinci's
				Facility java = new Facility("Java City",fresh.getOpen(),davincis.getClose());
				allFacilities.add(java);
				
				//Einstein Bros. has the same open time as Fresh and the same close time as Da Vinci's
				Facility einsteins = new Facility("Einstein Bros. Bagels",fresh.getOpen(),davincis.getClose());
				allFacilities.add(einsteins);
				
				//Garrett Subway has the same close time as Starbucks
				Facility gSubway = new Facility("Subway @ Garrett","10:00",starbucks.getClose());
				allFacilities.add(gSubway);
				
				//Subway at Bates has the same close time as Fresh
				Facility bSubway = new Facility("Subway @ Bates","08:00",fresh.getClose());
				allFacilities.add(bSubway);
				
				//P.O.D. has the same open time as Tower Food Court
				Facility pod = new Facility("P.O.D. @ Bates",tower.getOpen(),"24:00");
				allFacilities.add(pod);
				
				//Pit Stop Convenience has the same open time as Tower Food Court
				Facility pitStop = new Facility("Pit Stop Convenience Store", tower.getOpen(), "18:00");
				allFacilities.add(pitStop);
				
				//Panda Express has the same open time as Topper Cafe and the same close time as Tower Food Court.
				Facility panda = new Facility("Panda Express",topper.getOpen(),tower.getClose());
				allFacilities.add(panda);
				
			}
			//Saturday location hours
			else if(weekday==7)
			{
				//Initialize facilities with open/close times and add them to the arrayList
				
				//Fresh is open for two separate time slots on Saturdays.
				Facility fresh = new Facility("The Fresh Food Company","10:30","14:30");
				allFacilities.add(fresh);
				
				Facility fresh2 = new Facility("The Fresh Food Company","17:00","19:00");
				allFacilities.add(fresh2);
				
				Facility starbucks = new Facility("Starbucks", "10:00", "14:00");
				allFacilities.add(starbucks);
				
				Facility topper = new Facility("Topper Cafe","12:00","22:00");
				allFacilities.add(topper);
		
				Facility bSubway = new Facility("Subway @ Bates","10:00","24:00");
				allFacilities.add(bSubway);
				
				//P.O.D. has the same close time as Bates Subway
				Facility pod = new Facility("P.O.D. @ Bates","16:00",bSubway.getClose());
				allFacilities.add(pod);
				
			}
			//Sunday location hours
			else if(weekday==1)
			{	
				//Initialize facilities with open/close times and add them to the arrayList
				
				Facility fresh = new Facility("The Fresh Food Company","10:30","20:00");
				allFacilities.add(fresh);
			
				Facility topper = new Facility("Topper Cafe","12:00","24:00");
				allFacilities.add(topper);
				
				Facility tower = new Facility("Tower Food Court","16:00","23:00");
				allFacilities.add(tower);
				
				Facility java = new Facility("Java City","17:00","21:00");
				allFacilities.add(java);
		
				//Subway at Bates has the same close time as Topper
				Facility bSubway = new Facility("Subway @ Bates","10:00", topper.getClose());
				allFacilities.add(bSubway);
				
				//P.O.D. has the same open time as Tower Food Court and the same close time as Topper
				Facility pod = new Facility("P.O.D. @ Bates",tower.getOpen(),topper.getClose());
				allFacilities.add(pod);
				
				//Pit Stop Convenience has the same open time as Java City and the same close time as Tower Food Court
				Facility pitStop = new Facility("Pit Stop Convenience Store", java.getOpen(), tower.getClose());
				allFacilities.add(pitStop);
				
				//Panda Express has the same open time as Tower Food Court and the same close time as Fresh.
				Facility panda = new Facility("Panda Express",tower.getOpen(),fresh.getClose());
				allFacilities.add(panda);
				
				
			}
			//RuntimeException occurs somewhere within loop...
			//UPDATE: Apparently Runtime Exception got fixed. I have no clue how.
			//Compare current time with the open/close times of all facilities
			for(int counter=0; counter<allFacilities.size(); counter++)
			{
				Facility currentFacility = allFacilities.get(counter);
				
				Log.d("Current facility",currentFacility.getName());
				
				int currentHour = Integer.parseInt(time.substring(0,2));
				int currentMin = Integer.parseInt(time.substring(3,5));
				int openHour = Integer.parseInt(currentFacility.getOpen().substring(0, currentFacility.getOpen().indexOf(":")));
				Log.d("Open Hour", Integer.toString(openHour));
				int openMin = Integer.parseInt(currentFacility.getOpen().substring(currentFacility.getOpen().indexOf(":")+1, 
						currentFacility.getOpen().length()-1));
				Log.d("Open Min", Integer.toString(openMin));
				int closeHour = Integer.parseInt(currentFacility.getClose().substring(0, currentFacility.getClose().indexOf(":")));
				Log.d("Close Hour", Integer.toString(closeHour));
				int closeMin = Integer.parseInt(currentFacility.getClose().substring(currentFacility.getClose().indexOf(":")+1, 
						currentFacility.getClose().length()-1));
				Log.d("Close Min", Integer.toString(closeMin));
				
				//If the current time is between the checked facility's open and close times, add the checked facility to
				//an arraylist. (Called "allOpen")
				if((currentHour>openHour&&currentHour<closeHour) ||
						(currentHour==openHour&&currentMin>=openMin)||
						(currentHour==closeHour&&currentMin<=closeMin))
				{
					Log.d("Being added", allFacilities.get(counter).getName());
					allOpen.add(allFacilities.get(counter).getName());
				};
			};
			
			//Convert the ArrayList into a String[]
			String[] allOpenArray = new String[allOpen.size()];
			allOpen.toArray(allOpenArray);
			
			Log.d("Message 2","I got here.");
			
			Log.d("End Array Size", Integer.toString(allOpenArray.length));
			
			//Return the String[]
			return allOpenArray;

		}
		
		protected void onPostExecute(String[] allOpen)
		{
			allCards = new ArrayList<Card>();
			Card newCard;
			int counter1, counter2;
			
			//Check each element in the String Array
			for(counter1=0; counter1<allOpen.length; counter1++)
			{
				//If that element has a value, create a card for that element
				if(allOpen[counter1]!=null)
				{
					newCard = new Card(context);
					//Set the text of the card to the name of that facility
					newCard.setText(allOpen[counter1]);
					newCard.setFootnote("Tap for directions.");
					//Add the newly created card into the allCards list
					allCards.add(newCard);
				}
			};
			//If multiple cards are in the allCards list, create a CardScrollView that displays all of them.
			if(allCards.size()>=1)
			{
				CardScrollView showCards = new CardScrollView(context);
				//Create new ScrollAdapter object
				OpenCardScrollAdapter showCardAdapter = new OpenCardScrollAdapter();
				//Set the adapter (dataset) for the CardScrollView
				showCards.setAdapter(showCardAdapter);
				
				//Set listener for the CardScrollView to generate an action when the user selects a card.
				showCards.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
					{
						String location=(String) allCards.get(position).getText();
						Intent intent = new Intent(context, StartDirectionsActivity.class);
						intent.putExtra("Place", location);
						startActivity(intent);
					}
				});
				//Activate the ScrollView
				showCards.activate();
				//Show the ScrollView
				setContentView(showCards);
			}
			//Otherwise, if there are no elements in the allCards list, notify the user that no locations are open.
			else
			{
				//Create a new card
				newCard = new Card(context);
				//Set the text of the card to notify the user
				newCard.setText("No locations are currently open.");
				//Apologize to the user
				newCard.setFootnote("Sorry :(");
				//Show the newly created card
				setContentView(newCard.getView());
			}
		}
		
		//CardScrollAdapter that contains the allCards ArrayList.
		private class OpenCardScrollAdapter extends CardScrollAdapter 
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
	}
	
	//AsyncTask that is started to find open locations during the summer
	private class findOpenSummer extends AsyncTask<Void, Void, String[]>
	{
		protected String[] doInBackground(Void...params)
		{
			//Local variables
			ArrayList<Facility> allFacilities= new ArrayList<Facility>();
			ArrayList<String> allOpen = new ArrayList<String>();
			int weekday = now.get(Calendar.DAY_OF_WEEK);
			Date currentTime = now.getTime();
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			String time= timeFormat.format(currentTime);
			
			Log.d("Weekday", Integer.toString(weekday));
			
			//Monday-Thursday location hours
			if(weekday>=2&&
					weekday<=5)
			{
				//Initialize facilities with open/close times and add them to the arrayList
				
				Facility fresh = new Facility("The Fresh Food Company","11:00","13:30");
				allFacilities.add(fresh);
				
				//Starbucks's close time is the same as Fresh's open time.
				Facility starbucks = new Facility("Starbucks", "7:00", fresh.getOpen());
				allFacilities.add(starbucks);
				
				//Garrett Subway's close time is the same as Fresh
				Facility gSubway = new Facility("Subway @ Garrett","10:30",fresh.getClose());
				allFacilities.add(gSubway);
				
				
				Log.d("All Facilities Size", Integer.toString(allFacilities.size())); //Size is correct (14)
				
				
			}
			//Friday location hours
			else if(weekday==6)
			{
				//Initialize facilities with open/close times and add them to the arrayList
				
				Facility fresh = new Facility("The Fresh Food Company","11:00","13:00");
				allFacilities.add(fresh);
				
				//Starbucks closes when Fresh opens
				Facility starbucks = new Facility("Starbucks", "07:00", fresh.getOpen());
				allFacilities.add(starbucks);
				
				
				//Garrett Subway has the same close time as Fresh
				Facility gSubway = new Facility("Subway @ Garrett","10:30",fresh.getClose());
				allFacilities.add(gSubway);
				
			}
			//No locations are open on weekends in the summer
			
			
			//RuntimeException occurs somewhere within loop...
			//UPDATE: Apparently Runtime Exception got fixed. I have no clue how.
			//Compare current time with the open/close times of all facilities
			for(int counter=0; counter<allFacilities.size(); counter++)
			{
				Facility currentFacility = allFacilities.get(counter);
				
				Log.d("Current facility",currentFacility.getName());
				
				int currentHour = Integer.parseInt(time.substring(0,2));
				int currentMin = Integer.parseInt(time.substring(3,5));
				int openHour = Integer.parseInt(currentFacility.getOpen().substring(0, currentFacility.getOpen().indexOf(":")));
				Log.d("Open Hour", Integer.toString(openHour));
				int openMin = Integer.parseInt(currentFacility.getOpen().substring(currentFacility.getOpen().indexOf(":")+1, 
						currentFacility.getOpen().length()-1));
				Log.d("Open Min", Integer.toString(openMin));
				int closeHour = Integer.parseInt(currentFacility.getClose().substring(0, currentFacility.getClose().indexOf(":")));
				Log.d("Close Hour", Integer.toString(closeHour));
				int closeMin = Integer.parseInt(currentFacility.getClose().substring(currentFacility.getClose().indexOf(":")+1, 
						currentFacility.getClose().length()-1));
				Log.d("Close Min", Integer.toString(closeMin));
				
				//If the current time is between the checked facility's open and close times, add the checked facility to
				//an arraylist. (Called "allOpen")
				if((currentHour>openHour&&currentHour<closeHour) ||
						(currentHour==openHour&&currentMin>=openMin)||
						(currentHour==closeHour&&currentMin<=closeMin))
				{
					Log.d("Being added", allFacilities.get(counter).getName());
					allOpen.add(allFacilities.get(counter).getName());
				};
			};
			
			//Convert ArrayList into String[]
			String[] allOpenArray = new String[allOpen.size()];
			allOpen.toArray(allOpenArray);
			
			Log.d("Message 2","I got here.");
			
			Log.d("End Array Size", Integer.toString(allOpenArray.length));
			
			//Return String[]
			return allOpenArray;
			
			
		}
		
		protected void onPostExecute(String[] allOpen)
		{
			allCards = new ArrayList<Card>();
			Card newCard;
			int counter1;
			
			//Check each element in the String Array
			for(counter1=0; counter1<allOpen.length; counter1++)
			{
				//If that element has a value, create a card for that element
				if(allOpen[counter1]!=null)
				{
					newCard = new Card(context);
					//Set the text of the card to the name of that facility
					newCard.setText(allOpen[counter1]);
					newCard.setFootnote("Tap for directions.");
					//Add the newly created card into the allCards list
					allCards.add(newCard);
				}
			};
			//If multiple cards are in the allCards list, create a CardScrollView that displays all of them.
			if(allCards.size()>=1)
			{
				CardScrollView showCards = new CardScrollView(context);
				//Create new ScrollAdapter object
				OpenCardScrollAdapter showCardAdapter = new OpenCardScrollAdapter();
				//Set the adapter (dataset) for the CardScrollView
				showCards.setAdapter(showCardAdapter);
				
				//Set listener for the CardScrollView to generate an action when the user selects a card.
				showCards.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
					{
						String location=(String) allCards.get(position).getText();
						Intent intent = new Intent(context, StartDirectionsActivity.class);
						intent.putExtra("Place", location);
						startActivity(intent);
					}
				});
				//Activate the ScrollView
				showCards.activate();
				//Show the ScrollView
				setContentView(showCards);
			}
			//Otherwise, if there are no elements in the allCards list, notify the user that no locations are open.
			else
			{
				//Create a new card
				newCard = new Card(context);
				//Set the text of the card to notify the user
				newCard.setText("No locations are currently open.");
				//Apologize to the user
				newCard.setFootnote("Sorry :(");
				//Show the newly created card
				setContentView(newCard.getView());
			}
		}
		
		
		//CardScrollAdapter that contains the allCards ArrayList.
		private class OpenCardScrollAdapter extends CardScrollAdapter 
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
	}
	
}
