package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  返回搜索匹配全部歌手、专辑、歌曲
 * @author Administrator
 *
 */
public class SearchAllsResponse implements Serializable{
	
	private static final long serialVersionUID = 1L;
//	song_count	int	是	1000	歌曲搜索总数
//	album_count	int	是	100	专辑搜索总数
//	artist_count	int	是	10	艺人搜索总数
//	collect_count	int	是	10	精选集搜索总数
	public int song_count;
	public int album_count;
	public int artist_count;
	public int collect_count;
	
	public ArrayList<SongsBean> songs = new ArrayList<SongsBean>();
	public ArrayList<AlbumsBean> albums = new ArrayList<AlbumsBean>();
	public ArrayList<ArtistsBean> artists = new ArrayList<ArtistsBean>();
	
}

