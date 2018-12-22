package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.app.net.datasource.base.DetailApp;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailInfoActivity;
import com.prize.appcenter.ui.util.PermissionUtils;

import java.util.HashMap;

/**
 * app其他信息，权限，开发者等等容器
 *
 * @author huanglingjun
 */
public class AppDetailView extends LinearLayout {
    private Context mContext;
    private TextView mDeveloper;
    private TextView mPermission;
    private TextView developer_id, permission_id;
    private TextView moreBtn;
    private DetailApp mData;
    private String UNKNOW;

    public AppDetailView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public AppDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = inflate(context, R.layout.app_detail_view, this);
        mDeveloper = (TextView) view.findViewById(R.id.developerTv_id);
        developer_id = (TextView) view.findViewById(R.id.developer_id);
        permission_id = (TextView) view.findViewById(R.id.permission_id);
        mPermission = (TextView) view.findViewById(R.id.permissionTv_id);
        moreBtn = (TextView) view.findViewById(R.id.moreTv_id);
        moreBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,
                        AppDetailInfoActivity.class);
                intent.putExtra("AppDetailData", mData);
                mContext.startActivity(intent);
            }
        });
        UNKNOW = mContext.getResources().getString(R.string.unknow);
    }

    public AppDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }


    public void setMoreColorDrawables(int color) {
        if (moreBtn != null) {
            moreBtn.setTextColor(color);
            Drawable[] drawables = moreBtn.getCompoundDrawables();
            Drawable d1 = drawables[2].mutate();//http://blog.51cto.com/6169621/1618580
            d1.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            moreBtn.setCompoundDrawables(null, null, d1, null);
        }
    }

    public void setTitleColor(int color) {
        if (permission_id != null) {
            permission_id.setTextColor(color);
        }
        if (developer_id != null) {
            developer_id.setTextColor(color);
        }
    }

    public void setDescripColor(int color) {
        if (mDeveloper != null) {
            mDeveloper.setTextColor(color);
        }
        if (mPermission != null) {
            mPermission.setTextColor(color);
        }
    }


    public void setData(DetailApp appData, boolean isDetail) {
        if (appData == null)
            return;
        this.mData = appData;

        if (!TextUtils.isEmpty(appData.developer)) {
            mDeveloper.setText(appData.developer);
        } else {
            mDeveloper.setText(UNKNOW);
        }

        if (isDetail) {
            mPermission.setMaxLines(2);
        } else {
            moreBtn.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(appData.appPermission)) {
            String[] permissions = appData.appPermission.split("\\;");
            HashMap<String, String> permissionMaps = PermissionUtils
                    .getPermissions();
            StringBuilder sb = new StringBuilder();
            for (String permission : permissions) {
                String pm = permissionMaps.get(permission);
                if (pm != null) {
                    sb.append(pm).append("\r\n");
                }
            }
            mPermission.setText(sb.toString());
        } else {
            moreBtn.setVisibility(View.GONE);
            findViewById(R.id.permission_id).setVisibility(View.GONE);

        }
        requestLayout();
    }

}
