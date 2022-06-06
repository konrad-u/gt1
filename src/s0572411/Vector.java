package s0572411;

import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;
import s0572411.MapCell.Status;

public class Vector {

	public double x, y;
	
	public Vector() {
		
	}
	
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

	public Vector(Vector v) {
		x = v.x;
		y = v.y;
	}
	
//----------------type casting
	
	public Point vectorToPoint(Vector v) {
		return new Point((int)v.x, (int)v.y);
	}
	
	public DivingAction vectorToDivingAction(Vector start, Vector end, float acc) {
		
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
	
	public Vector seekVector(Vector pos, Vector goal) {
		return pos.subtractFromFirst(goal,  pos);
	}
	
	public Vector fleeVector(Vector pos, Vector goal) {
		return pos.subtractFromFirst(pos, goal);
	}
	
	public Vector multiplyVector(Vector v, float factor) {
		return new Vector (v.x * factor, v.y * factor);
	}
	
	public Vector divideVector(Vector v, float factor) {
		return new Vector(v.x/factor, v.y/factor);
	}
	
	public Vector clipLength(Vector v, float min, float max) {
		if(v.vectorLength(v) < min) {
			return v.multiplyVector(v.normalize(v), min);
		}
		else if(v.vectorLength(v) > max) {
			return v.multiplyVector(v.normalize(v), max);
		}
		else return v;
	}
	
	public boolean isVectorAnObstacle(Path2D[] obstacles, Vector v) {
		for(int i = 0; i < obstacles.length; i++) {
			if(obstacles[i].contains(v.vectorToPoint(v))) {
				System.out.println("one of the points is in an obstacle");
				return true;
			}
		}
		return false;
	}
}
