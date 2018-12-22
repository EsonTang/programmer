/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
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
package com.prize.appcenter.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.app.beans.Person;
import com.prize.app.net.datasource.base.AppDetailData;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.AppDetailParentFgm;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 类描述：填写评价信息的dialog
 * 
 * @author huanglingjun
 * @version 版本
 */
public class SubmitCommentDialog extends Dialog implements
		android.view.View.OnClickListener {
	public EditText mEdit;
	private Button mCancel;
	private Button mSure;
	private Context context;
	private RatingBar mRatingBar;
	private TextView mPhrase;
	private AppDetailParentFgm mDetailParentFgm;
	private Person mPerson;
	private AppDetailData mDetailData;
	public SubmitCommentCallBack submitCommentCallBack;

	private static final int CANCEL = 0;
	private static final int SURE = 1;

	public SubmitCommentDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		// 必须在setcontentView之前不然软键盘弹出被对话框遮挡
		/*
		 * View view = LayoutInflater.from(context).inflate(
		 * R.layout.dialog_submit_comment, null); setView(view, 0, 0, 0, 0);
		 */
	}

	public interface SubmitCommentCallBack {
		void submitCommentClick(String commentContent, float rating);
	};

	public void setData(AppDetailParentFgm detailParentFgm, Person person,
			AppDetailData detailData) {
		mDetailParentFgm = detailParentFgm;
		mPerson = person;
		mDetailData = detailData;
	}

	public void refreshPerson(Person person) {
		mPerson = person;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_submit_comment);
		mSure = (Button) findViewById(R.id.sure_id);
		mCancel = (Button) findViewById(R.id.cancel_id);
		mEdit = (EditText) findViewById(R.id.edittext_id);
		InputFilter emojiFilter = UIUtils.getEmojiFilter();
		mEdit.setFilters(new InputFilter[] { emojiFilter });
		mRatingBar = (RatingBar) findViewById(R.id.ratingBar_id);
		mPhrase = (TextView) findViewById(R.id.phrase_id);

		mSure.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mRatingBar.setOnRatingBarChangeListener(new RatingBarListener());
	}

	private class RatingBarListener implements
			RatingBar.OnRatingBarChangeListener {

		public void onRatingChanged(RatingBar ratingBar, float rating,
				boolean fromUser) {
			mRatingBar.setRating(rating);
			changePhrase(rating);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.sure_id:
			String commentContent = mEdit.getText().toString().trim();
			if (TextUtils.isEmpty(commentContent)) {
				ToastUtils.showToast(context.getString(R.string.no_comment),
						Toast.LENGTH_SHORT);
			} else if (commentContent.length() > 140) {
				ToastUtils.showToast(context.getString(R.string.comment_limit),
						Toast.LENGTH_SHORT);
			} else if (mRatingBar.getRating() <= 0) {
				ToastUtils.showToast(context.getString(R.string.no_scrol),
						Toast.LENGTH_SHORT);
			} else {
				if (submitCommentCallBack != null) {
					submitCommentCallBack.submitCommentClick(commentContent,
							mRatingBar.getRating());
				}
				// doCommentPost(commentContent,mRatingBar.getRating());
				this.dismiss();
			}
			break;

		case R.id.cancel_id:
			// submitCommentInfo.onClick(CANCEL, null, 0);
			this.dismiss();
			break;

		default:
			break;
		}

	}

	public void setCommentCallBack(SubmitCommentCallBack submitCommentCallBack) {
		this.submitCommentCallBack = submitCommentCallBack;
	}

	// private void doCommentPost(String commentContent,float rating) {
	// if (mPerson != null) {
	// mDetailParentFgm.doCommentRequest(mDetailData.app.id,
	// mDetailData.app.versionName, rating,
	// commentContent,
	// ClientInfo.getInstance().brand,
	// Integer.parseInt(mPerson.getUserId()),
	// mPerson.getRealName(), mPerson.getAvatar());
	// } else {
	// mDetailParentFgm.doCommentRequest(mDetailData.app.id,
	// mDetailData.app.versionName, rating,
	// commentContent,
	// ClientInfo.getInstance().brand, 0, "", "");
	// }
	// }

	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		mEdit.requestFocus();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) context
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300);
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		InputMethodManager inputmanger = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputmanger.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
		super.dismiss();
	}

	private void changePhrase(float rating) {
		if (rating <= 1) {
			if (rating == 1) {
				mPhrase.setText(context.getString(R.string.give)
						+ ((int) rating)
						+ context.getString(R.string.one_score));
			} else {
				mPhrase.setText(context.getString(R.string.give) + rating
						+ context.getString(R.string.one_score));
			}
			return;
		} else if (rating <= 2) {
			if (rating == 2) {
				mPhrase.setText(context.getString(R.string.give)
						+ ((int) rating)
						+ context.getString(R.string.two_score));
			} else {
				mPhrase.setText(context.getString(R.string.give) + rating
						+ context.getString(R.string.two_score));
			}
			return;
		} else if (rating <= 3) {
			if (rating == 3) {
				mPhrase.setText(context.getString(R.string.give)
						+ ((int) rating)
						+ context.getString(R.string.three_score));
			} else {
				mPhrase.setText(context.getString(R.string.give) + rating
						+ context.getString(R.string.three_score));
			}
			return;
		} else if (rating <= 4) {
			if (rating == 4) {
				mPhrase.setText(context.getString(R.string.give)
						+ ((int) rating)
						+ context.getString(R.string.four_score));
			} else {
				mPhrase.setText(context.getString(R.string.give) + rating
						+ context.getString(R.string.four_score));
			}

			return;
		} else if (rating <= 5) {
			if (rating == 5) {
				mPhrase.setText(context.getString(R.string.give)
						+ ((int) rating)
						+ context.getString(R.string.five_score));
			} else {
				mPhrase.setText(context.getString(R.string.give) + rating
						+ context.getString(R.string.five_score));
			}
		}
	}
}
