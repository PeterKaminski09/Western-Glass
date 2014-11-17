package com.example.allapps;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

/**
 * 
 * EventParse connects to the XML file provided by IT that contains WKU events, and it returns all the pertinent information
 * associated with said events
 * 
 * @author Peter Kaminski
 * EVENT ID's Athletics = 314 Arts = 315 Student Activities = 307 Campus Events= 313 ALL = all
 */

public class EventParse {
	//Static variables
	static final String URL_BEGINNING = "http://www.wku.edu/events/events/dsp_xml-mosiac.php?";
	static final String CATEGORY_ID = "categoryid=";
	static final String ATHLETICS = "314";
	static final String ARTS = "315";
	static final String STUDENT_ACTIVITIES = "307";
	static final String CAMPUS_EVENTS = "313";
	
	//ArrayList that has all the events we collect from the xml source code
	private ArrayList<Event> events = new ArrayList<Event>();
	
	/**
	 * Constructor takes an eventID (see static variables) that determine what events will be found. 
	 * @param eventId
	 */
	public EventParse(int eventId) {
		String urlString= URL_BEGINNING + CATEGORY_ID;
		
		switch (eventId){
		case 314:
			urlString += ATHLETICS;
		break;
		case 315:
			urlString += ARTS;
			break;
		case 307:
			urlString += STUDENT_ACTIVITIES;
			break;
		case 313:
			urlString += CAMPUS_EVENTS;
			break;
			
			
		}
		//Append the date to the urlString
		urlString += findTodaysDate();
		//Testing methods
		Log.d("Date", findTodaysDate());
		Log.d("URLSTRING", urlString);
		
		//Find and store the xml code
		StringBuffer source = new StringBuffer(fetchXML(urlString));
		Log.d("WOAH", source.toString());
		
		//Run through the html and save the events
		while(source.indexOf("<event>") != -1){
			collectItem(source);
		}
		
		
	}
	
	//Returns the events as an array list. 
	   public ArrayList<Event> getEvents()
	   {
	      return this.events;
	   }
	
