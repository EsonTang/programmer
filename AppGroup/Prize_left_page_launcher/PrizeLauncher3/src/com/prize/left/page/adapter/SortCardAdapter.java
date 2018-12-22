package com.prize.left.page.adapter;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.bean.table.SelCardType;
import com.prize.left.page.helper.ItemTouchHelperAdapter;
import com.prize.left.page.helper.OnStartDragListener;
import com.prize.left.page.util.DBUtils;
/***
 * 卡片管理recycleView适配器
 * @author fanjunchen
 *
 */
public class SortCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> 
	implements ItemTouchHelperAdapter {

	private List<SelCardType> cards;

	private Context mContext;
	
	private View.OnClickListener clickListener;
	
	private final OnStartDragListener mDragStartListener;
	/**位置是否发生了变化*/
	private boolean isChange = false;
	
	private RefreshHandler mHandler;
	/**是否允许托运*/
	private boolean isCanDrag = true;
	
	public SortCardAdapter(Context context, List<SelCardType> cards, OnStartDragListener dragStartListener) {
		this.mContext = context;
		this.cards = cards;
		
		mDragStartListener = dragStartListener;
		
		mHandler = new RefreshHandler();
		mHandler.setAdapter(this);
	}
	
	
	public void setClickListener(View.OnClickListener cls) {
		clickListener = cls;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		// 给ViewHolder设置布局文件
		View v = null;
		v = LayoutInflater.from(mContext).inflate(
				R.layout.manage_card_item, viewGroup, false);
		return new SortViewHolder(v);
				
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
		// 给ViewHolder设置元素
		SelCardType p = cards.get(pos);
		final SortViewHolder nsHolder = (SortViewHolder)viewHolder;
		nsHolder.titleTxt.setText(p.name);
		nsHolder.imgDel.setTag(p);
		if (p.canDel) {
			nsHolder.imgDel.setOnClickListener(clickListener);
			nsHolder.imgDel.setEnabled(true);
		} else {
			nsHolder.imgDel.setOnClickListener(null);
			nsHolder.imgDel.setEnabled(false);
		}
		
		nsHolder.imgDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isCanDrag && MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                	isCanDrag = false;
                    mDragStartListener.onStartDrag(nsHolder);
                }
                return false;
            }
        });
	}

	@Override
	public int getItemCount() {
		// 返回数据总数
		return cards == null ? 0 : cards.size();
	}
	
	@Override
    public void onItemDismiss(int pos) {
		
		if (pos >= cards.size())
			return;
		
		SelCardType a = cards.remove(pos);
        notifyItemRemoved(pos);
        // 保存到数据库
        try {
			LauncherApplication.getDbManager().delete(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
        a = null;
        isChange = true;
    }
	/***
	 * 获取item对应的位置
	 * @param c
	 * @return
	 */
	public int getItemPos(SelCardType c) {
		if (null == cards || null == c)
			return -1;
		
		int sz = cards.size();
		for (int i=0; i<sz; i++) {
			if (c == cards.get(i))
				return i;
		}
		return -1;
	}
	/***
	 * 允许托运
	 */
	public void resetDragEnable() {
		// 保存到数据库
        DBUtils.updateSelCardSort(cards);
        if (min != max)
        	notifyItemRangeChanged(min, max);
        min = -1;
        max = 0;
		isCanDrag = true;
	}

	private int min = -1, max = 0;
    @Override
    public boolean onItemMove(int fromPos, int toPos) {
    	
    	if (fromPos >= cards.size()) {
    		fromPos = cards.size() - 1;
    	}
    	if (toPos >= cards.size()) {
    		toPos = cards.size() - 1;
    	}
    	if (fromPos == toPos)
    		return true;
    	
        Collections.swap(cards, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
        if (min == -1) {
        	min = Math.min(fromPos, toPos);
        }
        else {
        	min = Math.min(min, toPos);
        	min = Math.min(min, fromPos);
        }
        
        if (max == 0) {
        	max = Math.max(fromPos, toPos);
        }
        else {
        	max = Math.max(fromPos, max);
        	max = Math.max(max, toPos);
        }
        /*notifyItemChanged(fromPos);
        notifyItemChanged(toPos);
        int s = Math.min(fromPos, toPos);
        int e = Math.max(fromPos, toPos);
        notifyItemRangeChanged(s, e);
        
        if (mHandler != null) {
        	mHandler.removeMessages(RefreshHandler.MSG_REFRESH);
        	Message msg = mHandler.obtainMessage(RefreshHandler.MSG_REFRESH);
        	msg.arg1 = fromPos;
        	msg.arg2 = toPos;
        	mHandler.sendMessageDelayed(msg, 500);
        }*/
        
        isChange = true;
        return true;
    }
    
    public boolean isChange() {
    	return isChange;
    }
    
    public void resetChange() {
    	isChange = false;
    }
	
	static class SortViewHolder extends RecyclerView.ViewHolder {
		
		public TextView titleTxt;

		public ImageView imgDel;
		
		public ImageView imgDrag;
		
		public SortViewHolder(View v) {
			super(v);
			titleTxt = (TextView) v.findViewById(R.id.txt_card_title);
			imgDel = (ImageView) v.findViewById(R.id.img_del);
			imgDrag = (ImageView) v.findViewById(R.id.img_drag);
		}
	}
	
	static class RefreshHandler extends Handler {
		
		private WeakReference<SortCardAdapter> mModel;
		
		static final int MSG_REFRESH = 1;
		
		public void setAdapter(SortCardAdapter m) {
			mModel = new WeakReference<SortCardAdapter>(m);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case MSG_REFRESH:
					SortCardAdapter m = mModel.get();
					if (m != null) {
						m.notifyItemChanged(msg.arg1);
				        m.notifyItemChanged(msg.arg2);
					}
					break;
			}
		}
	}
}
