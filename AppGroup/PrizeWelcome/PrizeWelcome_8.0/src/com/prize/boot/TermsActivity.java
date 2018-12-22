/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年8月3日
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
package com.prize.boot;

import java.io.File;

import com.prize.boot.util.Utils;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

//import com.lidroid.xutils.ViewUtils;
//import com.lidroid.xutils.view.annotation.event.OnClick;
//import com.prize.cloud.helper.PrizeAccount;
//import com.prize.cloud.helper.PrizeHelper;

/**
 * 使用条款
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class TermsActivity extends AbstractGuideActivity {

	protected static final String TAG = "TermsActivity";
	private static final String FILE_PATH = "file:///android_asset/html/statement.html";
	private static final String fileString="html/statement.html";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_terms);
//		ViewUtils.inject(this);
		setGuideTitle(R.drawable.terms_icon, R.string.termsOfuse);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.unAgree_btn) {
//			finish();
			nextStep(false);
		} else if (v.getId() == R.id.next_btn) {
			/*PrizeAccount account = PrizeHelper.curAccount(this);
			if (account != null)*/
				/*startActivity(new Intent(this, OtherSetActivity.class));
				finish();*/
			/*else
				startActivity(new Intent(this, BootUpAccountActivity.class));*/
			nextStep(true);
		} else if (v.getId() == R.id.toTerms_text) {
			toTermsClk();
		} else if (v.getId() == R.id.im_back) {
//			finish();
			nextStep(false);
		}
	}
	
	public void toTermsClk() {
//		Intent intent = new Intent(this, WebviewActivity.class);
//		startActivity(intent);
//		overridePendingTransition(R.anim.activity_open_in_anim, R.anim.activity_close_out_anim);
		/*final File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return;
        }*/
		File file = new File(fileString); 
		Uri contentUri = FileProvider.getUriForFile(this, "com.prize.htmlviewer", file);
		Log.d("hekeyi","contentUri = "+contentUri);
		
//		final Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse(FILE_PATH), "text/html");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(contentUri, "text/html");
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.agreement_bar_title));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage("com.prize.htmlviewer");
        try {
            startActivity(intent);
//            overridePendingTransition(R.anim.activity_open_in_anim, R.anim.activity_close_out_anim);
            finish();
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to find viewer", e);
        }
	}
	
}