	//Finds today's date and put's it into a string format to append to the URL
	public String findTodaysDate(){
		//Create a string with today's date. That is the only news we want to see
	    String date = new SimpleDateFormat("yyy-MM-dd").format(Calendar.getInstance().getTime());
		//Format the string and return it
		String dateString = "&startdate=" + date + "&enddate=" + date;
		return dateString;
	}
	
	
	//Retrieves the XML from the webpage. 
	public String fetchXML(String urlString){
		try {
			 URL url = new URL(urlString);
	         BufferedReader in = new BufferedReader(new InputStreamReader(
	               url.openStream()));
	         StringBuffer bufferReader = new StringBuffer();
	         String inputLine;

	         // Run through the connection and store the file into the buffer
	         // reader.
	         while ((inputLine = in.readLine()) != null)
	         {
	            bufferReader.append(inputLine);
	         }
	         // Close the connection for housekeeping purposes.
	         in.close();

	         String html = bufferReader.toString();
	         return html;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	//CollectItem takes a stringBuffer of xmlCode and proceeds to find the first event that is contained in the source code
	public void collectItem(StringBuffer xmlCode){
		//Event variables
		String title = "";
		String time = "";
		String location = "";
		
		//Turn the buffer into a string
		String xmlSource = xmlCode.toString();
		int indexofEvent = xmlSource.indexOf("<event>");
		
		int endIndex = xmlSource.indexOf("</event>");
		
		
		//Find the information within the xml tags
		if (indexofEvent != -1 && endIndex != -1){
			String contents = xmlSource.substring(indexofEvent, endIndex);
			
			int titleStart = contents.indexOf("<title>");
			int titleFinish = contents.indexOf("</title>");
			
			//Find title
			if (titleStart != 1 && titleFinish != -1){
				String titleContent = contents.substring(titleStart + 7, titleFinish);
				title = titleContent;
			}
			
			//Find location
			int locationStart = contents.indexOf("<location>");
			int locationFinish = contents.indexOf("</location>");
			
			if (locationStart != 1 && locationFinish != -1){
				String locationContent = contents.substring(locationStart + 10, locationFinish);
				location = locationContent;
			}
			
			//Find time of start
			int timeStart = contents.indexOf("<start_date>");
			int timeFinish = contents.indexOf("</start_date>");
			
			if (timeStart != 1 && timeFinish != -1){
			   String startTime = contents.substring(timeStart + 12, timeFinish);
               int firstT = startTime.indexOf("T");
               if(firstT != -1){
                  startTime = startTime.substring(firstT + 1);
               }
               time += convertToCivilian(startTime) + "-";
               Log.d("START TIME", time);
			}
			
			//Find time of start
			timeStart = contents.indexOf("<end_date>");
		    timeFinish = contents.indexOf("</end_date>");
			
			if (timeStart != 1 && timeFinish != -1){
			   String startTime = contents.substring(timeStart + 10, timeFinish);
               int firstT = startTime.indexOf("T");
               if(firstT != -1){
                  startTime = startTime.substring(firstT + 1);

                  System.out.println(convertToCivilian(startTime) + " converted civilian2");
                  System.out.println(startTime + " Start");
               }
               time += convertToCivilian(startTime);
               Log.d("END TIME", time);
			}
			
			time = time.trim();
			Log.d("Final time", time);
			
			//Create a new event from the information and add it to our list
	        Event event = new Event(title, time, location);
	        events.add(event);
			
			
//			//Create a new event from the fetched information and store it into the array list of events. 
//			//If the time is NOT passed, then add the event. 
//			if(!time.equals("Passed")){
//			Event event = new Event(title, time, location);
//			events.add(event);
//			}
			
			//Since StringBuffer is synchronized we can delete the event and this will prevent an infinite loop. 
			xmlCode.delete(0, endIndex + 8);
			
		}
		
	}
	
	//cleanTime takes a string and does a comparison to the current time. If the end time isn't past, then we clean the string
	// and add keep the event. Otherwise, we remove the event from the list of events. Who has time to scroll through past events?
	
	public String cleanTime(String nastyTime){
		String start = "";
		String end = "";
		
		//Find the Ts and Zs that are from the XML (XML for time looks like -> T19:00-23:00Z)
		int firstT = nastyTime.indexOf("T");
		int firstZ = nastyTime.indexOf("Z");
		
		//If these are present, remove them
		if(firstT != -1 && firstZ != -1)
		start = nastyTime.substring(firstT + 1, firstZ);
		
		//Get the end time
		nastyTime = nastyTime.substring(firstZ + 1, nastyTime.length());
		
		firstT = nastyTime.indexOf("T");
		firstZ = nastyTime.indexOf("Z");
		if(firstT != -1 && firstZ != -1)
			end = nastyTime.substring(firstT + 1, firstZ);
		
		//Do the comparison with end time, if it has passed, then return passed. 
		if(!compareTimes(end, Calendar.getInstance())){
			
			if ((start + end).contains("00:00:00")){
				return convertToCivilian(start);
			}
			else{
			return "Passed";
			}
		}
			
		//Return the time in civilian format
		 return convertToCivilian(start) + "-" + convertToCivilian(end);
		
		
	}
	
	//This converts military to civilian time
	public String convertToCivilian(String time){
		try {
			Date date = new SimpleDateFormat("H:mm:ss").parse(time);
			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
			return sdf.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return null;
	}
	
	//CompareTimes takes a string of time and a calendar object. It then compares the two times and return true if the event hasn't already happened
  	//False if the event has already happened today. This allows us to make a microinteraction for our users. They will have no interest in going to 
  	//Events that already happened so this prevents them from having to scroll through unnecessary cards. 
  	public boolean compareTimes(String time, Calendar cal){
  		Calendar event = Calendar.getInstance();
  		DateFormat formatter = new SimpleDateFormat("H:mm:ss");
  		
  		try {
  			//Create a date object from this with today's current values. 
  			Date date = formatter.parse(time);
  			event.setTime(date);
  			event.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
  			
  			if(cal.getTime().compareTo(event.getTime()) < 0){
  				return true;	
  			}
  			else{
  				return false;
  			}
  		}
  		catch (ParseException e) {
  			//If there is a parse error, return false in general. This card must have some funky date. 
  			e.printStackTrace();
  			return false;
  		}
  		
  	}

}