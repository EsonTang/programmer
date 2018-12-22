package com.prize.ui;

import com.android.camera.CameraActivity;
import com.android.camera.ui.RotateImageView;
import com.mediatek.camera.setting.SettingConstants;

import android.app.Activity;
import com.android.camera.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.camera.R;
public class SpotLight implements OnClickListener,OnSeekBarChangeListener{
	private static final String TAG = "SpotLight";
	private static final double SPOTLIGHT_MAXVALUE = 255;
	private static final int SPOTLIGHT_DIR_OFF = 0;
	private static final int SPOTLIGHT_DIR_LEFT = 1;
	private static final int SPOTLIGHT_DIR_LEFT_FRONT = 2;
	private static final int SPOTLIGHT_DIR_FRONT = 3;
	private static final int SPOTLIGHT_DIR_RIGHT_FRONT = 4;
	private static final int SPOTLIGHT_DIR_RIGHT = 5;
	private RelativeLayout mSpotLightLayout;
	private RotateImageView mSpotLightRotate;
	private TextView  mSpotLightDirText;
	private ImageView mSpotLightDirOffImg;
	private ImageView mSpotLightDirLeftImg;
	private ImageView mSpotLightDirLeftFrontImg;
	private ImageView mSpotLightDirFrontImg;
	private ImageView mSpotLightDirRightFrontImg;
	private ImageView mSpotLightDirRightImg;
	private LinearLayout mSpotlightSeekbarLayout;
	private SeekBar   mSpotLightStrenSeekBar;
	private TextView  mSpotLightStrenText;
	private double mStep;
	private int mSpotLightDir;
	private int mSpotLightStrenValue;
	private Activity mContext;
	
