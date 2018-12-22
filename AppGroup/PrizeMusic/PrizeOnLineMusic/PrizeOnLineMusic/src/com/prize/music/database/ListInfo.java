package com.prize.music.database;

/**
 * @see 保存歌单信息类
 * @author lixing
 *
 */
public class ListInfo {

	public static final long DEFAULT_LOCAL_LIST_ID = 0; // 仅用于本地歌单，本地歌单默认list_id
	public static final String DEFALUT_LOCAL_SOURCE_ONLINE_TYPE = "local"; // 仅用于本地歌单，本地歌单默认的source_online_type
	/** 该歌单的名字 **/
	public String menuName; //
	public String list_table_name; // // 该歌单对应的表名.每个歌单一个表
	public long menuId; // 该歌单对应的id 在线歌单用到，本地歌单使用list_table_name,本地歌单的id为0
	/**
	 * 歌单类型，是本地歌单，还是在线歌单
	 * DatabaseConstant.ONLIEN_TYPE/DatabaseConstant.LOCAL_TYPE
	 **/
	public String source_type; //
	public String list_user_id; // 该歌单对应的用户的id
	/** 在线歌单类型，是CollectDetailResponse还是AlbumDetailResponse **/
	public String /* source_online_type */menuType;//
	public String menuImageUrl;//
//	// 保存本地歌曲的bitmap字节数据
//	public byte[] inImage;

	
	
	public ListInfo(String list_name, String list_table_name, long list_id, String source_type, String menuType, String list_user_id){
		this.menuName = list_name;
		this.list_table_name = list_table_name;
		this.menuId = list_id;
		this.source_type = source_type;
		this.menuType = menuType;
		this.list_user_id = list_user_id;
	}
	
	public ListInfo(){
		
	}
	
	/**
	 * @return the list_name
	 */
	public String getList_name() {
		return menuName;
	}
	/**
	 * @param list_name the list_name to set
	 */
	public void setList_name(String list_name) {
		this.menuName = list_name;
	}
	/**
	 * @return the list_table_name
	 */
	public String getList_table_name() {
		return list_table_name;
	}
	/**
	 * @param list_table_name the list_table_name to set
	 */
	public void setList_table_name(String list_table_name) {
		this.list_table_name = list_table_name;
	}
	/**
	 * @return the list_id
	 */
	public long getList_id() {
		return menuId;
	}
	/**
	 * @param list_id the list_id to set
	 */
	public void setList_id(long list_id) {
		this.menuId = list_id;
	}
	/**
	 * @return the source_type
	 */
	public String getSource_type() {
		return source_type;
	}
	/**
	 * @param source_type the source_type to set
	 */
	public void setSource_type(String source_type) {
		this.source_type = source_type;
	}
	/**
	 * @return the list_user_id
	 */
	public String getList_user_id() {
		return list_user_id;
	}
	/**
	 * @param list_user_id the list_user_id to set
	 */
	public void setList_user_id(String list_user_id) {
		this.list_user_id = list_user_id;
	}
	
	public String toString(){
		return "list_name = " + menuName + ", list_table_name = " + list_table_name
				+ ", list_id = " + menuId + ", source_type = " + source_type
				+ ", list_user_id = " + list_user_id + ", menuType = " + menuType 
				;
	}
	
}
