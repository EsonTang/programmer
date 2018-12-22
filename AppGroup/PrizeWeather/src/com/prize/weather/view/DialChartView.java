package com.prize.weather.view;

import com.prize.weather.R;
import java.util.ArrayList;
import java.util.List;

import org.xclcharts.chart.DialChart;
import org.xclcharts.common.MathHelper;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.renderer.axis.RoundAxisRender;
import org.xclcharts.renderer.plot.PlotAttrInfo;
import org.xclcharts.view.GraphicalView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;

/**
 * 
 * @author wangzhong
 */
public class DialChartView extends GraphicalView {
	
	private String TAG = "DialChartView";
	private DialChart chart = new DialChart();
//	private float mPercentage = 0.9f;
	
	private int mMaxLabel = 500;
	private int mMinLabel = 0;
	private int mCurrentLabel = 0;

	public void setmMaxLabel(int mMaxLabel) {
		this.mMaxLabel = mMaxLabel;
	}

	public void setmMinLabel(int mMinLabel) {
		this.mMinLabel = mMinLabel;
	}

	public void setmCurrentLabel(int mCurrentLabel) {
		this.mCurrentLabel = mCurrentLabel;
	}

	public DialChartView(Context context) {
		super(context);
		initView();
	}
	
	public DialChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public DialChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}
	
	private void initView() {
		chartRender();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		chart.setChartRange(w, h);
	}
	
	public void chartRender() {
		try {
			//设置标题背景
//			chart.setApplyBackgroundColor(true);
//			chart.setBackgroundColor(Color.rgb(47, 199, 140));
			//绘制边框
//			chart.showRoundBorder();
			chart.setPadding(5, 5, 5, 5);
			
			//设置当前百分比
//			chart.setCurrentPercentage(mPercentage);
			//增加轴
			addAxis();
			//设置附加信息
			addAttrInfo();
			//指针
			chart.getPointer().setLength(0.68f, 0.05f);
			chart.getPointer().setPercentage((Float.valueOf(mCurrentLabel + "") - mMinLabel) / mMaxLabel);
			chart.getPointer().getPointerPaint().setColor(Color.WHITE);
			chart.getPointer().getPointerPaint().setAntiAlias(true);
			//指针底部环  -- 全局中心点
			chart.getPointer().getBaseCirclePaint().setColor(Color.WHITE);
			chart.getPointer().getBaseCirclePaint().setStyle(Style.STROKE);
			chart.getPointer().getBaseCirclePaint().setStrokeWidth(4);
			chart.getPointer().getBaseCirclePaint().setAntiAlias(true);
			chart.getPointer().setBaseRadius(10f);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
	
	/**
	 * @see RoundAxisRender #renderTicks  count
	 */
	public void addAxis() {
		// 轴1 --最外面的弧线轴
		chart.addArcLineAxis(1);
		
		// 轴2--刻度盘
		List<String> rlabels = new ArrayList<String>();
		int j = 0;
		for (int i = 0; i <= mMaxLabel; ) {
			if (0 == i || j == 4) {
//				rlabels.add(Integer.toString(i));
				rlabels.add("");
				j = 0;
			} else {
				rlabels.add("");
				j++;
			}
			i += 5;
		}
		chart.addInnerTicksAxis(0.85f, rlabels);
//		chart.addOuterTicksAxis(0.85f, rlabels);
		
		int cTrasWhite = Color.argb(119, 255, 255, 255);
		// 轴1--最外部弧线相关参数
		chart.getPlotAxis().get(0).getAxisPaint().setColor(cTrasWhite);
		chart.getPlotAxis().get(0).getAxisPaint().setStrokeWidth(2);
		chart.getPlotAxis().get(0).getAxisPaint().setAntiAlias(true);
		List<String> arcLabels = new ArrayList<String>();
		arcLabels.add(Integer.toString(mMinLabel));
		arcLabels.add(Integer.toString(mMaxLabel));
		((RoundAxisRender) chart.getPlotAxis().get(0)).setAxisLabels(arcLabels);
//		((RoundAxisRender) chart.getPlotAxis().get(0)).getAxisPaint().setTextSize(30);
		((RoundAxisRender) chart.getPlotAxis().get(0)).getAxisPaint().setTextSize(getResources().getDimension(R.dimen.chartTextSize));
		// 轴2--刻度盘
		chart.getPlotAxis().get(1).setDetailModeSteps(0);
		chart.getPlotAxis().get(1).getTickLabelPaint().setColor(Color.WHITE);
		chart.getPlotAxis().get(1).getTickMarksPaint().setColor(Color.WHITE);
		chart.getPlotAxis().get(1).getTickMarksPaint().setStrokeWidth(2);
		chart.getPlotAxis().get(1).getTickMarksPaint().setAntiAlias(true);
		chart.getPlotAxis().get(1).hideAxisLine();
		List<Integer> dcolor = new ArrayList<Integer>();
		dcolor.add(Color.WHITE);
		dcolor.add(cTrasWhite);
		((RoundAxisRender) chart.getPlotAxis().get(1)).setAxisColor(dcolor);
		List<Float> dPercentage = new ArrayList<Float>();
		float dMain = (Float.valueOf(mCurrentLabel + "") - mMinLabel) / mMaxLabel;
		float dPer = MathHelper.getInstance().sub(1, dMain);
		dPercentage.add(dMain);
		dPercentage.add(dPer);
		((RoundAxisRender) chart.getPlotAxis().get(1)).setAxisPercentage(dPercentage);
	}
	
	private void addAttrInfo() {
		PlotAttrInfo plotAttrInfo = chart.getPlotAttrInfo();
		
		Paint paintBT = new Paint();
		paintBT.setColor(Color.WHITE);
		paintBT.setAntiAlias(true);
		paintBT.setTextAlign(Align.CENTER);
//		paintBT.setTextSize(50);
		paintBT.setTextSize(getResources().getDimension(R.dimen.pmTextSize));
		
		plotAttrInfo.addAttributeInfo(XEnum.Location.BOTTOM, Integer.toString(mCurrentLabel), 0.45f, paintBT);
	}
	
	public void setCurrentStatus(int currentLabel) {
		//清理
		chart.clearAll();
		
		mCurrentLabel = currentLabel;
		//设置当前百分比
		chart.getPointer().setPercentage((Float.valueOf(mCurrentLabel + "") - mMinLabel) / mMaxLabel);
		addAxis();
		addAttrInfo();
	}
	
	@Override
	public void render(Canvas canvas) {
		try {
			chart.render(canvas);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

}
