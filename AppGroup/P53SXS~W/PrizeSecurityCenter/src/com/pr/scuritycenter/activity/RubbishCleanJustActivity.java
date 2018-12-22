package com.pr.scuritycenter.activity;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.utils.StateBarUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class RubbishCleanJustActivity extends Activity{

	private LinearLayout viruses_scan_content;
	private ImageButton return_main;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarUtils.initSateBar(this);
		setContentView(R.layout.garbage_clean_just);
		viruses_scan_content = (LinearLayout) findViewById(R.id.viruses_scan_content);
		viruses_scan_content.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		return_main = (ImageButton) findViewById(R.id.return_main);
		return_main.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
