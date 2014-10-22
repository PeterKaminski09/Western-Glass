package com.example.allapps;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
/*
 * This object class is called throughout the application to create adpaters for card scroll views. The ScrollAdapter is passed a 
 * list of cards during initialization, which will be displayed by a card scroll view.
 * 
 * This is the generic adapter for card scrolling and can be found on
 * https://developers.google.com/glass/develop/gdk/ui-widgets
 * 
 * Code commented by Lydia Buzzard
 */
public class ScrollAdapter extends CardScrollAdapter
{
	List<CardBuilder> mCards = new ArrayList<CardBuilder>();
	
	public ScrollAdapter(List<CardBuilder> cards)
	{
		mCards = cards;
	}
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
        return CardBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position)
    {
        return mCards.get(position).getItemViewType();
    }

    @Override
    public View getView(int position, View convertView,
            ViewGroup parent) 
    {
        return  mCards.get(position).getView(convertView, parent);
    }

}
