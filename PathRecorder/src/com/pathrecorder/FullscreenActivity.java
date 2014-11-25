package com.pathrecorder;

import com.pathrecorder.ProgramState.State;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.*;
import android.graphics.*;
import android.hardware.*;
import android.os.*;
import android.view.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD) 
public class FullscreenActivity extends Activity {
	
    private SketchView sketchView = null;
    private Orientation magnetometerAnalyzer = null;
    private ProgramState programState = null;
    private MenuManager menuManager = null;
    private Storage storage = null;
    private Movement movement = null;

    public Bitmap getBitmapResource(int index) 
    {
        try {
        	final int addressOffset = 0x7f020000;
        	Bitmap bit = BitmapFactory.decodeResource(this.getResources(), addressOffset + index);
        	return bit;
        } catch (Exception e) {
        	return null;
        } 
    }
    
    @SuppressWarnings("deprecation")
	private void setupSketchView() {
        // creating view
        if (sketchView == null)
        	sketchView = new SketchView(getApplicationContext());

        // setting view fields
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();		
        Point displaySize = new Point();
        
        // Our application only supports drawing to portrait canvas orientation
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
	    	sketchView.setBeginDrawing(true);
	    else
		    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    
	    // get display size for aspect ratio
        try {
            display.getRealSize(displaySize);
        } catch (java.lang.NoSuchMethodError ex) { // Older device
        	displaySize.x = display.getWidth();
        	displaySize.y = display.getHeight();
        	
        	// account for orientation change
        	int min = Math.min(displaySize.x, displaySize.y);
        	int max = Math.max(displaySize.x, displaySize.y);
        	displaySize = new Point(min, max);
        	displaySize.y += displaySize.x * 0.16; // fix for status bar height in older API
        }        
		
        float aspectRatio = (float)displaySize.y / (float)displaySize.x;

		sketchView.setAspectRatio(aspectRatio);
        sketchView.setBitmapArrow(getBitmapResource(0));
        
        setContentView(sketchView);    	
    }
    
    private void setupUiInteraction() {
    	
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        sketchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                	
                	float[] coords = {event.getX(), event.getY()};
                	
                	if (menuManager.getIsMenuVisible())
                		menuManager.menuProcessEvents(coords);
                	else {
                		if (programState.getProgramState() != ProgramState.State.STATE_CALIBRATION &&
                			programState.getProgramState() != ProgramState.State.STATE_EXPORT_MOVEMENT_PATH
                		    )
                			programState.setProgramState(ProgramState.State.STATE_PAUSE_WITH_MENU);
                	}
                	
                	sketchView.invalidate();
                }
                return true;
            }
        });
        
    }
    
    private void beginCalibrationStateMonitoring() {
    	
        new Thread() {
            public void run() {
            	while (true) {
            		try {
                		programState.checkCalibrationStateChange();            		
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
            	}
            }  
        }.start();
        
    }

    private void beginMovementStateMonitoring() {
    	
        new Thread() {
            public void run() {
            	while (true) {
            		try {
                		programState.checkMovementState();            		
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
            	}
            }  
        }.start();
        
    }    

    private void beginExportPathBitmapMonitoring() {
    	
        new Thread() {
            public void run() {
            	while (true) {
            		try {
                		programState.checkExportBitmapState();            		
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
            	}
            }  
        }.start();
        
    }        
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	sketchView.setBeginDrawing(true);
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSketchView();
        
        // magnetometer setup
        
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor magnetometerSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetometerSensor == null)
        	throw new RuntimeException("No magnetometer available !");
        
    	magnetometerAnalyzer = new Orientation(sketchView);
        sm.registerListener(magnetometerAnalyzer, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        	
        // program setup
        storage = new Storage(magnetometerAnalyzer, this);
        storage.loadCalibrationData();
        
        movement = new Movement(magnetometerAnalyzer);
        sketchView.setMovementObject(movement);
        
    	programState = new ProgramState(sketchView, magnetometerAnalyzer, storage, movement);
    	
    	menuManager = new MenuManager(programState);
        menuManager.setBitmapMenuArrow(getBitmapResource(2));
        menuManager.setBitmapMenuButton(getBitmapResource(3));
        
        sketchView.setMenuManager(menuManager);
        
        programState.setMenuManager(menuManager);
        programState.setProgramState(State.STATE_PAUSE_WITH_MENU);

        setupUiInteraction();
        
        sketchView.invalidate();
        
        beginCalibrationStateMonitoring();
        
        beginMovementStateMonitoring();
        
        beginExportPathBitmapMonitoring();
        
    }
    
}
