package s0572411;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;
import lenz.htw.ai4g.ai.ShoppingAction;
import lenz.htw.ai4g.ai.ShoppingItem;
import s0572411.MapCell.Status;

public class Pathfinder extends AI {

	// ---Info and Scene gets---
	Point[] pearlPoints = info.getScene().getPearl();
	Point[] visitedPearls = new Point[pearlPoints.length];
	Path2D[] obstacles = info.getScene().getObstacles();
	float maxVel = info.getMaxVelocity();
	int w = info.getScene().getWidth();
	int h = info.getScene().getHeight();
	float maxAir = info.getMaxAir();
	int nrPearlsCollected = 0;
	float maxAcc = info.getMaxAcceleration();
	float validProximityToPearl = 7;

	// --Own variables: Map---
	int res = 40;
	int wCells = w / res;
	int hCells = h / res;
	Map map = new Map(wCells, hCells, info, res);

	// ---Own variables: Player
	Point playerPos;
	MapCell playerCell;
	int surrPointDistance = 20;
	float surrPointsAngleFactor = 0.01f;
	float dirWeight = 0.7f;
	float fleeWeight = 1 - dirWeight;
	float currAir;
	int pBox = res / 2;

	ArrayList<Point> pointsToPearl;

	// --------------new vars from simpleSeekFlee class, for vector based steering
	// behavior

	public Vector playerVec = new Vector();
	// default circleDiv is 25
	public int circleDiv = 30;
	public Vector[] circle = new Vector[circleDiv];
	public int circleRadius = 25;
	public int circleContacts = 0;
	public Vector circleVectorSum = new Vector();
	public boolean circleInObstacle = false;
	// steerSmooth is weight of circle vectors; so far used 0.4f
	public float steerSmooth = 0.55f;

	// ----------new vars w4 and 5
	Point[] bottlePoints = info.getScene().getRecyclingProducts();
	Point shop = new Point(info.getScene().getShopPosition(), 0);
	int bottlesCollected = 0;
	int bottlesNeeded = 6;
	int itemsPurchased = 0;

	Vector seekV;
	Vector fleeV;
	Vector currentDirectionVec;

	// ---Enum for Optimize---
	enum OptStrat {
		shortenPath, straightToPearl
	}

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
		return "Tomitaro Fujii";
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

		updateEverything();

		if (currAir < 0.55 && nrPearlsCollected < 9) {
			Point nextPearlAir = new Point(playerPos.x, 0);
			currentDirectionVec = new Vector(nextPearlAir.x, nextPearlAir.y);
			DivingAction da = MoveToCell(map.PointToMapCell(wCells, hCells, nextPearlAir));
			return avoidObstacles(da);
		} else if (bottlesCollected < bottlesNeeded || itemsPurchased < bottlesNeeded / 2) {
			if (bottlesCollected < bottlesNeeded) {
				Point closestBottle = getClosestPearlOrBottle(bottlePoints, playerPos);
				currentDirectionVec = new Vector(closestBottle.x, closestBottle.y);
				DivingAction da = currentDirectionVec.vectorToDivingAction(playerVec, currentDirectionVec, maxAcc);
				return avoidObstacles(da);
				// find closestBottle
				// pursue closestBottle via avoidObstacles(da)
				// checking air?

			} else if (itemsPurchased < bottlesNeeded / 2) {
				// go to shop
				// check if in sufficient proximity to shop, i.e. validPearlDistance
				// purchase itemsPurchased
				// increment itemsPurchased
				if (calculateDistance(shop, playerPos) < validProximityToPearl) {
					// do shopping
					return buyItem(itemsPurchased);
				} else {
					currentDirectionVec = new Vector(shop.x, shop.y);
					DivingAction da = currentDirectionVec.vectorToDivingAction(playerVec, currentDirectionVec, maxAcc);
					return avoidObstacles(da);
				}
			}
		} else {
			// do everything else, i.e. pearl pursuit
		}

		if (pathToGoal.size() > 0 && playerCell == pathToGoal.get(0)) {
			pathToGoal.remove(0);
		}

