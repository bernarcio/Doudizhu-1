package com.example.administrator.doudizhu;

import java.util.ArrayList;
import java.util.Arrays;

class Player
{
	//用0到53，从小到大排，0为方块三，53为大王
	//当前手牌，true表示有，false表示没有
	boolean[] handCard;
	//分析后拆分的牌型集合
	private ArrayList<CardGroup> analyse = new ArrayList<> ();
	//选择牌的集合
	private CardGroup selection = new CardGroup ();
	//打出的牌的集合
	private CardGroup discard = new CardGroup ();
	//角色，true为地主，false为农民
	private boolean isLandlord;
	//上家角色
	private boolean leftLandlord;
	//下家角色
	private boolean rightLandlord;
	//编号标签
	private int No;
	//是否试过送下家走
	private boolean test;
	//叫地主的分数
	private int callScore = 0;
	//不出标志
	private boolean noDiscard;
	//游戏对象
	private Game game;

	Player (int No, Game game)
	{
		handCard = game.cardHeap.getHandCard (No);
		this.No = No;
		this.game = game;
	}

	//更新手牌，用于加入地主牌
	void setHandCard ()
	{
		handCard = game.cardHeap.getHandCard (No);
	}

	//获取手牌数量
	int getHandCardNum ()
	{
		return game.cardHeap.getHandCardNum (No);
	}

	//获取下家手牌数量
	private int getRightHandCardNum ()
	{
		return game.cardHeap.getHandCardNum ((No + 2) % 3);
	}

	//更新角色
	void setLandlord (int landlord)
	{
		//自己是地主
		if (landlord == No)
		{
			isLandlord = true;
			leftLandlord = false;
			rightLandlord = false;
		}

		//上家是地主
		else if (landlord == (No + 1) % 3)
		{
			isLandlord = false;
			leftLandlord = true;
			rightLandlord = false;
		}

		//下家是地主
		else// if (landlord == (No + 2) % 3)
		{
			isLandlord = false;
			leftLandlord = false;
			rightLandlord = true;
		}
	}

	//获取打出的牌的集合
	CardGroup getDiscard ()
	{
		return discard;
	}

	//获取编号
	int getNo ()
	{
		return No;
	}

	boolean getNoDiscard ()
	{
		return noDiscard;
	}

	void setNoDiscard (boolean noDiscard)
	{
		this.noDiscard = noDiscard;
	}

