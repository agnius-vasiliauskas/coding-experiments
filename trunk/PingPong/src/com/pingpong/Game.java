package com.pingpong;

import java.util.Arrays;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Point;

public class Game {

	public int BLOCKS_IN_ROW;
	public int BLOCKS_IN_COLUMN;
	public int STATUS_BAR_HEIGHT;
	
	private Point displaySize;
	private Random random;
	
	public boolean[][] blocks = null;
	public float[] ballPosition;
	public float[] racketPosition;
	
	public int lives;
	public int points;
	
	public Bitmap ball;
	public Bitmap racket;
	public Bitmap block;
	
	public final float paddingX = 20.0f;
	public final float paddingY = 40.0f;
	
	private float[] ballDirection;
	private float   ballSpeed;
	
	private final float[] ZERO_VECTOR = new float[] {0.0f, 0.0f};
	
	private void generateLeftTopQuarterOfBlocks() {
		
		for (int row=0; row < BLOCKS_IN_COLUMN/2; row++) {
			
			for (int column=0; column < BLOCKS_IN_ROW/2; column++) {
				blocks[row][column] = (random.nextInt(101) <= 80 )? true : false;
			}
			
		}		
	}

	private void generateRightTopQuarterOfBlocks() {
		
		for (int row=0; row < BLOCKS_IN_COLUMN/2; row++) {
			
			for (int column=BLOCKS_IN_ROW/2; column < BLOCKS_IN_ROW; column++) {
				blocks[row][column] = blocks[row][BLOCKS_IN_ROW - (column + 1)];
			}
			
		}		
	}	

	private void generateRightBottomQuarterOfBlocks() {
		
		for (int row=BLOCKS_IN_COLUMN/2; row < BLOCKS_IN_COLUMN; row++) {
			
			for (int column=BLOCKS_IN_ROW/2; column < BLOCKS_IN_ROW; column++) {
				blocks[row][column] = blocks[BLOCKS_IN_COLUMN - (row + 1)][column];
			}
			
		}		
	}	

	private void generateLeftBottomQuarterOfBlocks() {
		
		for (int row=BLOCKS_IN_COLUMN/2; row < BLOCKS_IN_COLUMN; row++) {
			
			for (int column=0; column < BLOCKS_IN_ROW/2; column++) {
				blocks[row][column] = blocks[BLOCKS_IN_COLUMN - (row + 1)][column];
			}
			
		}		
	}	

	
	public Game(Point displaySize, Bitmap ball, Bitmap racket, Bitmap block) {
		STATUS_BAR_HEIGHT = 25;
		
		ballDirection = new float[] {-1, -1};
		ballSpeed = 1.0f;
		
		random = new Random();
		
		lives = 3;
		points = 0;
		
		BLOCKS_IN_ROW = (displaySize.x - 4 * (int)paddingX) / block.getWidth();
		BLOCKS_IN_COLUMN = (displaySize.y - 2 * (int)paddingY - 2 * racket.getHeight() - 2 * ball.getHeight()) / block.getHeight();

		BLOCKS_IN_ROW = (BLOCKS_IN_ROW % 2 == 0)? BLOCKS_IN_ROW : BLOCKS_IN_ROW - 1;
		BLOCKS_IN_COLUMN = (BLOCKS_IN_COLUMN % 2 == 0)? BLOCKS_IN_COLUMN : BLOCKS_IN_COLUMN - 1;		
		
		blocks = new boolean[BLOCKS_IN_COLUMN][BLOCKS_IN_ROW];
		
		this.displaySize = displaySize;
		this.ball = ball;
		this.racket = racket;
		this.block = block;
		
		generateLeftTopQuarterOfBlocks();
		generateRightTopQuarterOfBlocks();
		generateRightBottomQuarterOfBlocks();
		generateLeftBottomQuarterOfBlocks();
		
		racketPosition = new float[] {displaySize.x / 2 - racket.getWidth() / 2 - paddingX, displaySize.y -  racket.getHeight() - paddingY};
		ballPosition = new float[] {racketPosition[0] + racket.getWidth() / 2 - ball.getWidth() / 2, racketPosition[1] - ball.getHeight()};
	}
	
	private float[] getCollidingBorderVector(float[] oldPosition, float[] newPosition) {
		float[] vec = Arrays.copyOf(ZERO_VECTOR, 2);
		float dX = newPosition[0] - oldPosition[0];
		float dY = newPosition[1] - oldPosition[1];
		
		final float RIGHT_FIX = 50.0f;
		
		// gets out to the left o right
		if (newPosition[0] < 0.0f || newPosition[0] > displaySize.x - RIGHT_FIX - ball.getWidth()) {
			vec[0] = 0.0f;
			vec[1] = (dY > 0)? +1.0f : -1.0f;
		}
		// gets out to bottom or top
		else if (newPosition[1] < 0.0f || newPosition[1] > displaySize.y - STATUS_BAR_HEIGHT - ball.getHeight()) {
			vec[0] = (dX > 0)? +1.0f : -1.0f;
			vec[1] = 0.0f;			
		}
		
		return vec;
	}
	
	private boolean updateBallDirectionOnCollisionWithWall(float[] oldPosition, float[] newPosition) {
		float[] borderVector = getCollidingBorderVector(oldPosition, newPosition);
		float sign = 0.0f;
		float dXSign = Math.signum(newPosition[0] - oldPosition[0]);
		float dYSign = Math.signum(newPosition[1] - oldPosition[1]);
		boolean dirUpdated = false;
		
		if (!Arrays.equals(borderVector, ZERO_VECTOR)) {
			// gets out to the left o right
			if (borderVector[0] == 0.0f) {
				sign = (dXSign == dYSign)? +1.0f : -1.0f;
			}
			// gets out to top or bottom
			else if (borderVector[1] == 0.0f) {
				sign = (dXSign != dYSign)? +1.0f : -1.0f;
			}
			
			int angle = Vector2D.angleBetweenTwoVectors(borderVector, ballDirection);
			float[] newDirection = Vector2D.RotateVector(ballDirection, (int)sign * 2 * angle);
			newDirection = Vector2D.Normalize(newDirection);
			ballDirection = newDirection;
			
			dirUpdated = true;
		}
		
		return dirUpdated;
	}
	
	public void updateGame() {
		float[] oldPosition = ballPosition;
		float[] newPosition = Vector2D.Add(ballPosition, Vector2D.MultiplyScalar(ballDirection, ballSpeed));
		
		boolean dirChangedBecauseOfWall = updateBallDirectionOnCollisionWithWall(oldPosition, newPosition);
		
		if (!dirChangedBecauseOfWall)
			ballPosition = newPosition;
	}
}
