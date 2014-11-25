package com.pathrecorder;

import java.util.*;

import android.graphics.Path;

class Move {
	public int direction;
	public long durationInMilliSec;
	
	public Move(int direction, long durationInMilliSec) {
		this.direction = direction;
		this.durationInMilliSec = durationInMilliSec;
	}
}

enum ANGLE_QUARTER {
	UNDEFINED,
	NORTH_WEST,
	WEST_SOUTH,
	SOUTH_EAST,
	EAST_NORTH
}

class MovementStatistics {
	private long durationToNorth;
	private long durationToSouth;
	private long durationToEast;
	private long durationToWest;
	private long rotationsClockWise;
	private long rotationsCounterClockWise;
	
	private final int TOTAL_INFO_LINES = 13;
	private String infoMessages[] = new String[TOTAL_INFO_LINES];
	
	public String[] getStatisticsInfoMessages() {
		long totalDuration = durationToNorth + durationToWest + durationToSouth + durationToEast;
		long totalRotations = rotationsClockWise + rotationsCounterClockWise;
		
		int ix = -1;
		String spaces = "    ";
		infoMessages[++ix] = "Movement statistics :";
		infoMessages[++ix] = "";
		infoMessages[++ix] = spaces + spaces + "Duration :";
		infoMessages[++ix] = spaces + spaces + spaces + String.format("North  : %d ms", durationToNorth);
		infoMessages[++ix] = spaces + spaces + spaces + String.format("West    : %d ms", durationToWest);
		infoMessages[++ix] = spaces + spaces + spaces + String.format("South  : %d ms", durationToSouth);
		infoMessages[++ix] = spaces + spaces + spaces + String.format("East     : %d ms", durationToEast);
		infoMessages[++ix] = spaces + spaces + spaces + String.format("TOTAL : %d ms", totalDuration);
		infoMessages[++ix] = "";
		infoMessages[++ix] = spaces + spaces + "Rotations :";
		infoMessages[++ix] = spaces + spaces + spaces + String.format("               Clockwise : %d", rotationsClockWise);
		infoMessages[++ix] = spaces + spaces + spaces + String.format("Counter-clockwise : %d", rotationsCounterClockWise);
		infoMessages[++ix] = spaces + spaces + spaces + String.format("                     TOTAL : %d", totalRotations);
		
		return infoMessages;
	}
	
	private ANGLE_QUARTER getDirectionQuarter(Move move) {
    	float[] startVec = {0.0f , -1.0f};
    	float[] origin = {0.0f, 0.0f};
		
		float[] vec = Vector2D.getDrawPoint(startVec, move.direction, 1.0f, origin);
		
    	final float SIGN_POSITIVE = +1.0f;
    	final float SIGN_NEGATIVE = -1.0f;
    	
    	boolean isNorthWest;
    	boolean isWestSouth;
    	boolean isSouthEast;
    	boolean isEastNorth;
				
		float xSign = Math.signum(vec[0]);
		float ySign = Math.signum(vec[1]);
		xSign = (xSign == 0.0f)? SIGN_POSITIVE : xSign;
		ySign = (ySign == 0.0f)? SIGN_POSITIVE : ySign;
		
		isNorthWest = (ySign == SIGN_NEGATIVE && xSign == SIGN_POSITIVE);
		isWestSouth = (ySign == SIGN_POSITIVE && xSign == SIGN_POSITIVE);
		isSouthEast = (ySign == SIGN_POSITIVE && xSign == SIGN_NEGATIVE);
		isEastNorth = (ySign == SIGN_NEGATIVE && xSign == SIGN_NEGATIVE);
		
		if (isNorthWest)
			return ANGLE_QUARTER.NORTH_WEST;
		
		if (isWestSouth)
			return ANGLE_QUARTER.WEST_SOUTH;
		
		if (isSouthEast)
			return ANGLE_QUARTER.SOUTH_EAST;
		
		if (isEastNorth)
			return ANGLE_QUARTER.EAST_NORTH;
		
		return ANGLE_QUARTER.UNDEFINED;
	}
	
