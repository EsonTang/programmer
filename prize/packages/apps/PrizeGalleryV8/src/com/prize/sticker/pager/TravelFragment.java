/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker.pager;

import com.android.gallery3d.filtershow.PickStickerActivity;
import com.prize.sticker.DividerGridItemDecoration;
import com.prize.sticker.StickerAdapter;
import com.prize.sticker.StickerManager;
import com.prize.sticker.StickerTool;
import com.prize.sticker.WatermarkBean;
import com.prize.sticker.StickerAdapter.OnRecyclerItemClickLitener;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.GridLayoutManager;

public class TravelFragment extends Fragment {

	private RecyclerView mStickerRecycler;
    private StickerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.prize_pick_sticker_frame, container,
                false);
        return v;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mStickerRecycler = (RecyclerView) view.findViewById(R.id.id_recyclerview);
		// mStickerRecycler.setLayoutManager(new LinearLayoutManager(this));
		mStickerRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));
		mStickerRecycler.setAdapter(mAdapter = new StickerAdapter(getActivity(), StickerManager.getStickerManager().getWatermarkBeans(StickerTool.TYPE_WATERMARK_TRAVEL)));
//		mStickerRecycler.addItemDecoration(new DividerGridItemDecoration(getActivity()));
		mAdapter.setOnItemClickLitener(new OnRecyclerItemClickLitener() {
			@Override
			public void onItemClick(View view, int position) {
				WatermarkBean obj = (WatermarkBean) mAdapter.getItem(position);
				PickStickerActivity activity = (PickStickerActivity) getActivity();
				activity.setPick(obj);
			}

			@Override
			public void onItemLongClick(View view, int position) {

			}

		});
		super.onViewCreated(view, savedInstanceState);
	}

}

