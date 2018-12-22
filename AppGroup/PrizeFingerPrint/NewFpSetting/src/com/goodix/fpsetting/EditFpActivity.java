package com.goodix.fpsetting;

import com.goodix.util.CustomEditText;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class EditFpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();  
		String mSLFpsvcFPName =  bundle.getString("FpName");  
		setContentView(R.layout.operation_dialog);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.dialog_view);
		EditText contentEdit = (EditText)layout.findViewById(R.id.content_text_edit);
		contentEdit.setText(mSLFpsvcFPName);
		contentEdit.setSelection(mSLFpsvcFPName.length());
		contentEdit.addTextChangedListener(mFPTextWatcher);
		TextView deleteButton = (TextView)layout.findViewById(R.id.operation_button);
		deleteButton.setOnClickListener(mFPDeleteClickListener);
		
	}
	
	private TextWatcher mFPTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			String fingerName = s.toString().trim();
//			renameFPInfo(fingerName);
			
		}
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}
	};
	
	private OnClickListener mFPDeleteClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

		}
	};

}
