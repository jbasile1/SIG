package org.geotools.tutorial.quickstart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseAdapter;
import org.geotools.swing.event.MapMouseEvent;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public class Serveur extends JMapPane
{
	public static final int RAYON=4;
	
	private Timer timer;
	private int millisDelay = 100;
	private List<DirectPosition2D> route;
	private Color lineColor;
	private java.awt.Stroke lineStroke;
	private boolean drawRoute;
	public RoutePainter routePainter;
	
	private static ArrayList<Livreur> livreurs;
	/**
	 * Main method. Prompts the user for a shapefile and displays it. When
	 * the user mouse clicks the map display a random walk is generated
	 * and drawn in animated form.
	 *
	 * @param args ignored
	 */
	public static void main(String[] args) throws IOException
	{
		File file = new File("RoutesUTM20NCorNew.shp");
		if (!file.exists())
		{
			return;
		}

		livreurs =new ArrayList<Livreur>();

		FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();

		MapContext map = new DefaultMapContext();
		map.addLayer(featureSource, null);
		ReferencedEnvelope bounds = featureSource.getBounds();

		final Serveur pane = new Serveur(new StreamingRenderer(), map);
		pane.setRouteStyle(Color.RED, 2.0f);
		pane.enableRouteDrawing(true);

		JFrame frame = new JFrame("Click to start a randome walk");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(pane);
		frame.setSize(800, 600);
		frame.setVisible(true);

		ServerSocket socketserver  ;
		Socket socketclient ;

		SimpleFeatureSource featuresource = dataStore.getFeatureSource();
		// get a feature collection somehow
		SimpleFeatureCollection fCollection = featuresource.getFeatures();
		//create a linear graph generate
		LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
		//wrap it in a feature graph generator
		FeatureGraphGenerator featureGen = new FeatureGraphGenerator( lineStringGen );
		//throw all the features into the graph generator

		FeatureIterator iter = fCollection.features();
		Feature feature = null;
		try {
			while(iter.hasNext()){
				feature = iter.next();

				featureGen.add( feature );
			}
		} finally {
			iter.close();
		}
		Graph graph = featureGen.getGraph()  ;
		
		ServerThread servThread = new ServerThread(pane);
		
		servThread.start();
		
		socketserver = new ServerSocket(2032);
		while(true)
		{
			try {

				socketclient = socketserver.accept();
				System.out.println("Un Livreur  s'est connecté !");
				Livreur l = new Livreur();
				livreurs.add(l);
				Thread_Livreur thread_livreur=new Thread_Livreur("thead_livreur",graph,socketclient, pane, l);
				thread_livreur.start();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public Serveur(GTRenderer renderer, MapContext context) {
		super(context);
		timer = new Timer(millisDelay, null);
		timer.setInitialDelay(0);
		timer.setRepeats(true);
		routePainter = new RoutePainter();
		//timer.addActionListener(routePainter);
		drawRoute = false;
	}


	public void paintPos()
	{
		
		
		Point current = new Point();
		AffineTransform tr = getWorldToScreenTransform();
		
		Graphics2D g2 = (Graphics2D) this.getGraphics();
		this.paint(g2);
		for(Livreur l : livreurs)
		{
			g2.setColor(l.getColor());
			g2.setStroke(lineStroke);
			try
			{
				tr.transform(new DirectPosition2D(l.getLongitude(), l.getLatitude()), current) ;
				g2.fillOval(current.x-RAYON, current.y-RAYON, RAYON*2, RAYON*2);
			}
			catch(NullPointerException e)
			{
				
			}
		}
	}


	public void setRoute(List<DirectPosition2D> route) {
		this.route = new ArrayList<DirectPosition2D>();
		this.route.addAll(route);
	}


	public void setRouteStyle(Color color, float lineWidth) {
		lineColor = color;
		lineStroke = new BasicStroke(lineWidth);
	}


	public void enableRouteDrawing(boolean b) {
		drawRoute = b;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (drawRoute && route != null) {
			routePainter.setRoute(route);
			timer.start();
		}
	}

	/**
	 * Generate a random walk
	 *
	 * @param start start location
	 * @param bounds bounds of the map display, used to scale the walk steps
	 * @param N number of steps in the walk
	 *
	 * @return a new List of DirectPosition2D walk points
	 */
	private static List<DirectPosition2D> randomWalk(DirectPosition2D start, ReferencedEnvelope bounds, int N) {
		final double stepLength = bounds.getWidth() / 100;
		final double maxTurn = Math.PI / 4;
		final CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
		Random rand = new Random();

		System.out.println("start.x : "+start.x);
		System.out.println("start.y : "+start.y);
		List<DirectPosition2D> walk = new ArrayList<DirectPosition2D>();
		DirectPosition2D pos = new DirectPosition2D(crs, start.x, start.y);
		walk.add(pos);
		double lastx = pos.x;
		double lasty = pos.y;
		double angle = Math.PI * 2 * rand.nextDouble();

		for (int i = 1; i < N; i++) {
			angle += maxTurn * (1.0 - 2.0 * rand.nextDouble());
			double x = stepLength * Math.sin(angle);
			double y = stepLength * Math.cos(angle);
			walk.add(new DirectPosition2D(crs, lastx + x+10, lasty + y+10));
			lastx += x+10;
			lasty += y+10;
		}
		List<DirectPosition2D> walk2 = new ArrayList<DirectPosition2D>();
		walk2.add(pos);
		walk2.add(new DirectPosition2D(crs, pos.x, pos.y+1000));
		walk2.add(new DirectPosition2D(crs, pos.x+1000, pos.y+1000));
		walk2.add(new DirectPosition2D(crs, pos.x+1000, pos.y));
		return walk2;
	}

	class RoutePainter implements ActionListener //TODO faire une classe a part
	{
		private Graphics2D g2 = null;
		private AffineTransform tr;
		private ListIterator<DirectPosition2D> iter;
		private Point previous;
		private Point current;
		private boolean firstPoint = true;

		public void setRoute(List<DirectPosition2D> route)
		{
			tr = getWorldToScreenTransform();

			g2 = (Graphics2D) Serveur.this.getGraphics();
			g2.setColor(lineColor);
			g2.setStroke(lineStroke);

			iter = route.listIterator();
			previous = new Point();
			current = new Point();

			if (iter.hasNext()) {
				tr.transform(iter.next(), previous);
			} else {
				finish();
			}
		}

		/**
		 * This method is called by the animated map timer on each
		 * time step. It draws the next segment in the route. If
		 * there are no more segments to draw it stops the timer.
		 */
		
		public void paintPos(double x, double y, Color c)
		{
			//Serveur.this.repaint();
			Point current = new Point();
			tr = getWorldToScreenTransform();
			
			g2 = (Graphics2D) Serveur.this.getGraphics();
			g2.setColor(c);
			g2.setStroke(lineStroke);
			
			tr.transform(new DirectPosition2D(x, y), current) ;
			g2.fillOval(current.x, current.y, 5, 5);
			System.out.println("-------------------------------------------");
			System.out.println(current.x);
			System.out.println(current.y);
			System.out.println("-------------------------------------------");
		}

		public void actionPerformed(ActionEvent e) {
			if (iter.hasNext()) {
				tr.transform(iter.next(), current);
				g2.drawLine(previous.x, previous.y, current.x, current.y);
				previous.setLocation(current);

			} else {
				finish();
			}
		}
		private void finish() {
			timer.stop();
			g2.dispose();
		}

	}
}
