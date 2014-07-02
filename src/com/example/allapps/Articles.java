package com.example.allapps;

/**
 * Articles is a shell for the WKU news branch of the application. Each article that is pulled from the server
 * can be distinguished as an article object that has a title, URL (to access the actual article from the internet)
 * and an excerpt that can be used to find out brief information regarding the article. 
 * @author peterkaminski
 *
 */
public class Articles
{
   /**
    * Instance variables, excerpt is created but I am not sure if it will be used. 
    */
   private String articleTitle;
   private String articleUrl;
   private String articleExcerpt;
   
   public Articles(String title, String url, String excerpt)
   {
      this.articleTitle = title;
      this.articleExcerpt = excerpt;
      this.articleUrl = url;
   }
   
   /**
    * Getter methods
    * @return String values pertaining to a comic. 
    */
   public String getTitle(){
      return this.articleTitle;
   }
   
   public String getURL(){
      return this.articleUrl;
      
   }
   
   public String getExcerpt(){
      return this.articleExcerpt;
   }
   

}
