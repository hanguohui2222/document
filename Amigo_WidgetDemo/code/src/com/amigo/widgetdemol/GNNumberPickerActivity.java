package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoNumberPicker;
import android.os.Bundle;

public class GNNumberPickerActivity extends AmigoActivity {

    private AmigoNumberPicker mPicker;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_number_picker_layout);
        mPicker = (AmigoNumberPicker) findViewById(R.id.gn_number_picker);
        mPicker.setMaxValue(60);
        mPicker.setMinValue(0);
        mPicker.setValue(30);
        mPicker.setUnit("分", 2);
    }

}
