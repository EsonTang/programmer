/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.ui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.prize.app.BaseApplication;

/**
 * *
 * 基于Xutils的图片图片加载工具
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class ImageUtil {


//    public static Bitmap blurScale(Bitmap bmp) {
//        // String
//        // v=FileUtils.loadFileToString(FileUtils.getRawFile("hello.txt",mContext));
//
//        float scaleFactor = 3.8f;// Float.valueOf(v);
//        float radius = 10;
//        Bitmap overlay = Bitmap.createBitmap(
//                (int) (bmp.getWidth() / scaleFactor),
//                (int) (bmp.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(overlay);
//        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
//        Paint paint = new Paint();
//        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
//        canvas.drawBitmap(bmp, 0, 0, paint);
//        canvas.drawColor(0x88000000);
//        overlay = doBlur(overlay, (int) radius, true);
//        return overlay;
//    }

//    /**
//     * 图片模糊处理方法
//     *
//     * @param sentBitmap       原图
//     * @param radius    模糊度
//     * @param canReuseInBitmap  boolean
//     * @return  Bitmap
//     */
//    private static Bitmap doBlur(Bitmap sentBitmap, int radius,
//                                boolean canReuseInBitmap) {
//        Bitmap bitmap;
//        if (canReuseInBitmap) {
//            bitmap = sentBitmap;
//        } else {
//            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
//        }
//
//        if (radius < 1) {
//            return (null);
//        }
//
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//
//        int[] pix = new int[w * h];
//        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
//
//        int wm = w - 1;
//        int hm = h - 1;
//        int wh = w * h;
//        int div = radius + radius + 1;
//
//        int r[] = new int[wh];
//        int g[] = new int[wh];
//        int b[] = new int[wh];
//        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
//        int vmin[] = new int[Math.max(w, h)];
//
//        int divsum = (div + 1) >> 1;
//        divsum *= divsum;
//        int dv[] = new int[256 * divsum];
//        for (i = 0; i < 256 * divsum; i++) {
//            dv[i] = (i / divsum);
//        }
//
//        yw = yi = 0;
//
//        int[][] stack = new int[div][3];
//        int stackpointer;
//        int stackstart;
//        int[] sir;
//        int rbs;
//        int r1 = radius + 1;
//        int routsum, goutsum, boutsum;
//        int rinsum, ginsum, binsum;
//
//        for (y = 0; y < h; y++) {
//            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
//            for (i = -radius; i <= radius; i++) {
//                p = pix[yi + Math.min(wm, Math.max(i, 0))];
//                sir = stack[i + radius];
//                sir[0] = (p & 0xff0000) >> 16;
//                sir[1] = (p & 0x00ff00) >> 8;
//                sir[2] = (p & 0x0000ff);
//                rbs = r1 - Math.abs(i);
//                rsum += sir[0] * rbs;
//                gsum += sir[1] * rbs;
//                bsum += sir[2] * rbs;
//                if (i > 0) {
//                    rinsum += sir[0];
//                    ginsum += sir[1];
//                    binsum += sir[2];
//                } else {
//                    routsum += sir[0];
//                    goutsum += sir[1];
//                    boutsum += sir[2];
//                }
//            }
//            stackpointer = radius;
//
//            for (x = 0; x < w; x++) {
//
//                r[yi] = dv[rsum];
//                g[yi] = dv[gsum];
//                b[yi] = dv[bsum];
//
//                rsum -= routsum;
//                gsum -= goutsum;
//                bsum -= boutsum;
//
//                stackstart = stackpointer - radius + div;
//                sir = stack[stackstart % div];
//
//                routsum -= sir[0];
//                goutsum -= sir[1];
//                boutsum -= sir[2];
//
//                if (y == 0) {
//                    vmin[x] = Math.min(x + radius + 1, wm);
//                }
//                p = pix[yw + vmin[x]];
//
//                sir[0] = (p & 0xff0000) >> 16;
//                sir[1] = (p & 0x00ff00) >> 8;
//                sir[2] = (p & 0x0000ff);
//
//                rinsum += sir[0];
//                ginsum += sir[1];
//                binsum += sir[2];
//
//                rsum += rinsum;
//                gsum += ginsum;
//                bsum += binsum;
//
//                stackpointer = (stackpointer + 1) % div;
//                sir = stack[(stackpointer) % div];
//
//                routsum += sir[0];
//                goutsum += sir[1];
//                boutsum += sir[2];
//
//                rinsum -= sir[0];
//                ginsum -= sir[1];
//                binsum -= sir[2];
//
//                yi++;
//            }
//            yw += w;
//        }
//        for (x = 0; x < w; x++) {
//            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
//            yp = -radius * w;
//            for (i = -radius; i <= radius; i++) {
//                yi = Math.max(0, yp) + x;
//
//                sir = stack[i + radius];
//
//                sir[0] = r[yi];
//                sir[1] = g[yi];
//                sir[2] = b[yi];
//
//                rbs = r1 - Math.abs(i);
//
//                rsum += r[yi] * rbs;
//                gsum += g[yi] * rbs;
//                bsum += b[yi] * rbs;
//
//                if (i > 0) {
//                    rinsum += sir[0];
//                    ginsum += sir[1];
//                    binsum += sir[2];
//                } else {
//                    routsum += sir[0];
//                    goutsum += sir[1];
//                    boutsum += sir[2];
//                }
//
//                if (i < hm) {
//                    yp += w;
//                }
//            }
//            yi = x;
//            stackpointer = radius;
//            for (y = 0; y < h; y++) {
//                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
//                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
//                        | (dv[gsum] << 8) | dv[bsum];
//
//                rsum -= routsum;
//                gsum -= goutsum;
//                bsum -= boutsum;
//
//                stackstart = stackpointer - radius + div;
//                sir = stack[stackstart % div];
//
//                routsum -= sir[0];
//                goutsum -= sir[1];
//                boutsum -= sir[2];
//
//                if (x == 0) {
//                    vmin[y] = Math.min(y + r1, hm) * w;
//                }
//                p = x + vmin[y];
//
//                sir[0] = r[p];
//                sir[1] = g[p];
//                sir[2] = b[p];
//
//                rinsum += sir[0];
//                ginsum += sir[1];
//                binsum += sir[2];
//
//                rsum += rinsum;
//                gsum += ginsum;
//                bsum += binsum;
//
//                stackpointer = (stackpointer + 1) % div;
//                sir = stack[stackpointer];
//
//                routsum += sir[0];
//                goutsum += sir[1];
//                boutsum += sir[2];
//
//                rinsum -= sir[0];
//                ginsum -= sir[1];
//                binsum -= sir[2];
//
//                yi += w;
//            }
//        }
//
//        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
//
//        return (bitmap);
//    }

    // PRIZE-launcher3-zhouerlong-2015-8-3-start

    /**
     * 将icon和mask图标重叠 如果没有达到mask重叠指标将用icon_bg和icon重叠
     *
     * @param orignal  Bitmap
     * @param pixels  像素
     * @param mask  Drawable
     * @param c  Context
     * @param bg  Drawable
     * @return  Bitmap
     */
    public static Bitmap toMaskBitmap(Bitmap orignal, int pixels,
                                      Drawable mask, Context c, Drawable bg) {
        Bitmap maskbmp = drawableToBitmap(mask);
        Bitmap bgs = drawableToBitmap(bg);
        int orignalW = orignal.getWidth();
        int orignalH = orignal.getHeight();
        if (neddResizeIcon(orignal, pixels)) {
            orignal = cropCenter(orignal, orignal.getWidth() - pixels,
                    orignal.getHeight() - pixels);
            orignal = resize(orignal, orignalW, orignalH);
            return createMaskImage(orignal, maskbmp);
        }
        return doodle(orignal, bgs);
    }

    // PRIZE-launcher3-zhouerlong-2015-8-3-end

