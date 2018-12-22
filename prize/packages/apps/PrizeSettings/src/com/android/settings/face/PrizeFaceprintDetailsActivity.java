package com.android.settings.face;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.face.utils.FaceXmlData;
import com.android.settings.face.utils.SaveListUtil;
import com.android.settings.face.utils.SpUtil;
import com.android.settings.fingerprint.PrizeFpCustomEditText;
import com.android.settings.fingerprint.PrizeFpOperationDialogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/10/18.
 */

public class PrizeFaceprintDetailsActivity extends Activity implements View.OnClickListener {

    private ActionBar mActionBar;
    private Button renameTv, deletetv;
    private TextView facedetailsName;
    private int mIndex;
    private String mUserName;

    private Dialog mRenameDialog;
    private Dialog mDeleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_SubSettings);

        setContentView(R.layout.prize_faceprint_details_activity);

        mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        setTitle(getText(R.string.faceid));

        renameTv = (Button) findViewById(R.id.rename_fa_button);
        deletetv = (Button) findViewById(R.id.delete_fa_button);
        facedetailsName = (TextView) findViewById(R.id.face_details_name);

        renameTv.setOnClickListener(this);
        deletetv.setOnClickListener(this);

        Intent intent = getIntent();
        mIndex = intent.getIntExtra("face_index", 5);
        mUserName = intent.getStringExtra("face_name");

        facedetailsName.setText(mUserName);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rename_fa_button:
                mRenameDialog = PrizeFpOperationDialogUtils.createDoubleButtonEditDialog(this, getString(R.string.prize_fa_dialog_title_edit_face), mUserName, mRenameConfirmClick, mRenameCancelClick);
                mRenameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0,
                                        InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                        }, 100);
                    }
                });
                mRenameDialog.show();
                break;
            case R.id.delete_fa_button:
                String notice = getString(R.string.prize_face_dialog_do_you_want_to_remove) + mUserName + "?";
                mDeleteDialog = PrizeFpOperationDialogUtils.createDoubleButtonTextDialog(
                        this, getString(R.string.prize_fp_operation_prompt), notice, mDeleteConfirmClick, mDeleteCancelClick);
                break;
            default:
                break;
        }
    }

    private OnClickListener mRenameConfirmClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PrizeFpCustomEditText renameView = (PrizeFpCustomEditText) mRenameDialog.findViewById(R.id.content_text_edit);
            String newName = renameView.getText().toString();

            //List<String> facelist = SaveListUtil.getList(PrizeFaceprintDetailsActivity.this);
            FaceBean faceBean = FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH);
            List<String> namelist = new ArrayList<String>();
            for (int i = 0; i < faceBean.faceSize; i++) {
                //namelist.add((String) SpUtil.getData(PrizeFaceprintDetailsActivity.this, facelist.get(i), ""));
                namelist.add(faceBean.face_name);
            }
            boolean isRepeated = false;
            for (String str : namelist) {
                if (str.equals(newName)) {
                    isRepeated = true;
                    break;
                }
            }

            if (null == newName || newName.length() == 0) {
                renameView.setText(mUserName);
                renameView.setSelection(mUserName.length());
                Toast.makeText(PrizeFaceprintDetailsActivity.this, R.string.prize_fingeprint_name_cannot_empty, Toast.LENGTH_SHORT).show();
                return;
            } else if (isRepeated) {
                renameView.setText(mUserName);
                renameView.setSelection(mUserName.length());
                Toast.makeText(PrizeFaceprintDetailsActivity.this, R.string.prize_fingeprint_name_cannot_repeated, Toast.LENGTH_SHORT).show();
                return;
            } else {
                facedetailsName.setText(newName);
                //SpUtil.saveData(PrizeFaceprintDetailsActivity.this, facelist.get(mIndex), newName);
                faceBean.face_name = newName;
                FaceXmlData.writeCheckRootDataFile(faceBean,FaceXmlData.FACEBEAN_PHTH);
                mUserName = newName;
                mRenameDialog.dismiss();
            }
        }
    };

    private OnClickListener mRenameCancelClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mRenameDialog.dismiss();
        }
    };

    private OnClickListener mDeleteConfirmClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            /*List<String> facelist = SaveListUtil.getList(PrizeFaceprintDetailsActivity.this);
            facelist.remove(mIndex);
            SaveListUtil.saveList(PrizeFaceprintDetailsActivity.this, facelist)*/;
            FaceBean faceBean = FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH);
            faceBean.faceSize = 0;
            FaceXmlData.writeCheckRootDataFile(faceBean,FaceXmlData.FACEBEAN_PHTH);
            mDeleteDialog.dismiss();
            PrizeFaceprintDetailsActivity.this.finish();
        }
    };

    private OnClickListener mDeleteCancelClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mDeleteDialog.dismiss();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
