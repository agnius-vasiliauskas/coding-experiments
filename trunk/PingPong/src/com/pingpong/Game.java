package com.pingpong;

import java.util.Arrays;
import java.util.Random;

import com.pingpong.Block.BLOCK_TYPE;
import com.pingpong.Vector2D.LINE_TYPE;

import android.graphics.Bitmap;
import android.graphics.Point;

class Block {
	
	public enum BLOCK_TYPE {
		GREEN,
		BLUE,
		RED,
		NONE
	}
		
	public static Bitmap bitmapBlockGreen;
	public static Bitmap bitmapBlockBlue;
	public static Bitmap bitmapBlockRed;

	public static final int PROBABILITY_OF_BLOCK = 80;
	public static final int PROBABILITY_OF_BLOCK_RED = 10;
	public static final int PROBABILITY_OF_BLOCK_BLUE = 30;	
	
	public float[] coords;
	public BLOCK_TYPE blockType;
	
	public Block(float[] coords) {
		this.blockType = BLOCK_TYPE.NONE;
		this.coords = coords;
	}
	
	public Bitmap blockImage() {
		
		if (blockType == BLOCK_TYPE.GREEN)
			return bitmapBlockGreen;
		else if (blockType == BLOCK_TYPE.BLUE)
			return bitmapBlockBlue;
		else if (blockType == BLOCK_TYPE.RED)
			return bitmapBlockRed;
		
		return null;
	}
	
	public int blockPoints() {
		
		if (blockType == BLOCK_TYPE.GREEN)
			return 100 - PROBABILITY_OF_BLOCK;
		else if (blockType == BLOCK_TYPE.BLUE)
			return 100 - PROBABILITY_OF_BLOCK_BLUE;
		else if (blockType == BLOCK_TYPE.RED)
			return 100 - PROBABILITY_OF_BLOCK_RED;
		
		return 0;
	}
}

public class Game {
	
	public enum GAME_RESULT {
		NONE,
		WIN,
		LOOSE
	}

	public int BLOCKS_IN_ROW;
	public int BLOCKS_IN_COLUMN;
	public int STATUS_BAR_HEIGHT;
	
	public Point displaySize;
	private Random random;
	
	public Block[][] blocks = null;
	
	public int lives;
	public int points;
	public int visibleBlocks;
	
	public Bitmap bitmapBall;
	public Bitmap bitmapRacket;
	
	private final float RIGHT_FIX = 50.0f;
	public final float paddingX = 40.0f;
	public final float paddingY = 40.0f;
	
	public float[]  ballPosition;
	private float[] ballDirection;
	private float   ballSpeed;
	
	public float[] racketPosition;
	public float   racketSpeed;
	
	private void setVisibilityLeftTopQuarterOfBlocks() {
		
		for (int row=0; row < BLOCKS_IN_COLUMN/2; row++) {
			
			for (int column=0; column < BLOCKS_IN_ROW/2; column++) {

				blocks[row][column].blockType = BLOCK_TYPE.NONE;
				
				if (random.nextInt(101) <= Block.PROBABILITY_OF_BLOCK) {
					visibleBlocks++;
					int rnd = random.nextInt(101);
					if (rnd <= Block.PROBABILITY_OF_BLOCK_RED)
						blocks[row][column].blockType = BLOCK_TYPE.RED;
					else if (rnd <= Block.PROBABILITY_OF_BLOCK_BLUE)
						blocks[row][column].blockType = BLOCK_TYPE.BLUE;
					else
						blocks[row][column].blockType = BLOCK_TYPE.GREEN;
					
				}
			}
			
		}		
	}

	private void setVisibilityRightTopQuarterOfBlocks() {
		
		for (int row=0; row < BLOCKS_IN_COLUMN/2; row++) {
			
			for (int column=BLOCKS_IN_ROW/2; column < BLOCKS_IN_ROW; column++) {
				blocks[row][column].blockType = blocks[row][BLOCKS_IN_ROW - (column + 1)].blockType;
				if (blocks[row][column].blockType != BLOCK_TYPE.NONE)
					visibleBlocks++;
			}
			
		}		
	}	

