package com.android.launcher3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;

public class LoadWallpaperResourceUtils {

	private static final String TAG_RESOURCES = "resources";
	private static final String TAG_ITEM = "item";

	private static final void beginDocument(XmlPullParser parser,
			String firstElementName) throws XmlPullParserException, IOException {
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG
				&& type != XmlPullParser.END_DOCUMENT) {
			;
		}

		if (type != XmlPullParser.START_TAG) {
			throw new XmlPullParserException("No start tag found");
		}

		if (!parser.getName().equals(firstElementName)) {
			throw new XmlPullParserException("Unexpected start tag: found "
					+ parser.getName() + ", expected " + firstElementName);
		}
	}

	public static List<String> loadWallpapersRes(Context context,
			String fileName) {

		boolean isExistsFile = FileUtils.isexistsFile(context, fileName);

		List<String> extendsWallpapers = new ArrayList<String>();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); // 创建解析代工厂实例
			XmlPullParser parser = null;
			factory.setNamespaceAware(true);

			parser = factory.newPullParser();// 通过工程创建XmlPullParser
			InputStream input = FileUtils.loadWallpaperXmlResource(context,
					fileName);
			if (isExistsFile) {
				if (input != null) {
					parser.setInput(input, "UTF-8");
				}
			}
			AttributeSet attrs = Xml.asAttributeSet(parser);
			beginDocument(parser, TAG_RESOURCES);

			final int depth = parser.getDepth();

			int type;
			while (((type = parser.next()) != XmlPullParser.END_TAG || parser
					.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

				if (type != XmlPullParser.START_TAG) {
					continue;
				}

				final String name = parser.getName();
				if (TAG_ITEM.equals(name)) {
					String wallpaperRes = parser.nextText();
					if (wallpaperRes != null) {
						extendsWallpapers.add(wallpaperRes);
					}
				}
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return extendsWallpapers;
	}
}
 