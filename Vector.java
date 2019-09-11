package objLoader;

public class Vector {
	public double x, y, z;
	public Vector(double x, double y, double z)
	{
		double Length = Math.sqrt(x*x + y*y + z*z);

		if(Length>0)
		{
			this.x = x/Length;
			this.y = y/Length;
			this.z = z/Length;
		}

	}
	
	public Vector CrossProduct(Vector V)
	{
		Vector CrossVector = new Vector(
				y * V.z - z * V.y,
				z * V.x - x * V.z,
				x * V.y - y * V.x);
		return CrossVector;		
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
}
