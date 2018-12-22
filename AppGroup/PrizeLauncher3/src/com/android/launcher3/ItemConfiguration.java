package com.android.launcher3;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.android.launcher3.CellLayout.AlignmentState;
import com.android.launcher3.CellLayout.LayoutParams;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ItemConfiguration {
        HashMap<View, CellAndSpan> map = new HashMap<View, CellAndSpan>();
        private HashMap<View, CellAndSpan> savedMap = new HashMap<View, CellAndSpan>();
          ArrayList<View> sortedViews = new ArrayList<View>();
        boolean isSolution = false;
        int dragViewX, dragViewY, dragViewSpanX, dragViewSpanY;
		ArrayList<Entry<View, CellAndSpan>> items;

        void save() {
            // Copy current state into savedMap
            for (View v: map.keySet()) {
                map.get(v).copy(savedMap.get(v));
            }
        }
        public  HashMap<View, CellAndSpan> get() {
        	return map;
        }
        void restore() {
            // Restore current state from savedMap
            for (View v: savedMap.keySet()) {
                savedMap.get(v).copy(map.get(v));
            }
        }

        void add(View v, CellAndSpan cs) {
            map.put(v, cs);
            savedMap.put(v, new CellAndSpan());
            sortedViews.add(v);
        }
        
        
	public void resort(final int mCountX, final AlignmentState state) {
		  if (items == null) {
			  items = new ArrayList<HashMap.Entry<View, CellAndSpan>>(
					map.entrySet());
		  }
	
		Collections.sort(items, new Comparator<HashMap.Entry<View, CellAndSpan>>() {
			@Override
			public int compare(HashMap.Entry<View, CellAndSpan> lhs,
					HashMap.Entry<View, CellAndSpan> rhs) {
				CellAndSpan rhsCel =rhs.getValue();
				CellAndSpan lhsCel =lhs.getValue();
				if (state == AlignmentState.UP)
					return (int) (lhsCel.pos - rhsCel.pos);
				else {

					return (int) (-lhsCel.pos + rhsCel.pos);
				}
			}
		});  
		
	}
        
        public void print() {
        	  for (View v: map.keySet()) {
        		  CellAndSpan c=  map.get(v);
                  int x=c.x;
                  int y = c.y;
                  if (v != null) {
                	  if (v instanceof TextView) {
                          String title = (String) ((TextView)v).getText();
                          Log.i("zhouerlong", "print:cellANdSpanInfo-- "+title+"--["+x+","+y+"]");
                	  }
                  }
              }
        }
        @Override
		public String toString() {
			return "ItemConfiguration [map=" + map + ", savedMap=" + savedMap
					+ "]";
		}
		int area() {
            return dragViewSpanX * dragViewSpanY;
        }
    }