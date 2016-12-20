package org.geotools.tutorial.quickstart;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;

public class Thread_Livreur extends Thread
{
	private int id;

	private Color c;
	private Serveur serveur;
	
	public Livreur livreur;
	private Socket socketclient;
	private Graph graph;
	private static Layer couche_livreur;
	private MapContent map;
	private static int ID = 0;
	
	public Thread_Livreur (String name,Graph graph,Socket socketclient , Serveur serveur, Livreur livreur)
	{
	    super(name);
	    this.serveur = serveur;
	    
	    this.livreur = livreur;
	    
	    this.socketclient=socketclient;
	    this.graph=graph;
	    this.id=ID;
	    ID++;
	  }

	
	  public void run()
	  {
		  
		 
		  ObjectInputStream is=null;
		  ObjectOutputStream out = null;
		  try {
			  //reception de message de la part du client
				 out= new ObjectOutputStream(socketclient.getOutputStream());
				 out.flush();
			 is= new ObjectInputStream(socketclient.getInputStream());
			try {
				// recuperation de la position courante
				
				Node position_client=(Node)is.readObject();
				
				//generation d'une node destination pour le client
				int destination=(int) (Math.random() * (graph.getNodes().toArray().length-1));
				
				//envoie de la destination au client
			
				out.writeObject((Object)destination);	
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  boolean boucle_bool=true;
		  while (boucle_bool)
		  {		
				try {
					
					//Lecture de la position du client
					Object obj=is.readObject();
					//si ce nest pas une chaine ce sont des coordonnees
					if (!(obj instanceof String))
					{
						
						Double longitude_client=(Double)obj;
						obj=is.readObject();
						Double latitude_client=(Double)obj;
						refresh_Livreur_layer(longitude_client,latitude_client,map);
						livreur.setPos(longitude_client, latitude_client);
						//System.out.println("Longitude "+longitude_client+" Latitude "+latitude_client);
						//serveur.routePainter.paintPos(longitude_client.doubleValue(), latitude_client.doubleValue(), livreur.getColor());
					}
					//si c une chair le client est arrive a destination il faut alors generer une nouvelle destination pour lui
					else
					{
						int destination=(int) (Math.random() * (graph.getNodes().toArray().length-1));
						out.writeObject((Object)destination);
					}
					
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
		  }
	   /* try {
			socketclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	  } 
	  
	  
	  //Fonction qui a pour but d'actualier la la couche livreur cela permettra de coir le deplacemment des livreur en temps reel
	  public static Layer refresh_Livreur_layer(double latitude,double longitude, MapContent map)
	  {
		   
	        return null;
	  }

}
