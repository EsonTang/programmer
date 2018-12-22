package com.android.dialer.service;
interface ILocationService
{
  /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -start*/
  String getLocationInfo(String phoneNumber);
  /*PRIZE-add aidl for get location by service -qiaohu-2018-6-11 -end*/
}