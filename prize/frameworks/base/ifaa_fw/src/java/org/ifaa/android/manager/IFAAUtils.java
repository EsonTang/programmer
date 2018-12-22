/**
 * @author yangzh6
 * 
 * Reference: <<IFAA 标准: REE 系统框架部分>>
 */

package org.ifaa.android.manager;

public class IFAAUtils {

	/** BIO TYPES */
	public static final int BIO_TYPE_FINGERPRINT	= 1 << 0; //0x01;
	public static final int BIO_TYPE_IRIS			= 1 << 1; //0x02;

	/** Return Values */
	public static final int COMMAND_OK				= 0;
	public static final int COMMAND_FAIL			= -1;

	/** Start Activity */
	public static final String ACTION_FP_SETTINGS	= "mediatek.settings.FINGERPRINT_OPERATION";
	public static final String ACTION_IRIS_SETTINGS	= "android.settings.IRIS_SETUP";

	/** Functions */
	public static boolean authTypeValid(int authType) {
		switch (authType) {
		case IFAAUtils.BIO_TYPE_FINGERPRINT:
		case IFAAUtils.BIO_TYPE_IRIS: {
			return true;
		}
		}
		return false;
	}

}
