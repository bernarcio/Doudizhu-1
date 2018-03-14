package com.example.administrator.doudizhu;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity
{
	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static double SCALE_VERTICAL;
	public static double SCALE_HORIAONTAL;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		getWindow ().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		DisplayMetrics sizeMatrix = new DisplayMetrics ();
		this.getWindowManager ().getDefaultDisplay ().getMetrics (sizeMatrix);
		SCREEN_WIDTH = sizeMatrix.widthPixels;
		SCREEN_HEIGHT = sizeMatrix.heightPixels;
		if (SCREEN_WIDTH < SCREEN_HEIGHT)
		{
			SCREEN_HEIGHT += SCREEN_WIDTH;
			SCREEN_WIDTH = SCREEN_HEIGHT - SCREEN_WIDTH;
			SCREEN_HEIGHT = SCREEN_HEIGHT - SCREEN_WIDTH;
		}
		//长宽缩放比
		SCALE_VERTICAL = SCREEN_HEIGHT / 360.0;
		SCALE_HORIAONTAL = SCREEN_WIDTH / 520.0;

		final String widthAndHeight = MainActivity.SCREEN_WIDTH + " " + MainActivity.SCREEN_HEIGHT;
		Intent intent1 = new Intent (MainActivity.this, GameActivity.class);
		intent1.putExtra ("widthAndHeight", widthAndHeight);

		MainView mainView = new MainView (this, intent1);
		setContentView (mainView);
	}
}
