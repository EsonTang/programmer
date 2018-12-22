/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.appcenter.R;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.BaseShareContent;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.RenrenShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分享对话框
 * @author fanjunchen
 */
public class WebShareDialog extends AlertDialog implements OnClickListener {

	private GridView grideView;
	
	private int[] shareIcon;
	
	private int[] moreIcon;
	
	private int[] shareStr;
	
	private int[] moreStr;
	
	private String shareUrl = "";
	
	private List<ShareBean> list;
	ShareButtonAdapter mShareButtonAdapter;
	private UMSocialService mController;
	private Context ctx;
	private boolean isVisible = false;
	
	private IReloadUrl listener = null;

	protected WebShareDialog(Context context) {
		super(context);
		ctx = context;
	}
	
	public void setListener(IReloadUrl l) {
		listener = l;
	}
	
	public void setUrl(String l) {
		shareUrl = l;
	}
	
	public WebShareDialog(Context context, int themeId) {

		super(context, themeId);
		ctx = context;
		mController = UMServiceFactory.getUMSocialService("com.umeng.share");

		shareIcon = new int[] { R.drawable.umeng_socialize_qq,
				R.drawable.umeng_socialize_weixin,//R.drawable.umeng_socialize_yixin,
				R.drawable.umeng_socialize_wxcircle,//R.drawable.umeng_socialize_sina_on,
				R.drawable.umeng_socialize_more };
		moreIcon = new int[] { 
				R.drawable.umeng_socialize_sina_on,
				R.drawable.umeng_socialize_renren,
				R.drawable.umeng_socialize_tencent_on };
		shareStr = new int[] { R.string.QQ, 
				R.string.weixin,
				R.string.QQ_circle, 
				R.string.more, };
		moreStr = new int[] {R.string.sina_weibo,
				R.string.renren, R.string.QQ_weibo };
		list = new ArrayList<ShareBean>();
		for (int i = 0; i < 4; i++) {
			list.add(new ShareBean(shareIcon[i], shareStr[i]));
		}
		mShareButtonAdapter = new ShareButtonAdapter(ctx, list);

	}

