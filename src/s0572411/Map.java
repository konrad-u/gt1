package s0572411;

import java.awt.Point;
import lenz.htw.ai4g.ai.Info;

public class Map{
	
	public MapCell[][] mapGrid;
	

	public Map() {
	}
	
	public MapCell PointToMapCell (Point target) {
		
		return null;
	}
	
	public Point MapCellToPoint (MapCell target) {
		
		return null;
	}
	
	public void setupMap(int res, Info info) {
		int divX = info.getScene().getWidth()/res;
		int divY = info.getScene().getHeight()/res;
		
		for (int i = 0; i < res; i++){
			for (int j = 0; j < res; j++) {
				mapGrid[i][j] = new MapCell();
				mapGrid[i][j].center = new Point(j + divX, i + divY);
			}
		}
	}
	
	public void setMapCellStatus(MapCell cell) {
		
	}

}
