package com.pr.scuritycenter.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import tmsdk.common.module.aresengine.IKeyWordDao;

/**
 * 关键字配置
 * 作为DEMO，只是简单采用内存的方式保存数据
 * 实际项目开发当中，应该采用可持久化的数据保存方式，如读取文件或者SQLite等
 * @author serenazhou
 *
 */
public class KeyWordDao implements IKeyWordDao {
	private static List<String> mWords = new ArrayList<String>();
	private static KeyWordDao mKeyWordDao;
	private static final String STATIC_KEYWORDS[] =	{
		"花费", "西洋菜", "花式", "hot girl"
	};
	
	private KeyWordDao() {
		reset();
	}
	
	// 更新一遍关键字列表
	public void reset() {
		mWords.clear();
		for (final String word:STATIC_KEYWORDS)	{
			mWords.add(word);
		}
	}

	// 得到关键字配置实例
	public static KeyWordDao getInstance() {
		if (null == mKeyWordDao) {
			synchronized (KeyWordDao.class) {
				mKeyWordDao = new KeyWordDao();
			}
		}
		return mKeyWordDao;
	}
	
	// 判断用户输入关键字是否在列表中存在
	@Override
	public boolean contains(String msg) {
		for (String word : mWords) {
			if (msg.contains(word)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<String> getAll() {
		return (ArrayList<String>) mWords;
	}

	@Override
	public void setAll(List<String> words) {
		mWords = words;
	}

}
