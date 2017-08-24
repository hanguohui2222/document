package com.demo.androidl;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoEditModeView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.amigo.widgetdemol.R;

public class EditModeDemo extends AmigoActivity {

    private AmigoEditModeView mView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.gn_editmode_demo);
        mView = (AmigoEditModeView) findViewById(R.id.editmodeview);
//        mView.setEditModeBackgroud(Color.RED);
//        
//        mView.setEditModeTextColor(Color.BLUE);
//        mView.setEditModeBtnTxt("leftbtn", "rightbtn");
        mView.setEditModeBtnClickListener(new AmigoEditModeView.EditModeClickListener() {

            @Override
            public void rightBtnClick() {
                Toast.makeText(EditModeDemo.this, "click ok", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void leftBtnClick() {
                Toast.makeText(EditModeDemo.this, "click cancel", Toast.LENGTH_SHORT).show();
                EditModeDemo.this.finish();
            }
        });
        getAmigoActionBar().hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
