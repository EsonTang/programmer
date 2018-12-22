package com.pr.scuritycenter.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.pr.scuritycenter.R;

public class CustomDialog extends Dialog {
	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Context context) {
		super(context);
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context mContext;
		private String mTitleStr;
		private String mMsgStr;
		private String mPositiveStr;
		private String mNegativeStr;
		private View mContentView;

		private DialogInterface.OnClickListener mPositiveButtonClickListener,
				mNegativeButtonClickListener;

		public Builder(Context context) {
			this.mContext = context;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param mTitleStr
		 * @return
		 */
		public Builder setMessage(String message) {
			this.mMsgStr = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param mTitleStr
		 * @return
		 */
		public Builder setMessage(int message) {
			this.mMsgStr = (String) mContext.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(int title) {
			this.mTitleStr = (String) mContext.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.mTitleStr = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.mContentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.mPositiveStr = (String) mContext.getText(positiveButtonText);
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener) {
			this.mPositiveStr = positiveButtonText;
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.mNegativeStr = (String) mContext.getText(negativeButtonText);
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener) {
			this.mNegativeStr = negativeButtonText;
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public CustomDialog create() {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(mContext,
					R.style.common_custom_dialog);
			View layout = inflater.inflate(R.layout.deletestation, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.tv_title)).setText(mTitleStr);
			// set the confirm button
			if (mPositiveStr != null) {
				((Button) layout.findViewById(R.id.btn_right))
						.setText(mPositiveStr);

				((Button) layout.findViewById(R.id.btn_right))
						.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								if (mPositiveButtonClickListener != null) {
									mPositiveButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
								dialog.dismiss();
							}
						});
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.btn_right).setVisibility(View.GONE);
			}
			// set the cancel button
			if (mNegativeStr != null) {
				((Button) layout.findViewById(R.id.btn_left))
						.setText(mNegativeStr);
				((Button) layout.findViewById(R.id.btn_left))
						.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								if (mNegativeButtonClickListener != null) {
									mNegativeButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
								dialog.dismiss();
							}
						});

			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.btn_left).setVisibility(View.GONE);
			}
			// set the content message
			if (mMsgStr != null) {
				((TextView) layout.findViewById(R.id.tv_msg)).setText(mMsgStr);
			} /*
			 * else if (contentView != null) { // if no message set // add the
			 * contentView to the dialog body ((LinearLayout)
			 * layout.findViewById(R.id.content)) .removeAllViews();
			 * ((LinearLayout) layout.findViewById(R.id.content))
			 * .addView(contentView, new LayoutParams(
			 * LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); }
			 */
			dialog.setContentView(layout);
			WindowManager.LayoutParams params = dialog.getWindow()
					.getAttributes();
			params.width = (int) mContext.getResources().getDimension(
					R.dimen.custom_dialog_width);
			params.height = (int) mContext.getResources().getDimension(
					R.dimen.custom_dialog_height);
			dialog.getWindow().setAttributes(params);
			return dialog;
		}

	}

}
