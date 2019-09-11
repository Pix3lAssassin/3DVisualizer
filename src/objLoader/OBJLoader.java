package objLoader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OBJLoader {

	public static ArrayList<OBJ> objs = new ArrayList<OBJ>();
	public static HashMap<String, ArrayList<Integer>> indexNames = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, Color> colorMap = new HashMap<String, Color>();
	
	public static void getModelsFromFile(String fileName) throws FileNotFoundException, IOException {
		
		colorMap = getColorsFromFile("res\\ModelColors.txt");
		
		File modelFile = new File(fileName);
		try(BufferedReader br = new BufferedReader(new FileReader(modelFile))) {
		    String line = br.readLine();
		    String name = "";
    		String strFile = fileName.replaceAll(".obj", "");
    		strFile = strFile.replaceAll(".*\\\\", "");
		    ArrayList<Vertex> vertices = new ArrayList<Vertex>(); 
		    ArrayList<Face> faces = new ArrayList<Face>(); 

		    while (line != null) {
			    StringBuilder sb = new StringBuilder(line);
		    	if (line.charAt(0) == 'o') {
		    		sb.delete(0, 2);
		    		name = sb.toString();
		    		if (faces.size() > 0) { 
		    		    addOBJ(strFile, name, faces);
		    			vertices.clear();
		    			faces.clear();
		    		}
		    	} else if (line.charAt(0) == 'v' && line.charAt(1) == ' ') {
		    		sb.delete(0, 2);
		    		String[] strValues = sb.toString().split(" ");
		    		double[] values = new double[strValues.length];
		    		for (int i = 0; i < values.length; i++) {
		    			values[i] = Double.parseDouble(strValues[i]);
		    		}
		    		vertices.add(new Vertex(values[0], values[2], values[1]));
		    	} else if (line.charAt(0) == 'f') {
		    		sb.delete(0, 2);
		    		String[] strValues = sb.toString().split(" ");
		    		Vertex[] verts = new Vertex[strValues.length];
		    		for (int i = 0; i < strValues.length; i++) {
		    			strValues[i] = strValues[i].replaceAll("//.*", "");
		    			verts[i] = vertices.get(Integer.parseInt(strValues[i])-1);
		    		}
		    		faces.add(new Face(verts));
		    	}
		        line = br.readLine();
		    }
		    addOBJ(strFile, name, faces);
		} catch(Exception e) {
			e.printStackTrace();
		} 	
		
	}
	
	private static HashMap<String, Color> getColorsFromFile(String path) throws FileNotFoundException, IOException {
		
		File colorFile = new File(path);
	    HashMap<String, Color> colorMap = new HashMap<String, Color>();

	    try(BufferedReader br = new BufferedReader(new FileReader(colorFile))) {
		    String line = br.readLine();
		    
		    while (line != null) {
			    String[] strValues = line.split(" ");
			    String name = strValues[0];
			    int[] rgbValues = new int[strValues.length-1];
			    for (int i = 1; i < strValues.length; i++) {
			    	rgbValues[i-1] = Integer.parseInt(strValues[i]);
			    }
			    colorMap.put(name, new Color(rgbValues[0], rgbValues[1], rgbValues[2]));
			    line = br.readLine();
		    }	
		}
		
	    return colorMap;
	}
	
	private static void addOBJ(String strFile, String name, ArrayList<Face> faces) {
		if (indexNames.containsKey(name)) {
			ArrayList<Integer> indexes = indexNames.get(name);
			indexes.add(objs.size());
			indexNames.put(name, indexes);
		} else {
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			indexes.add(objs.size());
			indexNames.put(name, indexes);
		}
		if (colorMap.containsKey(name)) {
			objs.add(new OBJ(strFile, name, faces, colorMap.get(name)));
		} else {
			objs.add(new OBJ(strFile, name, faces, new Color(255, 0, 212)));
		}	
		
	}
}
