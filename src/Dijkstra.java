import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.DijkstraIterator.EdgeWeighter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class Dijkstra 
{


  
    public static void main(String[] args) throws IOException {
        File file = new File("RoutesUTM20NCorNew.shp");
        if (file == null) {
            return;
        }

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

        //find some destinations to calculate paths to
        Node  destination = (Node) graph.getNodes().toArray()[10];
        		
        		
        		
        		
        		  Path path = pf.getPath( destination );
        		  System.out.println(path.toString());
        //calculate the paths
      
        
        
        
        
        
        
        
        
        

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

