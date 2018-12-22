package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchSongsResponse implements Serializable{

	private static final long serialVersionUID = 1L;
	
//	total	int	是	25	总数
//	more	bool	是	1	是否有下一页
//	songs	array	是		歌曲结果集 元素类型为object, 查看元素定义
	public int total;
	public boolean more;
	public ArrayList<SongDetailInfo> songs = new ArrayList<SongDetailInfo>();

}
