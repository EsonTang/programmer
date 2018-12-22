package com.prize.music;

public class SearchResult {
    
	public interface Watched {  
	    //在其接口中定义一个用来增加观察者的方法  
	    public void add(Watcher watcher);  
	    //再定义一个用来删除观察者权利的方法  
	    public void remove(Watcher watcher);  
	    //再定义一个可以实现行为变现并向观察者传输信息的方法  
	    public void notifyWatcher(String keyWord);  
	}  
	
	public interface Watcher {  
	    //再定义一个用来获取更新信息接收的方法  
	    public void updateNotify(String keyWord);  
	} 

}