	/*本局是否想当地主，并给出基本分
	因为在斗地主中，火箭、炸弹、王和2可以认为是大牌
	所以叫牌需要按照这些牌的多少来判断。下面是一个简单的原则：
	假定火箭为8分，炸弹为6分，大王4分，小王3分，一个2为2分，则当分数
	大于等于7分时叫三倍；
	大于等于5分时叫二倍；
	大于等于3分时叫一倍；
	小于三分不叫。*/
	//电脑玩家用
	int getBaseScoreAI ()
	{
		try
		{
			Thread.sleep (500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace ();
		}
		int sum = 0;
		//方便分析的权值-数量集合
		// 3-17权值集合，11->J，12->Q，13->K，14->1，15->2，16->小王，17->大王
		int[] group = new int[18];
		for (int i = 0; i < 54; ++i)
			if (handCard[i])
				++group[CardGroup.translate (i)];

		if (group[17] == 1 && group[16] == 1) //如果有王炸
			sum += 8;

		else if (group[17] == 1) //如果有大王
			sum += 4;

		else if (group[16] == 1) //如果有小王
			sum += 3;

		//如果有2，每个2加2分
		sum += 2 * group[15];

		for (int i = 3; i < 16; ++i)
			if (group[i] == 4) //如果炸弹
				sum += 6;

		if (sum >= 7)
			return 3;

		else if (sum >= 5)
			return 2;

		else if (sum >= 3)
			return 1;

		else
			return 0;
	}

	//选牌
	void selectCards ()
	{
		//是否需要重新分析手牌
		if (analyse.isEmpty ())
			divideIntoGroups ();

		threePlusAndAirplane ();
		deleteUnknown ();
		sort ();

		if (analyse.size () == 2)
			//手数为2，且有适合的炸弹直接出
			for (CardGroup c : analyse)
				if (c.getType () == Type.Bomb)
				{
					if (game.lastOne != null &&//如果自己是接别人的牌
							game.lastOne.discard.getType () == Type.Bomb //别人最后出牌为炸弹
							&& c.getValue () <= game.lastOne.discard.getValue ()) //且自己的炸弹不大于对方时，
						continue;//不能选择改牌
					selection.copyFrom (c);
					return;
				}

		if (game.lastOne == this) //上一次出牌的是自己
			Myself ();//直接出牌

		else if (this != game.landlord && game.lastOne != game.landlord)
			Friend ();//跟友方牌

		else
			Enemy ();//跟敌方的牌
	}

	private void divideIntoGroups ()
	{
		//复制一份副本
		boolean[] copyOfHandCard = Arrays.copyOf (handCard, handCard.length);
		//方便分析的权值-数量集合
		// 3-17权值集合，11->J，12->Q，13->K，14->1，15->2，16->小王，17->大王
		int[] group = new int[18];

		for (int i = 0; i < copyOfHandCard.length; ++i)
			if (copyOfHandCard[i])
				++group[CardGroup.translate (i)];

		if (group[16] == 1 && group[17] == 1) //如果有王炸
		{
			//记录一个牌组的类型和权值
			CardGroup c = new CardGroup (Type.Bomb, 17);
			//在副本中删掉这两张牌
			copyOfHandCard[52] = false;
			copyOfHandCard[53] = false;
			//记录这个牌组具体手牌
			c.addCard (52);
			c.addCard (53);
			//在分析中删掉这两张牌
			group[16] = 0;
			group[17] = 0;
			//加入最终的分析容器
			analyse.add (c);
		}

		//如果只有小王
		else if (group[16] == 1)
		{
			//记录一个牌组的类型和权值
			CardGroup c = new CardGroup (Type.Single, 16);
			//在副本中删掉这张牌
			copyOfHandCard[52] = false;
			//记录这个牌组具体手牌
			c.addCard (52);
			//在分析中删掉这两张牌
			group[16] = 0;
			//加入最终的分析容器
			analyse.add (c);
		}

		//如果只有大王
		else if (group[17] == 1)
		{
			//记录一个牌组的类型和权值
			CardGroup c = new CardGroup (Type.Single, 17);
			//在副本中删掉这张牌
			copyOfHandCard[53] = false;
			//记录这个牌组具体手牌
			c.addCard (53);
			//在分析中删掉这两张牌
			group[17] = 0;
			//加入最终的分析容器
			analyse.add (c);
		}

		//测试还有没有牌，如果没有牌就对analyse排序
		if (!isNotEmpty (group))
			return;

		//普通炸弹
		for (int i = 3; i < 16; ++i)
			if (group[i] == 4)
			{
				//记录一个牌组的类型和权值
				CardGroup c = new CardGroup (Type.Bomb, i);
				for (int j = 0; j < 4; ++j)
				{
					//在副本中删掉这四张牌
					copyOfHandCard[(i - 3) * 4 + j] = false;
					//记录这个牌组具体手牌
					c.addCard ((i - 3) * 4 + j);
				}

				//在分析中删掉这四张牌
				group[i] = 0;
				//加入最终的分析容器
				analyse.add (c);
			}

		//测试还有没有牌，如果没有牌就对analyse排序
		if (!isNotEmpty (group))
			return;

		//2不可以作为顺子，飞机之类，提前处理
		//如果有4个2上面已经去掉了，不会重复
		if (group[15] != 0)
		{
			//记录一个牌组的类型和权值
			CardGroup c;

			switch (group[15])
			{
				case 3:
					c = new CardGroup (Type.Three, 15);
					break;
				case 2:
					c = new CardGroup (Type.Double, 15);
					break;
				default:
					c = new CardGroup (Type.Single, 15);
			}

			for (int i = 0; i < 4; ++i)
				if (copyOfHandCard[48 + i])
				{
					//在副本中删掉这些牌
					copyOfHandCard[48 + i] = false;
					//记录这个牌组具体手牌
					c.addCard (48 + i);
				}

			//在分析中删掉这些张牌
			group[15] = 0;
			//加入最终的分析容器
			analyse.add (c);
		} //end if (group[15] != 0)

		//测试还有没有牌，如果没有牌就对analyse排序
		if (!isNotEmpty (group))
			return;

		//单顺
		//当前连续牌数，最后一张手牌
		int n = 0, end = 6; //假装没有
		for (int i = 14; i > 6; --i) //如果最后一张手牌比7小就不可能有单顺
			if (group[i] > 0)
			{
				end = i;
				break;
			}

		if (end > 6) //如果最后一张手牌比7小就不可能有单顺
			for (int i = 3; i <= end; ++i) //3，4，5...10，J，Q，K，1
			{
				if (group[i] == 0) //如果单顺断了，就从下一个开始继续找
				{
					n = 0;
					continue;
				}
				//单顺连起来
				++n;

				if (n == 5) //如果数量足够组成单顺
				{
					//记录一个牌组的类型和权值
					CardGroup c = new CardGroup (Type.SingleSeq, i);
					for (int first = i - 4; first <= i; ++first)
					{
						//在副本中删掉这些牌
						for (int j = 0; j < 4; ++j)
							if (copyOfHandCard[(first - 3) * 4 + j])
							{
								//在副本中删掉一张手牌
								copyOfHandCard[(first - 3) * 4 + j] = false;
								//记录这个牌组具体手牌
								c.addCard ((first - 3) * 4 + j);
								//删一张就够了
								break;
							}

						//在分析中删掉这些张牌
						--group[first];
					}

					//加入最终的分析容器
					analyse.add (c);
					//下一次直接从当前单顺的第一个i - 4开始，因为前面的不可能有5个了
					i -= 5; //执行完循环后会++i，所以是i -= 5
					n = 0;
				} //end if (n == 5)
			} //end for (int i = 3; i <= end; ++i)
		//end if (end > 6)

		//如可能，继续往单顺中添加剩余牌
		for (CardGroup cardGroup : analyse)
			if (cardGroup.getType () == Type.SingleSeq) //如果这个牌组是单顺
				//单顺后面第一个期待的牌
				for (int i = cardGroup.getValue () + 1; i < 15; ++i)
					if (group[i] > 0) //如果这个牌可以延长一个单顺
					{
						for (int j = 0; j < 4; ++j)
							if (copyOfHandCard[(i - 3) * 4 + j])
							{
								//在副本中删掉这张手牌
								copyOfHandCard[(i - 3) * 4 + j] = false;
								//记录这个牌组具体手牌
								cardGroup.addCard ((i - 3) * 4 + j);
								cardGroup.setValue ();
								//删一张就够了
								break;
							}

						//在分析中删掉这张牌
						--group[i];
					}
					//如果没有就可以断了
					else
						break;
		//end for (CardGroup cardGroup : analyse)

		//合成更长单顺或双顺
		for (CardGroup c1 : analyse) //单顺1
			if (c1.getType () == Type.SingleSeq)
				for (CardGroup c2 : analyse) //单顺2
					//两个引用用equals来比较
					//如果两个是不相同的单顺
					if (c2.getType () == Type.SingleSeq && !c2.equals (c1))
						//如果c2紧跟着c1
						if (c1.getValue () == c2.getValue () - c2.getCount ())
						{
							//把c2从start开始，长度为count的牌转移到c1里面
							//c2的第一位就是c1缺的下一位
							cutToFirst (c1, c2);
							//把c2的type标记为Unknown以待删除
							c2.setType (Type.Unknown);
						}

						//如果c1紧跟着c2
						else if (c2.getValue () == c1.getValue () - c1.getCount ())
						{
							//把c1从start开始，长度为count的牌转移到c2里面
							//c1的第一位就是c2缺的下一位
							cutToFirst (c2, c1);
							//把c1的type标记为Unknown以待删除
							c1.setType (Type.Unknown);
						}

						//如果c1和c2完全一样，可以合成双顺
						else if (c1.getValue () == c2.getValue ()
								&& c1.getCount () == c2.getCount ())
						{
							//把c2从start开始，长度为count的牌转移到c1里面
							cutToFirst (c1, c2);
							//把c1的type标记为DoubleSeq
							c1.setType (Type.DoubleSeq);
							//把c2的type标记为Unknown以待删除
							c2.setType (Type.Unknown);
						}
		//end for (CardGroup c1 : analyse)

		deleteUnknown ();

		//双顺
		n = 0;
		end = 4; //假装没有
		for (int i = 14; i > 4; --i) //如果最后一张手牌比5小就不可能有双顺
			if (group[i] > 1)
			{
				end = i;
				break;
			}

		if (end > 4) //如果最后一张手牌比5小就不可能有双顺
			for (int i = 3; i <= end; ++i) //3，4，5...10，J，Q，K，1
				if (group[i] > 1) //如果没断
					++n;

				else if (n > 2) //如果断连，并且数量已经有3个或以上了
				{
					//断连了i - 1就是最后一个连续的
					//双顺范围是[(i - 1) - n + 1, i - 1] = [i - n, i)
					//记录一个牌组的类型和权值
					CardGroup c = new CardGroup (Type.DoubleSeq, i - 1);
					for (int first = i - n; first < i; ++first)
					{
						int two = 2;
						//在副本中删掉这些牌
						for (int j = 0; j < 4; ++j)
							if (copyOfHandCard[(first - 3) * 4 + j])
							{
								//在副本中删掉两张手牌
								copyOfHandCard[(first - 3) * 4 + j] = false;
								//记录这个牌组具体手牌
								c.addCard ((first - 3) * 4 + j);
								//删两张就够了
								--two;
								if (two == 0)
									break;
							}

						//在分析中删掉两张牌
						group[first] -= 2;
					}

					//加入最终的分析容器
					analyse.add (c);
					//双顺不可能有重叠的部分，重叠就变成炸弹都被前面去掉了
				} // end else if (n > 2)

				else //断连了而且数量不够，继续往下找
					n = 0;
		//end if (n > 4)

		//合成三顺
		for (CardGroup c1 : analyse)
			if (c1.getType () == Type.SingleSeq)
				for (CardGroup c2 : analyse)
					if (c2.getType () == Type.DoubleSeq && c1.getValue () == c2.getValue ()
							&& c1.getCount () * 2 == c2.getCount ())
					{
						//把c1从start开始，长度为count的牌转移到c2里面
						cutToFirst (c2, c1);
						//把c2的type标记为ThreeSeq
						c2.setType (Type.ThreeSeq);
						//把c1的type标记为Unknown以待删除
						c1.setType (Type.Unknown);
					}

		deleteUnknown ();

		//剩余牌中查找三顺
		n = 0;
		end = 3; //假装没有
		for (int i = 14; i > 3; --i) //如果最后一张手牌比4小就不可能有三顺
			if (group[i] > 2)
			{
				end = i;
				break;
			}

		if (end > 3) //如果最后一张手牌比4小就不可能有三顺
			for (int i = 3; i <= end; ++i) //3，4，5...10，J，Q，K，1
				if (group[i] == 3) //如果没断，只能是三张，四张都被炸弹删了
					++n;

				else if (n > 1) //如果断连，并且数量已经有2个或以上了
				{
					//断连了i - 1就是最后一个连续的
					//三顺范围是[(i - 1) - n + 1, i - 1] = [i - n, i)
					//记录一个牌组的类型和权值
					CardGroup c = new CardGroup (Type.ThreeSeq, i - 1);
					for (int first = i - n; first < i; ++first)
					{
						//在副本中删掉这些牌，全部变成false就好，肯定是三张
						for (int j = 0; j < 4; ++j)
							if (copyOfHandCard[(first - 3) * 4 + j])
							{
								//在副本中删掉三张手牌
								copyOfHandCard[(first - 3) * 4 + j] = false;
								//记录这个牌组具体手牌
								c.addCard ((first - 3) * 4 + j);
							}

						//在分析中删掉三张牌
						group[first] = 0;
					}

					//加入最终的分析容器
					analyse.add (c);
					//双顺不可能有重叠的部分，重叠就变成炸弹都被前面去掉了
				} // end else if (n > 1)

				else //断连了而且数量不够，继续往下找
					n = 0;
		//end if (end > 3)

		//三条
		for (int i = 3; i < 15; ++i)
			if (group[i] == 3)
			{
				//记录一个牌组的类型和权值
				CardGroup c = new CardGroup (Type.Three, i);

				//在副本中删掉这些牌，全部变成false就好，肯定是三张
				for (int j = 0; j < 4; ++j)
					if (copyOfHandCard[(i - 3) * 4 + j])
					{
						//在副本中删掉三张手牌
						copyOfHandCard[(i - 3) * 4 + j] = false;
						//记录这个牌组具体手牌
						c.addCard ((i - 3) * 4 + j);
					}

				//在分析中删掉三张牌
				group[i] = 0;

				//加入最终的分析容器
				analyse.add (c);
			}

		//测试还有没有牌，如果没有牌就对analyse排序
		if (!isNotEmpty (group))
			return;

		//对
		for (int i = 3; i < 15; ++i)
			if (group[i] == 2)
			{
				//记录一个牌组的类型和权值
				CardGroup c = new CardGroup (Type.Double, i);

				//在副本中删掉这些牌，全部变成false就好，肯定是两张
				for (int j = 0; j < 4; ++j)
					if (copyOfHandCard[(i - 3) * 4 + j])
					{
						//在副本中删掉两张手牌
						copyOfHandCard[(i - 3) * 4 + j] = false;
						//记录这个牌组具体手牌
						c.addCard ((i - 3) * 4 + j);
					}

				//在分析中删掉两张牌
				group[i] = 0;

				//加入最终的分析容器
				analyse.add (c);
			}

		//测试还有没有牌，如果没有牌就对analyse排序
		if (!isNotEmpty (group))
			return;

		//单牌
		for (int i = 3; i < 16; ++i)
			if (group[i] == 1)
			{
				//记录一个牌组的类型和权值
				CardGroup c = new CardGroup (Type.Single, i);

				//在副本中删掉这张牌
				for (int j = 0; j < 4; ++j)
					if (copyOfHandCard[(i - 3) * 4 + j])
					{
						//在副本中删掉张手牌
						copyOfHandCard[(i - 3) * 4 + j] = false;
						//记录这个牌组具体手牌
						c.addCard ((i - 3) * 4 + j);
						break;
					}

				//在分析中删掉张牌
				group[i] = 0;

				//加入最终的分析容器
				analyse.add (c);
			}

		//按道理应该是空的了
		if (isNotEmpty (group))
			throw new AssertionError ();
		//最后整理一下
		sort ();
	}

	//由三条、三顺完善成三带一和飞机；先找单牌，再找对子，均不够就保持原样
	private void threePlusAndAirplane ()
	{
		int n, doubleNum = 0, singleNum = 0;
		for (CardGroup cardGroup : analyse)
			if (cardGroup.getType () == Type.Single)
				++singleNum;

			else if (cardGroup.getType () == Type.Double)
				++doubleNum;

		//完善飞机
		for (CardGroup c1 : analyse)
			if (c1.getType () == Type.ThreeSeq)
			{
				n = c1.getCount () / 3;
				//如果能凑够三带一
				if (singleNum >= n)
				{
					for (CardGroup c2 : analyse)
					{
						if (c2.getType () == Type.Single)
							for (int i = 0; i < 4; ++i)
								if (c2.hasCard ((c2.getValue () - 3) * 4 + i))
								{
									c1.addCard ((c2.getValue () - 3) * 4 + i);
									--singleNum;
									--n;
									c2.setType (Type.Unknown);
									break;
								}

						if (n == 0)
						{
							c1.setType (Type.Airplane);
							break;
						}
					}
				}

				//如果不行再考虑三带二
				else if (doubleNum >= n)
					for (CardGroup c2 : analyse)
					{
						if (c2.getType () == Type.Double)
						{
							for (int i = 0, count = 2; count > 0 && i < 4; ++i)
								if (c2.hasCard ((c2.getValue () - 3) * 4 + i))
								{
									c1.addCard ((c2.getValue () - 3) * 4 + i);
									//删两张
									--count;
								}
							--doubleNum;
							--n;
							c2.setType (Type.Unknown);
						}

						if (n == 0)
						{
							c1.setType (Type.Airplane);
							break;
						}
					}
			} //end if (c1.getType () == Type.ThreeSeq)
		// end for (CardGroup c1 : analyse)

		//普通的三张相同的牌
		for (CardGroup c1 : analyse)
			if (c1.getType () == Type.Three)
				//三带一
				if (singleNum > 0)
				{
					for (CardGroup c2 : analyse)
						if (c2.getType () == Type.Single)
						{
							for (int i = 0; i < 4; ++i)
								if (c2.hasCard ((c2.getValue () - 3) * 4 + i))
								{
									c1.addCard ((c2.getValue () - 3) * 4 + i);
									--singleNum;
									c1.setType (Type.ThreePlus);
									c2.setType (Type.Unknown);
									break;
								}
							break;
						}
				}
				//三带二
				else if (doubleNum > 0)
					for (CardGroup c2 : analyse)
						if (c2.getType () == Type.Double)
						{
							for (int i = 0, count = 2; count > 0 && i < 4; ++i)
								if (c2.hasCard ((c2.getValue () - 3) * 4 + i))
								{
									c1.addCard ((c2.getValue () - 3) * 4 + i);
									//删两张
									--count;
								}

							--doubleNum;
							c1.setType (Type.ThreePlus);
							c2.setType (Type.Unknown);
							break;
						}
	}

	//把c2从start开始，长度为count的牌转移到c1里面，用于顺子合并
	private void cutToFirst (CardGroup c1, CardGroup c2)
	{
		int start = c2.getValue () - c2.getCount () + 1;
		int count = c2.getCount ();
		for (int i = (start - 3) * 4; count > 0; ++i)
			if (c2.hasCard (i))
			{
				c1.addCard (i);
				--count;
			}

		//当两个单顺合并成双顺或者一个单顺和一个双顺合并成三顺的时候
		//start = c2.getValue () - c2.getCount () + 1
		//count = c2.getCount ()
		//c1原本的value与c2的value相同
		//此时start + count - 1 = c2.getValue () - c2.getCount () + 1 + c2.getCount () - 1
		//即start + count - 1 = c2.getValue () = c1.getValue ()，不会改变
		c1.setValue (start + count - 1);
	}

	//删掉由于合并被改成Unknown的牌组
	private void deleteUnknown ()
	{
		for (int i = 0; i < analyse.size (); )
			if (analyse.get (i).getType () == Type.Unknown)
				analyse.remove (i);
			else
				++i;
	}

	private boolean isNotEmpty (int[] a)
	{
		for (int aa : a)
			if (aa != 0)
				return true;

		//如果没有牌就对analyse排序
		sort ();
		return false;
	}

	private int compare (CardGroup o1, CardGroup o2)
	{
		if (o1.getType () != o2.getType ())
			return o1.getType ().getValue () < o2.getType ().getValue () ? -1 : 1;
		else
			return o1.getValue () < o2.getValue () ? -1 : 1;
	}

	private void sort ()
	{
		//冒泡实现排序
		int size = analyse.size ();
		CardGroup temp1;
		CardGroup temp2;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size - 1; j++)
			{
				temp1 = analyse.get (j);
				temp2 = analyse.get (j + 1);
				if (compare (temp1, temp2) == -1)
				{
					analyse.set (j, temp1);
					analyse.set (j + 1, temp2);
				}

				if (compare (temp1, temp2) == 1)
				{
					analyse.set (j + 1, temp1);
					analyse.set (j, temp2);
				}
			}
	}

//    private void sort() {
//
//        analyse.sort(new Comparator<CardGroup>()   //api等级太高
//        {
//            @Override
//            public int compare(CardGroup o1, CardGroup o2) {
//                if (o1.getType() != o2.getType())
//                    return o1.getType().getValue() < o2.getType().getValue() ? -1 : 1;
//                else
//                    return o1.getValue() < o2.getValue() ? -1 : 1;
//            }
//        });
//    }

