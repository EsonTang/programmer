package com.android.settings;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class LeatekCpuCorePreference extends Preference {

	private static final String TAG = "LeatekCpuCorePreference";
	private static final int TITLE_ID = R.id.cpu_title;
	private static final int SUMMARY_ID = R.id.cpu_summary;
	private static final int IMAGE_ID = R.id.cpu_state;
	private TextView mPreferenceTitle = null;
	private TextView mPreferenceSummary = null;
	private static ImageView mPreferenceImage = null;
	private CharSequence mTitleValue = "";
	private static String mSummaryValue = "";
	private static String mCpuEightCores;
	private AnimationDrawable mAnimationDrawable;

	public static final int UPDATE_CPU_STATE = 0x0001;

	private static int mCpuIndex = 0;

	private static int mCpuIndex2 = 0;
	

	private static int[] mCpuStateArray = {
			R.drawable.leatek_cpu_state_1, R.drawable.leatek_cpu_state_2,
			R.drawable.leatek_cpu_state_3, R.drawable.leatek_cpu_state_4,
			R.drawable.leatek_cpu_state_5,

			R.drawable.leatek_cpu_state_6, R.drawable.leatek_cpu_state_7,
			R.drawable.leatek_cpu_state_8, R.drawable.leatek_cpu_state_9,
			R.drawable.leatek_cpu_state_10,

			R.drawable.leatek_cpu_state_11, R.drawable.leatek_cpu_state_12,
			R.drawable.leatek_cpu_state_13, R.drawable.leatek_cpu_state_14,
			R.drawable.leatek_cpu_state_15,

			R.drawable.leatek_cpu_state_16, R.drawable.leatek_cpu_state_17,
			R.drawable.leatek_cpu_state_18, R.drawable.leatek_cpu_state_19,
			R.drawable.leatek_cpu_state_20,

			R.drawable.leatek_cpu_state_21, R.drawable.leatek_cpu_state_22,
			R.drawable.leatek_cpu_state_23, R.drawable.leatek_cpu_state_24,
			R.drawable.leatek_cpu_state_25,

			R.drawable.leatek_cpu_state_26, R.drawable.leatek_cpu_state_27,
			R.drawable.leatek_cpu_state_28, R.drawable.leatek_cpu_state_29,
			R.drawable.leatek_cpu_state_30,

			R.drawable.leatek_cpu_state_31, R.drawable.leatek_cpu_state_32,
			R.drawable.leatek_cpu_state_33, R.drawable.leatek_cpu_state_34,
			R.drawable.leatek_cpu_state_35,

			R.drawable.leatek_cpu_state_36, R.drawable.leatek_cpu_state_37,
			R.drawable.leatek_cpu_state_38, R.drawable.leatek_cpu_state_39,
			R.drawable.leatek_cpu_state_40,

			R.drawable.leatek_cpu_state_41, R.drawable.leatek_cpu_state_42,
			R.drawable.leatek_cpu_state_43, R.drawable.leatek_cpu_state_44,
			R.drawable.leatek_cpu_state_45,

			R.drawable.leatek_cpu_state_46, R.drawable.leatek_cpu_state_47,
			R.drawable.leatek_cpu_state_48, R.drawable.leatek_cpu_state_49,
			R.drawable.leatek_cpu_state_50,

			R.drawable.leatek_cpu_state_51, R.drawable.leatek_cpu_state_52,
			R.drawable.leatek_cpu_state_53, R.drawable.leatek_cpu_state_54,
			R.drawable.leatek_cpu_state_55,

			R.drawable.leatek_cpu_state_56, R.drawable.leatek_cpu_state_57,
			R.drawable.leatek_cpu_state_58, R.drawable.leatek_cpu_state_59,
			R.drawable.leatek_cpu_state_60,

			R.drawable.leatek_cpu_state_61, R.drawable.leatek_cpu_state_62,
			R.drawable.leatek_cpu_state_63, R.drawable.leatek_cpu_state_64,
			R.drawable.leatek_cpu_state_65,

			R.drawable.leatek_cpu_state_66, R.drawable.leatek_cpu_state_67,
			R.drawable.leatek_cpu_state_68, R.drawable.leatek_cpu_state_69,
			R.drawable.leatek_cpu_state_70,

			R.drawable.leatek_cpu_state_71, R.drawable.leatek_cpu_state_72,
			R.drawable.leatek_cpu_state_73, R.drawable.leatek_cpu_state_74,
			R.drawable.leatek_cpu_state_75,

			R.drawable.leatek_cpu_state_76, R.drawable.leatek_cpu_state_77,
			R.drawable.leatek_cpu_state_78, R.drawable.leatek_cpu_state_79,
			R.drawable.leatek_cpu_state_80,

			R.drawable.leatek_cpu_state_81, R.drawable.leatek_cpu_state_82,
			R.drawable.leatek_cpu_state_83, R.drawable.leatek_cpu_state_84,
			R.drawable.leatek_cpu_state_85,

			R.drawable.leatek_cpu_state_86, R.drawable.leatek_cpu_state_87,
			R.drawable.leatek_cpu_state_88, R.drawable.leatek_cpu_state_89,
			R.drawable.leatek_cpu_state_90,

			R.drawable.leatek_cpu_state_91, R.drawable.leatek_cpu_state_92,
			R.drawable.leatek_cpu_state_93, R.drawable.leatek_cpu_state_94,
			R.drawable.leatek_cpu_state_95,

			R.drawable.leatek_cpu_state_96, R.drawable.leatek_cpu_state_97,
			R.drawable.leatek_cpu_state_98, R.drawable.leatek_cpu_state_99,
			R.drawable.leatek_cpu_state_100,

			R.drawable.leatek_cpu_state_101, R.drawable.leatek_cpu_state_102,
			R.drawable.leatek_cpu_state_103, R.drawable.leatek_cpu_state_104,
			R.drawable.leatek_cpu_state_105,

			R.drawable.leatek_cpu_state_106, R.drawable.leatek_cpu_state_107,
			R.drawable.leatek_cpu_state_108, R.drawable.leatek_cpu_state_109,
			R.drawable.leatek_cpu_state_110,

			R.drawable.leatek_cpu_state_111, R.drawable.leatek_cpu_state_112,
			R.drawable.leatek_cpu_state_113, R.drawable.leatek_cpu_state_114,
			R.drawable.leatek_cpu_state_115,

			R.drawable.leatek_cpu_state_116, R.drawable.leatek_cpu_state_117,
			R.drawable.leatek_cpu_state_118, R.drawable.leatek_cpu_state_119,
			R.drawable.leatek_cpu_state_120,

			R.drawable.leatek_cpu_state_121, R.drawable.leatek_cpu_state_122,
			R.drawable.leatek_cpu_state_123, R.drawable.leatek_cpu_state_124,
			R.drawable.leatek_cpu_state_125,

			R.drawable.leatek_cpu_state_126 };
	private static int[] mCpuStateArray2 = {
			R.drawable.leatek_cpu2_state_1, R.drawable.leatek_cpu2_state_2,
			R.drawable.leatek_cpu2_state_3, R.drawable.leatek_cpu2_state_4,
			R.drawable.leatek_cpu2_state_5,

			R.drawable.leatek_cpu2_state_6, R.drawable.leatek_cpu2_state_7,
			R.drawable.leatek_cpu2_state_8, R.drawable.leatek_cpu2_state_9,
			R.drawable.leatek_cpu2_state_10,

			R.drawable.leatek_cpu2_state_11, R.drawable.leatek_cpu2_state_12,
			R.drawable.leatek_cpu2_state_13, R.drawable.leatek_cpu2_state_14,
			R.drawable.leatek_cpu2_state_15,

			R.drawable.leatek_cpu2_state_16, R.drawable.leatek_cpu2_state_17,
			R.drawable.leatek_cpu2_state_18, R.drawable.leatek_cpu2_state_19,
			R.drawable.leatek_cpu2_state_20,

			R.drawable.leatek_cpu2_state_21, R.drawable.leatek_cpu2_state_22,
			R.drawable.leatek_cpu2_state_23, R.drawable.leatek_cpu2_state_24,
			R.drawable.leatek_cpu2_state_25,

			R.drawable.leatek_cpu2_state_26, R.drawable.leatek_cpu2_state_27,
			R.drawable.leatek_cpu2_state_28, R.drawable.leatek_cpu2_state_29,
			R.drawable.leatek_cpu2_state_30,

			R.drawable.leatek_cpu2_state_31, R.drawable.leatek_cpu2_state_32,
			R.drawable.leatek_cpu2_state_33, R.drawable.leatek_cpu2_state_34,
			R.drawable.leatek_cpu2_state_35,

			R.drawable.leatek_cpu2_state_36, R.drawable.leatek_cpu2_state_37,
			R.drawable.leatek_cpu2_state_38, R.drawable.leatek_cpu2_state_39,
			R.drawable.leatek_cpu2_state_40,

			R.drawable.leatek_cpu2_state_41, R.drawable.leatek_cpu2_state_42,
			R.drawable.leatek_cpu2_state_43, R.drawable.leatek_cpu2_state_44,
			R.drawable.leatek_cpu2_state_45,

			R.drawable.leatek_cpu2_state_46, R.drawable.leatek_cpu2_state_47,
			R.drawable.leatek_cpu2_state_48, R.drawable.leatek_cpu2_state_49,
			R.drawable.leatek_cpu2_state_50,

			R.drawable.leatek_cpu2_state_51, R.drawable.leatek_cpu2_state_52,
			R.drawable.leatek_cpu2_state_53, R.drawable.leatek_cpu2_state_54,
			R.drawable.leatek_cpu2_state_55,

			R.drawable.leatek_cpu2_state_56, R.drawable.leatek_cpu2_state_57,
			R.drawable.leatek_cpu2_state_58, R.drawable.leatek_cpu2_state_59,
			R.drawable.leatek_cpu2_state_60,

			R.drawable.leatek_cpu2_state_61, R.drawable.leatek_cpu2_state_62,
			R.drawable.leatek_cpu2_state_63, R.drawable.leatek_cpu2_state_64,
			R.drawable.leatek_cpu2_state_65,

			R.drawable.leatek_cpu2_state_66, R.drawable.leatek_cpu2_state_67,
			R.drawable.leatek_cpu2_state_68, R.drawable.leatek_cpu2_state_69,
			R.drawable.leatek_cpu2_state_70,

			R.drawable.leatek_cpu2_state_71, R.drawable.leatek_cpu2_state_72,
			R.drawable.leatek_cpu2_state_73, R.drawable.leatek_cpu2_state_74,
			R.drawable.leatek_cpu2_state_75,

			R.drawable.leatek_cpu2_state_76, R.drawable.leatek_cpu2_state_77,
			R.drawable.leatek_cpu2_state_78, R.drawable.leatek_cpu2_state_79,
			R.drawable.leatek_cpu2_state_80,

			R.drawable.leatek_cpu2_state_81, R.drawable.leatek_cpu2_state_82,
			R.drawable.leatek_cpu2_state_83, R.drawable.leatek_cpu2_state_84,
			R.drawable.leatek_cpu2_state_85,

			R.drawable.leatek_cpu2_state_86, R.drawable.leatek_cpu2_state_87,
			R.drawable.leatek_cpu2_state_88, R.drawable.leatek_cpu2_state_89,
			R.drawable.leatek_cpu2_state_90,

			R.drawable.leatek_cpu2_state_91, R.drawable.leatek_cpu2_state_92,
			R.drawable.leatek_cpu2_state_93, R.drawable.leatek_cpu2_state_94,
			R.drawable.leatek_cpu2_state_95,

			R.drawable.leatek_cpu2_state_96, R.drawable.leatek_cpu2_state_97,
			R.drawable.leatek_cpu2_state_98, R.drawable.leatek_cpu2_state_99,
			R.drawable.leatek_cpu2_state_100,

			R.drawable.leatek_cpu2_state_101, R.drawable.leatek_cpu2_state_102,
			R.drawable.leatek_cpu2_state_103, R.drawable.leatek_cpu2_state_104,
			R.drawable.leatek_cpu2_state_105,

			R.drawable.leatek_cpu2_state_106, R.drawable.leatek_cpu2_state_107,
			R.drawable.leatek_cpu2_state_108, R.drawable.leatek_cpu2_state_109,
			R.drawable.leatek_cpu2_state_110,

			R.drawable.leatek_cpu2_state_111, R.drawable.leatek_cpu2_state_112,
			R.drawable.leatek_cpu2_state_113, R.drawable.leatek_cpu2_state_114,
			R.drawable.leatek_cpu2_state_115,

			R.drawable.leatek_cpu2_state_116, R.drawable.leatek_cpu2_state_117,
			R.drawable.leatek_cpu2_state_118, R.drawable.leatek_cpu2_state_119,
			R.drawable.leatek_cpu2_state_120,

			R.drawable.leatek_cpu2_state_121, R.drawable.leatek_cpu2_state_122,
			R.drawable.leatek_cpu2_state_123, R.drawable.leatek_cpu2_state_124,
			R.drawable.leatek_cpu2_state_125,

			R.drawable.leatek_cpu2_state_126 };

	public static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_CPU_STATE:
				if (null != mPreferenceImage) {
					Log.e(TAG, "------------mPreferenceImage-----------");
					if (mSummaryValue.indexOf(mCpuEightCores) != -1) {
						mCpuIndex = (mCpuIndex >= mCpuStateArray.length) ? 0
								: mCpuIndex;
						mPreferenceImage
								.setBackgroundResource(mCpuStateArray[mCpuIndex++]);
					} else {
						mCpuIndex2 = (mCpuIndex2 >= mCpuStateArray2.length) ? 0
								: mCpuIndex2;
						mPreferenceImage
								.setBackgroundResource(mCpuStateArray2[mCpuIndex2++]);
								
						mPreferenceImage.setBackgroundResource(0);//add liup 20171130
					}
					mHandler.removeMessages(UPDATE_CPU_STATE);
					mHandler.sendEmptyMessageDelayed(UPDATE_CPU_STATE, 1000);
				} else {
					Log.e(TAG, "---------mPreferenceImage is NULL----------");
				}
				break;
			}
		}

	};

	public LeatekCpuCorePreference(Context context) {
		this(context, null);
	}

	public LeatekCpuCorePreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LeatekCpuCorePreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		mCpuEightCores = context.getString(R.string.cpu_eight_cores);

		setLayoutResource(R.layout.leatek_cpu_core_view);

		if (super.getTitle() != null) {
			mTitleValue = super.getTitle().toString();
		}
		if (super.getSummary() != null) {
			mSummaryValue = super.getSummary().toString();
		}

	}
	
	
	
	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		mPreferenceTitle = (TextView) view.findViewById(TITLE_ID);
		mPreferenceTitle.setText(mTitleValue);
		mPreferenceSummary = (TextView) view.findViewById(SUMMARY_ID);
		mPreferenceSummary.setText(mSummaryValue);
		mPreferenceImage = (ImageView) view.findViewById(IMAGE_ID);
		mHandler.removeMessages(UPDATE_CPU_STATE);
		mHandler.sendEmptyMessage(UPDATE_CPU_STATE);
		Log.e(TAG, "------------onBindViewHolder-----------");
		super.onBindViewHolder(view);
	}

	@Override
	public void setTitle(CharSequence title) {
		if (null == mPreferenceTitle) {
			mTitleValue = title;
		}
		if (!title.equals(mTitleValue)) {
			mTitleValue = title;
			mPreferenceTitle.setText(mTitleValue);
		}
	}

	@Override
	public CharSequence getTitle() {
		return mTitleValue;
	}

	/**
	 * get the preference summary
	 * 
	 * @return the preference summary
	 */
	public String getSummary() {
		return mSummaryValue;
	}

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        Log.d(TAG, "onPrepareForRemoval()--");
        mHandler.removeMessages(UPDATE_CPU_STATE);
    }



}
