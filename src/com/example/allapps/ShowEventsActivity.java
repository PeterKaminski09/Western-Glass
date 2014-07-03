package com.example.allapps;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class ShowEventsActivity extends Activity
{

   // Card scrolling objects
   private List<Card> mCards = new ArrayList<Card>();
   // second list for the nested cards
   private List<Card> eventCards = new ArrayList<Card>();
   private CardScrollView mCardScrollView;
   private Context context = this;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_show_events);

      Intent intent = getIntent();
      ArrayList<String> eventStrings = intent
            .getStringArrayListExtra("event_strings");

      if (eventStrings.size() > 0)
      {
         for (int i = 0; i < eventStrings.size(); i++)
         {
            Card card = new Card(this);
            card.setText(eventStrings.get(i));
            mCards.add(card);
         }

      }

      else
      {
         Card card = new Card(this);
         card.setText("No events scheduled for today");
         mCards.add(card);

      }

      mCardScrollView = new CardScrollView(context);
      ExampleCardScrollAdapter adapter = new ExampleCardScrollAdapter();

      mCardScrollView.setAdapter(adapter);
      mCardScrollView.activate();

      // Update the view
      setContentView(mCardScrollView);

   }


   // This is the generic adapter for card scrolling and can be found on
   // https://developers.google.com/glass/develop/gdk/ui-widgets

   private class ExampleCardScrollAdapter extends CardScrollAdapter
   {

      @Override
      public int getPosition(Object item)
      {
         return mCards.indexOf(item);
      }

      @Override
      public int getCount()
      {
         return mCards.size();
      }

      @Override
      public Object getItem(int position)
      {
         return mCards.get(position);
      }

      @Override
      public int getViewTypeCount()
      {
         return Card.getViewTypeCount();
      }

      @Override
      public int getItemViewType(int position)
      {
         return mCards.get(position).getItemViewType();
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
         return mCards.get(position).getView(convertView, parent);

      }
   }

}
