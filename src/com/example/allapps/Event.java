package com.example.allapps;

public class Event
{
   private String title, time, location;
   public Event(String eventTitle, String eventTime, String eventLocation)
   {
      this.title = eventTitle;
      this.location = eventLocation;
      this.time = eventTime;
   }
   
   //Getter methods
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
