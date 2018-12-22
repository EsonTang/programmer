package com.goodix.aidl;

interface IVerifyCallback 
{ 
	boolean handleMessage(int msg,int arg0,int arg1,inout byte[] data);
}