package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.CallLogEntity;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ICallLogDao;
import android.util.Log;

/**
 * 私密电话记录配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class PrivateCallLogDao implements ICallLogDao<CallLogEntity> {
	private static List<CallLogEntity> mSecureCallLogList = new ArrayList<CallLogEntity>();
	private static PrivateCallLogDao msecureCallLogDao;
	
	private PrivateCallLogDao() {
	}
	
	// 获取私密电话记录配置实例
	public static PrivateCallLogDao getInstance() {
		if (null == msecureCallLogDao) {
			synchronized (PrivateCallLogDao.class) {
				msecureCallLogDao = new PrivateCallLogDao();
			}
		}
		return msecureCallLogDao;
	}
	
	// 加入新的私密电话信息
	@Override
	public long insert(CallLogEntity entity, FilterResult result) {
		Log.i("PrivateCallLogDao", "Inserting " + entity.name + " " + entity.phonenum);
		mSecureCallLogList.add(entity);
		return -1;
	}
}
