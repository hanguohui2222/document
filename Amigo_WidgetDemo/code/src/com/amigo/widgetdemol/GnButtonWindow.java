package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoButton;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;

public class GnButtonWindow extends AmigoActivity {
    AmigoButton mBtnLoadingInfinity;
    private AmigoButton mBtnLoadingPercent;
    private Handler mHandler = new Handler();
    private int mPercent;
    private AmigoButton normalButton;
    

    private void dummyLoading() {
        mHandler.postDelayed(mLoadingRunable, 200);
    }

    private Runnable mLoadingRunable = new Runnable() {
        public void run() {
            if (mPercent <= 100) {
                mBtnLoadingPercent.setUpdate(mPercent);
                mPercent++;
                dummyLoading();
            } else {
                mBtnLoadingPercent.reset();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_button_window);
        normalButton = (AmigoButton) findViewById(R.id.btn_cancel);
//        RippleDrawable background = (RippleDrawable) normalButton.getBackground();
//        background.setColor(getResources().getColorStateList(R.color.amigo_button_color));
    }

}