	private void updateMoveDurationStatistics(LinkedList<Move> moves, int index) {
    	float[] startVec = {0.0f , -1.0f};
    	float[] origin = {0.0f, 0.0f};
		
		Move move = moves.get(index);
		ANGLE_QUARTER quarter = getDirectionQuarter(move);
		float[] vec = Vector2D.getDrawPoint(startVec, move.direction, move.durationInMilliSec, origin);
		
		durationToNorth += (quarter == ANGLE_QUARTER.NORTH_WEST || quarter == ANGLE_QUARTER.EAST_NORTH)? (long)Math.abs(vec[1]) : 0;
		durationToSouth += (quarter == ANGLE_QUARTER.WEST_SOUTH || quarter == ANGLE_QUARTER.SOUTH_EAST)? (long)Math.abs(vec[1]) : 0;
		durationToWest  += (quarter == ANGLE_QUARTER.NORTH_WEST || quarter == ANGLE_QUARTER.WEST_SOUTH)? (long)Math.abs(vec[0]) : 0;
		durationToEast  += (quarter == ANGLE_QUARTER.SOUTH_EAST || quarter == ANGLE_QUARTER.EAST_NORTH)? (long)Math.abs(vec[0]) : 0;
		
	}

	private void updateMoveRotationStatistics(LinkedList<Move> moves, int index) {
		if (index == 0)
			return;
		
		int dirCurrent =  moves.get(index).direction + 180;
		int dirPrevious = moves.get(index - 1).direction + 180;
		int dirChange = dirCurrent - dirPrevious;
		
		Boolean isClockWise = null;
		
		if (dirChange > 0)
			isClockWise = true;
		else if (dirChange < 0)
			isClockWise = false;
		
		if (isClockWise != null) {
			if (isClockWise == true)
				rotationsClockWise += 1;
			else
				rotationsCounterClockWise += 1;
		}
		
	}
	
	public void calculateMovementStatistics(LinkedList<Move> moves) {
		
		durationToEast = 0;
		durationToNorth = 0;
		durationToSouth = 0;
		durationToWest = 0;
		rotationsClockWise = 0;
		rotationsCounterClockWise = 0;
		
		if (moves == null || moves.size() == 0)
			return;
		    	
		for (int i = 0; i < moves.size(); i++) {
			
			updateMoveDurationStatistics(moves, i);
			
			updateMoveRotationStatistics(moves, i);
		}
		
	}
	
}

public class Movement {
	
	private LinkedList<Move> moves;
	private Orientation orientation;
	private boolean isMovement;
	private boolean isMapRotatedAccordingToCompass;
	
	private final long MIN_DURATION_FOR_MOVE_IN_MILLISECONDS = 100;
	private final int DISCREET_CHANGE = 10;
	final int DEGREES_TO_NORTH_EPSILON = DISCREET_CHANGE / 2;
	
	private int lastDirection;
	private long lastTimeInMilliSec;
	private int oldDegreesToNorth;
	private int oldDegreesToNorthForMapRotate;
	
	private String[] messages;
	
	private float pixels_for_1_milliSecond;
	private final float SCALE_LOWER_BOUND = 0.0005f;
	private final float SCALE_UPPER_BOUND = SCALE_LOWER_BOUND * 100.0f;
	
	private MovementStatistics movementStats;
	private boolean isMovementStatisticsNeeded;
	
	public Movement(Orientation orientation) {
		this.isMovementStatisticsNeeded = false;
		this.movementStats = new MovementStatistics();
		this.orientation = orientation;
		this.isMovement = false;
		this.isMapRotatedAccordingToCompass = false;
		this.messages = new String[2];
		this.moves = null;
		this.pixels_for_1_milliSecond = SCALE_LOWER_BOUND + (SCALE_UPPER_BOUND - SCALE_LOWER_BOUND) / 2;
	}
	
	public void setIsMovementStatisticsNeeded(boolean statsNeeded) {
		this.isMovementStatisticsNeeded = statsNeeded;
	}
	
	public boolean getIsMovementStatisticsNeeded() {
		return this.isMovementStatisticsNeeded;
	}
	
	public void setIsMapRotatedAccordingToCompass(boolean rotateMap) {
		this.isMapRotatedAccordingToCompass = rotateMap;
	}
	
	public boolean getIsMapRotatedAccordingToCompass() {
		return this.isMapRotatedAccordingToCompass;
	}
	
	public String getMessage(boolean first) {
		return messages[ ((first)? 0 : 1) ];
	}
	
	public void setPixels_for_1_second(boolean increaseScale) {
		final float SCALE_CHANGE = 4.0f * SCALE_LOWER_BOUND;
		float sign = (increaseScale) ? +1.0f : -1.0f;
		this.pixels_for_1_milliSecond += SCALE_CHANGE * sign;
		this.pixels_for_1_milliSecond = Vector2D.Clamp(this.pixels_for_1_milliSecond, SCALE_LOWER_BOUND, SCALE_UPPER_BOUND);
		messages = new String[] {"Info:", String.format("%d millisecs for 1 pixel", 1+(int)(1.0f / pixels_for_1_milliSecond) )};
	}
	
