
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/
/**
 *****************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************
 */

package com.prize.weather.view;


import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.prize.weather.detail.HourWeatherAdapter;

/**
 **
 * 类描述：
 * @author 作者
 * @version 版本
 */
public class HourWeatherLayout extends LinearLayout{
	private static final String TAG = "HourWeatherLayout";
	private Context mContext;
	HourWeatherAdapter mHourWeatherAdapter;
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	
	public HourWeatherLayout(Context context) {
		super(context);
	}

	
	 /**
	 * 方法描述：
	 * @param 参数名 说明
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	
	public HourWeatherLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}
	
	public void setAdapter(HourWeatherAdapter adapter){
		this.mHourWeatherAdapter = adapter;
		for (int i = 0; i < adapter.getCount(); i++){
//			Log.d(TAG,"i = "+i);
			final Map<String, Object> map = adapter.getItem(i);
			View view = adapter.getView(i, null, null);
			//view.setPadding(10, 0, 10, 0);
			this.setOrientation(HORIZONTAL);
			this.addView(view, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

}

