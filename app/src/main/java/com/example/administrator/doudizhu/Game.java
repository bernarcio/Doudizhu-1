package com.example.administrator.doudizhu;

import java.util.Arrays;
import java.util.Random;

class Game
{
	private static Game game = new Game ();

	Status status; //游戏进度
	Card cardHeap; //发牌堆
	Player[] players; //真人玩家编号为0
	Player curPlayer; //当前出牌玩家
	Player lastOne; //最后出牌方
	Player landlord; //地主
	Boolean noDiscard = false;
	Boolean touch_button = false;
	Boolean my_world = false;  //判断是否是自己任意出牌局；
	int maxScore = 0; //当前叫地主分数最高的玩家分数
	int callBegin; //当前访问的下标
	boolean manScore = false; //真人玩家已经选择叫地主分数
	Boolean select[];   //选择的牌
	boolean game_begin = false; //控制游戏线程的开始结束
	boolean not_fit = false;     //选牌不合适
	boolean something_init[];
	private boolean first_get_cards1 = true;
	private boolean first_get_cards2 = true;
	int final_select = 0;
	Boolean player1_show[];
	Boolean player2_show[];
	int[] lord_show = new int[3];

	//游戏进度状态
	enum Status
	{
		NotStart, //游戏未开始
		GetLandlord, //叫地主阶段
		SetLandlord, //发地主牌阶段
		Discard, //出牌阶段
		Wait, //完成一局后等待选择
		GameOver //游戏结束
	}

	private Game ()
	{
		status = Status.NotStart;
		select = new Boolean[54];
		player1_show = new Boolean[54];
		player2_show = new Boolean[54];
		something_init = new boolean[3];
		something_init[0] = false;
		something_init[1] = false;
		something_init[2] = false;
		lord_show[0] = -1;
		for (int i = 0; i < 54; ++i)
		{
			select[i] = false;
			player1_show[i] = false;
			player2_show[i] = false;
		}
	}

	private void deal ()
	{
		//发牌函数
		cardHeap = Card.getCard (); //洗牌
		//初始化角色
		players = new Player[] {new Player (0, this),
				new Player (1, this), new Player (2, this)};
	}

	static Game getGame ()
	{
		return game;
	}

	//开始新游戏
	void startGame ()
	{
		curPlayer = lastOne = null;
		deal ();
		status = Status.GetLandlord;
	}

	//叫地主
	void callLandlord ()
	{
		//随机确定开始询问的玩家
		callBegin = new Random (System.currentTimeMillis ()).nextInt (3);

		//当前叫地主分数最高的玩家下标
		int maxIndex = -1;
		//玩家选择的分数

		//循环询问
		for (int questioned = 0; questioned < 3; ++questioned)
		{
			//四个按钮，不叫->0分，1分，2分，3分
			if (callBegin != 0) //电脑玩家
				players[callBegin].setCallScore (players[callBegin].getBaseScoreAI ());
			else
				while (!manScore) {}

			if (players[callBegin].getCallScore () > maxScore)
			{
				maxScore = players[callBegin].getCallScore ();
				maxIndex = callBegin;

				try
				{
					Thread.sleep (500);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace ();
				}

				//如果有三分叫地主的直接就是地主了
				if (players[callBegin].getCallScore () == 3)
					break;
			}

			something_init[players[callBegin].getNo ()] = true;
			callBegin = (callBegin + 1) % 3;
		}

		try
		{
			Thread.sleep (1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace ();
		}

		//这里如果电脑没有选地主则必然会跳到下面，因为没有给一个延迟
		if (maxIndex == -1) //如果没人叫地主，重新开始游戏
			status = Status.NotStart;

		else //如果有人叫地主
		{
			//地主先出牌
			curPlayer = lastOne = landlord = players[maxIndex];
			//发地主牌
			//返回一个长度为三的数组，每个的值对应[54]中的一个下标，表示一张牌
			//在game中记录地主，地主牌
			int[] landlordCard = cardHeap.setLandlord (maxIndex);
			if (lord_show[0] == -1)
				lord_show = Arrays.copyOf (landlordCard, landlordCard.length);

			//对应玩家加入地主牌
			players[maxIndex].setHandCard ();

			//每个玩家记录下谁是地主
			for (Player player : players)
				player.setLandlord (maxIndex);

			//更新游戏状态
			status = Status.SetLandlord;
		}
	}

	//出牌
	void discard ()
	{
		//如果该玩家出牌没人跟牌，自己开始新一轮出牌
		if (lastOne == curPlayer)
		{
			for (int i = 0; i < 3; ++i)
				if (players[i].getDiscard () != null)
					players[i].getDiscard ().clear ();
		}

		//如果是跟牌
		else
		{
			if (curPlayer.getDiscard () != null)
				curPlayer.getDiscard ().clear ();

			curPlayer.setNoDiscard (false);
		}

		//如果是真人玩家
		if (curPlayer == players[0])
		{
			my_world = curPlayer.getNo () == lastOne.getNo ();
			boolean flag = true;
			not_fit = false;
			while (flag)
				if (touch_button)
				{
					not_fit = false;
					touch_button = false;
					if (noDiscard) //点击不出按钮的时候进入这里
					{
						flag = false;
						curPlayer.setNoDiscard (true);
						for (int i = 0; i < 54; ++i)
							select[i] = false;   //清选牌
					}

					else //点击出牌按钮的时候进入这里
					{
						if (curPlayer.humanDiscard (select))
						{
							my_world = false;
							flag = false;
							curPlayer.setNoDiscard (false);
							for (int i = 0; i < 54; ++i)
								select[i] = false;
							lastOne = curPlayer;
						}
						else
						{
							not_fit = true;
							for (int i = 0; i < 54; ++i)
								select[i] = false;
						}
						// break;
					}
				} // end if (touch_button)
		}

		//如果是电脑玩家
		else
		{
			//清空此玩家显示的牌
			if (curPlayer.getNo () == 1)
			{
				first_get_cards1 = true;
				for (int i = 0; i < 54; ++i)
					player1_show[i] = false;
			}
			else if (curPlayer.getNo () == 2)
			{
				first_get_cards2 = true;
				for (int i = 0; i < 54; ++i)
					player2_show[i] = false;
			}

			try
			{
				Thread.sleep (500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace ();
			}

			//选牌
			curPlayer.selectCards ();
			//出牌
			if (curPlayer.discard ())
			{
				lastOne = curPlayer;

				//根据打出去的牌更新与ui相关的数组
				if (first_get_cards1 && curPlayer.getNo () == 1)
				{
					for (int i = 0; i < 54; ++i)
						player1_show[i] = curPlayer.getDiscard ().hasCard (i);
					first_get_cards1 = false;
				}

				if (first_get_cards2 && curPlayer.getNo () == 2)
				{
					for (int i = 0; i < 54; ++i)
						player2_show[i] = curPlayer.getDiscard ().hasCard (i);
					first_get_cards2 = false;
				}
			}
			else
				curPlayer.setNoDiscard (true);
		}

		if (curPlayer.getHandCardNum () == 0) //如果打完没有手牌了
			status = Status.Wait;//游戏结束

		else //如果没人赢就到下一个玩家
			curPlayer = players[(curPlayer.getNo () + 1) % 3];
	}

	void wait_final ()
	{
		while (final_select == 0) {}
		status = Status.GameOver;
	}
}
