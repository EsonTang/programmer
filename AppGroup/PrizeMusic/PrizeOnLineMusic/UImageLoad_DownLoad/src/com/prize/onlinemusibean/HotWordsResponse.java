package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  热搜热词
 * @author pengyang
 *
 */
public class HotWordsResponse implements Serializable{
    
	private static final long serialVersionUID = 1L;
	
	public ArrayList<SearchWords> search_words = new ArrayList<SearchWords>();
	public ArrayList<SearchWords> star_words = new ArrayList<SearchWords>();
	
	public class SearchWords implements Serializable{
		
		private static final long serialVersionUID = 1L;
		
        //   word	string	是	刘德华	热词
	    //	change	int	是	100	指数变化, 正数代表上升, 负数代表下降
		
		public String word;
		public int change;
	}
	
	public class StarWords implements Serializable{
		
		private static final long serialVersionUID = 1L;
//		word	string	是	中国好声音	词
//		url	string	是	xiami://artist/100	跳转地址
		
		public String word;
		public String url;
	}
	
}