//    /**
//     * 创建一些launcher必须用到的辅助图标
//     *
//     * @param context
//     */
//    public static void createHelpericon(Context context) {
//        icon_bg = context.getResources().getDrawable(R.drawable.bg_icon);
//        mask_icon = context.getResources().getDrawable(R.drawable.mask);// PRIZE-launcher3-zhouerlong-2015-8-3-start
//    }


    private static int[] getBitmapCantPixel(Bitmap bit, int resize) {
        int width = bit.getWidth();
        int height = bit.getHeight();
        int pixels[] = new int[4];

        pixels[0] = bit.getPixel(resize, resize); // left & top
        pixels[1] = bit.getPixel(width - resize, resize); // right& top
        pixels[2] = bit.getPixel(resize, height - resize); // left& bottom
        pixels[3] = bit.getPixel(width - resize, height - resize); // right

        return pixels;
    }

    private static Bitmap dividePart(Bitmap bmp, Rect src) {
        int width = src.width();
        int height = src.height();
        Rect des = new Rect(0, 0, width, height);
        Bitmap croppedImage = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedImage);
        canvas.drawBitmap(bmp, src, des, null);
        return croppedImage;
    }

    private static Bitmap cropCenter(Bitmap bm, int newWidth, int newHeight) {
        int startWidth = (bm.getWidth() - newWidth) / 2;
        int startHeight = ((bm.getHeight() - newHeight) / 2);
        Rect src = new Rect(startWidth, startHeight, startWidth + newWidth,
                startHeight + newHeight);

        return dividePart(bm, src);
    }

    // A by zel