	public void StartMovement() {
		if (orientation.getCalibrationRangeValues() == null) {
			messages = new String[] {"ERROR:", "Magnetometer not calibrated !"};
			return;
		}
		
		this.oldDegreesToNorth = -1000;
		this.oldDegreesToNorthForMapRotate = -1000;
		this.moves = new LinkedList<>();
		isMovement = true;
	}
	
	public boolean movementWasStarted() {
		return this.moves != null;
	}
	
	public void StopMovement() {
		isMovement = false;
	}
	
	public void ResetMovement() {
		isMovement = false;
		moves = null;
	}
	
	public void ResumeMovement()  {
		isMovement = true;
		long timeInMilliSec = System.currentTimeMillis();
		lastTimeInMilliSec = timeInMilliSec;
	}
	
	public boolean getIsMovement() {
		return isMovement;
	}
	
	private int getDiscreetAngle() {
		int degrees = getAveragedDirectionWithEpsilon();
		int sign = (degrees >= 0)? +1 : -1;
		float degreesMagnitude = Math.abs( (float)degrees / (float)DISCREET_CHANGE);
		int discreetAngle = sign * DISCREET_CHANGE * Math.round(degreesMagnitude);
		return -discreetAngle;
	}
	
	private int getAveragedDirectionWithEpsilon() {

		int degreesToNorth = getAveragedDirection();
		float degreesChange = Math.abs(degreesToNorth - oldDegreesToNorth);
		degreesToNorth = (degreesChange >= DEGREES_TO_NORTH_EPSILON) ? degreesToNorth : oldDegreesToNorth;
		oldDegreesToNorth = degreesToNorth;
		
		return degreesToNorth;
	}
	
