package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class NewsArticleActivity extends Activity
{
   //JdomParser is a parser that is created by the Argo JSON Parser jar
   private static final JdomParser JDOM_PARSER = new JdomParser();
   // AudioManager keeps track of our sounds
   private AudioManager mAudioManager;
   //Gesture detector lets us know when a tap has been sent
   private GestureDetector mGestureDetector;
   //We create a handler to queue which activities will be started
   private final Handler mHandler = new Handler();
   /**
    * Class variables
    */
   private String htmlContents;
//   private String IDNumber;
//   private String articleExcerpt;
//   private String articleTitle;
//   private int currentArticle = 0;
  
   //Card scrolling objects
   private List<Card> mCards = new ArrayList<Card>();
   private CardScrollView mCardScrollView;
   private Context context = this;
   
   //Bundle in which to save article info in case user leaves app
   Bundle savedCards = new Bundle();
   //Async task to download news articles
   NewsTask asyncTask;
   
   //List of top 5 articles, set as Article Objects. 
   ArrayList<Articles> articleObjects = new ArrayList<Articles>();
   
   //Layout items
   ImageView articleImage;
   TextView titleText, excerptText;
   String webpageURL;
   ProgressBar downloadBar;

   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      
      Log.d("OnCreate","is called");
      
      if(savedInstanceState!=null)
      {
    	  ArrayList<String> headlines = savedInstanceState.getStringArrayList("Headlines");
    	  
    	  Log.d("MESSAGE", headlines.get(0));
    	  mCardScrollView = new CardScrollView(context);
          ExampleCardScrollAdapter adapter = new ExampleCardScrollAdapter();
          mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener()
          {

             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                   int position, long id)
             {
                mAudioManager.playSoundEffect(Sounds.TAP);
                
                //Set the current article to be whatever article the user is looking at.
                webpageURL = articleObjects.get(position).getURL();
                
                Uri webpage = Uri.parse(webpageURL);
 	            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
 	            if (intent.resolveActivity(getPackageManager()) != null) 
 	            {
 	                startActivity(intent);
 	            }
                
             }
             
          });
          mCardScrollView.setAdapter(adapter);
          mCardScrollView.activate();
      }
      
      else{
      /**
       * setContentView opens the app with our launch layout, we will switch the layout within the AsyncTask
       */
    	  
    	  Log.d("OnCreate", "content happens");
      setContentView(new TuggableView(this, R.layout.better_launch));

      //Set up the audio and gestures
      mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
      
      /**
       * This sets the initial launch screen for the application letting the user know we are pulling data from
       * the network
       */
      downloadBar = (ProgressBar) findViewById(R.id.downloadBar);
     
      //Start a new task which gets the actual WKU news from the internet. 
      asyncTask = new NewsTask();
      asyncTask.execute();
      
      }
   }
   
   @Override
	protected void onResume()
	{
		Log.d("Message","On Resume is called");
		
		if(asyncTask.getStatus()==AsyncTask.Status.FINISHED)
		{
		if(articleObjects.size()<5)
		{
		if (savedCards != null){
			Log.d("OnCreate", "with saved info");
			onCreate(savedCards);
		} 
		else{
			Log.d("OnCreate", "no saved info");
			onCreate(null);
		}
		}
		else
		{
			Log.d("OnCreate", "not explicitly called");
		}
		}
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		Log.d("Message","On Pause is called");
		
		ArrayList<String> headlines = new ArrayList<String>();
		ArrayList<String> url = new ArrayList<String>();
		
		if(articleObjects.size()==5)
		{
			for(Articles articles: articleObjects)
			{
				headlines.add(articles.getTitle());
				url.add(articles.getURL());
			}
		}
		savedCards.putStringArrayList("Headlines", headlines);
		savedCards.putStringArrayList("URL", url);
		super.onPause();
	}
	
	@Override
	protected void onStop()
	{
		Log.d("Message", "On Stop is called");
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		Log.d("Message", "On Destroy is called");
		super.onDestroy();
	}
   
   //This listener displays the options menu to the user whenever the user taps on the glass
   //Base listener is the gesture detector's listener
   private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener(){
      @Override
      public boolean onGesture(Gesture gesture)
      {
 
         if(gesture == Gesture.TWO_TAP)
         {
            mAudioManager.playSoundEffect(Sounds.TAP);
            
           onCreate(null);
           return true;
         }
         
            return false;
         }
         
      };


   @Override
   public boolean onGenericMotionEvent(MotionEvent event){
      return mGestureDetector.onMotionEvent(event);
   }

   @Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:

			    //ACTION HERE
				 mAudioManager.playSoundEffect(Sounds.TAP);
		            
		            Log.d("Message","I got here.");
		            
		            Uri webpage = Uri.parse(webpageURL);
		            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
		            if (intent.resolveActivity(getPackageManager()) != null) 
		            {
		                startActivity(intent);
		            }
		                return true;
			default:
				return super.onKeyDown(keyCode, event);
			            
		}
		
	}
   


   /**
    * 
    * NewsTask creates a new thread and gets information from the Wordpress API that is specific to WKUNews.wordpress.com
    * This method is generic and can be reused by any wordpress account. 
    * @author peterkaminski
    *
    */
  
   private class NewsTask extends AsyncTask<Void, Void, Bitmap[]>
   {

      @Override
      protected void onPreExecute()
      {  
         //Executed before the thread begins
         super.onPreExecute();
         //Simulate starting the downloadBar
         downloadBar.setVisibility(0);
      }

      //This access the WordPress api and finds information about the top 5 posted articles, storing them as article objects
      @Override
      protected Bitmap[] doInBackground(Void... voids)
      {
         Bitmap[] bitmaps = new Bitmap[5];
         try
         {
            URL url = new URL(
                  "https://public-api.wordpress.com/rest/v1/sites/www.wkunews.wordpress.com/posts//?pretty=1");
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
            // Convert bufferReader into a string
            String jsonText = bufferReader.toString();
            // JsonRootNode turns the string into parseable JSON
            JsonRootNode json = JDOM_PARSER.parse(jsonText);
            
            for(int i = 0; i < 5; i++){
               String title, excerpt, articleUrl;
               
               //Find a title, remove all the HTML special characters with the help of the Apache Lang Libraries.
               title = json.getStringValue("posts", i, "title");
               title = StringEscapeUtils.unescapeHtml4(title).replaceAll("[^\\x20-\\x7e]", "");
               
               
               // Now we extract the excerpt of the article, giving the user a
               // brief intro to the articles contents
               String excerptHTML = json.getStringValue("posts", i, "excerpt");
               //Use jsoup to parse the html
               Document doc2 = Jsoup.parse(excerptHTML);
               //Now extract the text from the HTML
               excerpt = doc2.body().text();
               
               //Find the Url associated with the article
               articleUrl = json.getStringValue("posts", i, "URL");
               
               //Find the imgURL
               htmlContents = json.getStringValue("posts", i, "content");
               // Now parse the htmlContents using JSOUP
               Document doc = Jsoup.parse(htmlContents);
               
               //If there was an image in the HTML code let's grab it and return it
               if (htmlContents.contains("img"))
               {
                  // Find the img tag
                  Element link = doc.select("img").first();
                  // And retrieve the URL link
                  String linkHref = link.attr("src"); // "http://...."

                  // Now we open the strem
                  InputStream inputStream = new URL(linkHref).openStream();
                  Bitmap associatedImage = BitmapFactory.decodeStream(inputStream);
                  bitmaps[i] = associatedImage;
               }
               
               //If not lets present this generic WKU image to the user
               else
               {
                  InputStream inputStream = new URL(
                        "http://www.wkufoundation.com/Images/WKUCup.jpg").openStream();
                  Bitmap genericImage = BitmapFactory.decodeStream(inputStream);
                  bitmaps[i] = genericImage;
               }
               
              //Create an article object from all the gathered information
              Articles article = new Articles(title, articleUrl, excerpt);
              //Add the object to an array list of articles
              articleObjects.add(article);
              
               
            }

            //Either way we have to return the bitmap for the images, this is sent to onPostExecute
            return bitmaps;
         }
         catch (MalformedURLException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (InvalidSyntaxException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

         return null;
      }

      //After execution we need to update the UI and create the cards that will be in our scroll view. 
      @Override
      protected final void onPostExecute(Bitmap results[])
      {
         
         for(int i = 0; i < results.length; i++){
            Card card;
            card = new Card(context);
            card.setText(articleObjects.get(i).getTitle());
            card.setFootnote("Tap for details");
            card.setImageLayout(Card.ImageLayout.LEFT);
            card.addImage(results[i]);
            mCards.add(card);
         }
         
         mCardScrollView = new CardScrollView(context);
         ExampleCardScrollAdapter adapter = new ExampleCardScrollAdapter();
         mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener()
         {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                  int position, long id)
            {
               mAudioManager.playSoundEffect(Sounds.TAP);
               
               //Set the current article to be whatever article the user is looking at.
               webpageURL = articleObjects.get(position).getURL();
               
               Uri webpage = Uri.parse(webpageURL);
	            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
	            if (intent.resolveActivity(getPackageManager()) != null) 
	            {
	                startActivity(intent);
	            }
               
            }
            
         });
         mCardScrollView.setAdapter(adapter);
         mCardScrollView.activate();
         

         Log.d("OnPostExecute", "Card scroll view activated");
         //Dismiss the progress bar
         downloadBar.setVisibility(4);
         //Update the view
         setContentView(mCardScrollView);
         
         
      }

   }
   
   //This is the generic adapter for card scrolling and can be found on https://developers.google.com/glass/develop/gdk/ui-widgets
   
   private class ExampleCardScrollAdapter extends CardScrollAdapter {

      @Override
      public int getPosition(Object item) {
          return mCards.indexOf(item);
      }

      @Override
      public int getCount() {
          return mCards.size();
      }

      @Override
      public Object getItem(int position) {
          return mCards.get(position);
      }

      @Override
      public int getViewTypeCount() {
          return Card.getViewTypeCount();
      }

      @Override
      public int getItemViewType(int position){
          return mCards.get(position).getItemViewType();
      }

      @Override
      public View getView(int position, View convertView,
              ViewGroup parent) {
          return  mCards.get(position).getView(convertView, parent);
        
      }
  }

	}
