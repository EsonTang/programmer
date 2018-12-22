
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：数据库操作接口
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

package com.android.notepad.note.database;

import java.util.List;

import com.android.notepad.note.model.NoteEvent;

/**
 **
 * 类描述：数据库操作接口类
 * @author 朱道鹏
 * @version V1.0
 */
public interface NotePadDataBaseDao {
	
	public int create(NoteEvent event);
	
	public List<NoteEvent> queryForAll();
	
	public int deleteById(int id);
	
	public NoteEvent queryForId(int id);
	
	public int update(NoteEvent event);
	
	public int updateId(NoteEvent event,int id);
	
	public List<NoteEvent> queryForLike(String keyWord);
}

