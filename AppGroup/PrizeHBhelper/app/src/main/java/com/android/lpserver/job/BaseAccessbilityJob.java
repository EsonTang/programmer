package com.android.lpserver.job;

import android.content.Context;

import com.android.lpserver.Config;
import com.android.lpserver.QiangHongBaoService;

public abstract class BaseAccessbilityJob implements AccessbilityJob {

    private QiangHongBaoService service;

    @Override
    public void onCreateJob(QiangHongBaoService service) {
        this.service = service;
    }

    public  Context getContext() {
        return service.getApplicationContext();
    }

    public Config getConfig() {
        return service.getConfig();
    }

    public QiangHongBaoService getService() {
        return service;
    }
}
