package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.qihoo.cleandroid.sdk.TrashClearUtils;
import com.prize.qihoo.lib.atv.model.TreeNode;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;

public class CategoryHolder extends TreeNode.BaseNodeViewHolder<TrashClearCategory> {
	private Context mContext;
	
    public CategoryHolder(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	public View createNodeView(TreeNode node, TrashClearCategory category) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.clear_trash_clear_list_cate_item, null, false);
 
        TextView groupName = (TextView) view.findViewById(R.id.group_title);
        final ImageView arrow = (ImageView)view.findViewById(R.id.arrow_iv);
        final View divider = view.findViewById(R.id.divider_d7d7d7);
        TextView size = (TextView) view.findViewById(R.id.size_tv);
        
        switch (category.type) {
            case TrashClearEnv.CATE_SYSTEM:
                category.desc = mContext.getString(R.string.clear_sdk_trash_system);
                break;
            case TrashClearEnv.CATE_CACHE:
                category.desc = mContext.getString(R.string.clear_sdk_trash_cache);
                break;
            case TrashClearEnv.CATE_ADPLUGIN:
                category.desc = mContext.getString(R.string.clear_sdk_trash_adplugin);
                break;
            case TrashClearEnv.CATE_APK:
                category.desc = mContext.getString(R.string.clear_sdk_trash_apk);
                break;
            case TrashClearEnv.CATE_UNINSTALLED:
                category.desc = mContext.getString(R.string.clear_sdk_trash_uninstalled);
                break;
            case TrashClearEnv.CATE_BIGFILE:
                category.desc = mContext.getString(R.string.clear_sdk_trash_bigfile);
                break;

            default:
                break;
        }

        node.setClickListener(new TreeNode.TreeNodeClickListener() {
            @Override
            public void onClick(TreeNode node, Object value) {

                //判断是否已经打开列表
                if(node.isExpanded()){
                    arrow.setImageResource(R.drawable.node_not_expanded);
                    divider.setVisibility(View.VISIBLE);
                }else{
                    arrow.setImageResource(R.drawable.node_expanded);
                    divider.setVisibility(View.GONE);
                }
            }
        });

        groupName.setText(category.desc);
        size.setText(TrashClearUtils.getHumanReadableSizeMore(category.size));

        return view;
    }
}
    