	private void setVisibilityRightBottomQuarterOfBlocks() {
		
		for (int row=BLOCKS_IN_COLUMN/2; row < BLOCKS_IN_COLUMN; row++) {
			
			for (int column=BLOCKS_IN_ROW/2; column < BLOCKS_IN_ROW; column++) {
				blocks[row][column].blockType = blocks[BLOCKS_IN_COLUMN - (row + 1)][column].blockType;
				if (blocks[row][column].blockType != BLOCK_TYPE.NONE)
					visibleBlocks++;
			}
			
		}		
	}	

	private void setVisibilityLeftBottomQuarterOfBlocks() {
		
		for (int row=BLOCKS_IN_COLUMN/2; row < BLOCKS_IN_COLUMN; row++) {
			
			for (int column=0; column < BLOCKS_IN_ROW/2; column++) {
				blocks[row][column].blockType = blocks[BLOCKS_IN_COLUMN - (row + 1)][column].blockType;
				if (blocks[row][column].blockType != BLOCK_TYPE.NONE)
					visibleBlocks++;
			}
			
		}		
	}	

	private void generateBlocks() {
		
		for (int row=0; row < BLOCKS_IN_COLUMN; row++) {
			
			for (int column=0; column < BLOCKS_IN_ROW; column++) {
				float x = paddingX + column * Block.bitmapBlockGreen.getWidth();
				float y = paddingY + row * Block.bitmapBlockGreen.getHeight();
				
				blocks[row][column] = new Block(new float[] {x, y});
			}
			
		}

	}
		
	public GAME_RESULT getGameResult() {
		
		if (lives == -1)
			return GAME_RESULT.LOOSE;
		else if (visibleBlocks == 0)
			return GAME_RESULT.WIN;
		
		return GAME_RESULT.NONE;
	}
		
	public Game(Point displaySize, Bitmap ball, Bitmap racket, Bitmap blockGreen, Bitmap blockBlue, Bitmap blockRed) {
		STATUS_BAR_HEIGHT = 25;
		
		ballDirection = Vector2D.Normalize(new float[] {+1.0f, -1.0f});
		ballSpeed = 1.0f;
		
		racketSpeed = 0.0f;
		
		random = new Random();
		
		lives = 3;
		points = 0;
		visibleBlocks = 0;
		
		BLOCKS_IN_ROW = (displaySize.x - (int)RIGHT_FIX - 2 * (int)paddingX) / blockGreen.getWidth();
		BLOCKS_IN_COLUMN = (displaySize.y - 2 * (int)paddingY - 2 * racket.getHeight() - 2 * ball.getHeight()) / blockGreen.getHeight();

		BLOCKS_IN_ROW = (BLOCKS_IN_ROW % 2 == 0)? BLOCKS_IN_ROW : BLOCKS_IN_ROW - 1;
		BLOCKS_IN_COLUMN = (BLOCKS_IN_COLUMN % 2 == 0)? BLOCKS_IN_COLUMN : BLOCKS_IN_COLUMN - 1;		
		
		blocks = new Block[BLOCKS_IN_COLUMN][BLOCKS_IN_ROW];
		
		this.displaySize = displaySize;
		this.bitmapBall = ball;
		this.bitmapRacket = racket;
		Block.bitmapBlockGreen = blockGreen;
		Block.bitmapBlockBlue = blockBlue;
		Block.bitmapBlockRed = blockRed;
		
		generateBlocks();
		setVisibilityLeftTopQuarterOfBlocks();
		setVisibilityRightTopQuarterOfBlocks();
		setVisibilityRightBottomQuarterOfBlocks();
		setVisibilityLeftBottomQuarterOfBlocks();
		
		racketPosition = new float[] {(displaySize.x - RIGHT_FIX) / 2 - racket.getWidth() / 2, displaySize.y -  racket.getHeight() - paddingY};
		ballPosition = new float[] {racketPosition[0] + racket.getWidth() / 2, racketPosition[1] - ball.getHeight()};
	}
	
