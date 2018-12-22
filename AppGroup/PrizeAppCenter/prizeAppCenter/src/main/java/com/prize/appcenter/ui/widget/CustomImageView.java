package com.prize.appcenter.ui.widget;

import android.annotation.ColorInt;
import android.annotation.DimenRes;
import android.annotation.DrawableRes;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.widget.roundview.Corner;
import com.prize.appcenter.ui.widget.roundview.RoundedDrawable;

public class CustomImageView extends ImageView {
//    private Paint paint;
//    private Paint paintBorder;
//    private Bitmap mSrcBitmap;
//    /**
//     * 圆角的弧度
//     */
//    private float mRadius;
//    private boolean mIsCircle;
//
//    public CustomImageView(final Context context) {
//        this(context, null);
//    }
//
//    public CustomImageView(Context context, AttributeSet attrs) {
//        this(context, attrs, R.attr.customImageViewStyle);
//    }
//
//    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        TypedArray ta = context.obtainStyledAttributes(attrs,
//                R.styleable.CustomImageView, defStyle, 0);
//        mRadius = ta.getDimension(R.styleable.CustomImageView_iv_radius, 0);
//        mIsCircle = ta.getBoolean(R.styleable.CustomImageView_circle, false);
//        int srcResource = attrs.getAttributeResourceValue(
//                "http://schemas.android.com/apk/res/android", "src", 0);
//        if (srcResource != 0)
//            mSrcBitmap = BitmapFactory.decodeResource(getResources(),
//                    srcResource);
//        ta.recycle();
//        paint = new Paint();
//        paint.setAntiAlias(true);
//        paintBorder = new Paint();
//        paintBorder.setAntiAlias(true);
//    }
//
//    public void setmRadius(float mRadius) {
//        this.mRadius = mRadius;
//    }
//
//    @Override
//    public void onDraw(Canvas canvas) {
//        int width = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
//        int height = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
//        Bitmap image = drawableToBitmap(getDrawable());
//        if (mIsCircle) {
//            Bitmap reSizeImage = reSizeImageC(image, width, height);
//            if (reSizeImage == null) {
//                return;
//            }
//            canvas.drawBitmap(createCircleImage(reSizeImage, width, height),
//                    getPaddingLeft(), getPaddingTop(), null);
//
//        } else {
//            Bitmap reSizeImage = reSizeImage(image, width, height);
//            if (reSizeImage == null) {
//                return;
//            }
//            canvas.drawBitmap(createRoundImage(reSizeImage, width, height),
//                    getPaddingLeft(), getPaddingTop(), null);
//        }
//    }
//
//    /**
//     * 画圆角
//     *
//     * @param source Bitmap
//     * @param width  宽度
//     * @param height 高度
//     * @return Bitmap
//     */
//    private Bitmap createRoundImage(Bitmap source, int width, int height) {
//        if (source == null) {
//            return null;
//        }
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        try {
//            Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//            Canvas canvas = new Canvas(target);
//            RectF rect = new RectF(0, 0, width, height);
//            canvas.drawRoundRect(rect, mRadius, mRadius, paint);
//            // 核心代码取两个图片的交集部分
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//            canvas.drawBitmap(source, 0, 0, paint);
//            return target;
//        } catch (OutOfMemoryError ex) {
//            ex.printStackTrace();
//        }
//        return source;
//    }
//
//    /**
//     * 画圆
//     *
//     * @param source
//     * @param width
//     * @param height
//     * @return
//     */
//    private Bitmap createCircleImage(Bitmap source, int width, int height) {
//
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//        Canvas canvas = new Canvas(target);
//        canvas.drawCircle(width / 2, height / 2, Math.min(width, height) / 2,
//                paint);
//        // 核心代码取两个图片的交集部分
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(source, (width - source.getWidth()) / 2,
//                (height - source.getHeight()) / 2, paint);
//        return target;
//
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        setMeasuredDimension(width, height);
//    }
//
//    /**
//     * drawable转bitmap
//     *
//     * @param drawable
//     * @return
//     */
//    private Bitmap drawableToBitmap(Drawable drawable) {
//        if (drawable == null) {
//            if (mSrcBitmap != null) {
//                return mSrcBitmap;
//            } else {
//                return null;
//            }
//        } else if (drawable instanceof BitmapDrawable) {
//            return ((BitmapDrawable) drawable).getBitmap();
//        }
//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
//                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }
//
//    /**
//     * 重设Bitmap的宽高
//     *
//     * @param bitmap    Bitmap
//     * @param newWidth  宽
//     * @param newHeight 高
//     * @return Bitmap
//     */
//    private Bitmap reSizeImage(Bitmap bitmap, int newWidth, int newHeight) {
//        if (bitmap == null || newWidth <= 0 || newHeight <= 0) {
//            return null;
//        }
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        // 计算出缩放比
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // 矩阵缩放bitmap
//        Matrix matrix = new Matrix();
//
//        matrix.postScale(scaleWidth, scaleHeight);
//        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//    }
//
//    /**
//     * 重设Bitmap的宽高
//     *
//     * @param bitmap    Bitmap
//     * @param newWidth  int
//     * @param newHeight int
//     * @return Bitmap
//     */
//    private Bitmap reSizeImageC(Bitmap bitmap, int newWidth, int newHeight) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int x = (newWidth - width) / 2;
//        int y = (newHeight - height) / 2;
//        if (x > 0 && y > 0) {
//            return Bitmap.createBitmap(bitmap, 0, 0, width, height, null, true);
//        }
//        float scale = 1;
//        if (width > height) {
//            // 按照宽度进行等比缩放
//            scale = ((float) newWidth) / width;
//        } else {
//            // 按照高度进行等比缩放
//            // 计算出缩放比
//            scale = ((float) newHeight) / height;
//        }
//        Matrix matrix = new Matrix();
//        matrix.postScale(scale, scale);
//        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//    }

    public static final String TAG = "CustomImageView";
    // Constants for tile mode attributes
    private static final int TILE_MODE_UNDEFINED = -2;
    private static final int TILE_MODE_CLAMP = 0;
    private static final int TILE_MODE_REPEAT = 1;

    private static final int TILE_MODE_MIRROR = 2;
    public static final float DEFAULT_RADIUS = 0f;
    public static final float DEFAULT_BORDER_WIDTH = 0f;
    public static final Shader.TileMode DEFAULT_TILE_MODE = Shader.TileMode.CLAMP;
    private static final ScaleType[] SCALE_TYPES = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };

    private final float[] mCornerRadii =
            new float[]{DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS};

    private Drawable mBackgroundDrawable;
    private ColorStateList mBorderColor =
            ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
    private float mBorderWidth = DEFAULT_BORDER_WIDTH;
    private ColorFilter mColorFilter = null;
    private boolean mColorMod = false;
    private Drawable mDrawable;
    private boolean mHasColorFilter = false;
    private boolean mIsOval = false;
    private boolean mMutateBackground = false;
    private int mResource;
    private int mBackgroundResource;
    private ScaleType mScaleType;
    private Shader.TileMode mTileModeX = DEFAULT_TILE_MODE;
    private Shader.TileMode mTileModeY = DEFAULT_TILE_MODE;

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView, defStyle, 0);
        int index = a.getInt(R.styleable.CustomImageView_android_scaleType, -1);
        if (index >= 0) {
            setScaleType(SCALE_TYPES[index]);
        } else {
            // default scaletype to FIT_CENTER
            setScaleType(ScaleType.FIT_CENTER);
        }

