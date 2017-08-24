package com.amigo.widgetdemol;

import amigoui.changecolors.ChameleonColorManager;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

public class GnChangeColorActivity extends Activity {

    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mActionBar = getActionBar();
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setCustomView(R.layout.gn_change_color_custom_view);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if (ChameleonColorManager.isNeedChangeColor()) {
            mActionBar.setBackgroundDrawable(new ColorDrawable(ChameleonColorManager.getAppbarColor_A1()));
            getWindow().setStatusBarColor(ChameleonColorManager.getStatusbarBackgroudColor_S1());
        }
        super.onResume();
    }
}
