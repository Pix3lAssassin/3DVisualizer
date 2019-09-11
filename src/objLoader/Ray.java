package objLoader;

import java.util.ArrayList;

public class Ray {

	private Vertex origin;
	private Vector direction;
	
	Ray(Vertex origin, Vector direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	public Vertex march() {
		return new Vertex(0, 0, 0);
	}
	
	public double getClosestFace(Vertex start, ArrayList<OBJ> objs) {
		for (OBJ obj : objs) {
			for (Face face : obj.getFaces()) {
				double faceNormalX = face.getNormal().getDirection().getX();
				double faceNormalY = face.getNormal().getDirection().getY();
				double faceNormalZ = face.getNormal().getDirection().getZ();
				double t = 0;
				t = (face.getConstant() - 
						(faceNormalX * origin.getX() + 
						faceNormalY * origin.getY() + 
						faceNormalZ * origin.getZ())) /
							(faceNormalX * faceNormalX +
							faceNormalY * faceNormalY +
							faceNormalZ * faceNormalZ);
				Vertex point = new Vertex(origin.getX() + faceNormalX * t, 
						origin.getY() + faceNormalY * t, 
						origin.getZ() + faceNormalZ * t);
				
			}
		}
	}

	public Vertex getOrigin() {
		return origin;
	}

	public Vector getDirection() {
		return direction;
	}

}
