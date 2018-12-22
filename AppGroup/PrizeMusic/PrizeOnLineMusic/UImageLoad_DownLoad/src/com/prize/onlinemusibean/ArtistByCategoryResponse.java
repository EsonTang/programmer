package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * 搜索艺人
 * */
public class ArtistByCategoryResponse implements Serializable{

	private static final long serialVersionUID = 1L;
	
//	artists	array	是		艺人列表 元素类型为object, 查看元素定义
//	total	int	否	5	总数
//	more	bool	否	1	是否有下一页
	
	public ArrayList<ArtistsBean> artists = new ArrayList<ArtistsBean>();
	public int total;
	public boolean more;
}