	/***
	 * 设置分享的URL
	 * @param url
	 */
	public void setSharedUrl(String url) {
		shareUrl = url;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 这句代码换掉dialog默认背景，否则dialog的边缘发虚透明而且很宽
		// 总之达不到想要的效果
		// getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		setContentView(R.layout.web_share_dialog);
		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		grideView = (GridView) findViewById(R.id.grideView);
		findViewById(R.id.cancel_share).setOnClickListener(this);
		
		findViewById(R.id.txt_reload).setOnClickListener(this);
		findViewById(R.id.txt_open_in_browse).setOnClickListener(this);
		findViewById(R.id.txt_copy).setOnClickListener(this);
		
		grideView.setAdapter(mShareButtonAdapter);
		initSocialSDK();
		grideView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < 3) {
					SHARE_MEDIA[] share = new SHARE_MEDIA[] { SHARE_MEDIA.QQ,
							SHARE_MEDIA.WEIXIN, 
							SHARE_MEDIA.WEIXIN_CIRCLE
							};

					isShareSuccess(share[position]);
					dismiss();
				} else if (position == 3) {
					if (!isVisible) {
						isVisible = !isVisible;

						for (int i = 0; i < moreIcon.length; i++) {
							list.add(new ShareBean(moreIcon[i], moreStr[i]));
						}

						list.set(3, new ShareBean(
								R.drawable.umeng_socialize_qzone_on,
								R.string.QQ_zone));
						mShareButtonAdapter.notifyDataSetChanged();
					} else {
						isShareSuccess(SHARE_MEDIA.QZONE);
						dismiss();
					}
				} else if (position > 3) {
					SHARE_MEDIA[] shareMore = new SHARE_MEDIA[] {
						    SHARE_MEDIA.SINA,
							SHARE_MEDIA.RENREN, SHARE_MEDIA.TENCENT };

					isShareSuccess(shareMore[position - 4]);
					dismiss();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cancel_share:
				dismiss();
				break;
			case R.id.txt_reload:
				dismiss();
				if(listener != null)
					listener.onClickReload(shareUrl);
				break;
			case R.id.txt_open_in_browse:
				openBrowser();
				dismiss();
				break;
			case R.id.txt_copy:
				ClipboardManager cmb = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE); 
				ClipData data = ClipData.newPlainText(shareUrl, shareUrl);
				cmb.setPrimaryClip(data);
				dismiss();
				break;
		}
	}
	
	private void openBrowser() {
		Uri uri = Uri.parse(shareUrl);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		ctx.startActivity(intent);
	}

	class ShareButtonAdapter extends BaseAdapter {
		List<ShareBean> list;
		private Context context;

		public ShareButtonAdapter(Context context, List<ShareBean> list) {
			super();
			this.list = list;
			this.context = context;
		}

		@Override
		public int getCount() {

			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {

			return list.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(this.context).inflate(
						R.layout.share_item_layout, null);
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.icon);
				viewHolder.share_Tv = (TextView) convertView
						.findViewById(R.id.share_Tv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			ShareBean bean = (ShareBean) getItem(position);
			viewHolder.icon.setImageResource(bean.srcId);
			viewHolder.share_Tv.setText(this.context.getText(bean.strId));
			return convertView;
		}
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView share_Tv;
	}

	static class ShareBean implements Serializable {

		public ShareBean(int srcId, int strId) {
			super();
			this.srcId = srcId;
			this.strId = strId;
		}

		/** 用一句话描述这个变量表示什么 */
		private static final long serialVersionUID = 1L;
		public int srcId;
		public int strId;
	}

	private void initSocialSDK() {
		Context mContext = getContext();

		// wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
		String appId = "wx94fa8fc77a206f68";
		String appSecret = "d4624c36b6795d1d99dcf0547af5443d";
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mContext, appId, appSecret);
		wxHandler.addToSocialSDK();
		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mContext, appId,
				appSecret);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();

		// 设置微信好友分享内容
		WeiXinShareContent weixinContent = new WeiXinShareContent();
		setShareContent(weixinContent);
		// 设置微信朋友圈分享内容
		CircleShareContent circleMedia = new CircleShareContent();
		setShareContent(circleMedia);

		// 添加QQ平台
		UMQQSsoHandler qqHandler = new UMQQSsoHandler((Activity) ctx,
				"1104853719", "kC2RHki6G1WaxnJI");
		qqHandler.addToSocialSDK();
		QQShareContent qqShareContent = new QQShareContent();
		setShareContent(qqShareContent);

		// 添加QQ空间平台
		QZoneSsoHandler qzoneHandler = new QZoneSsoHandler((Activity) ctx,
				"1104853719", "kC2RHki6G1WaxnJI");
		qzoneHandler.addToSocialSDK();
		QZoneShareContent qzone = new QZoneShareContent();
		setShareContent(qzone);

		/*// 添加易信平台,参数1为当前activity, 参数2为在易信开放平台申请到的app id
		UMYXHandler yixinHandler = new UMYXHandler(ctx,
				"yxe6c443e88866454798d2721ae8704a57");
		// 关闭分享时的等待Dialog
		yixinHandler.enableLoadingDialog(false);
		// 把易信添加到SDK中
		yixinHandler.addToSocialSDK();
		YiXinShareContent yiXinShareContent = new YiXinShareContent();
		setShareContent(yiXinShareContent);*/
		
		// 新浪微博分享内容
		SinaShareContent sinaShareContent = new SinaShareContent();
		setShareContent(sinaShareContent);

		// 腾讯微博分享内容
		TencentWbShareContent tencentWbShareContent = new TencentWbShareContent();
		setShareContent(tencentWbShareContent);
		// RENREN
		RenrenShareContent renrenShareContent = new RenrenShareContent();
		setShareContent(renrenShareContent);
		mController.setAppWebSite(SHARE_MEDIA.RENREN, shareUrl);

		// 设置文字分享内容
		mController.setShareContent((mContext.getString(R.string.app_name)));
		// 图片分享内容
		mController
				.setShareMedia(new UMImage(mContext, R.drawable.ic_applauncher));
	}

	// 单独设置分享内容
	private void setShareContent(BaseShareContent shareContent) {
		// 设置分享文字
		shareContent.setShareContent(ctx.getString(R.string.share_content)
				+ shareUrl);
		// 设置title
		shareContent.setTitle(ctx.getString(R.string.app_name));
		// 设置分享内容跳转URL
		shareContent.setTargetUrl(shareUrl);
		mController.setShareMedia(shareContent);
	}

	// 判断分享是否成功
	private void isShareSuccess(SHARE_MEDIA share_media) {
		mController.getConfig().setDefaultShareLocation(false);
		mController.getConfig().closeToast();
		mController.postShare(ctx, share_media,
				new SocializeListeners.SnsPostListener() {

					@Override
					public void onComplete(SHARE_MEDIA arg0, int eCode,
							SocializeEntity arg2) {
						if (eCode == 200) {
							Toast.makeText(ctx, "分享成功.", Toast.LENGTH_SHORT)
									.show();
						} else {
							String eMsg = "";
							if (eCode == -101) {
								eMsg = "没有授权";
							}
							if (eCode == 40002) {
								Toast.makeText(ctx,
										ctx.getString(R.string.pls_install_qq),
										Toast.LENGTH_SHORT).show();
								return ;
							}
							if (eCode != 40000)
								Toast.makeText(ctx,
										"分享失败[" + eCode + "] " + eMsg,
										Toast.LENGTH_SHORT).show();
						}

					}

					@Override
					public void onStart() {

					}
				});
	}

	/***
	 * 重新加载接口
	 * @author Administrator
	 *
	 */
	public interface IReloadUrl {
		void onClickReload(String url);
	}
}
