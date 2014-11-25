package com.pathrecorder;

import android.graphics.Bitmap;

public class ProgramState {
	
	public enum State {
		STATE_UNDEFINED,
		STATE_PAUSE_NO_MENU,
		STATE_PAUSE_WITH_MENU,
		STATE_MOVEMENT_START,
		STATE_MOVEMENT_RESUME,
		STATE_CALIBRATION,
		STATE_ROTATE_MAP,
		STATE_MOVEMENT_STATISTICS,
		STATE_ZOOM_IN,
		STATE_ZOOM_OUT,
		STATE_MENU_ITEM_CHANGE,
		STATE_EXPORT_MOVEMENT_PATH
	}
	
	private State state;
	private SketchView sketchView;
	private Orientation orientation;
	private MenuManager menuManager;
	private Storage storage;
	private Movement movement;
	
	public ProgramState(SketchView view, Orientation orientation, Storage storage, Movement movement) {
		this.state = State.STATE_UNDEFINED;
		this.sketchView = view;
		this.orientation = orientation;
		this.storage = storage;
		this.movement = movement;
		this.menuManager = null;
	}
	
	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}
	
	public void checkMovementState() {
		movement.CheckMovement();
	}
	
	public void checkExportBitmapState() {

		if (state == State.STATE_EXPORT_MOVEMENT_PATH) {
			
			if (sketchView.getBitmapIsExported()) {
				setProgramState(State.STATE_PAUSE_WITH_MENU);
			}
		}

	}	
	
	public void checkCalibrationStateChange() {
		
		if (state == State.STATE_CALIBRATION) {
			if (!orientation.getIsCalibration()) {
				setProgramState(State.STATE_PAUSE_WITH_MENU);
			}
		}
	}
	
	public synchronized void setProgramState(State state) {

		if (menuManager == null)
			throw new RuntimeException("Set menu manager first !");

		State oldState = this.state;
		this.state = state;

		// business logics of application
		
		switch (state) {
		
			case STATE_PAUSE_WITH_MENU:
				
				if (movement.movementWasStarted()) {
					
					if (movement.getIsMovement())
						movement.StopMovement();					
					
					menuManager.menuSwitchItem(State.STATE_MOVEMENT_RESUME);
				}
				
				if (oldState == State.STATE_PAUSE_NO_MENU)
					sketchView.setMessages(null, null);
				
				if (oldState == State.STATE_CALIBRATION)
					storage.saveCalibrationData();
				
				if (oldState == State.STATE_EXPORT_MOVEMENT_PATH) {
					Bitmap bExportedPath = sketchView.getExportBitmap();
					String outFileName = storage.saveBitmap(bExportedPath);
					if (outFileName != null)
						sketchView.setMessages("Info:", String.format("Path exported to file '%s'", outFileName));
					else
						sketchView.setMessages("Error:", "Disconnect storage from PC !");
				}
				
				menuManager.setIsMenuVisible(true);

				break;
			
			case STATE_PAUSE_NO_MENU:
				menuManager.setIsMenuVisible(false);
				sketchView.setMessages("PAUSE", "Tap on screen for menu");
				break;

			case STATE_MOVEMENT_START:
				menuManager.setIsMenuVisible(false);
				sketchView.setMessages("Movement:", "Tap on screen for menu");
				movement.StartMovement();
				break;

			case STATE_MOVEMENT_RESUME:
				if (movement.movementWasStarted()) {
					menuManager.setIsMenuVisible(false);
					sketchView.setMessages("Movement:", "Tap on screen for menu");
					movement.ResumeMovement();
				}
				else {
					sketchView.setMessages("ERROR:", "Movement was not started !");
				}
				break;				

			case STATE_ROTATE_MAP:
				menuManager.setIsMenuVisible(false);
				boolean rotateMap = !movement.getIsMapRotatedAccordingToCompass();
				movement.setIsMapRotatedAccordingToCompass(rotateMap);
				String line2 = "Map auto-rotation is " + ((rotateMap) ? "ON" : "OFF");
				sketchView.setMessagesWithTimeout("Info:", line2);
				break;
				
			case STATE_MOVEMENT_STATISTICS:
				menuManager.setIsMenuVisible(false);
				boolean showMovementStats = !movement.getIsMovementStatisticsNeeded();
				movement.setIsMovementStatisticsNeeded(showMovementStats);
				String line2forStats = "Movements statistics is " + ((showMovementStats) ? "ON" : "OFF");
				sketchView.setMessagesWithTimeout("Info:", line2forStats);
				break;
				
			case STATE_CALIBRATION:
				menuManager.setIsMenuVisible(false);
				orientation.resetCalibration();
				sketchView.setDegreesToNorth(-1000); // do not paint compass if calibration is on line
				break;

			case STATE_EXPORT_MOVEMENT_PATH:
				menuManager.setIsMenuVisible(false);
				sketchView.exportPathToBitmap(true);
				break;
				
			case STATE_ZOOM_IN:
				menuManager.setIsMenuVisible(false);
				movement.setPixels_for_1_second(true);
				sketchView.setMessages(movement.getMessage(true), movement.getMessage(false));
				break;

			case STATE_ZOOM_OUT:
				menuManager.setIsMenuVisible(false);
				movement.setPixels_for_1_second(false);
				sketchView.setMessages(movement.getMessage(true), movement.getMessage(false));
				break;
				
			case STATE_MENU_ITEM_CHANGE:
				sketchView.setMessages(null, null);
				break;
	
			default:
				sketchView.setMessages("ERROR:", "Not implemented !");
				break;
			
		}
		
	}
	
	public State getProgramState() {
		return state;
	}
	
}
