package com.goodix.aidl;

import com.goodix.aidl.IEnrollCallback;
import com.goodix.aidl.IVerifyCallback;
import com.goodix.aidl.IUpdateBaseCallback;

interface IFingerprintManager 
{
	/*recognize*/
	int verify(IBinder token,IVerifyCallback callback);
	int cancelVerify(IBinder token);

	/*register*/
	int enroll(IBinder token,IEnrollCallback callback);
	int resetEnroll(IBinder token);
	int cancelEnroll(IBinder token);
	int saveEnroll(IBinder token,int index);

	/* fingerprint data*/
	int query();
	int delete(int index);

	/*password*/
	// int checkPassword(String password);
	// int changePassword(String oldPassword,String newPassword);

	/*engtest such as mode set*/
	// int EngTest(int cmd);

	/*get informaton about fingerFrint*/  
	String getInfo();

	void initMode();
	void setUpdateBaseCallback(IUpdateBaseCallback callback);
}
