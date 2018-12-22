package com.prize.app.net.req;

/**
 * 游戏评论获取评论列表 特殊分页处理，因为评论是 记载更多的方式，用户频繁刷新
 * 
 * @author prize
 * @version 1.0 2013-4-11
 * 
 */
public class GetGameCommentListReq extends BaseReq {
	private String gameCode;// 游戏编号
	private Integer lastItemId;// 上次请求的最后一条数据的id,null代表新的请求

	public String getGameCode() {
		return gameCode;
	}

	public void setGameCode(String gameCode) {
		this.gameCode = gameCode;
	}

	public Integer getLastItemId() {
		return lastItemId;
	}

	public void setLastItemId(Integer lastItemId) {
		this.lastItemId = lastItemId;
	}

}
