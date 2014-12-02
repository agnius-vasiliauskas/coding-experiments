package com.pingpong;

public class ProgramState {
	
	public enum State {
		STATE_UNDEFINED,
		STATE_GAME_NO_GAME,
		STATE_GAME_PROCESS,
		STATE_GAME_END_WIN,
		STATE_GAME_END_LOOSE
	}
	
	private State state;
	private Game game;
	private SketchView sketchView;
	
	public ProgramState(SketchView view, Game game) {
		this.state = State.STATE_UNDEFINED;
		this.sketchView = view;
		this.game = game;
	}
	
	public void updateGameState() {
		if (this.state == State.STATE_GAME_PROCESS) {
			this.game.updateGame();
			this.sketchView.invalidate();
		}
	}
	
	public synchronized void setProgramState(State state) {

		State oldState = this.state;
		this.state = state;

		// business logics of application
		
		switch (state) {
		
			case STATE_GAME_NO_GAME:
				sketchView.setMessages("Tap on screen", "to start game !");
				break;

			case STATE_GAME_PROCESS:
				sketchView.setMessages(null, null);
				sketchView.setGameMode(true);
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
