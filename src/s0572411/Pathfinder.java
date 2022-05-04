package s0572411;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DivingAction;
import lenz.htw.ai4g.ai.Info;
import lenz.htw.ai4g.ai.PlayerAction;

public class Pathfinder extends AI {
	
	Point[] pearlPoints = info.getScene().getPearl();
	Point[] visitedPearls = new Point[pearlPoints.length];
	Path2D[] obstacles = info.getScene().getObstacles();
	Point playerPos;

	public Pathfinder(Info info) {
		super(info);
		enlistForTournament(572411);
	}

	@Override
	public String getName() {
		return "Tomitaro Fujii Pathfinder";
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
		
		return new DivingAction(1.0f,3.14f);
	}
	
	public MapCell[][] getMapGrid(int cellSize){
		int 
		MapCell[][] mapGrid = new MapCell[][];
	}

}