        float cornerRadiusOverride =
                a.getDimensionPixelSize(R.styleable.CustomImageView_iv_radius, -1);

        mCornerRadii[Corner.TOP_LEFT] =
                a.getDimensionPixelSize(R.styleable.CustomImageView_riv_corner_radius_top_left, -1);
        mCornerRadii[Corner.TOP_RIGHT] =
                a.getDimensionPixelSize(R.styleable.CustomImageView_riv_corner_radius_top_right, -1);
        mCornerRadii[Corner.BOTTOM_RIGHT] =
                a.getDimensionPixelSize(R.styleable.CustomImageView_riv_corner_radius_bottom_right, -1);
        mCornerRadii[Corner.BOTTOM_LEFT] =
                a.getDimensionPixelSize(R.styleable.CustomImageView_riv_corner_radius_bottom_left, -1);

        boolean any = false;
        for (int i = 0, len = mCornerRadii.length; i < len; i++) {
            if (mCornerRadii[i] < 0) {
                mCornerRadii[i] = 0f;
            } else {
                any = true;
            }
        }

        if (!any) {
            if (cornerRadiusOverride < 0) {
                cornerRadiusOverride = DEFAULT_RADIUS;
            }
            for (int i = 0, len = mCornerRadii.length; i < len; i++) {
                mCornerRadii[i] = cornerRadiusOverride;
            }
        }

