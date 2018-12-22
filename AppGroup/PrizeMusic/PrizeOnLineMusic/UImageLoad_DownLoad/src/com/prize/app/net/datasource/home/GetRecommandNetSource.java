package com.prize.app.net.datasource.home;

import com.prize.app.net.datasource.base.SimpleGamePageNetSource;

/**
 **
 * 获取广告资源
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class GetRecommandNetSource extends SimpleGamePageNetSource {
	// 100 代表 滚动推荐游戏
	private static final String LISTCODE = "110";

	public GetRecommandNetSource() {
		super(LISTCODE);
	}

}
