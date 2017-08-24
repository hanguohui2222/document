package com.demo.amigoactionbar;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.amigo.widgetdemol.R;

public class GnActionBarBackDemo extends AmigoActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gn_actionbar_back_demo);

        AmigoActionBar actionBar = getAmigoActionBar();

        // 设置true，ActionBar会出现返回箭头，点击执行finish()。
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        
        // 返回按钮的监听接口，不会执行默认finish()。只执行onClick()。
        actionBar.setOnBackClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GnActionBarBackDemo.this, "back click", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        actionBar.setOnActionBarDoubleClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Log.e("GnActionBarBackDemo", "setOnActionBar2ClickListener click true");
            }
        });
    }
}
