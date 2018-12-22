package com.android.calendar.agenda;

import com.android.calendar.R;
import com.android.calendar.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;

import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SearchView;

import com.android.calendar.AllInOneMenuExtensionsInterface;
import com.prize.SearchViewStyle;
import com.android.calendar.ExtensionsFactory;
import android.graphics.Color;
import android.content.pm.PackageManager;
import android.Manifest;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PrizeAgendaActivity extends Activity {

	private FragmentManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initStatusBar();
		setContentView(R.layout.prize_agenda_layout);
		ActionBar mActionBar = getActionBar();
		mActionBar.setElevation(getResources().getDimension(R.dimen.prizeactionbar_lines));
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
		setTitle(R.string.all_agendas);
		fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		ft.replace(R.id.all_events_container, new AgendaFragment(System.currentTimeMillis(), false, false));
		ft.commit();

		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		View actionbarLayout = LayoutInflater.from(this).inflate(R.layout.prize_actionbar_title, null);

		getActionBar().setCustomView(
				actionbarLayout,
				new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
						ActionBar.LayoutParams.WRAP_CONTENT));
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Utils.returnToCalendarHome(this);
//			onBackPressed();
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//add by hekeyi for calendar V8.0
	private Menu mOptionsMenu;
	private SearchView mSearchView;
	private MenuItem mSearchMenu;
	private boolean mIsInSearchMode = false;
	private String mSearchString = null;
	private static final String BUNDLE_KEY_SEARCH_STRING = "key_search_string";
	private AllInOneMenuExtensionsInterface mExtensions = ExtensionsFactory.getAllInOneMenuExtensions();
	private int mOnCreateRequestPermissionFlag;
	private static final int ONCREATEPROCESSED = 1;
	private static final String[] CONTACTS_PERMISSION = {Manifest.permission.READ_CONTACTS};
	private static final String[] STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE};
	private static final String[] CALENDAR_PERMISSION = {Manifest.permission.READ_CALENDAR,
			Manifest.permission.WRITE_CALENDAR};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		Log.d("hekeyi_agenda","PrizeAgendaActivity  onCreateOptionsMenu");
//		Log.d("hekeyi_agenda","mOnCreateRequestPermissionFlag = "+mOnCreateRequestPermissionFlag);
//		Log.d("hekeyi_agenda","checkpermission(1) = "+checkPermissions(1));
//		if ((mOnCreateRequestPermissionFlag == ONCREATEPROCESSED) && (checkPermissions(1) == null)) {
			mOptionsMenu = menu;
			getMenuInflater().inflate(R.menu.all_in_one_title_bar_new, menu);

			// Add additional options (if any).
			Integer extensionMenuRes = mExtensions.getExtensionMenuResource(menu);
			if (extensionMenuRes != null) {
				getMenuInflater().inflate(extensionMenuRes, menu);
			}

			mSearchMenu = menu.findItem(R.id.action_search);
			mSearchView = (SearchView) mSearchMenu.getActionView();
			SearchViewStyle.on(mSearchView).setTextColor(Color.BLACK)
					.setTextSize(getResources().getDimension(R.dimen.prize_searchview_text_size))//prize-public-bug:20601 set text size-pengcancan-20160822
					.setHintTextColor(Color.parseColor("#787878")).setSearchButtonImageResource(R.drawable.ic_search_material)
					.setCloseBtnImageResource(R.drawable.search_cancel)
					.setGoBtnImageResource(R.drawable.ic_search_material).setCommitIcon(R.drawable.ic_search_material)
					.setSearchPlateDrawableId(R.drawable.searchview);
			if (mSearchView != null) {
				Utils.setUpSearchView(mSearchView, this);
//                mSearchView.setOnQueryTextListener(this);   //test
//                mSearchView.setOnSuggestionListener(this);  //test
			}

			if (mIsInSearchMode) {
				enterSearchMode();
				// Note: we should set search string after enterSearchMode(),
				// because enterSearchMode() will
				// set it to null
				if (mSearchView != null) {
					// restore search string to UI
//                    mSearchString = mBundleIcicleOncreate.getString(BUNDLE_KEY_SEARCH_STRING, null);
//                    mSearchView.setQuery(mSearchString, false);
				}
			}
//		}
		return true;
	}
	/**
	 * M: Enter activity's search mode, control UI on action bar, set search mode flag
	 */
	private void enterSearchMode() {
		mIsInSearchMode = true;
		if ((mSearchMenu != null) && !mSearchMenu.isActionViewExpanded()) {
			mSearchMenu.expandActionView();
		}
	}

	private String[] checkPermissions(int iPermissionCode) {
		boolean flagRequestPermission = false;
		ArrayList<String> list = new ArrayList<String>();
		String[] strPermission;

		if (!hasRequiredPermission(CALENDAR_PERMISSION)) {
			list.add(CALENDAR_PERMISSION[0]);
			list.add(CALENDAR_PERMISSION[1]);
			flagRequestPermission = true;
		}

		if (!hasRequiredPermission(STORAGE_PERMISSION)) {
			list.add(STORAGE_PERMISSION[0]);
			flagRequestPermission = true;
		}

		if (!hasRequiredPermission(CONTACTS_PERMISSION)) {
			list.add(CONTACTS_PERMISSION[0]);
			flagRequestPermission = true;
		}

		if (flagRequestPermission) {
			strPermission = new String[list.size()];

			strPermission = list.toArray(strPermission);

			return strPermission;
		}

		return null;
	}
	protected boolean hasRequiredPermission(String[] permissions) {
		for (String permission : permissions) {
			if (checkSelfPermission(permission)
					!= PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void initStatusBar() {
		Window window = getWindow();
		/*window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				//| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));      // prize modify zhaojian 8.0 2017803
        }else {
            window.setStatusBarColor(Color.TRANSPARENT);
        }*/
		window.setStatusBarColor(getResources().getColor(R.color.prize_bottom_button_bg_color));

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		try {
			Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
			Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
			Object gray = grayField.get(statusBarManagerClazz);
			Class windowManagerLpClazz = lp.getClass();
			Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
			statusBarInverseField.set(lp,gray);
			getWindow().setAttributes(lp);
		} catch (Exception e) {
		}
	}

	public FrameLayout getActionBarContainer() {
		Window window = getWindow();
		View v = window.getDecorView();
		int resId = getResources().getIdentifier("action_bar_container", "id", "android");
		return (FrameLayout)v.findViewById(resId);
	}
}
