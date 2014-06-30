package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

/**
 * CampusEvent is the structure for the event class.
 * 
 * With this class we make a call to the internet, based on what type of event
 * the user wants to see, and we return all related events. These events are
 * stored in Event objects and contain information such as Title, location and
 * time.
 * 
 * All information is accessed using the URL
 * www.wku.edu/events/index.php/index.php?categoryid=(YOUR ID HERE)
 * 
 * EVENT ID's Athletics = 314 Arts = 315 Student Activities = 307 Campus Events
 * = 313 ALL = all
 * 
 * @author peterkaminski
 * 
 */
public class CampusEvent
{

   private ArrayList<String> locations, times, titles;
   private ArrayList<Event> events;
   private Boolean bool;

   public CampusEvent(int categoryId)
   {
      events = new ArrayList<Event>();
      locations = new ArrayList<String>();
      times = new ArrayList<String>();
      titles = new ArrayList<String>();
      
      if (categoryId == 0)
      {
         String eventString = "http://www.wku.edu/events/index.php?categoryid=all";
         getSource(eventString);
      }

      else
      {
         String eventString = "http://www.wku.edu/events/index.php?categoryid="
               + String.valueOf(categoryId);
         getSource(eventString);
      }
      

      createEventObjects();
      if (events.size() != 0)
      {
         printEvents();
         bool = true;
      }
      else
      {
         bool = false;
      }

   }
   
   //This makes sure that there is actually an event for the selected category. 
   //@return if true, there are events. Else, there are no events
   public Boolean isEvents(){
      return this.bool;
   }

   //Get source code from the webpage and extract data from that request. 
   public void getSource(String urlString)
   {

      Document doc;
      try
      {
         // Access the source code and store it as a buffer reader.
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
         int findContent = bufferReader
               .indexOf("<div class='calendar-view-info'");
         if (findContent != -1)
         {
            bufferReader.delete(0, findContent);
            html = bufferReader.toString();
         }

         // Using JSOUP create a doc that contains all the source code that we
         // need
         doc = Jsoup.parse(html);

         // Now we can parse all the events out
         //Finds all event times
         Elements eventTimes = doc.select("div.calendar-event-time");
         //Finds all event titles
         Elements eventTitles = doc.select("span");
         //Finds all information, used to find locations
         Elements eventInfo = doc.select("div.calendar-event");

         // Now find all the event titles
         for (Element title : eventTitles)
         {
            if(title.text() != null){
            titles.add(title.text());
            }
            else{
               Log.i("NULL", "ERROR... somewhere");
            }

         }

         // Now find all the event times
         for (Element time : eventTimes)
         {
            // Turn the time into a string
            String stringTime = time.text();
            // Add a the time to the array list
            times.add(stringTime);

         }

         // Now find all the information about the locations
         for (Element event : eventInfo)
         {

            String eventDetails = event.text();

            // Find the location of the event
            String location;
            int locationInt = eventDetails.indexOf("Location:");
            if (locationInt != -1)
            {
               location = eventDetails.substring(locationInt + 10);
               locations.add(location);
            }

         }

      }
      //Error handling
      catch (MalformedURLException err)
      {
         err.printStackTrace();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   //Test purposes
   public void printLocations()
   {
      for (int i = 0; i < locations.size(); i++)
      {
         System.out.println(locations.get(i));
      }
   }

   //Test purposes
   public void printEvents()
   {
      for (int i = 0; i < events.size(); i++)
      {
         System.out.println(events.get(i).prettyRepresentation());
      }
   }

   // This adds all created events to an array list for easy access.
   public void createEventObjects()
   {
      for (int i = 0; i < titles.size(); i++)
      {
         Event event = new Event(titles.get(i), times.get(i), locations.get(i));
         events.add(event);
      }
   }

   //Returns the events as an array list. 
   public ArrayList<Event> getEvents()
   {
      return this.events;
   }

}