	private boolean updateBallPositionOnCollisionWithBottomLine(float[] oldPosition, float[] newPosition, float[] lineStart, float[] lineEnd) {
		float[] newPositionExtended = Vector2D.Add(newPosition, Vector2D.MultiplyScalar(ballDirection, (float)bitmapBall.getWidth() / 2.0f));
		boolean collides = Vector2D.linesIntersect(oldPosition, newPositionExtended, lineStart, lineEnd);

		if (collides) {
			ballDirection = Vector2D.Normalize(new float[] {+1.0f, -1.0f});
			ballPosition = new float[] {racketPosition[0] + this.bitmapRacket.getWidth() / 2, racketPosition[1] - this.bitmapBall.getHeight()};
			lives--;
		}
		
		return collides;
	}
		
	private boolean updateBallDirectionOnCollisionWithLine(float[] oldPosition, float[] newPosition, float[] lineStart, float[] lineEnd) {
		float[] newPositionExtended = Vector2D.Add(newPosition, Vector2D.MultiplyScalar(ballDirection, (float)bitmapBall.getWidth() / 2.0f));
		boolean collides = Vector2D.linesIntersect(oldPosition, newPositionExtended, lineStart, lineEnd);
		float[] speedProjectionOnCollidingLine = new float[2];
		
		if (collides) {
			Vector2D.LINE_TYPE lineType = Vector2D.getLineType(lineStart, lineEnd);
			
			float sign = 0.0f;
			float dXSign = Math.signum(newPosition[0] - oldPosition[0]);
			float dYSign = Math.signum(newPosition[1] - oldPosition[1]);
			
			if (lineType == LINE_TYPE.VERTICAL) {
				speedProjectionOnCollidingLine[0] = 0.0f;
				speedProjectionOnCollidingLine[1] = dYSign;
				
				sign = (dXSign == dYSign)? +1.0f : -1.0f;
			}
			else if (lineType == LINE_TYPE.HORIZONTAL) {
				speedProjectionOnCollidingLine[0] = dXSign;
				speedProjectionOnCollidingLine[1] = 0.0f;
				
				sign = (dXSign != dYSign)? +1.0f : -1.0f;
			}
			
			final float[] ZERO_VECTOR = {0.0f, 0.0f};
			int angle;
			float[] newDirection;
			
			if (Arrays.equals(speedProjectionOnCollidingLine, ZERO_VECTOR)) {
				angle = 180;
				newDirection = Vector2D.RotateVector(ballDirection, angle);				
			}
			else {
				angle = Vector2D.angleBetweenTwoVectors(speedProjectionOnCollidingLine, ballDirection);
				newDirection = Vector2D.RotateVector(ballDirection, (int)sign * 2 * angle);				
			}
			
			newDirection = Vector2D.Normalize(newDirection);
			ballDirection = newDirection;

		}
		
		return collides;
	}
		
