import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;
import com.stonedahl.robotmaze.SimRobot;


public class TestSimRobot {

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		SimRobot simRobot = new SimRobot("maze1.txt", 500); // 500 ms animation delay
		
		int x = 0;
		int y = 0;
		boolean right = false;
		boolean left = false;
		boolean back = false;
		boolean front = false;
		
		CellData[][] map = new CellData[4][4];
		int degreesRotated = 0;
		int angle;
		LinkedList<CellData> history = new LinkedList<CellData>();
		LinkedList<CellData> openDir = new LinkedList<CellData>();
		
		
		for (int robotMoveNum = 0; robotMoveNum < 20; robotMoveNum++)	{
			
			float distStraight = simRobot.getDistanceMeasurement();
			simRobot.neckRight90();
			float distRight = simRobot.getDistanceMeasurement();
			simRobot.neckRight90();
			float distBack = simRobot.getDistanceMeasurement();
			simRobot.neckRight90();
			float distLeft = simRobot.getDistanceMeasurement();
			
			simRobot.neckRight90();
			Thread.sleep(2000);
			
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
			if(degreesRotated % 360 == 0){
				map[x][y] = new CellData(x,y, front, back, right, left);
			} else if(degreesRotated % 360 == 90){
				map[x][y] = new CellData(x,y, left, right, front, back);
			} else if(degreesRotated % 360 == 180){
				map[x][y] = new CellData(x,y, back, front, left, right);
			} else if(degreesRotated % 360 == 270){
				map[x][y] = new CellData(x,y, right, left, back, front);
			}
			
			System.out.println("Distances Sensed:  R: " + distRight + " F: " + distStraight + " L: " + distLeft + "B: " + distBack);
			System.out.println("Move chosen: " + map[x][y].chooseMove());
			System.out.println(x + ", " + y);
			String str = map[x][y].toString();
			System.out.println(str);
			
			angle = degreesRotated % 360;
			
			
			//moving conditionals that track its body position relative to the map directions
			if(map[x][y].chooseMove() == 1) {
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
			} else if(map[x][y].chooseMove() == 3) {
				if(angle == 0){
					simRobot.right90();
					simRobot.right90();
				} else if(angle == 90){
					simRobot.right90();
				} else if(angle == 270){
					simRobot.left90();
				}
				System.out.println("Move south");
				simRobot.forwardOneCell();
				map[x][y].setTriedSouth(true);
				
				degreesRotated = 180;
				y--;
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
			}
			
			//end condition for the robot
			if(simRobot.colorSensorSeesGoal()) {
				System.out.println("goal found");
				break;
			}
			
			//resets the boolean values for the next i-th iteration
			right = false;
			left = false;
			front = false;
			back = false;
		

		}
		
	}
}
