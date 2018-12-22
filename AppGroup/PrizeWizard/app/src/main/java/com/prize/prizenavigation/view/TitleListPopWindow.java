package com.prize.prizenavigation.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.prize.prizenavigation.R;
import com.prize.prizenavigation.adapter.TitleListAdapter;
import com.prize.prizenavigation.bean.NaviDatas;

import java.util.ArrayList;
import java.util.List;

/**
 *自定义popwindow控件
 *
 * Created by liukun on 2017/3/6.
 */
public class TitleListPopWindow extends PopupWindow {
    /**上下文对象*/
    private Context mContext;
    /**回调接口对象*/
    private OnPopupWindowClickListener listener;
    /**TitleListAdapter对象*/
    private TitleListAdapter adapter;
    /**ListView的数据源*/
    private List<NaviDatas.ListBean> datas = new ArrayList<NaviDatas.ListBean>();

    public TitleListPopWindow(Context context){
        super(context);
        mContext = context;
        initView();
    }

    private void initView(){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.fragment_list_layout, null);
        setContentView(popupView);

        //设置宽度,若没有设置宽度为LayoutParams.WRAP_CONTENT
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);

        //设置动画，也可以不设置。不设置则是显示默认的
//        setAnimationStyle(R.style.popupwindow_animation);

        //此处注释 因为外部得不到pop消失时点击事件
//        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        //此处设置为flase 让外部来控制是否消失
        this.setOutsideTouchable(false);
        ListView listView = (ListView) popupView.findViewById(R.id.fra_title_lv);
        adapter = new TitleListAdapter(datas);
        listView.setAdapter(adapter);

        //ListView的点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TitleListPopWindow.this.dismiss();
                if(listener != null){
                    listener.onPopupWindowItemClick(position);
                }
            }
        });

    }

    /**
     * 为PopupWindow设置回调接口
     * @param listener
     */
    public void setOnPopupWindowClickListener(OnPopupWindowClickListener listener){
        this.listener = listener;
    }


    /**
     * 设置数据的方法，供外部调用
     * @param mList
     */
    public void changeData(List<NaviDatas.ListBean> mList) {
        //这里用addAll也很重要，如果用this.list = mList，调用notifyDataSetChanged()无效
        //notifyDataSetChanged()数据源发生改变的时候调用的，this.list = mList，list并没有发送改变
        if (mList!=null&&mList.size()>0) {
            datas.clear();
            datas.addAll(mList);
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * 回调接口.供外部调用
     *
     */
    public interface OnPopupWindowClickListener{
        /**
         * 当点击PopupWindow的ListView 的item的时候调用此方法，用回调方法的好处就是降低耦合性
         * @param position 位置
         */
        void onPopupWindowItemClick(int position);
    }

}

