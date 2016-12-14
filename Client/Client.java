public class Client  {


  
    public static void main(String[] args) throws IOException {
        File file = new File("RoutesUTM20NCorNew.shp");
        if (file == null) {
            return;
        }
        
        Socket socket = null;


        try {
        	  socket = new Socket("localhost",2032);   
        	
        	  ObjectInputStream is=null;
        	  ObjectOutputStream out = null;
        	
        	  out= new ObjectOutputStream(socket.getOutputStream());
        	  out.flush();
   	  
              FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
              FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
              MapContext map = new DefaultMapContext();
              map.addLayer(featureSource, null);
              ReferencedEnvelope bounds = featureSource.getBounds();
              // get a feature collection somehow
              SimpleFeatureCollection fCollection = (SimpleFeatureCollection) featureSource.getFeatures();
              //create a linear graph generate
              LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
              //wrap it in a feature graph generator
              FeatureGraphGenerator featureGen = new FeatureGraphGenerator( lineStringGen );
              //throw all the features into the graph generator
              FeatureIterator iter = fCollection.features();
              try {
              	while(iter.hasNext()){
              		Feature feature = iter.next();
              		featureGen.add( feature );
              	}
              } finally {
              	iter.close();
              }
              Graph graph = featureGen.getGraph();
              //reference to a graph, already built
              //find a source node (usually your user chooses one)
              Node start =  (Node) graph.getNodes().toArray()[5];
              // create a strategy for weighting edges in the graph
              // in this case we are using geometry length
              EdgeWeighter weighter = new EdgeWeighter() {
              	public double getWeight(Edge e) {
              		SimpleFeature feature = (SimpleFeature) e.getObject();
              		Geometry geometry = (Geometry) feature.getDefaultGeometry();
              		return geometry.getLength();
              	}
              };

      // Create GraphWalker - in this case DijkstraShortestPathFinder
              DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder( graph, start, weighter );
              pf.calculate();
        	  
        	  
        	  int pt_de_depart=(int) (Math.random() * (graph.getNodes().toArray().length-1));
      Integer ma_destination = null;
        	  out.writeObject((Node) graph.getNodes().toArray()[10]);
        	 
        	  is= new ObjectInputStream(socket.getInputStream());
        	
        	 try {
                ma_destination=(Integer)is.readObject();
        			} catch (ClassNotFoundException e2) {
        				// TODO Auto-generated catch block
        				e2.printStackTrace();
        			}
        	  
        	  
        	  
        	  
        	  
        	  
        	  
        	while (true)
        	{
        //find some destinations to calculate paths to
        		
        Node  destination = (Node) graph.getNodes().toArray()[ma_destination];       		
        Path path = pf.getPath( destination );
        if(path!=null)
        {
        System.out.println(path.toString());
        //calculate the paths
        SimpleFeatureCollection features = FeatureCollections.newCollection();

        for ( Iterator e = path.getEdges().iterator(); e.hasNext(); ) {
           Edge edge = (Edge) e.next();
           SimpleFeature feature = (SimpleFeature)  edge.getObject();
           int i = 0;
           
           	for (i=0;i<((Geometry)feature.getDefaultGeometry()).getCoordinates().length-1;i++);
           	{
           		
			double longitude=((Geometry)feature.getDefaultGeometry()).getCoordinates()[i].x;
			double latitude=((Geometry)feature.getDefaultGeometry()).getCoordinates()[i].y;
           	
			out.writeObject(longitude);
			out.writeObject(latitude);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
           	}
           	
           	
        }
        
        
        out.writeObject("FINI");
        
      pf = new DijkstraShortestPathFinder( graph, path.getLast(), weighter );
	      pf.calculate();
    	try {
			ma_destination=(Integer)is.readObject();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        }
 
        	}
           	
         
     }catch (UnknownHostException e) {

            

            e.printStackTrace();

        }catch (IOException e) {

            

            e.printStackTrace();

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

        return walk;
    }
}

