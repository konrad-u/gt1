package s0572411;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;

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

	int res = 50;
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

		if (playerCell.status == Status.pearl) {
			return seekPearl();
		} else {
			return seekClosestPearlCellCenter();
		}

	}
	
	
	//-----------------A* Methods-----------
	
	ArrayList<MapCell> cellsInProcess = new ArrayList<MapCell>();
	
	public void InitializeAStar() {
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
	}
	
	public void calcNeighborDistances(MapCell currentCell) {
		ArrayList<MapCell> neighborCells = new ArrayList<MapCell>();
		//O
		if(currentCell.mapY -1 >=0 && !map.mapGrid[playerCell.mapX][playerCell.mapY-1].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX][playerCell.mapY-1]);
		}
		//OR
		if(currentCell.mapY -1 >=0 && currentCell.mapX+1 < wCells && !map.mapGrid[playerCell.mapX+1][playerCell.mapY-1].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX+1][playerCell.mapY-1]);
		}
		//R
		if(currentCell.mapX+1 < wCells && !map.mapGrid[playerCell.mapX+1][playerCell.mapY].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX+1][playerCell.mapY]);
		}
		//UR
		if(currentCell.mapY +1 < hCells && currentCell.mapX+1 < wCells && !map.mapGrid[playerCell.mapX+1][playerCell.mapY+1].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX+1][playerCell.mapY+1]);
		}
		//U
		if(currentCell.mapY +1 < hCells && !map.mapGrid[playerCell.mapX][playerCell.mapY+1].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX][playerCell.mapY+1]);
		}
		//UL
		if(currentCell.mapY +1 < hCells && currentCell.mapX-1 >= 0 && !map.mapGrid[playerCell.mapX-1][playerCell.mapY+1].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX-1][playerCell.mapY+1]);
		}
		//L
		if(currentCell.mapX-1 >= 0 && !map.mapGrid[playerCell.mapX-1][playerCell.mapY].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX-1][playerCell.mapY]);
		}
		//OL
		if(currentCell.mapY -1 >=0 && currentCell.mapX-1 >= 0 && !map.mapGrid[playerCell.mapX-1][playerCell.mapY-1].marked) {
			neighborCells.add(map.mapGrid[playerCell.mapX-1][playerCell.mapY-1]);
		}
		for(MapCell mapCell : neighborCells) {
			float distance = calculateDistance(playerPos, mapCell.center);
			if(mapCell.graphCost > distance) {
				mapCell.graphCost = distance;
			}
		}
		currentCell.marked = true;
		cellsInProcess.remove(currentCell);
		cellsInProcess.addAll(neighborCells);
		
		MapCell smallestDistCell = null;
		
		for(MapCell mapCell : cellsInProcess) {
			if(smallestDistCell == null || mapCell.graphCost + mapCell.airCost < smallestDistCell.graphCost + smallestDistCell.airCost) {
				smallestDistCell = mapCell;
			}
		}
		if(smallestDistCell != null) {
			calcNeighborDistances(smallestDistCell);
		}
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

	public DivingAction DijkstraToClosestPearlCellCenter() {

		return null;
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
		gfx.setColor(new Color(255, 255, 255));
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
	}
}
