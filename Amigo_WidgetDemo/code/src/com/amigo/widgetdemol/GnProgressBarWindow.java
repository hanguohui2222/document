package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoButton;
import amigoui.widget.AmigoProgressBar;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

public class GnProgressBarWindow extends AmigoActivity implements OnClickListener {

 
    AmigoProgressBar mProgressBar;
    AmigoButton mJiaButton;
    AmigoButton mJianButton;
 
    int mProgress;
    int mSecondProgress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_progressbar_window);
        
        mProgress = 50;
        mSecondProgress = 75;
        
        mProgressBar = (AmigoProgressBar) this.findViewById(R.id.gn_progress);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(mProgress);
        mProgressBar.setSecondaryProgress(mSecondProgress);
        mJiaButton = (AmigoButton) this.findViewById(R.id.jia_btn);
        mJiaButton.setOnClickListener(this);
        mJianButton = (AmigoButton)  this.findViewById(R.id.jian_btn);
        mJianButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mJiaButton) {
            mProgress += 10;
            if (mProgress >= 100) {
                mProgress = 100;
            }
            mProgressBar.setProgress(mProgress);
        } else if (view == mJianButton) {
            mProgress -= 10;
            if (mProgress <= 0) {
                mProgress = 0;
            }
            mProgressBar.setProgress(mProgress);
        }
    }
}
