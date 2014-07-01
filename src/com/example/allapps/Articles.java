package com.example.allapps;

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
