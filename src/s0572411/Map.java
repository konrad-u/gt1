package s0572411;

import java.awt.Point;
import java.awt.geom.Path2D;

import lenz.htw.ai4g.ai.Info;
import s0572411.MapCell.Status;

public class Map{
	
	public MapCell[][] mapGrid;
	

	public Map(int res, Info info) {
		mapGrid = new MapCell[res][res];
		setupMap(res, info);
	}
	
	//this maybe a method of the MapCell class?
	public MapCell PointToMapCell (int res, Point target) {
		
		for (int i = 0; i < res; i++){
			for (int j = 0; j < res; j++) {
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
	public void setupMap(int res, Info info) {
		int divX = info.getScene().getWidth()/res;
		int divY = info.getScene().getHeight()/res;
		
		for (int i = 0; i < res; i++){
			for (int j = 0; j < res; j++) {
				mapGrid[i][j] = new MapCell();
				mapGrid[i][j].mapX = i;
				mapGrid[i][j].mapY = j;
				mapGrid[i][j].center = new Point(divX/2 + i*divX, divY/2 + i*divY);
				mapGrid[i][j].minX = i*divX;
				mapGrid[i][j].maxX = (i+1)*divX-1;
				mapGrid[i][j].minY = i*divY;
				mapGrid[i][j].maxY = (i+1)*divY-1;
				setMapCellStatus(mapGrid[i][j], info);
			}
		}
	}
	
	//better to create the points in the MapCell?
	public void setMapCellStatus(MapCell cell, Info info) {
		Path2D[] obstacles = info.getScene().getObstacles();
		Point[] pearlPoints = info.getScene().getPearl();
		for(int i = 0; i < obstacles.length; i++) {
			Point tl = new Point((int)cell.minX, (int)cell.minY);
			Point tr = new Point((int)cell.maxX, (int)cell.minY);
			Point bl = new Point((int)cell.minX, (int)cell.maxY);
			Point br = new Point((int)cell.maxX, (int)cell.maxY);
			if(obstacles[i].contains(tl) || obstacles[i].contains(tr) || obstacles[i].contains(bl) || obstacles[i].contains(br)) {
				//mark cell as obstacle
				cell.status = Status.obstacle;
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
