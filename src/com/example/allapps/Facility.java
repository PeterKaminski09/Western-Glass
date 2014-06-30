package com.example.allapps;

public class Facility 
{
	private String name;
	private String open;
	private String close;
	
	//Default constructor
	public Facility()
	{
		
	}
	
	//Intial value Constructor 1
	public Facility(String facilityName)
	{
		setName(facilityName);
	}
	
	//Initial value Constructor 2
	public Facility(String facilityName, String openTime, String closeTime)
	{
		setName(facilityName);
		setOpen(openTime);
		setClose(closeTime);
	}
	
	//Set methods
	public void setName (String facilityName)
	{
		name=facilityName;
	}
	
	public void setOpen (String openTime)
	{
		open=openTime;
	}
	
	public void setClose (String closeTime)
	{
		close=closeTime;
	}
	
	//Get methods
	public String getName ()
	{
		return name;
	}
	
	public String getOpen()
	{
		return open;
	}
	
	public String getClose()
	{
		return close;
	}

}
