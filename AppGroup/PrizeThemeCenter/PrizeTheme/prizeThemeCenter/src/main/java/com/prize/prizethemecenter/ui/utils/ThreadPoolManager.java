package com.prize.prizethemecenter.ui.utils;

/**
 * @更新人: $Author: bxh $
 * @更新时间: $Date: 2016年9月8日14:50:37
 * @更新描述: TODO
 */
public class ThreadPoolManager
{

	private static ThreadPoolProxy	mLongPool;						// 耗时操作的池子
	private static Object			mLongLock		= new Object();

	private static ThreadPoolProxy	mDownloadPool;					// 下载的池子
	private static Object			mDownloadLock	= new Object();

	/**
	 * 获得耗时操作的池子
	 * 
	 * @return
	 */
	public static ThreadPoolProxy getLongPool()
	{
		if (mLongPool == null)
		{
			synchronized (mLongLock)
			{
				if (mLongPool == null)
				{
					mLongPool = new ThreadPoolProxy(3, 3, 0L);
				}
			}
		}
		return mLongPool;
	}

	/**
	 * 获得下载的池子
	 * 
	 * @return
	 */
	public static ThreadPoolProxy getDownloadPool()
	{
		if (mDownloadPool == null)
		{
			synchronized (mDownloadLock)
			{
				if (mDownloadPool == null)
				{
					mDownloadPool = new ThreadPoolProxy(3, 3, 0L);
				}
			}
		}
		return mDownloadPool;
	}
}