	//上一次出牌的是自己
	private void Myself ()
	{
		if (analyse.size () == 1)
		{
			//剩最后一手牌
			selection.copyFrom (analyse.get (0));
			return;
		}

		//如果下家手牌数为1并且是队友
		if (getRightHandCardNum () == 1 && rightLandlord == isLandlord)
		{
			//没试验过下家牌，就打出最小的一张；否则就正常出牌
			if (!test)
			{
				int minSingle = findMinSingle ();
				selection = new CardGroup (Type.Single, CardGroup.translate (minSingle));
				selection.addCard (minSingle);
				test = true;
				//要重新分析牌
				analyse.clear ();
				return;
			}
		}

		//单牌→对→双顺→单顺→三条、三带一、飞机、炸弹
		sort ();
		for (CardGroup c : analyse)
		{
			if (c.getType () == Type.Bomb || c.getValue () > 14)
				continue;

			selection.copyFrom (c);
			return;
		}

		//避免什么都没选到
		int i = 0;
		selection.copyFrom (analyse.get (i++));
		while (selection.getType () == Type.Bomb)
			selection.copyFrom ((analyse.get (i++)));
	}

	//寻找最小的单牌
	private int findMinSingle ()
	{
		for (int i = 0; i < 54; ++i)
			if (handCard[i])
			{
				handCard[i] = false;
				return i;
			}
		return 0;
	}

