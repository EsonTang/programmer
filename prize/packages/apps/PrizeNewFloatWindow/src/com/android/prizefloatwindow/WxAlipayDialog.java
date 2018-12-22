package com.android.prizefloatwindow;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WxAlipayDialog extends Dialog {
	private RelativeLayout dialog_layout_outside;
    private LinearLayout wx_layout,alipay_layout;//确定按钮
    private TextView wx_label,alipay_label;//消息标题文本
    private ImageView wx_download,alipay_download,wx_icon,alipay_icon;
    private String mWxLable,mAlipayLabel;//从外界设置的消息文本
    private boolean isWxinstall,isAlipayinstall;
    private Context mContext;
    private onWxLayoutOnclickListener wxOnclickListener;
    private onAlipayLayoutOnclickListener alipayOnclickListener;
    private onWxImgOnclickListener wxImgOnclickListener;
    private onAlipayImgOnclickListener alipayImgOnclickListener;


    public void setOnWxLayoutOnclickListener(String str, onWxLayoutOnclickListener wxOnclickListener) {
        if (str != null) {
        	mWxLable = str;
        }
        this.wxOnclickListener = wxOnclickListener;
    }


    public void setOnAlipayLayoutOnclickListener(String str, onAlipayLayoutOnclickListener alipayOnclickListener) {
        if (str != null) {
        	mAlipayLabel = str;
        }
        this.alipayOnclickListener = alipayOnclickListener;
    }
    public void setOnWxImgOnclickListener(boolean isWxinstall,onWxImgOnclickListener wxImgOnclickListener) {
        this.wxImgOnclickListener = wxImgOnclickListener;
        this.isWxinstall = isWxinstall;
    }


    public void setOnAlipayImgOnclickListener(boolean isAlipayinstall,onAlipayImgOnclickListener alipayImgOnclickListener) {
        this.alipayImgOnclickListener = alipayImgOnclickListener;
        this.isAlipayinstall = isAlipayinstall;
    }
    public WxAlipayDialog(Context context) {
        super(context, R.style.MyDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wxalipaydialog);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.CENTER);
        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
    	Log.d("snail_Runtime", "----------initEvent-----");
    	dialog_layout_outside.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("snail_", "----------dialog_layout_outside-----------------");
				dismiss();
			}
		});
        //设置确定按钮被点击后，向外界提供监听
    	wx_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wxOnclickListener != null) {
                	wxOnclickListener.onWxLayoutClick();
                }
            } 
        });
        //设置取消按钮被点击后，向外界提供监听
    	alipay_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alipayOnclickListener != null) {
                	alipayOnclickListener.onAlipayLayoutClick();
                }
            }
        });
    	wx_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wxImgOnclickListener != null) {
                	wxImgOnclickListener.onWxImgClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
    	alipay_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alipayImgOnclickListener != null) {
                	alipayImgOnclickListener.onAlipayImgClick();
                }
            }
        });
    }

    public interface onWxLayoutOnclickListener {
        public void onWxLayoutClick();
    }
    public interface onAlipayLayoutOnclickListener {
        public void onAlipayLayoutClick();
    }
    public interface onWxImgOnclickListener {
        public void onWxImgClick();
    }
    public interface onAlipayImgOnclickListener {
        public void onAlipayImgClick();
    }
    
    private void initView() {
    	Log.d("snail_Runtime", "----------initView-----");
    	dialog_layout_outside = (RelativeLayout) findViewById(R.id.dialog_layout_outside);
    	wx_layout = (LinearLayout) findViewById(R.id.wx_layout);
    	alipay_layout = (LinearLayout) findViewById(R.id.alipay_layout);
    	wx_label = (TextView) findViewById(R.id.wx_label);
    	alipay_label = (TextView) findViewById(R.id.alipay_label);
    	wx_download = (ImageView) findViewById(R.id.wx_download);
    	alipay_download = (ImageView) findViewById(R.id.alipay_download);
    	wx_icon = (ImageView) findViewById(R.id.wx_icon);
    	alipay_icon = (ImageView) findViewById(R.id.alipay_icon);
    }
    private void initData() {
    	Log.d("snail_Runtime", "----------initData-----");
        if(!TextUtils.isEmpty(mWxLable)){
        	wx_label.setText(mWxLable);
        }
        if(!TextUtils.isEmpty(mAlipayLabel)){
        	alipay_label.setText(mAlipayLabel);
        }
        if(isWxinstall){
        	wx_icon.setBackground(getContext().getResources().getDrawable(R.drawable.wechat));
        	wx_download.setVisibility(View.GONE);
        }else {
        	wx_icon.setBackground(getContext().getResources().getDrawable(R.drawable.wechat_uninstall));
        	wx_download.setVisibility(View.VISIBLE);
		}
        if(isAlipayinstall){
        	alipay_icon.setBackground(getContext().getResources().getDrawable(R.drawable.alipay));
        	alipay_download.setVisibility(View.GONE);
        }else {
        	alipay_icon.setBackground(getContext().getResources().getDrawable(R.drawable.alipay_uninstall));
        	alipay_download.setVisibility(View.VISIBLE);
		}
    }
}
