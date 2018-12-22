package com.prize.left.page.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.launcher3.R;
import com.prize.left.page.bean.AddrBean;
import com.prize.left.page.model.SetAddrModel;
import com.prize.left.page.response.BaiduPlaceResponse;
/***
 * 用于选择地址的adapter
 * @author fanjunchen sad
 *
 */
public class AddrAdapter extends BaseAdapter implements Filterable {

	private List<AddrBean> datas = null;
	
	private LayoutInflater mInflate;
	
	private Context mCtx;
	 
	private SetAddrModel model;
	
	private AddrBean curAddr;
	
	public AddrAdapter(Context ctx) {
		this(ctx, null);
	}
	
	public AddrAdapter(Context ctx, List<AddrBean> data) {
		mCtx = ctx;
		datas = data;
		mInflate = LayoutInflater.from(ctx);
	}
	
	public void setCurAddr(AddrBean addr) {
		curAddr = addr;
	}
	
	public void setModel(SetAddrModel m) {
		model = m;
	}
	/***
	 * 设置数据
	 * @param d
	 */
	public void setDatas(List<AddrBean> d) {
		datas = d;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return datas == null ? 0 : datas.size();
	}

	@Override
	public String getItem(int pos) {
		AddrBean a = datas == null ? null : datas.get(pos);
		// model.setAddr(a);
		String str = a==null ? "" : a.name;
		return str;
	}
	
	public void setAddr(int pos) {
		AddrBean a = datas == null ? null : datas.get(pos);
		model.setAddr(a);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View v, ViewGroup parent) { // addr_item
		ItemHolder holder = null;
		if (null == v) {
			
			holder = new ItemHolder();
			v = mInflate.inflate(R.layout.addr_item, null);
			holder.txtAddrName = (TextView)v.findViewById(R.id.addr_name);
			holder.txtCityName = (TextView)v.findViewById(R.id.addr_city);
			
			v.setTag(holder);
		}
		else {
			holder = (ItemHolder)v.getTag();
		}
		
		if (pos < 0 || pos >= datas.size())
			return v;
		
		AddrBean b = datas.get(pos);
		
		holder.txtAddrName.setText(b.name);
		
		String city = b.getAddrDistrict();
		if (TextUtils.isEmpty(city)) {
			city = b.city;
		}
		holder.txtCityName.setText(city);
		
		return v;
	}

	static class ItemHolder {
		// 地名和城市区域名, 如:　金融科技大厦　深圳市南山区
		TextView txtAddrName, txtCityName;
	}

	private Filter mFilter;
	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		if (null == mFilter)
			mFilter = new Filter() {
	            /**
	             * 本方法在后台线程执行，定义过滤算法
	             */
	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) {
	                // 这里要处理网络请求
	            	if (null == constraint || "".equals(constraint)) {
	            		if (curAddr != null) {
	            			FilterResults filterResults = new FilterResults();
	            			List<AddrBean> ls = new ArrayList<AddrBean>(1);
	            			ls.add(curAddr);
	            			filterResults.values = ls;
	    	                filterResults.count = 1;
	    	                return filterResults;
	            		}
	            		return null;
	            	}
	            	model.setQuery(constraint.toString());
	            	BaiduPlaceResponse rp = model.doGetSync();
	                FilterResults filterResults = new FilterResults();
	                filterResults.values = rp.result;   // results是上面的过滤结果
	                filterResults.count = rp.result.size();  // 结果数量
	                return filterResults;
	            }
	            /**
	             * 本方法在UI线程执行，用于更新自动完成列表
	             */
	            @SuppressWarnings("unchecked")
				@Override
	            protected void publishResults(CharSequence constraint, FilterResults results) {
	                if (results != null && results.count > 0) {
	                    // 有过滤结果，显示自动完成列表
	                	if (datas != null) {
	                		datas.clear();
	                		datas.addAll((List<AddrBean>)results.values);
	                	} else
	                		datas = (List<AddrBean>)results.values;
	                    notifyDataSetChanged();
	                } else if (results != null) {
	                    // 无过滤结果，关闭列表
	                    notifyDataSetInvalidated();
	                }
	            }
	            /**
	             * 如果需要控制提示文本的显示方式，重载此函数
	             */
	        };
		return mFilter;
	}
}
