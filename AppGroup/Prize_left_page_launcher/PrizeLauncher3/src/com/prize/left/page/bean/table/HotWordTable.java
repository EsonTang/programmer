package com.prize.left.page.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "table_hotword")
public class HotWordTable  implements ITable {

	/**表名 */
	public static final String TABLE_NAME_ACCOUNT = "table_hotword";
	
	public HotWordTable() {
	}

	@Column(name = "id", isId = true)
    private int id;
	
	@Column(name = "word")
	public String word;
	
	@Column(name = "sort")
	public String sort;
	
	@Column(name = "url")
	public String url;
	
	@Override
	public String getTableName() {
		return TABLE_NAME_ACCOUNT;
	}
}
