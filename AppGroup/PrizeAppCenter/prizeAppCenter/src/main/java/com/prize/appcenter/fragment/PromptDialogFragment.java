package com.prize.appcenter.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * 删除提示框
 * */
public class PromptDialogFragment extends DialogFragment {
	private static final String TITLE = "title";
	private static final String CONTENT = "content";
	private static final String SURE = "sure";
	private static final String CACEL = "cancel";
	Activity mMainActivity = null;
	View.OnClickListener mListener = null;
	private Button add_neg;
	private Button sureBtn;

	/**
	 * 生成PromptDialogFragment的实例
	 * 
	 * @param title
	 *            提示对话框的标题
	 * @param positiveButtonClickListener
	 *            确认按钮的监听器
	 */
	public static PromptDialogFragment newInstance(String title,
			String content, String sure, String cancel,
			View.OnClickListener positiveButtonClickListener) {
		PromptDialogFragment f = new PromptDialogFragment();
		f.setOnClickListener(positiveButtonClickListener);
		Bundle args = new Bundle();
		args.putString(TITLE, title);
		args.putString(CONTENT, content);
		args.putString(SURE, sure);
		args.putString(CACEL, cancel);
		f.setArguments(args);
		return f;
	}

	public void setOnClickListener(View.OnClickListener l) {
		mListener = l;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String cancel = "";
		String title = "";
		String sure = "";
		String content = "";
		if (getArguments() != null) {
			title = getArguments().getString(TITLE);
			content = getArguments().getString(CONTENT);
			sure = getArguments().getString(SURE);
			cancel = getArguments().getString(CACEL);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_edit_name, null);
		add_neg = (Button) view.findViewById(R.id.add_neg);
		TextView content_tv = (TextView) view.findViewById(R.id.content_tv);
		TextView title_tv = (TextView) view.findViewById(R.id.title_tv);
		add_neg = (Button) view.findViewById(R.id.add_neg);
		sureBtn = (Button) view.findViewById(R.id.sure_Btn);
		if (!TextUtils.isEmpty(content)) {
			content_tv.setText(content);
		}
		if (!TextUtils.isEmpty(title)) {

			title_tv.setText(title);
		}
		if (!TextUtils.isEmpty(sure)) {
			sureBtn.setText(sure);

		}
		if (!TextUtils.isEmpty(cancel)) {
			add_neg.setText(cancel);

		}
		builder.setView(view);
		add_neg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PromptDialogFragment.this.dismissAllowingStateLoss();
				if (dismissCallBack != null) {
					dismissCallBack.oneKeyDialogDismiss();
				}
			}
		});

		sureBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				PromptDialogFragment.this.dismissAllowingStateLoss();
				if (mListener != null) {
					mListener.onClick(view);
				}
			}
		});
		return builder.create();

	}

	public interface DismissCallBack {
		void oneKeyDialogDismiss();
	}

	public DismissCallBack dismissCallBack;

	public void setDismissCallBack(DismissCallBack dismissCallBack) {
		this.dismissCallBack = dismissCallBack;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
