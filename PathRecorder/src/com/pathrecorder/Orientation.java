package com.pathrecorder;

import android.hardware.*;
import java.util.*;

public class Orientation implements SensorEventListener {

    private SketchView sketchView;

    // constants
    private final int MIN_CALIBRATION_POINTS = 10;
    private final int MAGNETOMETER_MAX_EVENTS = 1_000;
    private final int MAGNETOMETER_ACCURACY_FACTOR = 6;
    private final int DEGREES_TO_NORTH_EPSILON = 5;

    private boolean magnetometerCalibrationPhase_1_Performed;
    private boolean magnetometerCalibrationPhase_2_Performed;
    private float[] oldMagnetometerValues;
    private float magnetometerAccuracyX;
    private float magnetometerAccuracyY;    
    private int magnetometerEventCounter;
    private LinkedList<Float> calibrationXvalues;
    private LinkedList<Float> calibrationYvalues;
    private float[] calibrationRangeValues;
    private int oldDegreesToNorthForCompass;
    private int oldDegreesToNorthForMovement;

    
    private boolean isCalibration = false;
    private boolean isMovement = false;
    
    private void InitOrientation() {
    	
    	this.calibrationRangeValues = null;
    	
        this.magnetometerCalibrationPhase_1_Performed = false;
        this.magnetometerCalibrationPhase_2_Performed = false;
        
        this.magnetometerAccuracyX = 0.0f;
        this.magnetometerAccuracyY = 0.0f;
        this.magnetometerEventCounter = 0;    	
        
        calibrationXvalues = new LinkedList<>();
        calibrationYvalues = new LinkedList<>();
        oldDegreesToNorthForCompass = -1000;
        oldDegreesToNorthForMovement = -1000;
    }
    
    public Orientation(SketchView sketchView) {
    	this.sketchView = sketchView;
    	InitOrientation();
    }
    
    public float[] getCalibrationRangeValues()  {
    	return calibrationRangeValues;
    }

    public void setCalibrationRangeValues(float[] values)  {
    	this.calibrationRangeValues = values;
    }    
    
    public void setIsCalibration(boolean isCalibration) {
    	this.isCalibration = isCalibration;
    }
    
    public void setIsMovement(boolean isMovement) {
    	this.isMovement = isMovement;
    }
    
    public boolean getIsCalibration() {
    	return this.isCalibration;
    }
    
    public boolean getIsMovement() {
    	return this.isMovement;
    }
    
    private float[] findCalibrationMinMaxValues(LinkedList<Float> calibrationXvalues, LinkedList<Float> calibrationYvalues) {
    	float[] calibrationData = new float[4];

    	final float max_value = (float) 1e+10;
    	calibrationData[0] = max_value;     // X min
    	calibrationData[1] = -max_value;    // X max
    	calibrationData[2] = max_value;     // Y min
    	calibrationData[3] = -max_value;    // Y max
    	
		for (float vX : calibrationXvalues) {
			if (vX > calibrationData[1])
				calibrationData[1] = vX;
			
			if (vX < calibrationData[0])
				calibrationData[0] = vX;
		}
		for (float vY : calibrationYvalues) {
			if (vY > calibrationData[3])
				calibrationData[3] = vY;
			
			if (vY < calibrationData[2])
				calibrationData[2] = vY;
		}				
    	
    	return calibrationData;
    }
    
    public void resetCalibration() {
    	InitOrientation();
    	this.isCalibration = true;
    }
    
    private void performMagnetometerCalibrationPhase_1(SensorEvent event) {
		
    	this.magnetometerEventCounter++;

    	if (this.magnetometerEventCounter > 1) {
    		
	    	this.magnetometerAccuracyX += Math.abs(oldMagnetometerValues[0] - event.values[0]);
	    	this.magnetometerAccuracyY += Math.abs(oldMagnetometerValues[1] - event.values[1]);			
			
			if (this.magnetometerEventCounter >= MAGNETOMETER_MAX_EVENTS) {
				this.magnetometerAccuracyX /= this.magnetometerEventCounter;
				this.magnetometerAccuracyY /= this.magnetometerEventCounter;
				this.magnetometerCalibrationPhase_1_Performed = true;
				this.magnetometerEventCounter = 0;
				this.oldMagnetometerValues = null;
				return;
			}
    	}
		
		oldMagnetometerValues = event.values;		
		
		// updating progress
		int percentsCal =  (int) (100.0f * ((float)this.magnetometerEventCounter / (float)this.MAGNETOMETER_MAX_EVENTS) );
		sketchView.setMessages("Magnetometer calibration:", String.format("Phase 1 (%d complete)", percentsCal));		
    }
    
    private void checkCalibrationPhase2ErrorCondition(float epsilonX, float epsilonY) {
    	    	
    	int sizeX = calibrationXvalues.size();
    	int sizeY = calibrationYvalues.size();
    	final float EPSILON_MULTIPLICATOR = 1.2f;
    	float avgdX = 0.0f;
    	float avgdY = 0.0f;
    	
    	if (sizeX >= MIN_CALIBRATION_POINTS && sizeY >= MIN_CALIBRATION_POINTS ) {
    		
    		for (int i = 1; i < sizeX; i++)
    			avgdX += Math.abs(calibrationXvalues.get(i) - calibrationXvalues.get(i-1));
    		
    		for (int i = 1; i < sizeY; i++)
    			avgdY += Math.abs(calibrationYvalues.get(i) - calibrationYvalues.get(i-1));
    		
    		avgdX /= sizeX - 1;
    		avgdY /= sizeY - 1;
    		
    		if (avgdX > EPSILON_MULTIPLICATOR * epsilonX) {
    			calibrationXvalues = new LinkedList<>();
    			calibrationYvalues = new LinkedList<>();
    			sketchView.setMessagesWithTimeout("You are turning too fast !", "Calibration phase 2 re-initiated...");
    			return;
    		}
    		
    		if (avgdY > EPSILON_MULTIPLICATOR * epsilonY) {
    			calibrationXvalues = new LinkedList<>();
    			calibrationYvalues = new LinkedList<>();
    			sketchView.setMessagesWithTimeout("You are turning too fast !", "Calibration phase 2 re-initiated...");
    			return;
    		}
    		
    	}
    	
    	
    }
    
