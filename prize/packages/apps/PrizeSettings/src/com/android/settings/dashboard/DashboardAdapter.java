/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dashboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.DevelopmentSettings;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionAdapterUtils;
import com.android.settingslib.SuggestionParser;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.WirelessUtils;
import android.widget.Switch;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import android.os.UserManager;
import android.content.SharedPreferences;
import com.android.settings.dashboard.PrizeNotificationImageView;
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardItemHolder>
        implements View.OnClickListener {
    public static final String TAG = "DashboardAdapter";
    private static final int NS_SPACER = 0;
    private static final int NS_SUGGESTION = 1000;
    private static final int NS_ITEMS = 2000;
    private static final int NS_CONDITION = 3000;
LinearLayout mLinearLayout;
    private static int SUGGESTION_MODE_DEFAULT = 0;
    private static int SUGGESTION_MODE_COLLAPSED = 1;
    private static int SUGGESTION_MODE_EXPANDED = 2;
int lastPosition = 100;
    public  ImageView arrowReminders;
    public  Switch dashboardSwitch;
    private static final int DEFAULT_SUGGESTION_COUNT = 2;

    private final List<Object> mItems = new ArrayList<>();
    private final List<Integer> mTypes = new ArrayList<>();
    private final List<Integer> mIds = new ArrayList<>();
    private final List<Object> containertitle = new ArrayList<>();
    private final List<CharSequence> checkItems = new ArrayList<>();
    private final IconCache mCache;

    private final Context mContext;

    private List<DashboardCategory> mCategories;
    private List<Condition> mConditions;
    private List<Tile> mSuggestions;
    // Add by zhudaopeng at 2016-10-29 Start
    private boolean mIsShowSuggestions = false;
    // Add by zhudaopeng at 2016-10-29 End

    private boolean mIsShowingAll;
    // Used for counting items;
    private int mId;
    
    private int mSuggestionMode = SUGGESTION_MODE_DEFAULT;

    private Condition mExpandedCondition = null;
    private SuggestionParser mSuggestionParser;

    public DashboardAdapter(Context context, SuggestionParser parser) {
        mContext = context;
        mCache = new IconCache(context);
        mSuggestionParser = parser;

        setHasStableIds(true);
        setShowingAll(true);
    }

    public List<Tile> getSuggestions() {
        return mSuggestions;
    }

    public void setSuggestions(List<Tile> suggestions) {
        mSuggestions = suggestions;
        recountItems();
    }
    
    public Tile getTile(ComponentName component) {
        for (int i = 0; i < mCategories.size(); i++) {
            for (int j = 0; j < mCategories.get(i).tiles.size(); j++) {
                Tile tile = mCategories.get(i).tiles.get(j);
                if (component.equals(tile.intent.getComponent())) {
                    return tile;
                }
            }
        }
        return null;
    }

    public void setCategories(List<DashboardCategory> categories) {
        mCategories = categories;

        // TODO: Better place for tinting?
        TypedValue tintColor = new TypedValue();
        mContext.getTheme().resolveAttribute(com.android.internal.R.attr.colorAccent,
                tintColor, true);
        int categoryTilesSize = 0;
        for (int i = 0; i < categories.size(); i++) {
        	categoryTilesSize = categories.get(i).tiles.size();
            for (int j = 0; j < categoryTilesSize; j++) {
                Tile tile = categories.get(i).tiles.get(j);
                
                // Modify by zhudaopeng at 2016-11-04 Start
                // if(j == categoryTilesSize-1){
                // 	tile.isLastTile = true;
                // }
                // Modify by zhudaopeng at 2016-11-04 End

                if (!mContext.getPackageName().equals(
                        tile.intent.getComponent().getPackageName())) {
                    // If this drawable is coming from outside Settings, tint it to match the
                    // color.

                    /*prize modify for add other app entry by zhudaopeng 2017-01-11 start*/
                    // tile.icon.setTint(tintColor.data);
                    /*prize modify for add other app entry by zhudaopeng 2017-01-11 end*/
                }
            }
        }
        recountItems();
    }

    public void setConditions(List<Condition> conditions) {
        mConditions = conditions;
        recountItems();
    }

    public boolean isShowingAll() {
        return mIsShowingAll;
    }

    public void notifyChanged(Tile tile) {
        notifyDataSetChanged();
    }

    public void setShowingAll(boolean showingAll) {
        mIsShowingAll = showingAll;
        recountItems();
    }

    private void recountItems() {
        reset();
        // Modify by zhudaopeng at 2016-10-29 Start
        if(mIsShowSuggestions){
        	boolean hasConditions = false;
        	for (int i = 0; mConditions != null && i < mConditions.size(); i++) {
        		boolean shouldShow = mConditions.get(i).shouldShow();
        		hasConditions |= shouldShow;
        		countItem(mConditions.get(i), R.layout.condition_card, shouldShow, NS_CONDITION);
        	}

			boolean hasSuggestions = mSuggestions != null && mSuggestions.size() != 0;
			countItem(null, R.layout.dashboard_spacer, hasConditions && hasSuggestions, NS_SPACER);
			countItem(null, R.layout.suggestion_header, hasSuggestions, NS_SPACER);
			resetCount();
			if (mSuggestions != null) {
				int maxSuggestions = mSuggestionMode == SUGGESTION_MODE_DEFAULT
					? Math.min(DEFAULT_SUGGESTION_COUNT, mSuggestions.size())
							: mSuggestionMode == SUGGESTION_MODE_EXPANDED ? mSuggestions.size()
									: 0;
							for (int i = 0; i < mSuggestions.size(); i++) {
								countItem(mSuggestions.get(i), R.layout.suggestion_tile, i < maxSuggestions,
										NS_SUGGESTION);
							}
			}
			countItem(null, R.layout.dashboard_spacer, true, NS_SPACER);
		}
		// Modify by zhudaopeng at 2016-10-29 End

        resetCount();
		countItem(null, R.layout.settings_search_view, true, NS_ITEMS);
        for (int i = 0; mCategories != null && i < mCategories.size(); i++) {
            DashboardCategory category = mCategories.get(i);
            countItem(category, R.layout.dashboard_category, mIsShowingAll, NS_ITEMS);
            for (int j = 0; j < category.tiles.size(); j++) {
                Tile tile = category.tiles.get(j);
			containertitle.add(tile);
                countItem(tile, R.layout.dashboard_tile, false, NS_ITEMS);
            }
        }
        notifyDataSetChanged();
    }

    private void resetCount() {
        mId = 0;
    }

    private void reset() {
		checkItems.clear();
		containertitle.clear();
        mItems.clear();
        mTypes.clear();
        mIds.clear();
        mId = 0;
    }

    private void countItem(Object object, int type, boolean add, int nameSpace) {
        if (add) {
            mItems.add(object);
            mTypes.add(type);
            // TODO: Counting namespaces for handling of suggestions/conds appearing/disappearing.
            mIds.add(mId + nameSpace);
        }
        mId++;
    }

    @Override
    public DashboardItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DashboardItemHolder(LayoutInflater.from(parent.getContext()).inflate(
                viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(DashboardItemHolder holder, int position) {
        switch (mTypes.get(position)) {
			case R.layout.settings_search_view:
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						((SettingsActivity) mContext).getMenuItem().expandActionView();
					}
				});
				
				break;
            case R.layout.dashboard_category:
                onBindCategory(holder, (DashboardCategory) mItems.get(position),position);
                break;
            case R.layout.dashboard_tile:
                final Tile tile = (Tile) mItems.get(position);
                onBindTile(holder, tile);
                
                holder.itemView.setTag(tile);
                holder.itemView.setOnClickListener(this);
                break;
            case R.layout.suggestion_header:
            	// Modify by zhudaopeng at 2016-10-29 Start
            	if(mIsShowSuggestions){
            		onBindSuggestionHeader(holder);	
            	}
                break;
                // Modify by zhudaopeng at 2016-10-29 End
            case R.layout.suggestion_tile:
            	// Modify by zhudaopeng at 2016-10-29 Start
            	if(mIsShowSuggestions){
            		final Tile suggestion = (Tile) mItems.get(position);
            		onBindTile(holder, suggestion);
            		holder.itemView.setOnClickListener(new View.OnClickListener() {
            			@Override
            			public void onClick(View v) {
            				MetricsLogger.action(mContext, MetricsEvent.ACTION_SETTINGS_SUGGESTION,
            						DashboardAdapter.getSuggestionIdentifier(mContext, suggestion));
            				((SettingsActivity) mContext).startSuggestion(suggestion.intent);
            			}
            		});
            		holder.itemView.findViewById(R.id.overflow).setOnClickListener(
            				new View.OnClickListener() {
            					@Override
            					public void onClick(View v) {
            						showRemoveOption(v, suggestion);
            					}
            				});
            	}
            	// Modify by zhudaopeng at 2016-10-29 End
                break;
            case R.layout.see_all:
                onBindSeeAll(holder);
                break;
            case R.layout.condition_card:
                ConditionAdapterUtils.bindViews((Condition) mItems.get(position), holder,
                        mItems.get(position) == mExpandedCondition, this,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onExpandClick(v);
                            }
                        });
                break;
        }
    }

    private void showRemoveOption(View v, final Tile suggestion) {
        PopupMenu popup = new PopupMenu(
                new ContextThemeWrapper(mContext, R.style.Theme_AppCompat_DayNight), v);
        popup.getMenu().add(R.string.suggestion_remove).setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MetricsLogger.action(mContext, MetricsEvent.ACTION_SETTINGS_DISMISS_SUGGESTION,
                        DashboardAdapter.getSuggestionIdentifier(mContext, suggestion));
                disableSuggestion(suggestion);
                mSuggestions.remove(suggestion);
                recountItems();
                return true;
            }
        });
        popup.show();
    }

    public void disableSuggestion(Tile suggestion) {
        if (mSuggestionParser == null) {
            return;
        }
        if (mSuggestionParser.dismissSuggestion(suggestion)) {
            mContext.getPackageManager().setComponentEnabledSetting(
                    suggestion.intent.getComponent(),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            mSuggestionParser.markCategoryDone(suggestion.category);
        }
    }

    private void onBindSuggestionHeader(final DashboardItemHolder holder) {
        holder.icon.setImageResource(hasMoreSuggestions() ? R.drawable.ic_expand_more
                : R.drawable.ic_expand_less);
        holder.title.setText(mContext.getString(R.string.suggestions_title, mSuggestions.size()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasMoreSuggestions()) {
                    mSuggestionMode = SUGGESTION_MODE_EXPANDED;
                } else {
                    mSuggestionMode = SUGGESTION_MODE_COLLAPSED;
                }
                recountItems();
            }
        });
    }

    private boolean hasMoreSuggestions() {
        return mSuggestionMode == SUGGESTION_MODE_COLLAPSED
                || (mSuggestionMode == SUGGESTION_MODE_DEFAULT
                && mSuggestions.size() > DEFAULT_SUGGESTION_COUNT);
    }  

    private void onBindTile(DashboardItemHolder holder, Tile tile) {
        holder.icon.setImageDrawable(mCache.getIcon(tile.icon));
        holder.title.setText(tile.title);
        
        // Modify by zhudaopeng at 2016-11-04 Start
//        if (!TextUtils.isEmpty(tile.summary)) {
//            holder.summary.setText(tile.summary);
//            holder.summary.setVisibility(View.VISIBLE);
//        } else {
//            holder.summary.setVisibility(View.GONE);
//        }
        holder.summary.setVisibility(View.GONE);
        
        if(isAirPlaneTile(tile)){
        	holder.dashboardSwitch.setVisibility(View.VISIBLE);
        	holder.dashboardSwitch.setChecked(WirelessUtils.isAirplaneModeOn(mContext));
        	holder.arrowReminders.setVisibility(View.GONE);
        } else {
        	holder.dashboardSwitch.setVisibility(View.GONE);
        	holder.arrowReminders.setVisibility(View.VISIBLE);
        }
        
        // if(tile.isLastTile){
        // 	Log.d(TAG, "onBindTile() Tile Name = " + tile.title+" is Last Tile");
        // 	holder.divider.setVisibility(View.GONE);
        // } else {
        // 	holder.divider.setVisibility(View.VISIBLE);
        // }
        // Modify by zhudaopeng at 2016-11-04 End
    }

    private void onBindCategory(DashboardItemHolder holder, DashboardCategory category,int position) {
        holder.title.setText(category.title);
		String prizeSystemUpdateTitle = mContext.getResources().getString(R.string.system_update_settings_title);
		if(!checkItems.contains(category.title)){
			holder.dashboardCategory.removeAllViews();
			 for (int j = 0; j < category.tiles.size(); j++) {
				Tile tile = category.tiles.get(j);
				View view = LayoutInflater.from(mContext).inflate(R.layout.dashboard_tile,null,false);
				((ImageView)view.findViewById(android.R.id.icon)).setImageDrawable(mCache.getIcon(tile.icon));
				
				
				((TextView)view.findViewById(android.R.id.title)).setText(tile.title);
				 
            /* prize-add-fota notifycation from settings-lijimeng-20170427-start*/	
				 if(prizeSystemUpdateTitle != null && prizeSystemUpdateTitle.equals(tile.title)){
					PrizeNotificationImageView imageView = (PrizeNotificationImageView )view.findViewById(R.id.notification_imageview);
					boolean prizeSystemUpdateStatus = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_SYSTEM_UPDATE_CHANGE, 0) == 1;
					if(imageView != null && prizeSystemUpdateStatus){
						imageView.setVisibility(View.VISIBLE);
					}else if(imageView != null){
						imageView.setVisibility(View.GONE);
					}
				 }
            /* prize-add-fota notifycation from settings-lijimeng-20170427-end*/
				
				if(isAirPlaneTile(tile)){
					dashboardSwitch = (Switch) view.findViewById(R.id.dashboard_switch);
					
					arrowReminders = (ImageView) view.findViewById(R.id.im_arrow_reminders);
					dashboardSwitch.setVisibility(View.VISIBLE);
					dashboardSwitch.setChecked(WirelessUtils.isAirplaneModeOn(mContext));
					arrowReminders.setVisibility(View.GONE);
				} else {
					dashboardSwitch = (Switch) view.findViewById(R.id.dashboard_switch);
					arrowReminders = (ImageView) view.findViewById(R.id.im_arrow_reminders);
					dashboardSwitch.setVisibility(View.GONE);
					arrowReminders.setVisibility(View.VISIBLE);
				}
				if(isDevelopmentTile(tile) && !isShowDev()){
					view.setVisibility(View.GONE);
					continue;
				}
				if(j == category.tiles.size()-1){
					View titleDivider = view.findViewById(R.id.tile_divider);
					titleDivider.setVisibility(View.GONE);
				}
				view.setTag(tile);
				view.setOnClickListener(this);
				holder.dashboardCategory.addView(view);
             }
			checkItems.add(category.title);
		} 
        
    }

    private void onBindSeeAll(DashboardItemHolder holder) {
        holder.title.setText(mIsShowingAll ? R.string.see_less
                : R.string.see_all);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShowingAll(!mIsShowingAll);
            }
        });
    }
    
    private boolean isAirPlaneTile(Tile tile){
    	return tile.title.equals(mContext.getResources().getString(R.string.airplane_mode));
    } 
	private boolean isShowDev(){
		final UserManager um = UserManager.get(mContext);
		SharedPreferences mDevelopmentPreferences = ((SettingsActivity) mContext).getSharedPreferences(DevelopmentSettings.PREF_FILE,Context.MODE_PRIVATE);
    	final boolean showDev = mDevelopmentPreferences.getBoolean(DevelopmentSettings.PREF_SHOW, android.os.Build.TYPE.equals("eng"))&& !um.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES);
		return showDev;
    } 
	private boolean isDevelopmentTile(Tile tile){
    	return tile.title.equals(mContext.getResources().getString(R.string.development_settings_title)) || tile.title.equals(mContext.getResources().getString(com.android.settingslib.R.string.development_settings_title));
    }

    @Override
    public long getItemId(int position) {
        return mIds.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mTypes.get(position);
    }

    @Override
    public int getItemCount() {
        return mIds.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dashboard_tile) {
        	Tile tile = (Tile) v.getTag();
        	if(isAirPlaneTile(tile)){
        		Log.d(TAG, "onClick() Click:Air Plane Mode");
        		Switch mSwitch = (Switch) v.findViewById(R.id.dashboard_switch);
        		boolean isChecked = mSwitch.isChecked();
        		Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
        				!isChecked ? 1 : 0);

        		// Post the intent
        		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        		intent.putExtra("state", !isChecked);
        		mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        		mSwitch.setChecked(!isChecked);	
        	} else {
        		((SettingsActivity) mContext).openTile(tile);
        	}
            return;
        }
        if (v.getTag() == mExpandedCondition) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_SETTINGS_CONDITION_CLICK,
                    mExpandedCondition.getMetricsConstant());
            mExpandedCondition.onPrimaryClick();
        } else {
            mExpandedCondition = (Condition) v.getTag();
            MetricsLogger.action(mContext, MetricsEvent.ACTION_SETTINGS_CONDITION_EXPAND,
                    mExpandedCondition.getMetricsConstant());
            notifyDataSetChanged();
        }
    }

    public void onExpandClick(View v) {
        if (v.getTag() == mExpandedCondition) {
            MetricsLogger.action(mContext, MetricsEvent.ACTION_SETTINGS_CONDITION_COLLAPSE,
                    mExpandedCondition.getMetricsConstant());
            mExpandedCondition = null;
        } else {
            mExpandedCondition = (Condition) v.getTag();
            MetricsLogger.action(mContext, MetricsEvent.ACTION_SETTINGS_CONDITION_EXPAND,
                    mExpandedCondition.getMetricsConstant());
        }
        notifyDataSetChanged();
    }

    public Object getItem(long itemId) {
        for (int i = 0; i < mIds.size(); i++) {
            if (mIds.get(i) == itemId) {
                return mItems.get(i);
            }
        }
        return null;
    }

    public static String getSuggestionIdentifier(Context context, Tile suggestion) {
        String packageName = suggestion.intent.getComponent().getPackageName();
        if (packageName.equals(context.getPackageName())) {
            // Since Settings provides several suggestions, fill in the class instead of the
            // package for these.
            packageName = suggestion.intent.getComponent().getClassName();
        }
        return packageName;
    }

    private static class IconCache {

        private final Context mContext;
        private final ArrayMap<Icon, Drawable> mMap = new ArrayMap<>();

        public IconCache(Context context) {
            mContext = context;
        }

        public Drawable getIcon(Icon icon) {
            Drawable drawable = mMap.get(icon);
            if (drawable == null) {
                drawable = icon.loadDrawable(mContext);
                mMap.put(icon, drawable);
            }
            return drawable;
        }
    }

    public static class DashboardItemHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView title;
        public final TextView summary;
        public final ImageView arrowReminders;
        public final Switch dashboardSwitch;
        public final View divider;
        public final LinearLayout dashboardCategory;
        public final LinearLayout cardview;

        public DashboardItemHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(android.R.id.icon);
            title = (TextView) itemView.findViewById(android.R.id.title);
            summary = (TextView) itemView.findViewById(android.R.id.summary);
            
            arrowReminders = (ImageView) itemView.findViewById(R.id.im_arrow_reminders);
            dashboardSwitch = (Switch) itemView.findViewById(R.id.dashboard_switch);
            divider = (View) itemView.findViewById(R.id.tile_divider);
            dashboardCategory = (LinearLayout)itemView.findViewById(R.id.contain_dashboard_title);
            cardview = (LinearLayout)itemView.findViewById(R.id.serach_all);
			
        }
    }
}
