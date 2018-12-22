package com.prize.prizethemecenter.ui.utils;

public class SearchResult {

	public interface Watcher {  
	    //再定义一个用来获取更新信息接收的方法  
	    public void updateNotify(String keyWord);
	}

	public interface WatcherChange{
		public void updateTips(String tips);
	}

}
