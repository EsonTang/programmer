package com.android.soundrecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;


/***
 * @prize fanjunchen 2015-05-14 
 * A simple dialog box, choose the 
 * @author fanjunchen
 *
 */
public class PrizeSelectDialogFragment extends DialogFragment implements
        View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "SR/SelectDialogFragment";
    private static final String KEY_ITEM_ARRAY = "itemArray";
    private static final String KEY_SUFFIX_ARRAY = "suffixArray";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DEFAULT_SELECT = "nowSelect";
    private static final String KEY_DEFAULT_SELECTARRAY = "nowSelectArray";
    private static final String KEY_SINGLE_CHOICE = "singleChoice";
    private View.OnClickListener mClickListener = null;
    private RadioGroup.OnCheckedChangeListener mMultiChoiceClickListener = null;

    private View mView = null;
    
    private LayoutInflater mInflate = null;
    /**
     * M: create a instance of SelectDialogFragment
     *
     * @param itemArrayID
     *            the resource id array of strings that show in list
     * @param sufffixArray
     *            the suffix array at the right of list item
     * @param titleID
     *            the resource id of title string
     * @param nowSelect
     *            the current select item index
     * @return the instance of SelectDialogFragment
     */
    public static PrizeSelectDialogFragment newInstance(int[] itemArrayID, CharSequence[] sufffixArray,
            int titleID, boolean singleChoice, int nowSelect, boolean[] nowSelectArray) {
        PrizeSelectDialogFragment frag = new PrizeSelectDialogFragment();
        Bundle args = new Bundle();
        args.putIntArray(KEY_ITEM_ARRAY, itemArrayID);
        args.putCharSequenceArray(KEY_SUFFIX_ARRAY, sufffixArray);
        args.putInt(KEY_TITLE, titleID);
        args.putBoolean(KEY_SINGLE_CHOICE, singleChoice);
        if (singleChoice) {
            args.putInt(KEY_DEFAULT_SELECT, nowSelect);
        } else {
            args.putBooleanArray(KEY_DEFAULT_SELECTARRAY, nowSelectArray.clone());
        }
        frag.setArguments(args);
        return frag;
    }

    /**
     * M: create a instance of SelectDialogFragment
     *
     * @param itemArrayID
     *            the resource id array of strings that show in list
     * @param sufffixArray
     *            the suffix array at the right of list item
     * @param titleID
     *            the resource id of title string
     * @param nowSelect
     *            the current select item index
     * @return the instance of SelectDialogFragment
     */
    public static PrizeSelectDialogFragment newInstance(String[] itemArrayString, CharSequence[] sufffixArray,
            int titleID, boolean singleChoice, int nowSelect, boolean[] nowSelectArray) {
        PrizeSelectDialogFragment frag = new PrizeSelectDialogFragment();
        Bundle args = new Bundle();
        //args.putIntArray(KEY_ITEM_ARRAY, itemArrayID);
        args.putStringArray(KEY_ITEM_ARRAY, itemArrayString);
        args.putCharSequenceArray(KEY_SUFFIX_ARRAY, sufffixArray);
        args.putInt(KEY_TITLE, titleID);
        args.putBoolean(KEY_SINGLE_CHOICE, singleChoice);
        if (singleChoice) {
            args.putInt(KEY_DEFAULT_SELECT, nowSelect);
        } else {
            args.putBooleanArray(KEY_DEFAULT_SELECTARRAY, nowSelectArray.clone());
        }
        frag.setArguments(args);
        return frag;
    }

    @Override
    /**
     * M: create a select dialog
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogUtils.i(TAG, "<onCreateDialog>");
        Bundle args = getArguments();
        final String title = getString(args.getInt(KEY_TITLE));
        CharSequence[] itemArray = null;
        if (args.getInt(KEY_TITLE) == R.string.select_voice_quality) {
            itemArray = appendSurffix(RecordParamsSetting
                    .getFormatStringArray(this.getActivity()),
                    args.getCharSequenceArray(KEY_SUFFIX_ARRAY));
        } else {
            itemArray = appendSurffix(args.getIntArray(KEY_ITEM_ARRAY),
                    args.getCharSequenceArray(KEY_SUFFIX_ARRAY));
        }

        final boolean singleChoice = args.getBoolean(KEY_SINGLE_CHOICE);
        AlertDialog.Builder builder = null;
        mInflate = LayoutInflater.from(getActivity());
        mView = mInflate.inflate(R.layout.sel_dialog_layout,
				null);
        if (singleChoice) {
            int nowSelect = args.getInt(KEY_DEFAULT_SELECT);
            builder = new AlertDialog.Builder(getActivity());
            builder.setView(mView);
            setTitle(title);
            setSingleChoiceItems(itemArray, nowSelect, this);
            setNegativeButton(getString(R.string.cancel), null);
        } else {
            boolean[] nowSelectArray = args.getBooleanArray(KEY_DEFAULT_SELECTARRAY);
            builder = new AlertDialog.Builder(getActivity());
            builder.setView(mView);
            setTitle(title);
            setMultiChoiceItems(itemArray, nowSelectArray, this);
            setNegativeButton(getString(R.string.cancel), null);
            setPositiveButton(getString(R.string.ok), this);
        }
        AlertDialog d = builder.create();
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return d;
    }
    /***
     * Set title 
     * @param title
     */
    private void setTitle(String title) {
    	if (mView != null) {
    		TextView t = (TextView)mView.findViewById(R.id.dialog_title);
    		t.setText(title);
    	}
    }
    /***
     * Set cancel button
     * @param title
     */
    private void setNegativeButton(String title, OnClickListener listener) {
    	if (mView != null) {
    		Button t = (Button)mView.findViewById(R.id.btn_cancel);
    		t.setText(title);
    		t.setVisibility(View.VISIBLE);
    		if (listener != null)
    			t.setOnClickListener(listener);
    		else
    			t.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dismiss();
					}
    			});
    	}
    }
    
    /***
     * Set the OK button 
     * @param title
     */
    private void setPositiveButton(String title, OnClickListener listener) {
    	if (mView != null) {
    		Button t = (Button)mView.findViewById(R.id.btn_ok);
    		t.setText(title);
    		t.setVisibility(View.VISIBLE);
    		if (listener != null)
    			t.setOnClickListener(listener);
    		else
    			t.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dismiss();
					}
    			});
    	}
    }
    /***
     * Set the radio entry 
     * @param items
     * @param checkedItem
     * @param listener
     */
    private void setSingleChoiceItems(CharSequence[] items, int checkedItem, OnCheckedChangeListener listener) {
    	if (null == items)
    		return;
    	if (mView != null) {
    		RadioGroup group = (RadioGroup)mView.findViewById(R.id.dialog_radio_group);
    		group.removeAllViews();
    		RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
    				RadioGroup.LayoutParams.MATCH_PARENT,//getResources().getDimensionPixelSize(R.dimen.radio_item_width), 
    				getResources().getDimensionPixelSize(R.dimen.radio_item_height));
    		for (int i=0; i<items.length; i++) {
        		RadioButton radio = (RadioButton)mInflate.inflate(R.layout.radio_item,
        				null);
        		radio.setBackgroundResource(R.drawable.pop_item_selector);
        		radio.setText(items[i]);
        		radio.setId(i);
        		if (i == checkedItem) {
        			radio.setChecked(true);
        		}
        		group.addView(radio, params);
        	}
    		
    		group.setOnCheckedChangeListener(listener);
//    		OnCheckedChangeListener a = new OnCheckedChangeListener() {
//				@Override
//				public void onCheckedChanged(RadioGroup group, int checkedId) {
//					
//				}
//    		};
    	}
    }
    
    /***
     * Set multiple entries
     * @param items
     * @param checkedItem
     * @param listener
     */
    private void setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, OnClickListener listener) {
    	if (null == items)
    		return;
    	if (mView != null) {
    		LinearLayout group = (LinearLayout)mView.findViewById(R.id.multi_ck);
    		group.removeAllViews();
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    				LinearLayout.LayoutParams.MATCH_PARENT,//getResources().getDimensionPixelSize(R.dimen.radio_item_width), 
    				getResources().getDimensionPixelSize(R.dimen.radio_item_height));
    		for (int i=0; i<items.length; i++) {
        		CheckBox radio = (CheckBox)mInflate.inflate(R.layout.check_item,
        				null);
        		radio.setText(items[i]);
        		radio.setId(i);
        		if (i <checkedItems.length && checkedItems[i]) {
        			radio.setChecked(true);
        		}
        		radio.setOnClickListener(listener);
        		group.addView(radio, params);
        	}
    	}
    }

    @Override
    public void onClick(View v) {
        if (null != mClickListener) {
            mClickListener.onClick(v);
        }
    }

    @Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
    	if (null != mMultiChoiceClickListener) {
    		mMultiChoiceClickListener.onCheckedChanged(group, checkedId);
    		dismiss();
    	}
	}

    public void setOnClickListener (View.OnClickListener lis) {
    	mClickListener = lis;
    }
    
    public void setOnMultiChoiceListener(RadioGroup.OnCheckedChangeListener ckLis) {
    	mMultiChoiceClickListener = ckLis;
    }

    private CharSequence[] appendSurffix(int[] itemStringId, CharSequence[] suffix) {
        if (null == itemStringId) {
            return null;
        }
        CharSequence[] itemArray = new CharSequence[itemStringId.length];
        for (int i = 0; i < itemStringId.length; i++) {
            itemArray[i] = getString(itemStringId[i]) + ((suffix != null) ? suffix[i] : "");
        }
        return itemArray;
    }

    private CharSequence[] appendSurffix(String[] itemString, CharSequence[] suffix) {
        if (null == itemString) {
            return null;
        }
        CharSequence[] itemArray = new CharSequence[itemString.length];
        for (int i = 0; i < itemString.length; i++) {
            itemArray[i] = itemString[i] + ((suffix != null) ? suffix[i] : "");
        }
        return itemArray;
    }
	
}