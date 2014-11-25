package com.pathrecorder;

import android.graphics.*;

public class Vector2D {
	
	private static float dotProduct(float[] vec1, float[] vec2) {
		return vec1[0] * vec2[0] + vec1[1] * vec2[1];
	}
	
	public static int angleBetweenTwoVectors(float[] vec1, float[] vec2) {
		double angle = Math.acos((double) (dotProduct(vec1, vec2) / ( Length(vec1) * Length(vec2) ) ) );
		return (int) Math.toDegrees(angle);
	}

	public static int angleBetweenTwoVectors(Float[] vec1, Float[] vec2) {
		float[] v1 = new float[] {vec1[0], vec1[1]};
		float[] v2 = new float[] {vec2[0], vec2[1]};
		
		return angleBetweenTwoVectors(v1, v2);
	}
	
	public static float Length(float[] vec) {
		return (float)Math.sqrt(Math.pow((double)vec[0], 2.0) + Math.pow((double)vec[1],2.0));
	}
	
	public static float[] Normalize(float[] vec) {
		float length = Length(vec);
		return new float[] {vec[0]/length, vec[1]/length};
	}
	
	public static float[] Add(float[] vec1, float[] vec2) {
		return new float[] {vec1[0]+vec2[0], vec1[1]+vec2[1]};
	}

	public static Float[] Add(Float[] vec1, Float[] vec2) {
		return new Float[] {vec1[0]+vec2[0], vec1[1]+vec2[1]};
	}	
	
	public static float[] MultiplyScalar(float[] vec, float scalar) {
		return new float[] {vec[0]*scalar, vec[1]*scalar};
	}
	
	public static float[] Subtract(float[] vec1, float[] vec2) {
		return new float[] {vec1[0]-vec2[0], vec1[1]-vec2[1]};
	}

	public static Float[] Subtract(Float[] vec1, Float[] vec2) {
		return new Float[] {vec1[0]-vec2[0], vec1[1]-vec2[1]};
	}	
	
	public static float Clamp(float value, float min, float max) {
		
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
	}
	
	public static int Clamp(int value, int min, int max) {
		
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
	}
	
	public static float[] RotateVector(float[] vec, int degrees) {
		float[] rotated = new float[2];
		double rad = Math.toRadians(degrees);
		
		rotated[0] = vec[0] * (float) Math.cos(rad) - vec[1] * (float)Math.sin(rad);
		rotated[1] = vec[0] * (float) Math.sin(rad) + vec[1] * (float)Math.cos(rad);
		
		return rotated;
	}
	
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	public static float[] getDrawPoint(float[] unitVector, int degrees, float lineLength, float[] lineStartCoords) {
    	float[] rotated = Vector2D.RotateVector(unitVector, degrees);
    	float[] normalizedVec = Vector2D.Normalize(rotated);
    	float[] scaledVec = Vector2D.MultiplyScalar(normalizedVec, lineLength);
    	float[] drawVector = Vector2D.Add(scaledVec, lineStartCoords);
    	return drawVector;
	}

}
