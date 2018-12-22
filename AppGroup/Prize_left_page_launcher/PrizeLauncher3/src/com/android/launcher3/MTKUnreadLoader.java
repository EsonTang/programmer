package com.android.launcher3;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.android.gallery3d.util.LogUtils;
import com.android.internal.util.XmlUtils;
import com.android.launcher3.R;
import com.android.launcher3.view.WechatBubbleTextView;
import com.lqsoft.lqtheme.LqShredPreferences;
import com.mediatek.launcher3.ext.LauncherLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

class UnreadSupportShortcut {
    public UnreadSupportShortcut(String pkgName, String clsName, String keyString, int type) {
        mComponent = new ComponentName(pkgName, clsName);
        mKey = keyString;
        mShortcutType = type;
        mUnreadNum = 0;
    }

    ComponentName mComponent;
    String mKey;
    int mShortcutType;
    int mUnreadNum;

    @Override
    public String toString() {
        return "{UnreadSupportShortcut[" + mComponent + "], key = " + mKey + ",type = "
                + mShortcutType + ",unreadNum = " + mUnreadNum + "}";
    }
}

/**
 * M: This class is a util class, implemented to do the following two things,:
 *
 * 1.Read config xml to get the shortcuts which support displaying unread number,
 * then get the initial value of the unread number of each component and update
 * shortcuts and folders through callbacks implemented in Launcher.
 *
 * 2. Receive unread broadcast sent by application, update shortcuts and folders in
 * workspace, hot seat and update application icons in app customize paged view.
 */
public class MTKUnreadLoader extends BroadcastReceiver {
    private static final String TAG = "MTKUnreadLoader";
    private static final String TAG_UNREADSHORTCUTS = "unreadshortcuts";

    private static final ArrayList<UnreadSupportShortcut> UNREAD_SUPPORT_SHORTCUTS = new ArrayList<UnreadSupportShortcut>();

    private static int sUnreadSupportShortcutsNum = 0;
    public static int sAppInstanceIndex = 0;
    private static final Object LOG_LOCK = new Object();

    private Context mContext;

    private WeakReference<UnreadCallbacks> mCallbacks;
	private static int mAppInstanceIndex=-1;
    public static String packageName;

