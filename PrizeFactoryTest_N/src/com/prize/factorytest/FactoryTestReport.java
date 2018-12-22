package com.prize.factorytest;

import java.sql.Date;
import java.text.SimpleDateFormat;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import java.util.ArrayList;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.graphics.Color;
import java.util.HashMap;
import android.content.Context;
public class FactoryTestReport extends Activity {
	TextView mTestReport;
	String testreport;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		LinearLayout VersionLayout = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.testreport, null);
		setContentView(VersionLayout);
		mTestReport = (TextView) findViewById(R.id.testreport_show);
		testreport = getTestReport();
		mTestReport.setText(testreport);
		testReportDisplay();
	}

	private String getTestReport() {
		String temp = null;
		StringBuilder info = new StringBuilder();
		SimpleDateFormat formatter = new SimpleDateFormat(getResources().getString(R.string.date_format));
		Date curDate = new Date(System.currentTimeMillis());
		temp = formatter.format(curDate) + "\n";
		info.append(temp);
		return info.toString();
	}
	
	private ArrayList<HashMap<String,String>> testReportList = new ArrayList<HashMap<String,String>>();
    ListView testReportListView;
    TestReportAdapter testReportAdapter;
	private void testReportDisplay(){
		testReportListView = (ListView) findViewById(R.id.testreport_lv);
		getListViewData();
		testReportAdapter = new TestReportAdapter(getBaseContext(),testReportList);     
		testReportListView.setAdapter(testReportAdapter);
	}
	private void getListViewData(){
		for (PrizeFactoryTestListActivity.itempos = 0; PrizeFactoryTestListActivity.itempos < PrizeFactoryTestListActivity.items.length; PrizeFactoryTestListActivity.itempos++) {
			HashMap<String, String> testReportInfo = new HashMap<String, String>();
			testReportInfo.put("key", PrizeFactoryTestListActivity.items[PrizeFactoryTestListActivity.itempos]+":");
			if(null!=PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos]){
				testReportInfo.put("value",PrizeFactoryTestListActivity.testReportresult[PrizeFactoryTestListActivity.itempos]);
			}else{
				testReportInfo.put("value",getString(R.string.no_test));
			}
			
			testReportList.add(testReportInfo);
		}	
	}
	public class TestReportAdapter extends BaseAdapter{
    	Context context;
    	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    	
    	public TestReportAdapter(Context context,ArrayList<HashMap<String,String>> snList){
    		this.context = context;
    		list = snList;
    	}    	
    	
    	@Override
    	public int getCount() {
    		// TODO Auto-generated method stub
    		return list.size();
    	}

    	@Override
    	public Object getItem(int position) {
    		// TODO Auto-generated method stub
    		return position;  
    	}

    	@Override
    	public long getItemId(int id) {
    		// TODO Auto-generated method stub
    		return id;
    	}

    	
    	@Override
    	public View getView(int position, View convertView,
    			ViewGroup parent) {
    		// TODO Auto-generated method stub
    		ViewHolder holder;
    		if(convertView==null){
    	    LayoutInflater inflater = LayoutInflater.from(context);
    		convertView = inflater.inflate(R.layout.sn_item,parent, false);
    		holder = new ViewHolder();
    		holder.textViewItem01 = (TextView)convertView.findViewById(  
                    R.id.key);  
    		holder.textViewItem02 = (TextView)convertView.findViewById(  
                    R.id.value);  

    		convertView.setTag(holder);
    		}else{
    			holder = (ViewHolder)convertView.getTag();                 
    		}
    		holder.textViewItem01.setText(list.get(position).get("key").toString());     		
    		holder.textViewItem02.setText(list.get(position).get("value").toString());  
			if(list.get(position).get("value").toString().equals(getString(R.string.result_normal))){
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.GREEN);
			}else if(list.get(position).get("value").toString().equals(getString(R.string.result_error))){
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.RED);
			}else{
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.GRAY);
			}
    		return convertView;
    	}
    }
    
    public class ViewHolder{  
        TextView textViewItem01;  
        TextView textViewItem02;   
    }
}
