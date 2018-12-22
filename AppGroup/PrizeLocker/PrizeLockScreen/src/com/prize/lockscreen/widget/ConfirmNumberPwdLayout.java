package com.prize.lockscreen.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.prize.lockscreen.interfaces.OnUnlockListener;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.prizelockscreen.R;

public class ConfirmNumberPwdLayout extends LinearLayout {

	public ConfirmNumberPwdLayout(Context context) {
		super(context);
		init();
	}

	public ConfirmNumberPwdLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ConfirmNumberPwdLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ConfirmNumberPwdLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private OnUnlockListener onUnlockListener;
	private boolean isConfirm;
	private NumberView nv1;
	private NumberView nv2;
	private NumberView nv3;
	private String numberPwd;

	public void setOnUnlockListener(OnUnlockListener onUnlockListener) {
		this.onUnlockListener = onUnlockListener;
	}

	public void setConfirm(boolean isConfirm) {
		this.isConfirm = isConfirm;
	}

	private void init() {
		numberPwd = SharedPreferencesTool.getNumberPassword(getContext());
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		nv1 = (NumberView) findViewById(R.id.number1);
		nv2 = (NumberView) findViewById(R.id.number2);
		nv3 = (NumberView) findViewById(R.id.number3);
		nv1.setOnClickListener(onClickListener);
		nv2.setOnClickListener(onClickListener);
		nv3.setOnClickListener(onClickListener);
	}

	View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String number = (String) v.getTag();
			numberPwd += number;
			System.out.println("numberPwd--->" + numberPwd);
			switch (v.getId()) {
			case R.id.number1:
				break;
			case R.id.number2:
				break;
			case R.id.number3:
				break;
			}
		}
	};

}
