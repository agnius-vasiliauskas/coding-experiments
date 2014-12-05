package com.pingpong;

import com.pingpong.Game.GAME_RESULT;

public class ProgramState {
	
	public enum State {
		STATE_UNDEFINED,
		STATE_GAME_NO_GAME,
		STATE_GAME_PROCESS,
		STATE_GAME_END
	}
	
	private State state;
	private Game game;
	private SketchView sketchView;
	
	public ProgramState(SketchView view) {
		this.state = State.STATE_UNDEFINED;
		this.sketchView = view;
	}
	
	public void setGameObject(Game game) {
		this.game = game;
	}
	
	public void updateGameState() {
		if (this.game != null && this.state == State.STATE_GAME_PROCESS) {
			
			if (this.game.getGameResult() != GAME_RESULT.NONE) {
				setProgramState(State.STATE_GAME_END);
				return;
			}
			
			this.game.updateGame();
			this.sketchView.invalidate();
		}
	}
	
	public synchronized void setProgramState(State state) {

		this.state = state;

		// business logics of application
		
		switch (state) {
		
			case STATE_GAME_NO_GAME:
				sketchView.setMessages("Tap on screen", "to start game !");
				break;
				
			case STATE_GAME_END:
				GAME_RESULT res = this.game.getGameResult();
				String resMess = String.format("You %s", ((res == GAME_RESULT.WIN)? "win" : "loose"));
				sketchView.setMessages(resMess, "Tap to start new game !");
				sketchView.invalidate();
				break;

			case STATE_GAME_PROCESS:
				if (!game.isDemo) 
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
