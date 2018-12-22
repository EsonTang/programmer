/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppGameListActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;
import java.util.List;

import static com.prize.appcenter.activity.CategoryAppGameListActivity.parentID;
import static com.prize.appcenter.activity.CategoryAppGameListActivity.subtypeName;
import static com.prize.appcenter.activity.CategoryAppGameListActivity.typeName;

/**
 * 类描述：首页card（田字格）
 *
 * @author huanglingjun
 * @version 1.0
 */
public class CardMattsGridView extends LinearLayout implements AdapterView.OnItemClickListener {
    private Activity mContext;
    private MattsAdapter mMattsAdapter;
    private GridView gridView = null;
    private PrizeAppsCardData.FocusBean mFocusBean;
    private String fromPage;
    private boolean isGame = false;

    public CardMattsGridView(Activity context, String fromPage, boolean isGame) {
        super(context);
        mContext = context;
        this.fromPage = fromPage;
        this.isGame = isGame;
        mMattsAdapter = new MattsAdapter(context);
        setOrientation(VERTICAL);
        View view = inflate(context, R.layout.card_matts_gridview, this);
        findViewById(view);
    }


    private void findViewById(View view) {
        gridView = (GridView) view.findViewById(R.id.mClassify);
        gridView.setOnItemClickListener(this);
    }

    public void setData(PrizeAppsCardData.FocusBean items) {
        mFocusBean = items;
        if (mMattsAdapter != null && items != null && items.catFocusList != null) {
            gridView.setAdapter(mMattsAdapter);
            mMattsAdapter.setData(items.catFocusList);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(mContext,
                CategoryAppGameListActivity.class);
        intent.putExtra(parentID, mMattsAdapter.getItem(position).cId);
        intent.putExtra(typeName, mMattsAdapter.getItem(position).pCatName);
        intent.putExtra(subtypeName, mMattsAdapter.getItem(position).catName);
        intent.putExtra(CategoryAppGameListActivity.tags, mMattsAdapter.getItem(position).tags);
        intent.putExtra(CategoryAppGameListActivity.selectPos, mMattsAdapter.getItem(position).cIdpos);
        intent.putExtra(CategoryAppGameListActivity.isGameKey, (mMattsAdapter.getItem(position).catTypeId) == 2);
        mContext.startActivity(intent);
        MTAUtil.onclicmatts(mMattsAdapter.getItem(position).title, position + 1);

        if (!TextUtils.isEmpty(fromPage)) {
            MTAUtil.onHomePageFocusClick(mFocusBean.positon);
        } else {
            if (isGame) {
                MTAUtil.onGamePageFocusClick(mFocusBean.positon);
            } else {
                MTAUtil.onAppPageFocusClick(mFocusBean.positon);
            }
        }
    }


    public class MattsAdapter extends BaseAdapter {

        private Context context;
        /**
         * 列表项
         */
        private List<PrizeAppsCardData.CatFocusBean> items = new ArrayList<PrizeAppsCardData.CatFocusBean>();

        public MattsAdapter(Context context) {
            this.context = context;
        }

        /**
         * 设置列表项
         *
         * @param data
         */
        public void setData(ArrayList<PrizeAppsCardData.CatFocusBean> data) {
            if (null == data) {
                return;
            }

            items = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public PrizeAppsCardData.CatFocusBean getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.item_matts_imageview, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.img_id);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
                convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PrizeAppsCardData.CatFocusBean bean = items.get(position);
            holder.title.setText(bean.title);
            ImageLoader.getInstance().displayImage(bean.imageUrl, holder.image,
                    UILimageUtil.getUINewAppHeader(), null);

            return convertView;
        }


    }

    private static class ViewHolder {
        ImageView image;

        TextView title;
    }
}
