package s0572411;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;
import s0572411.MapCell.Status;

public class Pathfinder extends AI {
	
	/*
	 * 11.05 problem is can not draw in map, and can not see if player is actually steering to closest cell holding a pearl
	 * I suspect that the status setting for each cell has some error...
	 * 
	 */
	
	Point[] pearlPoints = info.getScene().getPearl();
	Point[] visitedPearls = new Point[pearlPoints.length];
	Path2D[] obstacles = info.getScene().getObstacles();
	Point playerPos;
	float maxVel = info.getMaxVelocity();
	
	int res = 100;
	Map map = new Map(res, info);
	
	//Graphics g;

	public Pathfinder(Info info) {
		super(info); 
		enlistForTournament(572411);
	}

	@Override
	public String getName() {
		return "Pathfinder";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.red;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.green;
	}
	
	@Override
	public PlayerAction update() {
		playerPos = new Point((int)info.getX(), (int)info.getY());
		
		//drawPointCenters(res, g, map);

		Point closestPearl = returnClosestPearl();
		
		Point directionPoint = pointFromStartToGoal(playerPos, closestPearl);
		
		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);
		
		float directionToPearl = -(float)Math.atan2(seekNormPoints[1],seekNormPoints[0]);
		
		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);
		
		return pearlPursuit;
	}
	
	public void drawPointCenters(int res, Graphics g, Map map) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(255,255,255));
		for (int i = 0; i < 10; i++){
			for (int j = 0; j < 10; j++) {
				g2d.fillOval(map.mapGrid[i][j].center.x, map.mapGrid[i][j].center.y, 10,10);
				drawDebugStuff(g2d);
			}
		}
	}

	public Point returnClosestPearl() {
		
		Point closestPearl = new Point();
		float closestPearlDistance = 100000;
		for (int i = 0; i < 10; i++){
			for (int j = 0; j < 10; j++) {
				MapCell c = map.mapGrid[i][j];
				if(c.status == Status.pearl) {
					float currentPearlDistance = calculateDistance(c.center, playerPos);
					if(currentPearlDistance < closestPearlDistance) {
						closestPearl = c.center;
						closestPearlDistance = currentPearlDistance;
					}
				}
			}
		}
		return closestPearl;
	}
	
	//---------------------------W1 methods----------------------
	
	public float calculateDistance(Point start, Point goal) {
		Point distancePoint = pointFromStartToGoal(start, goal);
		float distance = (float)Math.sqrt(distancePoint.x*distancePoint.x + distancePoint.y*distancePoint.y);
		return distance;
	}
	
	public Point pointFromStartToGoal(Point start, Point goal) {
		Point directionPoint = new Point ((goal.x - start.x),(goal.y - start.y));
		return directionPoint;
	}
	
	public float[] normalizePointToFloatArray(Point point) {
		float normPoint[] = new float[2];
		float pointVectorLength = calculateDistance(playerPos, point);
		normPoint[0] = (point.x/pointVectorLength);
		normPoint[1] = (point.y/pointVectorLength);
		return normPoint;
	}
	
}
