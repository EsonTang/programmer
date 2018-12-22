package com.koobee.koobeecenter;

import java.lang.reflect.Field;

import com.koobee.koobeecenter02.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;



public class ShowFeedbackResultActivity  extends Activity{
	
	private View LeftView;
	private TextView showTextview;
	String qtype;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.feedback_status_color));
			// window.setNavigationBarColor(Color.TRANSPARENT);
		}
		
		setContentView(R.layout.suggestionresult);
		showTextview=(TextView)findViewById(R.id.problem_title2);
		LeftView=findViewById(R.id.leftback);
		LeftView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				FeedBackRequestActivity.feedbackRequestActivity.finish();
				ShowFeedbackResultActivity.this.finish();
			
				
			}
		});
		
		
	 qtype=	getIntent().getStringExtra("qtype");
	 initStatusBar();
	
	}

	
	
	private void initStatusBar() {
		Window window = getWindow();
		window.setStatusBarColor(getResources().getColor(R.color.color_fafafa));

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		try {
			Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
			Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
			Object gray = grayField.get(statusBarManagerClazz);
			Class windowManagerLpClazz = lp.getClass();
			Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
			statusBarInverseField.set(lp,gray);
			getWindow().setAttributes(lp);
		} catch (Exception e) {
		}
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(qtype.equals("8")){
			showTextview.setText(getResources().getString(R.string.advice_feedback));
		}
		
	}






	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	

}
