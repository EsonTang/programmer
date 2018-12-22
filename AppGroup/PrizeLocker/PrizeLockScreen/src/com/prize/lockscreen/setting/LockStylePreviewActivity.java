package com.prize.lockscreen.setting;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.StatusBarManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.utils.BitmapUtils;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.prizelockscreen.R;
/***
 * 锁屏样式预览
 * @author fanjunchen
 *
 */
public class LockStylePreviewActivity extends Activity implements OnClickListener {

	
	private StyleData mData = null;
	
	private int mStyleType;
	/**参数名称为 parcelable类型*/
	public static final String P_DATA = "p_data";
	
	private View topView, bottomView;
	
	private ImageView preview;
	
	private ProgressDialog progressDialog;
	
	private boolean isApplying = false;
	
	@Override
	protected void onCreate(Bundle instance) {
		super.onCreate(instance);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//不显示系统的标题栏          
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN );
		/*if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			int color = getResources().getColor(R.color.color_title);
			window.setStatusBarColor(color);
		}*/
		
		setContentView(R.layout.single_style_page);

		WindowManager.LayoutParams lp= getWindow().getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
        getWindow().setAttributes(lp);
        
		init();
	}

	private void init() {
		mData = (StyleData)getIntent().getParcelableExtra(P_DATA);
		
		topView = findViewById(R.id.top_lay);
		bottomView = findViewById(R.id.bottom_lay);
		
		preview = (ImageView) findViewById(R.id.img_preview);
		
		preview.setImageResource(mData.imgResId);
		
		TextView title = (TextView) findViewById(R.id.style_title);
		title.setText(mData.name);
		
		mStyleType = SharedPreferencesTool.getLockStyle(this);
		
		/*if (mStyleType == mData.styleType) {
			findViewById(R.id.style_apply).setEnabled(false);
		}*/
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_back:
				finish();
				break;
			case R.id.img_preview:
				if (topView.getVisibility() == View.VISIBLE) {
					topView.setVisibility(View.GONE);
					bottomView.setVisibility(View.GONE);
				}
				else
				{
					topView.setVisibility(View.VISIBLE);
					bottomView.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.style_apply:
				if (isApplying)
					return;
				isApplying = true;
				progressDialog = ProgressDialog.show(this, "", getString(R.string.applying), true, false);
				new SaveBgTask().execute(mData.bgImgResId);
				break;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		int t = SharedPreferencesTool.getLockStyle(this);
		if (mStyleType != t) {
			mStyleType = t;
		}
	}
	/**
	 * 保存图片到锁屏壁纸位置
	 * @author fanjunchen
	 *
	 */
	class SaveBgTask extends AsyncTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			if (null == params || params.length < 1)
				return false;
			
			boolean result = false;
			int resId = params[0];
			Bitmap bit = BitmapFactory.decodeResource(getResources(),
					resId);
			if (bit != null) {
				String path = LockScreenApplication.getPaperPath();
				result = BitmapUtils.compressImage(bit, new File(path), 100);
				bit.recycle();
				LockScreenApplication.forceGetBgImg();
			}
			
			bit = null;
			return result;
		}
		@Override
		public void onPostExecute(Boolean result) {
			isApplying = false;
			if (progressDialog != null)
				progressDialog.dismiss();
			if (result) {
				SharedPreferencesTool.setLockStyle(LockStylePreviewActivity.this, mData.styleType);
				Toast.makeText(LockStylePreviewActivity.this, R.string.applied, Toast.LENGTH_LONG).show();
				finish();
			}
			else 
				Toast.makeText(LockStylePreviewActivity.this, R.string.applied_fail, Toast.LENGTH_LONG).show();
		}
	}
}
