package com.android.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import com.android.settings.R;
/**
 * Created by prize on 2017/11/6.
 */
public class PrizeAlertDialog extends AlertDialog {

    private ListView mListView;
    private List<Integer> mList = new ArrayList<Integer>();
	private Context mContext;
	private TextView mWifiName;
    protected PrizeAlertDialog(Context context) {
        super(context);
    }
    protected PrizeAlertDialog(Context context, List<Integer>list) {
        super(context);
		mContext = context;
        mList.clear();
        mList.addAll(list);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.prize_content_menu_list,null);
        setView(view);
        mListView = (ListView) view.findViewById(R.id.prize_content_listview);
		mWifiName = (TextView)view.findViewById(R.id.prize_wifi_name_title);
        mListView.setAdapter(new PrizeAdapter());
    }

    private class PrizeAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PrizeMenuHolder mPrizeMenuHolder = null;
            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.prize_content_menu_list_item,parent,false);
                mPrizeMenuHolder = new PrizeMenuHolder(convertView);
                convertView.setTag(mPrizeMenuHolder);
            }else{
                mPrizeMenuHolder = (PrizeMenuHolder) convertView.getTag();
            }
            mPrizeMenuHolder.mTextView = (TextView) convertView.findViewById(R.id.prize_wifi_menu);
            mPrizeMenuHolder.mTextView.setText(mContext.getResources().getString(mList.get(position)));
            return convertView;
        }
    }
    class PrizeMenuHolder {
        private TextView mTextView;
        PrizeMenuHolder(View view){
            mTextView = (TextView) view.findViewById(R.id.prize_wifi_menu);
        }
    }
    public ListView getListView(){
        return mListView;
    }
	public TextView getTextView(){
        return mWifiName;
    }
}
