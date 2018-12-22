package com.prize.lockscreen.utils;

import com.prize.prizelockscreen.R;

import android.util.SparseArray;
import android.view.View;
/***
 * listView中用的viewHolder
 * @author Administrator
 *
 */
public class ViewHolder {
   
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag(R.id.icon);
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(R.id.icon, viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
