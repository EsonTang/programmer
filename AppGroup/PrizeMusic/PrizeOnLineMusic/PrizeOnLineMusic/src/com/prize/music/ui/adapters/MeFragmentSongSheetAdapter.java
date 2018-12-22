package com.prize.music.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.MainActivity;
import com.prize.music.database.DatabaseConstant;
import com.prize.music.database.ListInfo;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.views.ParabolaView;
import com.prize.music.R;
import com.xiami.sdk.utils.ImageUtil;

public class MeFragmentSongSheetAdapter extends BaseAdapter {

	private Context mContext;
	private int layoutId;
	private LayoutInflater inflater;
	private List<ListInfo> mLists = new ArrayList<ListInfo>();

	/** 存储每个条目勾选的状态 */
	protected SparseBooleanArray mCheckedStates = null;
	private boolean isSelectAll = false;
	private boolean isSelectMode = false;
	private ParabolaView parabolaView;
	public MeFragmentSongSheetAdapter(FragmentActivity context, int layoutId) {
		this.mContext = context;
		this.layoutId = layoutId;
		inflater = LayoutInflater.from(context);
		mCheckedStates = new SparseBooleanArray();
		ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
		parabolaView = (ParabolaView) rootView.findViewById(R.id.parabolaView1);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mLists.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mLists.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// final View view = super.getView(position, convertView, parent);
		GridViewHolder holder = null;
		if (convertView == null) {
			holder = new GridViewHolder();
			convertView = inflater.inflate(layoutId, null);
			holder.title = (TextView) convertView.findViewById(R.id.edit_title);
			holder.img = (ImageView) convertView.findViewById(R.id.eidt_img);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.edit_check);
			holder.checkbox.setVisibility(View.GONE);
			holder.play_iv = (ImageView) convertView.findViewById(R.id.play_iv);
			holder.icon_fly = (ImageView) convertView.findViewById(R.id.icon_fly);
			convertView.setTag(holder);
		} else {
			holder = (GridViewHolder) convertView.getTag();
		}
		final ListInfo bean = mLists.get(position);
		holder.img.setImageBitmap(BitmapFactory.decodeResource(
				mContext.getResources(), R.drawable.new_list));
		holder.title.setText(bean.menuName);
		if (isSelectMode) {
			holder.checkbox.setVisibility(View.VISIBLE);
		}
		if (mCheckedStates.get(position)) {
			holder.checkbox.setChecked(true);
		} else {
			holder.checkbox.setChecked(false);
		}

		// if(!isResponse){
		if (!TextUtils.isEmpty(mLists.get(position).source_type)
				&& mLists.get(position).source_type
						.equals(DatabaseConstant.ONLIEN_TYPE)) {
			String url = ImageUtil.transferImgUrl(bean.menuImageUrl, 220);
			ImageLoader.getInstance().displayImage(url, holder.img,
					UILimageUtil.getTwoOneZeroDpLoptions());
			
			if (isSelectMode) {
				holder.play_iv.setVisibility(View.GONE);
			} else {
				holder.play_iv.setVisibility(View.VISIBLE);
			}
		} else {
			if (!TextUtils.isEmpty(bean.menuImageUrl)) {
				if (bean.menuImageUrl.startsWith("http")) {
					String url = ImageUtil.transferImgUrl(bean.menuImageUrl,
							220);
					ImageLoader.getInstance().displayImage(url, holder.img,
							UILimageUtil.getTwoOneZeroDpLoptions());
				} else {
					ImageLoader.getInstance().displayImage(
							"file://" + bean.menuImageUrl, holder.img,
							UILimageUtil.getTwoOneZeroDpLoptions());
				}
			} else {
				holder.img.setImageResource(R.drawable.bg_two_one_zero_dp);
			}
			holder.play_iv.setVisibility(View.GONE);
		}


