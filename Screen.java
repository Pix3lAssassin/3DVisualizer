package objLoader;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.JPanel;

public class Screen extends JPanel implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	// ArrayList of all the 3D polygons - each 3D polygon has a 2D 'PolygonObject'
	// inside called 'DrawablePolygon'
	static ArrayList<Cube> Cubes = new ArrayList<Cube>();
	static ArrayList<DPolygon> DPolygons = new ArrayList<DPolygon>();
	
	//Cameras
	ArrayList<Camera> cameras = new ArrayList<Camera>();
	BufferedImage[] testImage;
	int currentCamera;

	// Used for keeping mouse in center
	Robot r;

	static double[] LightDir = new double[] { 1, 1, 1 };

	// The smaller the zoom the more zoomed out you are and visa versa, although
	// altering too far from 1000 will make it look pretty weird
	static double MouseX = 0, MouseY = 0, MovementSpeed = 0.25;

	// FPS is a bit primitive, you can set the MaxFPS as high as u want
	double drawFPS = 0, MaxFPS = 1000, SleepTime = 1000.0 / MaxFPS, LastRefresh = 0,
			StartTime = System.currentTimeMillis(), LastFPSCheck = 0, Checks = 0;

	double SunPos = 0;
	// will hold the order that the polygons in the ArrayList DPolygon should be
	// drawn meaning DPolygon.get(NewOrder[0]) gets drawn first

	static boolean OutLines = false;
	boolean[] Keys = new boolean[4];

	long repaintTime = 0;

	public Screen() {
		this.addKeyListener(this);
		setFocusable(true);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		invisibleMouse();

//		Cubes.add(new Cube(0, -5, 0, 2, 2, 2, Color.yellow));
		
		try {
			OBJLoader.getModelsFromFile(
					"res\\untitled.obj");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Create objs
		createObjs();

		//Create ground
		createGround();
		
		//Initialize cameras
		try {
			cameras = loadCameras("res\\cameras.json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
				
		for (Camera camera : cameras)
			camera.fillPolygonList(DPolygons);
		
		testImage = new BufferedImage[1];
		testImage[0] = cameras.get(1).renderImage(32, 32);
	}

	public void paintComponent(Graphics g) {

		CameraMovement();

		ControlSunAndLight();

		cameras.get(currentCamera).renderToScreen(g);

		// FPS display
		g.drawString("FPS: " + (int) drawFPS + " (Benchmark)", 40, 40);
		g.drawString("Camera " + (currentCamera+1), 200, 40);
		for (int i = 0; i < testImage.length; i++) {
			g.drawImage(testImage[i],(int) DDDTutorial.ScreenSize.getWidth()-testImage[i].getWidth()*4 - (testImage[i].getWidth()*4*i), 
					(int) DDDTutorial.ScreenSize.getHeight()-testImage[i].getHeight()*4, 
					testImage[i].getWidth()*4, testImage[i].getHeight()*4, null);
			g.drawRect((int) DDDTutorial.ScreenSize.getWidth()-testImage[i].getWidth()*4 - (testImage[i].getWidth()*4*i), 
					(int) DDDTutorial.ScreenSize.getHeight()-testImage[i].getHeight()*4, 
					testImage[i].getWidth()*4, testImage[i].getHeight()*4);
		}
		
//		repaintTime = System.currentTimeMillis() - repaintTime; 
//		System.out.println(repaintTime);
		SleepAndRefresh();
	}
	
	public void saveCameras(String filename) throws Exception {
		JSONObject file = new JSONObject();
		JSONArray cams = new JSONArray();
		for (int i = 0; i < cameras.size(); i++) {
			JSONObject camera = new JSONObject();
			camera.put("renderWidth", cameras.get(i).getRenderWidth());
			camera.put("renderHeight", cameras.get(i).getRenderHeight());
			camera.put("zoom", cameras.get(i).getZoom());
			camera.put("fov", cameras.get(i).getFov());
			camera.put("vertLook", cameras.get(i).getVertLook());
			camera.put("horLook", cameras.get(i).getHorLook());
			for (int x = 0; x < cameras.get(i).getPos().length; x++) {
				camera.put("pos " + (x == 0 ? "x" : x == 1 ? "y" : "z"), cameras.get(i).getPos()[x]);
			}
			cams.add(camera);
		}
		file.put("cameras", cams);
		Files.write(Paths.get(filename), file.toJSONString().getBytes());
	}
	
	public ArrayList<Camera> loadCameras(String filename) throws FileNotFoundException, IOException, ParseException {
		ArrayList<Camera> camList = new ArrayList<Camera>();
		
		FileReader reader = new FileReader(filename);
		JSONParser jsonParser = new JSONParser();
		JSONObject file = (JSONObject) jsonParser.parse(reader);
		JSONArray cams = (JSONArray) file.get("cameras");
		if (cams.size() <= 0) {
			return new ArrayList<Camera>();
		} else {
			for (int i = 0; i < cams.size(); i++) {
				JSONObject camera = (JSONObject) cams.get(i);
				Camera cam = new Camera((double)camera.get("renderWidth"), (double)camera.get("renderHeight"),
						new double[] {(double)camera.get("pos x"), (double)camera.get("pos y"), (double)camera.get("pos z")},
						(double)camera.get("vertLook"), (double)camera.get("horLook"), 
						(double)camera.get("zoom"), (double)camera.get("fov"));
				camList.add(cam);
			}
			
			return camList;
		}

	}
	
	public void createGround() {
		int size = 10;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				double padSize = 4;
				Vertex[] vertices = new Vertex[] {
						new Vertex(-size*(padSize/2) + y * padSize, -size*(padSize/2) + x * padSize, 0),
						new Vertex(-size*(padSize/2) + y * padSize, -size*(padSize/2) + x * padSize + padSize, 0), 
						new Vertex(-size*(padSize/2) + y * padSize + padSize, -size*(padSize/2) + x * padSize + padSize, 0), 
						new Vertex(-size*(padSize/2) + y * padSize + padSize, -size*(padSize/2) + x * padSize, 0) };
				DPolygons.add(new DPolygon(
						new double[] { vertices[0].getX(), vertices[1].getX(), vertices[2].getX(), vertices[3].getX() },
						new double[] { vertices[0].getY(), vertices[1].getY(), vertices[2].getY(), vertices[3].getY() },
						new double[] { vertices[0].getZ(), vertices[1].getZ(), vertices[2].getZ(), vertices[3].getZ() },
						new Color(64, 72, 96), false));
			}
		}
	}
	
	public void createObjs() {
		if (OBJLoader.objs.size() > 0) {
			for (OBJ obj : OBJLoader.objs) {
				for (Face face : obj.getFaces()) {
					double[] xVertexCoordinates = new double[face.getVerts().length];
					double[] yVertexCoordinates = new double[face.getVerts().length];
					double[] zVertexCoordinates = new double[face.getVerts().length];
					for (int i = 0; i < face.getVerts().length; i++) {
						xVertexCoordinates[i] = face.getVerts()[i].getX();
						yVertexCoordinates[i] = face.getVerts()[i].getY();
						zVertexCoordinates[i] = face.getVerts()[i].getZ();
					}
					DPolygons.add(new DPolygon(xVertexCoordinates, yVertexCoordinates, zVertexCoordinates,
							obj.getColor(), false));
				}
			}
		}
	}
	
	void invisibleMouse() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
		Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "InvisibleCursor");
		setCursor(invisibleCursor);
	}

	void SleepAndRefresh() {
		long timeSLU = (long) (System.currentTimeMillis() - LastRefresh);

		Checks++;
		if (Checks >= 15) {
			drawFPS = Checks / ((System.currentTimeMillis() - LastFPSCheck) / 1000.0);
			LastFPSCheck = System.currentTimeMillis();
			Checks = 0;
		}

		if (timeSLU < 1000.0 / MaxFPS) {
			try {
				Thread.sleep((long) (1000.0 / MaxFPS - timeSLU));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		LastRefresh = System.currentTimeMillis();

		repaint();
	}

	void ControlSunAndLight() {
		SunPos += 0.005;
		double mapSize = 100;
		LightDir[0] = mapSize / 2 - (mapSize / 2 + Math.cos(SunPos) * mapSize * 10);
		LightDir[1] = -200;
		LightDir[2] = mapSize / 2 - (mapSize / 2 + Math.sin(SunPos) * mapSize * 10);
	}

	void CameraMovement() {
		Vector ViewVector = new Vector(cameras.get(currentCamera).getViewTo()[0] - cameras.get(currentCamera).getPos()[0], 
				cameras.get(currentCamera).getViewTo()[1] - cameras.get(currentCamera).getPos()[1], 
				cameras.get(currentCamera).getViewTo()[2] - cameras.get(currentCamera).getPos()[2]);
		double xMove = 0, yMove = 0, zMove = 0;
		Vector VerticalVector = new Vector(0, 0, 1);
		Vector SideViewVector = ViewVector.CrossProduct(VerticalVector);

		if (Keys[0]) {
			xMove += ViewVector.x;
			yMove += ViewVector.y;
			zMove += ViewVector.z;
		}

		if (Keys[2]) {
			xMove -= ViewVector.x;
			yMove -= ViewVector.y;
			zMove -= ViewVector.z;
		}

		if (Keys[1]) {
			xMove += SideViewVector.x;
			yMove += SideViewVector.y;
			zMove += SideViewVector.z;
		}

		if (Keys[3]) {
			xMove -= SideViewVector.x;
			yMove -= SideViewVector.y;
			zMove -= SideViewVector.z;
		}

		Vector MoveVector = new Vector(xMove, yMove, zMove);
		MoveTo(cameras.get(currentCamera).getPos()[0] + MoveVector.x * MovementSpeed, 
				cameras.get(currentCamera).getPos()[1] + MoveVector.y * MovementSpeed,
				cameras.get(currentCamera).getPos()[2] + MoveVector.z * MovementSpeed);
	}

	void MoveTo(double x, double y, double z) {
		cameras.get(currentCamera).setPos(new double[] {x, y, z});
	}

	void MouseMovement(double NewMouseX, double NewMouseY) {
		double difX = (NewMouseX - DDDTutorial.ScreenSize.getWidth() / 2);
		double difY = (NewMouseY - DDDTutorial.ScreenSize.getHeight() / 2);
		difY *= 6 - Math.abs(cameras.get(currentCamera).getVertLook()) * 5;
		cameras.get(currentCamera).addVertLook(-(difY / cameras.get(currentCamera).getVertRotSpeed()));
		cameras.get(currentCamera).addHorLook(difX / cameras.get(currentCamera).getHorRotSpeed());

		if (cameras.get(currentCamera).getVertLook() > 0.999)
			cameras.get(currentCamera).setVertLook(0.999);

		if (cameras.get(currentCamera).getVertLook() < -0.999)
			cameras.get(currentCamera).setVertLook(-0.999);

	}

	void CenterMouse() {
		try {
			r = new Robot();
			r.mouseMove((int) DDDTutorial.ScreenSize.getWidth() / 2, (int) DDDTutorial.ScreenSize.getHeight() / 2);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_W)
			Keys[0] = true;
		if (e.getKeyCode() == KeyEvent.VK_A)
			Keys[1] = true;
		if (e.getKeyCode() == KeyEvent.VK_S)
			Keys[2] = true;
		if (e.getKeyCode() == KeyEvent.VK_D)
			Keys[3] = true;
		if (e.getKeyCode() == KeyEvent.VK_O)
			OutLines = !OutLines;
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
		if (e.getKeyCode() == KeyEvent.VK_P) {
			try {
				saveCameras("res\\cameras.json");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_I) {
			cameras.add(new Camera((int) DDDTutorial.ScreenSize.getWidth(), (int) DDDTutorial.ScreenSize.getHeight(),
				new double[] {10, 10, 5}, -0.05, -2.35, 1000, 60) );
			cameras.get(cameras.size()-1).fillPolygonList(DPolygons);
			currentCamera = cameras.size()-1;
		}
		if (e.getKeyCode() == KeyEvent.VK_U) {
			if (cameras.size() > 1) {
				cameras.remove(currentCamera);
				currentCamera -= 1;
				if (currentCamera < 0)
					currentCamera = 0;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			currentCamera++;
			if (currentCamera >= cameras.size()) {
				currentCamera = 0;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			currentCamera--;
			if (currentCamera < 0) {
				currentCamera = cameras.size()-1;
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_W)
			Keys[0] = false;
		if (e.getKeyCode() == KeyEvent.VK_A)
			Keys[1] = false;
		if (e.getKeyCode() == KeyEvent.VK_S)
			Keys[2] = false;
		if (e.getKeyCode() == KeyEvent.VK_D)
			Keys[3] = false;
	}

	public void keyTyped(KeyEvent e) {
	}

	public void mouseDragged(MouseEvent arg0) {
		MouseMovement(arg0.getX(), arg0.getY());
		MouseX = arg0.getX();
		MouseY = arg0.getY();
		CenterMouse();
	}

	public void mouseMoved(MouseEvent arg0) {
		MouseMovement(arg0.getX(), arg0.getY());
		MouseX = arg0.getX();
		MouseY = arg0.getY();
		CenterMouse();
	}

	public void mouseClicked(MouseEvent arg0) {
		testImage[0] = cameras.get(currentCamera).renderImage(32, 32);
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}
}
