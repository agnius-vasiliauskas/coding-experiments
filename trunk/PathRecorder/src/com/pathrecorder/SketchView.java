package com.pathrecorder;

import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.view.*;


public class SketchView extends View {
		
	private boolean beginDrawing = false;
	private final int TOTAL_MESSAGES = 2;
	private final long TIMEOUT_FOR_MESSAGES_MS = 1000;
	private final int backGroundColor =  0xffffffff;
	private final int drawColor =        0xffff0000;
	private final int compassTextColor = 0xff0000ff;
	private final int compassBorderColor = 0xff7f7f7f;
	private final int compassOvalColor = 0xffC8C8C8;
	private final int strokeWidth = 3;
	private final int strokeWidthCompassText = 2;
	private final float compassCenterPadding = 7.0f;
	private final float compassRadius = 20.0f;
	private float viewYlimit = -1.0f;
	
	private long lastTimeForMessageInit;
	private static Rect textBounds = new Rect();
	private String[] messages = new String[TOTAL_MESSAGES];
	private String[] messagesWithTimeout = new String[TOTAL_MESSAGES];
	private int degreesToNorth = -1000;
	private float screenAspectRatio = -1.0f;
	private Bitmap bitmapArrow;
	private Paint paint = null;
	private Paint paintCompassText = null;	
	private Paint ovalPaint = null;
	private Paint ovalCenterPaint = null;	
	private Paint ovalBorderPaint = null;
	private Paint ovalLinesPaint = null;
	private Paint menuTextPaint = null;	
	
	private MenuManager menuManager = null;
	private Movement movement = null;
	private Bitmap exportBitmap;
	private boolean exportToBitmap;
	private boolean exportBitmapIsExported;
	private Canvas exportCanvas;
		
	private void InitSketch() {
		if (paint == null) {
			paint = new Paint();
			paint.setColor(drawColor);
			paint.setStrokeWidth(1);
			paint.setStyle(Style.STROKE);
			paint.setAntiAlias(true);
		}
		if (paintCompassText == null) {
			paintCompassText = new Paint();
			paintCompassText.setColor(compassTextColor);
			paintCompassText.setStrokeWidth(strokeWidthCompassText);
			paintCompassText.setStyle(Style.FILL);
			paintCompassText.setAntiAlias(true);
		}
		if (ovalPaint == null) {
	    	ovalPaint =  new Paint();
	    	ovalPaint.setColor(compassOvalColor);
	    	ovalPaint.setStyle(Style.FILL);			
	    	ovalPaint.setAntiAlias(true);
		}
		if (ovalCenterPaint == null) {
			ovalCenterPaint =  new Paint();
			ovalCenterPaint.setColor(compassBorderColor);
			ovalCenterPaint.setStyle(Style.FILL);
			ovalCenterPaint.setAntiAlias(true);
		}
		if (ovalLinesPaint == null) {
			ovalLinesPaint = new Paint();
			ovalLinesPaint.setColor(drawColor);
			ovalLinesPaint.setStrokeWidth(strokeWidthCompassText);
			ovalLinesPaint.setAntiAlias(true);
		}
		if (ovalBorderPaint == null) {
			ovalBorderPaint =  new Paint();
			ovalBorderPaint.setStrokeWidth(strokeWidth);
			ovalBorderPaint.setColor(compassBorderColor);
			ovalBorderPaint.setStyle(Style.STROKE);
			ovalBorderPaint.setAntiAlias(true);
		}
		if (menuTextPaint == null) {
			menuTextPaint = new Paint();
			menuTextPaint.setStrokeWidth(strokeWidthCompassText);
			menuTextPaint.setColor(backGroundColor);
			menuTextPaint.setStyle(Style.FILL);			
			menuTextPaint.setTextSize(30.0f);
			menuTextPaint.setAntiAlias(true);
		}
		
	}
	