//    private static int[] getBitmapCantPixel(Drawable icon, int resize) {
//        Bitmap bit = drawableToBitmap(icon);
//        int width = bit.getWidth();
//        int height = bit.getHeight();
//        int pixels[] = new int[5];
//        int offsetX = width / resize;
//        int offsetY = height / resize;
//
//        pixels[0] = bit.getPixel(offsetX, offsetY);
//        pixels[1] = bit.getPixel(width - offsetX, offsetY);
//        pixels[2] = bit.getPixel(offsetX, height - offsetY);
//        pixels[3] = bit.getPixel(width - offsetX, height - offsetY);
//        pixels[4] = bit.getPixel(offsetX, height / 2);
//
//        return pixels;
//    }

    private final static int TRANSPARENT = 0x00;

    private static boolean neddResizeIcon(Bitmap bit, int resize) {
        int pixels[] = getBitmapCantPixel(bit, resize);

        return !(pixels[0] == TRANSPARENT && pixels[1] == TRANSPARENT && pixels[2] == TRANSPARENT && pixels[3] == TRANSPARENT);
//        boolean need = (pixels[0] == TRANSPARENT && pixels[1] == TRANSPARENT
//                && pixels[2] == TRANSPARENT && pixels[3] == TRANSPARENT) ? false
//                : true;

//        return need;
    }

//    public static Bitmap resizeIcon(Drawable icon, int w, int h, int resize) {
//        if (neddResizeIcon(icon, resize)) {
//            return resize(icon, w, h);
//        } else {
//            return null;
//        }
//    }

//    // 是否需要重新设计大小
//    static boolean neddResizeIcon(Drawable icon, int resize) {
//        int pixels[] = getBitmapCantPixel(icon, resize);
//
//        boolean need = (pixels[0] != TRANSPARENT && pixels[1] != TRANSPARENT
//                && pixels[2] != TRANSPARENT && pixels[3] != TRANSPARENT) ? true
//                : false;
//
//        return need;
//    }