	public SpotLight(Activity context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	public void initView(ViewGroup viewGroup) {
		View view = mContext.getLayoutInflater().inflate(R.layout.facebeauty_spotlight, viewGroup);
		mStep = SPOTLIGHT_MAXVALUE/100;
		mSpotLightLayout = (RelativeLayout)view.findViewById(R.id.beautiful_spotlight_layout);
		mSpotLightRotate = (RotateImageView)view.findViewById(R.id.spotlight_dir_image);
		mSpotLightDirText = (TextView)view.findViewById(R.id.text_spotlight_dir);
		mSpotLightDirOffImg = (ImageView)view.findViewById(R.id.spotlight_off_img);
		mSpotLightDirLeftImg = (ImageView)view.findViewById(R.id.spotlight_left_img);
		mSpotLightDirLeftFrontImg = (ImageView)view.findViewById(R.id.spotlight_leftfront_img);
		mSpotLightDirFrontImg = (ImageView)view.findViewById(R.id.spotlight_front_img);
		mSpotLightDirRightFrontImg = (ImageView)view.findViewById(R.id.spotlight_rightfront_img);
		mSpotLightDirRightImg = (ImageView)view.findViewById(R.id.spotlight_right_img);
		mSpotlightSeekbarLayout = (LinearLayout)view.findViewById(R.id.spotlight_seekbar_layout);
		mSpotLightStrenSeekBar = (SeekBar)view.findViewById(R.id.beautifu_spotlight_seekbar);
		mSpotLightStrenText = (TextView)view.findViewById(R.id.spotlight_stren_text);
		
		mSpotLightRotate.setOnClickListener(this);
		mSpotLightDirOffImg.setOnClickListener(this);
		mSpotLightDirLeftFrontImg.setOnClickListener(this);
		mSpotLightDirLeftImg.setOnClickListener(this);
		mSpotLightDirFrontImg.setOnClickListener(this);
		mSpotLightDirRightFrontImg.setOnClickListener(this);
		mSpotLightDirRightImg.setOnClickListener(this);
		mSpotLightStrenSeekBar.setOnSeekBarChangeListener(this);
		 
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String spotlightDir = mContext.getString(R.string.spotlight_dir_title);
		switch(v.getId()){
		case R.id.spotlight_dir_image:
			 mSpotLightLayout.setVisibility(mSpotLightLayout.getVisibility() > View.VISIBLE ? View.VISIBLE:View.GONE);
			 break;
		case R.id.spotlight_off_img:
			 mSpotLightDir = SPOTLIGHT_DIR_OFF;
			 mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_off));
			 mSpotLightRotate.setImageResource(R.drawable.spotlight_off_normal);
			 closeSpotLight();
			 setUiHightlight(v);
			 break;
		case R.id.spotlight_left_img:
			 mSpotLightDir = SPOTLIGHT_DIR_LEFT;
			 mSpotLightRotate.setImageResource(R.drawable.spotlight_on);
			 mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_left));
			 setUiHightlight(v);
			 break;
		case R.id.spotlight_leftfront_img:
			 mSpotLightDir = SPOTLIGHT_DIR_LEFT_FRONT;
			 mSpotLightRotate.setImageResource(R.drawable.spotlight_on);
			 mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_leftfront));
			 setUiHightlight(v);
			 break;
		case R.id.spotlight_front_img:
			 mSpotLightDir = SPOTLIGHT_DIR_FRONT;
			 mSpotLightRotate.setImageResource(R.drawable.spotlight_on);
			 mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_front));
			 setUiHightlight(v);
			 break;
		case R.id.spotlight_rightfront_img:
			 mSpotLightDir = SPOTLIGHT_DIR_RIGHT_FRONT;
			 mSpotLightRotate.setImageResource(R.drawable.spotlight_on);
			 mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_rightfront));
			 setUiHightlight(v);
			 break;
		case R.id.spotlight_right_img:
			 mSpotLightDir = SPOTLIGHT_DIR_RIGHT;
			 mSpotLightRotate.setImageResource(R.drawable.spotlight_on);
			 mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_right));
			 setUiHightlight(v);
			 break;
		
		}
		
	}
	
	private void setUiHightlight(View v) {
		// TODO Auto-generated method stub
		if(v != null){
			clearSeletor();
			setSeletor(v);
		}
		if(v.getId() == R.id.spotlight_off_img) {
			mSpotlightSeekbarLayout.setVisibility(View.GONE);
		}else {
			mSpotlightSeekbarLayout.setVisibility(View.VISIBLE);
		}
		
		Log.d(TAG, "setUiHightlight mSpotLightDir:"+mSpotLightDir);
		((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SPOTLIGHT_DIR, String.valueOf(mSpotLightDir));
		((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SPOTLIGHT_DIR).setValue(String.valueOf(mSpotLightDir));
		((CameraActivity)mContext).applyParametersToServer();
	}
	
	private void setSeletor(View v) {
		// TODO Auto-generated method stub
		v.setSelected(true);
	}
	
	private void clearSeletor() {
		// TODO Auto-generated method stub
		mSpotLightDirOffImg.setSelected(false);
		mSpotLightDirLeftImg.setSelected(false);
		mSpotLightDirLeftFrontImg.setSelected(false);
		mSpotLightDirFrontImg.setSelected(false);
		mSpotLightDirRightFrontImg.setSelected(false);
		mSpotLightDirRightImg.setSelected(false);
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		if(progress == 0) {
			mSpotLightStrenText.setTextColor(mContext.getResources().getColor(R.color.white));
		}else {
			mSpotLightStrenText.setTextColor(mContext.getResources().getColor(R.color.main_color));
		}
		mSpotLightStrenText.setText(String.valueOf(progress));
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		int value = (int)(seekBar.getProgress()*mStep);
		Log.d(TAG, "onStopTrackingTouch value:"+value);
		((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SPOTLIGHT_LEVEL, String.valueOf(value));
		((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_3DLOOK_LEVEL, String.valueOf(value));
		((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SPOTLIGHT_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
		((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_3DLOOK_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
		((CameraActivity)mContext).applyParametersToServer();
	}
	
	public void closeSpotLight() {
		Log.d(TAG, "closeSpotLight");
		((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SPOTLIGHT_DIR, String.valueOf(0));
		((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SPOTLIGHT_LEVEL, String.valueOf(0));
		((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_3DLOOK_LEVEL, String.valueOf(0));
	}
	public void show() {
		// TODO Auto-generated method stub
		String spotlightDir = mContext.getString(R.string.spotlight_dir_title);
		if(((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SPOTLIGHT_DIR) != null){
			mSpotLightDir = Integer.valueOf(((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SPOTLIGHT_DIR).getValue());
			View hightView = null;
			switch (mSpotLightDir) {
			case SPOTLIGHT_DIR_OFF:
				hightView = mSpotLightDirOffImg;
				mSpotLightRotate.setImageResource(R.drawable.spotlight_off_normal);
				mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_off));
				closeSpotLight();
				break;
			case SPOTLIGHT_DIR_LEFT:
				hightView = mSpotLightDirLeftImg;
				mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_left));
				break;
			case SPOTLIGHT_DIR_LEFT_FRONT:
				hightView = mSpotLightDirLeftFrontImg;
				mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_leftfront));
				break;
			case SPOTLIGHT_DIR_FRONT:
				hightView = mSpotLightDirFrontImg;
				mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_front));
				break;
			case SPOTLIGHT_DIR_RIGHT_FRONT:
				hightView = mSpotLightDirRightFrontImg;
				mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_rightfront));
				break;
			case SPOTLIGHT_DIR_RIGHT:
				hightView = mSpotLightDirRightImg;
				mSpotLightDirText.setText(spotlightDir+mContext.getString(R.string.spotlight_dir_right));
				break;
			default:
			
				break;
			}
			setUiHightlight(hightView);
			
		}
		
		if(((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SPOTLIGHT_LEVEL) != null){
			int level = Integer.valueOf(((CameraActivity)mContext).getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SPOTLIGHT_LEVEL).getValue());
			mSpotLightStrenSeekBar.setProgress(level);
			mSpotLightStrenText.setText(String.valueOf(level));
			((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SPOTLIGHT_LEVEL, String.valueOf((int)(level*mStep)));
			((CameraActivity)mContext).getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_3DLOOK_LEVEL, String.valueOf((int)(level*mStep)));
			((CameraActivity)mContext).applyParametersToServer();
		}
	}
	
	public void setSpotLightVisible(int visible) {
		mSpotLightRotate.setVisibility(visible);
	}
}
