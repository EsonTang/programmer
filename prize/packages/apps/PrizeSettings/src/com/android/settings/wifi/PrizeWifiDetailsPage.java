package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.PreferenceGroup;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.support.v7.widget.RecyclerView;
import android.support.v7.preference.PreferenceViewHolder;
/**
 * Created by prize on 2017/8/15.
 */
public class PrizeWifiDetailsPage extends SettingsPreferenceFragment implements Preference.OnPreferenceClickListener ,
        TextWatcher,View.OnTouchListener,AdapterView.OnItemClickListener,
        PrizeWifiIpSettingsPreference.PrizeCallBack,PrizeProxySettingsPreference.PrizeCallBackProxy{
    public static final String PRIZE_ADD_NETWORK  = "prize_add_network";
    public static final String PRIZE_REMOVE_NETWORK  = "prize_remove_network";
    public static final String PRIZE_CHANGED_PASSWORD  = "prize_changed_password";
    public static final String WIFI_STATUS  = "wifi_status";
    public static final String WIFI_SIGNAL  = "wifi_signal";
    public static final String WIFI_SPEED  = "wifi_speed";
    public static final String WIFI_SECURITY  = "wifi_security";
    public static final String WIFI_IP_ADDRESS  = "wifi_ip_address";
    public static final String PROXY_SETTINGS_TITLE  = "proxy_settings_title";
    public static final String WIFI_IP_SETTINGS  = "wifi_ip_settings";
    public static final String PROXY_SETTINGS_CONTAINER  = "proxy_settings_container";
    public static final String PRIZE_WIFI_DETAILS_PAGE  = "prize.wifi.details.page";
    public static final int ADD_NETWORK_STATUS0 = 0;
    public static final int ADD_NETWORK_STATUS1 = 1;
    public static final int CONNECT_NETWORK = 2;
    public static final int REMOVE_NETWORK = 3;
    public static final int SHOW_EAP_DIALOG = 4;
    public static final int BACK_PRESS = 5;
    private boolean isOpen = true;
    private boolean isProxyOpen = true;
    private boolean isBackPress = true;
    private String wifiStatus;
    private String wifiSignal;
    private String wifiSpeed;
    private String wifiSecurity;
    private String wifiIpAddress;
    private String wifiPassword;
    private EditText mEditText;
    private  String wifiName;
    private  String mWifiStaticIp;
    private  String mRouter;
    private  String mLength;
    private  String mDomain1;
    private  String mDomain2;
    private  AlertDialog mAlertDialog;
    private PopupWindow popupWindow;
    private View rootView;
    private PrizeWifiAdapter mPrizeWifiAdapter ;
    private String[]mProxyData;
    private String[]proxyData;
    private String[]mIpSetting;
    private  String[]ipSettings;
    private boolean showPassword = true;
    private int status =0;
    private Preference preferenceStatus;
    private Preference preferenceSignal;
    private Preference preferenceSpeed;
    private Preference preferenceSecurity;
    private Preference preferenceIpAddress;
    private Preference preferenceAddNetwork;
    private Preference preferenceRemoveNetwork;
    private Preference preferenceChangedPasswork;
    private Preference preferenceProxy;
    private Preference preferenceWifiIpSetting;
    private PreferenceCategory mProxyPreferenceCategory;
    private String ipAddress;
    private String router;
    private String length;
    private String domain1;
    private String domain2;
    private String pac;
    private List<PrizeWifiIpSettingsPreference>mList;
    private List<PrizeProxySettingsPreference>mProxyList;

    private String ipAddressEdiText;
    private String routerEdiText;
    private String lengthEdiText;
    private String domain1EdiText;
    private String domain2EdiText;
    private String hostEdiText;
    private String portEdiText;
    private String urlEdiText;
    private String pacEdiText;

    private String acessApointHost;
    private String acessApointPort;
    private String acessApointUrl;
    private String acessApointPac;
    private PreferenceAdapter mAdapter;
    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.WIFI;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.prize_wifi_details_page);
        Activity activity = getActivity();
        Intent mIntent  = activity.getIntent();
        Bundle mBubdle = mIntent.getBundleExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS);
         wifiStatus = mBubdle.getString("wifistatus");
         wifiSignal = mBubdle.getString("wifisignal");
         wifiSpeed = mBubdle.getString("wifispeed");
         wifiSecurity = mBubdle.getString("wifisecurity");
         wifiIpAddress = mBubdle.getString("wifiipaddress");
        status = mBubdle.getInt("status");
        wifiPassword = mBubdle.getString("wifipassword");
        wifiName = mBubdle.getString("wifiname");
        mWifiStaticIp = mBubdle.getString("wifiip");
        mRouter = mBubdle.getString("router");
        mLength = mBubdle.getString("length");
        mDomain1 = mBubdle.getString("domain1");
        mDomain2 = mBubdle.getString("domain2");

        acessApointHost = mBubdle.getString("hostname");
        acessApointPort = mBubdle.getString("port");
        acessApointUrl = mBubdle.getString("url");
        acessApointPac = mBubdle.getString("pac");
        activity.setTitle(wifiName);
        initPreference();
        initData();
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
    }
    private void initData() {
        mProxyData = getActivity().getResources().getStringArray(R.array.wifi_proxy_settings);
        proxyData = getActivity().getResources().getStringArray(R.array.wifi_proxy_settings_values);
        mIpSetting = getActivity().getResources().getStringArray(R.array.wifi_ip_settings);
        ipSettings = getActivity().getResources().getStringArray(R.array.wifi_ip_settings_values);

        ipAddress = getActivity().getResources().getString(R.string.wifi_ip_address);
        router = getActivity().getResources().getString(R.string.prize_wifi_router);
        length = getActivity().getResources().getString(R.string.prize_wifi_network_prefix_length);
        domain1 = getActivity().getResources().getString(R.string.prize_wifi_domain_first);
        domain2 = getActivity().getResources().getString(R.string.prize_wifi_domain_second);
        pac = getActivity().getResources().getString(R.string.proxy_url_title);
        mList = new ArrayList<>();
        mProxyList = new ArrayList<>();
        if(mWifiStaticIp != null){
            preferenceWifiIpSetting.setSummary(mIpSetting[1]);
			ipAddressEdiText = mWifiStaticIp;
			routerEdiText = mRouter;
			lengthEdiText = mLength;
			domain1EdiText = mDomain1;
			domain2EdiText = mDomain2;
            isStaticIp();
        }else{
            preferenceWifiIpSetting.setSummary(mIpSetting[0]);
        }
        if(acessApointHost != null && acessApointHost.length() > 0 && !acessApointHost.equals("localhost")){
            preferenceProxy.setSummary(mProxyData[1]);
			hostEdiText = acessApointHost;
			portEdiText = acessApointPort;
			urlEdiText = acessApointUrl;
           isStaticProxy();
        }else if(acessApointPac != null && acessApointPac.length() > 0){
            preferenceProxy.setSummary(mProxyData[2]);
			pacEdiText = acessApointPac;
            isPacProxy();
        }else{
            preferenceProxy.setSummary(mProxyData[0]);
        }

    }

    private void initPreference() {
        preferenceStatus = (Preference) findPreference(WIFI_STATUS);
        preferenceSignal = (Preference) findPreference(WIFI_SIGNAL);
        preferenceSpeed = (Preference) findPreference(WIFI_SPEED);
        preferenceSecurity = (Preference) findPreference(WIFI_SECURITY);
        preferenceIpAddress = (Preference) findPreference(WIFI_IP_ADDRESS);
        preferenceAddNetwork = (Preference) findPreference(PRIZE_ADD_NETWORK);
       preferenceRemoveNetwork = (Preference) findPreference(PRIZE_REMOVE_NETWORK);
        preferenceChangedPasswork = (Preference) findPreference(PRIZE_CHANGED_PASSWORD);
        preferenceProxy = (Preference) findPreference(PROXY_SETTINGS_TITLE);
        preferenceWifiIpSetting = (Preference) findPreference(WIFI_IP_SETTINGS);
        mProxyPreferenceCategory = (PreferenceCategory) findPreference(PROXY_SETTINGS_CONTAINER);
        preferenceProxy.setOnPreferenceClickListener(this);
        preferenceWifiIpSetting.setOnPreferenceClickListener(this);
        preferenceAddNetwork.setOnPreferenceClickListener(this);
        preferenceChangedPasswork.setOnPreferenceClickListener(this);
        preferenceRemoveNetwork.setOnPreferenceClickListener(this);
        if(status == 2){
            preferenceStatus.setSummary(wifiStatus);
            preferenceSignal.setSummary(wifiSignal);
            preferenceSpeed.setSummary(wifiSpeed);
            preferenceSecurity.setSummary(wifiSecurity);
            preferenceIpAddress.setSummary(wifiIpAddress);
            getPreferenceScreen().removePreference(preferenceAddNetwork);
            getPreferenceScreen().removePreference(preferenceChangedPasswork);

        }else if(status == 1){
            getPreferenceScreen().removePreference(preferenceStatus);
            getPreferenceScreen().removePreference(preferenceSpeed);
            getPreferenceScreen().removePreference(preferenceIpAddress);
            preferenceSignal.setSummary(wifiSignal);
            preferenceSecurity.setSummary(wifiSecurity);
        }else{
            getPreferenceScreen().removePreference(preferenceStatus);
            getPreferenceScreen().removePreference(preferenceSpeed);
            getPreferenceScreen().removePreference(preferenceIpAddress);
            getPreferenceScreen().removePreference(preferenceRemoveNetwork);
            getPreferenceScreen().removePreference(preferenceChangedPasswork);
            preferenceSignal.setSummary(wifiSignal);

            preferenceSecurity.setSummary(wifiSecurity);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        String key = preference.getKey();
        switch (key){
            case PRIZE_ADD_NETWORK:
				if(wifiSecurity != null){
					//String securityType = getActivity().getResources().getString(com.android.settingslib.R.string.wifi_security_none);
					String securityTypeEAP = getActivity().getResources().getString(com.android.settingslib.R.string.wifi_security_eap);
					// if(wifiSecurity.equals(securityType)){
						// setResult(ADD_NETWORK_STATUS0);
						// finish();
					// }
					if(wifiSecurity.equals(securityTypeEAP)){
						setResult(SHOW_EAP_DIALOG);
						isBackPress = false;
						finish();
						return true;
					}
				}
                if (status == 1){
                    setResult(CONNECT_NETWORK);
					isBackPress = false;
                    finish();
                }else{
                    showDialog(0);
                }
                break;
            case PRIZE_REMOVE_NETWORK:
                setResult(REMOVE_NETWORK);
				isBackPress = false;
                finish();
                break;
            case PRIZE_CHANGED_PASSWORD:
                showDialog(1);
                break;
            case PROXY_SETTINGS_TITLE:
                showPopWindow(mProxyData);
                break;
            case WIFI_IP_SETTINGS:
                showPopWindow(mIpSetting);
                break;

        }
        return false;
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.prize_wifi_password_dialog, null);
        TextView textView = (TextView)view.findViewById(R.id.wifi_title);
        textView.setText(wifiName);
        mEditText = (EditText)view.findViewById(R.id.wifi_wdittext_password);
        mEditText.addTextChangedListener(this);
        mEditText.setOnTouchListener(this);
        builder.setView(view).setPositiveButton(R.string.wifi_connect, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(status == 1){
                    Intent intent = new Intent();
                    intent.putExtra("wifiPassword",mEditText.getText().toString());
					sendData(intent);
                    setResult(ADD_NETWORK_STATUS1,intent);
					isBackPress = false;
                    finish();
                }else{
                    Intent intent = new Intent();
                    intent.putExtra("wifiPassword",mEditText.getText().toString());
                    sendData(intent);
                    setResult(ADD_NETWORK_STATUS0, intent);
					isBackPress = false;
                    finish();
                }
            }
        }).setNegativeButton(R.string.wifi_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
		mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                 mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });
		
        return mAlertDialog;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(mAlertDialog != null){
            if(mEditText.getText().length()>7){
                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }else{
                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Drawable drawable = mEditText.getCompoundDrawables()[2];
        if (drawable == null)
            return false;
        if (event.getAction() != MotionEvent.ACTION_UP)
            return false;
        if (event.getX() > mEditText.getWidth() - mEditText.getPaddingRight() - drawable.getIntrinsicWidth()){
          if(showPassword){
              mEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
              mEditText.setSelection(mEditText.getText().toString().length());
			  mEditText.setCompoundDrawablesWithIntrinsicBounds(null,null,getActivity().getResources().getDrawable(R.drawable.prize_press_show_password),null);
              showPassword = false;
          }else{
              mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
              mEditText.setSelection(mEditText.getText().toString().length());
			  mEditText.setCompoundDrawablesWithIntrinsicBounds(null,null,getActivity().getResources().getDrawable(R.drawable.prize_show_password),null);
              showPassword = true;
          }
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mPrizeWifiAdapter.getCount() == 3){
            switch (position){
                case 0:
                    if(mProxyList != null && mProxyList.size() != 0){
                        for(int i = 0;i < mProxyList.size();i++){
                            mProxyPreferenceCategory.removePreference(mProxyList.get(i));
                        }
                    }
                    mProxyList.clear();
                    isProxyOpen = true;
                    popupWindow.dismiss();
                    preferenceProxy.setSummary(mProxyData[position]);
                    break;
                case 1:
                    if(mProxyList != null && mProxyList.size() == 1){
                        mProxyPreferenceCategory.removePreference(mProxyList.get(0));
                        mProxyList.clear();
                    }
                    for(int i = 0;i < proxyData.length;i++){
                        if(isProxyOpen){
                            PrizeProxySettingsPreference prizeWifiIpSettingsPreference = new PrizeProxySettingsPreference(getActivity(),this);
                            prizeWifiIpSettingsPreference.setTitle(proxyData[i]);
                            mProxyPreferenceCategory.addPreference(prizeWifiIpSettingsPreference);
                            mProxyList.add(prizeWifiIpSettingsPreference);
                        }

                    }
                    isProxyOpen = false;
                    popupWindow.dismiss();
                    preferenceProxy.setSummary(mProxyData[position]);
                    break;
                case 2:
                    if(mProxyList != null && mProxyList.size()> 1){
                        for(int i = 0;i < mProxyList.size();i++){
                            mProxyPreferenceCategory.removePreference(mProxyList.get(i));
                        }
                        mProxyList.clear();
                    }
                    if(mProxyList.size() == 1){
                        popupWindow.dismiss();
                        isProxyOpen = true;
                        preferenceProxy.setSummary(mProxyData[position]);
                    }else{
                        PrizeProxySettingsPreference prizeWifiIpSettingsPreference = new PrizeProxySettingsPreference(getActivity(),this);
                        prizeWifiIpSettingsPreference.setTitle(R.string.proxy_url_title);
                        mProxyPreferenceCategory.addPreference(prizeWifiIpSettingsPreference);
                        mProxyList.add(prizeWifiIpSettingsPreference);
                        popupWindow.dismiss();
                        isProxyOpen = true;
                        preferenceProxy.setSummary(mProxyData[position]);
                    }
                    break;
            }
        }else{
            switch (position){
                case 0:
                    if(mList != null && mList.size() != 0){
                        for(int i = 0;i < mList.size();i++){
                            getPreferenceScreen().removePreference(mList.get(i));
                        }
                    }
                    mList.clear();
                    popupWindow.dismiss();
                    isOpen = true;
                    preferenceWifiIpSetting.setSummary(mIpSetting[position]);
                    break;
                case 1:
                    for(int i = 0;i < ipSettings.length;i++){
                        if(isOpen){
                            PrizeWifiIpSettingsPreference prizeWifiIpSettingsPreference = new PrizeWifiIpSettingsPreference(getActivity(),this);
                            prizeWifiIpSettingsPreference.setTitle(ipSettings[i]);
                            getPreferenceScreen().addPreference(prizeWifiIpSettingsPreference);
                            mList.add(prizeWifiIpSettingsPreference);
                        }

                    }
                    isOpen = false;
                    popupWindow.dismiss();
                    preferenceWifiIpSetting.setSummary(mIpSetting[position]);
                    break;
            }

        }
    }

    @Override
    public void getEditTextValus(PrizeWifiIpSettingsPreference mPrizeWifiIpSettingsPreference) {
        String titleId = mPrizeWifiIpSettingsPreference.getTitle().toString();

        if(titleId.equals(ipAddress)){
            Map <String,EditText>map = mPrizeWifiIpSettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            ipAddressEdiText = editText.getText().toString();
        }else if(titleId.equals(router)){
            Map <String,EditText>map = mPrizeWifiIpSettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            routerEdiText = editText.getText().toString();
        }else if(titleId.equals(length)){
            Map <String,EditText>map = mPrizeWifiIpSettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            lengthEdiText = editText.getText().toString();
        }else if(titleId.equals(domain1)){
            Map <String,EditText>map = mPrizeWifiIpSettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            domain1EdiText = editText.getText().toString();
        }else if(titleId.equals(domain2)){
            Map <String,EditText>map = mPrizeWifiIpSettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            domain2EdiText = editText.getText().toString();
        }
    }

    @Override
    public void getProxyEditTextValus(PrizeProxySettingsPreference mPrizeProxySettingsPreference) {
        String titleId = mPrizeProxySettingsPreference.getTitle().toString();
        Map <String,EditText>map1 = mPrizeProxySettingsPreference.getMap();
        EditText editText1 =  map1.get(titleId);
       String text = editText1.getText().toString();
        if(titleId.equals(proxyData[0])){
            Map <String,EditText>map = mPrizeProxySettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            hostEdiText = editText.getText().toString();
        }else if(titleId.equals(proxyData[1])){
            Map <String,EditText>map = mPrizeProxySettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            portEdiText = editText.getText().toString();
        }else if(titleId.equals(proxyData[2])){
            Map <String,EditText>map = mPrizeProxySettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            urlEdiText = editText.getText().toString();
        }else if(titleId.equals(pac)){
            Map <String,EditText>map = mPrizeProxySettingsPreference.getMap();
            EditText editText =  map.get(titleId);
            pacEdiText = editText.getText().toString();
        }
    }

    public class PrizeWifiAdapter extends BaseAdapter{
        String []mData = null;
        Context mContext;
        public PrizeWifiAdapter(String[] data,Context context){
            mData = data;
            mContext = context;
        }
        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public Object getItem(int position) {
            return mData[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mViewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.prize_show_window_item,parent,false);
                mViewHolder = new ViewHolder();
                mViewHolder.mImageView = (ImageView)convertView.findViewById(R.id.checked_iamge);
                mViewHolder.mTextView = (TextView)convertView.findViewById(R.id.security_type);
                convertView.setTag(mViewHolder);
            }else{
                mViewHolder = (ViewHolder) convertView.getTag();
            }
            if(getCount()>2){
                if(preferenceProxy.getSummary().equals(mProxyData[position])){
                    mViewHolder.mImageView.setVisibility(View.VISIBLE);
                }else{
                    mViewHolder.mImageView.setVisibility(View.GONE);
                }
            }else{
                if(preferenceWifiIpSetting.getSummary().equals(mIpSetting[position])){
                    mViewHolder.mImageView.setVisibility(View.VISIBLE);
                }else{
                    mViewHolder.mImageView.setVisibility(View.GONE);
                }
            }

            mViewHolder.mTextView.setText(mData[position]);
            return convertView;
        }

        class ViewHolder{
            private TextView mTextView;
            private ImageView mImageView;
        }
    }

    public void showPopWindow(String[]data){
        popupWindow = new PopupWindow(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.prize_show_popwindow,null,false);
        popupWindow.setContentView(view);
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(R.style.popAnimator);
        final ListView mListView = (ListView)view.findViewById(R.id.pw_layout);
        mPrizeWifiAdapter = new PrizeWifiAdapter(data,getActivity());
        mListView.setAdapter(mPrizeWifiAdapter);
        mListView.setOnItemClickListener(this);
        ColorDrawable dw = new ColorDrawable(getActivity().getResources().getColor(R.color.settings_background));
        popupWindow.setBackgroundDrawable(dw);
        setBackGround();
        /* prize-add-by-lijimeng-for bugid 52914-20180319-start*/
        preferenceProxy.setEnabled(false);
        preferenceWifiIpSetting.setEnabled(false);
        /* prize-add-by-lijimeng-for bugid 52914-20180319-end*/
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1f;
                getActivity().getWindow().setAttributes(lp);
                /* prize-add-by-lijimeng-for bugid 52914-20180319-start*/
                preferenceProxy.setEnabled(true);
                preferenceWifiIpSetting.setEnabled(true);
                /* prize-add-by-lijimeng-for bugid 52914-20180319-end*/
            }
        });
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setFocusable(true);
    }
    private void setBackGround(){
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.7f;
        getActivity().getWindow().setAttributes(lp);
    }

    public PrizeWifiIpSettingsPreference.PrizeCallBack getPrizeCallBack(){

        return this;
    }
    public void isStaticIp(){
        for(int i = 0;i < ipSettings.length;i++){
            PrizeWifiIpSettingsPreference prizeWifiIpSettingsPreference = new PrizeWifiIpSettingsPreference(getActivity(),this);
            prizeWifiIpSettingsPreference.setTitle(ipSettings[i]);
            getPreferenceScreen().addPreference(prizeWifiIpSettingsPreference);
            mList.add(prizeWifiIpSettingsPreference);
        }
        isOpen = false;
    }
    public String getIpConfig(String title){
        if(title == null){
            return null;
        }
        if(title.equals(ipAddress)){
          return  mWifiStaticIp;
        }else if(title.equals(router)){
            return  mRouter;
        }else if(title.equals(length)){
            return  mLength;
        }else if(title.equals(domain1)){
            return  mDomain1;
        }else if (title.equals(domain2)){
            return  mDomain2;
        }
        return null;
    }
    private void isStaticProxy(){
        for(int i = 0;i < proxyData.length;i++){
            PrizeProxySettingsPreference prizeWifiIpSettingsPreference = new PrizeProxySettingsPreference(getActivity(),this);
            prizeWifiIpSettingsPreference.setTitle(proxyData[i]);
            mProxyPreferenceCategory.addPreference(prizeWifiIpSettingsPreference);
            mProxyList.add(prizeWifiIpSettingsPreference);
        }
        isProxyOpen = false;
    }
    private void isPacProxy(){
        PrizeProxySettingsPreference prizeWifiIpSettingsPreference = new PrizeProxySettingsPreference(getActivity(),this);
        prizeWifiIpSettingsPreference.setTitle(pac);
        mProxyPreferenceCategory.addPreference(prizeWifiIpSettingsPreference);
        mProxyList.add(prizeWifiIpSettingsPreference);
       // EditText editText = (EditText) prizeWifiIpSettingsPreference.getMap().get(pac);
        //editText.setText(acessApointPac);
    }
    public String getProxy(String title){
        if(title == null){
            return null;
        }
        if(title.equals(proxyData[0])){
            if(acessApointUrl != null && !acessApointUrl.equals("localhost")){
                return acessApointHost;
            }
        }else if(title.equals(proxyData[1])){
            if(acessApointPort != null && Integer.parseInt(acessApointPort) > 0){
                return acessApointPort;
            }
        }else if(title.equals(proxyData[2])){
            return acessApointUrl;
        }else if(title.equals(pac)){
            return acessApointPac;
        }
        return null;
    }
    public String getIpValues(PrizeWifiIpSettingsPreference mPrizeWifiIpSettingsPreference){
        String titleId = mPrizeWifiIpSettingsPreference.getTitle().toString();
        if(titleId.equals(ipAddress)){
            return ipAddressEdiText;
        }else if(titleId.equals(router)){
           return routerEdiText;
        }else if(titleId.equals(length)){
            return lengthEdiText;
        }else if(titleId.equals(domain1)){
            return domain1EdiText;
        }else if(titleId.equals(domain2)){
            return domain2EdiText;
        }
        return null;
    }
    public String getProxyValues(PrizeProxySettingsPreference mPrizeProxySettingsPreference){
        String titleId = mPrizeProxySettingsPreference.getTitle().toString();
        if(titleId.equals(proxyData[0])){
            return hostEdiText;
        }else if(titleId.equals(proxyData[1])){
            return portEdiText;
        }else if(titleId.equals(proxyData[2])){
            return urlEdiText;
        }else if(titleId.equals(pac)){
            return pacEdiText;
        }
        return null;
    }
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        mAdapter = new PreferenceAdapter(preferenceScreen,getContext());
        return mAdapter;
    }
    public static class PreferenceAdapter extends HighlightablePreferenceGroupAdapter{
        public PreferenceAdapter(PreferenceGroup preferenceGroup, Context context) {
            super(preferenceGroup, context);
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.setIsRecyclable(false);
        }
    }
	
	@Override
    public void onDestroyView() {
		if(getActivity() != null && isBackPress){
			Intent intent = new Intent();
			sendData(intent);
			intent.setAction(PRIZE_WIFI_DETAILS_PAGE);
			getActivity().sendBroadcast(intent);
		}
		
        super.onDestroyView();
    }
	private void sendData(Intent intent){
		intent.putExtra("ip_address",ipAddressEdiText);
        intent.putExtra("router",routerEdiText);
        intent.putExtra("length",lengthEdiText);
        intent.putExtra("domian1",domain1EdiText);
        intent.putExtra("domian2",domain2EdiText);
        intent.putExtra("ip_summary",preferenceWifiIpSetting.getSummary().toString());
		
        intent.putExtra("host",hostEdiText);
        intent.putExtra("port",portEdiText);
        intent.putExtra("url",urlEdiText);
        intent.putExtra("pac",pacEdiText);
        intent.putExtra("proxy_summary",preferenceProxy.getSummary().toString());
	}
}