	//上一次出牌的是友方
	private void Friend ()
	{
		if (game.lastOne != game.landlord && leftLandlord)
			return;//上家为地主，但最后出牌方为友方，则不出牌

		CardGroup cardGroup = game.lastOne.getDiscard ();

		for (CardGroup c : analyse)
			if (c.getType () == cardGroup.getType ()
					&& c.getCount () == cardGroup.getCount ()
					&& c.getValue () > cardGroup.getValue ())
			{
				selection.copyFrom (c);
				break;
			}

		//手牌手数大于2，并且所选牌权值大于14（A），则不出牌
		if (analyse.size () > 2 && selection.getValue () > 14)
			selection.clear ();
	}

	//上一次出牌的是敌方
	private void Enemy ()
	{
		CardGroup cardGroup = game.lastOne.getDiscard ();

		//拆成基本牌
		analyse.clear ();
		divideIntoGroups ();
		sort ();

		//查看是否有相应牌，并且权值大
		for (CardGroup c : analyse)
			if (c.getType () == cardGroup.getType ()
					&& c.getCount () == cardGroup.getCount ()
					&& c.getValue () > cardGroup.getValue ())
			{
				selection.copyFrom (c);
				return;
			}

		//需要拆牌
		switch (cardGroup.getType ())
		{
			case Single:
				needSingle ();
				break;
			case Double:
				needDouble ();
				break;
			case SingleSeq:
				NeedSingleSeq ();
				break;
			case ThreePlus:
				NeedThreePlus ();
				break;
			case Airplane:
				NeedAirplane ();
				break;
		}

		deleteUnknown ();
		if (selection.getCount () == 1)
			return;

		if (cardGroup.getCount () > 3 || cardGroup.getValue () > 14)
			for (CardGroup c1 : analyse)
				if (c1.getType () == Type.Bomb)
				{
					//如果别人的是炸弹还比自己的炸弹大
					if (game.lastOne.getDiscard ().getType () == Type.Bomb
							&& c1.getValue () < game.lastOne.getDiscard ().getValue ())
						continue;

					selection.copyFrom (c1);
					return;
				}
	}

