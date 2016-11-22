package org.geotools.tutorial;

import java.io.File;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;;

public class quickstart {
	public static void main(String[] args) throws Exception{
		File file = JFileDataStoreChooser.showOpenFile("shp",null);
		if(file == null){
			return;
		}
		
		FileDataStore store = FileDataStoreFinder.getDataStore(file);
		SimpleFeatureSource featureSource = store.getFeatureSource();
		
		MapContent map = new MapContent();
		map.setTitle("quickstart");
		
		Style style =SLD.createSimpleStyle(featureSource.getSchema());
		Layer layer =new FeatureLayer(featureSource, style);
		map.addLayer(layer);
		
		JMapFrame.showMap(map);
	}

}
