package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
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
 * 
 * Code written and commented by Lydia Buzzard
 */
public class ShowVacancyActivity extends Activity 
{
	//Previous intent
	Intent previous;
	//Context of the application
	Context context = this;
	//String to contain computer type (Mac or PC)
	String type;
	//Speech Request, necessary only when speech recognizer is used
	private static final int SPEECH_REQUEST = 0;
	//Shared preferences object for retrieving past preferences
	SharedPreferences mostRecent, micro;
	//Boolean value to determine whether or not microinteractions should be used
	boolean useMicro;
	//Shared preferences editor for changing the most recently chosen option
	SharedPreferences.Editor editor;
	
	//Menu Id. This changes when the user switches from viewing PC to Mac vacancies or vice versa
	int menuId, count;
	//Variables necessary for timing interactions (testing purposes only)
	long startTime, endTime;
    public static List<String> info = new ArrayList<String>();
	
	ProgressBar downloadBar;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		if(count==0)
		{
			onResume();
		}
		setContentView(R.layout.activity_show_vacancy);
		
		//Get the previous intent
		previous = getIntent();
		//Initialize SharedPreferences object for saving last computer type with the correct settings
		mostRecent = getSharedPreferences("Computer Vacancy",Context.MODE_PRIVATE);
		//Initialize SharedPreferences editor to specify the preference to be changed
		editor = mostRecent.edit();
		//Initialize SharedPreferences object for retrieving current microinteraction settings
		micro = getSharedPreferences("Microinteractions", Context.MODE_PRIVATE);
		//Retrieve boolean value from microinteraction preferences. Set microinteractions to "true" if no preferences have
		//been set.
		useMicro = micro.getBoolean("Value", true);
		
		try
		{
			//If a computer type has been passed with the intent to start the activity, display that computer type
				
				type = previous.getStringExtra("Type");
				determineType();
			
		}
		catch(Exception e)
		{
		
			
			//If microinteractions have been turned on, call the microOn() method to check and implement sharedPreferences
			if(useMicro)
			{
				microOn();
			}
			//If microinteractions are disabled, display the Speech Recognizer to receive a computer type from the user vocally
			else
			{
				displaySpeechRecognizer();
			}
		}

	}
	
	//This method is called when microinteractions are turned on.
	public void microOn()
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
	
	//This method uses a SharedPreferences object to set the computer type to the last one that was chosen by the user
	public void setTypeFromOptions()
	{
		type=mostRecent.getString("Computer Type", null);
		//Call determine type to figure out which AsyncTask should be started based on the contents of "type"
		determineType();
	}
	//This method displays a speech recognizer to obtain a computer type from the user vocally
	public void displaySpeechRecognizer() 
	{
	       Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	       intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "What kind of computer are \nyou looking for?\nPC\nMac");
	       startActivityForResult(intent, SPEECH_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	           Intent data)
	{
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
			
			stopTime();
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
			
			stopTime();
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
	//When onResume is called, the current time will be retrieved as the "start" time of the interaction
	@Override
	public void onResume()
	{
		super.onResume();
		if(count!=1)
		{
		startTime = System.currentTimeMillis();
		count=1;
		}
		
	}
	
	public void stopTime()
	{
		//Stop the time
        endTime = System.currentTimeMillis();
        Log.d("End time", String.valueOf(endTime));
        Log.d("start time", String.valueOf(startTime));
        //Find the time by subtracting
        String time = String.valueOf(endTime - startTime);
        Log.d("Microinteractions", String.valueOf(useMicro));
        Log.d("Lab Vacancy time", time);
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
		switch(id)
		{
		case R.id.refresh:
			onCreate(null);
			return true;
		case R.id.see_mac:
			//Save Mac as most recently used computer type
			editor.putString("Computer Type", "mac");
			editor.commit();
			setTypeFromOptions();
			return true;
		case R.id.see_pc:
			//Save PC as most recently used computer type
			editor.putString("Computer Type", "pc");
			editor.commit();
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
	//This AsyncTask is called when PC vacancies should be displayed. It retrieves the vacancy information (location and number of
	//units available) using a SOAP service and displays them in a custom XML layout.
	private class PCSoap extends AsyncTask<Void, Void, String>
	{
		ArrayList<String> locations = new ArrayList<String>();
    	ArrayList<String> pcs = new ArrayList<String>();
    	
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
	        	   SoapObject element1 = (SoapObject) ((Vector) response).elementAt(1);
	        	   xml = xml +" "+element1.toString();
	        	   
	        	   int startIndex=1, counter=0, stopIndex=0, present=0, inUse=0;
	   			
	   			String location="";
	   	    	
	   	    	
	   	    	while(xml.indexOf("<Location>", startIndex)>=0)
	   	    	{
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
	   	    		present = Integer.valueOf(xml.substring(startIndex, stopIndex));
	   	    		
	   	    		//Parse the number of units in use from the XML
	   	    		startIndex = xml.indexOf("<PCsInUse>", startIndex);
	   	    		startIndex = xml.indexOf(">", startIndex)+1;
	   	    		stopIndex = xml.indexOf("</PCsInUse>", startIndex);
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
			};
			
			//Find number of units available based on the number of units present and the number of units currently in use.
			String allAvailable="";
			for(int counter=0; counter<pcs.size(); counter++)
			{
				allAvailable=allAvailable+"\n"+pcs.get(counter);
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
			locHeading.setText("PC Locations");
			
			//Create list of locations
			locationList.setText(allLocations);
			locationList.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
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
			availableList.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
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
		
			
			//PROBLEM OCCURS HERE
			if(showAll!=null)
			{
			//Add locations and units to the overall display
			showAll.addView(units);
			}
			
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
	//This AsyncTask is called when PC vacancies should be displayed. It retrieves the vacancy information (location and number of
	//units available) using a SOAP service and displays them in a custom XML layout.
	private class MacSoap extends AsyncTask<Void, Void, String>
	{
	
		ArrayList<String> locations = new ArrayList<String>();
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
        	   SoapObject element1 = (SoapObject) ((Vector) response).elementAt(1);
        	   xml = xml +" "+element1.toString();
        	   
        	   int startIndex=1, counter=0, stopIndex=0, present=0, inUse=0;
   			
   			String location="";
   	    	
   	    	while(xml.indexOf("<Location>", startIndex)>=0)
   	    	{
   	    		//Parse the location title from the XML
   	    		startIndex = xml.indexOf("<Location>",startIndex);
   	    		startIndex = xml.indexOf(">", startIndex)+1;
   	    		stopIndex = xml.indexOf("</Location>", startIndex);
   	    		location = xml.substring(startIndex, stopIndex);
   	    		
   	    		startIndex = stopIndex+18;
   	    		
   	    		//Parse the number of units present from the XML
   	    		startIndex = xml.indexOf("<Macs>", startIndex);
   	    		startIndex = xml.indexOf(">", startIndex)+1;
   	    		stopIndex = xml.indexOf("</Macs>", startIndex);
   	    		present = Integer.valueOf(xml.substring(startIndex, stopIndex));
   	    		
   	    		
   	    		if(present!=0)
   	    		{
   	    			locations.add(location);
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