	private int getAveragedDirection() {
		final int DIRECTION_POINTS = 100;
		int[] directions = new int[DIRECTION_POINTS];
		
		for (int i = 0; i < directions.length; i++) {
			directions[i]= orientation.getLastDegreesToNorthPole();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		
		int avgPos = 0;
		int avgNeg = 0;
		int countPos = 0;
		int countNeg = 0;
		
		// calculating averages of negative & positive angles
		for (int i = 0; i < directions.length; i++) {
			if (directions[i] >= 0) {
				countPos++;
				avgPos += directions[i];
			}
			else {
				countNeg++;
				avgNeg += directions[i];
			}
		}
		
		if (countNeg > 0)
			avgNeg /= countNeg;
		if (countPos > 0)
			avgPos /= countPos;
		
		int dir;
		
		if (countNeg == 0)
			dir =  avgPos;
		else if (countPos == 0)
			dir =  avgNeg;
		else {
			final int ANGLE_FROM_AXIS_THRESHOLD = 80;
			if (avgPos <= ANGLE_FROM_AXIS_THRESHOLD)
				dir =  +0;
			else if (avgPos >= 180 - ANGLE_FROM_AXIS_THRESHOLD)
				dir =  +180;
			else {
				if (countPos > countNeg)
					dir =  avgPos;
				else
					dir =  avgNeg;
			}
				
		}
		
		return dir;

	}

	public void CheckMovement() {
		if (moves == null)
			return;
		
		if (!isMovement)
			return;
		
		long timeInMilliSec = System.currentTimeMillis();
		int direction = getDiscreetAngle();
		
		if (moves.size() == 0) {
			moves.add(new Move(direction, 0));
			lastDirection = direction;
			lastTimeInMilliSec = timeInMilliSec;
		}
		else {
			long timeDelta = timeInMilliSec - lastTimeInMilliSec;
			
			if (timeDelta >= MIN_DURATION_FOR_MOVE_IN_MILLISECONDS) {
				moves.add(new Move(lastDirection, timeDelta));
				lastDirection = direction;
				lastTimeInMilliSec = timeInMilliSec;
			}
		}
	}
	
	private LinkedList<Float[]> convertMovesToPoints(float[] startPoint) {

    	float[] startVec = {0.0f , -1.0f};
    	
    	// rotate map according to compass if user wants this
    	if (this.isMapRotatedAccordingToCompass) {
        	int degreesNorth = orientation.getLastDegreesToNorthPole();
    		float degreesChange = Math.abs(degreesNorth - oldDegreesToNorthForMapRotate);
    		degreesNorth = (degreesChange >= DEGREES_TO_NORTH_EPSILON) ? degreesNorth : oldDegreesToNorthForMapRotate;
    		oldDegreesToNorthForMapRotate = degreesNorth;
        	
        	startVec = Vector2D.RotateVector(startVec, degreesNorth);
        	startVec = Vector2D.Normalize(startVec);
    	}
    	
    	// convert moves to X,Y coordinates
		LinkedList<Float[]> points = new LinkedList<>();
		points.add(new Float[] {startPoint[0], startPoint[1]});
		
		float[] lastPoint = startPoint;
		for (int i = 1; i < moves.size(); i++) {
			Move move = moves.get(i);
			float[] drawPoint = Vector2D.getDrawPoint(startVec, move.direction, move.durationInMilliSec * pixels_for_1_milliSecond, lastPoint);
			points.add(new Float[] {drawPoint[0], drawPoint[1]});
			lastPoint = drawPoint;
		}
		
		return points;
	}
	
	private float[] getMinMaxCoordsOfPoints(LinkedList<Float[]> points) {
		float[] limits = new float[4];

		final float limit = (float) 1e+10;
		limits[0] = +limit; // min X
		limits[1] = -limit; // max X
		limits[2] = +limit; // min Y
		limits[3] = -limit; // max Y
		
		for (Float[] point : points) {
			if (point[0] < limits[0])
				limits[0] = point[0];
			if (point[0] > limits[1])
				limits[1] = point[0];
			
			if (point[1] < limits[2])
				limits[2] = point[1];
			if (point[1] > limits[3])
				limits[3] = point[1];
		}		
		
		return limits;
	}
	
	private void centerPathToScreen(LinkedList<Float[]> points, float[] startPoint, float[] limits) {
		
		float minX = limits[0];
		float maxX = limits[1];
		float minY = limits[2];
		float maxY = limits[3];
		
		float[] planCenter = new float[] {minX + (maxX - minX)/2.0f, minY + (maxY - minY)/2.0f};
		Float[] shift = new Float[] {startPoint[0] - planCenter[0], startPoint[1] - planCenter[1]};
		
		// translate coordinates so that plan center would move to canvas center
		for (int i = 0; i < points.size(); i++) {
			Float[] oldPoint = points.get(i);
			Float[] newPoint = Vector2D.Add(oldPoint, shift);
			points.set(i, newPoint);
		}
		
	}
	
	private Path convertPointsToPath(LinkedList<Float[]> points) {
		Path path = new Path();
		
		for (int i = 0; i < points.size(); i++) {
			Float[] coords = points.get(i);
			if (i==0)
				path.moveTo(coords[0], coords[1]);
			else
				path.lineTo(coords[0], coords[1]);
		}
		
		return path;
	}
	
	private LinkedList<Float[]> filterOutLinesWithSmallAngleChange(LinkedList<Float[]> points) {
		
		if (points == null || points.size() < 3)
			return points;
		
		LinkedList<Float[]> filteredPoints = new LinkedList<>();
		filteredPoints.add(points.get(0));
		filteredPoints.add(points.get(1));
		final int DEGREES_CHANGE = 20;
		
		for (int i = 2; i < points.size(); i++) {
			Float[] lastPoint = filteredPoints.get(filteredPoints.size() - 1);
			Float[] rel1 = Vector2D.Subtract(lastPoint, filteredPoints.get(filteredPoints.size() - 2));
			Float[] rel2 = Vector2D.Subtract(points.get(i), lastPoint);
			int angleInDegrees = Vector2D.angleBetweenTwoVectors(rel1, rel2);
			if (angleInDegrees >= DEGREES_CHANGE)
				filteredPoints.add(points.get(i));
			else
				filteredPoints.set(filteredPoints.size() - 1, points.get(i));
		}
		
		return filteredPoints;
	}
	
	public Object[] getMovementDataForDraw(float[] startPoint) {
    	
		if (moves == null || moves.size() < 2)
			return null;
		
		if (this.isMovementStatisticsNeeded)
			this.movementStats.calculateMovementStatistics(moves);

		LinkedList<Float[]> points = convertMovesToPoints(startPoint);
		
		float[] limits = getMinMaxCoordsOfPoints(points);
		
		centerPathToScreen(points, startPoint, limits);
		
		LinkedList<Float[]> pointsFiltered = filterOutLinesWithSmallAngleChange(points);
		
		Path path = convertPointsToPath(pointsFiltered);
		
		Object[] ret = new Object[3];
		ret[0] = path;
		ret[1] = pointsFiltered.getLast();
		ret[2] = this.movementStats;
		
		return ret;
	}
	
}