    private void checkCalibrationPhase2Termination(float epsilonX, float epsilonY) {
		
		if (calibrationXvalues.size() >= MIN_CALIBRATION_POINTS && 
			    calibrationYvalues.size() >= MIN_CALIBRATION_POINTS &&
			    Math.abs(calibrationXvalues.getFirst() - calibrationXvalues.getLast()) <= epsilonX &&
			    Math.abs(calibrationYvalues.getFirst() - calibrationYvalues.getLast()) <= epsilonY
			    ) {
				
				
				calibrationRangeValues = findCalibrationMinMaxValues(calibrationXvalues, calibrationYvalues);
				calibrationXvalues = null;
				calibrationYvalues = null;
				
				this.magnetometerCalibrationPhase_2_Performed = true;
				this.isCalibration = false;
				sketchView.setMessages("Info:", "Calibration complete !");
				
			}
			else {
				sketchView.setMessages("Magnetometer calibration:", "Phase 2 (Slowly turn arround with phone)") ;
			}
    }

    private void performMagnetometerCalibrationPhase_2(SensorEvent event) {
		float[] magneticFieldAmplitude =  new float[3];
		
		if (oldMagnetometerValues != null) {
			
			float magFieldChangeX = Math.abs(event.values[0] - oldMagnetometerValues[0]);
			float magFieldChangeY = Math.abs(event.values[1] - oldMagnetometerValues[1]);
			
			float epsilonX = (float)MAGNETOMETER_ACCURACY_FACTOR * magnetometerAccuracyX;
			float epsilonY = (float)MAGNETOMETER_ACCURACY_FACTOR * magnetometerAccuracyY;
			
			magneticFieldAmplitude[0] = (magFieldChangeX > epsilonX) ? event.values[0] : oldMagnetometerValues[0];
			magneticFieldAmplitude[1] = (magFieldChangeY > epsilonY) ? event.values[1] : oldMagnetometerValues[1];
			magneticFieldAmplitude[2] = event.values[2];
			
			if (magneticFieldAmplitude[0] != oldMagnetometerValues[0])
				calibrationXvalues.add(magneticFieldAmplitude[0]);
			
			if (magneticFieldAmplitude[1] != oldMagnetometerValues[1])
				calibrationYvalues.add(magneticFieldAmplitude[1]);
			
			checkCalibrationPhase2ErrorCondition(epsilonX, epsilonY);
			
			checkCalibrationPhase2Termination(epsilonX, epsilonY);
		}
		
		oldMagnetometerValues = magneticFieldAmplitude;
    	
    }
    
    private int degreesToNorthPole(SensorEvent event) {
    	int degrees = -1000;
    	
    	// X max points to west, Y min points to North
    	
    	if (calibrationRangeValues != null) {
    		float XvalueClamped = Vector2D.Clamp(event.values[0], calibrationRangeValues[0], calibrationRangeValues[1]);
    		float YvalueClamped = Vector2D.Clamp(event.values[1], calibrationRangeValues[2], calibrationRangeValues[3]);
    		
    		float XdistToWest = Math.abs(calibrationRangeValues[1] - XvalueClamped);
    		float Xrange = Math.abs(calibrationRangeValues[1]-calibrationRangeValues[0]);

    		float YdistToNorth = Math.abs(calibrationRangeValues[2] - YvalueClamped);
    		float Yrange = Math.abs(calibrationRangeValues[3]-calibrationRangeValues[2]);

    		int degreesToEastWestAxis = (int) (180.0f * XdistToWest / Xrange);
    		int degreesToNorthSouthAxis = (int) (180.0f * YdistToNorth / Yrange);
    		
    		int sign = (degreesToEastWestAxis <= 90) ? -1 : +1;
    		degrees = sign * degreesToNorthSouthAxis;
    	}
    	
    	return degrees;
    }
    
    public int getLastDegreesToNorthPole() {
    	return oldDegreesToNorthForMovement;
    }
    
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (isCalibration) {		
			if (!magnetometerCalibrationPhase_1_Performed) {
				performMagnetometerCalibrationPhase_1(event);
			}
			else if (!magnetometerCalibrationPhase_2_Performed) {
				performMagnetometerCalibrationPhase_2(event);
			}
		}
		else {
			
			if (calibrationRangeValues != null) {
				int degreesToNorth = degreesToNorthPole(event);
				oldDegreesToNorthForMovement = degreesToNorth;
				float degreesChange = Math.abs(degreesToNorth - oldDegreesToNorthForCompass);
				degreesToNorth = (degreesChange >= DEGREES_TO_NORTH_EPSILON) ? degreesToNorth : oldDegreesToNorthForCompass;
				oldDegreesToNorthForCompass = degreesToNorth;
				
				sketchView.setDegreesToNorth(degreesToNorth);
			}
			
		}
        
		sketchView.invalidate();
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