	private void needSingle ()
	{
		CardGroup cardGroup = game.lastOne.getDiscard ();

		//拆长度大于5的单顺
		for (CardGroup c : analyse)
			if (c.getType () == Type.Single && c.getCount () > 5)
				//如果最小的比别人大
				if (c.getValue () - c.getCount () + 1 > cardGroup.getValue ())
				{
					//加入一张具体的牌，打出去后会有别的函数把它删掉
					for (int i = 0; i < 4; ++i)
						if (c.hasCard ((c.getValue () - 3) * 4 + i))
						{
							selection.addCard ((c.getValue () - 3) * 4 + i);
							selection.setValue (c.getValue () - c.getCount () + 1);
							selection.setType (Type.Single);
							analyse.clear ();
							return;
						}
				}
				//如果最大的比别人大，中间的就不拆了
				else if (c.getValue () > cardGroup.getValue ())
					//加入一张具体的牌，打出去后会有别的函数把它删掉
					for (int i = 0; i < 4; ++i)
						if (c.hasCard ((c.getValue () - 3) * 4 + i))
						{
							selection.addCard ((c.getValue () - 3) * 4 + i);
							selection.setValue (c.getValue ());
							selection.setType (Type.Single);
							analyse.clear ();
							return;
						}
		//end for (CardGroup c : analyse)

		//拆三条
		for (CardGroup c : analyse)
			if (c.getType () == Type.Three && c.getValue () > cardGroup.getValue ())
				//加入一张具体的牌，打出去后会有别的函数把它删掉
				for (int i = 0; i < 4; ++i)
					if (c.hasCard ((c.getValue () - 3) * 4 + i))
					{
						selection.addCard ((c.getValue () - 3) * 4 + i);
						selection.setValue (c.getValue ());
						selection.setType (Type.Single);
						analyse.clear ();
						return;
					}

		//拆一对
		for (CardGroup c : analyse)
			if (c.getType () == Type.Double && c.getValue () > cardGroup.getValue ())
				//加入一张具体的牌，打出去后会有别的函数把它删掉
				for (int i = 0; i < 4; ++i)
					if (c.hasCard ((c.getValue () - 3) * 4 + i))
					{
						selection.addCard ((c.getValue () - 3) * 4 + i);
						selection.setValue (c.getValue ());
						selection.setType (Type.Single);
						analyse.clear ();
						return;
					}
	}

