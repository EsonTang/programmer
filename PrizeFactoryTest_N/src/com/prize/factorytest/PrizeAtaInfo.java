package com.prize.factorytest;

import com.prize.factorytest.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.os.SystemProperties;
import java.util.ArrayList;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import java.util.HashMap;
import android.content.Context;

public class PrizeAtaInfo extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ata_info);
		ataInfoDisplay();
	}
	
	private int get(char c, int index)
    {
        return (c & (0x1 << index)) >> index;
    }
	
	private char charToHex(char c){
		char r=0x00;
		switch(c){
		case '0':
			r=0x00;
			break;
		case '1':
			r=0x01;
			break;
		case '2':
			r=0x02;
			break;
		case '3':
			r=0x03;
			break;
		case '4':
			r=0x04;
			break;
		case '5':
			r=0x05;
			break;
		case '6':
			r=0x06;
			break;
		case '7':
			r=0x07;
			break;
		case '8':
			r=0x08;
			break;
		case '9':
			r=0x09;
			break;
		case 'A':
			r=0x0a;
			break;
		case 'B':
			r=0x0b;
			break;
		case 'C':
			r=0x0c;
			break;
		case 'D':
			r=0x0d;
			break;
		case 'E':
			r=0x0e;
			break;
		case 'F':
			r=0x0f;
			break;
		default:
			r=0x00;
			break;
		}
		return r;
	}
	private void ataTestInfo(String temp){
		HashMap<String, String> ataInfo = new HashMap<String, String>();
		if(null!=temp&&temp.length()>42)
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "GPS: ");
		if(null != temp&&temp.length()>33){						
			if(get(charToHex(temp.charAt(33)),0)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "FM: ");
		if(null != temp&&temp.length()>33){						
			if(get(charToHex(temp.charAt(33)),1)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "BT: ");
		if(null != temp&&temp.length()>33){						
			if(get(charToHex(temp.charAt(33)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "WIFI: ");
		if(null != temp&&temp.length()>33){						
			if(get(charToHex(temp.charAt(33)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "ALS/PS: ");
		if(null != temp&&temp.length()>34){						
			if(get(charToHex(temp.charAt(34)),0)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "M-Sensor: ");
		if(null != temp&&temp.length()>34){						
			if(get(charToHex(temp.charAt(34)),1)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "G-Sensor: ");
		if(null != temp&&temp.length()>34){						
			if(get(charToHex(temp.charAt(34)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Signal RSSI: ");
		if(null != temp&&temp.length()>34){						
			if(get(charToHex(temp.charAt(34)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "MainCamera: ");
		if(null != temp&&temp.length()>35){						
			if(get(charToHex(temp.charAt(35)),0)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Touch Panel: ");
		if(null != temp&&temp.length()>35){						
			if(get(charToHex(temp.charAt(35)),1)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "LCM: ");
		if(null != temp&&temp.length()>35){						
			if(get(charToHex(temp.charAt(35)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "GYROSCOPE: ");
		if(null != temp&&temp.length()>35){						
			if(get(charToHex(temp.charAt(35)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Key Pad: ");
		if(null != temp&&temp.length()>36){						
			if(get(charToHex(temp.charAt(36)),0)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "SIM: ");
		if(null != temp&&temp.length()>36){						
			if(get(charToHex(temp.charAt(36)),1)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "T Card: ");
		if(null != temp&&temp.length()>36){						
			if(get(charToHex(temp.charAt(36)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "SubCamera: ");
		if(null != temp&&temp.length()>36){						
			if(get(charToHex(temp.charAt(36)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Vibrator: ");
		if(null != temp&&temp.length()>37){						
			if(get(charToHex(temp.charAt(37)),0)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Headset: ");
		if(null != temp&&temp.length()>37){						
			if(get(charToHex(temp.charAt(37)),1)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Speaker: ");
		if(null != temp&&temp.length()>37){						
			if(get(charToHex(temp.charAt(37)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Receiver: ");
		if(null != temp&&temp.length()>37){						
			if(get(charToHex(temp.charAt(37)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Idle Current: ");
		if(null != temp&&temp.length()>38){						
			if(get(charToHex(temp.charAt(38)),0)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "OTG: ");
		if(null != temp&&temp.length()>38){						
			if(get(charToHex(temp.charAt(38)),1)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "LED: ");
		if(null != temp&&temp.length()>38){						
			if(get(charToHex(temp.charAt(38)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Charger: ");
		if(null != temp&&temp.length()>38){						
			if(get(charToHex(temp.charAt(38)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Fingerprint: ");
		if(null != temp&&temp.length()>39){						
			if(get(charToHex(temp.charAt(39)),2)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);
		
		ataInfo = new HashMap<String, String>();
		ataInfo.put("key", "Off Current: ");
		if(null != temp&&temp.length()>39){						
			if(get(charToHex(temp.charAt(39)),3)==1)
				ataInfo.put("value", getString(R.string.is_test));
			else
				ataInfo.put("value", getString(R.string.no_test));
		}else
			ataInfo.put("value", getString(R.string.no_test));
		ataList.add(ataInfo);		
	}
	
	private ArrayList<HashMap<String,String>> ataList = new ArrayList<HashMap<String,String>>();
    ListView ataListView;
    AtaInfoAdapter ataAdapter;
	private void ataInfoDisplay(){
		ataListView = (ListView) findViewById(R.id.ata_lv);
		getListViewData(SystemProperties.get("gsm.serial"));
		ataAdapter = new AtaInfoAdapter(getBaseContext(),ataList);     
		ataListView.setAdapter(ataAdapter);
	}
	private void getListViewData(String temp){
			ataTestInfo(temp);
	}
	public class AtaInfoAdapter extends BaseAdapter{
    	Context context;
    	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    	
    	public AtaInfoAdapter(Context context,ArrayList<HashMap<String,String>> snList){
    		this.context = context;
    		list = snList;
    	}    	
    	
    	@Override
    	public int getCount() {
    		return list.size();
    	}

    	@Override
    	public Object getItem(int position) {
    		return position;  
    	}

    	@Override
    	public long getItemId(int id) {
    		return id;
    	}
    	
    	@Override
    	public View getView(int position, View convertView,
    			ViewGroup parent) {
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
			if(list.get(position).get("value").toString().equals(getString(R.string.is_test))){
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.GREEN);
			}else{
				holder.textViewItem01.setTextColor(Color.WHITE);
				holder.textViewItem02.setTextColor(Color.RED);
			}
    		return convertView;
    	}
    }
    
    public class ViewHolder{  
        TextView textViewItem01;  
        TextView textViewItem02;   
    }
}