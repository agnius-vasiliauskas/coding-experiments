package com.pathrecorder;

import com.pathrecorder.ProgramState.State;

import android.graphics.Bitmap;

public class MenuManager {

	public enum MenuEventType {
		NONE,
		MOVE_TO_NEXT_ITEM,
		MOVE_TO_PREVIOUS_ITEM,
		EXECUTE_ITEM
	}

	private boolean isMenuVisible = false;
	
	private Bitmap bitmapMenuArrow;
	private Bitmap bitmapMenuButton;
	
	public float[] menuCoordsArrowUp;
	public float[] menuCoordsArrowDown;
	public float[] menuCoordsButton;
	
	private final int MENU_ITEM_COUNT = 9;
	private MenuItem[] menuItems = new MenuItem[MENU_ITEM_COUNT];
	private int menuItemCurrent = 0;
	
	private ProgramState programState;
	
	public MenuManager(ProgramState programState) {
		
		bitmapMenuArrow = null;
		bitmapMenuButton = null;
		menuCoordsArrowDown = null;
		menuCoordsArrowUp = null;
		menuCoordsButton = null;
		
		this.programState = programState;
		
		int mnuIx = -1;
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_PAUSE_NO_MENU, "Hide","menu");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_CALIBRATION ,"Magnetometer","calibration");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_MOVEMENT_START, "Start", "movement");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_MOVEMENT_RESUME, "Resume", "movement");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_ROTATE_MAP, "Set map", "auto-rotation");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_MOVEMENT_STATISTICS, "Show movement", "statistics");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_ZOOM_IN, "Zoom", "in");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_ZOOM_OUT, "Zoom", "out");
		menuItems[++mnuIx] = new MenuItem(programState, ProgramState.State.STATE_EXPORT_MOVEMENT_PATH, "Export", "movement path");
	}
	
	public void setIsMenuVisible(boolean isMenuVisible) {
		this.isMenuVisible = isMenuVisible;
	}
	
	public boolean getIsMenuVisible() {
		return this.isMenuVisible;
	}
		
	public void setBitmapMenuArrow(Bitmap bitmap) {
		this.bitmapMenuArrow = bitmap;
	}

	public void setBitmapMenuButton(Bitmap bitmap) {
		this.bitmapMenuButton = bitmap;
	}

	public Bitmap getBitmapMenuArrow() {
		return this.bitmapMenuArrow;
	}

	public Bitmap getBitmapMenuButton() {
		return this.bitmapMenuButton;
	}
	
	public void menuSwitchItem(ProgramState.State state) {
		for (int i=0; i < menuItems.length; i++)
			if (menuItems[i].getMenuItemState() == state) {
				menuItemCurrent = i;
				return;
			}
	}
		
	private void menuSwitchItem(boolean isNext) {
		if (isNext) {
			if (canSwitchNext())
				menuItemCurrent++;
		}
		else {
			if (canSwitchPrevious())
				menuItemCurrent--;			
		}
	}
	
	public boolean canSwitchNext() {
		return menuItemCurrent < MENU_ITEM_COUNT - 1;
	}
	
	public boolean canSwitchPrevious() {
		return menuItemCurrent > 0;
	}
		
	private boolean isHitEvent(float[] hitCoords, float[] menuItemCoords, Bitmap itemBitmap) {
		if (hitCoords[0] >= menuItemCoords[0] && hitCoords[0] <= menuItemCoords[0] + itemBitmap.getWidth() &&
			hitCoords[1] >= menuItemCoords[1] && hitCoords[1] <= menuItemCoords[1] + itemBitmap.getHeight() )
			return true;
		else
			return false;
	}
	
	private MenuEventType menuGetEventType(float[] hitCoords) {
		
		if (menuCoordsArrowDown == null || menuCoordsArrowUp == null || menuCoordsButton == null)
			return MenuEventType.NONE;
		
		if (isHitEvent(hitCoords, menuCoordsArrowUp, bitmapMenuArrow))
			return MenuEventType.MOVE_TO_PREVIOUS_ITEM;
		
		else if (isHitEvent(hitCoords, menuCoordsArrowDown, bitmapMenuArrow))
			return MenuEventType.MOVE_TO_NEXT_ITEM;
		
		else if (isHitEvent(hitCoords, menuCoordsButton, bitmapMenuButton))
			return MenuEventType.EXECUTE_ITEM;
		
		else
			return MenuEventType.NONE;
	}
	
	public void menuProcessEvents(float[] hitCoords) {
		MenuEventType event = menuGetEventType(hitCoords);
		
		switch (event) {
			case EXECUTE_ITEM:
				menuItems[menuItemCurrent].executeMenuItem();
				break;
			case MOVE_TO_NEXT_ITEM:
				menuSwitchItem(true);
				programState.setProgramState(State.STATE_MENU_ITEM_CHANGE);
				break;
			case MOVE_TO_PREVIOUS_ITEM:
				menuSwitchItem(false);
				programState.setProgramState(State.STATE_MENU_ITEM_CHANGE);
				break;
			default:
				break;
		}
	}
	
	public String getMenuText(boolean isFirstLine) {
		return menuItems[menuItemCurrent].getMenuText(isFirstLine);
	}
	
}
