package com.prize.onlinemusibean;

import java.io.Serializable;

/**歌手*/
public class ArtistsBean implements Serializable{


	private static final long serialVersionUID = 1L;
    
//	artist_id	int	是	135	艺人id, BIGINT类型
//	artist_name	string	是	陈奕迅	艺人名字
//	artist_logo	string	是	http://img.xiami.net/images/artistlogo/82/13832967518882.jpg	艺人头像
//	count_likes	int	是	100	粉丝数
//	area	string	是	大陆	地区
//	english_name	string	是	Eason Chan	英文名
//	recommends	int	是		分享数
//	gender	string	是		性别, M 男性, F 女性, B 乐队
//	category	int	是		艺人类别
//	description	string	是	liudehua	描述信息
	
	public int artist_id;
	public String artist_name;
	public String artist_logo;  
	public int count_likes;
	public String area;	
	public String english_name;
	public int recommends;	
	public String gender;
	public int category;
	public String description;
	public int albums_count;
	public int songs_count;
	
}
