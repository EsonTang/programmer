package com.prize.left.page.activity;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.cloud.activity.MainActivityCloud;
import com.prize.cloud.widgets.CircleImageView;
import com.prize.left.page.bean.AppInfoBean;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.model.IResponse;
import com.prize.left.page.model.LeftMenuModel;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.response.UpgradeResponse;
import com.prize.left.page.ui.UpdateSelfDialog;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.ToastUtils;

/**
 * 菜单对话框
 * @author fanjunchen
 */
public class LeftMenuDialog extends AlertDialog implements OnClickListener, LauncherApplication.IPersonChange {
	
	private Context ctx;
	
	private PersonTable person = null;
	
	private TextView txtName = null, txtAccount = null;
	
	private CircleImageView imgHead;
	
	private View txtNew;
	
	// private View btnLogin;
	/**业务实例*/
	private LeftMenuModel mModel;
	
	private UpdateSelfDialog mUpdateSelfDialog; 
	
	private AppInfoBean bean;
	
	/**图片配置器*/
	private ImageOptions imgOption = null;
	
	public LeftMenuDialog(Context context) {
		this(context, 0);
	}
	
	public LeftMenuDialog(Context context, int themeId) {
		super(context, themeId);
		ctx = context;
		mModel = new LeftMenuModel(ctx);
		mModel.setIResponse(irs);
		
		int px = ctx.getResources().getDimensionPixelSize(R.dimen.dp_64);
		
		imgOption = new ImageOptions.Builder()
    	.setSize(px, px)
    	.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
    	.setFailureDrawableId(R.drawable.ico_person_head)
    	.setLoadingDrawableId(R.drawable.ico_person_head)
    	.build();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 这句代码换掉dialog默认背景，否则dialog的边缘发虚透明而且很宽
		// 总之达不到想要的效果
		// getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		setContentView(R.layout.left_menu_dialog);
		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		
		initView();
	}
	
	private void initView() {
		
		/*btnLogin = findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(this);*/
		
		imgHead = (CircleImageView)findViewById(R.id.head_img);
		
		txtName = (TextView)findViewById(R.id.txt_person_name);
		
		txtAccount = (TextView)findViewById(R.id.txt_person_account);
		
		findViewById(R.id.txt_manager_card).setOnClickListener(this);
		findViewById(R.id.txt_addr_set).setOnClickListener(this);
		findViewById(R.id.lay_version_check).setOnClickListener(this);
		TextView version = (TextView) findViewById(R.id.txt_version_check);
		
		
		version.setText(Html.fromHtml(toLocVersion()));
		findViewById(R.id.txt_feedback).setOnClickListener(this);
		imgHead.setOnClickListener(this);
		
		txtNew = findViewById(R.id.txt_new);
		
		if (LauncherApplication.getInstance() != null) {
			LauncherApplication.getInstance().setPersonChange(this);
			if (LeftModel.getInstance()!=null&&LeftModel.getInstance().isNeedUpdate()) {
				txtNew.setVisibility(View.VISIBLE);

				version.setText(Html.fromHtml(toNewVersion()));
			}
			else {
				txtNew.setVisibility(View.GONE);
				version.setText(Html.fromHtml(toLocVersion()));
			}
		}
		setPerson();
	}
	