    public MTKUnreadLoader(Context context) {
        mContext = context;
    }

    
    private int toFiterUnreadNum(String title) {
    	int unreadNum=1;
        if(title!=null&& title.contains("[")) {
             int start=title.indexOf("[");
             int end=title.indexOf("]");
             if(start!=-1&&end!=-1) {
              String titles=  title.substring(start+1, end-1);
               try {
                   unreadNum = Integer.valueOf(titles);
			} catch (Exception e) {
				unreadNum=1;
			}
             }
        }
        return unreadNum;
    }
    
    
    private int toFilterQQUnreadNum(String title,int un) {
    	int unreadNum=un;
    	if(title!=null&&Utilities.hasDigit(title))  {
    		String num =Utilities.getNumbers(title) ;
    		if(num!=null) {
    		 try {
    			 if(title.contains("条")) {
                     unreadNum = Integer.valueOf(num);
    			 }
			} catch (Exception e) {
				unreadNum=1;
			}
    		}
    	}
        return unreadNum;
    }
    

    
	private int toFilterWechatUnreadUum(String title,int un) {
		int unreadNum = un;
		if (title != null) {
			String titles = Utilities.getFirstNumbers(title);
			try {
				if(titles!=null) {
					boolean hasBegin = title.contains("[")&&title.indexOf("[")==0;
					boolean hasnum = Utilities.isNumeric(title.substring(1, 1));
					boolean hasEnd = title.contains("]")&&title.indexOf("]")==titles.length()+2;
					if(hasBegin&&hasnum&&hasEnd)
					unreadNum = Integer.valueOf(titles);
				}
			} catch (Exception e) {
				unreadNum = 1;
			}
		}
		return unreadNum;
	}
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        int appInstanceIndex=-1;
        if(Launcher.isSupportClone) {
        	 appInstanceIndex=intent.getAppInstanceIndex();
        	 
         	if(intent.getAppInstanceIndex()==0) {
         		appInstanceIndex=-1;
         	}
     		mAppInstanceIndex=appInstanceIndex;
        }
        if (Intent.ACTION_UNREAD_CHANGED.equals(action)) {
            final ComponentName componentName = (ComponentName) intent
                    .getExtra(Intent.EXTRA_UNREAD_COMPONENT);
            String title = intent.getStringExtra("name");
            Bitmap mMessageIcon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
            packageName = componentName.getPackageName();
             int unreadNum = intent.getIntExtra(Intent.EXTRA_UNREAD_NUMBER, -1);

             PendingIntent pedingIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

             
			if (componentName.getPackageName().equals("com.tencent.mm")&&pedingIntent != null && pedingIntent.getIntent() != null
					) {
				if(pedingIntent.getIntent().getComponent()!=null&&Launcher.isSupportClone) {
					pedingIntent.getIntent().setAppInstanceIndex(appInstanceIndex);
					if (!pedingIntent.getIntent().getComponent().toShortString()
							.contains("LauncherUI")) {
						return;
					}
				}
				
				if(pedingIntent.getIntent().getComponent()==null&&title!=null) {
					return;
				}
				
				
			}
			
			
			if (Launcher.isSupportClone&&componentName.getPackageName().equals("com.tencent.mobileqq")&&pedingIntent != null && pedingIntent.getIntent() != null) {
				pedingIntent.getIntent().setAppInstanceIndex(appInstanceIndex);
				if(pedingIntent.getIntent().getComponent()!=null) {
				if (!pedingIntent.getIntent().getComponent().toShortString()
						.contains("SplashActivity")|| (title!=null&&title.contains("QQ正在后台运行"))) {
					return;
				}
				}
				if(pedingIntent.getIntent().getComponent()==null&&title!=null) {
					return;
				}
			}
         /*   if(title!=null&& title.contains("[")) {
                 int start=title.indexOf("[");
                 int end=title.indexOf("]");
                 if(start!=-1&&end!=-1) {
                  String titles=  title.substring(start+1, end-1);
                   try {
                       unreadNum = Integer.valueOf(titles);
				} catch (Exception e) {
					unreadNum=1;
				}
                 }
            }*/
             if(componentName.getPackageName().equals("com.tencent.mm")) {
            	 unreadNum= toFilterWechatUnreadUum(title,unreadNum);
             }else if(componentName.getPackageName().equals("com.tencent.mobileqq")) {
            	 unreadNum= toFilterQQUnreadNum(title,unreadNum);
             }
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "Receive unread broadcast: componentName = " + componentName
                        + ", unreadNum = " + unreadNum + ", mCallbacks = " + mCallbacks
                        + getUnreadSupportShortcutInfo());
            }
            
            LogUtils.i("zhouerlong", "unread:::::"+title+unreadNum);

            if (mCallbacks != null && componentName != null && unreadNum != -1) {
                final int index = supportUnreadFeature(componentName);
                if (index >= 0) {
                    boolean ret = setUnreadNumberAt(index, unreadNum);
                    if (ret ||title !=null||unreadNum==0) {
                        final UnreadCallbacks callbacks = mCallbacks.get();
                        if (callbacks != null) {
                            callbacks.bindComponentUnreadChanged(componentName, unreadNum,title,mMessageIcon, pedingIntent, appInstanceIndex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(UnreadCallbacks callbacks) {
    	if(mCallbacks!=null&&mCallbacks.get()!=null) {
    		mCallbacks.clear();
    		mCallbacks=null;
    	}
        mCallbacks = new WeakReference<UnreadCallbacks>(callbacks);
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "initialize: callbacks = " + callbacks + ", mCallbacks = " + mCallbacks);
        }
    }

    /**
     * Load and initialize unread shortcuts.
     *
     * @param context
     */
    void loadAndInitUnreadShortcuts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                loadUnreadSupportShortcuts();
                initUnreadNumberFromSystem();
                return null;
            }

            @Override
            protected void onPostExecute(final Void result) {
                if (mCallbacks != null) {
                    UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindUnreadInfoIfNeeded();
                    }
                }
            }
        }.execute();
    }

    /**
     * Initialize unread number by querying system settings provider.
     *
     * @param context
     */
    private void initUnreadNumberFromSystem() {
        final ContentResolver cr = mContext.getContentResolver();
        final int shortcutsNum = sUnreadSupportShortcutsNum;
        UnreadSupportShortcut shortcut = null;
        for (int i = 0; i < shortcutsNum; i++) {
            shortcut = UNREAD_SUPPORT_SHORTCUTS.get(i);
            try {
                shortcut.mUnreadNum = android.provider.Settings.System.getInt(cr, shortcut.mKey);
                if (LauncherLog.DEBUG_UNREAD) {
                    LauncherLog.d(TAG, "initUnreadNumberFromSystem: key = " + shortcut.mKey
                            + ", unreadNum = " + shortcut.mUnreadNum);
                }
            } catch (android.provider.Settings.SettingNotFoundException e) {
                LauncherLog.e(TAG, "initUnreadNumberFromSystem SettingNotFoundException key = "
                        + shortcut.mKey + ", e = " + e.getMessage());
            }
        }
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "initUnreadNumberFromSystem end:" + getUnreadSupportShortcutInfo());
        }
    }

    private void loadUnreadSupportShortcuts() {
        long start = System.currentTimeMillis();
        if (LauncherLog.DEBUG_PERFORMANCE) {
            LauncherLog.d(TAG, "loadUnreadSupportShortcuts begin: start = " + start);
        }

        // Clear all previous parsed unread shortcuts.
        UNREAD_SUPPORT_SHORTCUTS.clear();

        try {
            XmlResourceParser parser = mContext.getResources().getXml(
                    R.xml.unread_support_shortcuts);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_UNREADSHORTCUTS);

            final int depth = parser.getDepth();

            int type = -1;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.UnreadShortcut);
                synchronized (LOG_LOCK) {
                    UNREAD_SUPPORT_SHORTCUTS.add(new UnreadSupportShortcut(a
                            .getString(R.styleable.UnreadShortcut_unreadPackageName), a
                            .getString(R.styleable.UnreadShortcut_unreadClassName), a
                            .getString(R.styleable.UnreadShortcut_unreadKey), a.getInt(
                            R.styleable.UnreadShortcut_unreadType, 0)));
                }
                a.recycle();

            }
        } catch (XmlPullParserException e) {
            LauncherLog.w(TAG, "Got XmlPullParserException while parsing unread shortcuts.", e);
        } catch (IOException e) {
            LauncherLog.w(TAG, "Got IOException while parsing unread shortcuts.", e);
        }
        sUnreadSupportShortcutsNum = UNREAD_SUPPORT_SHORTCUTS.size();
        if (LauncherLog.DEBUG_PERFORMANCE) {
            LauncherLog.d(TAG, "loadUnreadSupportShortcuts end: time used = "
                    + (System.currentTimeMillis() - start) + ",sUnreadSupportShortcutsNum = "
                    + sUnreadSupportShortcutsNum + getUnreadSupportShortcutInfo());
        }
    }

    /**
     * Get unread support shortcut information, since the information are stored
     * in an array list, we may query it and modify it at the same time, a lock
     * is needed.
     *
     * @return
     */
    private static String getUnreadSupportShortcutInfo() {
        String info = " Unread support shortcuts are ";
        synchronized (LOG_LOCK) {
            info += UNREAD_SUPPORT_SHORTCUTS.toString();
        }
        return info;
    }

    /**
     * Whether the given component support unread feature.
     *
     * @param component
     * @return
     */
    static int supportUnreadFeature(ComponentName component) {
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "supportUnreadFeature: component = " + component);
        }
        if (component == null) {
            return -1;
        }

        final int size = UNREAD_SUPPORT_SHORTCUTS.size();
        for (int i = 0, sz = size; i < sz; i++) {
            if (UNREAD_SUPPORT_SHORTCUTS.get(i).mComponent.equals(component)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Set the unread number of the item in the list with the given unread number.
     *
     * @param index
     * @param unreadNum
     * @return
     */
    static synchronized boolean setUnreadNumberAt(int index, int unreadNum) {
        if (index >= 0 || index < sUnreadSupportShortcutsNum) {
            if (LauncherLog.DEBUG_UNREAD) {
                LauncherLog.d(TAG, "setUnreadNumberAt: index = " + index + ",unreadNum = " + unreadNum
                        + getUnreadSupportShortcutInfo());
            }
            if (UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum != unreadNum) {
                UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum = unreadNum;
                return true;
            }
        }
        return false;
    }

    /**
     * Get unread number of application at the given position in the supported
     * shortcut list.
     *
     * @param index
     * @return
     */
    static synchronized int getUnreadNumberAt(int index) {
        if (index < 0 || index >= sUnreadSupportShortcutsNum) {
            return 0;
        }
        if (LauncherLog.DEBUG_UNREAD) {
            LauncherLog.d(TAG, "getUnreadNumberAt: index = " + index
                    + getUnreadSupportShortcutInfo());
        }
        return UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum;
    }

    /**
     * Get unread number for the given component.
     *
     * @param component
     * @return
     */
    static int getUnreadNumberOfComponent(ComponentName component) {
        final int index = supportUnreadFeature(component);
        return getUnreadNumberAt(index);
    }
    
    static void drawUnreadMessageIcon(Canvas canvas,Bitmap icon,View view,float progress) {

		if (LqShredPreferences.isLqtheme(view.getContext()) && icon != null) {
			icon = IconCache.getLqIcon(null, icon, true, "");
			if(icon!=null) {

			final int scrollX = view.getScrollX();
			final int scrollY = view.getScrollX();
			int bitWidth = Utilities.sIconWidth;
			int bitHeight = Utilities.sIconWidth;
			int w = bitWidth/2;
			int h =bitHeight/2;
			icon=ImageUtils.resize(icon, w, h);
			canvas.translate(scrollX, scrollY);
			int x = (view.getWidth() - bitWidth) / 2;
			int y = view.getPaddingTop();
			y+=bitHeight-h;
			canvas.save();

        	canvas.scale(progress, progress,w/2+x,h/2+y);
			canvas.drawBitmap(icon, x, y, null);
			canvas.restore();
			}
		}
	}
    
    
    static void drawWechatUnreadMessage(Canvas canvas,Bitmap icon,View view,String title,Bitmap  bg) {

		if (LqShredPreferences.isLqtheme(view.getContext()) && icon != null) {
			icon = IconCache.getLqIcon(null, icon, true, "");
			if (icon != null) {

				final int scrollX = view.getScrollX();
				final int scrollY = view.getScrollX();

				int bitWidth = icon.getWidth();
				int bitHeight = icon.getHeight();
				int w = bitWidth / 4;
				int h = bitHeight / 4;
				icon = ImageUtils.resize(icon, w, h);
				canvas.translate(scrollX, scrollY);

				int bgW = bg.getWidth();
				int bgH = bg.getHeight();
				int bgX = view.getWidth() - bgW;
				int bgY = 0;
				canvas.save();
				canvas.drawBitmap(bg, bgX, bgY, null);
				canvas.restore();

				int iconX = bgX+w/4;
				int iconY = bgY + bgH / 2 - h/2-h/4;
				canvas.save();
				canvas.drawBitmap(icon, iconX, iconY, null);
				canvas.restore();

				if(title!=null) {
				canvas.save();
				Paint p = new Paint();
				String familyName = "宋体";
				Typeface font = Typeface.create(familyName, Typeface.BOLD);
				p.setColor(Color.BLACK);
				p.setTypeface(font);
				p.setTextSize(18);
				int titleX = iconX+w;
				int titleY =iconY+h;
				canvas.drawText(title, titleX, titleY, p);
				canvas.restore();
				}

			}
		}
	}

    /**
     * Draw unread number for the given icon.
     *
     * @param canvas
     * @param icon
     * @return
     */
    static void drawUnreadEventIfNeed(Canvas canvas, View icon,float progress) {
        ItemInfo info = (ItemInfo)icon.getTag();
        if(info!=null&&info instanceof ShortcutInfo&&info.getIntent()!=null) {
        	/*if(Launcher.isSupportClone&&info.getIntent().getAppInstanceIndex()!=mAppInstanceIndex) {
        		return ;
        	}*/
        }
        if (info != null && (info.unreadNum > 0|| info.unreadTitle != null)) {

           /* if(Launcher.isSupportClone) {
            	if(info instanceof ShortcutInfo&&info.getIntent()!=null&&info.getIntent().getAppInstanceIndex()==1) {
            		return;
            	}
            }*/
        	if(info.messageIcon!=null&&icon instanceof WechatBubbleTextView) {
        		drawUnreadMessageIcon(canvas,info.messageIcon,icon,progress);
//       		drawWechatUnreadMessage(canvas,info.messageIcon,icon,info.unreadTitle,ImageUtils.drawableToBitmap1(d));
        	}
            Resources res = icon.getContext().getResources();
            /// M: Meature sufficent width for unread text and background image
            Paint unreadTextNumberPaint = new Paint();

//			unreadTextNumberPaint.setTypeface(Launcher.mTypeface);
            unreadTextNumberPaint.setTextSize(res.getDimension(R.dimen.unread_text_number_size));
            unreadTextNumberPaint.setColor(0xffffffff);
            unreadTextNumberPaint.setTextAlign(Paint.Align.CENTER);
            unreadTextNumberPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

            Paint unreadTextPlusPaint = new Paint(unreadTextNumberPaint);
            unreadTextPlusPaint.setTypeface(Launcher.mTypeface);
            unreadTextPlusPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            unreadTextPlusPaint.setTextSize(res.getDimension(R.dimen.unread_text_plus_size));

            String unreadTextNumber;
            String unreadTextPlus = "+";
            Rect unreadTextNumberBounds = new Rect(0, 0, 0, 0);
            Rect unreadTextPlusBounds = new Rect(0, 0, 0, 0);
            if (info.unreadNum > Launcher.MAX_UNREAD_COUNT) {
                unreadTextNumber = String.valueOf(Launcher.MAX_UNREAD_COUNT);
                unreadTextPlusPaint.getTextBounds(unreadTextPlus, 0, unreadTextPlus.length(), unreadTextPlusBounds);
            } else {
                unreadTextNumber = String.valueOf(info.unreadNum);
            }
            unreadTextNumberPaint.getTextBounds(unreadTextNumber, 0, unreadTextNumber.length(), unreadTextNumberBounds);
            int textHeight = unreadTextNumberBounds.height();
            int textWidth = unreadTextNumberBounds.width() + unreadTextPlusBounds.width();

            /// M: Draw unread background image.
            Drawable unreadBgNinePatchDrawable =  res.getDrawable(R.drawable.ic_newevents_numberindication);
            if(info.unreadNum>9) {
            	unreadBgNinePatchDrawable=res.getDrawable(R.drawable.ic_newevents_numberindication_1);
            }
            int unreadBgWidth = (int) (unreadBgNinePatchDrawable.getIntrinsicWidth()/*/1.5f*/);
            int unreadBgHeight = (int) (unreadBgNinePatchDrawable.getIntrinsicHeight()/*/1.5f*/);

            int unreadMinWidth = (int) res.getDimension(R.dimen.unread_minWidth);
            if (unreadBgWidth < unreadMinWidth) {
                unreadBgWidth = unreadMinWidth;
            }
            int unreadTextMargin = (int) res.getDimension(R.dimen.unread_text_margin);
            if (unreadBgWidth < textWidth + unreadTextMargin) {
                unreadBgWidth = textWidth + unreadTextMargin;
            }
            if (unreadBgHeight < textHeight) {
                unreadBgHeight = textHeight;
            }
            Rect unreadBgBounds = new Rect(0, 0, unreadBgWidth, unreadBgHeight);
            unreadBgNinePatchDrawable.setBounds(unreadBgBounds);

            int unreadMarginTop = 0;
            int unreadMarginRight = 0;
            if (info instanceof ShortcutInfo) {
                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.workspace_unread_margin_right);
                } else {
                    unreadMarginTop = (int) res.getDimension(R.dimen.folder_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.folder_unread_margin_right);
                }
            } else if (info instanceof FolderInfo) {
//            	Log.d("MTKUnreadLoader","info instanceof FolderInfo");
                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                	Log.d("MTKUnreadLoader","info instanceof FolderInfo info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT");
                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
//                	Log.d("MTKUnreadLoader","info instanceof FolderInfo info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP");
                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.workspace_unread_margin_right);
                }
            }else if (info instanceof AppInfo) {
//            	Log.d("MTKUnreadLoader","info instanceof AppInfo");
                unreadMarginTop = (int) res.getDimension(R.dimen.app_list_unread_margin_top);
                unreadMarginRight = (int) res.getDimension(R.dimen.app_list_unread_margin_right);
            }
            int unreadBgPosX = icon.getScrollX() + icon.getWidth() - unreadBgWidth - unreadMarginRight;
            int unreadBgPosY = icon.getScrollY() + unreadMarginTop;

