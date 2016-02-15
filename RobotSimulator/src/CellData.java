
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
		//by default set to the wall value--if there is a wall then that route can't be explored
		this.triedNorth = northWall;
		this.triedEast = eastWall;
		this.triedSouth = southWall;
		this.triedWest = westWall;
		
	}
	//------accessor methods------//
	public boolean getNorthWall(){
		return northWall;
	}
	
	public boolean getEastWall(){
		return eastWall;
	}
	
	public boolean getSouthWall(){
		return southWall;
	}
	
	public boolean getWestWall(){
		return westWall;
	}
	
	public boolean getTriedNorth(){
		return triedNorth;
	}
	
	public boolean getTriedEast(){
		return triedEast;
	}
	
	public boolean getTriedSouth(){
		return triedSouth;
	}
	
	public boolean getTriedWest(){
		return triedWest;
	}
	
	public int getX(){
		return xCoord;
	}
	
	public int getY(){
		return yCoord;
	}
	//------end accessor methods------//
	
	//------mutator methods------//
	public void setTriedNorth(boolean value){
		this.triedNorth = value;
	}
	
	public void setTriedEast(boolean value){
		this.triedEast = value;
	}
		
	public void setTriedSouth(boolean value){
		this.triedSouth = value;
	}
	
	public void setTriedWest(boolean value){
		this.triedWest = value;
	}
	//------end mutator methods------//
	
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
	
	public void setCameFrom(int cameFrom){
		if(cameFrom == 1){
			triedSouth = true;
		} else if(cameFrom == 2){
			triedWest = true;
		} else if(cameFrom == 3){
			triedNorth = true;
		} else if(cameFrom == 4){
			triedEast = true;
		}
	}
	
	public String toString(){
		String str = "(" + xCoord + ", " + yCoord + ")";
		return str;
	}
}
