package objLoader;

public class Face {

	private Vertex[] verts;
	private Ray[] vertexNormals;
	private Ray faceNormal;
	private double constant;
	private Vertex center;
	
	public Face(Vertex... verts) {
		this.verts = verts;
		double x = 0, y = 0, z = 0;
		for (int i = 0; i < verts.length; i++) {
			x += verts[i].getX();
			y += verts[i].getY();
			z += verts[i].getZ();
		}
		x /= verts.length;
		y /= verts.length;
		z /= verts.length;
		faceNormal = new Ray(new Vertex(x, y, z), calculateSurfaceNormal(verts));
		constant = faceNormal.getDirection().getX() * verts[0].getX() + 
				faceNormal.getDirection().getY() * verts[0].getY() + 
				faceNormal.getDirection().getZ() * verts[0].getZ();
	}
	
	public Vertex[] getVerts() {
		return verts;
	}
	
	public Ray getNormal() {
		return faceNormal;
	}
	
	public double getConstant() {
		return constant;
	}
	
	private Vector calculateSurfaceNormal (Vertex[] verts) {

		Vector u = new Vector(verts[1].getX() - verts[0].getX(), verts[1].getY() - verts[0].getY(), verts[1].getZ() - verts[0].getZ());
		Vector v = new Vector(verts[2].getX() - verts[0].getX(), verts[2].getY() - verts[0].getY(), verts[2].getZ() - verts[0].getZ());
	
		Vector normal = u.CrossProduct(v);
				
		return normal;
	}
}
