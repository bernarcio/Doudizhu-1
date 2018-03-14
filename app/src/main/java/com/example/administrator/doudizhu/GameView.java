package com.example.administrator.doudizhu;

import android.content.Context;
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

public class GameView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener
{
	private Game game;
	private CardImage cardImage;
	private Bitmap background = null;
	private Bitmap player1_p = null;
	private Bitmap player2_p = null;
	private Bitmap player3_p = null;
	private Bitmap desk = null;
	private Bitmap begin = null;
	private Bitmap cards[];
	private Bitmap card_back;

	private Bitmap card_back_s;
	private Bitmap cards_s[];
	private Bitmap cards_m[];
	private Bitmap no1;
	private Bitmap pass;
	private Bitmap discard;
	private Bitmap score11;
	private Bitmap score12;
	private Bitmap score13;
	private Bitmap score21;
	private Bitmap score22;
	private Bitmap score23;
	private Bitmap score31;
	private Bitmap score32;
	private Bitmap score33;
	private Bitmap lord_logo;
	private Bitmap noDiscard;
	private Bitmap time;
	private Bitmap restart;
	private Bitmap win;
	private Bitmap fail;

	private Context gamePlayViewContext;
	boolean flag = true;
	SurfaceHolder surfaceHolder;
	//response pixel
	private int retx = 0, rety = 0;
	//表示游戏进行时页面的不同状态 0：游戏界面 1：菜单界面
	// 2：停止界面 3：停止菜单界面 4：个人信息界面5:提示界面6：结束界面
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