	private void needDouble ()
	{
		CardGroup cardGroup = game.lastOne.getDiscard ();

		//拆三条
		for (CardGroup c : analyse)
			if (c.getType () == Type.Three && c.getValue () > c.getValue ())
			{
				//加入两张具体的牌，打出去后会有别的函数把它删掉
				for (int i = 0, count = 2; i < 4; ++i)
					if (c.hasCard ((c.getValue () - 3) * 4 + i))
					{
						selection.addCard ((c.getValue () - 3) * 4 + i);
						--count;

						if (count == 0)
						{
							break;
						}
					}

				selection.setValue (c.getValue ());
				selection.setType (Type.Double);
				analyse.clear ();
				return;
			}

		//拆三顺
		for (CardGroup c : analyse)
			if (c.getType () == Type.ThreePlus && c.getValue () > cardGroup.getValue ())
				//如果最小的比别人大
				if (c.getValue () - c.getCount () + 1 > cardGroup.getValue ())
				{
					//加入三张具体的牌，打出去后会有别的函数把它删掉
					for (int i = 0, count = 3; i < 4; ++i)
						if (c.hasCard ((c.getValue () - 3) * 4 + i))
						{
							selection.addCard ((c.getValue () - 3) * 4 + i);
							--count;

							if (count == 0)
								break;
						}

					selection.setValue (c.getValue () - c.getCount () + 1);
					selection.setType (Type.Double);
					analyse.clear ();
					return;
				}

				//拆最大的，中间的就不拆了
				else
				{
					//加入三张具体的牌，打出去后会有别的函数把它删掉
					for (int i = 0, count = 3; i < 4; ++i)
						if (c.hasCard ((c.getValue () - 3) * 4 + i))
						{
							selection.addCard ((c.getValue () - 3) * 4 + i);
							--count;

							if (count == 0)
								break;
						}

					selection.setValue (c.getValue ());
					selection.setType (Type.Double);
					analyse.clear ();
					return;
				}
		// end for (CardGroup c : analyse)
	}

	private void NeedSingleSeq ()
	{
		CardGroup cardGroup = game.lastOne.getDiscard ();

		for (CardGroup c : analyse)
			//拆更长的单顺
			if (c.getType () == Type.SingleSeq
					&& c.getValue () > cardGroup.getValue ()
					&& c.getCount () > cardGroup.getCount ())
				//如果自己的单顺最小的比别人最小的小，最大的比别人最大的大
				//自己最小，别人最小，别人最大，自己最大
				//从自己最大的开始往小拆，这样可以增大自己小的还能成为单顺的机会
				if (c.getValue () - c.getCount () < c.getValue () - cardGroup.getCount ())
				{
					for (int i = c.getValue (), count = 0; count < cardGroup.getCount (); --i)
						//加入一张具体的牌，打出去后会有别的函数把它删掉
						for (int j = 0; j < 4; ++j)
							if (c.hasCard ((i - 3) * 4 + j))
							{
								selection.addCard ((i - 3) * 4 + j);
								++count;
								break;
							}

					selection.setValue (c.getValue ());
					selection.setType (Type.SingleSeq);
					analyse.clear ();
					return;
				}
				//如果自己的单顺最小的比别人最小的大，最大的比别人最大的大
				//别人最小，自己最小，别人最大，自己最大
				//或者
				//别人最小，别人最大，自己最小，自己最大
				//从自己最小的开始往大拆，这样可以增大自己大的还能成为单顺的机会
				else
				{
					for (int i = c.getValue () - c.getCount () + 1, count = 0;
							count < cardGroup.getCount (); ++i)
						//加入一张具体的牌，打出去后会有别的函数把它删掉
						for (int j = 0; j < 4; ++j)
							if (c.hasCard ((i - 3) * 4 + j))
							{
								selection.addCard ((i - 3) * 4 + j);
								++count;
								break;
							}

					selection.setValue (c.getValue () - c.getCount () + cardGroup.getCount ());
					selection.setType (Type.SingleSeq);
					analyse.clear ();
					return;
				}
	}

	private void NeedThreePlus ()
	{
		CardGroup cardGroup = game.lastOne.getDiscard ();

		for (CardGroup c1 : analyse)
			if (c1.getType () == Type.Three && c1.getValue () > cardGroup.getValue ())
			{
				if (cardGroup.getCount () == 4) //是三带一
				{
					//如果有单牌
					if (analyse.get (0).getType () == Type.Single)
					{
						for (int i = 0; i < 4; ++i)
							if (analyse.get (0).hasCard ((analyse.get (0).getValue () - 3) * 4 + i))
							{
								//把这张单牌加三条中
								c1.addCard ((analyse.get (0).getValue () - 3) * 4 + i);
								c1.setType (Type.ThreePlus);
								//标记为Unknown以待删除
								analyse.get (0).setType (Type.Unknown);
								selection.copyFrom (c1);
								return;
							}
					}

					//如果没有单牌，要拆
					else
					{
						//拆长度大于5的单顺，拿最小的一张
						for (CardGroup c : analyse)
							if (c.getType () == Type.Single && c.getCount () > 5)
								//加入一张具体的牌，打出去后会有别的函数把它删掉
								for (int i = 0; i < 4; ++i)
									if (c.hasCard ((c.getValue () - c.getCount () - 2) * 4 + i))
									{
										//把这张单牌加三条中
										c1.addCard ((c.getValue () - c.getCount () - 2) * 4 + i);
										c1.setType (Type.ThreePlus);
										selection.copyFrom (c1);
										analyse.clear ();
										return;
									}

						//拆三条
						for (CardGroup c : analyse)
							if (c.getType () == Type.Three)
								//加入一张具体的牌，打出去后会有别的函数把它删掉
								for (int i = 0; i < 4; ++i)
									if (c.hasCard ((c.getValue () - 3) * 4 + i))
									{
										c1.addCard ((c.getValue () - 3) * 4 + i);
										c1.setType (Type.ThreePlus);
										selection.copyFrom (c1);
										analyse.clear ();
										return;
									}

						//拆一对
						for (CardGroup c : analyse)
							if (c.getType () == Type.Double)
								//加入一张具体的牌，打出去后会有别的函数把它删掉
								for (int i = 0; i < 4; ++i)
									if (c.hasCard ((c.getValue () - 3) * 4 + i))
									{
										c1.addCard ((c.getValue () - 3) * 4 + i);
										c1.setType (Type.ThreePlus);
										selection.copyFrom (c1);
										analyse.clear ();
										return;
									}
					}// end else
				}//end if (cardGroup.getCount () == 4)

				else //是三带二
				{
					//找一对
					for (CardGroup c : analyse)
						if (c.getType () == Type.Double && c.getValue () < 14)
							//加入两张具体的牌，打出去后会有别的函数把它删掉
							for (int i = 0, count = 0; i < 4; ++i)
								if (c.hasCard ((c.getValue () - 3) * 4 + i))
								{
									c1.addCard ((c.getValue () - 3) * 4 + i);
									++count;

									if (count == 2)
									{
										c1.setType (Type.ThreePlus);
										selection.copyFrom (c1);
										analyse.clear ();
										//标记为Unknown以待删除
										c.setType (Type.Unknown);
										return;
									}
								}

					//拆三条
					for (CardGroup c : analyse)
						//不能是自己
						if (c.getType () == Type.Three && !c.equals (c1) && c.getValue () < 14)
							for (int i = 0, count = 0; i < 4; ++i)
								if (c.hasCard ((c.getValue () - 3) * 4 + i))
								{
									c1.addCard ((c.getValue () - 3) * 4 + i);
									++count;

									if (count == 3)
									{
										c1.setType (Type.ThreePlus);
										selection.copyFrom (c1);
										analyse.clear ();
										return;
									}
								}
				} //end else
			} //end if (c1.getType () == Type.Three && c1.getValue () > cardGroup.getValue ())
	}

