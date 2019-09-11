package objLoader;

import java.awt.Color;
import java.util.ArrayList;

public class OBJ {
	
	private String name, fileName;
	private ArrayList<Face> faces;
	private Color c;
	private boolean visible;
	
	public String getName() {
		return name;
	}

	public ArrayList<Face> getFaces() {
		return faces;
	}

	public Color getColor() {
		return c;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public OBJ(String fileName, String name, ArrayList<Face> faces, Color c) {
		this.fileName = fileName;
		this.name = name;
		this.faces = faces;
		this.c = c;
		this.visible = false;
	}

	public String getFileName() {
		return fileName;
	}
	
	
}
