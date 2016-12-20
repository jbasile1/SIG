package org.geotools.tutorial.quickstart;

import java.util.Random;

public class ServerThread extends Thread
{
	private Serveur serv;

	public ServerThread(Serveur serv)
	{
		super();
		this.serv = serv;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			serv.paintPos();
			serv.validate();
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
