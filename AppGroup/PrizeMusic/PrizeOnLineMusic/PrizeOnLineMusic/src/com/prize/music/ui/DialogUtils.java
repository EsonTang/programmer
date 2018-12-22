package com.prize.music.ui;

import java.util.List;
import java.util.Map;

import com.prize.music.R;
import com.prize.music.activities.EditActivity;
import com.prize.music.activities.NewListActivity;
import com.prize.music.helpers.utils.MusicUtils;
import com.prize.music.menu.PlaylistDialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore.Audio;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 对话框管理类
 * 
 * @author huanglingjun
 *
 */
public class DialogUtils implements OnClickListener {
	private Context context;
	private EditText dia_edit;
	private AlertDialog createDialog;

	private AlertDialog renameDialog;
	private EditText dia_rename_edit;
	private long renameId;

	public String lastTableName = null;
	List<Map<String, Object>> lists;

	public static int listCount = 1;

	public DialogUtils(Context context) {
		super();
		this.context = context;
	}

	// 新建列表
	public void createTbleDialog() {
		createDialog = new AlertDialog.Builder(context).create();
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.dialog_new_list, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Button dia_neg = (Button) view.findViewById(R.id.dia_neg);
		Button dia_sure = (Button) view.findViewById(R.id.dia_sure);
		dia_edit = (EditText) view.findViewById(R.id.dia_edit);

		dia_edit.setText("新建播放列表" + listCount);
		dia_neg.setOnClickListener(this);
		dia_sure.setOnClickListener(this);
		createDialog.setView(view);
		createDialog.show();
	}

	// 重命名列表
	public void renameTbleDialog(String orName,
			List<Map<String, Object>> lists, long renameId) {
		this.lists = lists;
		this.renameId = renameId;
		renameDialog = new AlertDialog.Builder(context).create();
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.dialog_rename, null);
		view.setBackgroundResource(R.drawable.icon_dialog);

		Button dia_rename_neg = (Button) view.findViewById(R.id.dia_rename_neg);
		Button dia_rename_sure = (Button) view
				.findViewById(R.id.dia_rename_sure);
		dia_rename_edit = (EditText) view.findViewById(R.id.dia_rename_edit);

		dia_rename_edit.setText(orName);
		dia_rename_neg.setOnClickListener(this);
		dia_rename_sure.setOnClickListener(this);
		renameDialog.setView(view);
		renameDialog.show();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.dia_sure:
			sure();
			break;
		case R.id.dia_neg:
			if (createDialog != null && createDialog.isShowing()) {
				createDialog.dismiss();
			}
			break;
		case R.id.dia_rename_sure:
			rename();
			break;
		case R.id.dia_rename_neg:
			if (renameDialog != null && renameDialog.isShowing()) {
				renameDialog.dismiss();
			}
			break;

		default:
			break;
		}
	}

	public void sure() {
		String name = dia_edit.getText().toString();
		if (name != null && name.length() > 0) {
			int id = idForplaylist(name);
			if (id >= 0) {
				MusicUtils.clearPlaylist(context, id);
				// MusicUtils.addToPlaylist(PlaylistDialog.this, mList, id);
			} else {
				long new_id = MusicUtils.createPlaylist(context, name);
				Intent intent = new Intent(context, NewListActivity.class);
				intent.putExtra("new_id", new_id);
				intent.putStringArrayListExtra("mListData", null);
				context.startActivity(intent);
				FragmentActivity activity = (FragmentActivity) context;
				activity.overridePendingTransition(R.anim.in_from_left,
						R.anim.out_to_right);
				listCount++;
			}
			if (createDialog != null && createDialog.isShowing()) {
				createDialog.dismiss();
			}
		}

	}

	public void rename() {
		String name = dia_rename_edit.getText().toString();
		if (name != null && name.length() > 0) {
			for (int i = 0; i < lists.size(); i++) {
				String tableName = (String) lists.get(i).get("name");
				if (tableName.equals(name)) {
					Toast.makeText(context, "列表已经存在", Toast.LENGTH_LONG).show();
				} else {
					MusicUtils.renamePlaylist(context, renameId, name);
					lastTableName = name;
				}
			}
			if (renameDialog != null && renameDialog.isShowing()) {
				renameDialog.dismiss();
			}
		}
	}

	public String getLastName() {
		return lastTableName;
	}

	private int idForplaylist(String name) {

		Cursor cursor = MusicUtils.query(context,
				Audio.Playlists.EXTERNAL_CONTENT_URI,
				new String[] { Audio.Playlists._ID }, Audio.Playlists.NAME
						+ "=?", new String[] { name }, Audio.Playlists.NAME, 0);
		int id = -1;
		if (cursor != null) {
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				id = cursor.getInt(0);
			}
			cursor.close();
		}

		return id;
	}
}
