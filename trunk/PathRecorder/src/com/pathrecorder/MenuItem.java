package com.pathrecorder;

public class MenuItem {
		
	private String menuText1;
	private String menuText2;
	private ProgramState programState;
	private ProgramState.State menuItemState;
	
	public MenuItem(ProgramState programState, ProgramState.State menuItemState, String menuText1, String menuText2) {
		this.menuText1 = (menuText1 == null) ? "Text" : menuText1;
		this.menuText2 = (menuText2 == null) ? "Undefined" : menuText2;
		this.programState = programState;
		this.menuItemState = menuItemState;
	}
	
	public void setMenuText(String text1, String text2) {
		this.menuText1 = text1;
		this.menuText2 = text2;
	}
	
	public String getMenuText(boolean firstLine) {
		return (firstLine) ? menuText1 : menuText2;
	}
	
	public void executeMenuItem() {
		programState.setProgramState(menuItemState);
	}
	
	public ProgramState.State getMenuItemState() {
		return menuItemState;
	}

}
