import java.io.FileNotFoundException;
import java.lang.reflect.Array;

import com.stonedahl.robotmaze.SimRobot;


public class TestSimRobot {

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		SimRobot simRobot = new SimRobot("maze1.txt", 500); // 500 ms animation delay...

		// NOTE: The following is NOT a good solution to the maze problem...
		//    the robot just moves straight if it can (without hitting a wall)  
		//    and otherwise it turns right 90 degrees.
		//
		// The purpose is just to demonstrate how to use the SimRobot class...
		
		/*for (int robotMoveNum = 0; robotMoveNum < 20; robotMoveNum++)
		{
			simRobot.neckRight90();
			float distRight = simRobot.getDistanceMeasurement();
			simRobot.neckLeft90();
			float distStraight = simRobot.getDistanceMeasurement();
			simRobot.neckLeft90();
			float distLeft = simRobot.getDistanceMeasurement();
			System.out.print("Distances Sensed:  R: " + distRight + " S: " + distStraight + " L:" + distLeft);
			simRobot.neckRight90();
			Thread.sleep(2000);

			if (distStraight > 1) {
				simRobot.forwardOneCell();	
				if (simRobot.colorSensorSeesGoal()) {
					System.out.println("FOUND GOAL!");
					break;
				}
			} else  {
				simRobot.right90();
			}
		}		
	*/
		
		int x = 0;
		int y = 0;
		CellData[][] map = new CellData[4][4];
		
		
		
	
	
	}
	
		

}
