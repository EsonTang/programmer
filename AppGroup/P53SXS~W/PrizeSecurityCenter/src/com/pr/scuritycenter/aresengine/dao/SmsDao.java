package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.ISmsDao;
import tmsdk.common.module.aresengine.SmsEntity;

/**
 * 短信拦截记录配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class SmsDao implements ISmsDao<SmsEntity> {
	private static List<SmsEntity> mSmsList = new ArrayList<SmsEntity>();	
	private static SmsDao mSmsDao;
	
	private SmsDao() {
	}
	
	// 获取短信拦截记录配置实例
	public static SmsDao getInstance() {
		if (null == mSmsDao) {
			synchronized (SmsDao.class) {
				mSmsDao = new SmsDao();
			}
		}
		return mSmsDao;
	}
	
	// 移除短信拦截记录
	public boolean delete(SmsEntity entity) {
		mSmsList.remove(entity);
		return true;
	}

	// 加入新的短信拦截记录
	@Override
	public long insert(SmsEntity entity, FilterResult result) {
		mSmsList.add(entity);
		return mSmsList.size() - 1;
	}

	// 更新短信拦截记录列表，若匹配到正确的短信拦截记录，删掉现有的，再重新加入一遍
	public boolean update(SmsEntity entity) {
		int size = mSmsList.size();
		SmsEntity tempEntity;
		for(int i = 0; i < size; i++) {
			tempEntity = mSmsList.get(i);
			if(tempEntity.phonenum.equals(entity.phonenum)){
				mSmsList.remove(tempEntity);
				mSmsList.add(i,tempEntity);
			}
		}
		return true;
	}
}
