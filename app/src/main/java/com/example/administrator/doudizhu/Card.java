package com.example.administrator.doudizhu;
import java.util.Random;

class Card
{
	private static Card card = new Card ();
	private int[] fullCard = new int[54];

	private Card ()
	{
		for (int i = 0; i < 17; ++i)
			fullCard[i] = 0;
		for (int i = 17; i < 34; ++i)
			fullCard[i] = 1;
		for (int i = 34; i < 51; ++i)
			fullCard[i] = 2;
		for (int i = 51; i < 54; ++i)
			fullCard[i] = -1;
		swap ();
	}

	//交互手牌当作洗牌
	private void swap ()
	{
		Random random = new Random (System.currentTimeMillis ());
		int temp, a, b;
		for (int i = 0; i < 500; )
		{
			a = random.nextInt (54);
			b = random.nextInt (54);
			if (a != b)
			{
				temp = fullCard[a];
				fullCard[a] = fullCard[b];
				fullCard[b] = temp;
				++i;
			}
		}
	}

	//单例
	static Card getCard ()
	{
		return card;
	}

	//获取手牌
	boolean[] getHandCard (int No)
	{
		if (!(No > -1 && No < 3))
			throw new AssertionError ();
		boolean[] handCard = new boolean[54];
		for (int i = 0; i < fullCard.length; ++i)
			handCard[i] = (fullCard[i] == No);
		return handCard;
	}

	//获取手牌数量
	int getHandCardNum (int No)
	{
		if (!(No > -1 && No < 3))
			throw new AssertionError ();
		int num = 0;
		for (int aFullCard : fullCard)
			if (aFullCard == No)
				++num;
		return num;
	}

	//分配地主牌
	int[] setLandlord (int No)
	{
		if (!(No > -1 && No < 3))
			throw new AssertionError ();
		int[] landlordCard = new int[3];
		for (int i = 0, j = 0; i < fullCard.length; ++i)
			if (fullCard[i] == -1)
			{
				fullCard[i] = No;
				landlordCard[j] = i;
				++j;
			}

		//返回一个长度为三的数组，每个的值对应[54]中的一个下标，表示一张牌
		return landlordCard;
	}

	//打出去的牌
	void deleteCard (int i)
	{
		fullCard[i] = -1;
	}
}