//    static public Bitmap drawableToBitmap1(Drawable drawable) {
//        Bitmap bitmap = Bitmap
//                .createBitmap(
//                        drawable.getIntrinsicWidth(),
//                        drawable.getIntrinsicHeight(),
//                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//                                : Bitmap.Config.RGB_565);
//        Canvas canvas = new Canvas(bitmap);
//        // canvas.setBitmap(bitmap);
//        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
//                drawable.getIntrinsicHeight());
//        drawable.draw(canvas);
//        return bitmap;
//
//    }

    public static  Bitmap drawableToBitmap(Drawable drawable) {

        return ((BitmapDrawable) drawable).getBitmap();

    }
//
//    public static void saveBefore(String path) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
//        options.inJustDecodeBounds = false;
//        int be = (int) (options.outHeight / (float) 200);
//        if (be <= 0)
//            be = 1;
//        options.inSampleSize = 2;
//        bitmap = BitmapFactory.decodeFile(path, options);
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        // savePNG_After(bitmap,path);
//        saveJPGE_After(bitmap, path);
//    }

//    private static Bitmap resize(Drawable icon, int w, int h) {
//        Bitmap BitmapOrg = drawableToBitmap(icon);
//        int width = BitmapOrg.getWidth();
//        int height = BitmapOrg.getHeight();
//        float scaleWidth = ((float) w) / width;
//        float scaleHeight = ((float) h) / height;
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
//        // recreate the new Bitmap
//        return Bitmap.createBitmap(BitmapOrg, 0, 0, width,
//                height, matrix, true);
//    }

    private static Bitmap resize(Bitmap bm, int w, int h) {

        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) w) / width;
        float scaleHeight = ((float) h) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width,
                height, matrix, true);
        Bitmap bg = Bitmap.createBitmap(w, h, Config.ARGB_8888);// modify by
        // zhouerlong
        if (resizedBitmap != null) {
            return doodle(resizedBitmap, bg);
        }
        return null;
    }

//    public static void savePNG_After(Drawable icon, String name) {
//        name += ".png";
//        Bitmap bitmap = drawableToBitmap(icon);
//        File file = new File(name);
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
//                out.flush();
//                out.close();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public static void savePNG_After(Bitmap bitmap, String name) {
//        // add by zhouerlong 0728 start
//        File dir = new File(Environment.getExternalStorageDirectory(), "desk");
//        if (!dir.exists()) {
//            dir.mkdir();
//        }
//        File file = new File(Environment.getExternalStorageDirectory(), name);
//        // add by zhouerlong 0728 end
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
//                out.flush();
//                out.close();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void saveJPGE_After(Bitmap bitmap, String path) {
//        File file = new File(path);
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
//                out.flush();
//                out.close();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // M by zhouerlong
    private static Bitmap doodle(Bitmap src, Bitmap bg) {
        Bitmap newb = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(newb);
        canvas.drawBitmap(bg, 0, 0, null);

        canvas.drawBitmap(src, (bg.getWidth() - src.getWidth()),
                (bg.getHeight()-src.getHeight() ), null);
//        canvas.drawBitmap(src, (bg.getWidth() - src.getWidth()) / 2,
//                (bg.getHeight() - src.getHeight()) / 2, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        // src.recycle();
        // src = null;

        return newb;
    }

    // PRIZE-launcher3-zhouerlong-2015-8-3-start

    /**
     * 创建遮罩图片
     *
     * @param source 原图
     * @param mask   遮罩图片 可以实现不同形状的图片
     * @return  Bitmap
     */
    public static Bitmap createMaskImage(Bitmap source, Bitmap mask) {
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
                Config.ARGB_8888);
        // 将遮罩层的图片放到画布中
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));// 叠加重复的部分，
        mCanvas.drawBitmap(source, 0, 0, null);

        mCanvas.drawBitmap(mask, (source.getWidth() - mask.getWidth()) / 2,
                (source.getHeight() - mask.getHeight()) / 2, paint);
        paint.setXfermode(null);
        return result;
    }

    // PRIZE-launcher3-zhouerlong-2015-8-3-end

    // add by zhouerlong start 0728
