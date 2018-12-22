
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：Note事件类
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.android.notepad.note.model;


/**
 **
 * 类描述：Note事件类
 * @author 朱道鹏
 * @version V1.0
 */
public class NoteEvent implements Comparable<NoteEvent>{
	
	public static final String TABLE_NAME = "note_event";
	
	public static final String ID = "_id";
	public static final String CONTENTS = "contents";
	public static final String CREATED_DATE = "created_date";
	public static final String BG_COLOR = "bg_color";
	public static final String FONT_SIZE = "font_size";
	public static final String UUID = "uuid";
	
	/**
	 * id，无需主动设置，数据库已设为自增长
	 */
    private int id;  
  
	/**
	 * 便签事件内容
	 */
    private String contents;  
  
    /**
	 * 便签建立时间
	 */
    private long created_date;  
    
    /**
	 * 便签的背景res
	 */
    private int bg_color;  
    
    /**
	 * 便签文本字体大小
	 */
    private int font_size;
    
    /**
	 * 唯一标识
	 */
    private String uuid;
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public long getCreateDate() {
		return created_date;
	}

	public void setCreateDate(long created_date) {
		this.created_date = created_date;
	}

	public int getBgColor() {
		return bg_color;
	}

	public void setBgColor(int bg_color) {
		this.bg_color = bg_color;
	}

	public int getFontSize() {
		return font_size;
	}

	public void setFontSize(int font_size) {
		this.font_size = font_size;
	}
	
	 public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * 方法描述：建立时间比较器
	 * @param NoteEvent
	 * @return int
	 * @see NoteEvent#compareTo
	 */
	@Override
	public int compareTo(NoteEvent another) {
		return (int)(another.getCreateDate()-created_date);
	}  
  
    
}

