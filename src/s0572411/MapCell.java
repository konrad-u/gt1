package s0572411;

import java.awt.Point;
import java.util.ArrayList;

public class MapCell {
	
	//center is setup i and j minus half the cell size 
	public Point center;
	
	//'coordinates' within 2D Map Array (not sure if this is actually needed)
	//as these are automatically already provided by holding array
	//update: is needed to directly access a mapCell in the Array
	public int mapX;
	public int mapY;
	
	public float minX, maxX, minY, maxY;
	
	public Point tl, tr, bl, br;
	
	public float graphCost, airCost;
	
	public boolean marked;
	
	//enum status describes either if cell is free (i.e. swimmable),
	//an obstacle (so not to be considered for path) 
	//or a pearl (diver is then to find the closest pearl and go there)
	public enum Status{
		free,obstacle,pearl, collected
	}
	
	public Status status;
	
	public ArrayList<MapCell> neighborCells = new ArrayList<MapCell>();
	
	public MapCell() {
		
	}
	
	public void initNeighbors(Map map, int wCells, int hCells) {
		
		//O
		if(mapY -1 >=0) {
			neighborCells.add(map.mapGrid[mapX][mapY-1]);
		}
		//OR
		if(mapY -1 >=0 && mapX+1 < wCells) {
			neighborCells.add(map.mapGrid[mapX+1][mapY-1]);
		}
		//R
		if(mapX+1 < wCells) {
			neighborCells.add(map.mapGrid[mapX+1][mapY]);
		}
		//UR
		if(mapY +1 < hCells && mapX+1 < wCells) {
			neighborCells.add(map.mapGrid[mapX+1][mapY+1]);
		}
		//U
		if(mapY +1 < hCells) {
			neighborCells.add(map.mapGrid[mapX][mapY+1]);
		}
		//UL
		if(mapY +1 < hCells && mapX-1 >= 0) {
			neighborCells.add(map.mapGrid[mapX-1][mapY+1]);
		}
		//L
		if(mapX-1 >= 0) {
			neighborCells.add(map.mapGrid[mapX-1][mapY]);
		}
		//OL
		if(mapY -1 >=0 && mapX-1 >= 0 && !map.mapGrid[mapX-1][mapY-1].marked) {
			neighborCells.add(map.mapGrid[mapX-1][mapY-1]);
	}
	}

}