//
//    /**
//     * 实现图片组合
//     *
//     * @param decade :十位数字
//     * @param unit   :各位数字
//     * @param bg     :背景
//     * @return
//     */
//    public static Bitmap doodlesrc(Bitmap decade, Bitmap unit, Bitmap bg,
//                                   Bitmap weekIcon) {
//        Bitmap newb = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
//                Config.ARGB_8888);
//        Canvas canvas = new Canvas(newb);
//        canvas.drawBitmap(bg, 0, 0, null);
//        int decadeLeft = bg.getWidth() / 4;
//        int weekTop = (int) (bg.getWidth() / 2.6f);
//        int weekLeft = 0;
//        int decadeTop = -bg.getWidth() / 8;
//        int unitLeft = decadeLeft + decade.getWidth();
//        int unittop = -bg.getWidth() / 8;
//        Rect decadeRect = new Rect(decadeLeft, decadeTop, decadeLeft
//                + decade.getWidth(), decade.getHeight() + decadeTop);
//        Rect unitRect = new Rect(unitLeft, unittop, unitLeft + unit.getWidth(),
//                unit.getHeight() + unittop);
//        canvas.drawBitmap(decade, null, decadeRect, null);
//        canvas.drawBitmap(unit, null, unitRect, null);
//        if (weekIcon != null) {
//
//            Rect weekRect = new Rect(weekLeft, weekTop, weekLeft
//                    + weekIcon.getWidth(), weekIcon.getHeight() + weekTop);
//            canvas.drawBitmap(weekIcon, null, weekRect, null);
//        }
//        canvas.save(Canvas.ALL_SAVE_FLAG);
//        canvas.restore();
//
//        return newb;
//    }
//
//    public static Bitmap bytesToBimap(byte[] b) {
//        if (b.length != 0) {
//            return BitmapFactory.decodeByteArray(b, 0, b.length);
//        } else {
//            return null;
//        }
//    }
//
//    public static byte[] bitmapToBytes(Bitmap bm) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        return baos.toByteArray();
//    }

    public static Drawable bitmapToDrawable(Bitmap bmp) {
        return new BitmapDrawable(BaseApplication.curContext.getResources(), bmp);
    }


//    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
//
//        Matrix m = new Matrix();
//        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
//        float targetX, targetY;
//        if (orientationDegree == 90) {
//            targetX = bm.getHeight();
//            targetY = 0;
//        } else {
//            targetX = bm.getHeight();
//            targetY = bm.getWidth();
//        }
//
//        final float[] values = new float[9];
//        m.getValues(values);
//
//        float x1 = values[Matrix.MTRANS_X];
//        float y1 = values[Matrix.MTRANS_Y];
//
//        m.postTranslate(targetX - x1, targetY - y1);
//        try {
//            Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
//            Paint paint = new Paint();
//            Canvas canvas = new Canvas(bm1);
//            canvas.drawBitmap(bm, m, paint);
//
//        } catch (OutOfMemoryError ex) {
//
//        }
//
//        return null;
//    }

    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
        return null;
    }

//    /**
//
//     * 设置底部tab图标
//
//     * @paramradioButton控件
//
//     * @paramdrawableNormal常态时的图片
//
//     * @paramdrawableSelect选中时的图片
//
//     */
//
//    public static void setSelectorDrawable(CheckBox cbButton, Drawable drawableNormal, Drawable drawableSelect){
//
//        StateListDrawable drawable =new StateListDrawable();
//        //选中
//       drawable.addState(new int[]{android.R.attr.state_checked},drawableSelect);
//        //未选中
//        drawable.addState(new int[]{-android.R.attr.state_checked},drawableNormal);
//        if (Build.VERSION.SDK_INT >= 16) {
//            cbButton.setBackground(drawable);
//        }else{
//            cbButton.setBackgroundDrawable(drawable);
//        }
//    }
}
