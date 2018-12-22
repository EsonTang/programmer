package com.android.launcher3;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.content.Context;
import android.util.Xml;

public class XmlParser {

	public static List<String> parseIntercepts(String xmlPath, Context context,
			List<String> packageNames) throws Exception {
		XmlPullParser xmlParse = Xml.newPullParser();
		InputStream stream = null;
		try {
			// get file stream and set encoding
			stream = context.getResources().getAssets().open(xmlPath);
			xmlParse.setInput(stream, "utf-8");
			// get event type
			int evnType = xmlParse.getEventType();
			// continue to end document
			while (evnType != XmlPullParser.END_DOCUMENT) {
				switch (evnType) {
				case XmlPullParser.START_TAG:
					String tag = xmlParse.getName();
					if ("item".equalsIgnoreCase(tag)) {
						String pkgName = xmlParse.getAttributeValue(null,
								"packageName");
						packageNames.add(pkgName);
					}
				case XmlPullParser.END_TAG:
					break;
				default:
					break;
				}
				evnType = xmlParse.next();
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return packageNames;
	}
}
