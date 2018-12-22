package com.prize.music.database;

/**
 * 用于与服务器交互，批量取消歌单
 * @see 保存歌单信息类
 * @author longbaoxiu
 *
 */
public class SubListInfo {
	
	/**该歌单的名字**/
	public String menuName;  //
	public long menuId;  //该歌单对应的id 在线歌单用到，本地歌单使用list_table_name,本地歌单的id为0
	/**歌单类型，是本地歌单，还是在线歌单 DatabaseConstant.ONLIEN_TYPE/DatabaseConstant.LOCAL_TYPE**/
	/**在线歌单类型，是CollectDetailResponse还是AlbumDetailResponse**/
	public String /*source_online_type*/menuType;// 
	public String menuImageUrl;// 

	
	
	
	public SubListInfo(){
		
	}




	public SubListInfo(String menuName, long menuId, String menuType,
			String menuIamgeUrl) {
		super();
		this.menuName = menuName;
		this.menuId = menuId;
		this.menuType = menuType;
		this.menuImageUrl = menuIamgeUrl;
	}
	

}
