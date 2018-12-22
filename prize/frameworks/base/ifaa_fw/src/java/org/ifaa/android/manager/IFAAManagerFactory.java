/**
 * @author yangzh6
 *
 * Reference: <<IFAA标准-REE系统框架部分.pdf>>
 */

package org.ifaa.android.manager;

import android.content.Context;
import android.util.Log;

public class IFAAManagerFactory {
	private static final String TAG = IFAAManagerFactory.class.getSimpleName();

	/**
	 * Description: 返回对应的IFAAManager实例
	 *
	 * @param context
	 *            上下文环境
	 * @param authType
	 *            生物特征识别类型,指纹为1,虹膜为2
	 * @return 返回对应的IFAAManager实例
	 */
	public static IFAAManager getIFAAManager(Context context, int authType) {
		if ((null == context) || !IFAAUtils.authTypeValid(authType)) {
			Log.e(TAG, "context = " + context + ", authType = " + authType);
			return null;
		}

		switch (authType) {
		case IFAAUtils.BIO_TYPE_FINGERPRINT: {
			return new IFAAManagerFp();
		}
		case IFAAUtils.BIO_TYPE_IRIS: {
			// return new IFAAManagerIris();
		}
		}
		return null;
	}
}
