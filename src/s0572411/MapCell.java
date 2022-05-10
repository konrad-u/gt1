package s0572411;

import java.awt.Point;

public class MapCell {
	
	//center is setup i and j minus half the cell size 
	public Point center;
	
	//'coordinates' within 2D Map Array (not sure if this is actually needed)
	//as these are automatically already provided by holding array
	//update: is needed to directly access a mapCell in the Array
	public int mapX;
	public int mapY;
	
	public float minX, maxX, minY, maxY;
	
	//enum status describes either if cell is free (i.e. swimmable),
	//an obstacle (so not to be considered for path) 
	//or a pearl (diver is then to find the closest pearl and go there)
	public enum Status{
		free,obstacle,pearl
	}
	
	public Status status;
	
	public MapCell() {
		
	}
	
	public void setStatus(int statusType) {
		if(statusType == 0) {
			//status = free;
		}
	}

}
