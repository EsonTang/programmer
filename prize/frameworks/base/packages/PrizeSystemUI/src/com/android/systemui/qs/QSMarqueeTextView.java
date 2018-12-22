/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：快速设置文字自动循环的view
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-5-14
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

/**
* 类描述：快速设置文字自动循环的view
* @author liufan
* @version V1.0
*/
public class QSMarqueeTextView extends TextView {

    public QSMarqueeTextView(Context context) {
        super(context);
    }

    public QSMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QSMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    @ExportedProperty(category = "focus")
    public boolean isFocused() {
        return false;
    }

}
