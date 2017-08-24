package com.demo.preference;

import amigoui.preference.AmigoPreferenceFragment;
import android.os.Bundle;

import com.amigo.widgetdemol.R;

public class GnPreferenceFragmentDemo extends AmigoPreferenceFragment{
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gionee_preference);
    }
}
