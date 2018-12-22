package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  返回搜索推荐歌手
 * @author Administrator
 *
 */
public class RecommendSingerResponse implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public ArrayList<ArtistsBean> artists = new ArrayList<ArtistsBean>();

}
