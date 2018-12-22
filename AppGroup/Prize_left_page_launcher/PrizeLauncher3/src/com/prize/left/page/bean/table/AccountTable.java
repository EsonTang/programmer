/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.left.page.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 类描述：账号表信息
 * @author fanjunchen
 */
@Table(name = "table_account")
public class AccountTable implements ITable {
	/**表名 */
	public static final String TABLE_NAME_ACCOUNT = "table_account";

	public static final String ACCOUNT_LOGINNAME = "loginName";
	public static final String ACCOUNT_PASSWORD = "password";
	public static final String ACCOUNT_PASSPORT = "passport";
	
	@Column(name = "id", isId = true)
    private int id;
    /**登录名*/
    @Column(name = "loginName")
    public String loginName;
    /**密码*/
    @Column(name = "password")
    public String password;
    
    /**passport*/
    @Column(name = "passport")
    public String passport;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
	/** 已下载table字段名数组*/
	public static final String ACCOUNT_COLUMNS[] = new String[] {
		AccountTable.ACCOUNT_LOGINNAME, AccountTable.ACCOUNT_PASSWORD,
		AccountTable.ACCOUNT_PASSPORT };
	
	
	@Override
	public String getTableName() {
		return TABLE_NAME_ACCOUNT;
	}
}
