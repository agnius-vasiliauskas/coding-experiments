package com.pathrecorder;

import java.io.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.Bitmap.*;
import android.os.*;

public class Storage {
	
	private Orientation orientation;
	private Activity activity;
	
	private final String keyXmin = "calXmin";
	private final String keyXmax = "calXmax";
	private final String keyYmin = "calYmin";
	private final String keyYmax = "calYmax";
	
	public Storage(Orientation orientation, Activity activity) {
		if (orientation == null || activity == null)
			throw new RuntimeException("Parameters are not set for Storage constructor!");
		
		this.orientation = orientation;
		this.activity = activity;
	}
	
	public String saveBitmap(Bitmap bitmap) {
		String fileNameShort = String.format("path_%d.jpg", System.currentTimeMillis()/1000);
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	    File file = new File(path, fileNameShort);
		
		try {			
			OutputStream stream = new FileOutputStream(file);
			boolean writeSuccess = bitmap.compress(CompressFormat.JPEG, 100, stream);
			if (!writeSuccess)
				return null;
			if (stream != null)
				stream.close();
			return fileNameShort;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public void saveCalibrationData() {
		float[] calibrationData = orientation.getCalibrationRangeValues();
		if (calibrationData == null)
			return;
		
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putFloat(keyXmin, calibrationData[0]);
		editor.putFloat(keyXmax, calibrationData[1]);
		editor.putFloat(keyYmin, calibrationData[2]);
		editor.putFloat(keyYmax, calibrationData[3]);
		
		boolean writeOk = editor.commit();		
		if (!writeOk)
			throw new RuntimeException("Error saving data !");
		
	}
	
	public void loadCalibrationData() {
		final float defaultValue = (float) 1e+10;
		SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
		float[] calibrationData = new float[4];
		
		calibrationData[0] = sharedPref.getFloat(keyXmin, defaultValue);		
		calibrationData[1] = sharedPref.getFloat(keyXmax, defaultValue);		
		calibrationData[2] = sharedPref.getFloat(keyYmin, defaultValue);		
		calibrationData[3] = sharedPref.getFloat(keyYmax, defaultValue);		
		
		for (int i = 0; i < 4; i++) {
			if (calibrationData[i] == defaultValue) {
				calibrationData = null;
				break;
			}
		}
		
		orientation.setCalibrationRangeValues(calibrationData);
	}
	
}
