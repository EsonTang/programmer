package com.android.incallui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.telecom.VideoProfile;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PrizeAnswerLayout extends RelativeLayout {

    private final static String TAG = "PrizeAnswerLayout";

    private final static int SMALL_OFFSET_COUNT_MAX = 21;
    private final static int SMALL_OFFSET_COUNT_MIN = 0;
    private boolean isReverseSmallOffset = false;
    private int mSmallOffset = 0;
    private int mSmallCellOffset = 0;
    private final static int BIG_OFFSET_COUNT_MAX = 80;
    private final static int BIG_OFFSET_COUNT_MIN = 0;
    private boolean isReverseBigOffset = false;
    private int mBigOffset = 0;
    private int mBigCellOffset = 0;
    private int mVideoState = android.telecom.VideoProfile.STATE_BIDIRECTIONAL;

    private int mBigRect = 0;
    private int mSmallRect = 0;

    // the view
    private TextView wait_reject, wait_answer, wait_message;
    private TextView prize_reject_arrow, prize_answer_arrow, prize_message_arrow;
    private RelativeLayout rl_reject_incall, rl_answer_message;
    private TextView tv_response_answer, tv_response_reject, tv_response_message;

    //prize-add for video call to audio call-by xiekui-20180814-start
    private LinearLayout mPrizeToAudioCallRoot;
    private ImageView mPrizeToAudioImg;
    //prize-add for video call to audio call-by xiekui-20180814-end

    private Bitmap dragBitmap, bigdragBitmap, arrowBitmap, rejectBitmap, messageBitmap;
    private Bitmap dragBitmapPress, rejectBitmapPress, messageBitmapPress;
    private Bitmap[] arrowBitmaps;
    private Context mContext = null;
    private Rect rect, rect1, rect2;
    private Rect rect3;//prize-add for video call to audio call-by xiekui-20180814

    private TranslateAnimation mTranslateAnimation, rejectTranslateAnimation, messageTranslateAnimation;
    private AlphaAnimation mAlphaAnimation;
    private AnimationSet animationSet;

    // the status
    private boolean isAnswerDown, isRejectDown, isMessageDown = false;
    private int mLastMoveY = 1000;
    private int mLastMoveX = 0;
    private boolean onceMessage = true;
    private boolean isAnswer = false;
    private int mMotionEventActionDownIndex;
    private IPrizeAnswerLayoutResponseListener mIPrizeAnswerLayoutResponseListener;

    public interface IPrizeAnswerLayoutResponseListener {
        void prizeIncallAnswer(int videoState);

        void prizeIncallReject();

        void prizeIncallMessage();
    }

    public void setIPrizeAnswerLayoutResponseListener(IPrizeAnswerLayoutResponseListener iPrizeAnswerLayoutResponseListener) {
        this.mIPrizeAnswerLayoutResponseListener = iPrizeAnswerLayoutResponseListener;
    }

    public int getLastMoveY() {
        return mLastMoveY;
    }

    public void setLastMoveY(int lastMoveY) {
        mLastMoveY = lastMoveY;
    }

    public int getLastMoveX() {
        return mLastMoveX;
    }

    public void setLastMoveX(int lastMoveX) {
        mLastMoveX = lastMoveX;
    }

    public int getMotionEventActionDownIndex() {
        return mMotionEventActionDownIndex;
    }

    public void setMotionEventActionDownIndex(int motionEventActionDownIndex) {
        mMotionEventActionDownIndex = motionEventActionDownIndex;
    }

    public PrizeAnswerLayout(Context context) {
        super(context);
        mContext = context;
        setWillNotDraw(false);
        initDragBitmap();
    }

    public PrizeAnswerLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        setWillNotDraw(false);
        initDragBitmap();
    }

    public PrizeAnswerLayout(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        setWillNotDraw(false);
        initDragBitmap();
    }

    public void initLayoutResponseStatus() {
        isAnswer = false;
    }

    private void initDragBitmap() {
        mSmallCellOffset = mContext.getResources().getDimensionPixelOffset(R.dimen.prize_small_cell_offset);
        mBigCellOffset = mContext.getResources().getDimensionPixelOffset(R.dimen.prize_big_cell_offset);

        mSmallRect = mContext.getResources().getDimensionPixelOffset(R.dimen.prize_small_rect);
        mBigRect = mContext.getResources().getDimensionPixelOffset(R.dimen.prize_big_rect);

        if (dragBitmap == null) {
            dragBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.prize_answer_normal);
            bigdragBitmap = dragBitmap.createScaledBitmap(dragBitmap,
                    (int) (dragBitmap.getWidth() * 1.2), (int) (dragBitmap.getHeight() * 1.2), true);
        }

        if (rejectBitmap == null) {
            rejectBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.prize_reject_lockedscreen_normal);
        }

        if (messageBitmap == null) {
            messageBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.prize_message_lockedscreen_normal);
        }

        if (dragBitmapPress == null) {
            dragBitmapPress = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.prize_answer_pressed);
        }

        if (rejectBitmapPress == null) {
            rejectBitmapPress = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.prize_reject_lockedscreen_pressed);
        }

        if (messageBitmapPress == null) {
            messageBitmapPress = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.prize_message_lockedscreen_pressed);
        }

        arrowBitmaps = new Bitmap[10];
        arrowBitmaps[0] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_1);
        arrowBitmaps[1] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_2);
        arrowBitmaps[2] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_3);
        arrowBitmaps[3] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_4);
        arrowBitmaps[4] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_5);
        arrowBitmaps[5] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_6);
        arrowBitmaps[6] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_7);
        arrowBitmaps[7] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_8);
        arrowBitmaps[8] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_9);
        arrowBitmaps[9] = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.prize_arrow_10);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        rl_reject_incall = (RelativeLayout) findViewById(R.id.rl_reject_incall);
        rl_answer_message = (RelativeLayout) findViewById(R.id.rl_answer_message);
        tv_response_answer = (TextView) findViewById(R.id.answer_incall);
        tv_response_reject = (TextView) findViewById(R.id.reject_incall);
        tv_response_message = (TextView) findViewById(R.id.answer_message);

        wait_answer = (TextView) findViewById(R.id.prize_answer_img);
        wait_reject = (TextView) findViewById(R.id.prize_reject_img);
        wait_message = (TextView) findViewById(R.id.prize_message_img);
        prize_reject_arrow = (TextView) findViewById(R.id.prize_reject_arrow);
        prize_answer_arrow = (TextView) findViewById(R.id.prize_answer_arrow);
        prize_message_arrow = (TextView) findViewById(R.id.prize_message_arrow);

        //prize-add for video call to audio call-by xiekui-20180814-start
        mPrizeToAudioCallRoot = (LinearLayout) findViewById(R.id.prize_to_audio_call_root);
        mPrizeToAudioImg = (ImageView) findViewById(R.id.prize_tv_to_audio_img);
        mPrizeToAudioImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIPrizeAnswerLayoutResponseListener.prizeIncallAnswer(VideoProfile.STATE_AUDIO_ONLY);
            }
        });
        //prize-add for video call to audio call-by xiekui-20180814-end

        mTranslateAnimation = new TranslateAnimation(0, 0, -70, 0);
        mTranslateAnimation.setFillAfter(true);
        mTranslateAnimation.setInterpolator(new BounceInterpolator());
        mTranslateAnimation.setDuration(2000);
        mTranslateAnimation.setRepeatCount(Animation.INFINITE);
        mAlphaAnimation = new AlphaAnimation(1, 0);
        mAlphaAnimation.setDuration(1500);
        rejectTranslateAnimation = new TranslateAnimation(0, 0, -25, 0);
        rejectTranslateAnimation.setFillAfter(true);
        rejectTranslateAnimation.setInterpolator(new BounceInterpolator());
        rejectTranslateAnimation.setDuration(2000);
        messageTranslateAnimation = new TranslateAnimation(0, 0, -25, 0);
        messageTranslateAnimation.setFillAfter(true);
        messageTranslateAnimation.setInterpolator(new BounceInterpolator());
        messageTranslateAnimation.setDuration(2000);

        prize_reject_arrow.setVisibility(View.INVISIBLE);
        prize_answer_arrow.setVisibility(View.INVISIBLE);
        prize_message_arrow.setVisibility(View.INVISIBLE);
        prize_reject_arrow.setClickable(false);
        prize_answer_arrow.setClickable(false);
        prize_message_arrow.setClickable(false);
        animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 2f, 1f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, 0f, -50f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        translateAnimation.setDuration(1000);
        translateAnimation.setRepeatCount(Animation.INFINITE);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                prize_reject_arrow.setVisibility(View.INVISIBLE);
                prize_answer_arrow.setVisibility(View.INVISIBLE);
                prize_message_arrow.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    // the status
    private boolean isAnswerDown() {
        return isAnswerDown;
    }

    private void setAnswerDown(boolean answerDown) {
        isAnswerDown = answerDown;
    }

    private boolean isRejectDown() {
        return isRejectDown;
    }

    private void setRejectDown(boolean rejectDown) {
        isRejectDown = rejectDown;
    }

    private boolean isMessageDown() {
        return isMessageDown;
    }

    private void setMessageDown(boolean messageDown) {
        isMessageDown = messageDown;
    }

    private void initTouchDownTakeEffect() {
        setAnswerDown(false);
        setRejectDown(false);
        setMessageDown(false);
    }

    private boolean hasTouchDownTakeEffect() {
        return isAnswerDown() || isRejectDown() || isMessageDown();
    }

    private boolean isMultiTouch(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        Log.d(TAG, ".......... isMultiTouch  pointerCount :" + pointerCount);
        if (pointerCount > 1) {
            return true;
        }
        return false;
    }

    /**
     * Avoid coordinates jump.
     */
    private boolean isCurrentEffectiveArea(int x, int y) {
        if (Math.abs(getLastMoveX() - x) < 10 && Math.abs(getLastMoveY() - y) < 10) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG, "..... onTouchEvent event.getActionMasked() : " + event.getActionMasked() + ",     event.getActionIndex() : " + event.getActionIndex());
        int index = event.getActionIndex();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return handleActionDownEvenet(event);
            case MotionEvent.ACTION_POINTER_DOWN:
                if (hasTouchDownTakeEffect()) {
                    Log.d(TAG, ".......... MotionEvent.ACTION_POINTER_DOWN   hasTouchDownTakeEffect : true   ----------index : " + index);
                    if (index == getMotionEventActionDownIndex() || index < getMotionEventActionDownIndex()) {
                        setMotionEventActionDownIndex(getMotionEventActionDownIndex() + 1);
                    }
                    return super.onTouchEvent(event);
                }
                return handleActionDownEvenet(event);


            case MotionEvent.ACTION_MOVE:
                if (hasTouchDownTakeEffect()) {
                    moveResponseView(event);
                }
                return true;


            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, ".......... MotionEvent.ACTION_CANCEL");
            case MotionEvent.ACTION_UP:
                Log.d(TAG, ".......... MotionEvent.ACTION_UP   ----------index : " + index);
                if (hasTouchDownTakeEffect()) {
                    setMotionEventActionDownIndex(-1);
                    handleActionUpEvent();
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, ".......... MotionEvent.ACTION_POINTER_UP   ----------index : " + index + ",   getMotionEventActionDownIndex : " + getMotionEventActionDownIndex() + ",                   msked event:" + event.getActionMasked());
                if (hasTouchDownTakeEffect()) {
                    if (index == getMotionEventActionDownIndex()) {
                        initLayoutResponseStatus();
                        handleActionUpEvent();
                        return true;
                    }
                    if (index < getMotionEventActionDownIndex()) {
                        setMotionEventActionDownIndex(getMotionEventActionDownIndex() - 1);
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Finger raised events.
     */
    public void handleActionUpEvent() {
        initTouchDownTakeEffect();
        onceMessage = true;

        prize_reject_arrow.setVisibility(View.INVISIBLE);
        prize_answer_arrow.setVisibility(View.INVISIBLE);
        prize_message_arrow.setVisibility(View.INVISIBLE);
        prize_answer_arrow.clearAnimation();
        prize_reject_arrow.clearAnimation();
        prize_message_arrow.clearAnimation();
        if (!isAnswer) {
            resetViewState();
        }
    }

    /**
     * Finger press event.
     * @param event
     * @return
     */
    private boolean handleActionDownEvenet(MotionEvent event) {
        int downIndex = event.getActionIndex();
        int downX = (int) event.getX(downIndex);
        int downY = (int) event.getY(downIndex);
        Log.d(TAG, ".......... MotionEvent.ACTION   DOWN   downX :" + downX + "  , downY : " + downY + ",     ----------downIndex : " + downIndex);

        setLastMoveY(downY);
        setLastMoveX(downX);
        rect = new Rect();
        rect1 = new Rect();
        rect2 = new Rect();
        wait_answer.getHitRect(rect);
        wait_reject.getHitRect(rect1);
        wait_message.getHitRect(rect2);

        rect = new Rect(rect.left - mBigRect, rect.top - mBigRect, rect.right + mBigRect, rect.bottom + mBigRect * 3 / 2);
        rect1 = new Rect(rect1.left - mSmallRect, rect1.top - mSmallRect, rect1.right + mSmallRect, rect1.bottom + mSmallRect * 3 / 2);
        rect2 = new Rect(rect2.left - mSmallRect, rect2.top - mSmallRect, rect2.right + mSmallRect, rect2.bottom + mSmallRect * 3 / 2);

        boolean isHitAnswer = rect.contains(downX, downY);
        boolean isHitReject = rect1.contains(downX - rl_reject_incall.getLeft(), downY);
        boolean isHitMessage = rect2.contains(downX - rl_answer_message.getLeft(), downY);

        if (isHitAnswer) {
            wait_reject.setVisibility(View.INVISIBLE);
            wait_message.setVisibility(View.INVISIBLE);
        }
        if (isHitReject) {
            wait_answer.setVisibility(View.INVISIBLE);
            wait_message.setVisibility(View.INVISIBLE);
        }
        if (isHitMessage) {
            wait_answer.setVisibility(View.INVISIBLE);
            wait_reject.setVisibility(View.INVISIBLE);
        }

        setAnswerDown(isHitAnswer);
        setRejectDown(isHitReject);
        setMessageDown(isHitMessage);

        //prize-add for video call to audio call-by xiekui-20180814-start
        rect3 = new Rect();
        mPrizeToAudioCallRoot.getHitRect(rect3);
        boolean isInVideoCallRoot = rect3.contains(downX, downY);
        if (isInVideoCallRoot) {
            mPrizeToAudioImg.onTouchEvent(event);
        }
        //prize-add for video call to audio call-by xiekui-20180814-end

        // Distinguish between effective click your fingers.
        if (hasTouchDownTakeEffect()) {
            setMotionEventActionDownIndex(downIndex);
            hidePrizeToAudioCall();//prize-add for video call to audio call-by xiekui-20180814
        }
        if (isHitAnswer) {
            prize_answer_arrow.startAnimation(animationSet);
            return true;
        } else if (isHitReject) {
            prize_reject_arrow.startAnimation(animationSet);
            return true;
        } else if (isHitMessage) {
            prize_message_arrow.startAnimation(animationSet);
            return true;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        invalidateDragImg(canvas);
    }

    private void invalidateDragImg(Canvas canvas) {
        // TODO: AUTO
        if (!hasTouchDownTakeEffect()) {
            int yOffsetMessage = wait_message.getBottom() - wait_message.getHeight();
            int yOffsetReject = wait_reject.getBottom() - wait_reject.getHeight();
            int yOffsetAnswer = wait_answer.getBottom() - wait_answer.getHeight();
            // TODO The small offset.
            int smallOffset;
            if (isReverseSmallOffset) {
                mSmallOffset--;
                smallOffset = -1;
            } else {
                mSmallOffset++;
                smallOffset = 1;
            }
            if (mSmallOffset > SMALL_OFFSET_COUNT_MAX) {
                isReverseSmallOffset = true;
            } else if (mSmallOffset < SMALL_OFFSET_COUNT_MIN) {
                isReverseSmallOffset = false;
            }
            // correct the response view.
            if (isAcrossBelowView(wait_reject, tv_response_reject)) {
                mSmallOffset = 0;
                isReverseSmallOffset = false;
            }
            // wait_message
            //canvas.drawBitmap(messageBitmap, drawMessageXCor, yOffsetMessage - mSmallOffset, null);
            wait_message.layout(wait_message.getLeft(), wait_message.getTop() - smallOffset, wait_message.getRight(), wait_message.getBottom() - smallOffset);
            // wait_reject
            //canvas.drawBitmap(rejectBitmap, drawRejectXCor, yOffsetReject - mSmallOffset, null);
            wait_reject.layout(wait_reject.getLeft(), wait_reject.getTop() - smallOffset, wait_reject.getRight(), wait_reject.getBottom() - smallOffset);

            // TODO The big offset.
            int bigOffset;
            if (isReverseBigOffset) {
                mBigOffset -= 2;
                bigOffset = -2;
            } else {
                mBigOffset += 2;
                bigOffset = 2;
            }
            if (mBigOffset > BIG_OFFSET_COUNT_MAX) {
                isReverseBigOffset = true;
            } else if (mBigOffset < BIG_OFFSET_COUNT_MIN) {
                isReverseBigOffset = false;
            }
            // correct the response view.
            if (isAcrossBelowView(wait_answer, tv_response_answer)) {
                mBigOffset = 0;
                isReverseBigOffset = false;
            }
            //wait_answer
            //canvas.drawBitmap(dragBitmap, drawXCor, yOffsetAnswer - mBigOffset, null);
            wait_answer.layout(wait_answer.getLeft(), wait_answer.getTop() - bigOffset, wait_answer.getRight(), wait_answer.getBottom() - bigOffset);
        }
        //invalidate();
    }

    /**
     * Finger motion event.
     * @param event
     */
    private void moveResponseView(MotionEvent event) {
        int moveY = 0;
        int moveIndex = event.getActionIndex();
        int downIndex = getMotionEventActionDownIndex();
        isMultiTouch(event);
        Log.d(TAG, "............... MotionEvent.ACTION_MOVE   ----------moveIndex :" + moveIndex + "  , getMotionEventActionDownIndex : " + downIndex);
        //if (downIndex == moveIndex) {
            int x = (int) event.getX(downIndex);
            int y = (int) event.getY(downIndex);
            Log.d(TAG, "............... MotionEvent.ACTION_MOVE  x :" + x + "  , y : " + y);
            moveY = getLastMoveY() - y;
            setLastMoveY(y);
            setLastMoveX(x);
        //}

        // TODO: MOVE
        // answer
        if (isAnswerDown()) {
            boolean isMoveUp;
            if (isAcrossBelowView(wait_answer, tv_response_answer)) {
                isMoveUp = false;
            } else {
                isMoveUp = true;
            }
            if (moveY > 0) {
                isMoveUp = true;
            }
            if (isMoveUp) {
                wait_answer.layout(wait_answer.getLeft(), wait_answer.getTop() - moveY, wait_answer.getRight(), wait_answer.getBottom() - moveY);
                layoutResponseView(prize_answer_arrow, wait_answer);

                if (isResponse(wait_answer, tv_response_answer)) {
                    isAnswer = true;
                    setResponseViewVisibility(View.INVISIBLE);
                    mIPrizeAnswerLayoutResponseListener.prizeIncallAnswer(mVideoState);
                }
            } else {
                layoutResponseView(wait_answer, tv_response_answer);
                layoutResponseView(prize_answer_arrow, wait_answer);
            }
        }

        // reject
        if (isRejectDown()) {
            boolean isMoveUp;
            if (isAcrossBelowView(wait_reject, tv_response_reject)) {
                isMoveUp = false;
            } else {
                isMoveUp = true;
            }
            if (moveY > 0) {
                isMoveUp = true;
            }
            if (isMoveUp) {
                wait_reject.layout(wait_reject.getLeft(), wait_reject.getTop() - moveY, wait_reject.getRight(), wait_reject.getBottom() - moveY);
                layoutResponseView(prize_reject_arrow, wait_reject);

                if (isResponse(wait_reject, tv_response_reject)) {
                    isAnswer = true;
                    setResponseViewVisibility(View.INVISIBLE);
                    mIPrizeAnswerLayoutResponseListener.prizeIncallReject();
                }
            } else {
                layoutResponseView(wait_reject, tv_response_reject);
                layoutResponseView(prize_reject_arrow, wait_reject);
            }
        }

        //message
        if (isMessageDown()) {
            boolean isMoveUp;
            if (isAcrossBelowView(wait_message, tv_response_message)) {
                isMoveUp = false;
            } else {
                isMoveUp = true;
            }
            if (moveY > 0) {
                isMoveUp = true;
            }
            if (isMoveUp) {
                wait_message.layout(wait_message.getLeft(), wait_message.getTop() - moveY, wait_message.getRight(), wait_message.getBottom() - moveY);
                layoutResponseView(prize_message_arrow, wait_message);

                if (isResponse(wait_message, tv_response_message) && onceMessage) {
                    onceMessage = false;
                    mIPrizeAnswerLayoutResponseListener.prizeIncallMessage();
                }
            } else {
                layoutResponseView(wait_message, tv_response_message);
                layoutResponseView(prize_message_arrow, wait_message);
            }
        }
    }

    /**
     * reset all state.
     */
    public void resetViewState() {
        isAnswer = false;
        setResponseViewVisibility(View.VISIBLE);
        resetResponseViewReverse();
        resetResponseViewOffset();
        resetResponseViewLayout();
    }

    private void setResponseViewVisibility(int visibility) {
        wait_answer.setVisibility(visibility);
        wait_reject.setVisibility(visibility);
        wait_message.setVisibility(visibility);

        //prize-add for video call to audio call-by xiekui-20180814-start
        if (visibility == VISIBLE && VideoProfile.isBidirectional(mVideoState)) {
            showPrizeToAudioCall();
        } else {
            hidePrizeToAudioCall();
        }
        //prize-add for video call to audio call-by xiekui-20180814-end
    }

    //prize-add for video call to audio call-by xiekui-20180814-start
    private void showPrizeToAudioCall() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mPrizeToAudioCallRoot, "alpha", .0f, 1.0f);
        animator.setDuration(300);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPrizeToAudioCallRoot.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void hidePrizeToAudioCall() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mPrizeToAudioCallRoot, "alpha", 1.0f, .0f);
        animator.setDuration(300);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mPrizeToAudioCallRoot.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
    //prize-add for video call to audio call-by xiekui-20180814-end

    private void resetResponseViewReverse() {
        isReverseSmallOffset = false;
        isReverseBigOffset = false;
    }

    private void resetResponseViewOffset() {
        mSmallOffset = 0;
        mBigOffset = 0;
    }

    private void resetResponseViewLayout() {
        // TODO Reset the small offset.
        layoutResponseView(wait_message, tv_response_message);
        layoutResponseView(wait_reject, tv_response_reject);

        // TODO Reset the big offset.
        layoutResponseView(wait_answer, tv_response_answer);
    }

    private final static int MARGIN_BOTTOM = 15;
    private final static int DISTANCE_RESPONSE = 170;

    /**
     * Layout according to the benchmark view again.
     * @param v
     * @param benchmarkView
     */
    private void layoutResponseView(View v, View benchmarkView) {
        v.layout(v.getLeft(), benchmarkView.getTop() - MARGIN_BOTTOM - v.getHeight(), v.getRight(), benchmarkView.getTop() - MARGIN_BOTTOM);
    }

    /**
     * Determine whether collision according to the below view.
     * @param v
     * @param belowView
     * @return
     */
    private boolean isAcrossBelowView(View v, View belowView) {
        if (v.getBottom() >= belowView.getTop() - MARGIN_BOTTOM) {
            return true;
        }
        return false;
    }

    /**
     * Whether meet the response condition.
     * @param v
     * @param benchmark
     * @return
     */
    private boolean isResponse(View v, View benchmark) {
        if ((benchmark.getTop() - v.getBottom()) > DISTANCE_RESPONSE) {
            return true;
        }
        return false;
    }

    public void setVideoState(int videoState) {
        mVideoState = videoState;
        //prize-add for video call to audio call-by xiekui-20180814-start
        if (VideoProfile.isBidirectional(mVideoState)) {
            wait_answer.setBackgroundResource(R.drawable.prize_answer_video_selector);
        } else {
            wait_answer.setBackgroundResource(R.drawable.prize_answer_selector);
        }
        //prize-add for video call to audio call-by xiekui-20180814-end
    }

}
