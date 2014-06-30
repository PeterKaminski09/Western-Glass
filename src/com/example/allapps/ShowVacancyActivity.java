package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.glass.app.Card;
/*
 * This class displays computer lab vacancies for all computer labs on campus. It displays either PC vacancies or Mac vacancies 
 * depending on the user's most recently chosen option. Upon first installation, the user will have to choose either Mac or PC 
 * to open the app, but subsequent uses will not require this additional prompt unless Microinteractions are turned off. The 
 * vacancy information is retrieved/displayed by way of an AsyncTask, and the last user preference is saved using
 * SharedPreferences.
 */
public class ShowVacancyActivity extends Activity 
{
	//Context of the application
	Context context = this;
	//String to contain computer type (Mac or PC)
	String type;
	//Speech Request, necessary only when speech recognizer is used
	private static final int SPEECH_REQUEST = 0;
	//Shared preferences object for retrieving past preferences
	SharedPreferences mostRecent;
	//Shared preferences editor for changing the most recently chosen option
	SharedPreferences.Editor editor;
	
	//Menu Id. This changes when the user switches from viewing PC to Mac vacancies or vice versa
	int menuId;
	
	ProgressBar downloadBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_vacancy);
		
		//If microinteractions have been turned on, attempt to receive the last used option from SharedPreferences
		if(Microinteractions.on == true)
		{
			//Initialize SharedPreferences object with the correct settings
			mostRecent = getSharedPreferences("Computer Vacancy",Context.MODE_PRIVATE);
			//Initialize SharedPreferences editor to specify the preference to be changed
			editor = mostRecent.edit();
			//Get the previous intent
			Intent previous = getIntent();
			
			try
			{
				//If a computer type was passed with the intent, determine what that computer type is by calling determineType()
				type=previous.getStringExtra("Type");
				determineType();
			}
			//If no computer type was passed with the intent, display the speech recognizer (if it is the app's first use) or set
			//the type using the most recently used option
			catch(Exception e)
			{
				if(mostRecent.getString("Computer Type",null)==null)
				{
					//Display speech recognizer if no previously used type was found
					displaySpeechRecognizer();
				}
				//If the user has already selected a computer type, call setTypeFromOptions to display that type
				else
				{
					setTypeFromOptions();
				}
			}
		}
		//If microinteractions are disabled, display the speech recognizer  to obtain a computer type from the user
		else
		{
			displaySpeechRecognizer();
		}

	}
	
	//This method uses a SharedPreferences object to set the computer type to the last one that was chosen by the user
	public void setTypeFromOptions()
	{
		Log.d("setTypeFromOptions","Is called");
		type=mostRecent.getString("Computer Type", null);
		//Call determine type to figure out which AsyncTask should be started based on the contents of "type"
		determineType();
	}
	//This method displays a speech recognizer to obtain a computer type from the user vocally
	public void displaySpeechRecognizer() 
	{
		   Log.d("Message", "Got here.");
	       Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	       intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What kind of computer are you looking for?\nPC\nMac");
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
	           
	           //Make it lower case so we don't have to deal with the ambiguity of the English langauge and NLP
	           type = spokenText;
	           
	           determineType();
	           
	       }
	       else
	       {
	    	   Intent intent = new Intent(this, LabMenuActivity.class);
	    	   startActivity(intent);
	       }
	       
	}
	//This method checks whether "type" contains "PC" or "Mac." If it contains one or the other, it will save the user's choice
	//into Shared Preferences and then call the appropriate AsyncTask. If it contains neither, the computer lab vacancy menu will
	//be displayed so the user can select an option manually
	public void determineType()
	{
		//If the user requested to see PC vacancies, call the getPCVacanciesTask
		if(type.contains("pc"))
		{
			//Save PC as most recently used computer type
			editor.putString("Computer Type", "pc");
			editor.commit();
			menuId=R.menu.pc_vacancy;
			Log.d("Menu Change","PC Menu");
			Log.d("Current Menu ID", Integer.toString(menuId));
			//If the user chose to see available PCs, call PCSoap
			new PCSoap().execute();
		}
		//If the user requested to see Mac vacancies, call MacSoap
		else if(type.contains("mac"))
		{
			//Save Mac as most recently used computer type
			editor.putString("Computer Type","mac");
			editor.commit();
			menuId=R.menu.mac_vacancy;
			Log.d("Menu Change", "Mac Menu");
			Log.d("Current Menu ID", Integer.toString(menuId));
			//If the user chose to see available Macs, call MacSoap
			new MacSoap().execute();
		}
		//Otherwise, display the main menu
		else
		{
			Intent intent = new Intent(context, LabMenuActivity.class);
			startActivity(intent);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(menuId, menu);
		return true;
	}
	
	//This method needed to be implemented in order to switch menus dynamically.
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		onCreateOptionsMenu(menu);
	
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Log.d("Current Menu ID", Integer.toString(menuId));
		switch(id)
		{
		case R.id.refresh:
			onCreate(null);
			return true;
		case R.id.see_mac:
			//Save Mac as most recently used computer type
			editor.putString("Computer Type", "mac");
			editor.commit();
			Log.d("Message", "Mac vacancies get called.");
			setTypeFromOptions();
			return true;
		case R.id.see_pc:
			//Save PC as most recently used computer type
			editor.putString("Computer Type", "pc");
			editor.commit();
			Log.d("Message", "PC vacancies get called.");
			setTypeFromOptions();
			return true;
		case R.id.wku_main_menu:
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			return true;
		default:
		return super.onOptionsItemSelected(item);
		}
	}
	
	private class PCSoap extends AsyncTask<Void, Void, String>
	{
		ArrayList<String> locations = new ArrayList<String>();
    	ArrayList<String> pcs = new ArrayList<String>();
    	ArrayList<String> macs = new ArrayList<String>();
    	
    	protected void onPreExecute()
    	{
    		//Executed before the thread begins
	         super.onPreExecute();
	         setContentView(R.layout.activity_show_vacancy);
	         downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
	         //Simulate starting the downloadBar
	         downloadBar.setVisibility(0);
    	}
		protected String doInBackground(Void...voids)
		{
			SoapSerializationEnvelope envelope;
			String xml="";
			try {
	            // Create SOAP Connection
				HttpTransportSE htse = new HttpTransportSE(
						"https://atechlabs.wku.edu/soap/traffic/");
				
	            // Send SOAP Message to SOAP Server
	           String url = "https://atechlabs.wku.edu/soap/traffic/";
	           
	           envelope = createSOAPRequest(url);
	           
	           htse.call("https://atechlabs.wku.edu/soap/traffic/GetLabTraffic", 
						envelope);

	            // Process the SOAP Response
	           Object response =  envelope.getResponse();
	           if(response instanceof Vector)
	           {
	        	   SoapPrimitive element0= (SoapPrimitive) ((Vector) response).elementAt(0);
	        	   xml = element0.toString();
//	        	   Log.d("Response", xml);
//	        	   Log.d("Vector size", String.valueOf(((Vector) response).capacity()));
	        	   SoapObject element1 = (SoapObject) ((Vector) response).elementAt(1);
	        	   xml = xml +" "+element1.toString();
//	        	   Log.d("Response", xml);
	        	   
	        	   int startIndex=1, counter=0, stopIndex=0, present=0, inUse=0;
	   			
	   			String location="";
	   	    	
	   	    	Log.d("Before while loop", "I got here");
	   	    	
	   	    	while(xml.indexOf("<Location>", startIndex)>=0)
	   	    	{
	   	    		Log.d("While loop", "I got here");
	   	    		//Parse the location title from the XML
	   	    		startIndex = xml.indexOf("<Location>",startIndex);
	   	    		startIndex = xml.indexOf(">", startIndex)+1;
	   	    		stopIndex = xml.indexOf("</Location>", startIndex);
	   	    		location = xml.substring(startIndex, stopIndex);
	   	    		locations.add(location);
	   	    		startIndex = stopIndex+18;
	   	    		
	   	    		//Parse the number of units present from the XML
	   	    		startIndex = xml.indexOf("<PCs>", startIndex);
	   	    		startIndex = xml.indexOf(">", startIndex)+1;
	   	    		stopIndex = xml.indexOf("</PCs>", startIndex);
	   	    		Log.d("Present",xml.substring(startIndex, stopIndex));
	   	    		present = Integer.valueOf(xml.substring(startIndex, stopIndex));
	   	    		
	   	    		//Parse the number of units in use from the XML
	   	    		startIndex = xml.indexOf("<PCsInUse>", startIndex);
	   	    		startIndex = xml.indexOf(">", startIndex)+1;
	   	    		stopIndex = xml.indexOf("</PCsInUse>", startIndex);
	   	    		Log.d("In use",xml.substring(startIndex, stopIndex));
	   	    		inUse = Integer.valueOf(xml.substring(startIndex, stopIndex));
	   	    		
	   	    		//Add the number of available PCs to the arrayList
	   	    		pcs.add(String.valueOf(present-inUse));
	   	    		
	   	    	}
	     
	           }

	        } 
			catch (Exception e) 
			{
	            System.err.println("Error occurred while sending SOAP Request to Server");
	            e.printStackTrace();
	        }
			
			return xml;
			
			
		}
		
		private SoapSerializationEnvelope createSOAPRequest(String url) throws Exception 
	    {

	        SoapObject getTrafficRequest = new SoapObject(url,"GetLabTraffic");
	        getTrafficRequest.addProperty("OutputType","XML");

	        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
	        
	        envelope.setOutputSoapObject(getTrafficRequest);
	        
	        return envelope;
	    }
		
		protected void onPostExecute(String string)
	    {
			
			//Create a string of all locations, each on a new line
			String allLocations="";
			for(int counter=0; counter<locations.size(); counter++)
			{
				allLocations=allLocations+"\n"+locations.get(counter);
				Log.d("Location loop", "Location added");
			};
			
			//Find number of units available based on the number of units present and the number of units currently in use.
			String allAvailable="";
			for(int counter=0; counter<pcs.size(); counter++)
			{
				allAvailable=allAvailable+"\n"+pcs.get(counter);
				Log.d("Available loop", "Num added");
			};

			//Create relative layout for the right hand side of the display
			RelativeLayout showAll = (RelativeLayout) findViewById(R.id.right_column);
			Log.d("Right ID", String.valueOf(R.id.right_column));
			Log.d("Right layout", "found");
			
			//Create relative layout for the list of locations
			RelativeLayout locations = new RelativeLayout(context);
			RelativeLayout.LayoutParams locationParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			locations.setId(11);
			Log.d("Location view", "created");
			
			//Create text view object for the location list and location heading
			TextView locationList = new TextView(context);
			TextView locHeading = new TextView(context);
			//Set ID's for the location list and heading to refer to them later.
			locHeading.setId(1);
			locationList.setId(2);
			
			//Create locations heading
			locHeading.setText("PC Locations");
			
			//Create list of locations
			locationList.setText(allLocations);
			locationList.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
			locationParams.addRule(RelativeLayout.BELOW, locHeading.getId());
			locationList.setLayoutParams(locationParams);
			Log.d("Location heading", "set");
			Log.d("Location list", "set");
			
			//Add location/location heading to the relative layout.
			locations.addView(locHeading);
			locations.addView(locationList);
			Log.d("Location view", "children added");
			
			
			//Create relative layout for the list of units available
			RelativeLayout units = new RelativeLayout(context);
			RelativeLayout.LayoutParams unitParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			units.setId(10);
			Log.d("Unit view", "created");
			
			//Create units available heading
			TextView unitHeading = new TextView(context);
			unitHeading.setText("Available");
			unitHeading.setId(3);
			Log.d("Unit heading", "created");
			
			//Create list of units available
			TextView availableList = new TextView(context);
			availableList.setText(allAvailable);
			availableList.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			//Set the parameters of the available list so that it appears below the heading and in the center of its column.
			unitParams.addRule(RelativeLayout.BELOW, unitHeading.getId());
			unitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			//Assign the predefined parameters to the availableList TextView.
			availableList.setLayoutParams(unitParams);
			Log.d("Unit list", "set");
			
			availableList.setId(4);
			
			//Add unit heading, units available, and refresh button to the unit Relative Layout
			units.addView(unitHeading);
			units.addView(availableList);
			Log.d("Unit view", "children added");
		
			
			RelativeLayout.LayoutParams listParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			Log.d("Parameters", "created");
			units.setLayoutParams(listParams);
			Log.d("Parameters", "Added");
			
			//PROBLEM OCCURS HERE
			if(showAll==null)
			{
				Log.d("ShowAll", "is null");
			}
			else
			{
			//Add locations and units to the overall display
			showAll.addView(units);
			Log.d("Right layout", "unit view added");
			}
			
			//Create RelativeLayout for left side of the display
			RelativeLayout showLeft = (RelativeLayout) findViewById(R.id.left_column);
			showLeft.addView(locations);
			Log.d("Left layout", "found");
			Log.d("Left layout", "location view added");
			
			RelativeLayout all = (RelativeLayout) findViewById(R.id.allInfo);
			
			Log.d("Entire Layout", "found");
			downloadBar.setVisibility(4);
			Log.d("Progress bar", "removed");
			all.removeViewAt(0);
			//Set content view to the XML layout file
			setContentView(all);
	    		
	    	}
	    	
	    }
	
	private class MacSoap extends AsyncTask<Void, Void, String>
	{
	
		ArrayList<String> locations = new ArrayList<String>();
		ArrayList<String> pcs = new ArrayList<String>();
		ArrayList<String> macs = new ArrayList<String>();
	
	protected void onPreExecute()
	{
		//Executed before the thread begins
        super.onPreExecute();
        setContentView(R.layout.activity_show_vacancy);
        downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
        //Simulate starting the downloadBar
        downloadBar.setVisibility(0);
	}
	protected String doInBackground(Void...voids)
	{
		SoapSerializationEnvelope envelope;
		String xml="";
		try {
            // Create SOAP Connection
			HttpTransportSE htse = new HttpTransportSE(
					"https://atechlabs.wku.edu/soap/traffic/");
			
            // Send SOAP Message to SOAP Server
           String url = "https://atechlabs.wku.edu/soap/traffic/";
           
           envelope = createSOAPRequest(url);
           
           htse.call("https://atechlabs.wku.edu/soap/traffic/GetLabTraffic", 
					envelope);

            // Process the SOAP Response
           Object response =  envelope.getResponse();
           if(response instanceof Vector)
           {
        	   SoapPrimitive element0= (SoapPrimitive) ((Vector) response).elementAt(0);
        	   xml = element0.toString();
//        	   Log.d("Response", xml);
//        	   Log.d("Vector size", String.valueOf(((Vector) response).capacity()));
        	   SoapObject element1 = (SoapObject) ((Vector) response).elementAt(1);
        	   xml = xml +" "+element1.toString();
//        	   Log.d("Response", xml);
        	   
        	   int startIndex=1, counter=0, stopIndex=0, present=0, inUse=0;
   			
   			String location="";
   	    	
   	    	Log.d("Before while loop", "I got here");
   	    	
   	    	while(xml.indexOf("<Location>", startIndex)>=0)
   	    	{
   	    		Log.d("While loop", "I got here");
   	    		//Parse the location title from the XML
   	    		startIndex = xml.indexOf("<Location>",startIndex);
   	    		startIndex = xml.indexOf(">", startIndex)+1;
   	    		stopIndex = xml.indexOf("</Location>", startIndex);
   	    		location = xml.substring(startIndex, stopIndex);
   	    		locations.add(location);
   	    		startIndex = stopIndex+18;
   	    		
   	    		//Parse the number of units present from the XML
   	    		startIndex = xml.indexOf("<Macs>", startIndex);
   	    		startIndex = xml.indexOf(">", startIndex)+1;
   	    		stopIndex = xml.indexOf("</Macs>", startIndex);
   	    		present = Integer.valueOf(xml.substring(startIndex, stopIndex));
   	    		
   	    		if(present==0)
   	    		{
   	    			macs.add("0");
   	    		}
   	    		else
   	    		{
   		    		//Parse the number of units in use from the XML
   		    		startIndex = xml.indexOf("<MacsInUse>", startIndex);
   		    		startIndex = xml.indexOf(">", startIndex)+1;
   		    		stopIndex = xml.indexOf("</MacsInUse>", startIndex);
   		    		inUse = Integer.valueOf(xml.substring(startIndex, stopIndex));
   		    		
   		    		macs.add(String.valueOf(present-inUse));
   	    		}
   	    		
   	    	}
     
           }

        } 
		catch (Exception e) 
		{
            System.err.println("Error occurred while sending SOAP Request to Server");
            e.printStackTrace();
        }
		
		return xml;
		
		
	}
	
	private SoapSerializationEnvelope createSOAPRequest(String url) throws Exception 
    {

        SoapObject getTrafficRequest = new SoapObject(url,"GetLabTraffic");
        getTrafficRequest.addProperty("OutputType","XML");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        
        envelope.setOutputSoapObject(getTrafficRequest);
        
        return envelope;
    }
	
	protected void onPostExecute(String string)
    {
		for(int counter=0; counter<macs.size(); counter++)
		{
			if(Integer.valueOf(macs.get(counter))==0)
			{
				locations.remove(counter);
				macs.remove(counter);
			}
		}
		
		
		//Create a string of all locations, each on a new line
		String allLocations="";
		for(int counter=0; counter<locations.size(); counter++)
		{
			allLocations=allLocations+"\n"+locations.get(counter);
		};
		
		//Find number of units available based on the number of units present and the number of units currently in use.
		String allAvailable="";
		for(int counter=0; counter<macs.size(); counter++)
		{
			allAvailable=allAvailable+"\n"+macs.get(counter);
		};

		//Create relative layout for the right hand side of the display
		RelativeLayout showAll = (RelativeLayout) findViewById(R.id.right_column);
		
		//Create relative layout for the list of locations
		RelativeLayout locations = new RelativeLayout(context);
		RelativeLayout.LayoutParams locationParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		locations.setId(11);
		
		//Create text view object for the location list and location heading
		TextView locationList = new TextView(context);
		TextView locHeading = new TextView(context);
		//Set ID's for the location list and heading to refer to them later.
		locHeading.setId(1);
		locationList.setId(2);
		
		//Create locations heading
		locHeading.setText("Mac Locations");
		
		//Create list of locations
		locationList.setText(allLocations);
		locationList.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
		locationParams.addRule(RelativeLayout.BELOW, locHeading.getId());
		locationList.setLayoutParams(locationParams);
		
		//Add location/location heading to the relative layout.
		locations.addView(locHeading);
		locations.addView(locationList);
		
		
		//Create relative layout for the list of units available
		RelativeLayout units = new RelativeLayout(context);
		RelativeLayout.LayoutParams unitParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		units.setId(10);
		
		//Create units available heading
		TextView unitHeading = new TextView(context);
		unitHeading.setText("Available");
		unitHeading.setId(3);
		
		//Create list of units available
		TextView availableList = new TextView(context);
		availableList.setText(allAvailable);
		availableList.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		//Set the parameters of the available list so that it appears below the heading and in the center of its column.
		unitParams.addRule(RelativeLayout.BELOW, unitHeading.getId());
		unitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//Assign the predefined parameters to the availableList TextView.
		availableList.setLayoutParams(unitParams);
		
		availableList.setId(4);
		
		//Add unit heading, units available, and refresh button to the unit Relative Layout
		units.addView(unitHeading);
		units.addView(availableList);
		
		RelativeLayout.LayoutParams listParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		units.setLayoutParams(listParams);
		
		//Add locations and units to the overall display
		showAll.addView(units);
		
		//Create RelativeLayout for left side of the display
		RelativeLayout showLeft = (RelativeLayout) findViewById(R.id.left_column);
		showLeft.addView(locations);
		
		RelativeLayout all = (RelativeLayout) findViewById(R.id.allInfo);
		
		downloadBar.setVisibility(4);
		all.removeViewAt(0);
		//Set content view to the XML layout file
		setContentView(all);
    		
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

}
