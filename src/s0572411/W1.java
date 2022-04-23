package s0572411;

import java.awt.Color;
import java.awt.Point;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class W1 extends AI {

	public W1(Info info) {
		super(info);
		enlistForTournament(572411);
	}

	@Override
	public String getName() {
		return "AI Taucher";
		
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
		//return new DivingAction(1.0f,3.14f);
		return persueClosestPearl();
	}
	
	public DivingAction persueClosestPearl() {
		Point[] pearlPoints = info.getScene().getPearl();
		Point playerPos = new Point((int)info.getX(), (int)info.getY());
		
		Point closestPearl = closestPoint(pearlPoints, playerPos);
		
		Point directionPoint = fromStartToGoal(playerPos, closestPearl);
		
		float directionToPearl = (float)Math.atan2(directionPoint.y,directionPoint.x);
		
		//insert 2 methods; one for calculating flee Diving action, 
		//one for balancing this against the seek diving action
		
		return new DivingAction(1.0f, -directionToPearl);
	}
	
	public Point addPoints(Point point1, Point point2) {
		Point summedPoint = new Point ((point1.x + point2.x), (point1.y + point2.y));
		return summedPoint;
	}
	
	public Point fromStartToGoal(Point start, Point goal) {
		Point directionPoint = new Point ((goal.x - start.x),(goal.y - start.y));
		return directionPoint;
	}
	
	public float calculateDistance(Point start, Point goal) {
		float distance = 0;
		
		Point distancePoint = fromStartToGoal(start, goal);
		
		distance = (float)Math.sqrt(distancePoint.x*distancePoint.x + distancePoint.y*distancePoint.y);
		
		return distance;
	}

	public Point closestPoint (Point[] pearls, Point player) {
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
	
	public void printSceneInfo() {
		System.out.print("thing");
		System.out.print(info);
	}
}
