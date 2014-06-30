package com.example.allapps;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.glass.touchpad.GestureDetector;

public class StartDirectionsActivity extends Activity
{
   
 
   //Gesture detector lets us know when a tap has been sent
   private GestureDetector mGestureDetector;
   private String location;
   private static final int SPEECH_REQUEST = 0;
  
   /*
   //This listener displays the options menu to the user whenever the user taps on the glass
   //Base listener is the gesture detector's listener
   private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener(){
      @Override
      public boolean onGesture(Gesture gesture)
      {
         if(gesture == Gesture.TAP){
            mAudioManager.playSoundEffect(Sounds.TAP);
            openOptionsMenu();
            return true;
         }
         else{
            return false;
         }
      }
   };
   */
   
   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_start_directions_app);
      
      Log.d("Message", "Got to OnCreate");
   
      try
      {
    	  //This finds out what the user said
    	  ArrayList<String> voiceResults = getIntent().getExtras()
    	         .getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
    	  
    	  location = voiceResults.get(0).toLowerCase();
    	  startNavigation();
    
      }
      catch(NullPointerException e)
      {
    	 Intent previous = getIntent();
    	 try
    	 {
    	 location = previous.getStringExtra("Place");
    	 startNavigation();
    	 }
    	 catch(NullPointerException n)
    	 {
    	 displaySpeechRecognizer();
    	 }
      }
      
      //Now we want to determine what location the user meant, this may take some NLP
      
      
   }
   
   public void displaySpeechRecognizer() 
   {
	   Log.d("Message", "Got here.");
       Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
       intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Where would you like to go?");
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
           location = spokenText;
           
           startNavigation();
       }
       
   }
   
   @Override
   public boolean onGenericMotionEvent(MotionEvent event){
      return mGestureDetector.onMotionEvent(event);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {

      // Inflate the menu; This sets the menu that will be displayed when the user taps for options
      getMenuInflater().inflate(R.menu.start_directions, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      //We will post these activities, allowing the menu to gracefully close whenever the user taps on the
      // icon within the menu
      switch (item.getItemId()){
         case R.id.new_directions:
           
          return true;
          
         case R.id.settings:
           
            return true;
            
         default:
            return false;
      }
      
   }

   
   //This starts the navigation intent
   private void startNavigation(){
      
      String latLong = findLatLong(this.location);
      Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:ll=" + latLong + "&title="+ this.location.toUpperCase() + "&mode=w"));
      startActivity(navIntent);
      
   }
   
   
   //Returns the LatLong coordinate based upon the location the user wishes to find
   private String findLatLong(String location){
      //Set the default LatLong to that of Downing Student Union
      String latLong = "36.984874, -86.456781";
      
      //Takes care of all the locations at DUC
      if(location.contains("fresh") || location.contains("food company") || location.contains("duck") || location.contains("downing") || location.contains("student center") 
            || location.contains("bookstore") || location.contains("store") || location.contains("book") || location.contains("student union") || location.contains("dsu")  || location.contains("duc")){
         latLong = "36.984874, -86.456781";
      }
      //Garrett locations
      else if(location.contains("garrett") || location.contains("conference center") || location.contains("food court") || location.contains("DELO") || location.contains("testing center")){
         latLong = "36.986932, -86.452101";
      }
      else if(location.contains("parking") || location.contains("structure") || location.contains("1")){
         latLong = "36.987108, -86.456795";
      }
      else if(location.contains("football") || location.contains("houchens") || location.contains("smith") ){
         latLong = "36.9857363, -86.4545844";
      }
      else if(location.contains("gatton") || location.contains("florence") || location.contains("schneider") || location.contains("academy") || location.contains("mathematics")){
         latLong = "36.986962, -86.454338";
      }
      else if(location.contains("arts") || location.contains("fac") || location.contains("fine arts") || location.contains("ivan wilson") ){
         latLong = "36.986126, -86.453286";
      }
      else if(location.contains("colonnades") || location.contains("columns") || location.contains("steps") || location.contains("DELO") ){
         latLong = "36.986653, -86.453104";
      }
      else if(location.contains("valley")){
         latLong = "36.987960, -86.455357";
      }
      else if(location.contains("rodes") || location.contains("roads") || location.contains("harlin")){
         latLong = "36.988427, -86.455276";
      }
      else if(location.contains("library") || location.contains("helms") || location.contains("java") || location.contains("coffee") ){
         latLong = "36.985992, -86.452222";
      }
      else if(location.contains("cravens")){
         latLong = "36.985572, -86.452566";
      }
      else if(location.contains("van") || location.contains("meter") ){
         latLong = "36.988047, -86.452319";
      }
      else if(location.contains("cherry") ){
         latLong = "36.987448, -86.451167";
      }
      else if(location.contains("college heights") || location.contains("cooh") || location.contains("cee oh oh h") || location.contains("oh")){
         latLong = "36.986916, -86.450399";
      }
      else if(location.contains("environmental science")){
         latLong = "36.985978, -86.449965";
      }
      else if(location.contains("ebs") || location.contains("environmental and bio") || location.contains("biological sciences") || location.contains("e bee s")){
         latLong = "36.985862, -86.449005";
      }
      else if(location.contains("thompson complex central") || location.contains("tccw") || location.contains("central wing") ){
         latLong = "36.986611, -86.449171";
      }
      else if(location.contains("thompson complex north") || location.contains("north wing") || location.contains("tcnw") || location.contains("tee cee n w")){
         latLong = "36.986761, -86.448232";
      }
      else if(location.contains("snell") ){
         latLong = "36.986302, -86.448656";
      }
      else if(location.contains("gi") || location.contains("gilbert")){
         latLong = "36.987577, -86.455602";
      }
      else if(location.contains("mccormack")){
         latLong = "36.988168, -86.455940";
      }
      else if(location.contains("kentucky building") || location.contains("museum")){
         latLong = "36.988666, -86.454401";
      }
      else if(location.contains("chapel") || location.contains("chandler") || location.contains("memorial") ){
         latLong = "36.988250, -86.453575";
      }
      else if(location.contains("augenstein") || location.contains("alumni center") || location.contains("big red") ){
         latLong = "36.989480, -86.451283";
      }
      else if(location.contains("gordon") || location.contains("wilson") ){
         latLong = "36.987933, -86.451664";
      }
      else if(location.contains("music hall") || location.contains("music")){
         latLong = "36.985701, -86.453510";
      }
      else if(location.contains("bates") || location.contains("runner") ){
         latLong = "36.985731, -86.455688";
      }
      else if(location.contains("pod") || location.contains("market") ){
         latLong = "36.985692, -86.455999";
      }
      else if(location.contains("northeast") || location.contains("north") || location.contains("east") ){
         latLong = "36.984930, -86.455033";
      }
      else if(location.contains("south") || location.contains("west") || location.contains("southwest")){
         latLong = "36.984552, -86.455462";
      }
      else if(location.contains("minton") ){
         latLong = "36.984240, -86.456020";
      }
      else if(location.contains("mcclean") || location.contains("lean") ){
         latLong = "36.986151, -86.454915";
      }
      else if(location.contains("garrett") || location.contains("conference center") || location.contains("food court") || location.contains("DELO") || location.contains("testing center")){
         latLong = "36.986932, -86.452101";
      }
      else if(location.contains("wkyu") || location.contains("pbs") ){
         latLong = "36.983743, -86.457061";
      }
      else if(location.contains("student publication") || location.contains("center") ){
         latLong = "36.983863, -86.456589";
      }
      else if(location.contains("gymnasium") || location.contains("gym") || location.contains("preston") || location.contains("raymond") ){
         latLong = "36.982873, -86.458837";
      }
      else if(location.contains("mass media") || location.contains("technology") || location.contains("media") ){
         latLong = "36.982958, -86.456712";
      }
      else if(location.contains("baseball") || location.contains("nick") || location.contains("field")){
         latLong = "36.983070, -86.461133";
      }
      
      else if(location.contains("gary") || location.contains("ransdell") ){
         latLong = "36.982020, -86.456128";
      }
      else if(location.contains("pft food") || location.contains("pierce ford tower food") ){
         latLong = "36.981501, -86.460355";
      }
      else if(location.contains("pft") || location.contains("pierce") || location.contains("ford") || location.contains("tower")){
         latLong = "36.981193, -86.456128";
      }
      else if(location.contains("keen")){
         latLong = "36.981707, -86.460880";
      }
      else if(location.contains("poland")){
         latLong = "36.981896, -86.459738";
      }
      return latLong;
   }

}