		if (position == (mLists.size() - 1) && !isSelectMode) {
			holder.img.setImageResource(R.drawable.icon_add_list);
			holder.play_iv.setVisibility(View.GONE);
		}
		final ImageView flyView=holder.icon_fly;
		holder.play_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(ClientInfo.networkType==ClientInfo.NONET){
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				String type=bean.menuType;
				try {
					MusicUtils.playOnLineSheet(
							mContext,
							String.valueOf(bean.menuId),
							bean.menuType);
				} catch (Exception e) {

					e.printStackTrace();

				}
				if (parabolaView != null) {
					ImageView bottomView = null;
					if (mContext instanceof MainActivity) {
						bottomView = ((MainActivity) mContext)
								.getBottomView();
					}
					parabolaView.setAnimationPara(flyView,
							bottomView);
					if (!parabolaView.isRunning()) {
						parabolaView.showMovie();
					}
				}

			}
				
		});
		return convertView;
	}

	/**
	 * 改变指定位置条目的选择状态，如果已经处于勾选状态则取消勾选，如果处于没有勾选则勾选
	 * 
	 * @param position
	 *            要改变的条目选择状态的位置
	 */
	public void toggleCheckedState(int position) {
		if (position >= 0 && position < getCount()) {
			if (mCheckedStates.get(position)) {
				mCheckedStates.put(position, false);
			} else {
				mCheckedStates.put(position, true);
			}
//			notifyDataSetChanged();
		}

	}

	/**
	 * 全选或全不选
	 *
	 * @param selectAll
	 *            true表示全选,false表示全不选
	 */
	public void selectAllItem(boolean selectAll) {
		if (selectAll) {
			for (int i = 0; i < getCount(); i++) {
				mCheckedStates.put(i, true);
			}
			isSelectAll = true;
		} else {
			for (int i = 0; i < getCount(); i++) {
				mCheckedStates.put(i, false);
			}
			isSelectAll = false;
		}
		notifyDataSetChanged();
	}

	public boolean isSelectAll() {
		return isSelectAll;
	}

	public boolean isSelectMode() {
		return isSelectMode;
	}

	/**
	 * 获得已选择的条目们在列表中的位置
	 * 
	 * @return 所有已选择的条目在列表中的位置
	 */
	public int[] getSelectedItemPositions() {
		int count = 0;
		for (int i = 0; i < getCount(); i++) {
			if (mCheckedStates.get(i)) {
				count++;
			}
		}
		int[] checkedPostions = new int[count];
		for (int i = 0, j = 0; i < getCount(); i++) {
			if (mCheckedStates.get(i)) {
				checkedPostions[j] = i;
				j++;
			}
		}
		return checkedPostions;
	}

	public List<ListInfo> getSelectedAudioIds() {
		int[] checkedPostions = getSelectedItemPositions();
		List<ListInfo> array_list = new ArrayList<ListInfo>();
		int len = checkedPostions.length;
		for (int i = 0; i < len; i++) {
			array_list.add((ListInfo) (getItem(checkedPostions[i])));
		}
		return array_list;
	}

	public void setIsSelectAll(boolean selectAll) {
		isSelectAll = selectAll;
	}

	public void setSelectMode(boolean isSelectMode) {
		this.isSelectMode = isSelectMode;
		notifyDataSetChanged();
	}

	// public final class AsyncImageTask extends AsyncTask<ImageView, Void,
	// Bitmap> {
	// ImageView mView;
	// protected Bitmap doInBackground(ImageView... views) {
	// String url = null ;
	// ImageView view = views[0];
	// mView = view;
	// if(view.getTag() != null){
	// url = (String) view.getTag();
	// }
	// Bitmap bitmap = LoadBitmapFromWebOperations(url);
	// return bitmap;
	// }
	// protected void onPostExecute(Bitmap bm) {
	// if(bm != null)
	// mView.setImageBitmap(bm);
	// }
	// }
	//
	// public Bitmap LoadBitmapFromWebOperations(String url) {
	// Bitmap d = null;
	// try {
	// URLConnection conn = new URL(url).openConnection();
	// conn.setConnectTimeout(20*1000);
	// if(((HttpURLConnection) conn).getResponseCode() == 200){
	// InputStream inputStream = conn.getInputStream();
	// d = BitmapFactory.decodeStream(inputStream);
	// inputStream.close();
	// }
	// } catch (Exception e) {
	// System.out.println("Exc=" + e);
	// return null;
	// }
	// return d;
	// }

	public final class GridViewHolder {
		public TextView title;
		public ImageView img;
		public CheckBox checkbox;
		public ImageView play_iv;
		public ImageView icon_fly;
	}

	/**
	 * @see 是否尾部添加，true则直接添加到List里，false则先清除List
	 * @param lists
	 * @param isAppend
	 */
	public void addList(List<ListInfo> lists, boolean isAppend) {
		if (isAppend) {
			mLists.addAll(lists);
			notifyDataSetChanged();
		} else {
			mLists.clear();
			mLists.addAll(lists);
			notifyDataSetChanged();

		}

	}

}
