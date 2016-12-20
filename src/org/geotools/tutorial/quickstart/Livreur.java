package org.geotools.tutorial.quickstart;

import java.awt.Color;
import java.util.Random;

public class Livreur
{
	
	private Double longitude;
	private Double latitude;
	private Color color;
	
	public Livreur(Color color)
	{
		super();
		this.longitude = null;
		this.latitude = null;
		this.color = color;
	}
	
	public Livreur()
	{
		this(randomColor());
	}
	
	public Livreur(double longitude, double latitude)
	{
		this(longitude, latitude, randomColor());
	}

	public Livreur(double longitude, double latitude, Color color)
	{
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.color = color;
	}

	public void setPos(double longitude, double latitude)
	{
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public void setPos(Double longitude, Double latitude)
	{
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	private static Color randomColor()
	{
		Random rand = new Random();
		int r = rand.nextInt(256);
		int g = rand.nextInt(256);
		int b = rand.nextInt(256);
		return new Color(r,g,b);
	}

	public Color getColor()
	{
		return color;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(Double longitude)
	{
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude)
	{
		this.latitude = latitude;
	}
}
