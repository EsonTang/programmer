package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchArtistsResponse implements Serializable{

	private static final long serialVersionUID = 1L;
	
//	total	int	是	25	总数
//	more	bool	是	1	是否有下一页
//	artists	array	是		艺人结果集 元素类型为object, 查看元素定义
	public int total;
	public boolean more;
	public ArrayList<ArtistsBean> artists = new ArrayList<ArtistsBean>();

}
