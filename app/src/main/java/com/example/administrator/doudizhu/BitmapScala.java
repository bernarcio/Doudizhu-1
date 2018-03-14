package com.example.administrator.doudizhu;

import android.graphics.Bitmap;
import android.graphics.Matrix;

class BitmapScala
{

	static Bitmap scalamap (Bitmap bitmap, float w, float h)
	{
		if (bitmap == null) //判断Bitmap
			return null;

		int width = bitmap.getWidth ();
		int height = bitmap.getHeight ();
		Matrix matrix = new Matrix ();
		float scaleWidth = w / (float) width;
		float scaleHeight = h / (float) height;

		matrix.postScale (scaleWidth, scaleHeight);
		return Bitmap.createBitmap (bitmap, 0, 0, width, height, matrix, true);
	}
}
