package s0572411;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class W1 extends AI {

	Point[] pearlPoints = info.getScene().getPearl();
	Point[] visitedPearls = new Point[pearlPoints.length];
	Path2D[] obstacles = info.getScene().getObstacles();
	float maxVel = info.getMaxVelocity();
	Point playerPos;
	int obsDistance = 5;
	int changeDivision = 10;
	

	public W1(Info info) {
		super(info);
		enlistForTournament(572411);
	}

	@Override
	public String getName() {
		return "W1";
		
	}

	@Override
	public Color getPrimaryColor() {
		return Color.black;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.white;
	}

	@Override
	public PlayerAction update() {
		playerPos = new Point((int)info.getX(), (int)info.getY());
		
		checkIfPlayerReachedPearl(); 
		
		//return new DivingAction(1.0f,3.14f);
		return seekClosestPearl();
	}
	
	//
	
	//own methods 
	
	public DivingAction seekClosestPearl() {
		
		Point closestPearl = getClosestPearl(pearlPoints, playerPos);
		
		Point directionPoint = pointFromStartToGoal(playerPos, closestPearl);
		
		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);
		
		float directionToPearl = -(float)Math.atan2(seekNormPoints[1],seekNormPoints[0]);
		
		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);
		
		Point obstacleAvoidance = getFleeFromObstacle();
		
		if(obstacleAvoidance != null) {
			//need new compensation function here
			
			//if object in way
			
			return null;
			
		}
		
		return pearlPursuit;
	}
	
	public float adjustAngle(float desiredAngle, float degreeOfAdjustment) {
		
		float adjustedAngle;
		
		float desiredAngleDegrees = (float) (desiredAngle*(180/Math.PI));
		if(desiredAngleDegrees < 0) {
			desiredAngleDegrees = desiredAngleDegrees + 360;
		}
		
		float currentAngle = info.getOrientation();
		
		float currentAngleDegrees = (float) (currentAngle*(180/Math.PI));
		if(currentAngleDegrees < 0) {
			currentAngleDegrees = currentAngleDegrees + 360;
		}
		
		if(currentAngleDegrees < desiredAngleDegrees) {
			adjustedAngle = (float) (currentAngleDegrees - (desiredAngleDegrees/degreeOfAdjustment*Math.PI/180));
			return adjustedAngle;
		}
		if(currentAngleDegrees > desiredAngleDegrees) {
			adjustedAngle = (float) (currentAngle + (desiredAngle/degreeOfAdjustment*Math.PI/180));
			return adjustedAngle;
		}
		return (Float) null;
	}
	
	public DivingAction averageTwoDAs(DivingAction d1, DivingAction d2) {
		
		float avgVel = ((d1.getPower()) + d2.getPower())/2;
		float avgDir = ((d1.getDirection()) + (d2.getDirection()));
		
		DivingAction averagedAction = new DivingAction(avgVel, avgDir);
		
		return averagedAction;
	}
	
	public Point pointFromStartToGoal(Point start, Point goal) {
		Point directionPoint = new Point ((goal.x - start.x),(goal.y - start.y));
		return directionPoint;
	}
	
	public float calculateDistance(Point start, Point goal) {
		Point distancePoint = pointFromStartToGoal(start, goal);
		float distance = (float)Math.sqrt(distancePoint.x*distancePoint.x + distancePoint.y*distancePoint.y);
		return distance;
	}
	
	public Point getClosestPearl (Point[] pearls, Point player) {

		Point closestPearl = new Point();
		float closestPearlDistance = 100000;
		for(int i = 0; i < pearls.length; i++) {
			float currentPearlDistance = calculateDistance(pearls[i], player);
			if(currentPearlDistance < closestPearlDistance) {
				closestPearl = pearls[i];
				closestPearlDistance = currentPearlDistance;
			}
		}
		return closestPearl;
	}
	
	public Point getFleeFromObstacle() {
		//this method should only target the point ahead of the diver; how do I get that?
		
		// new 45° approach; to each side of straight ahead, get the points +- 45°; if the center i.e. current
		// direction hits a target, check if either one doesn't. choose the way that is closer to the goal and is also 
		// not hitting anything else. 
		
	DivingAction currentDA = new DivingAction(info.getVelocity(), info.getOrientation());
	
	
		
		return null;
	}
	
	public Point[] getPointSurrounding(int distanceFromPoint, Point pointOfSurrounding) {
		int i = distanceFromPoint;
		Point[] playerSurrounding = new Point[8];
		playerSurrounding[0] = new Point (pointOfSurrounding.x+i, pointOfSurrounding.y);
		playerSurrounding[1] = new Point (pointOfSurrounding.x-i, pointOfSurrounding.y);
		playerSurrounding[2] = new Point (pointOfSurrounding.x+i, pointOfSurrounding.y+i);
		playerSurrounding[3] = new Point (pointOfSurrounding.x+i, pointOfSurrounding.y-i);
		playerSurrounding[4] = new Point (pointOfSurrounding.x-i, pointOfSurrounding.y+i);
		playerSurrounding[5] = new Point (pointOfSurrounding.x-i, pointOfSurrounding.y-i);
		playerSurrounding[6] = new Point (pointOfSurrounding.x, pointOfSurrounding.y+i);
		playerSurrounding[7] = new Point (pointOfSurrounding.x, pointOfSurrounding.y-i);
		return playerSurrounding;
	}

	public void checkIfPlayerReachedPearl() {
		for(int i = 0; i < pearlPoints.length; i++) {
			if(calculateDistance(pearlPoints[i], playerPos) < 5) {
				pearlPoints[i] = new Point(99999999, 999999999);
				return;
			}
		}
	}

	public float[] normalizePointToFloatArray(Point point) {
		float normPoint[] = new float[2];
		float pointVectorLength = calculateDistance(playerPos, point);
		normPoint[0] = (point.x/pointVectorLength);
		normPoint[1] = (point.y/pointVectorLength);
		return normPoint;
	}

	
}
