package s0572411;

import java.awt.Color;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;

import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.PlayerAction;
import s0572411.MapCell.Status;


//-----------simple test class for vector steer and flee
// goal is lowest point straight down for setting seek
// avoid obstacles with flee
//now working with acceleration instead of velocity


public class simpleSeekFlee extends AI {

	//---Info and Scene gets---
		Point[] pearlPoints = info.getScene().getPearl();
		Point[] visitedPearls = new Point[pearlPoints.length];
		Path2D[] obstacles = info.getScene().getObstacles();
		float maxVel = info.getMaxVelocity();
		int w = info.getScene().getWidth();
		int h = info.getScene().getHeight();
		float maxAir = info.getMaxAir();
		int nrPearlsCollected = 0;
		float maxAcc = info.getMaxAcceleration();
		
		//--Own variables: Map---
		int res = 40;
		int wCells = w / res;
		int hCells = h / res;
		Map map = new Map(wCells, hCells, info, res);
		
		//---Own variables: Player
		Vector playerPos = new Vector();
		MapCell playerCell;
		Vector pointAbove = new Vector();
		Vector pointBelow = new Vector();
		//above below used to be 20
		int surrPointDistance = 25;
		float surrPointsAngleFactor = 0.3f;
		float dirWeight = 0.6f;
		float fleeWeight = 1-dirWeight;
		float currAir;
		Point pTL, pTR, pBL, pBR;
		int pBox = res/2;
		
		ArrayList<Point> pointsToPearl;
	
		Vector closestPearl = new Vector();

		public int circleDiv = 10;
		public Vector[] circle = new Vector[circleDiv];
		public int circleRadius = 30;
		public Vector circleVectorSum = new Vector();
		public boolean circleInObstacle = false;
	
	public simpleSeekFlee(Info info) {
		super(info);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "Tomitaro Fujii";
	}

	@Override
	public Color getPrimaryColor() {
		return Color.white;
	}

	@Override
	public Color getSecondaryColor() {
		return Color.white;
	}

	@Override
	public PlayerAction update() {
		currAir = info.getAir() / maxAir;
		updatePlayerPos();
		updateAboveBelowPoints();
		updateCircle();
		closestPearl = new Vector(pearlPoints[4].x, pearlPoints[4].y);
		return steerToGoal();
	}

	public DivingAction steerToGoal() {
		
		Vector seekV;
		if(currAir > 0.5) {
		 seekV = playerPos.normalize(playerPos.seekVector(playerPos, closestPearl));
		 System.out.println("seekV magnitude is before: " + seekV.vectorLength(seekV));
		 System.out.println("maxAcc is " + maxAcc);
			seekV = seekV.clipLength(seekV, 0, maxAcc);
			 System.out.println("seekV magnitude is after: " + seekV.vectorLength(seekV));
		}
		else {
			 seekV = playerPos.normalize(playerPos.fleeVector(playerPos, closestPearl));
			 System.out.println("seekV magnitude is before: " + seekV.vectorLength(seekV));
				seekV = seekV.clipLength(seekV, 0, maxAcc);
			 System.out.println("seekV magnitude is after: " + seekV.vectorLength(seekV));
		}
		if(circleInObstacle) {
			Vector fleeV;
			System.out.println("---------circle in obstacle------");
			fleeV = playerPos.normalize(playerPos.fleeVector(playerPos, circleVectorSum));
			seekV = seekV.addVectors(seekV, fleeV);
		}
		
//		if(pointAbove.isVectorAnObstacle(obstacles, pointAbove)) {
//			System.out.println("pointAbove is an obstacle");
//			Vector fleeV = playerPos.normalize(playerPos.seekVector(pointBelow, pointAbove));
//			seekV = fleeV.clipLength(fleeV.addVectors(seekV, fleeV), 0, maxAcc);
//		}
//		if(pointBelow.isVectorAnObstacle(obstacles, pointBelow)) {
//			System.out.println("pointBelow is an obstacle");
//			Vector fleeV = playerPos.normalize(playerPos.seekVector(pointAbove, pointBelow));
//			seekV = fleeV.addVectors(seekV, fleeV);
//		}
		
		//-----------------------Using circle to find contact vectors-----------
		

		float dir = -(float) Math.atan2(seekV.y,  seekV.x);
		
		System.out.println("the current velocity is: " + info.getVelocity());
		return new DivingAction(maxAcc, dir);
	}