	private boolean dirChangedBecauseOfScreenBorders(float[] oldPosition, float[] newPosition) {
		
		boolean dirChangedBecauseOfLine = false;
		float[][][] lines = getRectangleBorderLines(new float[] {0.0f, 0.0f}, displaySize.x - RIGHT_FIX, displaySize.y - STATUS_BAR_HEIGHT);
		
		dirChangedBecauseOfLine = updateBallPositionOnCollisionWithBottomLine(oldPosition, newPosition, lines[3][0], lines[3][1]);
		if (dirChangedBecauseOfLine) {
			return dirChangedBecauseOfLine;
		}
		
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[0][0], lines[0][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[1][0], lines[1][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[2][0], lines[2][1]) : dirChangedBecauseOfLine;
		
		return dirChangedBecauseOfLine;
	}
	
	private float[][][] getRectangleBorderLines(float[] start, float width, float height) {
		float[][][] lines = new float[4][][];
		
		float[] leftStart = {start[0], start[1]};
		float[] leftEnd =   {start[0], start[1] + height};

		float[] rightStart = {start[0] + width, start[1]};
		float[] rightEnd =   {start[0] + width, start[1] + height};

		float[] topStart = {start[0],         start[1]};
		float[] topEnd =   {start[0] + width, start[1]};

		float[] bottomStart = {start[0],         start[1] + height};
		float[] bottomEnd =   {start[0] + width, start[1] + height};
		
		lines[0] = new float[][] {leftStart, leftEnd};
		lines[1] = new float[][] {rightStart, rightEnd};
		lines[2] = new float[][] {topStart, topEnd};
		lines[3] = new float[][] {bottomStart, bottomEnd};
		
		return lines;
	}

	private boolean dirChangedBecauseOfRacketBorders(float[] oldPosition, float[] newPosition) {
		
		boolean dirChangedBecauseOfLine = false;
		float[][][] lines = getRectangleBorderLines(new float[] {racketPosition[0], racketPosition[1]}, bitmapRacket.getWidth(), bitmapRacket.getHeight());

		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[0][0], lines[0][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[1][0], lines[1][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[2][0], lines[2][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[3][0], lines[3][1]) : dirChangedBecauseOfLine;
		
		return dirChangedBecauseOfLine;
	}
	
	private boolean dirChangedBecauseOfBlock(float[] oldPosition, float[] newPosition, Block block) {
		boolean dirChangedBecauseOfLine = false;
		float[][][] lines = getRectangleBorderLines(new float[] {block.coords[0], block.coords[1]}, Block.bitmapBlockGreen.getWidth(), Block.bitmapBlockGreen.getHeight());

		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[0][0], lines[0][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[1][0], lines[1][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[2][0], lines[2][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[3][0], lines[3][1]) : dirChangedBecauseOfLine;

		if (dirChangedBecauseOfLine) {
			this.points += block.blockPoints();
			block.blockType = BLOCK_TYPE.NONE;
			visibleBlocks--;
		}
		
		return dirChangedBecauseOfLine;		
	}
	
	private boolean dirChangedBecauseOfBlockGrid(float[] oldPosition, float[] newPosition) {
		int colCurrent = ((int)newPosition[0] - (int)paddingX) / Block.bitmapBlockGreen.getWidth();
		int rowCurrent = ((int)newPosition[1] - (int)paddingY) / Block.bitmapBlockGreen.getHeight();
		final int BLOCK_COUNT = 2;
		
		for (int row = rowCurrent - BLOCK_COUNT; row < rowCurrent + BLOCK_COUNT + 1; row++) {
			for (int column = colCurrent - BLOCK_COUNT; column < colCurrent + BLOCK_COUNT + 1; column++) {
				try {					
					if (blocks[row][column].blockType != BLOCK_TYPE.NONE) {
						boolean dirChangedBecauseOfLine = dirChangedBecauseOfBlock(oldPosition, newPosition, blocks[row][column]);
						if (dirChangedBecauseOfLine)
							return true;
					}
				} catch (IndexOutOfBoundsException e) {
				}
				
			}
			
		}
		
		return false;	
	}
	
	public void updateGame() {
		
		racketPosition[0] = Vector2D.Clamp(racketPosition[0] + racketSpeed, 0.0f, displaySize.x - RIGHT_FIX - bitmapRacket.getWidth() );
		
		float[] oldPosition = ballPosition;
		float[] newPosition = Vector2D.Add(ballPosition, Vector2D.MultiplyScalar(ballDirection, ballSpeed));
		
		boolean dirChangedBecauseOfLine = false;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? dirChangedBecauseOfScreenBorders(oldPosition, newPosition) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? dirChangedBecauseOfRacketBorders(oldPosition, newPosition) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? dirChangedBecauseOfBlockGrid(oldPosition, newPosition)     : dirChangedBecauseOfLine;
				
		if (!dirChangedBecauseOfLine)
			ballPosition = newPosition;
	}
}
