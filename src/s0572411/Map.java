package s0572411;

import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.Info;
import s0572411.MapCell.Status;

public class Map{
	
	public MapCell[][] mapGrid;
	

	public Map(int wCells, int hCells, Info info, int res) {
		mapGrid = new MapCell[wCells][hCells];
		setupMap(wCells, hCells, info, res);
		for (int i = 0; i < wCells; i++){
			for (int j = 0; j < hCells; j++) {
				mapGrid[i][j].initNeighbors(this, wCells, hCells);
				}
			}
	}
	
	//this maybe a method of the MapCell class?
	public MapCell PointToMapCell (int wCells, int hCells, Point target) {
		
		for (int i = 0; i < wCells; i++){
			for (int j = 0; j < hCells; j++) {
				MapCell cell = mapGrid[i][j];
				if(isPointInMapCell(target, cell)) {
					return cell;
				}
			}
		}
		return null;
	}
	
	//what am I doing with this? I can directly access mapCell.center
	public Point MapCellToPoint (MapCell target) {
		return null;
	}
	
	//setting -1 for the maxX and maxY so no overlap between cells
	public void setupMap(int wCells, int hCells, Info info, int res) {
		
		for (int i = 0; i < wCells; i++){
			for (int j = 0; j < hCells; j++) {
				mapGrid[i][j] = new MapCell();
				mapGrid[i][j].mapX = i;
				mapGrid[i][j].mapY = j;
				mapGrid[i][j].center = new Point(res/2 + i*res, res/2 + j*res);
				mapGrid[i][j].minX = i*res;
				mapGrid[i][j].maxX = (i+1)*res-1;
				mapGrid[i][j].minY = j*res;
				mapGrid[i][j].maxY = (j+1)*res-1;
				
				mapGrid[i][j].tl = new Point((int)mapGrid[i][j].minX, (int)mapGrid[i][j].minY);
				mapGrid[i][j].tr = new Point((int)mapGrid[i][j].maxX, (int)mapGrid[i][j].minY);
				mapGrid[i][j].bl = new Point((int)mapGrid[i][j].minX, (int)mapGrid[i][j].maxY);
				mapGrid[i][j].br = new Point((int)mapGrid[i][j].maxX, (int)mapGrid[i][j].maxY);
				
				setMapCellStatus(mapGrid[i][j], info);
			}
		}
	}
	
	//better to create the points in the MapCell?
	public void setMapCellStatus(MapCell cell, Info info) {
		Path2D[] obstacles = info.getScene().getObstacles();
		Point[] pearlPoints = info.getScene().getPearl();
		for(int i = 0; i < obstacles.length; i++) {
			if(obstacles[i].contains(cell.tl) || obstacles[i].contains(cell.tr) || obstacles[i].contains(cell.bl) || obstacles[i].contains(cell.br)) {
				//mark cell as obstacle
				cell.status = Status.obstacle;
				break;
			}else {
				cell.status = Status.free;
				
			}
		}
		for(int i = 0; i < pearlPoints.length; i++) {
			if(isPointInMapCell(pearlPoints[i], cell)) {
				//mark cell as pearl
				cell.status = Status.pearl;
			}
		}
	}
	
	public boolean isPointInMapCell(Point p, MapCell c) {
		if(p.x >= c.minX && p.x <= c.maxX && p.y >= c.minY && p.y <= c.maxY) {
			return true;
		}
		return false;
	}
	
	

}
