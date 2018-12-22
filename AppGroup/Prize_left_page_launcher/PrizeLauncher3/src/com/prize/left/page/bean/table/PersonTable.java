package com.prize.left.page.bean.table;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 类描述：个人信息表
 * @author fanjunchen
 */
@Table(name = "table_person")
public class PersonTable implements ITable {
	/** 表名 */
	public static final String TABLE_NAME_PERSON = "table_person";

	public static final String PERSON_USERID = "userId";
	public static final String PERSON_AVATAR = "avatar";
	public static final String PERSON_PHONE = "phone";
	public static final String PERSON_EMAIL = "email"; // 游戏显示小图标
	public static final String PERSON_REALNAME = "realName";
	public static final String PERSON_SEX = "sex";

	public static final String PERSON_COLUMNS[] = new String[] {
		PersonTable.PERSON_USERID, PersonTable.PERSON_AVATAR,
		PersonTable.PERSON_PHONE, PersonTable.PERSON_EMAIL,
		PersonTable.PERSON_REALNAME,
		PersonTable.PERSON_SEX};
	
	@Column(name = "id", isId = true)
    private int id;
    /**用户ID*/
    @Column(name = "userId")
    public String userId;
    /**等级*/
    @Column(name = "avatar")
    public String avatar;
    /**电话*/
    @Column(name = "phone")
    public String phone;
    /**邮箱*/
    @Column(name = "email")
    public String email;
    /**真实姓名*/
    @Column(name = "realName")
    public String realName;
    /**性别*/
    @Column(name = "sex")
    public int sex;
    
	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
	@Override
	public String getTableName() {
		return TABLE_NAME_PERSON;
	}
}
