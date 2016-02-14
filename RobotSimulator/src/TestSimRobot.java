import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;
import com.stonedahl.robotmaze.SimRobot;


public class TestSimRobot {

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		SimRobot simRobot = new SimRobot("maze1.txt", 0); // 500 ms animation delay
		
		int x = 0;
		int y = 0;
		boolean right = false;
		boolean left = false;
		boolean back = false;
		boolean front = false;
		int lastMove = -1;
		
		CellData[][] map = new CellData[4][4];
		int degreesRotated = 0;
		int angle;
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
			
			
			System.out.println("right: " + right + ", left: " + left + ", front: " + front + ", back: " + back);
			
			//keeps track of the orientation of the robot after it has moved with respect to the map
			//and creates new cell data if the current cell is new
			if(map[x][y] == null){
				if(degreesRotated % 360 == 0){
					map[x][y] = new CellData(x,y, front, back, right, left);
				} else if(degreesRotated % 360 == 90){
					map[x][y] = new CellData(x,y, left, right, front, back);
				} else if(degreesRotated % 360 == 180){
					map[x][y] = new CellData(x,y, back, front, left, right);
				} else if(degreesRotated % 360 == 270){
					map[x][y] = new CellData(x,y, right, left, back, front);
				}
				if(lastMove != -1){
					map[x][y].setCameFrom(lastMove);
				}
				int possibleMoves = map[x][y].possibleMoves();
				if(possibleMoves == 1 || possibleMoves == 0){ 
					moveHistory.push(map[x][y]);
				} else if(possibleMoves == 2){
					moveHistory.push(map[x][y]);
					moveHistory.push(map[x][y]);
					notFullyExplored.push(map[x][y]);
				} else if(possibleMoves == 3){
					moveHistory.push(map[x][y]);
					moveHistory.push(map[x][y]);
					moveHistory.push(map[x][y]);
					notFullyExplored.push(map[x][y]);
					notFullyExplored.push(map[x][y]);
				}
			}
			
			
			System.out.println("Distances Sensed:  R: " + distRight + " F: " + distStraight + " L: " + distLeft + "B: " + distBack);
			System.out.println("Move chosen: " + map[x][y].chooseMove());
			System.out.println(map[x][y]);
			
			angle = degreesRotated % 360;
			
			//moving conditionals that track its body position relative to the map directions
			if(map[x][y].chooseMove() == 0){
				x = notFullyExplored.peek().getX();
				y = notFullyExplored.peek().getY();
				findClosestMove(simRobot, degreesRotated, moveHistory, notFullyExplored);
				degreesRotated = 0;
			} else if(map[x][y].chooseMove() == 1) {
				if(angle == 90){
					simRobot.left90();
				} else if(angle == 180){
					simRobot.left90();
					simRobot.left90();
				} else if(angle == 270){
					simRobot.right90();
				}
				System.out.println("Move north");
				simRobot.forwardOneCell();
				map[x][y].setTriedNorth(true);
				y++;
				degreesRotated = 0;
				lastMove = 1;
			} else if(map[x][y].chooseMove() == 2) {
				if(angle == 0){
					simRobot.right90();
				} else if(angle == 180){
					simRobot.left90();
				} else if(angle == 270){
					simRobot.right90();
					simRobot.right90();
				}
				System.out.println("Move east");
				simRobot.forwardOneCell();
				map[x][y].setTriedEast(true);
				degreesRotated = 90;
				x++;
				lastMove = 2;
			} else if(map[x][y].chooseMove() == 3) {
				if(angle == 0){
					simRobot.right90();
					simRobot.right90();
				} else if(angle == 90){
					simRobot.right90();
				} else if(angle == 270){
					simRobot.left90();
				}
				System.out.println("Move South");
				simRobot.forwardOneCell();
				map[x][y].setTriedSouth(true);
				degreesRotated = 180;
				y--;
				lastMove = 3;
			} else if(map[x][y].chooseMove() == 4) {
				if(angle == 0){
					simRobot.left90();
				} else if(angle == 90){
					simRobot.right90();
					simRobot.right90();
				} else if(angle == 180){
					simRobot.right90();
				}
				System.out.println("Move west");
				simRobot.forwardOneCell();
				map[x][y].setTriedWest(true);
				degreesRotated = 270;
				x--;
				lastMove = 4;
			}
			
			System.out.println(moveHistory);
			System.out.println(notFullyExplored);
			//end condition for the robot
			
			if(simRobot.colorSensorSeesGoal()) {
				System.out.println("goal found");
				break;
			}
			
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
			} else {
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
		//after getting to the cell with the next open move, the robot orients itself north so we know what angle it is looking in
		if(angle % 360 == 90){
			simRobot.left90();
		} else if(angle % 360 == 180){
			simRobot.left90();
			simRobot.left90();
		} else if(angle % 360 == 270){
			simRobot.right90();
		}
	}
}
