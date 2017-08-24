package com.amigo.widgetdemol;

import amigoui.changecolors.ChameleonColorManager;
import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        
        ChameleonColorManager.getInstance().register(this);
    }
}
