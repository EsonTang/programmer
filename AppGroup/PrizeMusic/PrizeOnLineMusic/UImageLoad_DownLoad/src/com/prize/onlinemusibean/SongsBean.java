package com.prize.onlinemusibean;

import java.io.Serializable;

public class SongsBean implements Serializable{

	private static final long serialVersionUID = 1L;
   
//	song_id	int	是	10000	歌曲ID, BIGINT类型
//	song_name	string	是	她总在某一个地方	歌曲名称
//	artist_id	int	是	202	艺人id, BIGINT类型
//	artist_name	string	是	刘德华	艺人名
//	artist_logo	string	是	http://img.xiami.net/images/collect/5/5/32755005_1416305260_pZPV_4.jpeg	艺人logo
//	singers	string	是		演唱者
//	album_id	int	是	830	专辑ID, BIGINT类型
//	album_name	string	是	滚石香港黄金十年 杜德伟精选	专辑名称
//	album_logo	string	是	http://img.xiami.net/images/collect/5/5/32755005_1416305260_pZPV_4.jpeg	专辑LOGO
//	pace	int	是		节拍数
//	length	int	是	277	歌曲长度
//	track	int	是		歌曲序号
//	cd_serial	int	是		CD号
//	music_type	int	是		是否纯音乐，1为纯音乐，0为非纯音乐
//	permission	object	是		歌曲权限信息 //是否提供服务: 0:正常, 1:不提供服务, 2:需要VIP 查看对象定义
	
	public int song_id;
	public String song_name;
	public int artist_id;
	public String artist_name;
	public String artist_logo;
	public String singers;
	public int album_id;
	public String album_name;
	public String album_logo;
	public int pace;
	public int length;
	public int track;
	public int cd_serial;
	public int music_type;
	
}