	public GameView (Context context, Game game)
	{
		super (context);
		this.game = game;
		//game.callLandlord();
		//selected = new Boolean[54];

		gamePlayViewContext = context;
		draw (canvas);
		cardImage = new CardImage ();
		cards = new Bitmap[54];
		cards_s = new Bitmap[54];
		cards_m = new Bitmap[54];
		surfaceHolder = getHolder ();
		Resources resources = getResources ();
		background = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.place), 800 * GameActivity.SCREEN_WIDTH / 800, 480 * GameActivity.SCREEN_HEIGHT / 480);
		desk = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.desk), 700 * GameActivity.SCREEN_WIDTH / 800, 430 * GameActivity.SCREEN_HEIGHT / 480);

		player1_p = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.lf), 88 * GameActivity.SCREEN_WIDTH / 800, 142 * GameActivity.SCREEN_HEIGHT / 480);
		player2_p = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.as), 188 * GameActivity.SCREEN_WIDTH / 800, 142 * GameActivity.SCREEN_HEIGHT / 480);

		player3_p = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.sb2), 128 * GameActivity.SCREEN_WIDTH / 800, 142 * GameActivity.SCREEN_HEIGHT / 480);
		begin = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.begin), 200 * GameActivity.SCREEN_WIDTH / 800, 50 * GameActivity.SCREEN_HEIGHT / 480);
		card_back = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.card_back), 108 * GameActivity.SCREEN_WIDTH / 800, 85 * GameActivity.SCREEN_HEIGHT / 480);
		card_back_s = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.card_back), 40 * GameActivity.SCREEN_WIDTH / 800, 54 * GameActivity.SCREEN_HEIGHT / 480);

		no1 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.no1), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score11 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score11), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score12 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score12), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score13 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score13), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score21 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score21), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score22 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score22), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score23 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score23), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score31 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score31), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score32 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score32), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		score33 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score33), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);

		discard = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.discard1), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		pass = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.pass1), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);

		lord_logo = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.lord_logo), 50 * GameActivity.SCREEN_WIDTH / 800, 50 * GameActivity.SCREEN_HEIGHT / 480);
		noDiscard = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.nodiscard), 50 * GameActivity.SCREEN_WIDTH / 800, 50 * GameActivity.SCREEN_HEIGHT / 480);
		time = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.time), 50 * GameActivity.SCREEN_WIDTH / 800, 50 * GameActivity.SCREEN_HEIGHT / 480);
		restart = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.restart1), 200 * GameActivity.SCREEN_WIDTH / 800, 100 * GameActivity.SCREEN_HEIGHT / 480);
		win = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.win1), 400 * GameActivity.SCREEN_WIDTH / 800, 350 * GameActivity.SCREEN_HEIGHT / 480);
		fail = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.failed), 400 * GameActivity.SCREEN_WIDTH / 800, 350 * GameActivity.SCREEN_HEIGHT / 480);

		//初始化卡牌
		for (int i = 0; i < 54; i++)
		{
			Bitmap temp = BitmapFactory.decodeResource (context.getResources (), cardImage.cardImages[i / 4][i % 4]);
			cards[i] = BitmapScala.scalamap (temp, 80 * GameActivity.SCREEN_WIDTH / 800, 120 * GameActivity.SCREEN_HEIGHT / 480);
			cards_m[i] = BitmapScala.scalamap (temp, 60 * GameActivity.SCREEN_WIDTH / 800, 90 * GameActivity.SCREEN_HEIGHT / 480);
			cards_s[i] = BitmapScala.scalamap (temp, 40 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			game.select[i] = false;
		}

		this.getHolder ().addCallback (this);
		this.setOnTouchListener (this);
	}

	private void initPokers (Canvas canvas)
	{
		int n = 0;
		for (int i = 53; i >= 0; i--)
		{
			if (game.players[0].handCard[i] && (!game.select[i]))
			{
				canvas.drawBitmap (cards[i], (100 + n * 25) * GameActivity.SCREEN_WIDTH / 800, 350 * GameActivity.SCREEN_HEIGHT / 480, null);
				//  canvas.drawBitmap(card_back, 60 * GameActivity.SCREEN_WIDTH / 800, (350-n*20) * GameActivity.SCREEN_HEIGHT / 480, null);
				// canvas.drawBitmap(card_back, 632 * GameActivity.SCREEN_WIDTH / 800, (350-n*20) * GameActivity.SCREEN_HEIGHT / 480, null);
				n++;
			}
			else if (game.players[0].handCard[i] && (game.select[i]))
			{
				canvas.drawBitmap (cards[i], (100 + n * 25) * GameActivity.SCREEN_WIDTH / 800, 330 * GameActivity.SCREEN_HEIGHT / 480, null);
				//  canvas.drawBitmap(card_back, 60 * GameActivity.SCREEN_WIDTH / 800, (350-n*20) * GameActivity.SCREEN_HEIGHT / 480, null);
				// canvas.drawBitmap(card_back, 632 * GameActivity.SCREEN_WIDTH / 800, (350-n*20) * GameActivity.SCREEN_HEIGHT / 480, null);
				n++;
			}
		}
		canvas.drawBitmap (card_back_s, 620 * GameActivity.SCREEN_WIDTH / 800, 100 * GameActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (card_back_s, 100 * GameActivity.SCREEN_WIDTH / 800, 100 * GameActivity.SCREEN_HEIGHT / 480, null);
		Paint paint = new Paint ();
		paint.setTextSize (60);

		String str1 = String.valueOf (game.players[1].getHandCardNum ());
		String str2 = String.valueOf (game.players[2].getHandCardNum ());
		canvas.drawText (str1, 620 * GameActivity.SCREEN_WIDTH / 800, 130 * GameActivity.SCREEN_HEIGHT / 480, paint);
		canvas.drawText (str2, 100 * GameActivity.SCREEN_WIDTH / 800, 130 * GameActivity.SCREEN_HEIGHT / 480, paint);
	}

	private void draw_card_loard (Canvas canvas)
	{
		if (game.status == Game.Status.GetLandlord || game.status == Game.Status.NotStart)
		{
			canvas.drawBitmap (card_back_s, 320 * GameActivity.SCREEN_WIDTH / 800, 20 * GameActivity.SCREEN_HEIGHT / 480, null);
			canvas.drawBitmap (card_back_s, 380 * GameActivity.SCREEN_WIDTH / 800, 20 * GameActivity.SCREEN_HEIGHT / 480, null);
			canvas.drawBitmap (card_back_s, 440 * GameActivity.SCREEN_WIDTH / 800, 20 * GameActivity.SCREEN_HEIGHT / 480, null);
		}
		else if (game.status == Game.Status.SetLandlord)
		{
			game.status = Game.Status.Discard;
		}
		else if (game.status == Game.Status.Discard)
		{
			int[] a = game.lord_show;
			canvas.drawBitmap (cards_s[a[0]], 320 * GameActivity.SCREEN_WIDTH / 800, 20 * GameActivity.SCREEN_HEIGHT / 480, null);
			canvas.drawBitmap (cards_s[a[1]], 380 * GameActivity.SCREEN_WIDTH / 800, 20 * GameActivity.SCREEN_HEIGHT / 480, null);
			canvas.drawBitmap (cards_s[a[2]], 440 * GameActivity.SCREEN_WIDTH / 800, 20 * GameActivity.SCREEN_HEIGHT / 480, null);
		}
	}

	private void draw_other_scores (Canvas canvas)
	{
		Paint paint = new Paint ();
		paint.setTextSize (60);

		String str1 = String.valueOf (game.players[1].getCallScore ()) + "分";
		String str2 = String.valueOf (game.players[2].getCallScore ()) + "分";
		if (game.something_init[1])
		{
			canvas.drawText (str1, 620 * GameActivity.SCREEN_WIDTH / 800, 80 * GameActivity.SCREEN_HEIGHT / 480, paint);
		}
		if (game.something_init[2])
		{
			canvas.drawText (str2, 100 * GameActivity.SCREEN_WIDTH / 800, 80 * GameActivity.SCREEN_HEIGHT / 480, paint);
		}
	}

	private void draw_button1 (Canvas canvas)
	{    //不叫 1分 2 分 3 分
		canvas.drawBitmap (no1, 170 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
		switch (game.maxScore)
		{
			case 0:
				canvas.drawBitmap (score11, 290 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				canvas.drawBitmap (score21, 410 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				canvas.drawBitmap (score31, 530 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				break;
			case 1:
				canvas.drawBitmap (score13, 290 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				canvas.drawBitmap (score21, 410 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				canvas.drawBitmap (score31, 530 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				break;
			case 2:
				canvas.drawBitmap (score13, 290 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				canvas.drawBitmap (score23, 410 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				canvas.drawBitmap (score31, 530 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
				break;
		}
	}

	private void draw_button2 (Canvas canvas)
	{     //出牌  不出
		Resources resources = getResources ();
		if (game.my_world)
		{
			//   pass = BitmapScala.scalamap(BitmapFactory.decodeResource(resources, R.drawable.pass), 108* GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			pass = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.pass3), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		}
		else
		{
			pass = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.pass1), 100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
		}


		canvas.drawBitmap (discard, 410 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (pass, 290 * GameActivity.SCREEN_WIDTH / 800, 260 * GameActivity.SCREEN_HEIGHT / 480, null);


	}


	private void draw_lord_logo (Canvas canvas)
	{
		if (game.landlord.getNo () == 0)
		{
			canvas.drawBitmap (lord_logo, 40 * GameActivity.SCREEN_WIDTH / 800, 280 * GameActivity.SCREEN_HEIGHT / 480, null);
		}
		else if (game.landlord.getNo () == 1)
		{
			canvas.drawBitmap (lord_logo, 650 * GameActivity.SCREEN_WIDTH / 800, 40 * GameActivity.SCREEN_HEIGHT / 480, null);

		}
		else if (game.landlord.getNo () == 2)
		{
			canvas.drawBitmap (lord_logo, 50 * GameActivity.SCREEN_WIDTH / 800, 100 * GameActivity.SCREEN_HEIGHT / 480, null);

		}
	}

	private void draw_show_cards (Canvas canvas)
	{
		int n = 0;
		if (game.players[0].getNoDiscard ())
		{
			canvas.drawBitmap (noDiscard, (300 + 20 * n) * GameActivity.SCREEN_WIDTH / 800, 250 * GameActivity.SCREEN_HEIGHT / 480, null);

		}
		else
		{
			for (int i = 0; i < 54; i++)
			{
				if (game.players[0].getDiscard ().hasCard (i))
				{
					canvas.drawBitmap (cards_m[i], (300 + 20 * n) * GameActivity.SCREEN_WIDTH / 800, 250 * GameActivity.SCREEN_HEIGHT / 480, null);
					n++;
				}
			}
		}
		n = 0;
		if (game.players[2].getNoDiscard ())
		{
			canvas.drawBitmap (noDiscard, (200) * GameActivity.SCREEN_WIDTH / 800, 120 * GameActivity.SCREEN_HEIGHT / 480, null);
		}
		else
		{
			for (int i = 0; i < 54; i++)
			{
				if (game.player2_show[i])
				{
					canvas.drawBitmap (cards_m[i], (200 + n * 20) * GameActivity.SCREEN_WIDTH / 800, 120 * GameActivity.SCREEN_HEIGHT / 480, null);
					n++;
				}
			}
		}
		n = 0;
		if (game.players[1].getNoDiscard ())
			canvas.drawBitmap (noDiscard, (500) * GameActivity.SCREEN_WIDTH / 800, 120 * GameActivity.SCREEN_HEIGHT / 480, null);

		else
			for (int i = 0; i < 54; i++)
				if (game.player1_show[i])
				{
					canvas.drawBitmap (cards_m[i], (500 + n * 20) * GameActivity.SCREEN_WIDTH / 800, 120 * GameActivity.SCREEN_HEIGHT / 480, null);
					n++;
				}
	}

	private void draw_curr (Canvas canvas)
	{
		//绘制图标给正在打牌玩家
		int x = 0;
		int y = 0;
		if (game.curPlayer.getNo () == 0)
		{
			x = 100;
			y = 300;
		}
		else if (game.curPlayer.getNo () == 1)
		{
			x = 700;
			y = 120;
		}
		else if (game.curPlayer.getNo () == 2)
		{
			x = 100;
			y = 120;
		}
		canvas.drawBitmap (time, x * GameActivity.SCREEN_WIDTH / 800, y * GameActivity.SCREEN_HEIGHT / 480, null);
	}

	private void draw_not_fit (Canvas canvas)
	{
		//出牌不符合规则时绘制
		Paint paint = new Paint ();
		paint.setTextSize (70);
		String str = "你选的牌不符合";
		canvas.drawText (str, 300 * GameActivity.SCREEN_WIDTH / 800, 200 * GameActivity.SCREEN_HEIGHT / 480, paint);
	}

	private void draw_final_view (Canvas canvas)
	{
		if (game.players[0].getHandCardNum () == 0)
			canvas.drawBitmap (win, 200 * GameActivity.SCREEN_WIDTH / 800, 65 * GameActivity.SCREEN_HEIGHT / 480, null);

		else
			canvas.drawBitmap (fail, 200 * GameActivity.SCREEN_WIDTH / 800, 65 * GameActivity.SCREEN_HEIGHT / 480, null);

		canvas.drawBitmap (restart, 300 * GameActivity.SCREEN_WIDTH / 800, 300 * GameActivity.SCREEN_HEIGHT / 480, null);
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
		canvas.drawBitmap (desk, 50 * GameActivity.SCREEN_WIDTH / 800, 50 * GameActivity.SCREEN_HEIGHT / 480, null);

		canvas.drawBitmap (player1_p, 10 * GameActivity.SCREEN_WIDTH / 800, 326 * GameActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (player2_p, (-50) * GameActivity.SCREEN_WIDTH / 800, 12 * GameActivity.SCREEN_HEIGHT / 480, null);
		canvas.drawBitmap (player3_p, 672 * GameActivity.SCREEN_WIDTH / 800, 12 * GameActivity.SCREEN_HEIGHT / 480, null);

		initPokers (canvas);

		draw_card_loard (canvas);
		if (game.status == Game.Status.GetLandlord && game.callBegin == 0)
			draw_button1 (canvas);

		if (game.status == Game.Status.GetLandlord)
			draw_other_scores (canvas);

		if (game.status == Game.Status.Discard && game.curPlayer.getNo () == 0)
			draw_button2 (canvas);

		if (game.status == Game.Status.Discard)
		{
			draw_curr (canvas);
			draw_lord_logo (canvas);
			draw_show_cards (canvas);
			if (game.not_fit)
				draw_not_fit (canvas);
		}

		if (game.status == Game.Status.Wait)
			draw_final_view (canvas);
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
		// 判断点击正面牌的区域
		if (rety > 320 * GameActivity.SCREEN_HEIGHT / 480 && rety < 458 * GameActivity.SCREEN_HEIGHT / 480 && game.status == Game.Status.Discard)
		{
			int n = (retx * 800 / GameActivity.SCREEN_WIDTH - 100) / 25;
			if ((retx * 800 / GameActivity.SCREEN_WIDTH >= (75 + game.players[0].getHandCardNum () * 25)) && (retx * 800 / GameActivity.SCREEN_WIDTH <= (85 + 75 + game.players[0].getHandCardNum () * 25)))
				n = game.players[0].getHandCardNum () - 1;

			if ((n < game.players[0].getHandCardNum ()) && (n >= 0))
				for (int i = 53; i >= 0; i--)
				{
					if (game.players[0].handCard[i])
						n--;

					if (n == -1 && game.select[i])
					{
						game.select[i] = false;
						return true;
					}

					else if (n == -1 && (!game.select[i]))
					{
						game.select[i] = true;
						return true;
					}
				}

			return true;
		}

		//按钮出牌
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 410 * GameActivity.SCREEN_HEIGHT / 480 && retx < 510 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.Discard)
		{
			Resources resources = getResources ();
			discard = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.discard2),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		//按钮不出
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 290 * GameActivity.SCREEN_HEIGHT / 480 && retx < 390 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.Discard && (!game.my_world))
		{
			Resources resources = getResources ();
			pass = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.pass2),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		// 不叫
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 170 * GameActivity.SCREEN_HEIGHT / 480 && retx < 270 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			no1 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.no2),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		//1分
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 290 * GameActivity.SCREEN_HEIGHT / 480 && retx < 390 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			score11 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score12),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		// 2分
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 410 * GameActivity.SCREEN_HEIGHT / 480 && retx < 510 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			score21 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score22),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		// 3分
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 530 * GameActivity.SCREEN_HEIGHT / 480 && retx < 630 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			score31 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score32),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		//重新开始游戏
		else if (rety > 190 * GameActivity.SCREEN_HEIGHT / 480 && rety < 240 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 300 * GameActivity.SCREEN_HEIGHT / 480 && retx < 500 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.Wait)
		{
			game.final_select = 1;
			Resources resources = getResources ();
			restart = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.restart2),
					200 * GameActivity.SCREEN_WIDTH / 800, 100 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		return false;
	}

	public boolean playViewUp ()
	{
		//按钮出牌
		if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 410 * GameActivity.SCREEN_HEIGHT / 480 && retx < 510 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.Discard)
		{
			Resources resources = getResources ();

			discard = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.discard1),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			game.noDiscard = false;
			game.touch_button = true;
			return true;
		}

		//按钮不出
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 290 * GameActivity.SCREEN_HEIGHT / 480 && retx < 390 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.Discard && (!game.my_world))
		{
			Resources resources = getResources ();
			pass = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.pass1),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			game.noDiscard = true;
			game.touch_button = true;
			return true;
		}

		// 不叫
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 170 * GameActivity.SCREEN_HEIGHT / 480 && retx < 270 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			no1 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.no1),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			game.manScore = true;
			game.players[0].setCallScore (0);
			return true;
		}

		//1分
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 290 * GameActivity.SCREEN_HEIGHT / 480 && retx < 390 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			score11 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score11),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			game.manScore = true;
			game.players[0].setCallScore (1);
			return true;
		}

		// 2分
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 410 * GameActivity.SCREEN_HEIGHT / 480 && retx < 510 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			game.manScore = true;
			game.players[0].setCallScore (2);
			score21 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score21),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
			return true;
		}

		// 3分
		else if (rety > 260 * GameActivity.SCREEN_HEIGHT / 480 && rety < 320 * GameActivity.SCREEN_HEIGHT / 480
				&& retx > 530 * GameActivity.SCREEN_HEIGHT / 480 && retx < 630 * GameActivity.SCREEN_HEIGHT / 480
				&& game.status == Game.Status.GetLandlord)
		{
			Resources resources = getResources ();
			game.manScore = true;
			game.players[0].setCallScore (3);
			score31 = BitmapScala.scalamap (BitmapFactory.decodeResource (resources, R.drawable.score31),
					100 * GameActivity.SCREEN_WIDTH / 800, 60 * GameActivity.SCREEN_HEIGHT / 480);
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
