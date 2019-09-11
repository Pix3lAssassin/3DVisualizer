package objLoader;

import java.awt.Color;

public class DPolygon {
	Color c;
	double[] x, y, z;
	boolean draw = true, seeThrough = false;
	double[] CalcPos, newX, newY;
	PolygonObject DrawablePolygon;
	double AvgDist;
	
	public DPolygon(double[] x, double[] y,  double[] z, Color c, boolean seeThrough)
	{
		this.x = x;
		this.y = y;
		this.z = z;		
		this.c = c;
		this.seeThrough = seeThrough; 
		createPolygon();
	}
	
	void createPolygon()
	{
		DrawablePolygon = new PolygonObject(new double[x.length], new double[x.length], c, Screen.DPolygons.size(), seeThrough);
	}
	
	void updatePolygon(Camera camera)
	{		
		newX = new double[x.length];
		newY = new double[x.length];
		draw = true;
		for(int i=0; i<x.length; i++)
		{
			CalcPos = camera.CalculatePositionP(camera.getPos(), camera.getViewTo(), x[i], y[i], z[i]);
			newX[i] = (camera.getRenderWidth()/2 - camera.CalcFocusPos[0]) + CalcPos[0] * camera.getZoom();
			newY[i] = (camera.getRenderHeight()/2 - camera.CalcFocusPos[1]) + CalcPos[1] * camera.getZoom();			
			if(camera.t < -1)
				draw = false;
		}
		
		calcLighting();
		
		DrawablePolygon.draw = draw;
		DrawablePolygon.updatePolygon(newX, newY);
		AvgDist = GetDist(camera);
	}
	
	void calcLighting()
	{
		Plane lightingPlane = new Plane(this);
		double angle = Math.acos(((lightingPlane.NV.x * Screen.LightDir[0]) + 
			  (lightingPlane.NV.y * Screen.LightDir[1]) + (lightingPlane.NV.z * Screen.LightDir[2]))
			  /(Math.sqrt(Screen.LightDir[0] * Screen.LightDir[0] + Screen.LightDir[1] * Screen.LightDir[1] + Screen.LightDir[2] * Screen.LightDir[2])));
		
		DrawablePolygon.lighting = 0.2 + 1 - Math.sqrt(Math.toDegrees(angle)/180);

		if(DrawablePolygon.lighting > 1)
			DrawablePolygon.lighting = 1;
		if(DrawablePolygon.lighting < 0)
			DrawablePolygon.lighting = 0;
	}
		
	double GetDist(Camera camera)
	{
		double total = 0;
		for(int i=0; i<x.length; i++)
			total += GetDistanceToP(camera, i);
		return total / x.length;
	}
	
	double GetDistanceToP(Camera camera, int i)
	{
		return Math.sqrt((camera.getPos()[0]-x[i])*(camera.getPos()[0]-x[i]) + 
						 (camera.getPos()[1]-y[i])*(camera.getPos()[1]-y[i]) +
						 (camera.getPos()[2]-z[i])*(camera.getPos()[2]-z[i]));
	}
}
