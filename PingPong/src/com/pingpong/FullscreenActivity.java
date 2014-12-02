package com.pingpong;

import com.pingpong.ProgramState.State;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.*;
import android.graphics.*;
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
    private ProgramState programState = null;
    private Game game = null;

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
        
        // Our application only supports drawing to landscape canvas orientation
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
	    	sketchView.setBeginDrawing(true);
	    else
		    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    
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
        
        setContentView(sketchView);    	
        
        // setup game
        Bitmap ball = getBitmapResource(0);
        Bitmap racket = getBitmapResource(3);
        Bitmap block = getBitmapResource(1);
        
        displaySize = new Point(displaySize.y, displaySize.x);
        game = new Game(displaySize, ball, racket, block);
    }
    
    private void setupUiInteraction() {
    	
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        sketchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                	
//                	float[] coords = {event.getX(), event.getY()};
                	if (programState.getProgramState() == State.STATE_GAME_NO_GAME)
                		programState.setProgramState(State.STATE_GAME_PROCESS);
                	
                	sketchView.invalidate();
                }
                
                return true;
            }
        });
        
    }
    
    private void beginGameStateMonitoring() {
    	
        new Thread() {
            public void run() {
            	while (true) {
            		
             		try {
                		Thread.sleep(10);	            		
					} catch (InterruptedException e) {
					}
            		
        			runOnUiThread(new Runnable() {
       			     @Override
       			     public void run() {
       			    	programState.updateGameState();
       			    }
       			});

            	}
            }  
        }.start();
        
    }    
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    	sketchView.setBeginDrawing(true);
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupSketchView();
                	
        // program setup
        
    	programState = new ProgramState(sketchView, this.game);
    	                
        programState.setProgramState(State.STATE_GAME_NO_GAME);

        setupUiInteraction();
        
        sketchView.setGame(this.game);
        sketchView.invalidate();
                
        beginGameStateMonitoring();
        
    }
    
}
