package com.prize.weather.framework.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.prize.weather.framework.model.WeatherDBCache;

/**
 * 
 * @author wangzhong
 *
 */
public class WeatherCacheDataBaseDao implements IWeatherCacheDataBaseDao {

	private Context mContext;
	private Dao<WeatherDBCache, Integer> dao;

	public WeatherCacheDataBaseDao(Context context) {
		this.mContext = context;
		initDao();
	}

	private void initDao() {
		if (null == dao) {
			try {
				WeatherCacheDataBaseHelper helper = WeatherCacheDataBaseHelper.getInstance(mContext);
				dao = helper.getDao(WeatherDBCache.class);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int create(WeatherDBCache weatherDBCache) {
		initDao();
		int flag = -1;
		try {
			flag = dao.create(weatherDBCache);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public List<WeatherDBCache> queryAll() {
		initDao();
		List<WeatherDBCache> list = null;
		try {
			list = dao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public List<WeatherDBCache> queryByLike(String keyWord) {
		Log.d("WEATHER", "ACTION  WeatherCacheDataBaseDao  queryByLike(String keyWord)  keyWord = " + keyWord);
		initDao();
		List<WeatherDBCache> list = null;
		try {
			list = dao.queryBuilder().where().like("city_name", "%" + keyWord + "%").query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public List<WeatherDBCache> queryByLikeCreatedDate(String date) {
		initDao();
		List<WeatherDBCache> list = null;
		try {
			list = dao.queryBuilder().where().like("created_date", "%" + date + "%").query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public WeatherDBCache queryByID(int id) {
		initDao();
		WeatherDBCache weatherDBCache = null;
		try {
			weatherDBCache = dao.queryForId(Integer.valueOf(id));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return weatherDBCache;
	}

	@Override
	public int deleteAll() {
		initDao();
		// TODO Temporarily idle.
//		int flag = dao.d
		return 0;
	}

	@Override
	public int deleteByID(int id) {
		initDao();
		int flag = 0;
		try {
			flag = dao.deleteById(Integer.valueOf(id));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public int update(WeatherDBCache weatherDBCache) {
		initDao();
		int flag = 0;
		try {
			flag = dao.update(weatherDBCache);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flag;
	}

}
