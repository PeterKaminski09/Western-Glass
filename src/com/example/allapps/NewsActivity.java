package com.example.allapps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import argo.jdom.JdomParser;
import argo.jdom.JsonRootNode;
import argo.saj.InvalidSyntaxException;

public class NewsActivity extends Activity
{

   private static final JdomParser JDOM_PARSER = new JdomParser();
   private String htmlContents;
   private String IDNumber;
   private String articleExcerpt;
   private String articleTitle;
   ImageView articleImage;
   TextView titleText, excerptText;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      // setContentView(R.layout.activity_main);

      //set the content view to be tuggable. I just added that class from the Google documentation
      //It creates a "tugging" effect on the cards but I think glass does this automatically. 
      setContentView(new TuggableView(this, R.layout.article_layout));
      articleImage = (ImageView)findViewById(R.id.articleImage);
      titleText = (TextView)findViewById(R.id.articleTitle);
      excerptText = (TextView)findViewById(R.id.articleExcerpt);
      
      //Start the asyncTask to find the news. 
      NewsTask asyncTask = new NewsTask();
      asyncTask.execute();
      
   }

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

   private class NewsTask extends AsyncTask<Void, Void, Bitmap>
   {

      @Override
      protected void onPreExecute()
      {
         super.onPreExecute();
      }

      //DoInBackground we need to connect to the API and find the JSON information regarding the latest articles
      @Override
      protected Bitmap doInBackground(Void... voids)
      {

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
            // htmlContents is the HTML code associated with the most current
            // post
            htmlContents = json.getStringValue("posts", 0, "content");
            // IDNumber is the IDNumber of the latest post, we will store this
            // into user preferences
            IDNumber = json.getNumberValue("posts", 0, "ID");
            articleTitle = json.getStringValue("posts", 0, "title");
            // Now parse the htmlContents using JSOUP
            Document doc = Jsoup.parse(htmlContents);
            
            //Now we extract the excerpt of the article, giving the user a brief intro to the 
            // articles contents
            String excerpt = json.getStringValue("posts", 0, "excerpt");
            Document doc2 = Jsoup.parse(excerpt);
            articleExcerpt = doc2.body().text();

            //If there is an image associated with the article, save it 
            if (htmlContents.contains("img"))
            {
               // Find the img tag
               Element link = doc.select("img").first();
               // And retrieve the URL link
               String linkHref = link.attr("src"); // "http://...."

               // Now we open the strem
               InputStream inputStream = new URL(linkHref).openStream();
               Bitmap associatedImage = BitmapFactory.decodeStream(inputStream);
               return associatedImage;
            }

            else
            {
               InputStream inputStream = new URL(
                     "http://www.wku.edu/mediarelations/images/wkunews_logo_rw150.png")
                     .openStream();
               Bitmap genericImage = BitmapFactory.decodeStream(inputStream);
               return genericImage;
            }
            
            

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

      @Override
      protected final void onProgressUpdate(Void... voids)
      {

      }

      @Override
      protected final void onPostExecute(Bitmap result)
      {
       articleImage.setImageBitmap(result);
       titleText.setText(articleTitle);
       excerptText.setText(articleExcerpt);
      }

   }

}
