package com.prize.onlinemusibean;

import java.io.Serializable;

public class AutoTipsBean implements Serializable{

		private static final long serialVersionUID = 1L;
		
//		type	string	是	艺人	类型, 如艺人, 歌曲, 专辑等
//		tip	string	是	刘德华	提示
//		url	string	是	/artist/1000	scheme url
//		object_type	int	是		对象状态, 0 正常, 1已下架, 2未发布, 3新歌
		
		public String type;
		public String tip;
		public String url;
		public int object_type;

}
