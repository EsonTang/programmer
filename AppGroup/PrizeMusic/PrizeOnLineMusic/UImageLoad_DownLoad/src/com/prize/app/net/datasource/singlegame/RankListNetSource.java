package com.prize.app.net.datasource.singlegame;

import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.SimpleAppNetSource;

public class RankListNetSource extends SimpleAppNetSource {
	private static final String LISTCODE = "115";

	public RankListNetSource(boolean isGame) {
		super(LISTCODE, isGame);
	}

	@Override
	public String getUrl() {
		return Constants.GIS_URL + "/rank/list";
	}

}
