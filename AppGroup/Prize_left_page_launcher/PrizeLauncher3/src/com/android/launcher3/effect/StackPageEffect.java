/** Created by Spreadtrum */
package com.android.launcher3.effect;

import com.android.launcher3.CellLayout;
import com.android.launcher3.Workspace.ZInterpolator;

import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Scroller;

public class StackPageEffect extends EffectInfo {

	public StackPageEffect(int id) {
		super(id);
	}

    private ZInterpolator mZInterpolator = new ZInterpolator(0.5f);
    private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4);

    protected static final float TRANSITION_SCALE_FACTOR = 0.5f;
    protected AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
	@Override
	public boolean getCellLayoutChildStaticTransformation(ViewGroup viewGroup,
			View viewiew, Transformation transformation, Camera camera,
			float offset) {
		return false;
	}

	@Override
	public boolean getWorkspaceChildStaticTransformation(ViewGroup viewGroup,
			View viewiew, Transformation transformation, Camera camera,
			float offset) {
		return false;
	}

	@Override
	public Scroller getScroller(Context context) {
		return null;
	}

	@Override
	public int getSnapTime() {
		return 0;
	}

	/* SPRD: Fix bug258437 @{ */
	@Override
	public void getTransformationMatrix(View view, float offset, int pageWidth,
			int pageHeight, float distance, boolean overScroll,
			boolean overScrollLeft) {
        float interpolatedProgress;
        float translationX;
        float maxScrollProgress = Math.max(0, offset);
        float minScrollProgress = Math.min(0, offset);

        if (overScroll) {
            translationX = maxScrollProgress * pageWidth;
            interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(maxScrollProgress));
        } else {
            translationX = minScrollProgress * pageWidth;
            interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(minScrollProgress));
        }
        float scale = (1 - interpolatedProgress) +
                interpolatedProgress * TRANSITION_SCALE_FACTOR;

        float alpha;
        if (overScroll && (offset > 0)) {
            alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(maxScrollProgress));
        } else if (!overScroll && (offset < 0)) {
            alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(offset));
        } else {
            //  On large screens we need to fade the page as it nears its leftmost position
            alpha = mLeftScreenAlphaInterpolator.getInterpolation(1 - offset);
        }

        view.setTranslationX(translationX);
        view.setScaleX(scale);
        view.setScaleY(scale);
        if (view instanceof CellLayout) {
            ((CellLayout) view).setAlpha(alpha);
        } else {
            view.setAlpha(alpha);
        }

        // If the view has 0 alpha, we set it to be invisible so as to prevent
        // it from accepting touches
       /* if (alpha == 0) {
            view.setVisibility(View.INVISIBLE);
        } else if (view.getVisibility() != View.VISIBLE) {
        	view.setVisibility(View.VISIBLE);
        }*/
    }
}
