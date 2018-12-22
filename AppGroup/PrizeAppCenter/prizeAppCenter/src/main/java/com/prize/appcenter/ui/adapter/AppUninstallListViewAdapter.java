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

package com.prize.appcenter.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.threads.SingleThreadExecutor;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.PackageUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppUninstallActivity;
import com.prize.appcenter.bean.AppInfo;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.widget.PrizeCommButton;

import java.util.List;

/**
 * 类描述：
 *
 * @author huangchangguo
 * @version 版本1.7
 */
public class AppUninstallListViewAdapter extends BaseAdapter {

    private AppUninstallActivity mActivity;
    private List<AppInfo> userInfos;
//	private List<AppInfo> systemInfos;

    public AppUninstallListViewAdapter(AppUninstallActivity mCtx,
                                       List<AppInfo> userInfos) {
        super();
        this.mActivity = mCtx;
        this.userInfos = userInfos;

    }

    @Override
    public int getCount() {
        int count = 0;
        if (userInfos != null) {
            count += userInfos.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (userInfos.size() <= 0)
            return convertView;
        ViewHolder viewHolder = null;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.item_app_uninstall_list, null);

            convertView.setTag(viewHolder);
            // 不用硬件加速
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            viewHolder.mIcon = (ImageView) convertView
                    .findViewById(R.id.app_uninstall_iv);
            viewHolder.mName = (TextView) convertView
                    .findViewById(R.id.app_uninstall_name_tv);
            viewHolder.mSize = (TextView) convertView
                    .findViewById(R.id.app_uninstall_size_tv);

            viewHolder.mUninstall = (PrizeCommButton) convertView
                    .findViewById(R.id.app_uninstall_btn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        getUninstallView(viewHolder, position);

        final ViewHolder viewHolderbtn = viewHolder;
        AppInfo appInfo = userInfos.get(position);
        final String pakageName = appInfo.mPackageName;
        final boolean isUninstalling = appInfo.isUninstalling;
        viewHolderbtn.mUninstall.enabelDefaultPress(true);
        viewHolderbtn.mUninstall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (pakageName == null) {
                    ToastUtils.showToast("应用不存在");
                    return;
                }
                if (AIDLUtils.hasInstallTask()) {
                    ToastUtils.showToast(R.string.has_install_task);
                    return;
                }

                // 静默卸载，注销第三方卸载老版本
                if (BaseApplication.isThird || !BaseApplication.isNewSign) {
                    // 第三方卸载
                    SingleThreadExecutor.getInstance().execute(
                            new UnInstallTask(pakageName));

                } else {
                    // 静默卸载
                    // 不是正在卸载中时
                    if (!isUninstalling) {
                        viewHolderbtn.mUninstall.setCurrentText(mActivity.getString(R.string.uninstalling));
                        viewHolderbtn.mUninstall.setEnabled(false);
                        AppManagerCenter.uninstallSilent(pakageName);
                    }

                }

            }

        });
        // }
        return convertView;
    }

    private void getUninstallView(ViewHolder viewHolder, int position) {

        AppInfo appInfo = null;

        if (position < userInfos.size() && position >= 0) { // 如果是用户程序条件判断满足
            appInfo = userInfos.get(position);
        }

        if (appInfo == null) {
            return;
        } else {
            viewHolder.mIcon.setImageDrawable(appInfo.mIcon);
            viewHolder.mName.setText(appInfo.mLabel);

            // 设置button的值
            if (AppManagerCenter.isUninstalling(userInfos.get(position).mPackageName)) {

                viewHolder.mUninstall.setCurrentText(mActivity.getString(R.string.uninstalling));
                viewHolder.mUninstall.setEnabled(false);

            } else {
                viewHolder.mUninstall.setCurrentText(mActivity.getString(R.string.uninstall));
                viewHolder.mUninstall.setEnabled(true);
            }
//			// 设置button的值
//			if (!userInfos.get(position).isUninstalling) {
//				
//				viewHolder.mUninstall.setText(R.string.uninstall);
//				viewHolder.mUninstall.setEnabled(true);
//				
//			} else {
//				
//				viewHolder.mUninstall.setText(R.string.uninstalling);
//				viewHolder.mUninstall.setEnabled(false);
//			}
            viewHolder.mSize.setText(CommonUtils.formatSize(appInfo.mSize, "#.00"));
//			viewHolder.mSize.setText(Formatter.formatFileSize(mActivity, appInfo.mSize));
        }

    }

//

    private class ViewHolder {
        private ImageView mIcon;
        private TextView mName;
        private TextView mSize;
        // private ProgressButton mUninstallProgB;
        private PrizeCommButton mUninstall;
//        private Button mUninstall;
    }


    static class UnInstallTask implements Runnable {

        private String pkg;

        public UnInstallTask(String pkg) {
            super();
            this.pkg = pkg;
        }

        @Override
        public void run() {
            PackageUtils.uninstallNormal(BaseApplication.curContext, pkg);
        }

    }
}
