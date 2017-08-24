package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.changecolors.ChameleonColorManager;
import android.os.Bundle;

public class GnTextViewWindow extends AmigoActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_text_view_window);
        
        if(ChameleonColorManager.isNeedChangeColor()){
        	findViewById(R.id.appbar_layout).setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
        }
    }
    
}