	public String toNewVersion() {
		StringBuffer str = new StringBuffer();
		 String source = "<font color='#989797' size='1'>"; 
		str.append(getContext().getString(R.string.str_version_check));
		str.append(source);
		str.append("(");
		str.append( ClientInfo.getInstance(ctx).appVersionCode);
		str.append("-->");
		str.append( ClientInfo.getInstance(ctx).newVersionCode);
		str.append(")");
		str.append("</font>");
		return str.toString();
		
	}
	
	
	public String toLocVersion() {
		StringBuffer str = new StringBuffer();
		 String source = "<font color='#989797' size='1'>";  
		str.append(getContext().getString(R.string.str_version_check));
		str.append(source);
		str.append("(");
		str.append(getContext().getString(R.string.str_local_version));
		str.append( ClientInfo.getInstance(ctx).appVersionCode);
		str.append(")");
		str.append("</font>");
		return str.toString();
		
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		if (txtNew != null) {
			if (LeftModel.getInstance() != null&&LeftModel.getInstance().isNeedUpdate())
				txtNew.setVisibility(View.VISIBLE);
			else
				txtNew.setVisibility(View.GONE);
		}
		super.show();
	}
	/***
	 * 设置个人信息
	 */
	private void setPerson() {
		
		if (LauncherApplication.getInstance() != null) {
			person = LauncherApplication.getInstance().getLoginPerson();
		}
		
		if (person != null) {
			if (!TextUtils.isEmpty(person.realName)) {
				txtName.setText(person.realName);
				txtName.setVisibility(View.VISIBLE);
			} 
			else {
				txtName.setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(person.userId)) {
				txtAccount.setText(person.userId);
				txtAccount.setVisibility(View.VISIBLE);
			}
			
			// btnLogin.setVisibility(View.GONE);
			
			if (!TextUtils.isEmpty(person.avatar))
				x.image().bind(imgHead, person.avatar, imgOption);
			else
				imgHead.setImageResource(R.drawable.ico_person_head);
		}
		else {
			// btnLogin.setVisibility(View.VISIBLE);
			// txtName.setVisibility(View.VISIBLE);
			txtName.setText(R.string.str_left_account);
			txtAccount.setVisibility(View.VISIBLE);
			txtAccount.setText(R.string.str_left_no_login);
			imgHead.setImageResource(R.drawable.ico_person_head);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.txt_manager_card:
				Intent it = new Intent(ctx, ManagerCardActivity.class);
				ctx.startActivity(it);
				it = null;
				dismiss();
				break;
			case R.id.txt_feedback:
				it = new Intent(ctx, FeedbackActivity.class);
				ctx.startActivity(it);
				it = null;
				dismiss();
				break;
			case R.id.txt_addr_set:
				it = new Intent(ctx, NormalAddrActivity.class);
				ctx.startActivity(it);
				it = null;
				dismiss();
				break;
			case R.id.lay_version_check:
			/*	if (LeftModel.getInstance() != null&&LeftModel.getInstance().isNeedUpdate()) {
					bean = LeftModel.getInstance().getUpgradApp();
					displayDialog();
				}
				else {
					mModel.doCheckUpdate();
				}*/
				break;
			case R.id.txt_no_img:
				break;
			// case R.id.btn_login:
			case R.id.head_img:
				it = new Intent(ctx, MainActivityCloud.class);
				ctx.startActivity(it);
				it = null;
				break;
		}
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		dismiss();
		return super.onPrepareOptionsMenu(menu);
	}
	private IResponse<UpgradeResponse> irs = new IResponse<UpgradeResponse>(){
		@Override
		public void onResponse(UpgradeResponse resp) {
			// TODO Auto-generated method stub
			if (resp.code == 0) {

				boolean isupdate=false;
				if(resp.data != null && resp.data != null 
						&& resp.data.app != null) {

					int localVerCode = ClientInfo.getInstance(ctx).appVersion;
					 isupdate =resp.data.app.versioncode > localVerCode;
				}
				if(resp.data != null && resp.data != null 
						&& resp.data.app != null&&isupdate) {
					bean = resp.data.app;
					displayDialog();
				}
				else {
					ToastUtils.showToast(ctx, R.string.str_no_update);
				}
			}
			else if (resp.code == 2) {
				ToastUtils.showToast(ctx, R.string.str_no_update);
			}
			else {
				ToastUtils.showToast(ctx, R.string.str_no_update);
			}
		}
		
	};
	@Override
	public void onPersonChange() {
		// TODO Auto-generated method stub
		setPerson();
	}
	
	private void displayDialog() {
		if (bean == null)
			return;
		if (mUpdateSelfDialog == null) {
			/*mUpdateSelfDialog = new UpdateSelfDialog(ctx,
					R.style.add_dialog,
					ClientInfo.getInstance(ctx).appVersionCode, 
					ctx.getResources().getString(R.string.new_version_name,	bean.versionName),
									bean.updateInfo);*/
			mUpdateSelfDialog = UpdateSelfDialog.getInstance(ctx,
					R.style.add_dialog,
					ClientInfo.getInstance(ctx).appVersionCode, 
					ctx.getResources().getString(R.string.new_version_name,	bean.versionname),
									bean.updateinfo);
			mUpdateSelfDialog.setBean(bean);
		}
		if (mUpdateSelfDialog != null && !mUpdateSelfDialog.isShowing()) {
			mUpdateSelfDialog.show();
		}
	}
}
