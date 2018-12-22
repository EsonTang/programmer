package com.prize.onlinemusibean;

import java.io.Serializable;

public class AlbumBean implements Serializable{

	private static final long serialVersionUID = 1L;
	
//	album_id	int	是	615067792	歌曲ID, BIGINT类型
//	album_name	string	是	战斗吧！	专辑名称
//	album_logo	string	是	http://img.xiami.net/images/album/img67/906621467/6150677921416484101_2.png	专辑LOGO
//	artist_id	int	是	135	艺人id, BIGINT类型
//	artist_name	string	是	陈奕迅	艺人名
//	gmt_publish	int	是	1416384868	发布时间戳
//	song_count	int	是	100	歌曲总数
//	company	string	是	BIG Machine	发行公司
//	language	string	是	英语	专辑语种
//	cd_count	int	是	1	专辑CD数
	
	public int album_id;
	public String album_name;
	public String album_logo;
	public int artist_id;
	public String artist_name;
	public int gmt_publish;
	public int song_count;
	public String company;
	public String language;
	public String cd_count;
	
   
}
