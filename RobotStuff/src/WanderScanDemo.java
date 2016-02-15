/** TODO:
 * change rotation angle of 90 to 85
 * reads faulty directions
 */

import java.util.LinkedList;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.Color;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;


public class WanderScanDemo {
	static DifferentialPilot robotPilot;
	static RegulatedMotor frontNeckMotor = Motor.A;
	static EV3UltrasonicSensor distanceSensor;
	static EV3TouchSensor bumpSensor; 
	static EV3ColorSensor colorSensor;
	static SampleProvider bumpSampleProvider;
	static SampleProvider distanceSampleProvider;
	static SampleProvider colorSampleProvider;
	
	static final float nextCellDist = 110;  //moves 1 meter to traverse cell to cell

	static long startTimeMillis;

	public static void main(String[] args) {
		// Sets up Pilot for robot and sensors for robot
		robotPilot = new DifferentialPilot(5.4, 14.5, Motor.C, Motor.B, false);
		distanceSensor = new EV3UltrasonicSensor(SensorPort.S1);
		bumpSensor = new EV3TouchSensor(SensorPort.S2);
		colorSensor = new EV3ColorSensor(SensorPort.S4);
		distanceSampleProvider = distanceSensor.getDistanceMode();

		robotPilot.setTravelSpeed(30);
		robotPilot.setAcceleration(60);
		robotPilot.setRotateSpeed(60);
		robotPilot.reset();

		LCD.clear();
		LCD.drawString("ENTER to run!", 0,0);
		Button.waitForAnyPress();
		LCD.clear();
		
		run();
	}

