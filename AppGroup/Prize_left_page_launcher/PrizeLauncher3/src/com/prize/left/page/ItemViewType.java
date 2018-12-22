package com.prize.left.page;

/***
 * card item 类型, 若有新的类型需要在这里增加.
 * @author fanjunchen
 *
 */
public interface ItemViewType {
	/**头类型如list的setHeader, 前面2个值为预留用*/
	int	HEADER = 0;
	/**导航*/
	int NAVI = 1;//navigation
	/**使用手册(帮助)*/
	int HELP = 2;
	/**最近使用(应用和联系人电话)*/
	int RECENT_USE = 3;
	
	/**头条新闻*/
	int NEWS = 4;
	/**百度团购美食*/
	int FOOD = 5;
	/**脚注*/
	int FOOTER = 6;
	/**百度在映电影*/
	int BDMOVIE = 7;//vertical
	/**百度团购*/
	int BD_GROUP = 17;//horizontal_goods
	/**百度外卖*/
//	int BD_TAKE_AWAY = 27;
	/**百度热词*/
	int BD_HOT_WD = 28;//hotsearch
	
	int BD_HOT_TIPS = 29;//hotsearch
	
	/**一点资讯,下面最多可放40个频道*/
	int ONE_NEWS = 100;
	/**英威诺资讯*/
	int INVNO_NEWS = 150;//horizontal_message
	/**应用或者其他推荐*/
	int PUSH_APP = 160;//common
	/**搜索的应用卡片类型*/
	int SEARCH_APP = 1;
	/**搜索的联系人卡片类型*/
	int SEARCH_CONTACT = SEARCH_APP + 1;
	
	
}
