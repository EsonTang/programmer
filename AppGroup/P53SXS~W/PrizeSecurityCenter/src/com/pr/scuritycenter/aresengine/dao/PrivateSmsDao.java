package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.SmsEntity;
import android.util.Log;

/**
 * 私密短信记录配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class PrivateSmsDao implements ISmsDao<SmsEntity> {
	private static List<SmsEntity> mSecureSmsList = new ArrayList<SmsEntity>();	
	private static PrivateSmsDao mSecureSmsDao;
	
	private PrivateSmsDao() {
	}
	
	// 获取私密短信记录配置实例
	public static PrivateSmsDao getInstance() {
		if (null == mSecureSmsDao) {
			synchronized (PrivateSmsDao.class) {
				mSecureSmsDao = new PrivateSmsDao();
			}
		}
		return mSecureSmsDao;
	}
	
	// 删除私密短信记录
	public boolean delete(SmsEntity entity) {
		mSecureSmsList.remove(entity);
		return true;
	}
	
	// 加入新的私密短信记录信息
	@Override
	public long insert(SmsEntity entity, FilterResult result) {
		Log.i("PrivateSmsDao", "Inserting " + entity.name + " " + entity.phonenum);
		mSecureSmsList.add(entity);
		return mSecureSmsList.size() - 1;
	}

	// 更新私密短信记录，若匹配到正确的私密短信记录，删掉现有的，再重新加入一遍
	public boolean update(SmsEntity entity) {
		int size = mSecureSmsList.size();
		SmsEntity tempEntity;
		for(int i = 0; i < size; i++) {
			tempEntity = mSecureSmsList.get(i);
			if(tempEntity.phonenum.equals(entity.phonenum)){
				mSecureSmsList.remove(tempEntity);
				mSecureSmsList.add(i,tempEntity);
			}
		}
		return true;
	}
}
