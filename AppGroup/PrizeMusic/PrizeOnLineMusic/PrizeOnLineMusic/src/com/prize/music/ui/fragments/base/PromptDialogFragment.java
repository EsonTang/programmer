package com.prize.music.ui.fragments.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.prize.music.R;

/**
 * 删除提示框
 * */
public class PromptDialogFragment extends DialogFragment {
	private static final String TITLE = "title";
	Activity mMainActivity = null;
	View.OnClickListener mListener = null;
	private Button add_neg;
	private Button add_sure;

	public void setmListener(View.OnClickListener mListener) {
		this.mListener = mListener;
	}

	/**
	 * 生成PromptDialogFragment的实例
	 * 
	 * @param title
	 *            提示对话框的标题
	 * @param positiveButtonClickListener
	 *            确认按钮的监听器
	 */
	public static PromptDialogFragment newInstance(String title,
			View.OnClickListener positiveButtonClickListener) {
		PromptDialogFragment f = new PromptDialogFragment();
		Bundle args = new Bundle();
		args.putString(TITLE, title);
		f.setArguments(args);
		return f;
	}

	// public void setOnPositiveButtonClickedListener(
	// DialogInterface.OnClickListener positiveButtonClickListener) {
	// mListener = positiveButtonClickListener;
	// }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	String title = "";
	private TextView content_tv;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (getArguments() != null) {
			title = getArguments().getString(TITLE);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_edit_name, null);
		add_sure = (Button) view.findViewById(R.id.add_sure);
		add_neg = (Button) view.findViewById(R.id.add_neg);
		content_tv = (TextView) view.findViewById(R.id.content_tv);
		content_tv.setText(title);
		builder.setView(view);
		add_neg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PromptDialogFragment.this.dismissAllowingStateLoss();

			}
		});

		add_sure.setOnClickListener(mListener);
		return builder.create();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mListener = null;
	}

	public void setTitle(String title) {
		this.title = title;
		content_tv.setText(title);
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View view = inflater.inflate(R.layout.fragment_edit_name, container);
	// add_sure = (Button) view.findViewById(R.id.add_sure);
	// add_neg = (Button) view.findViewById(R.id.add_neg);
	// // add_neg.set
	// return view;
	// }
}
