
public class CellData {
	
	private boolean northWall;
	private boolean eastWall;
	private boolean southWall;
	private boolean westWall;
	private boolean triedNorth;
	private boolean triedEast;
	private boolean triedSouth;
	private boolean triedWest;
	private int xCoord;
	private int yCoord;
	
	public CellData(int xCoord, int yCoord, boolean northWall, boolean southWall, boolean eastWall, boolean westWall){
		
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.northWall = northWall;
		this.eastWall = eastWall;
		this.southWall = southWall;
		this.westWall = westWall;
		if(northWall){
			triedNorth = true;
		}
		if(eastWall){
			triedEast = true;
		}
		if(southWall){
			triedSouth = true;
		}
		if(westWall){
			triedWest = true;
		}
	}
	
	public int getX(){
		return xCoord;
	}
	
	public int getY(){
		return yCoord;
	}
	
	//returns the number of possible moves for a cell.
	public int possibleMoves(){
		int count = 0;
		if(!triedNorth){
			count++;
		}
		if(!triedEast){
			count++;
		}
		if(!triedSouth){
			count++;
		}
		if(!triedWest){
			count++;
		}
		return count;
	}
	
	//Set the precedence of move choice.  North over East over South over West.
	public int chooseMove(){
		if(!triedNorth){
			return 1;
		} else if(!triedEast){
			return 2;
		} else if(!triedSouth){
			return 3;
		}else if(!triedWest){
			return 4;
		} else {
			return 0;
		}
	}
}
