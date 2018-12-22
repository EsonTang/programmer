package com.prize.applock.fingerprintapplock.view;

import com.prize.applock.fingerprintapplock.R;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * @author wangzhong
 */
public class PinLockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_NUMBER = 0;
    private static final int VIEW_TYPE_DELETE = 1;

    private Context mContext;
    private CustomizationOptionsBundle mCustomizationOptionsBundle;
    private OnNumberClickListener mOnNumberClickListener;
    private OnDeleteClickListener mOnDeleteClickListener;
    private int mPinLength;

    public PinLockAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_NUMBER) {
            View view = inflater.inflate(R.layout.prize_layout_number_item, parent, false);
            viewHolder = new NumberViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.prize_layout_delete_item, parent, false);
            view.setVisibility(View.GONE);
            viewHolder = new DeleteViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_NUMBER) {
            NumberViewHolder vh1 = (NumberViewHolder) holder;
            configureNumberButtonHolder(vh1, position);
        } else if (holder.getItemViewType() == VIEW_TYPE_DELETE) {
            DeleteViewHolder vh2 = (DeleteViewHolder) holder;
            configureDeleteButtonHolder(vh2);
        }
    }

    private void configureNumberButtonHolder(NumberViewHolder holder, int position) {
        if (holder != null) {
            if (position == 10) {
                //holder.mNumberButton.setText("0");
                holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_0_bg);
            } else if (position == 9) {
                holder.mNumberButton.setVisibility(View.GONE);
            } else {
                //holder.mNumberButton.setText(String.valueOf((position + 1) % 10));
                switch ((position + 1) % 10) {
                    case 1:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_1_bg);
                        break;
                    case 2:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_2_bg);
                        break;
                    case 3:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_3_bg);
                        break;
                    case 4:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_4_bg);
                        break;
                    case 5:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_5_bg);
                        break;
                    case 6:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_6_bg);
                        break;
                    case 7:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_7_bg);
                        break;
                    case 8:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_8_bg);
                        break;
                    case 9:
                        holder.mNumberButton.setBackgroundResource(R.drawable.selector_pinlockview_item_number_9_bg);
                        break;
                    default:
                        break;
                }
            }

            if (mCustomizationOptionsBundle != null) {
                holder.mNumberButton.setTextColor(mCustomizationOptionsBundle.getTextColor());
                if (mCustomizationOptionsBundle.getButtonBackgroundDrawable() != null) {
                    if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        holder.mNumberButton.setBackgroundDrawable(mCustomizationOptionsBundle.getButtonBackgroundDrawable());
                    } else {
                        holder.mNumberButton.setBackground(mCustomizationOptionsBundle.getButtonBackgroundDrawable());
                    }
                }
                holder.mNumberButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCustomizationOptionsBundle.getTextSize());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mCustomizationOptionsBundle.getButtonSize(), mCustomizationOptionsBundle.getButtonSize());
                //holder.mNumberButton.setLayoutParams(params);
            }
        }
    }

    private void configureDeleteButtonHolder(DeleteViewHolder holder) {
        if (holder != null) {
            if (mCustomizationOptionsBundle.isShowDeleteButton() && mPinLength > 0) {
                /*holder.mButtonImage.setVisibility(View.VISIBLE);
                if (mCustomizationOptionsBundle.getDeleteButtonDrawable() != null) {
                    holder.mButtonImage.setImageDrawable(mCustomizationOptionsBundle.getDeleteButtonDrawable());
                }
                holder.mButtonImage.setColorFilter(mCustomizationOptionsBundle.getTextColor(), PorterDuff.Mode.SRC_ATOP);*/
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mCustomizationOptionsBundle.getDeleteButtonSize(), mCustomizationOptionsBundle.getDeleteButtonSize());
                holder.mButtonImage.setLayoutParams(params);
            } else {
                /*holder.mButtonImage.setVisibility(View.GONE);*/
            }
        }
    }

    @Override
    public int getItemCount() {
        return 12;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return VIEW_TYPE_DELETE;
        }
        return VIEW_TYPE_NUMBER;
    }

    public int getPinLength() {
        return mPinLength;
    }

    public void setPinLength(int pinLength) {
        this.mPinLength = pinLength;
    }

    public OnNumberClickListener getOnItemClickListener() {
        return mOnNumberClickListener;
    }

    public void setOnItemClickListener(OnNumberClickListener onNumberClickListener) {
        this.mOnNumberClickListener = onNumberClickListener;
    }

    public OnDeleteClickListener getOnDeleteClickListener() {
        return mOnDeleteClickListener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.mOnDeleteClickListener = onDeleteClickListener;
    }

    public CustomizationOptionsBundle getCustomizationOptions() {
        return mCustomizationOptionsBundle;
    }

    public void setCustomizationOptions(CustomizationOptionsBundle customizationOptionsBundle) {
        this.mCustomizationOptionsBundle = customizationOptionsBundle;
    }

    public class NumberViewHolder extends RecyclerView.ViewHolder {
        Button mNumberButton;

        public NumberViewHolder(final View itemView) {
            super(itemView);
            mNumberButton = (Button) itemView.findViewById(R.id.button);
            mNumberButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnNumberClickListener != null) {
                        mOnNumberClickListener.onNumberClicked(getAdapterPosition());
                    }
                }
            });
        }
    }

    public class DeleteViewHolder extends RecyclerView.ViewHolder {
        LinearLayout mDeleteButton;
        ImageView mButtonImage;

        public DeleteViewHolder(final View itemView) {
            super(itemView);
            mDeleteButton = (LinearLayout) itemView.findViewById(R.id.button);
            mButtonImage = (ImageView) itemView.findViewById(R.id.buttonImage);

            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnDeleteClickListener != null) {
                        mOnDeleteClickListener.onDeleteClicked();
                    }
                }
            });

            mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnDeleteClickListener != null) {
                        mOnDeleteClickListener.onDeleteLongClicked();
                    }
                    return true;
                }
            });

            /*mDeleteButton.setOnTouchListener(new View.OnTouchListener() {
                private Rect rect;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        mButtonImage.setColorFilter(mCustomizationOptionsBundle.getDeleteButtonPressesColor());
                        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    }
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        mButtonImage.clearColorFilter();
                    }
                    if(event.getAction() == MotionEvent.ACTION_MOVE){
                        if(!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                            mButtonImage.clearColorFilter();
                        }
                    }
                    return false;
                }
            });*/
        }
    }

    public interface OnNumberClickListener {
        void onNumberClicked(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClicked();

        void onDeleteLongClicked();
    }
}
