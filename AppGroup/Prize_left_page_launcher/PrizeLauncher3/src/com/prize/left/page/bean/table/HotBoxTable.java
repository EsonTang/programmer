package com.prize.left.page.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "table_hotbox")
public class HotBoxTable  implements ITable {

	/**表名 */
	public static final String TABLE_NAME_ACCOUNT = "table_hotbox";
	
	public HotBoxTable() {
	}

	@Column(name = "id", isId = true)
	public String id;
	
	@Column(name = "name")
	public String name;
	
	@Column(name = "url")
	public String url;
	
	public String placeholders;
	
	@Override
	public String getTableName() {
		return TABLE_NAME_ACCOUNT;
	}
}
