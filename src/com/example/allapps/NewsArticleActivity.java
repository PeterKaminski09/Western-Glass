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

/**
 * This is the activity for finding and displaying the WKU News, which is collected using the WordPress API. 
 * @author peterkaminski
 *
 */
public class NewsArticleActivity extends Activity
{
   // JdomParser is a parser that is created by the Argo JSON Parser jar
   private static final JdomParser JDOM_PARSER = new JdomParser();
   // AudioManager keeps track of our sounds
   private AudioManager mAudioManager;
   // Gesture detector lets us know when a tap has been sent
   private GestureDetector mGestureDetector;
   /**
    * Class variables
    */
   private String htmlContents;

   // Card scrolling objects
   private List<Card> mCards = new ArrayList<Card>();
   private CardScrollView mCardScrollView;
   private Context context = this;

   // Bundle in which to save article info in case user leaves app
   Bundle savedCards = new Bundle();
   // Async task to download news articles
   NewsTask asyncTask;

   // List of top 5 articles, set as Article Objects.
   ArrayList<Articles> articleObjects = new ArrayList<Articles>();

   // Layout items
   ImageView articleImage;
   TextView titleText, excerptText;
   String webpageURL;
   ProgressBar downloadBar;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      //This determines if there have already been some articles that were fetched from the internet,
      //This usually happens when a user clicks on an article and swipes to go back to the list of articles.
      if (savedInstanceState != null)
      {
         //Create an array list of strings that contains all the previously acquired headlines
         ArrayList<String> headlines = savedInstanceState
               .getStringArrayList("Headlines");

         mCardScrollView = new CardScrollView(context);
         ScrollAdapter adapter = new ScrollAdapter(mCards);
         mCardScrollView
               .setOnItemClickListener(new AdapterView.OnItemClickListener()
               {

                  @Override
                  public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id)
                  {
                     mAudioManager.playSoundEffect(Sounds.TAP);

                     // Set the current article to be whatever article the user
                     // is looking at.
                     webpageURL = articleObjects.get(position).getURL();
                     
                     //And access Glass's web browser intent, passing the URI as the webpage. 
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

      //Otherwise, they are accessing the news for the first time in the day or in a while
      else
      {
         /**
          * This sets the initial launch screen for the application letting the
          * user know we are pulling data from the network
          */
         setContentView(R.layout.better_launch);

         // Set up the audio and gestures
         mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         mGestureDetector = new GestureDetector(this)
               .setBaseListener(mBaseListener);

         //Get the progress bar's instance. 
         downloadBar = (ProgressBar) findViewById(R.id.downloadBar);

         // Start a new task which gets the actual WKU news from the internet.
         asyncTask = new NewsTask();
         //Start the task
         asyncTask.execute();

      }
   }

   @Override
   protected void onResume()
   {
      // OnResume we need to make sure the async task is finished and then fill
      // the arraylist of articles
      // with the articles they were last looking at
      if (asyncTask.getStatus() == AsyncTask.Status.FINISHED)
      {
         // Make sure we have the appropriate amount of articles
         if (articleObjects.size() < 5)
         {
            //Make sure we have saved info
            if (savedCards != null)
            {
               onCreate(savedCards);
            }
            //Otherwise, call on create to start the activity as if they are using the app for the first time
            else
            {
               onCreate(null);
            }
         }
      }
      super.onResume();
   }

   @Override
   protected void onPause()
   {
      // Store the headlines and urls into an arraylist
      ArrayList<String> headlines = new ArrayList<String>();
      ArrayList<String> url = new ArrayList<String>();

      // Save the top 5 articles into an array list
      if (articleObjects.size() == 5)
      {
         for (Articles articles : articleObjects)
         {
            headlines.add(articles.getTitle());
            url.add(articles.getURL());
         }
      }
      // Put the cards into the bundle, which will be passed to onCreate when it is needed. 
      savedCards.putStringArrayList("Headlines", headlines);
      savedCards.putStringArrayList("URL", url);
      super.onPause();
   }

   // This listener displays the options menu to the user whenever the user taps
   // on the glass
   // Base listener is the gesture detector's listener
   private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener()
   {
      /**
       * We allow the user to refresh their feed with a two finger tap. 
       */
      @Override
      public boolean onGesture(Gesture gesture)
      {

         //Refresh the timeline of the articles if the user does a two fingered tap
         if (gesture == Gesture.TWO_TAP)
         {
            mAudioManager.playSoundEffect(Sounds.TAP);
            //Call on create and fetch new articles. By passing null we make sure that the bundle has no articles,
            //And that the AsyncTask will be executed again. 
            onCreate(null);
            return true;
         }

         return false;
      }

   };

   //Class that must be implemented to use a gesture detector
   @Override
   public boolean onGenericMotionEvent(MotionEvent event)
   {
      return mGestureDetector.onMotionEvent(event);
   }

  

   /**
    * 
    * NewsTask creates a new thread and gets information from the Wordpress API
    * that is specific to WKUNews.wordpress.com This method is generic and can
    * be reused by any wordpress account.
    * 
    * @author peterkaminski
    * 
    */

   private class NewsTask extends AsyncTask<Void, Void, Bitmap[]>
   {

      /*
       * Before execution set the progress bar to be visible
       */
      @Override
      protected void onPreExecute()
      {
         // Executed before the thread begins
         super.onPreExecute();
         // Simulate starting the downloadBar
         downloadBar.setVisibility(0);
      }

      // This accesses the WordPress API and finds information about the top 5
      // posted articles, storing them as article objects and sending their related image's bitmaps to the postExecute
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

            for (int i = 0; i < 5; i++)
            {
               String title, excerpt, articleUrl;

               // Find a title, remove all the HTML special characters with the
               // help of the Apache Lang Libraries.
               title = json.getStringValue("posts", i, "title");
               title = StringEscapeUtils.unescapeHtml4(title).replaceAll(
                     "[^\\x20-\\x7e]", "");

               // Now we extract the excerpt of the article, giving the user a
               // brief intro to the articles contents
               String excerptHTML = json.getStringValue("posts", i, "excerpt");
               // Use jsoup to parse the html
               Document doc2 = Jsoup.parse(excerptHTML);
               // Now extract the text from the HTML
               excerpt = doc2.body().text();

               // Find the Url associated with the article
               articleUrl = json.getStringValue("posts", i, "URL");

               // Find the imgURL
               htmlContents = json.getStringValue("posts", i, "content");
               // Now parse the htmlContents using JSOUP
               Document doc = Jsoup.parse(htmlContents);

               // If there was an image in the HTML code let's grab it and
               // return it
               if (htmlContents.contains("img"))
               {
                  // Find the img tag
                  Element link = doc.select("img").first();
                  // And retrieve the URL link
                  String linkHref = link.attr("src"); // "http://...."

                  // Now we open the strem
                  InputStream inputStream = new URL(linkHref).openStream();
                  Bitmap associatedImage = BitmapFactory
                        .decodeStream(inputStream);
                  bitmaps[i] = associatedImage;
               }

               // If not lets present this generic WKU image to the user
               else
               {
                  InputStream inputStream = new URL(
                        "http://www.wkufoundation.com/Images/WKUCup.jpg")
                        .openStream();
                  Bitmap genericImage = BitmapFactory.decodeStream(inputStream);
                  bitmaps[i] = genericImage;
               }

               // Create an article object from all the gathered information
               Articles article = new Articles(title, articleUrl, excerpt);
               // Add the object to an array list of articles
               articleObjects.add(article);

            }

            // Either way we have to return the bitmap for the images, this is
            // sent to onPostExecute
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

      // After execution we need to update the UI and create the cards that will
      // be in our scroll view.
      @Override
      protected final void onPostExecute(Bitmap results[])
      {
         //For all the results, create a card and add it to the list. 
         for (int i = 0; i < results.length; i++)
         {
            Card card;
            card = new Card(context);
            card.setText(articleObjects.get(i).getTitle());
            card.setFootnote("Tap for details");
            card.setImageLayout(Card.ImageLayout.LEFT);
            card.addImage(results[i]);
            mCards.add(card);
         }

         mCardScrollView = new CardScrollView(context);
         ScrollAdapter adapter = new ScrollAdapter(mCards);
         mCardScrollView
               .setOnItemClickListener(new AdapterView.OnItemClickListener()
               {

                  @Override
                  public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id)
                  {
                     mAudioManager.playSoundEffect(Sounds.TAP);

                     // Set the current article to be whatever article the user
                     // is looking at.
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

         // Dismiss the progress bar
         downloadBar.setVisibility(4);
         // Update the view
         setContentView(mCardScrollView);

      }

   }


}