//            Log.d("MTKUnreadLoader","unreadMarginRight = " + unreadMarginRight + " ,unreadBgPosX = " + unreadBgPosX);
//            Log.d("MTKUnreadLoader","unreadMarginTop = " + unreadMarginTop + " ,unreadBgPosY = " + unreadBgPosY);
            canvas.save();
            canvas.translate(unreadBgPosX, unreadBgPosY);
        	canvas.scale(progress, progress,unreadBgWidth/2,unreadBgHeight/2);

			if (unreadBgNinePatchDrawable != null && info.unreadNum >0) {
            	unreadBgNinePatchDrawable.draw(canvas);
			} else {
				if (LauncherLog.DEBUG_UNREAD) {
		            LauncherLog.d(TAG, "drawUnreadEventIfNeed: unreadBgNinePatchDrawable is null pointer");
	        	}
				return;
			}
			
            /// M: Draw unread text.
            Paint.FontMetrics fontMetrics = unreadTextNumberPaint.getFontMetrics();
            if (info.unreadNum > Launcher.MAX_UNREAD_COUNT|| info.unreadTitle !=null) {
            	if (info.unreadNum >0) {
                    canvas.drawText(unreadTextNumber,
                            (unreadBgWidth - unreadTextPlusBounds.width()) / 2,
                            (unreadBgHeight + textHeight) / 2,
                            unreadTextNumberPaint);
        	if(info.unreadNum > Launcher.MAX_UNREAD_COUNT)
            canvas.drawText(unreadTextPlus,
                            (unreadBgWidth + unreadTextNumberBounds.width()) / 2,
                            (unreadBgHeight + textHeight) / 2 + fontMetrics.ascent / 2,
                            unreadTextPlusPaint);
            	}
            	
            	if(info.unreadTitle !=null) {

                 /*   canvas.drawText(info.unreadTitle,
                            (unreadBgWidth - unreadTextPlusBounds.width()) / 2,
                            (unreadBgHeight + textHeight) / 2,
                            unreadTextNumberPaint);*/
            	}
            } else {
                canvas.drawText(unreadTextNumber,
                                unreadBgWidth / 2,
                                (unreadBgHeight + textHeight) / 2,
                                unreadTextNumberPaint);
            }

            canvas.restore();
        }
    }

    public interface UnreadCallbacks {
        /**
         * Bind shortcuts and application icons with the given component, and
         * update folders unread which contains the given component.
         *
         * @param component
         * @param unreadNum
         */
        void bindComponentUnreadChanged(ComponentName component, int unreadNum,String title,Bitmap b,PendingIntent p,int appIndex);

        /**
         * Bind unread shortcut information if needed, this call back is used to
         * update shortcuts and folders when launcher first created.
         */
        void bindUnreadInfoIfNeeded();
    }
}
