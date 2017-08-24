package com.demo.amigoactionbar;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amigo.widgetdemol.R;

public class GnActionBarCustomViewDemo extends AmigoActivity {
    AmigoActionBar mActionBar;
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getAmigoActionBar();
        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.gn_actionbar_custom_bg));
        mActionBar.setDisplayShowExtraViewEnabled(true);
        setContentView(R.layout.gn_actionbar_custom_demo);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enterCustomMode();
                } else {
                    exitCustomMode();
                }
            }
        });
    }

    /**
     * 使用自定义view
     */
    private void enterCustomMode() {
        TextView v = new TextView(GnActionBarCustomViewDemo.this);
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        v.setText("自定义Aciontbar布局");
        v.setGravity(Gravity.CENTER);
        // v.setBackgroundColor(Color.parseColor("#990000"));

        mActionBar.setDisplayOptions(AmigoActionBar.DISPLAY_SHOW_CUSTOM, AmigoActionBar.DISPLAY_SHOW_CUSTOM
                | AmigoActionBar.DISPLAY_SHOW_HOME | AmigoActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setCustomView(v);
    }

    /**
     * 不使用自定义view
     */
    private void exitCustomMode() {
        mActionBar.setDisplayShowCustomEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_STANDARD);
    }
}