package com.example.allapps;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Event is the shell for a WKU event. Each event has a title, time, and location
 * @author peterkaminski
 *
 */
public class Event
{
   //Class variables
   private String title, time, location;
   //Constructor takes all information and sets the object 
   public Event(String eventTitle, String eventTime, String eventLocation)
   {
      this.title = StringEscapeUtils.unescapeHtml4(eventTitle).replaceAll(
            "[^\\x20-\\x7e]", "");
      this.location = StringEscapeUtils.unescapeHtml4(eventLocation).replaceAll(
            "[^\\x20-\\x7e]", "");;
      this.time = eventTime;
   }
   
   //Getter/Setter methods
   public String getTime(){
      return this.time;
   }
   
   public String getTitle(){
      return this.title;
   }
   
   public String getLocation(){
      return this.location;
   }
   
   //The to String method for the class.
   public String prettyRepresentation(){
      return String.format("%s\n%s\n%s", this.title, this.time, this.location);
   }

}