	private void NeedAirplane ()
	{
		CardGroup cardGroup = game.lastOne.getDiscard ();

		int wing; //翅膀类型
		int n; //单顺中三张牌的个数\
		//当牌数是20的时候两个会重合，但是20张牌意味着地主一次全部出完了
		//所以下面的分法不会有任何问题
		if (cardGroup.getCount () % 4 == 0)
		{
			//这是三带一组成的飞机
			wing = 1;
			n = cardGroup.getCount () / 4;
		}
		else
		{
			//这是三带二组成的飞机
			wing = 2;
			n = cardGroup.getCount () / 5;
		}

		for (CardGroup c : analyse)
		{
			if (c.getType () == Type.ThreeSeq && c.getCount () == 3 * n
					&& c.getValue () > cardGroup.getValue ())
			{
				//总数有多少单牌或者对
				int count = 0;
				for (CardGroup c1 : analyse)
					if (c1.getType () == (wing == 1 ? Type.Single : Type.Double))
					{
						++count;
					}

				//如果总数不够就没必要凑了
				if (count < n)
					continue;

				for (CardGroup c1 : analyse)
				{
					if (c1.getType () == (wing == 1 ? Type.Single : Type.Double))
					{
						//标记为Unknown以待删除
						c1.setType (Type.Unknown);
						//加入一或两张具体的牌，打出去后会有别的函数把它删掉
						for (int i = 0, j = wing; i < 4; ++i)
							if (c1.hasCard ((c1.getValue () - 3) * 4 + i))
							{
								c.addCard ((c1.getValue () - 3) * 4 + i);
								--j;

								if (j == 0)
									break;
							}
						//凑一张或一对就减一
						--count;
					}

					//凑够了
					if (count == 0)
						break;
				}

				c.setType (Type.Airplane);
				selection.copyFrom (c);
			}
		}
	}

