package com.prize.left.page.bean;

import android.graphics.Bitmap;

public class ContactPerson {
	/**联系人姓名*/
	public String name;
	/**联系人电话*/
	public String phoneNum;
	/**1 呼入, 2 呼出, 3 未接*/
	public int type;
	/**头像*/
	public Bitmap headIco;
	
	public long contactId;
	
	public ContactPerson() {
	}

}