        mBorderWidth = a.getDimensionPixelSize(R.styleable.CustomImageView_riv_border_width, -1);
        if (mBorderWidth < 0) {
            mBorderWidth = DEFAULT_BORDER_WIDTH;
        }

        mBorderColor = a.getColorStateList(R.styleable.CustomImageView_riv_border_color);
        if (mBorderColor == null) {
            mBorderColor = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
        }

        mMutateBackground = a.getBoolean(R.styleable.CustomImageView_riv_mutate_background, false);
        mIsOval = a.getBoolean(R.styleable.CustomImageView_riv_oval, false);

        final int tileMode = a.getInt(R.styleable.CustomImageView_riv_tile_mode, TILE_MODE_UNDEFINED);
        if (tileMode != TILE_MODE_UNDEFINED) {
            setTileModeX(parseTileMode(tileMode));
            setTileModeY(parseTileMode(tileMode));
        }

        final int tileModeX =
                a.getInt(R.styleable.CustomImageView_riv_tile_mode_x, TILE_MODE_UNDEFINED);
        if (tileModeX != TILE_MODE_UNDEFINED) {
            setTileModeX(parseTileMode(tileModeX));
        }

        final int tileModeY =
                a.getInt(R.styleable.CustomImageView_riv_tile_mode_y, TILE_MODE_UNDEFINED);
        if (tileModeY != TILE_MODE_UNDEFINED) {
            setTileModeY(parseTileMode(tileModeY));
        }

        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(true);

        if (mMutateBackground) {
            //noinspection deprecation
            super.setBackgroundDrawable(mBackgroundDrawable);
        }