	public Vector returnClosestPearlCellCenter() {

		Vector closestPearl = new Vector();
		
		float closestPearlDistance = 100000;
		for (int i = 0; i < wCells; i++) {
			for (int j = 0; j < hCells; j++) {
				MapCell c = map.mapGrid[i][j];
				if (c.status == Status.pearl) {
					Vector cCenter = new Vector(c.center);
					float currentPearlDistance = cCenter.distanceBetweenVectors(cCenter, playerPos);
					if (currentPearlDistance < closestPearlDistance) {
						closestPearl = new Vector(c.center);
						closestPearlDistance = currentPearlDistance;
					}
				}
			}
		}
		return closestPearl;
	}

	public void updateAboveBelowPoints(){
		float orientation = info.getOrientation();
		
		double pointX = playerPos.x + (surrPointDistance * Math.cos(orientation + Math.PI/2 * surrPointsAngleFactor));
		double pointY = playerPos.y - (surrPointDistance * Math.sin(orientation + Math.PI/2 * surrPointsAngleFactor));
		pointAbove =  new Vector(pointX,pointY);		
		
		//pointY = playerPos.y + playerPos.y - (belowFactor * Math.sin(orientation));
		pointX = playerPos.x + (surrPointDistance * Math.cos(orientation - Math.PI/2 * surrPointsAngleFactor));
		pointY = playerPos.y - (surrPointDistance * Math.sin(orientation - Math.PI/2 * surrPointsAngleFactor));
		pointBelow =  new Vector(pointX,pointY);
	}
	public void updatePlayerPos() {
		playerPos.x = info.getX();
		playerPos.y = info.getY();
	}
	
	
	public void updateCircle() {
		circleVectorSum = new Vector(playerPos);
		int circleHits = 1;
		circleInObstacle = false;
		for(int i = 0; i < circle.length; i++) {
			//int angle = 360/circleDiv;
			double angle = Math.toRadians((double)i/circle.length) * (double) 360;
			circle[i] = new Vector ((Math.cos(angle)* circleRadius) + playerPos.x, (Math.sin(angle) * circleRadius) + playerPos.y);

			if(circle[i	] != null && circle[i].isVectorAnObstacle(obstacles, circle[i])) {
				circleInObstacle = true;
				circleVectorSum = circleVectorSum.addVectors(circle[i], circleVectorSum);
				circleHits++;
				System.out.println("-------------circleSumCoords are: " + circleVectorSum.x + " , " + circleVectorSum.y);
			}
		}
		circleVectorSum = circleVectorSum.divideVector(circleVectorSum, (float)circleHits);
	}
	
	public void drawDebugStuff(Graphics2D gfx) {
		gfx.drawLine((int)playerPos.x,  (int)playerPos.y,  (int)pearlPoints[4].x, (int)pearlPoints[4].y);
		
		gfx.setColor(new Color(0,255,0));
		//gfx.drawLine(pointAhead.x, pointAhead.y, playerPos.x, playerPos.y);
		gfx.drawOval((int)pointAbove.x,  (int)pointAbove.y,  10, 10);
		gfx.setColor(new Color(255,0,0));
		gfx.drawOval((int)pointBelow.x,  (int)pointBelow.y,  10, 10);
		
		for(int i = 0; i < circle.length; i++) {
			if(circle[i	] != null && circle[i].x > 0 && circle[i].y > 0) {
				gfx.setColor(new Color(0,255,255));
				if(circle[i].isVectorAnObstacle(obstacles, circle[i])) {
					gfx.setColor(new Color(255,0,0));
					gfx.drawLine((int)circle[i].x, (int)circle[i].y, (int)playerPos.x, (int)playerPos.y);
				}
				gfx.drawOval((int)circle[i].x, (int)circle[i].y, 5,5);
			}
		}
		gfx.setColor(new Color(255,255,255));
		gfx.drawOval((int)circleVectorSum.x, (int)circleVectorSum.y, 10, 10);
	}
}