	//电脑出牌
	boolean discard ()
	{
		try
		{
			Thread.sleep (2000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace ();
		}

		//如果没选到牌就不出
		if (selection.getCount () == 0)
		{
			noDiscard = true;
			return false;
		}

		//否则正常出牌
		discardAndClear ();
		return true;
	}

	private void discardAndClear ()
	{
		//把选牌放入出牌区：打出选牌
		discard.copyFrom (selection);

		//本次出牌是否为拆牌，需要更新分析牌堆
		boolean needClear = true;
		for (CardGroup c : analyse)
			if (c.getType () == selection.getType ()
					&& c.getValue () == selection.getValue ()
					&& c.getCount () == selection.getCount ())
			{
				analyse.remove (c);
				needClear = false;
				break;
			}

		//如果需要清空，下次出牌要重新分析
		if (needClear)
			analyse.clear ();

		//在手牌中删去这些打出的牌
		for (int i = 0; i < 54; ++i)
			if (discard.hasCard (i))
			{
				handCard[i] = false;
				game.cardHeap.deleteCard (i);
			}

		selection.clear ();
	}

	//判断真人出牌时候合理
	boolean humanDiscard (Boolean[] cards)
	{
		for (int i = 0; i < 54; ++i)
			if (cards[i])
				selection.addCard (i);

		//如果没有选牌就按了出牌
		if (selection.getCount () == 0)
		{
			selection = new CardGroup ();
			return false;
		}

		//如果是跟牌，而且牌的数量不同，并且不是2或者4，即不可能为炸弹
		if (game.lastOne.getNo () != 0
				&& game.lastOne.getDiscard ().getCount () != selection.getCount ()
				&& selection.getCount () != 4 && selection.getCount () != 2)
		{
			selection = new CardGroup ();
			return false;
		}

		selection.setType (Type.Unknown);
		//分析所选牌的类型及权值
		AnalyseSelection ();

		boolean flag = isValid ();

		//如果能出牌
		if (selection.getType () != Type.Unknown && flag)
		{
			discardAndClear ();
			return true;
		}

		//如果不能出牌
		else
		{
			selection = new CardGroup ();
			return false;
		}
	}

	//判断能不能打出去
	private boolean isValid ()
	{
		//不是跟牌
		if (game.lastOne.getNo () == 0)
			return true;

		Type type = game.lastOne.getDiscard ().getType ();
		int value = game.lastOne.getDiscard ().getValue ();
		int count = game.lastOne.getDiscard ().getCount ();

		//如果自己是炸弹别人不是炸弹或者自己的炸弹比别人的大
		if (selection.getType () == Type.Bomb
				&& (type != Type.Bomb || value < selection.getValue ()))
			return true;

		//如果类型不符或数量不符
		if (selection.getType () != type || selection.getCount () != count)
			return false;

		//如果不够大
		return selection.getValue () > value;
	}

	//分析所选牌的类型及权值
	private void AnalyseSelection ()
	{
		//同牌面的最大数量
		int maxNum = 0;
		//最大数量的最大权值
		int maxValue = 0;

		//王炸
		if (selection.getCount () == 2 && selection.hasCard (52)
				&& selection.hasCard (53))
		{
			selection.setType (Type.Bomb);
			selection.setValue (17);
			return;
		}

		//找出相同牌面的最大数量，和最大权值
		int cardCount = 0;
		int findMaxValue;
		int findMaxNum;
		for (int i = 53; i >= 0 && cardCount < selection.getCount (); --i)
			if (selection.hasCard (i))
			{
				findMaxNum = 0;
				++cardCount;
				findMaxValue = CardGroup.translate (i);
				if (findMaxValue > 15)
				{
					maxNum = 1;
					maxValue = findMaxValue;
					break;
				}
				else
				{
					for (int j = 0; j < 4; ++j)
						if (selection.hasCard ((findMaxValue - 3) * 4 + j))
							++findMaxNum;

					if (findMaxNum > maxNum)
					{
						maxNum = findMaxNum;
						maxValue = findMaxValue;
					}
				}
			}

		switch (maxNum)
		{
			case 4:
				//炸弹
				if (selection.getCount () == 4)
				{
					selection.setType (Type.Bomb);
					selection.setValue (maxValue);
					return;
				}
				//四带二
				if (selection.getCount () == 6)
				{
					selection.setType (Type.FourSeq);
					selection.setValue (maxValue);
					return;
				}
				//什么都不是
				return;
			case 3:
				//三条
				if (selection.getCount () == 3)
				{
					selection.setType (Type.Three);
					selection.setValue (maxValue);
					return;
				}
				//三带一
				if (selection.getCount () == 4)
				{
					selection.setType (Type.ThreePlus);
					selection.setValue (maxValue);
					return;
				}
				//三带二
				if (selection.getCount () == 5)
				{
					int count = 2;
					//找一对
					for (int i = 0; i < 53; ++i)
						//不能是三条的牌
						if (selection.hasCard (i) && CardGroup.translate (i) != maxValue)
							for (int j = 0; j < 4; ++j)
								if (selection.hasCard ((i - 3) * 4 + j))
								{
									--count;
									if (count == 0)
									{
										selection.setType (Type.ThreePlus);
										selection.setValue (maxValue);
										return;
									}
								}

					//五张牌却不是三带二，什么都不是
					return;
				}

				//飞机
				//找出有多少个连续三张
				int threeNum = 0;
				//是否连续
				boolean threeFlag = true;
				for (int i = maxValue; threeFlag; --i)
				{
					threeFlag = false;
					//(i - 3) * 4 + j是i对应的第一张牌
					for (int j = 0, count = 0; j < 4; ++j)
					{
						if (selection.hasCard ((i - 3) * 4 + j))
							++count;

						if (count == 3)
						{
							++threeNum;
							threeFlag = true;
							break;
						}
					}
				}

				//如果是三顺的飞机
				if (selection.getCount () == 3 * threeNum)
				{
					selection.setType (Type.ThreeSeq);
					selection.setValue (maxValue);
					return;
				}

				//如果是三带一的飞机
				if (selection.getCount () == 4 * threeNum)
				{
					selection.setType (Type.Airplane);
					selection.setValue (maxValue);
					return;
				}

				//如果是三带二的飞机
				if (selection.getCount () == 5 * threeNum)
				{
					int count = 2;
					//找threeNum对
					for (int i = 0; i < 53; ++i)
					{
						//不能是三条的牌
						if (selection.hasCard (i) && CardGroup.translate (i) != maxValue)
						{
							for (int j = 0; j < 4; ++j)
								if (selection.hasCard ((i - 3) * 4 + j))
								{
									--count;
									if (count == 0)
										break;
								}
							//找到一对
							if (count == 0)
							{
								//这是这一对的最后一张牌，下一次循环从下一张牌开始，避免重复
								i = 4 * i - 9;
								--threeNum;
								count = 2;

								//找到threeNum对
								if (threeNum == 0)
								{
									selection.setType (Type.Airplane);
									selection.setValue (maxValue);
									return;
								}
							}
							else //什么都不是
								return;
						} //end if (selection.hasCard (i) && CardGroup.translate (i) != maxValue)
					} //end for (int i = 0; i < 53; ++i)
				}
				//什么都不是
				return;
			case 2:
				//一对
				if (selection.getCount () == 2)
				{
					selection.setType (Type.Double);
					selection.setValue (maxValue);
					return;
				}
				//连对
				if (selection.getCount () > 5 && selection.getCount () % 2 == 0)
				{
					//找出有多少个连续一对
					int twoNum = 0;
					//是否连续
					boolean twoFlag = true;
					for (int i = maxValue; twoFlag; --i)
					{
						twoFlag = false;
						//(i - 3) * 4 + j是i对应的第一张牌
						for (int j = 0, count = 0; j < 4; ++j)
						{
							if (selection.hasCard ((i - 3) * 4 + j))
								++count;

							if (count == 2)
							{
								++twoNum;
								twoFlag = true;
								break;
							}
						}
					}
					if (twoNum == selection.getCount () / 2)
					{
						selection.setType (Type.DoubleSeq);
						selection.setValue (maxValue);
						return;
					}
					else
						return;
				} //end if (selection.getCount () > 5 && selection.getCount () % 2 == 0)
				//什么都不是
				return;
			case 1:
				//单牌
				if (selection.getCount () == 1)
				{
					selection.setType (Type.Single);
					selection.setValue (maxValue);
				}
				//单顺
				else if (selection.getCount () > 4)
				{
					//单顺不能有2和更大的牌
					if (selection.getValue () > 14)
						return;

					//找出有多少个连续单牌
					int oneNum = 0;
					//是否连续
					boolean oneFlag = true;
					for (int i = maxValue; oneFlag; --i)
					{
						oneFlag = false;
						//(i - 3) * 4 + j是i对应的第一张牌
						for (int j = 0; j < 4; ++j)
							if (selection.hasCard ((i - 3) * 4 + j))
							{
								++oneNum;
								oneFlag = true;
								break;
							}
					}
					if (oneNum == selection.getCount ())
					{
						selection.setType (Type.SingleSeq);
						selection.setValue (maxValue);
					}
				} //end else if (selection.getCount () > 4)
		}
	}

	int getCallScore ()
	{
		return callScore;
	}

	void setCallScore (int callScore)
	{
		this.callScore = callScore;
	}
}