	public void exportPathToBitmap(boolean export) {
		this.exportToBitmap = export;
		this.exportBitmapIsExported = false;
		
		if (export) {
			Bitmap.Config conf = Bitmap.Config.RGB_565;
			final int bHeight = 800;
			final int bWidth = 600;
			this.exportBitmap = Bitmap.createBitmap(bWidth, bHeight, conf);
			this.exportCanvas = new Canvas(this.exportBitmap);
		}
	}
	
	public SketchView(Context context) {
		super(context);
		InitSketch();	
		exportPathToBitmap(false);
	}
	
	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}
	
	public void setMovementObject(Movement movement) {
		this.movement = movement;
	}
	
	public void setBeginDrawing(boolean draw) {
		this.beginDrawing = draw;
	}
	
	public void setBitmapArrow(Bitmap bitmapArrow) {
		this.bitmapArrow = bitmapArrow;
	}
	
	public void setMessages(String str1, String str2) {
		messages[0] = str1;
		messages[1] = str2;
	}

	public void setMessagesWithTimeout(String str1, String str2) {
		lastTimeForMessageInit = System.currentTimeMillis();
		messagesWithTimeout[0] = str1;
		messagesWithTimeout[1] = str2;
	}
	
	public void setDegreesToNorth(int degreesToNorth) {
		this.degreesToNorth = degreesToNorth;
	}
	
	public void setAspectRatio(float aspectRatio) {
		this.screenAspectRatio = aspectRatio;
	}
	
	private void drawCompass(Canvas canvas, int degreesToNorth) {
		
		if (canvas == null || degreesToNorth == -1000)
			return;
		
        final float insideCircleRadius = 3.0f;            
        float[] center = {compassRadius + compassCenterPadding, 
        				 (float)canvas.getHeight() - screenAspectRatio * (compassRadius + compassCenterPadding) };
        
    	// calculate direction line part
    	float[] startVec = {0.0f , -1.0f};
    	float[] rotated = Vector2D.RotateVector(startVec, degreesToNorth);
    	float[] normalizedVec = Vector2D.Normalize(rotated);
    	float[] scaledVec = Vector2D.MultiplyScalar(normalizedVec, compassRadius);
    	float[] drawEnd = Vector2D.Add(scaledVec, center);
    	
    	// calculate arrow coordinates part
    	Bitmap rotatedArrow = Vector2D.RotateBitmap(bitmapArrow, degreesToNorth);
    	final float shiftToCompassCenter = 3.0f;
    	float[] shiftToOrigin = {rotatedArrow.getWidth()/2 , rotatedArrow.getHeight()/2 };
    	float[] drawEndRelativeToCenter = Vector2D.Subtract(drawEnd, center);
    	float[] arrowCoordsRelativeToCenter;
    	arrowCoordsRelativeToCenter = Vector2D.Subtract(drawEndRelativeToCenter, shiftToOrigin);
    	arrowCoordsRelativeToCenter = Vector2D.Subtract(arrowCoordsRelativeToCenter, Vector2D.MultiplyScalar(normalizedVec, shiftToCompassCenter));
    	float[] arrowCoords = Vector2D.Add(center, arrowCoordsRelativeToCenter);
    	
    	// compass background
    	canvas.drawCircle(center[0], center[1], compassRadius, ovalPaint);
    	
    	// compass border
    	canvas.drawCircle(center[0], center[1], compassRadius, ovalBorderPaint);
    	
    	// some graduation lines
    	int lineLength = 6;
    	canvas.drawLine(center[0] + compassRadius, center[1], center[0] + compassRadius - lineLength, center[1], ovalBorderPaint);
    	canvas.drawLine(center[0] - compassRadius, center[1], center[0] - compassRadius + lineLength, center[1], ovalBorderPaint);
    	canvas.drawLine(center[0], center[1] + compassRadius, center[0], center[1] + compassRadius - lineLength, ovalBorderPaint);
    	canvas.drawLine(center[0], center[1] - compassRadius, center[0], center[1] - compassRadius + lineLength, ovalBorderPaint);

    	// compass arrow
    	canvas.drawLine(center[0], center[1], drawEnd[0], drawEnd[1], ovalLinesPaint);
    	canvas.drawBitmap(rotatedArrow, arrowCoords[0], arrowCoords[1], ovalLinesPaint);        	
    	
    	// compass center
    	canvas.drawCircle(center[0], center[1], insideCircleRadius, ovalCenterPaint);
		
	}
	
	private void drawStatusBar(Canvas canvas) {

		// bar lines
		canvas.drawLine(0, viewYlimit, (float)canvas.getWidth(), viewYlimit, ovalBorderPaint);
		
		float drawXvert = 2.0f * compassRadius + 2.0f * compassCenterPadding;
		canvas.drawLine(drawXvert, viewYlimit, drawXvert, canvas.getHeight(), ovalBorderPaint);

		// draw messages
    	final String testMessage = " !";
    	paintCompassText.getTextBounds(testMessage, 0, testMessage.length(), textBounds);
		final float textHeightPadding = 4.0f;		
        
		String[] messagesForStatusBar = messagesWithTimeout;
		if (System.currentTimeMillis() - lastTimeForMessageInit > TIMEOUT_FOR_MESSAGES_MS ||
			messagesForStatusBar[0] == null || messagesForStatusBar[1] == null || 
			messagesForStatusBar[0].length() == 0 || messagesForStatusBar[1].length() == 0 )
			messagesForStatusBar = messages;
		
		for (int i = 0; i < messagesForStatusBar.length; i++) {
			if (messagesForStatusBar[i] != null && messagesForStatusBar[i].length() > 0) {
				float textY = viewYlimit + 
							  screenAspectRatio * compassCenterPadding + 
							  screenAspectRatio * textHeightPadding + 
							  i * (textBounds.height() + textHeightPadding);
				
	        	canvas.drawText(messagesForStatusBar[i], drawXvert + 2.0f * compassCenterPadding, textY, paintCompassText);				
			}
		}    	
		
	}
	
	private void drawMenu(Canvas canvas) {
		
		if (menuManager == null || !menuManager.getIsMenuVisible())
			return;
		
		float startX = 10.0f;
		float startY = 30.0f;
		float padding = 10.0f;
		float buttonX = -menuManager.getBitmapMenuArrow().getWidth() / 2 + canvas.getWidth() / 2;
		
    	if (menuManager.menuCoordsArrowUp == null)
    		menuManager.menuCoordsArrowUp = new float[] {buttonX, startY};
    	
    	if (menuManager.menuCoordsButton == null)
    		menuManager.menuCoordsButton = new float[] {startX, menuManager.menuCoordsArrowUp[1] + menuManager.getBitmapMenuArrow().getHeight() + padding};
    	
    	if (menuManager.menuCoordsArrowDown == null)
    		menuManager.menuCoordsArrowDown = new float[] {buttonX, menuManager.menuCoordsButton[1] + menuManager.getBitmapMenuButton().getHeight() + padding};
    	
    	Bitmap rotatedArrow = Vector2D.RotateBitmap(menuManager.getBitmapMenuArrow(), 180);
    	if (menuManager.canSwitchPrevious())
    		canvas.drawBitmap(menuManager.getBitmapMenuArrow(),  menuManager.menuCoordsArrowUp[0], menuManager.menuCoordsArrowUp[1], ovalLinesPaint);
    	
    	canvas.drawBitmap(menuManager.getBitmapMenuButton(), menuManager.menuCoordsButton[0], menuManager.menuCoordsButton[1], ovalLinesPaint);
    	
    	if (menuManager.canSwitchNext())
    		canvas.drawBitmap(rotatedArrow,  menuManager.menuCoordsArrowDown[0], menuManager.menuCoordsArrowDown[1], ovalLinesPaint);
    	
    	Rect menuTextSize1 = new Rect();
    	Rect menuTextSize2 = new Rect();
    	String line1 = menuManager.getMenuText(true);
    	String line2 = menuManager.getMenuText(false);
		menuTextPaint.getTextBounds(line1, 0, line1.length(), menuTextSize1);
		menuTextPaint.getTextBounds(line2, 0, line2.length(), menuTextSize2);
		float xShift1 = (menuManager.getBitmapMenuButton().getWidth() - menuTextSize1.width() ) / 2;
		float xShift2 = (menuManager.getBitmapMenuButton().getWidth() - menuTextSize2.width() ) / 2;
		xShift1 = (xShift1 < 0) ? 0 : xShift1;
		xShift2 = (xShift2 < 0) ? 0 : xShift2;

    	float textY = 10.0f +   menuManager.menuCoordsButton[1] + menuManager.getBitmapMenuButton().getHeight() / 2;
    	canvas.drawText(line1,  menuManager.menuCoordsButton[0] + xShift1, textY, menuTextPaint);
    	canvas.drawText(line2,  menuManager.menuCoordsButton[0] + xShift2, textY + menuTextSize1.height() + padding, menuTextPaint);
    	
	}
	
	private void drawMovementStatistics(MovementStatistics stats, Canvas canvas) {
		
		if (stats == null || !movement.getIsMovementStatisticsNeeded())
			return;
		
		// draw messages
    	final String testMessage = " !";
    	paintCompassText.getTextBounds(testMessage, 0, testMessage.length(), textBounds);
		final float textHeightPadding = 4.0f;		
        
		String[] messagesOfMovementStats = stats.getStatisticsInfoMessages();
		
		float xPadd = 10.0f;
		float yPadd = 10.0f;
		
		for (int i = 0; i < messagesOfMovementStats.length; i++) {
			if (messagesOfMovementStats[i] != null && messagesOfMovementStats[i].length() > 0) {
				float textY = yPadd + 
							  screenAspectRatio * textHeightPadding + 
							  i * (textBounds.height() + textHeightPadding);
				
	        	canvas.drawText(messagesOfMovementStats[i], xPadd, textY, paintCompassText);				
			}
		}    	
		
	}
	
	private void drawMovement(Canvas canvas) {
		
		if (movement == null)
			return;
				
		final float[] startPoint = new float[] {canvas.getWidth() / 2, viewYlimit / 2};
		
		Object[] movementData = movement.getMovementDataForDraw(startPoint);
		if (movementData != null) {
			
			if (movement.getIsMovement()) {
				String line1 = movement.getMessage(true);
				String line2 = movement.getMessage(false);
				
				setMessages(line1, line2);
			}
			
			Path path = (Path)movementData[0];
			Float[] lastPoint = (Float[])movementData[1];
			MovementStatistics stats = (MovementStatistics)movementData[2];
			
			final int pathPointRadius = 2;
			
			canvas.drawPath(path, paint);
			
			canvas.drawCircle(lastPoint[0], lastPoint[1], pathPointRadius, ovalCenterPaint);
			
			drawMovementStatistics(stats, canvas);
		}
		
	}
	
	private void initDrawing(Canvas canvas) {		
		if (viewYlimit == -1.0f)
			viewYlimit = (float)canvas.getHeight() - screenAspectRatio * compassRadius * 2.0f;
	}
	
	private void drawBackground(Canvas canvas) {
        canvas.drawColor(backGroundColor);
	}
	
	private void drawToBitmap() {
        if (exportToBitmap) {
            drawBackground(exportCanvas);
            drawMovement(exportCanvas);        	
            exportToBitmap = false;
            exportBitmapIsExported = true;
        }		
	}
	
	public boolean getBitmapIsExported() {
		return this.exportBitmapIsExported;
	}
	
	public Bitmap getExportBitmap() {
		return this.exportBitmap;
	}
	
    @Override
    protected void onDraw(Canvas canvas) {
    	if (!beginDrawing)
    		return;
    	
        super.onDraw(canvas);

        initDrawing(canvas);
        
        drawBackground(canvas);
        drawMovement(canvas);
        
        drawToBitmap();

        drawStatusBar(canvas);
    	
        drawCompass(canvas, degreesToNorth);
        
        drawMenu(canvas);
    }

}