	public static void run(){
		
		bumpSampleProvider = bumpSensor.getTouchMode();
		float[] bumpSample = new float[bumpSampleProvider.sampleSize()];
		float[] distanceScanData = new float[4];
		bumpSampleProvider.fetchSample(bumpSample, 0);
		
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
		
	
		//looking for white
		while(colorSensor.getColorID() != Color.WHITE && !Button.ESCAPE.isDown()){
			
			
			
			scanSurroundings(distanceScanData);
			

			float distRight = distanceScanData[1];
			float distBack = distanceScanData[2];
			float distLeft = distanceScanData[3];
			float distStraight = distanceScanData[0];

			correction(distanceScanData);
			
			Delay.msDelay(5000);
			LCD.clear();
			LCD.drawString(String.format("DF: %.2f DR: %.2f", distStraight, distRight),0,1);
			LCD.drawString(String.format("DL: %.2f DB: %.2f", distLeft, distBack),0,3);
			Delay.msDelay(5000);
			LCD.clear();
			if(distRight > 0 && distRight < 1) {
				right = true;
				LCD.drawString("right: " + right, 0,1);
			}
			if(distLeft > 0 && distLeft < 1) {
				left = true;
				LCD.drawString("left: " + left, 0,2);
			}
			if(distStraight > 0 && distStraight < 1) {
				front = true;
				LCD.drawString("front: " + front, 0,3);
			}
			if(distBack > 0 && distBack < 1) {
				back = true;
				LCD.drawString("back: " + back, 0,4);
			}
			Delay.msDelay(5000);
			
			//keeps track of the orientation of the robot after it has moved with respect to the map
			//and creates new cell data if the current cell is new
			if(map[x][y] == null){
				if(degreesRotated == 0){
					map[x][y] = new CellData(x,y, front, back, right, left);
				} else if(degreesRotated == 90){
					map[x][y] = new CellData(x,y, left, right, front, back);
				} else if(degreesRotated == 180){
					map[x][y] = new CellData(x,y, back, front, left, right);
				} else if(degreesRotated == 270){
					map[x][y] = new CellData(x,y, right, left, back, front);
				}

				analyzeSurroundings(map[x][y], map);
				//adding the cell to the stack the correct number of times
				int possibleMoves = map[x][y].possibleMoves();
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
			LCD.clear();
			LCD.drawString("move chosen: " + map[x][y].chooseMove(), 0, 1);
			Delay.msDelay(5000);
			LCD.clear();
			LCD.drawString("deg rot: " + degreesRotated, 0,1);
			Delay.msDelay(2000);
			//moving conditionals that track its body position relative to the map directions
			if(map[x][y].chooseMove() == 0){ //if no possible moves begin to backtrack
				//setting the coordinates of the cell it will end up in since the
				//findClosestMove method doesn't keep track of the coordinates
				x = notFullyExplored.peek().getX();
				y = notFullyExplored.peek().getY();
				findClosestMove(robotPilot, degreesRotated, moveHistory, notFullyExplored);
				degreesRotated = 0;
			} else if(map[x][y].chooseMove() == 1) {
				if(degreesRotated == 90){
					robotPilot.rotate(90);
				} else if(degreesRotated == 180){
					robotPilot.rotate(160);
				} else if(degreesRotated == 270){
					robotPilot.rotate(-90);
				}
				robotPilot.travel(nextCellDist);
				map[x][y].setTriedNorth(true);
				y++;
				degreesRotated = 0;
			} else if(map[x][y].chooseMove() == 2) {
				if(degreesRotated == 0){
					robotPilot.rotate(-90);
				} else if(degreesRotated == 180){
					robotPilot.rotate(90);
				} else if(degreesRotated == 270){
					robotPilot.rotate(160);
				}
				robotPilot.travel(nextCellDist);
				map[x][y].setTriedEast(true);
				degreesRotated = 90;
				x++;
			} else if(map[x][y].chooseMove() == 3) {
				if(degreesRotated == 0){
					robotPilot.rotate(160);
				} else if(degreesRotated == 90){
					robotPilot.rotate(-90);
				} else if(degreesRotated == 270){
					robotPilot.rotate(90);
				}
				robotPilot.travel(nextCellDist);
				map[x][y].setTriedSouth(true);
				degreesRotated = 180;
				y--;
			} else if(map[x][y].chooseMove() == 4) {
				if(degreesRotated == 0){
					robotPilot.rotate(90);
				} else if(degreesRotated == 90){
					robotPilot.rotate(160);
				} else if(degreesRotated == 180){
					robotPilot.rotate(-90);
				}
				robotPilot.travel(nextCellDist);
				map[x][y].setTriedWest(true);
				degreesRotated = 270;
				x--;
			}
			
			//resets the directional values for the next iteration
			right = false;
			left = false;
			front = false;
			back = false;

			while (robotPilot.isMoving()) {
				bumpSampleProvider.fetchSample(bumpSample, 0);
				if(bumpSample[0] == 1){ // is the touch sensor currently pushed in?
					robotPilot.stop();
					robotPilot.travel(-10);
					robotPilot.rotate(-90); // always turn 90 degrees when you bump into something?
				}
				if (colorSensor.getColorID() == Color.WHITE){ // found the GOAL
					break;
				}
			}
		}
		robotPilot.stop();
		Button.LEDPattern(4); // victory celebration!
		Sound.beepSequenceUp();
		Sound.beepSequence();
		Button.waitForAnyPress();
	}

	/** Robots looks around at 4 evenly spaced intervals, measuring the 
	 *  distance to an object at each of those angles, and storing those distances
	 * into the distanceScanData array parameter that was passed in.
	 */
	public static void scanSurroundings(float[] distanceScanData ){
		int count = 0;
		while(count < 4){
			if (Button.ENTER.isDown()) { return; } // break out, if someone is holding down the ENTER button.

			distanceScanData[count] = getDistanceMeasurement();
			frontNeckMotor.rotate(90);  // 30 degrees is 1/12 of a full circle...
			count++;
		}
		frontNeckMotor.rotate(-360); // rotate neck back (otherwise cords will tighten/tangle). 
	}

	/**
	 *  Robot chooses an angle to turn, based on the distance readings in each direction.
	 *  In this case, it goes straight if the path in front is clear, but otherwise
	 *   it chooses to go in the direction that has the longest line-of-sight distance. 
	 * @param distanceScanData - array of distance measurements at each angle.
	 * @return an angle (in degrees) for the robot to turn.
	 */
	public static int chooseDirectionAngle(float[] distanceScanData){

		float maxDistance = 0;
		if(distanceScanData[0]>1){ // if the path seems clear up to 1 meter ahead, go straight ahead
			return 0; // angle 0, no turn
		}
		int maxIndex = 0;

		// this loop finds the direction that had the farthest distance reading
		Delay.msDelay(5000);
		for(int i = 0; i < distanceScanData.length; i++){
			if(Double.isInfinite(distanceScanData[i])){  // anything beyond 2.5m reads as infinity from the sensor
				maxIndex = i;
				break;
			}else if(distanceScanData[i] >= maxDistance){
				maxDistance = distanceScanData[i];
				maxIndex = i;
			}
		}	
		int angle = (maxIndex* -90);   // goes towards that index
		if (angle < -180) { angle += 360; }

		return angle;
	}
	
	public static void correction(float[] dirDist) {
		
		float distance;
		
		float frontDist = dirDist[0];
		float rightDist = dirDist[1];
		float leftDist = dirDist[3];
		float backDist = dirDist[2];
		
		
		LCD.drawString(String.format("Dist: %.2f", rightDist), 0, 4);
		Delay.msDelay(500);
		LCD.clear();
		
		if(rightDist <= 0.25) {
			Sound.playNote(Sound.FLUTE, 700, 100);
			robotPilot.rotate(-90);
			distance = getDistanceMeasurement();
			LCD.drawString(String.format("Dist: %.2f", distance), 0, 4);
			Delay.msDelay(500);
			while(distance <= 0.4 && !Button.ESCAPE.isDown()) { 	//distance is in meters?
				LCD.clear();
				LCD.drawString(String.format("Dist: %.2f", distance), 0, 4);
				robotPilot.travel(-5);
				Delay.msDelay(500);
				distance = getDistanceMeasurement();
			}
			robotPilot.rotate(90);
		}
		if(leftDist <= 0.25) {
			Sound.playNote(Sound.FLUTE, 700, 100);
			robotPilot.rotate(90);
			while(leftDist <= 0.4) { 	//distance is in meters?
				LCD.clear();
				LCD.drawString(String.format("Dist: %.2f", leftDist), 0, 4);
				robotPilot.travel(-5);
				Delay.msDelay(500);
				
				distance = getDistanceMeasurement();
			}
			robotPilot.rotate(-90);
		}
		if(backDist <= 0.25) {
			Sound.playNote(Sound.FLUTE, 700, 100);
			frontNeckMotor.rotate(180);
			while(backDist <= 0.4) { 	//distance is in meters?
				LCD.clear();
				LCD.drawString(String.format("Dist: %.2f", leftDist), 0, 4);
				robotPilot.travel(5);
				Delay.msDelay(500);
				
				distance = getDistanceMeasurement();
			}
			frontNeckMotor.rotate(-180);
		}
		if(frontDist <= 0.25) {
			Sound.playNote(Sound.FLUTE, 700, 100);
			while(backDist <= 0.4) { 	//distance is in meters?
				LCD.clear();
				LCD.drawString(String.format("Dist: %.2f", leftDist), 0, 4);
				robotPilot.travel(-5);
				Delay.msDelay(500);
				
				distance = getDistanceMeasurement();
			}
		}
		
		
	}

	/** Convenience method to get ONE distance measurement from the robot.
	 * (Technically it's a bit inefficient to keep re-allocating this small float[] array every time,
	 *   when we could be directly fetching the value into the scan array, but using this method
	 *   probably promotes more readable code...)
	 * 
	 * @return the distance reading (in meters) in the direction the EV3's ultra-sonic sensor is currently facing.
	 */
	public static float getDistanceMeasurement(){
		float[] distanceSample = new float[distanceSampleProvider.sampleSize()];
		distanceSampleProvider.fetchSample(distanceSample, 0);

		return distanceSample[0];
	}
	
	public static void analyzeSurroundings(CellData currentCell, CellData[][] map){
		int x = currentCell.getX();
		int y = currentCell.getY();
		
		//look at the cell to the North
		try{
			CellData north = map[x][y+1];
			if(north == null){
			} else {
				currentCell.setTriedNorth(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			currentCell.setTriedNorth(true);
		}
		
		//look at the cell to the East
		try{
			CellData east = map[x+1][y];
			if(east == null){
			} else {
				currentCell.setTriedEast(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			currentCell.setTriedEast(true);
		}
		
		//look at the cell to the South
		try{
			CellData south = map[x][y-1];
			if(south == null){
			} else {
				currentCell.setTriedSouth(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			currentCell.setTriedSouth(true);
		} 
		
		//look at the cell to the West
		try{
			CellData west = map[x-1][y];
			if(west == null){
			} else {
				currentCell.setTriedWest(true);
			}
		} catch(IndexOutOfBoundsException e){
			//exception thrown if at the edge of the maze
			currentCell.setTriedWest(true);
		}
		LCD.clear();
		LCD.drawString("Surroundings analyzed", 0, 1);
		Delay.msDelay(1000);
		
	}
	
	public static void findClosestMove(DifferentialPilot robotPilot, int angle, LinkedList<CellData> moveHistory, LinkedList<CellData> notFullyExplored){
		CellData destination = notFullyExplored.pop();
		CellData currentCell = moveHistory.pop();
		CellData nextMove = moveHistory.peek();
		//while the robot is still making its way back to the closest open move
		while((currentCell.getX() != destination.getX()) || (currentCell.getY() != destination.getY())){
			if(currentCell.getX() - nextMove.getX() == 0){
				//if the x values are the same then move either north or south
				if(currentCell.getY() - nextMove.getY() == 1){
					//then go south
					if(angle % 360 == 0){
						robotPilot.rotate(160);
					} else if(angle % 360 == 90){
						robotPilot.rotate(-90);
					} else if(angle % 360 == 270){
						robotPilot.rotate(90);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to south
					angle = 180;
					currentCell = moveHistory.pop();
				} else if(currentCell.getY() - nextMove.getY() == -1){
					//then go North
					if(angle % 360 == 90){
						robotPilot.rotate(90);
					} else if(angle % 360 == 180){
						robotPilot.rotate(160);
					} else if(angle % 360 == 270){
						robotPilot.rotate(-90);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to North
					angle = 0;
					currentCell = moveHistory.pop();
				}
			} else {
				if(currentCell.getX() - nextMove.getX() == 1) {
					//go west
					if(angle % 360 == 0){
						robotPilot.rotate(90);
					} else if(angle % 360 == 90){
						robotPilot.rotate(160);
					} else if(angle % 360 == 180){
						robotPilot.rotate(-90);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to North
					angle = 270;
					currentCell = moveHistory.pop();
				} else if(currentCell.getX() - nextMove.getX() == -1){
					//go East
					if(angle % 360 == 0){
						robotPilot.rotate(-90);
					} else if(angle % 360 == 180){
						robotPilot.rotate(90);
					} else if(angle % 360 == 270){
						robotPilot.rotate(160);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to North
					angle = 90;
					currentCell = moveHistory.pop();
				}
			}
			nextMove = moveHistory.peek();
		}
		//after getting to the cell with the next open move, the robot orients itself north
		if(angle % 360 == 90){
			robotPilot.rotate(90);
		} else if(angle % 360 == 180){
			robotPilot.rotate(160);
		} else if(angle % 360 == 270){
			robotPilot.rotate(-90);
		}
	}
	
	public static void goHome(DifferentialPilot robotPilot, int angle, LinkedList<CellData> moveHistory){
		CellData destination = moveHistory.getLast();
		CellData currentCell = moveHistory.pop();
		CellData nextMove = moveHistory.peek();
		
		while((currentCell.getX() != destination.getX()) || (currentCell.getY() != destination.getY())){
			if((currentCell.getX() - nextMove.getX() == 0) && (currentCell.getY() - nextMove.getY() == 0)){
				currentCell = moveHistory.pop();
			} else if(currentCell.getX() - nextMove.getX() == 0){
				//if the x values are the same then move either north or south
				if(currentCell.getY() - nextMove.getY() == 1){
					//then go south
					if(angle % 360 == 0){
						robotPilot.rotate(160);
					} else if(angle % 360 == 90){
						robotPilot.rotate(-90);
					} else if(angle % 360 == 270){
						robotPilot.rotate(90);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to south
					angle = 180;
					currentCell = moveHistory.pop();
				} else {
					//then go North
					if(angle % 360 == 90){
						robotPilot.rotate(90);
					} else if(angle % 360 == 180){
						robotPilot.rotate(160);
					} else if(angle % 360 == 270){
						robotPilot.rotate(-90);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to North
					angle = 0;
					currentCell = moveHistory.pop();
				}
			} else if(currentCell.getY() - nextMove.getY() == 0){
				if(currentCell.getX() - nextMove.getX() == 1) {
					//go west
					if(angle % 360 == 0){
						robotPilot.rotate(90);
					} else if(angle % 360 == 90){
						robotPilot.rotate(160);
					} else if(angle % 360 == 180){
						robotPilot.rotate(-90);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to North
					angle = 270;
					currentCell = moveHistory.pop();
				} else {
					//go East
					if(angle % 360 == 0){
						robotPilot.rotate(-90);
					} else if(angle % 360 == 180){
						robotPilot.rotate(90);
					} else if(angle % 360 == 270){
						robotPilot.rotate(160);
					}
					robotPilot.travel(nextCellDist);
					//reset the angle to North
					angle = 90;
					currentCell = moveHistory.pop();
				}
			}
			nextMove = moveHistory.peek();
		}
	}



}
