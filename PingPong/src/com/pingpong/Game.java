package com.pingpong;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import com.pingpong.Block.BLOCK_TYPE;
import com.pingpong.Block.BONUS_TYPE;
import com.pingpong.Vector2D.LINE_TYPE;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

class Block {
	
	public enum BLOCK_TYPE {
		GREEN,
		BLUE,
		RED,
		NONE
	}
	
	public enum BONUS_TYPE {
		NONE,
		PLUS,
		MINUS
	}
		
	public static Bitmap bitmapBlockGreen;
	public static Bitmap bitmapBlockBlue;
	public static Bitmap bitmapBlockRed;

	public static final int PROBABILITY_OF_BLOCK = 80;
	public static final int PROBABILITY_OF_BLOCK_RED = 5;
	public static final int PROBABILITY_OF_BLOCK_BLUE = 20;	

	public static final int PROBABILITY_OF_BONUS = 40;
	public static final int PROBABILITY_OF_BONUS_MINUS = 30;	
	
	public static final String STRING_MINUS = "-";
	public static final String STRING_PLUS =  "+";
	
	public static Rect[] bonusTextBounds = null;
	
	public float[] coords;
	public float[] coordsText;
	public float[] coordsTextBonus;
	public String  blockText;
	public String  blockTextBonus;
	
	public BLOCK_TYPE blockType;
	public BONUS_TYPE blockBonus;
	
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
	
	public String blockBonusText() {
		
		if (blockBonus == BONUS_TYPE.MINUS)
			return STRING_MINUS;
		else if (blockBonus == BONUS_TYPE.PLUS)
			return STRING_PLUS;
		else 
			return "";
	}
	