		// removing isCleartoclosesttpear to see if we can get a decent steering without
		// shortcuts
		if (playerCell.status == Status.pearl || isClearToClosestPearl()) {
			// if (playerCell.status == Status.pearl) {
			DivingAction da = seekPearl();
			return avoidObstacles(da);
		} else {
			// return seekClosestPearlCellCenter();
			if (pathToGoal.isEmpty()) {
				DivingAction da = seekPearl();
				return avoidObstacles(da);
			}
			DivingAction da = MoveToCell(pathToGoal.get(0));
			currentDirectionVec = new Vector(pathToGoal.get(0).center.x, pathToGoal.get(0).center.y);
			return avoidObstacles(da);
		}

	}

	// --------------w4, collecting bottles and shopping
	// method returns all methods from update
	public void updateEverything() {
		currAir = info.getAir() / maxAir;

		if (currAir < 0.55) {
			System.out.println(currAir * 100 + "% air left.");
		}

		playerPos = new Point((int) info.getX(), (int) info.getY());
		playerCell = map.PointToMapCell(wCells, hCells, playerPos);

		checkIfPlayerReachedPearl();

		updatePlayerCellStatus();

		// ----------new methods from simple steer class

		updateCircle();
		updatePlayerVec();

		updateCurrentDirection();

		// System.out.println("The current angle is " +
		// info.getOrientation()*180/Math.PI);
		steerSmooth = (float) (circleContacts + 1) / (circleDiv);

		// ---------------w4 updates--------
		checkIfPlayerReachedBottle();
	}

	// same as pearls collected, need to iterate trash, find closest, change
	// location once collected and update count
	// find closest = refactored method returnClosestPearlOrBottle(Point[]
	// pearlsOrBottles, playerPos);

	public void checkIfPlayerReachedBottle() {
		for (int i = 0; i < bottlePoints.length; i++) {
			if (calculateDistance(bottlePoints[i], playerPos) <= validProximityToPearl + 1) {
				bottlePoints[i] = new Point(99999999, 999999999);
				bottlesCollected++;
				return;
			}
		}
	}

	public PlayerAction buyItem(int itemNr) {
		ShoppingAction sa;
		switch (itemNr) {
		case 0:
			sa = new ShoppingAction(ShoppingItem.BALLOON_SET);
			itemsPurchased++;
			maxAir = maxAir * 2;
			System.out.println("Purchased item " + sa.getProductToBuy());
			return sa;
		case 1:
			sa = new ShoppingAction(ShoppingItem.STREAMLINED_WIG);
			itemsPurchased++;
			System.out.println("Purchased item " + sa.getProductToBuy());
			return sa;
		case 2:
			sa = new ShoppingAction(ShoppingItem.MOTORIZED_FLIPPERS);
			itemsPurchased++;
			System.out.println("Purchased item " + sa.getProductToBuy());
			return sa;
		case 3:
			sa = new ShoppingAction(ShoppingItem.CORNER_CUTTER);
			itemsPurchased++;
			System.out.println("Purchased item " + sa.getProductToBuy());
			return sa;
		}
		return null;
	}

	// ---------------------methods from simpleSeekFlee class for vector based
	// steering behavior
	public void updateCircle() {
		circleVectorSum = new Vector(playerPos.x, playerPos.y);
		int circleHits = 1;
		circleInObstacle = false;
		for (int i = 0; i < circle.length; i++) {
			// int angle = 360/circleDiv;
			double angle = Math.toRadians((double) i / circle.length) * (double) 360;
			circle[i] = new Vector((Math.cos(angle) * circleRadius) + playerPos.x,
					(Math.sin(angle) * circleRadius) + playerPos.y);

			if (circle[i] != null && circle[i].isVectorAnObstacle(obstacles, circle[i])) {
				circleInObstacle = true;
				circleVectorSum = circleVectorSum.addVectors(circle[i], circleVectorSum);
				circleHits++;
				System.out
						.println("-------------circleSumCoords are: " + circleVectorSum.x + " , " + circleVectorSum.y);
			}
		}
		circleContacts = circleHits - 1;
		circleVectorSum = circleVectorSum.divideVector(circleVectorSum, (float) circleHits);
	}

	public void updatePlayerVec() {
		playerVec.x = info.getX();
		playerVec.y = info.getY();
	}

	public void updateCurrentDirection() {
		float orientation = info.getOrientation();

		double pointX = playerPos.x + (surrPointDistance * Math.cos(orientation + Math.PI / 2));
		double pointY = playerPos.y - (surrPointDistance * Math.sin(orientation + Math.PI / 2));
		currentDirectionVec = new Vector(pointX, pointY);
	}
	// ----------------W3 Methods-----------

	public DivingAction avoidObstacles(DivingAction currentAction) {

		if (playerCell.status != Status.pearl) {
			if (currAir > 0.55) {
				seekV = playerVec.normalize(playerVec.seekVector(playerVec, currentDirectionVec));
				seekV = seekV.clipLength(seekV, -maxAcc, maxAcc);
			} else {
				seekV = new Vector(playerVec.x, 0);
				// Why must i subtract here???
				seekV = seekV.subtractFromFirst(seekV, playerVec);
				seekV = seekV.normalize(seekV);
				seekV = seekV.clipLength(seekV, -maxAcc, maxAcc);

			}
			if (circleInObstacle) {
				fleeV = new Vector();
				fleeV = fleeV.fleeVector(playerVec, circleVectorSum);
				fleeV = fleeV.normalize(fleeV);
				fleeV = fleeV.clipLength(fleeV, -maxAcc, maxAcc);
				// trying a flip here
				// fleeV = fleeV.multiplyVector(fleeV, -1);
				seekV = seekV.addVectors(fleeV.multiplyVector(fleeV, steerSmooth),
						seekV.multiplyVector(seekV, 1 - steerSmooth));
			}
			float dir = -(float) Math.atan2(seekV.y, seekV.x);
			return new DivingAction(seekV.vectorLength(seekV), dir);
		} else {
			return currentAction;
		}
	}

	public Point getObsContactPoint(Point surrPoint) {
		// line function taken from
		// https://stackoverflow.com/questions/37100841/draw-line-function

		// first for pointAbove
		double slope = (double) (surrPoint.y - playerPos.y) / (surrPoint.x - playerPos.x);
		// adjustable resolution factor
		double resolution = 1;
		double x = playerPos.x;
		if (x <= surrPoint.x) {
			while (x <= surrPoint.x) {
				double y = slope * (x - playerPos.x) + playerPos.y;
				Point collPoint = new Point((int) x, (int) y);
				// if(isPointAnObstacle(linePoint)) {
				// if(map.PointToMapCell(wCells, hCells, linePoint).status == Status.obstacle) {
				for (int i = 0; i < obstacles.length; i++) {
					if (obstacles[i].contains(collPoint)) {
						// System.out.println("NOT CLEAR");
						return collPoint;
					}
				}
				x += resolution;
			}
		}

		else if (x >= surrPoint.x) {
			while (x >= surrPoint.x) {
				double y = slope * (x - playerPos.x) + playerPos.y;
				Point collPoint = new Point((int) x, (int) y);
				// if(map.PointToMapCell(wCells, hCells, linePoint).status == Status.obstacle) {
				for (int i = 0; i < obstacles.length; i++) {
					if (obstacles[i].contains(collPoint)) {
						// System.out.println("NOT CLEAR");
						return collPoint;
					}
				}
				x -= resolution;
			}
		}
		return null;
	}

	public boolean isClearToClosestPearl() {
		Point closestPearl = getClosestPearlOrBottle(pearlPoints, playerPos);
		// line function taken from
		// https://stackoverflow.com/questions/37100841/draw-line-function
		double slope = (double) (closestPearl.y - playerPos.y) / (closestPearl.x - playerPos.x);
		// adjustable resolution factor
		double resolution = 1;
		double x = playerPos.x;
		if (x <= closestPearl.x) {
			while (x <= closestPearl.x) {
				double y = slope * (x - playerPos.x) + playerPos.y;
				Point linePoint = new Point((int) x, (int) y);
				// if(isPointAnObstacle(linePoint)) {
				// if(map.PointToMapCell(wCells, hCells, linePoint).status == Status.obstacle) {

				// if(obstacles[i].contains(linePoint)) {
				if (isPointAnObstacle(linePoint)) {
					// System.out.println("NOT CLEAR");
					return false;
				}
				for (int i = 0; i < obstacles.length; i++) {
				}
				x += resolution;
			}
		}

		// else if(x >= returnClosestPearlCellCenter().x) {
		else {
			while (x >= closestPearl.x) {
				double y = slope * (x - playerPos.x) + playerPos.y;
				Point linePoint = new Point((int) x, (int) y);
				// if(map.PointToMapCell(wCells, hCells, linePoint).status == Status.obstacle) {
				if (isPointAnObstacle(linePoint)) {
					// System.out.println("NOT CLEAR");
					return false;
				}
				x -= resolution;
			}
		}
		// create condition for straight down situation which force checks along a line
		if (Math.abs(playerPos.x - closestPearl.x) < 5) {
			// float absDist = calculateDistance(playerPos, closestPearl);
			for (int i = playerPos.y; i < closestPearl.y; i += 10) {
				Point p = new Point(playerPos.x, i);
				if (isPointAnObstacle(p)) {
					return false;
				}
			}
		}
		// System.out.println("CLEAR");
		return true;
	}

	public void updatePlayerCellStatus() {
		playerCell.status = map.getMapCellStatusViaPoint(wCells, hCells, playerPos);
	}

	// -----------------A* Methods-----------

	int debugCount = 0;

	ArrayList<MapCell> cellsInProcess;

	ArrayList<MapCell> pathToGoal;

	// graphCost is player to cell, airCost is cell to closest pearl

	public void InitializeAStar() {
		cellsInProcess = new ArrayList<MapCell>();
		pathToGoal = new ArrayList<MapCell>();
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

		// System.out.println(debugCount);
		Collections.reverse(pathToGoal);
		OptimizePath();
	}

	public void calcNeighborDistances(MapCell currentCell) {
		debugCount++;
		ArrayList<MapCell> unmarkedNeighbors = new ArrayList<MapCell>();

		for (MapCell mapCell : currentCell.neighborCells) {
			if (!mapCell.marked && mapCell.status != Status.obstacle) {
				// float distance = calculateDistance(playerPos, mapCell.center);
				float distance = calculateDistance(currentCell.center, mapCell.center) + currentCell.graphCost;
				if (mapCell.graphCost > distance) {
					mapCell.graphCost = distance;
				}
				unmarkedNeighbors.add(mapCell);
			}
		}
		currentCell.marked = true;
		cellsInProcess.remove(currentCell);
		for (MapCell cell : unmarkedNeighbors) {
			if (!cellsInProcess.contains(cell)) {
				cellsInProcess.add(cell);
			}
		}

		MapCell smallestDistCell = null;

		for (MapCell mapCell : cellsInProcess) {
			if (mapCell.status == Status.pearl) {

				// print cost grid

				for (int i = 0; i < hCells; i++) {
					System.out.println(" ");
					for (int j = 0; j < wCells; j++) {
						if (map.mapGrid[j][i].status == Status.pearl) {
							System.out.print("X");
						}
						if (map.mapGrid[j][i].graphCost == Float.MAX_VALUE) {
							System.out.print(" -1 ");
						} else {
							if ((int) map.mapGrid[j][i].graphCost < 10) {
								System.out.print(" " + (int) map.mapGrid[j][i].graphCost + "  ");
							} else if ((int) map.mapGrid[j][i].graphCost < 100) {
								System.out.print(" " + (int) map.mapGrid[j][i].graphCost + " ");
							} else {
								System.out.print((int) map.mapGrid[j][i].graphCost + " ");
							}
						}
					}
				}
				System.out.println(" --------------------------------------------------- ");

				returnPath(mapCell);
				smallestDistCell = null;
				break;
			}
			if (smallestDistCell == null
					|| mapCell.graphCost + mapCell.airCost < smallestDistCell.graphCost + smallestDistCell.airCost) {
				smallestDistCell = mapCell;
			}
		}
		if (smallestDistCell != null) {
			calcNeighborDistances(smallestDistCell);
		}
	}

	public void returnPath(MapCell currentCell) {
		// System.out.println(" current cell graph cost: " + currentCell.graphCost);
		pathToGoal.add(currentCell);
		MapCell nextCell = null;
		for (MapCell cell : currentCell.neighborCells) {
			if (nextCell == null || cell.graphCost < nextCell.graphCost) {
				nextCell = cell;
			}
		}
		if (nextCell != playerCell) {
			// StackOverflow here when path is too long?
			returnPath(nextCell);
		}
	}

	public DivingAction MoveToCell(MapCell cell) {

		Point directionPoint = pointFromStartToGoal(playerPos, cell.center);

		// float[] normDir = normalizePointToFloatArray(directionPoint);

		float directionToCellCenter = -(float) Math.atan2(directionPoint.y, directionPoint.x);

		DivingAction pearlPursuit;

		pearlPursuit = new DivingAction(maxVel, directionToCellCenter);

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
		if (pathToGoal.size() > 2) {
			while (startIndex <= pathToGoal.size() - 3) {
				// endIndexOutOfBounds error
				System.out.println(
						"indexes" + startIndex + "," + endIndex + ", " + "pathToGoal size" + pathToGoal.size());
				boolean optimizeDone = false;
				while (!optimizeDone && pathToGoal.size() > 2 && endIndex < pathToGoal.size()) {

					MapCell start = pathToGoal.get(startIndex);
					MapCell goal = pathToGoal.get(endIndex);

					// Point vector = pointFromStartToGoal(start.center, goal.center);

					ArrayList<MapCell> cellsInPath = new ArrayList<MapCell>();
					findCellsInLineOfSight(start, goal, cellsInPath);
					boolean lineOfSightIsClear = true;
					for (MapCell cell : cellsInPath) {
						if (cell.status == Status.obstacle) {
							lineOfSightIsClear = false;
							break;
						}
					}
					if (lineOfSightIsClear) {
						pathToGoal.remove(startIndex + 1);
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
		for (MapCell cell : start.neighborCells) {
			if (cell == goal) {
				break;
			}
			if (lineRect(start.center.x, start.center.y, goal.center.x, goal.center.y, cell.tl.x, cell.tl.y, res,
					res)) {
				cellsInPath.add(cell);
				findCellsInLineOfSight(cell, goal, cellsInPath);
				break;
			}
		}
	}

	// source for two following methods lineRect and lineLine:
	// http://www.jeffreythompson.org/collision-detection/line-rect.php
	// LINE/RECTANGLE
	boolean lineRect(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) {

		// check if the line has hit any of the rectangle's sides
		// uses the Line/Line function below
		boolean left = lineLine(x1, y1, x2, y2, rx, ry, rx, ry + rh);
		boolean right = lineLine(x1, y1, x2, y2, rx + rw, ry, rx + rw, ry + rh);
		boolean top = lineLine(x1, y1, x2, y2, rx, ry, rx + rw, ry);
		boolean bottom = lineLine(x1, y1, x2, y2, rx, ry + rh, rx + rw, ry + rh);

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
		float uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
		float uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

		// if uA and uB are between 0-1, lines are colliding
		if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {

			// optionally, draw a circle where the lines meet
			// float intersectionX = x1 + (uA * (x2-x1));
			// float intersectionY = y1 + (uA * (y2-y1));

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

		normPoint[0] = (point.x / (float) Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2)));
		normPoint[1] = (point.y / (float) Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2)));
		return normPoint;
	}

	public float[] normalizeVectorToFloatArray(float[] vector) {
		float normPoint[] = new float[2];

		normPoint[0] = (vector[0] / (float) Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2)));
		normPoint[1] = (vector[1] / (float) Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2)));
		return normPoint;
	}

	public void checkIfPlayerReachedPearl() {
		for (int i = 0; i < pearlPoints.length; i++) {
			if (calculateDistance(pearlPoints[i], playerPos) <= validProximityToPearl + 2) {
				map.PointToMapCell(wCells, hCells, pearlPoints[i]).status = Status.obstacle;
				pearlPoints[i] = new Point(99999999, 999999999);
				nrPearlsCollected++;
				// new A*
				InitializeAStar();
				return;
			}
		}
	}

	public Point getClosestPearlOrBottle(Point[] pearlsOrBottles, Point player) {

		Point closestPearlOrBottle = new Point();
		float closestPearlDistance = 100000;
		for (int i = 0; i < pearlsOrBottles.length; i++) {
			float currentPearlDistance = calculateDistance(pearlsOrBottles[i], player);
			if (currentPearlDistance < closestPearlDistance) {
				closestPearlOrBottle = pearlsOrBottles[i];
				closestPearlDistance = currentPearlDistance;
				for (int j = 0; j < pearlsOrBottles.length; j++) {
					if (pearlsOrBottles[j].y < closestPearlOrBottle.y
							&& closestPearlOrBottle.y > info.getScene().getHeight() * 0.5) {
						closestPearlOrBottle = pearlsOrBottles[j];
						currentPearlDistance = calculateDistance(pearlsOrBottles[j], player);
					}
				}
			}
		}
		return closestPearlOrBottle;
	}

	public DivingAction seekPearl() {

		Point closestPearl = getClosestPearlOrBottle(pearlPoints, playerPos);

		Point directionPoint = pointFromStartToGoal(playerPos, closestPearl);

		float[] seekNormPoints = normalizePointToFloatArray(directionPoint);

		currentDirectionVec = new Vector(closestPearl.x, closestPearl.y);

		float directionToPearl = -(float) Math.atan2(seekNormPoints[1], seekNormPoints[0]);

		DivingAction pearlPursuit = new DivingAction(maxVel, directionToPearl);

		return pearlPursuit;
	}

	public boolean isPointAnObstacle(Point p) {
		for (int i = 0; i < obstacles.length; i++) {
			if (obstacles[i].contains(p)) {
				// System.out.println("one of the points is in an obstacle");
				return true;
			}
		}
		return false;
	}

	public float[] averageTwoPointsWithWeighing(float[] f1, float[] f2, float p1w, float p2w) {

		float[] f = new float[2];
		f[0] = (f1[0] * p1w + f2[0] * p2w) / 2;
		f[1] = (f1[1] * p1w + f2[1] * p2w) / 2;
		return normalizeVectorToFloatArray(f);
	}

	// ---------DrawDebugStuff------------------------

	@Override
	public void drawDebugStuff(Graphics2D gfx) {
		// --drawing in cell types---
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
		// ---drawing in path to goal---
		for (int i = 1; i < pathToGoal.size(); i++) {
			gfx.setColor(new Color(255, 255, 255));
			gfx.drawLine(pathToGoal.get(i).center.x, pathToGoal.get(i).center.y, pathToGoal.get(i - 1).center.x,
					pathToGoal.get(i - 1).center.y);
		}

		gfx.setColor(new Color(255, 255, 255));
		gfx.drawOval(playerPos.x, playerPos.y, 10, 10);

		// ---drawing in line to closestPearl, if obstacles in path draw red, else draw
		// green using new method
		Point closestPearl = getClosestPearlOrBottle(pearlPoints, playerPos);

		// line function taken from
		// https://stackoverflow.com/questions/37100841/draw-line-function
		double slope = (double) (closestPearl.y - playerPos.y) / (closestPearl.x - playerPos.x);
		// adjustable resolution factor
		double resolution = 1;
		double x = playerPos.x;
		// while (x <= returnClosestPearlCellCenter().x) {
		if (x <= closestPearl.x) {
			while (x < closestPearl.x) {
				double y = slope * (x - playerPos.x) + playerPos.y;
				Point linePoint = new Point((int) x, (int) y);
				// if(map.PointToMapCell(wCells, hCells, linePoint).status == Status.obstacle) {
				if (isPointAnObstacle(linePoint)) {
					gfx.setColor(new Color(255, 0, 0));
				} else if (!isPointAnObstacle(linePoint)) {
					gfx.setColor(new Color(0, 255, 0));
				}
				gfx.drawOval(linePoint.x, linePoint.y, 2, 2);
				x += resolution;
			}
		} else if (x >= closestPearl.x) {
			while (x > closestPearl.x) {
				double y = slope * (x - playerPos.x) + playerPos.y;
				Point linePoint = new Point((int) x, (int) y);
				// if(map.PointToMapCell(wCells, hCells, linePoint).status == Status.obstacle) {
				if (isPointAnObstacle(linePoint)) {
					gfx.setColor(new Color(255, 0, 0));
				} else if (!isPointAnObstacle(linePoint)) {
					gfx.setColor(new Color(0, 255, 0));
				}
				gfx.drawOval(linePoint.x, linePoint.y, 2, 2);
				x -= resolution;
			}
		}
		if (Math.abs(playerPos.x - closestPearl.x) < 5) {
			// float absDist = calculateDistance(playerPos, closestPearl);
			for (int i = playerPos.y; i < closestPearl.y; i += 10) {
				Point p = new Point(playerPos.x, i);
				gfx.setColor(new Color(0, 255, 0));
				if (isPointAnObstacle(p)) {
					gfx.setColor(new Color(255, 0, 0));
				} else {
					gfx.drawOval(p.x, p.y, 2, 2);
				}
			}
		}

		for (int i = 0; i < circle.length; i++) {
			if (circle[i] != null && circle[i].x > 0 && circle[i].y > 0) {
				gfx.setColor(new Color(0, 255, 255));
				if (circle[i].isVectorAnObstacle(obstacles, circle[i])) {
					gfx.setColor(new Color(255, 0, 0));
					gfx.drawLine((int) circle[i].x, (int) circle[i].y, (int) playerPos.x, (int) playerPos.y);
				}
				// gfx.drawOval((int)circle[i].x, (int)circle[i].y, 5,5);
			}
		}
		gfx.setColor(new Color(125, 255, 125));
		gfx.drawOval((int) playerVec.x, (int) playerVec.y, 10, 10);
		gfx.setColor(new Color(255, 165, 0));
		gfx.drawOval((int) circleVectorSum.x, (int) circleVectorSum.y, 15, 15);
		gfx.drawLine((int) playerVec.x, (int) playerVec.y, (int) currentDirectionVec.x, (int) currentDirectionVec.y);

	}
}
