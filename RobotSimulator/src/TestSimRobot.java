import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;
import com.stonedahl.robotmaze.SimRobot;


public class TestSimRobot {

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		SimRobot simRobot = new SimRobot("maze1.txt", 100); // 500 ms animation delay
		
		int x = 0;
		int y = 0;
		boolean right = false;
		boolean left = false;
		boolean back = false;
		boolean front = false;
		
		CellData[][] map = new CellData[4][4];
		int degreesRotated = 0;
		LinkedList<CellData> moveHistory = new LinkedList<CellData>();
		LinkedList<CellData> notFullyExplored = new LinkedList<CellData>();
		
		for (int robotMoveNum = 0; robotMoveNum < 20; robotMoveNum++)	{
			//getting the distance reading for the current cell
			float distStraight = simRobot.getDistanceMeasurement();
			simRobot.neckRight90();
			float distRight = simRobot.getDistanceMeasurement();
			simRobot.neckRight90();
			float distBack = simRobot.getDistanceMeasurement();
			simRobot.neckRight90();
			float distLeft = simRobot.getDistanceMeasurement();
			
			simRobot.neckRight90();
			Thread.sleep(2000);
			
			//determining where the walls are
			if(Math.ceil(distRight) == 1) {
				right = true;
			}
			if(Math.ceil(distLeft) == 1) {
				left = true;
			}
			if(Math.ceil(distStraight) == 1) {
				front = true;
			}
			if(Math.ceil(distBack) == 1) {
				back = true;
			}
			
			System.out.println("Where are the walls? Front: " + front + " Right: " + right + " Back: " + back + " Left: " + left);
			
			//keeps track of the orientation of the robot after it has moved with respect to the map
			//and creates new cell data if the current cell is new
			if(map[x][y] == null){
				System.out.print("Creating new cell data");
				if(degreesRotated == 0){
					System.out.println(" while looking North");
					map[x][y] = new CellData(x,y, front, back, right, left);
				} else if(degreesRotated == 90){
					System.out.println(" while looking East");
					map[x][y] = new CellData(x,y, left, right, front, back);
				} else if(degreesRotated == 180){
					System.out.println(" while looking South");
					map[x][y] = new CellData(x,y, back, front, left, right);
				} else if(degreesRotated == 270){
					System.out.println(" while looking West");
					map[x][y] = new CellData(x,y, right, left, back, front);
				}
				
				analyzeSurroundings(map[x][y], map);
				
				//adding the cell to the stack the correct number of times
				int possibleMoves = map[x][y].possibleMoves();
				System.out.println(possibleMoves);
				if(possibleMoves >= 2){ 
					if(possibleMoves == 2){
						moveHistory.push(map[x][y]);
						moveHistory.push(map[x][y]);
						notFullyExplored.push(map[x][y]);
					} else { //there are 3 possible open moves
						moveHistory.push(map[x][y]);
						moveHistory.push(map[x][y]);
						moveHistory.push(map[x][y]);
						notFullyExplored.push(map[x][y]);
						notFullyExplored.push(map[x][y]);
					}
				} else { //there are 0 or 1 possible open moves
					moveHistory.push(map[x][y]);
				}
			}
			//after getting the distance readings from the cell and creating the cell info,
			//then check to see if the goal has been found
			if(simRobot.colorSensorSeesGoal()) {
				System.out.println("Goal found!");
				goHome(simRobot, degreesRotated, moveHistory);
				break;
			}
			
			System.out.println("Distances Sensed:  R: " + distRight + " F: " + distStraight + " L: " + distLeft + " B: " + distBack);
			System.out.println("Move chosen: " + map[x][y].chooseMove());
			
			int choice = map[x][y].chooseMove();
			//moving conditionals that track its body position relative to the map directions
			if(choice == 0){ //if no possible moves begin to backtrack
				//setting the coordinates of the cell it will end up in since the
				//findClosestMove method doesn't keep track of the coordinates
				x = notFullyExplored.peek().getX();
				y = notFullyExplored.peek().getY();
				findClosestMove(simRobot, degreesRotated, moveHistory, notFullyExplored);
				degreesRotated = 0;
			} else if(choice == 1) {
				moveNorth(simRobot, degreesRotated);
				map[x][y].setTriedNorth(true);
				y++;
				degreesRotated = 0;
			} else if(choice == 2) {
				moveEast(simRobot, degreesRotated);
				map[x][y].setTriedEast(true);
				degreesRotated = 90;
				x++;
			} else if(choice == 3) {
				moveSouth(simRobot, degreesRotated);
				map[x][y].setTriedSouth(true);
				degreesRotated = 180;
				y--;
			} else if(choice == 4) {
				moveWest(simRobot, degreesRotated);
				map[x][y].setTriedWest(true);
				degreesRotated = 270;
				x--;
			}
			
			System.out.println(moveHistory);
			System.out.println(notFullyExplored);
			
			//resets the boolean values for the next iteration
			right = false;
			left = false;
			front = false;
			back = false;
		}
		
	}
	
	public static void findClosestMove(SimRobot simRobot, int angle, LinkedList<CellData> moveHistory, LinkedList<CellData> notFullyExplored){
		CellData destination = notFullyExplored.pop();
		CellData currentCell = moveHistory.pop();
		CellData nextMove = moveHistory.peek();
		System.out.println("The current cell is (" + currentCell.getX() + ", " + currentCell.getY() + ")");
		System.out.println("The destination cell is (" + destination.getX() + ", " + destination.getY() + ")");
		System.out.println("The next cell should be (" + nextMove.getX() + ", " + nextMove.getY() + ")");
		
		//while the robot is still making its way back to the closest open move
		while((currentCell.getX() != destination.getX()) || (currentCell.getY() != destination.getY())){
			System.out.println("We are in the while loop!");
			System.out.println("The current cell is (" + currentCell.getX() + ", " + currentCell.getY() + ")");
			System.out.println("The next cell should be (" + nextMove.getX() + ", " + nextMove.getY() + ")");
			if(currentCell.getX() - nextMove.getX() == 0){
				//if the x values are the same then move either north or south
				System.out.println("Move North or South");
				if(currentCell.getY() - nextMove.getY() == 1){
					moveSouth(simRobot, angle);
					angle = 180;
					currentCell = moveHistory.pop();
				} else if(currentCell.getY() - nextMove.getY() == -1){
					moveNorth(simRobot, angle);
					angle = 0;
					currentCell = moveHistory.pop();
				}
			} else {
				System.out.println("Move East or West");
				if(currentCell.getX() - nextMove.getX() == 1) {
					moveWest(simRobot, angle);
					angle = 270;
					currentCell = moveHistory.pop();
				} else if(currentCell.getX() - nextMove.getX() == -1){
					moveEast(simRobot, angle);
					angle = 90;
					currentCell = moveHistory.pop();
				}
			}
			nextMove = moveHistory.peek();
		}
		//orient robot north after arrived at desired cell
		if(angle == 90){
			simRobot.left90();
		} else if(angle == 180){
			simRobot.left90();
			simRobot.left90();
		} else if(angle == 270){
			simRobot.right90();
		}
	}
	
	
	public static void goHome(SimRobot simRobot, int angle, LinkedList<CellData> moveHistory){
		System.out.println("E.T. phone home...");
		CellData destination = moveHistory.getLast();
		CellData currentCell = moveHistory.pop();
		CellData nextMove = moveHistory.peek();
		
		while((currentCell.getX() != destination.getX()) || (currentCell.getY() != destination.getY())){
			System.out.println("The current cell is (" + currentCell.getX() + ", " + currentCell.getY() + ")");
			System.out.println("The next cell should be (" + nextMove.getX() + ", " + nextMove.getY() + ")");
			if((currentCell.getX() - nextMove.getX() == 0) && (currentCell.getY() - nextMove.getY() == 0)){
				System.out.println("There was a duplicate");
				currentCell = moveHistory.pop();
			} else if(currentCell.getX() - nextMove.getX() == 0){
				//if the x values are the same then move either north or south
				System.out.println("Move North or South");
				if(currentCell.getY() - nextMove.getY() == 1){
					//then go south
					System.out.println("Go South");
					if(angle % 360 == 0){
						simRobot.right90();
						simRobot.right90();
					} else if(angle % 360 == 90){
						simRobot.right90();
					} else if(angle % 360 == 270){
						simRobot.left90();
					}
					simRobot.forwardOneCell();
					//reset the angle to south
					angle = 180;
					currentCell = moveHistory.pop();
				} else {
					//then go North
					System.out.println("Go North");
					if(angle % 360 == 90){
						simRobot.left90();
					} else if(angle % 360 == 180){
						simRobot.left90();
						simRobot.left90();
					} else if(angle % 360 == 270){
						simRobot.right90();
					}
					simRobot.forwardOneCell();
					//reset the angle to North
					angle = 0;
					currentCell = moveHistory.pop();
				}
			} else if(currentCell.getY() - nextMove.getY() == 0){
				System.out.println("Move East or West");
				if(currentCell.getX() - nextMove.getX() == 1) {
					//go west
					System.out.println("Go West");
					if(angle % 360 == 0){
						simRobot.left90();
					} else if(angle % 360 == 90){
						simRobot.right90();
						simRobot.right90();
					} else if(angle % 360 == 180){
						simRobot.right90();
					}
					simRobot.forwardOneCell();
					//reset the angle to North
					angle = 270;
					currentCell = moveHistory.pop();
				} else {
					//go East
					System.out.println("Go East");
					if(angle % 360 == 0){
						simRobot.right90();
					} else if(angle % 360 == 180){
						simRobot.left90();
					} else if(angle % 360 == 270){
						simRobot.right90();
						simRobot.right90();
					}
					simRobot.forwardOneCell();
					//reset the angle to North
					angle = 90;
					currentCell = moveHistory.pop();
				}
			}
			nextMove = moveHistory.peek();
		}
		System.out.println("E.T. made it back successfully!");
	}
	
	/**
	 * Looks at the surrounding cells based on the current one to determine which ways have been tried
	 * already and sets the appropriate boolean values in the current cell
	 * 
	 * @param currentCell -- this is the cell in the maze the robot is currently in
	 * @param map -- this is the array that stores the cell data of all explored cells in the maze
	 */
	public static void analyzeSurroundings(CellData currentCell, CellData[][] map){
		int x = currentCell.getX();
		int y = currentCell.getY();
		
		//look at the cell to the North
		try{
			CellData north = map[x][y+1];
			if(north == null){
				System.out.println("No cell to the North");
			} else {
				System.out.println("There is a cell to the North");
				currentCell.setTriedNorth(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			System.out.println("At the northern edge");
			currentCell.setTriedNorth(true);
		}
		
		//look at the cell to the East
		try{
			CellData east = map[x+1][y];
			if(east == null){
				System.out.println("No cell to the East");
			} else {
				System.out.println("There is a cell to the East");
				currentCell.setTriedEast(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			System.out.println("At the Eastern edge");
			currentCell.setTriedEast(true);
		}
		
		//look at the cell to the South
		try{
			CellData south = map[x][y-1];
			if(south == null){
				System.out.println("No cell to the South");
			} else {
				System.out.println("There is a cell to the North");
				currentCell.setTriedSouth(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			System.out.println("At the Southern edge");
			currentCell.setTriedSouth(true);
		} 
		
		//look at the cell to the West
		try{
			CellData west = map[x-1][y];
			if(west == null){
				System.out.println("No cell to the West");
			} else {
				System.out.println("There is a cell to the West");
				currentCell.setTriedWest(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			System.out.println("At the Western edge");
			currentCell.setTriedWest(true);
		}
		
	}
	
	public static void moveNorth(SimRobot simRobot, int degreesRotated){
		if(degreesRotated == 90){
			simRobot.left90();
		} else if(degreesRotated == 180){
			simRobot.left90();
			simRobot.left90();
		} else if(degreesRotated == 270){
			simRobot.right90();
		}
		simRobot.forwardOneCell();
	}
	public static void moveEast(SimRobot simRobot, int degreesRotated){
		if(degreesRotated == 0){
			simRobot.right90();
		} else if(degreesRotated == 180){
			simRobot.left90();
		} else if(degreesRotated == 270){
			simRobot.right90();
			simRobot.right90();
		}
		simRobot.forwardOneCell();
	}
	public static void moveSouth(SimRobot simRobot, int degreesRotated){
		if(degreesRotated == 0){
			simRobot.right90();
			simRobot.right90();
		} else if(degreesRotated == 90){
			simRobot.right90();
		} else if(degreesRotated == 270){
			simRobot.left90();
		}
		simRobot.forwardOneCell();
	}
	public static void moveWest(SimRobot simRobot, int degreesRotated){
		if(degreesRotated == 0){
			simRobot.left90();
		} else if(degreesRotated == 90){
			simRobot.right90();
			simRobot.right90();
		} else if(degreesRotated == 180){
			simRobot.right90();
		}
		simRobot.forwardOneCell();
	}
}
