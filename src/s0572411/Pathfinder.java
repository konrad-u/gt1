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
	
	Point[] pearlPoints = info.getScene().getPearl();
	Point[] visitedPearls = new Point[pearlPoints.length];
	Path2D[] obstacles = info.getScene().getObstacles();
	Point playerPos;
	float maxVel = info.getMaxVelocity();
	
	int res = 50;
	Map map = new Map(res, info);

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
		return Color.orange;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.black;
	}
	
	@Override
	public PlayerAction update() {
		playerPos = new Point((int)info.getX(), (int)info.getY());
		
		MapCell playerCell = map.PointToMapCell(res, playerPos);

		checkIfPlayerReachedPearl();
		
		if(playerCell.status == Status.pearl) {
			//pursue pearl
			return seekClosestPearl();
		}
		else {
			//pursue cell center of closest pearl marked cell
			Point closestPearlCellCenter = returnClosestPearlCellCenter();
			
			Point directionPoint = pointFromStartToGoal(playerPos, closestPearlCellCenter);
			
			float[] seekNormPoints = normalizePointToFloatArray(directionPoint);
			
			float directionToPearl = -(float)Math.atan2(seekNormPoints[1],seekNormPoints[0]);
			
			DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);
			
			return pearlPursuit;
		}
		
	}

	public Point returnClosestPearlCellCenter() {
		
		Point closestPearl = new Point();
		float closestPearlDistance = 100000;
		for (int i = 0; i < res; i++){
			for (int j = 0; j < res; j++) {
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
	
	public void checkIfPlayerReachedPearl() {
		for(int i = 0; i < pearlPoints.length; i++) {
			if(calculateDistance(pearlPoints[i], playerPos) < 5) {
				pearlPoints[i] = new Point(99999999, 999999999);
				return;
			}
		}
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
	
	public DivingAction seekClosestPearl() {
		
		Point closestPearl = getClosestPearl(pearlPoints, playerPos);
		
		Point directionPoint = pointFromStartToGoal(playerPos, closestPearl);
		
		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);
		
		float directionToPearl = -(float)Math.atan2(seekNormPoints[1],seekNormPoints[0]);
		
		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);
		
		return pearlPursuit;
	}
	
	//---------DrawDebugStuff------------------------
	
	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		gfx.setColor(new Color(255,255,255));
		for (int i = 0; i < res; i++){
			for (int j = 0; j < res; j++) {
				MapCell c = map.mapGrid[i][j];
				switch(c.status) {
				case free: 
					gfx.setColor(new Color (0,0,255,30));
					break;
				case obstacle: 
					gfx.setColor(new Color(255,0,0,30));
					break;
				case pearl: 
					gfx.setColor(new Color(0,255,0));
				}
				gfx.fillOval(c.center.x, c.center.y, 1,1);
				gfx.drawLine((int)c.minX, (int)c.minY, (int)c.minX, (int)c.maxY);
				gfx.drawLine((int)c.minX, (int)c.minY, (int)c.maxX, (int)c.minY);
				gfx.drawLine((int)c.maxX, (int)c.minY, (int)c.maxX, (int)c.maxY);
				gfx.drawLine((int)c.minX, (int)c.maxY, (int)c.maxX, (int)c.maxY);
			}
		}
	}
}
