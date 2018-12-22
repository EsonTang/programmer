/** Created by Spreadtrum */
package com.android.launcher3.effect;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.Launcher;

public class EffectFactory {

	private static List<EffectInfo> allEffects = new ArrayList<EffectInfo>();

	public static List<EffectInfo> getAllEffects() {
		return loadEffectsList();
	}
	public static int getEffectSize() {

		if (allEffects.isEmpty()) {
			loadEffectsList();
		}
		return allEffects.size();
	}
	public static EffectInfo getEffect(int id) {
		if (id == 0) {
			return null; // if is is 0 , we return null , and mean we do not
							// need animation for workspace
		}
		if (allEffects.isEmpty()) {
			loadEffectsList();
		}
		for (int i = 0, count = allEffects.size(); i < count; i++) {
			EffectInfo eInfo = allEffects.get(i);
			if (eInfo.id == id) {
				return eInfo;
			}
		}
		return null;
	}

	

	private static List<EffectInfo> loadEffectsList() {
		allEffects.clear();
		
		if (Launcher.viewHeight == 800) {
			CrossEffect crossEffect = new CrossEffect(1);
//			allEffects.add(crossEffect); // 十字架

			PageEffect pageEffect = new PageEffect(1);
			allEffects.add(pageEffect);// 翻页

			CubeEffect cubeInEffect = new CubeEffect(3, true);
//			allEffects.add(cubeInEffect);// 盒子内

			CubeEffect cubeOutEffect = new CubeEffect(4, false);
//			allEffects.add(cubeOutEffect); // 盒子外

			CarouselEffect carouselLeftEffect = new CarouselEffect(5, true);
//			allEffects.add(carouselLeftEffect);// 旋转 left

			CarouselEffect carouselRightEffect = new CarouselEffect(6, false);
//			allEffects.add(carouselRightEffect);// 旋转 right

			RotateEffect rotateEffect = new RotateEffect(2);
			allEffects.add(rotateEffect); // 卡片
		/*	LayerEffect layerEffect = new LayerEffect(8);
			allEffects.add(layerEffect);//层叠
			FadeEffect fadeEffect = new FadeEffect(9);
			allEffects.add(fadeEffect);//退去*/
		}else {
			StackPageEffect stackEffect = new StackPageEffect(1);
			allEffects.add(stackEffect); // 重叠

			PageEffect pageEffect = new PageEffect(2);
			allEffects.add(pageEffect);// 翻页

			ZoomCubeEffect cubeInEffect = new ZoomCubeEffect(3, true);
			allEffects.add(cubeInEffect);// 淡入

			CubeEffect cubeOutEffect = new CubeEffect(4, false);
			allEffects.add(cubeOutEffect); // 盒子外

//			CarouselEffect carouselLeftEffect = new CarouselEffect(5, true);
//			allEffects.add(carouselLeftEffect);// 旋转 left
//
//			CarouselEffect carouselRightEffect = new CarouselEffect(6, false);
//			allEffects.add(carouselRightEffect);// 旋转 right

			RotateEffect rotateEffect = new RotateEffect(5);
			allEffects.add(rotateEffect); // 卡片
		/*	LayerEffect layerEffect = new LayerEffect(8);
			allEffects.add(layerEffect);//层叠
			FadeEffect fadeEffect = new FadeEffect(9);
			allEffects.add(fadeEffect);//退去
	*/		
		} 

		return allEffects;
	}
}