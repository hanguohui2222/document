package com.gionee.secretary.ui.activity;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoTextView;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.DisplayUtils;

public class ForgetPasswordActivity extends AmigoActivity {
    private AmigoActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        initActionBar();
    }

    public void initActionBar() {
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        View actionBarLayout = getLayoutInflater().inflate(R.layout.actionbar_set_password, null);
        ImageView btn_back = (ImageView) actionBarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        AmigoTextView tv_password = (AmigoTextView) actionBarLayout.findViewById(R.id.tv_password);
        tv_password.setText(this.getResources().getString(R.string.forget_pw));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(AmigoActionBar.LayoutParams.MATCH_PARENT, AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mActionBar.setCustomView(actionBarLayout, param);
        mActionBar.show();
    }
}
