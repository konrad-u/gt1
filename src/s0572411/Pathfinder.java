package s0572411;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;

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
	MapCell playerCell;
	float maxVel = info.getMaxVelocity();

	int res = 40;
	int w = info.getScene().getWidth();
	int h = info.getScene().getHeight();

	int wCells = w / res;
	int hCells = h / res;

	Map map = new Map(wCells, hCells, info, res);

	public Pathfinder(Info info) {
		super(info);
		enlistForTournament(572411);
		playerPos = new Point((int) info.getX(), (int) info.getY());
		playerCell = map.PointToMapCell(wCells, hCells, playerPos);
		System.out.println(wCells * hCells);
		InitializeAStar();
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
		playerPos = new Point((int) info.getX(), (int) info.getY());
		playerCell = map.PointToMapCell(wCells, hCells, playerPos);

		checkIfPlayerReachedPearl();
		
		if(pathToGoal.size() > 0 && playerCell == pathToGoal.get(0)) {
			pathToGoal.remove(0);
		}

		if (playerCell.status == Status.pearl) {
			return seekPearl();
		} else {
			//return seekClosestPearlCellCenter();
			return MoveToCell(pathToGoal.get(0));
		}

	}
	
	
	//-----------------A* Methods-----------
	
	int debugCount = 0;
	
	 ArrayList<MapCell> cellsInProcess;
	
	ArrayList<MapCell> pathToGoal;
	
	public void InitializeAStar() {
		cellsInProcess = new ArrayList<MapCell>();
		pathToGoal  = new ArrayList<MapCell>();
		for (int i = 0; i < wCells; i++) {
			for (int j = 0; j < hCells; j++) {
				map.mapGrid[i][j].graphCost = Float.MAX_VALUE;
				map.mapGrid[i][j].airCost = calculateDistance(playerPos, returnClosestPearlCellCenter());
				map.mapGrid[i][j].marked = false;
				}
			}
		MapCell currentCell = playerCell;
		currentCell.graphCost = 0;
		cellsInProcess.add(currentCell);
		calcNeighborDistances(currentCell);
		System.out.println(debugCount);
		Collections.reverse(pathToGoal);
		OptimizePath();
	}
	
	public void calcNeighborDistances(MapCell currentCell) {
		debugCount++;
		ArrayList<MapCell> unmarkedNeighbors = new ArrayList<MapCell>();
		
		for(MapCell mapCell : currentCell.neighborCells) {
			if(!mapCell.marked && mapCell.status != Status.obstacle) {
				float distance = calculateDistance(playerPos, mapCell.center);
				if(mapCell.graphCost > distance) {
					mapCell.graphCost = distance;
				}
				unmarkedNeighbors.add(mapCell);
				}
		}
		currentCell.marked = true;
		cellsInProcess.remove(currentCell);
		for(MapCell cell : unmarkedNeighbors) {
			if(!cellsInProcess.contains(cell)) {
				cellsInProcess.add(cell);
			}
		}
		
		MapCell smallestDistCell = null;
		
		for(MapCell mapCell : cellsInProcess) {
			if(mapCell.status == Status.pearl) {
				returnPath(mapCell);
				smallestDistCell = null;
				break;
			}
			if(smallestDistCell == null || mapCell.graphCost + mapCell.airCost < smallestDistCell.graphCost + smallestDistCell.airCost) {
				smallestDistCell = mapCell;
			}
		}
		if(smallestDistCell != null) {
			calcNeighborDistances(smallestDistCell);
		}
	}
	
	public void returnPath(MapCell currentCell){
		pathToGoal.add(currentCell);
		MapCell nextCell = null;
		for(MapCell cell : currentCell.neighborCells) {
			if(nextCell == null || cell.graphCost< nextCell.graphCost) {
				nextCell = cell;
			}
		}
		if(nextCell != playerCell) {
			//StackOverflow here when path is too long?
			returnPath(nextCell);
		}
	}
	
	public DivingAction MoveToCell(MapCell cell) {

		Point directionPoint = pointFromStartToGoal(playerPos, cell.center);

		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);

		float directionToPearl = -(float) Math.atan2(seekNormPoints[1], seekNormPoints[0]);

		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);

		return pearlPursuit;
	}

	// ----------------Pathfinder Methods----------------

	public Point returnClosestPearlCellCenter() {

		Point closestPearl = new Point();
		float closestPearlDistance = 100000;
		for (int i = 0; i < wCells; i++) {
			for (int j = 0; j < hCells; j++) {
				MapCell c = map.mapGrid[i][j];
				if (c.status == Status.pearl) {
					float currentPearlDistance = calculateDistance(c.center, playerPos);
					if (currentPearlDistance < closestPearlDistance) {
						closestPearl = c.center;
						closestPearlDistance = currentPearlDistance;
					}
				}
			}
		}
		return closestPearl;
	}

	public DivingAction seekClosestPearlCellCenter() {

		Point closestPearlCellCenter = returnClosestPearlCellCenter();

		Point directionPoint = pointFromStartToGoal(playerPos, closestPearlCellCenter);

		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);

		float directionToPearl = -(float) Math.atan2(seekNormPoints[1], seekNormPoints[0]);

		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);

		return pearlPursuit;
	}


	public void OptimizePath() {
		int startIndex = 0;
		int endIndex = 2;
		if(pathToGoal.size() >2) {
			while(startIndex <= pathToGoal.size() - 3) {
				// endIndexOutOfBounds error
				System.out.println("indexes" +  startIndex +  "," +  endIndex + ", " + "pathToGoal size"+ pathToGoal.size());
				boolean optimizeDone = false;
				while(!optimizeDone && pathToGoal.size() > 2) {
					
					MapCell start = pathToGoal.get(startIndex);
					MapCell goal = pathToGoal.get(endIndex);
					
					Point vector = pointFromStartToGoal(start.center, goal.center);

					ArrayList <MapCell> cellsInPath = new ArrayList<MapCell>();
					findCellsInLineOfSight(start, goal, cellsInPath);
					boolean lineOfSightIsClear = true;
					for(MapCell cell : cellsInPath) {
						if(cell.status == Status.obstacle) {
							lineOfSightIsClear = false;
							break;
						}
					}
					if(lineOfSightIsClear) {
						pathToGoal.remove(startIndex+1);
					} else {
						optimizeDone = true;
					}
				}
				startIndex++;
				endIndex++;
			}
		}
		
	}
	
	public void findCellsInLineOfSight(MapCell start, MapCell goal, ArrayList<MapCell> cellsInPath) {
		for(MapCell cell : start.neighborCells) {
			if(cell == goal) {
				break;
			}
			if(lineRect(start.center.x, start.center.y, goal.center.x, goal.center.y, cell.tl.x, cell.tl.y, res,res )) {
				cellsInPath.add(cell);
				findCellsInLineOfSight(cell, goal, cellsInPath);
				break;
			}
		}
	}
	
	//source for two following methods: http://www.jeffreythompson.org/collision-detection/line-rect.php
	// LINE/RECTANGLE
	boolean lineRect(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) {

	  // check if the line has hit any of the rectangle's sides
	  // uses the Line/Line function below
	  boolean left =   lineLine(x1,y1,x2,y2, rx,ry,rx, ry+rh);
	  boolean right =  lineLine(x1,y1,x2,y2, rx+rw,ry, rx+rw,ry+rh);
	  boolean top =    lineLine(x1,y1,x2,y2, rx,ry, rx+rw,ry);
	  boolean bottom = lineLine(x1,y1,x2,y2, rx,ry+rh, rx+rw,ry+rh);

	  // if ANY of the above are true, the line
	  // has hit the rectangle
	  if (left || right || top || bottom) {
	    return true;
	  }
	  return false;
	}


	// LINE/LINE
	boolean lineLine(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {

	  // calculate the direction of the lines
	  float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
	  float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));

	  // if uA and uB are between 0-1, lines are colliding
	  if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {

	    // optionally, draw a circle where the lines meet
	    //float intersectionX = x1 + (uA * (x2-x1));
	    //float intersectionY = y1 + (uA * (y2-y1));

	    return true;
	  }
	  return false;
	}
	
	// ---------------------------W1 methods----------------------

	public float calculateDistance(Point start, Point goal) {
		Point distancePoint = pointFromStartToGoal(start, goal);
		float distance = (float) Math.sqrt(distancePoint.x * distancePoint.x + distancePoint.y * distancePoint.y);
		return distance;
	}

	public Point pointFromStartToGoal(Point start, Point goal) {
		Point directionPoint = new Point((goal.x - start.x), (goal.y - start.y));
		return directionPoint;
	}

	public float[] normalizePointToFloatArray(Point point) {
		float normPoint[] = new float[2];
		float pointVectorLength = calculateDistance(playerPos, point);
		normPoint[0] = (point.x / pointVectorLength);
		normPoint[1] = (point.y / pointVectorLength);
		return normPoint;
	}

	public void checkIfPlayerReachedPearl() {
		for (int i = 0; i < pearlPoints.length; i++) {
			if (calculateDistance(pearlPoints[i], playerPos) < 5) {
				map.PointToMapCell(wCells, hCells, pearlPoints[i]).status = Status.collected;
				pearlPoints[i] = new Point(99999999, 999999999);
				//new A* 
				InitializeAStar();
				return;
			}
		}
	}

	public Point getClosestPearl(Point[] pearls, Point player) {

		Point closestPearl = new Point();
		float closestPearlDistance = 100000;
		for (int i = 0; i < pearls.length; i++) {
			float currentPearlDistance = calculateDistance(pearls[i], player);
			if (currentPearlDistance < closestPearlDistance) {
				closestPearl = pearls[i];
				closestPearlDistance = currentPearlDistance;
			}
		}
		return closestPearl;
	}

	public DivingAction seekPearl() {

		Point closestPearl = getClosestPearl(pearlPoints, playerPos);

		Point directionPoint = pointFromStartToGoal(playerPos, closestPearl);

		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);

		float directionToPearl = -(float) Math.atan2(seekNormPoints[1], seekNormPoints[0]);

		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);

		return pearlPursuit;
	}

	// ---------DrawDebugStuff------------------------

	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		//gfx.setColor(new Color(255, 255, 255));
		for (int i = 0; i < wCells; i++) {
			for (int j = 0; j < hCells; j++) {
				MapCell c = map.mapGrid[i][j];
				switch (c.status) {
				case free:
					gfx.setColor(new Color(0, 0, 255, 30));
					break;
				case obstacle:
					gfx.setColor(new Color(255, 0, 0, 30));
					break;
				case pearl:
					gfx.setColor(new Color(0, 255, 0));
					break;
				case collected:
					gfx.setColor(new Color(0, 0, 0));
				}
				gfx.fillOval(c.center.x, c.center.y, 1, 1);
				gfx.drawLine((int) c.minX, (int) c.minY, (int) c.minX, (int) c.maxY);
				gfx.drawLine((int) c.minX, (int) c.minY, (int) c.maxX, (int) c.minY);
				gfx.drawLine((int) c.maxX, (int) c.minY, (int) c.maxX, (int) c.maxY);
				gfx.drawLine((int) c.minX, (int) c.maxY, (int) c.maxX, (int) c.maxY);
			}
		}
		for(int i = 1; i < pathToGoal.size(); i++) {
			gfx.setColor(new Color(255,255,255));
			gfx.drawLine(pathToGoal.get(i).center.x, pathToGoal.get(i).center.y, pathToGoal.get(i-1).center.x, pathToGoal.get(i-1).center.y);
		}
	}
	
	
}
