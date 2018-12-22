package com.pr.scuritycenter.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.base.BaseActivity;

public class TrafficCorrectionSetting extends BaseActivity implements
		OnClickListener {

	private TextView return_back;
	private EditText traffic_month_total;
	private TextView traffic_month_remain;
	private EditText traffic_month_clean;
	private TextView traffic_location;

	@Override
	protected void setContentView() {
		setContentView(R.layout.traffic_setting);
	}

	@Override
	protected void findViewById() {
		return_back = (TextView) findViewById(R.id.return_back);
		traffic_month_total = (EditText)findViewById(R.id.traffic_month_total);
		//traffic_month_remain = (TextView) findViewById(R.id.traffic_month_remain);
		traffic_month_clean = (EditText) findViewById(R.id.traffic_month_clean);
		traffic_location = (TextView) findViewById(R.id.traffic_location);
	}

	@Override
	protected void controll() {
		
		
		return_back.setOnClickListener(this);
		//traffic_month_remain.setOnClickListener(this);
		//traffic_month_clean.setOnClickListener(this);
		traffic_location.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.return_back:
			this.finish();
			break;

		/*case R.id.traffic_month_remain:
			break;*/
	/*	case R.id.traffic_month_clean:
			break;
		case R.id.traffic_location:
			break;*/

		}
	}
}