        a.recycle();
    }

    private static Shader.TileMode parseTileMode(int tileMode) {
        switch (tileMode) {
            case TILE_MODE_CLAMP:
                return Shader.TileMode.CLAMP;
            case TILE_MODE_REPEAT:
                return Shader.TileMode.REPEAT;
            case TILE_MODE_MIRROR:
                return Shader.TileMode.MIRROR;
            default:
                return null;
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        assert scaleType != null;

        if (mScaleType != scaleType) {
            mScaleType = scaleType;

            switch (scaleType) {
                case CENTER:
                case CENTER_CROP:
                case CENTER_INSIDE:
                case FIT_CENTER:
                case FIT_START:
                case FIT_END:
                case FIT_XY:
                    super.setScaleType(ScaleType.FIT_XY);
                    break;
                default:
                    super.setScaleType(scaleType);
                    break;
            }

            updateDrawableAttrs();
            updateBackgroundDrawableAttrs(false);
            invalidate();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mResource = 0;
        mDrawable = RoundedDrawable.fromDrawable(drawable);
        updateDrawableAttrs();
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mResource = 0;
        mDrawable = RoundedDrawable.fromBitmap(bm);
        updateDrawableAttrs();
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        if (mResource != resId) {
            mResource = resId;
            mDrawable = resolveResource();
            updateDrawableAttrs();
            super.setImageDrawable(mDrawable);
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    private Drawable resolveResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }

        Drawable d = null;

        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource);
            } catch (Exception e) {
                Log.w(TAG, "Unable to find resource: " + mResource, e);
                // Don't try again.
                mResource = 0;
            }
        }
        return RoundedDrawable.fromDrawable(d);
    }

    @Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        if (mBackgroundResource != resId) {
            mBackgroundResource = resId;
            mBackgroundDrawable = resolveBackgroundResource();
            setBackgroundDrawable(mBackgroundDrawable);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundDrawable = new ColorDrawable(color);
        setBackgroundDrawable(mBackgroundDrawable);
    }

    private Drawable resolveBackgroundResource() {
        Resources rsrc = getResources();
        if (rsrc == null) {
            return null;
        }

        Drawable d = null;

        if (mBackgroundResource != 0) {
            try {
                d = rsrc.getDrawable(mBackgroundResource);
            } catch (Exception e) {
                Log.w(TAG, "Unable to find resource: " + mBackgroundResource, e);
                // Don't try again.
                mBackgroundResource = 0;
            }
        }
        return RoundedDrawable.fromDrawable(d);
    }

    private void updateDrawableAttrs() {
        updateAttrs(mDrawable, mScaleType);
    }

    private void updateBackgroundDrawableAttrs(boolean convert) {
        if (mMutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable);
            }
            updateAttrs(mBackgroundDrawable, ScaleType.FIT_XY);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mColorFilter != cf) {
            mColorFilter = cf;
            mHasColorFilter = true;
            mColorMod = true;
            applyColorMod();
            invalidate();
        }
    }

    private void applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable.mutate();
            if (mHasColorFilter) {
                mDrawable.setColorFilter(mColorFilter);
            }
            // TODO: support, eventually...
            //mDrawable.setXfermode(mXfermode);
            //mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private void updateAttrs(Drawable drawable, ScaleType scaleType) {
        if (drawable == null) {
            return;
        }

        if (drawable instanceof RoundedDrawable) {
            ((RoundedDrawable) drawable)
                    .setScaleType(scaleType)
                    .setBorderWidth(mBorderWidth)
                    .setBorderColor(mBorderColor)
                    .setOval(mIsOval)
                    .setTileModeX(mTileModeX)
                    .setTileModeY(mTileModeY);

            if (mCornerRadii != null) {
                ((RoundedDrawable) drawable).setCornerRadius(
                        mCornerRadii[Corner.TOP_LEFT],
                        mCornerRadii[Corner.TOP_RIGHT],
                        mCornerRadii[Corner.BOTTOM_RIGHT],
                        mCornerRadii[Corner.BOTTOM_LEFT]);
            }

            applyColorMod();
        } else if (drawable instanceof LayerDrawable) {
            // loop through layers to and set drawable attrs
            LayerDrawable ld = ((LayerDrawable) drawable);
            for (int i = 0, layers = ld.getNumberOfLayers(); i < layers; i++) {
                updateAttrs(ld.getDrawable(i), scaleType);
            }
        }
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
        updateBackgroundDrawableAttrs(true);
        //noinspection deprecation
        super.setBackgroundDrawable(mBackgroundDrawable);
    }

    /**
     * @return the largest corner radius.
     */
    public float getCornerRadius() {
        return getMaxCornerRadius();
    }

    /**
     * @return the largest corner radius.
     */
    public float getMaxCornerRadius() {
        float maxRadius = 0;
        for (float r : mCornerRadii) {
            maxRadius = Math.max(r, maxRadius);
        }
        return maxRadius;
    }

    /**
     * Get the corner radius of a specified corner.
     *
     * @param corner the corner.
     * @return the radius.
     */
    public float getCornerRadius(@Corner int corner) {
        return mCornerRadii[corner];
    }

    /**
     * Set all the corner radii from a dimension resource id.
     *
     * @param resId dimension resource id of radii.
     */
    public void setCornerRadiusDimen(@DimenRes int resId) {
        float radius = getResources().getDimension(resId);
        setCornerRadius(radius, radius, radius, radius);
    }

    /**
     * Set the corner radius of a specific corner from a dimension resource id.
     *
     * @param corner the corner to set.
     * @param resId  the dimension resource id of the corner radius.
     */
    public void setCornerRadiusDimen(@Corner int corner, @DimenRes int resId) {
        setCornerRadius(corner, getResources().getDimensionPixelSize(resId));
    }

    /**
     * Set the corner radii of all corners in px.
     *
     * @param radius the radius to set.
     */
    public void setCornerRadius(float radius) {
        setCornerRadius(radius, radius, radius, radius);
    }

    /**
     * Set the corner radius of a specific corner in px.
     *
     * @param corner the corner to set.
     * @param radius the corner radius to set in px.
     */
    public void setCornerRadius(@Corner int corner, float radius) {
        if (mCornerRadii[corner] == radius) {
            return;
        }
        mCornerRadii[corner] = radius;

        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        invalidate();
    }

    /**
     * Set the corner radii of each corner individually. Currently only one unique nonzero value is
     * supported.
     *
     * @param topLeft     radius of the top left corner in px.
     * @param topRight    radius of the top right corner in px.
     * @param bottomRight radius of the bottom right corner in px.
     * @param bottomLeft  radius of the bottom left corner in px.
     */
    public void setCornerRadius(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        if (mCornerRadii[Corner.TOP_LEFT] == topLeft
                && mCornerRadii[Corner.TOP_RIGHT] == topRight
                && mCornerRadii[Corner.BOTTOM_RIGHT] == bottomRight
                && mCornerRadii[Corner.BOTTOM_LEFT] == bottomLeft) {
            return;
        }

        mCornerRadii[Corner.TOP_LEFT] = topLeft;
        mCornerRadii[Corner.TOP_RIGHT] = topRight;
        mCornerRadii[Corner.BOTTOM_LEFT] = bottomLeft;
        mCornerRadii[Corner.BOTTOM_RIGHT] = bottomRight;

        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        invalidate();
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(@DimenRes int resId) {
        setBorderWidth(getResources().getDimension(resId));
    }

    public void setBorderWidth(float width) {
        if (mBorderWidth == width) {
            return;
        }

        mBorderWidth = width;
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        invalidate();
    }

    @ColorInt
    public int getBorderColor() {
        return mBorderColor.getDefaultColor();
    }

    public void setBorderColor(@ColorInt int color) {
        setBorderColor(ColorStateList.valueOf(color));
    }

    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    public void setBorderColor(ColorStateList colors) {
        if (mBorderColor.equals(colors)) {
            return;
        }

        mBorderColor =
                (colors != null) ? colors : ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        if (mBorderWidth > 0) {
            invalidate();
        }
    }

    /**
     * Return true if this view should be oval and always set corner radii to half the height or
     * width.
     *
     * @return if this {@link CustomImageView} is set to oval.
     */
    public boolean isOval() {
        return mIsOval;
    }

    /**
     * Set if the drawable should ignore the corner radii set and always round the source to
     * exactly half the height or width.
     *
     * @param oval if this {@link CustomImageView} should be oval.
     */
    public void setOval(boolean oval) {
        mIsOval = oval;
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        invalidate();
    }

    public Shader.TileMode getTileModeX() {
        return mTileModeX;
    }

    public void setTileModeX(Shader.TileMode tileModeX) {
        if (this.mTileModeX == tileModeX) {
            return;
        }

        this.mTileModeX = tileModeX;
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        invalidate();
    }

    public Shader.TileMode getTileModeY() {
        return mTileModeY;
    }

    public void setTileModeY(Shader.TileMode tileModeY) {
        if (this.mTileModeY == tileModeY) {
            return;
        }

        this.mTileModeY = tileModeY;
        updateDrawableAttrs();
        updateBackgroundDrawableAttrs(false);
        invalidate();
    }

    /**
     * If {@code true}, we will also round the background drawable according to the settings on this
     * ImageView.
     *
     * @return whether the background is mutated.
     */
    public boolean mutatesBackground() {
        return mMutateBackground;
    }

    /**
     * Set whether the {@link CustomImageView} should round the background drawable according to
     * the settings in addition to the source drawable.
     *
     * @param mutate true if this view should mutate the background drawable.
     */
    public void mutateBackground(boolean mutate) {
        if (mMutateBackground == mutate) {
            return;
        }

        mMutateBackground = mutate;
        updateBackgroundDrawableAttrs(true);
        invalidate();
    }
}