	public Rect blockBonusBounds() {
		
		if (bonusTextBounds == null)
			return null;
		
		if (blockBonus == BONUS_TYPE.MINUS)
			return bonusTextBounds[0];
		else if (blockBonus == BONUS_TYPE.PLUS)
			return bonusTextBounds[1];
		else 
			return null;
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
	public boolean isDemo;
	private Random random;
	public boolean isGameStarted;
	
	public Block[][] blocks = null;
	public LinkedList<Block> unvisibleBlocksForPoints = null;
	public LinkedList<Block> unvisibleBlocksForBonus = null;
	
	public int lives;
	public int points;
	public int visibleBlocks;
	
	public Bitmap bitmapBall;
	
	private float PADDING_X;
	private float PADDING_Y;

	private final float  RACKET_SPEED = 2.0f;
	private final Point  RACKET_SIZE = new Point(80, 20);
	private final int    RACKET_WIDTH_CHANGE = 5;
	private final int    RACKET_WIDTH_CHANGE_MAX = 50;
	
	private final float SPEED_CHANGE = 0.0002f;
	private final float SPEED_MIN = 1.0f;
	private final float SPEED_MAX = 5.0f;
	
	public float[]  ballPosition;
	private float[] ballDirection;
	private float   ballSpeed;
	
	public float[] racketPosition;
	public Point   racketSize;
	private float  racketSpeed;
	
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
				float x = PADDING_X + column * Block.bitmapBlockGreen.getWidth();
				float y = PADDING_Y + row * Block.bitmapBlockGreen.getHeight();
				
				blocks[row][column] = new Block(new float[] {x, y});
				blocks[row][column].coordsText = new float[] {0.0f, -100.0f};
				blocks[row][column].blockText = "";
				
				if (random.nextInt(101) <= Block.PROBABILITY_OF_BONUS) {
					int rnd = random.nextInt(101);
					if (rnd <= Block.PROBABILITY_OF_BONUS_MINUS)
						blocks[row][column].blockBonus = BONUS_TYPE.MINUS;
					else
						blocks[row][column].blockBonus = BONUS_TYPE.PLUS;
				}
				else {
					blocks[row][column].blockBonus = BONUS_TYPE.NONE;
				}
				
				blocks[row][column].blockTextBonus = "";
			}
			
		}

	}
		
	public GAME_RESULT getGameResult() {
		
		if (!isGameStarted)
			return GAME_RESULT.NONE;
		
		if (lives == -1)
			return GAME_RESULT.LOOSE;
		else if (visibleBlocks == 0)
			return GAME_RESULT.WIN;
		
		return GAME_RESULT.NONE;
	}
	
	public void setRacketSpeed(float hitX) {
		
    	float newRacketSpeed = 0.0f;
    	
    	if (hitX > this.racketPosition[0] + this.racketSize.x / 2)
    		newRacketSpeed = + this.RACKET_SPEED;
    	else if (hitX < this.racketPosition[0] + this.racketSize.x / 2)
    		newRacketSpeed = - this.RACKET_SPEED;
    	
    	this.racketSpeed = newRacketSpeed;		
	}
	
	public void setRacketSpeedToZero() {
    	this.racketSpeed = 0.0f;
	}
	
	private void startGame() {
		STATUS_BAR_HEIGHT = 25;
		
		unvisibleBlocksForPoints = new LinkedList<>();
		unvisibleBlocksForBonus = new LinkedList<>();
		
		ballDirection = Vector2D.Normalize(new float[] {+1.0f, -1.0f});
		ballSpeed = 1.0f;
		
		racketSpeed = 0.0f;
		racketSize = RACKET_SIZE;
		
		random = new Random();
		
		lives = 3;
		points = 0;
		visibleBlocks = 0;
		
		BLOCKS_IN_ROW = (displaySize.x - 2 * (int)PADDING_X) / Block.bitmapBlockGreen.getWidth();
		BLOCKS_IN_COLUMN = (displaySize.y - 2 * (int)PADDING_Y - 2 * racketSize.y - 2 * bitmapBall.getHeight()) / Block.bitmapBlockGreen.getHeight();

		BLOCKS_IN_ROW = (BLOCKS_IN_ROW % 2 == 0)? BLOCKS_IN_ROW : BLOCKS_IN_ROW - 1;
		BLOCKS_IN_COLUMN = (BLOCKS_IN_COLUMN % 2 == 0)? BLOCKS_IN_COLUMN : BLOCKS_IN_COLUMN - 1;		
		
		blocks = new Block[BLOCKS_IN_COLUMN][BLOCKS_IN_ROW];
				
		generateBlocks();
		setVisibilityLeftTopQuarterOfBlocks();
		setVisibilityRightTopQuarterOfBlocks();
		setVisibilityRightBottomQuarterOfBlocks();
		setVisibilityLeftBottomQuarterOfBlocks();
		
		racketPosition = new float[] {displaySize.x / 2 - racketSize.x / 2, displaySize.y -  racketSize.y - PADDING_Y};
		ballPosition = new float[] {racketPosition[0] + racketSize.x / 2, racketPosition[1] - bitmapBall.getHeight()};		
		
		isGameStarted = true;
	}
	
	public void setDisplaySize(Point displaySize) {
		this.displaySize = displaySize;
	}
	
	public Game(Bitmap ball, Bitmap blockGreen, Bitmap blockBlue, Bitmap blockRed, boolean isDemo) {
		
		this.PADDING_X = 2.0f * blockGreen.getWidth();
		this.PADDING_Y = 2.0f * blockGreen.getHeight();

		this.displaySize = null;
		this.bitmapBall = ball;
		this.isDemo = isDemo;
		this.isGameStarted = false;

		Block.bitmapBlockGreen = blockGreen;
		Block.bitmapBlockBlue = blockBlue;
		Block.bitmapBlockRed = blockRed;
	}
	
	private boolean updateBallPositionOnCollisionWithBottomLine(float[] oldPosition, float[] newPosition, float[] lineStart, float[] lineEnd) {
		float[] newPositionExtended = Vector2D.Add(newPosition, Vector2D.MultiplyScalar(ballDirection, (float)bitmapBall.getWidth() / 2.0f));
		boolean collides = Vector2D.linesIntersect(oldPosition, newPositionExtended, lineStart, lineEnd);

		if (collides) {
			racketSize = RACKET_SIZE;
			ballSpeed = SPEED_MIN;
			ballDirection = Vector2D.Normalize(new float[] {+1.0f, -1.0f});
			ballPosition = new float[] {racketPosition[0] + this.racketSize.x / 2, racketPosition[1] - this.bitmapBall.getHeight()};
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
		float[][][] lines = getRectangleBorderLines(new float[] {0.0f, 0.0f}, displaySize.x, displaySize.y - STATUS_BAR_HEIGHT);
		
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
		
		float[][][] lines = getRectangleBorderLines(new float[] {racketPosition[0], racketPosition[1]}, racketSize.x, racketSize.y);

		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[0][0], lines[0][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[1][0], lines[1][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[2][0], lines[2][1]) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? updateBallDirectionOnCollisionWithLine(oldPosition, newPosition, lines[3][0], lines[3][1]) : dirChangedBecauseOfLine;

		if (dirChangedBecauseOfLine) {
			final float RACKET_IMPULSE_FACTOR = 0.1f;
			float[] racketSpeedVec = {RACKET_IMPULSE_FACTOR * racketSpeed, 0.0f};
			ballDirection = Vector2D.Normalize(Vector2D.Add(ballDirection, racketSpeedVec));
		}
		
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
			unvisibleBlocksForPoints.add(block);
			unvisibleBlocksForBonus.add(block);
			points += block.blockPoints();
			block.blockText = String.format("%d", block.blockPoints());
			block.blockType = BLOCK_TYPE.NONE;
			block.coordsText = 		Arrays.copyOf(block.coords, block.coords.length);
			block.coordsTextBonus = Arrays.copyOf(block.coords, block.coords.length);
			block.blockTextBonus = block.blockBonusText();
			visibleBlocks--;
		}
		
		return dirChangedBecauseOfLine;		
	}
	
	private boolean dirChangedBecauseOfBlockGrid(float[] oldPosition, float[] newPosition) {
		int colCurrent = ((int)newPosition[0] - (int)PADDING_X) / Block.bitmapBlockGreen.getWidth();
		int rowCurrent = ((int)newPosition[1] - (int)PADDING_Y) / Block.bitmapBlockGreen.getHeight();
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
	
	private boolean isBallInsideRacket(float newRacketX) {
		return ballPosition[0] + bitmapBall.getWidth() / 2  > newRacketX && 
			   ballPosition[0] - bitmapBall.getWidth() / 2 < newRacketX + racketSize.x &&
			   ballPosition[1] > racketPosition[1] && 
			   ballPosition[1] < racketPosition[1] + racketSize.y;
	}
	
	private boolean isBonusMarkerInsideRacket(float newRacketX, Block block) {
		Rect bonusBounds = block.blockBonusBounds();
		if (bonusBounds == null)
			return false;
		if (block.blockBonus == BONUS_TYPE.NONE)
			return false;

		float yPad = (block.blockBonus == BONUS_TYPE.MINUS)? 8.0f : 4.0f;
		
		return  block.coordsTextBonus[0] + bonusBounds.width() > newRacketX && 
				block.coordsTextBonus[0] < newRacketX + racketSize.x &&
				block.coordsTextBonus[1] - yPad > racketPosition[1] && 
				block.coordsTextBonus[1] + yPad < racketPosition[1] + racketSize.y;
	}
	
	private void updateUnvisibleBlocksForPoints() {
		LinkedList<Block> deletableBlocks = new LinkedList<>();
		
		for (Block b : unvisibleBlocksForPoints)
			if (b.coordsText[1] < 0)
				deletableBlocks.add(b);
		
		unvisibleBlocksForPoints.removeAll(deletableBlocks);

		final float TEXT_POS_CHANGE = 2.0f;
		for (Block b : unvisibleBlocksForPoints)
			b.coordsText[1] -= TEXT_POS_CHANGE;
		
	}

	private void updateUnvisibleBlocksForBonus(float newRacketX) {
		LinkedList<Block> deletableBlocks = new LinkedList<>();
		
		for (Block b : unvisibleBlocksForBonus) {
			boolean isBonusInsideRacket = isBonusMarkerInsideRacket(newRacketX, b);
			if (b.coordsText[1] > displaySize.y || isBonusInsideRacket) {
				if (isBonusInsideRacket) {
					int sign = (b.blockBonus == BONUS_TYPE.MINUS)? -1 : +1;
					int newRacketWidth = racketSize.x + sign * RACKET_WIDTH_CHANGE;
					newRacketWidth = Vector2D.Clamp(newRacketWidth, 
													RACKET_SIZE.x - RACKET_WIDTH_CHANGE_MAX , 
													RACKET_SIZE.x + RACKET_WIDTH_CHANGE_MAX);
					racketSize = new Point(newRacketWidth, racketSize.y);
				}
				b.blockTextBonus = "";
				deletableBlocks.add(b);
			}
		}
		
		unvisibleBlocksForBonus.removeAll(deletableBlocks);

		final float TEXT_POS_CHANGE = 1.0f;
		for (Block b : unvisibleBlocksForBonus)
			b.coordsTextBonus[1] += TEXT_POS_CHANGE;
		
	}
	
	public void updateGame() {	
		
		if (displaySize == null)
			return;
		
		if (!isGameStarted) {
			startGame();
			return;
		}

		ballSpeed = Vector2D.Clamp(ballSpeed + SPEED_CHANGE, SPEED_MIN, SPEED_MAX);
		
		float[] oldPosition =  Arrays.copyOf(ballPosition, ballPosition.length);
		float[] newPosition = Vector2D.Add(ballPosition, Vector2D.MultiplyScalar(ballDirection, ballSpeed));
		
		boolean dirChangedBecauseOfLine = false;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? dirChangedBecauseOfScreenBorders(oldPosition, newPosition) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? dirChangedBecauseOfRacketBorders(oldPosition, newPosition) : dirChangedBecauseOfLine;
		dirChangedBecauseOfLine = (!dirChangedBecauseOfLine)? dirChangedBecauseOfBlockGrid(oldPosition, newPosition)     : dirChangedBecauseOfLine;
				
		if (!dirChangedBecauseOfLine)
			ballPosition = newPosition;
		
		if (isDemo)
			this.setRacketSpeed(ballPosition[0]);
		
		float newRacketX = Vector2D.Clamp(racketPosition[0] + racketSpeed, 0.0f, displaySize.x - racketSize.x );
		
		updateUnvisibleBlocksForPoints();
		updateUnvisibleBlocksForBonus(newRacketX);
		
		if (!isBallInsideRacket(newRacketX))
			racketPosition[0] = newRacketX;
	}
}
