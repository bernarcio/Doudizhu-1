package com.example.administrator.doudizhu;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener
{
	private Bitmap background = null;
	private Bitmap begin = null;
	private Bitmap as = null;
	private Bitmap lf = null;
	private Bitmap sb = null;

	private Context MainViewContext;
	boolean flag = true;
	SurfaceHolder surfaceHolder;
	private int retx = 0, rety = 0;
	private Intent intent;
	int playViewState = 0;

	private Canvas canvas;
	Runnable sendable = new Runnable ()
	{
		@Override
		public void run ()
		{
			while (flag)
			{
				try
				{
					canvas = surfaceHolder.lockCanvas ();
					if (canvas != null)
						synchronized (this)
						{
							draw (canvas);
							Thread.sleep (30);
							surfaceHolder.unlockCanvasAndPost (canvas);
						}
				}
				catch (InterruptedException e)
				{
					Thread.currentThread ().interrupt ();
				}

				try
				{
					Thread.sleep (30);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace ();
				}
			}
		}
	};

	public MainView (Context context, Intent intent)
	{
		super (context);
		this.intent = intent;

		MainViewContext = context;
		draw (canvas);

		surfaceHolder = getHolder ();
		Resources resources = getResources ();
		background = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.place), 800 * MainActivity.SCREEN_WIDTH / 800, 480 * MainActivity.SCREEN_HEIGHT / 480);
		begin = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.begin), 200 * MainActivity.SCREEN_WIDTH / 800, 100 * MainActivity.SCREEN_HEIGHT / 480);
		lf = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.lf1), 400 * MainActivity.SCREEN_WIDTH / 800, 300 * MainActivity.SCREEN_HEIGHT / 480);
		sb = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.sb1), 400 * MainActivity.SCREEN_WIDTH / 800, 300 * MainActivity.SCREEN_HEIGHT / 480);
		as = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.as1), 400 * MainActivity.SCREEN_WIDTH / 800, 300 * MainActivity.SCREEN_HEIGHT / 480);

		this.getHolder ().addCallback (this);
		this.setOnTouchListener (this);
	}

	@Override
	public void onDraw (Canvas canvas)
	{
		super.onDraw (canvas);
	}

	public void draw (Canvas canvas)
	{
		if (canvas == null)
			return;

		super.draw (canvas);
		Rect src = new Rect ();
		Rect des = new Rect ();
		//设置变量来绘制全局背景图
		src.set (0, 0, background.getWidth (), background.getHeight ());
		des.set (0, 0, MainActivity.SCREEN_WIDTH, MainActivity.SCREEN_HEIGHT);
		//定义画笔
		Paint paint = new Paint ();

		canvas.drawBitmap (background, src, des, paint);
		canvas.drawBitmap (sb, 200 * MainActivity.SCREEN_WIDTH / 800, 180 * MainActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (lf, 0 * MainActivity.SCREEN_WIDTH / 800, 180 * MainActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (as, 400 * MainActivity.SCREEN_WIDTH / 800, 180 * MainActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (begin, 300 * MainActivity.SCREEN_WIDTH / 800, 100 * MainActivity.SCREEN_HEIGHT / 480, null);
	}

	@Override
	public void surfaceChanged (SurfaceHolder holder, int format, int width, int height)
	{
	}

	@Override
	public void surfaceDestroyed (SurfaceHolder holder)
	{
		flag = false;
	}

	@Override
	public boolean onTouch (View v, MotionEvent event)
	{
		int iAction = event.getAction ();
		if (iAction == MotionEvent.ACTION_DOWN)
		{
			retx = (int) event.getRawX ();
			rety = (int) event.getRawY ();
			if (playViewState == 0)
				return playViewDown ();
		}

		if (iAction == MotionEvent.ACTION_UP)
		{
			retx = (int) event.getRawX ();
			rety = (int) event.getRawY ();
			if (playViewState == 0)
				return playViewUp ();
		}

		return true;
	}

	//Listener definition
	public boolean playViewDown ()
	{
		return rety > 100 * MainActivity.SCREEN_HEIGHT / 480 && rety < 200 * MainActivity.SCREEN_HEIGHT / 480
				&& retx > 300 * MainActivity.SCREEN_HEIGHT / 480 && retx < 500 * MainActivity.SCREEN_HEIGHT / 480;
	}

	public boolean playViewUp ()
	{
		if (rety > 100 * MainActivity.SCREEN_HEIGHT / 480 && rety < 200 * MainActivity.SCREEN_HEIGHT / 480
				&& retx > 300 * MainActivity.SCREEN_HEIGHT / 480 && retx < 500 * MainActivity.SCREEN_HEIGHT / 480)
		{
			MainViewContext.startActivity (intent);
			return true;
		}

		return false;
	}

	@Override
	public void surfaceCreated (SurfaceHolder holder)
	{
		flag = true;
		new Thread (sendable).start ();
	}
}
