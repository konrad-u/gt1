package s0572411;

import java.awt.Point;
import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;
import s0572411.MapCell.Status;

public class Vector {

	public double x, y;
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector(int x, int y) {
		this.x = (double)x;
		this.y = (double)y;
	}
	
	public Vector(float x, float y) {
		this.x = (double)x;
		this.y = (double)y;
	}
	
	public Vector(Point p) {
		new Vector(p.x, p.y);
	}

//----------------type casting
	
	public Point vectorToPoint(Vector v) {
		return new Point((int)v.x, (int)v.y);
	}
	
	public DivingAction DivingAction(Vector start, Vector end, float acc) {
		
		return new DivingAction(normDirectionAToB(start, end), acc);
	}
	
//--------------- vector to vector operations
	
	public Vector addVectors(Vector a, Vector b) {
		return new Vector(a.x + b.x, a.y + b.y);
	}
	
	public Vector subtractFromFirst(Vector a, Vector b) {
		return new Vector(a.x - b.x, a.y - b.y);
	}

	public Vector averageVectors(Vector a, Vector b) {
		return new Vector((a.x + b.x)/2, (a.y + b.y)/2);
	}
	
	public Vector multiplyVectors(Vector a, Vector b) {
		return new Vector(a.x * b.x, a.y * b.y);
	}
	
	public Vector fromStartToGoal(Vector start, Vector goal) {
		return new Vector(goal.x - start.x, goal.y - start.y);
	}
	
//--------------------Other
	
	public Vector normalize(Vector v) {
		return new Vector(
				v.x/Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)), 
				v.y/Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
	}

	public float normDirectionAToB(Vector a, Vector b) {
		Vector aNorm = normalize(a);
		Vector bNorm = normalize(b);
		Vector c = fromStartToGoal(aNorm, bNorm);
		return (float)Math.atan2(c.y,  c.x);
	}
	
	public float vectorLength(Vector a) {
		return (float)Math.sqrt(a.x*a.x + a.y*a.y);
	}
	
	public float distanceBetweenVectors(Vector a, Vector b) {
		return vectorLength(subtractFromFirst(a,b));
		
	}
}
