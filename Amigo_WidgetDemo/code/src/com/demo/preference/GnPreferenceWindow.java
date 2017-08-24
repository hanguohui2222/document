package com.demo.preference;

import java.lang.reflect.Method;

import amigoui.preference.AmigoCheckBoxAndClickPreference;
import amigoui.preference.AmigoEditTextPreference;
import amigoui.preference.AmigoListPreference;
import amigoui.preference.AmigoPreference;
import amigoui.preference.AmigoPreference.OnPreferenceChangeListener;
import amigoui.preference.AmigoPreferenceActivity;
import amigoui.preference.AmigoPreferenceButtonCategory;
import amigoui.preference.AmigoPreferenceButtonCategory.IAmigoCategoryBtnClickListener;
import amigoui.preference.AmigoPreferenceCategory;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.amigo.widgetdemol.R;
import com.amigoui.internal.util.ReflectionUtils;

public class GnPreferenceWindow extends AmigoPreferenceActivity implements OnPreferenceChangeListener, IAmigoCategoryBtnClickListener {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.gionee_preference);

        AmigoPreference amigoVersion = (AmigoPreference) findPreference("pref_key_version");        
        String version = getAmigoFrameworkVersion();
        amigoVersion.setSummary(version);        
        AmigoCheckBoxAndClickPreference checkAndClickPreference =(AmigoCheckBoxAndClickPreference) findPreference("pref_key_check_click");
        checkAndClickPreference.setRBtnOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GnPreferenceWindow.this, "点击显示", Toast.LENGTH_SHORT).show();
            }
        });
        AmigoEditTextPreference editPreference = (AmigoEditTextPreference) findPreference("testEditPreference");
        editPreference.setDialogMessage("SIM卡PIN码");
        
        AmigoPreference amigoUA = (AmigoPreference) findPreference("pref_key_ua");        
        String ua = getAmigoFrameworkUA(null);
        amigoUA.setSummary(ua);
        
        AmigoPreference amigoUACxt = (AmigoPreference) findPreference("pref_key_ua_cxt");        
        String uaCxt = getAmigoFrameworkUA(this);
        amigoUACxt.setSummary(uaCxt);
//        AmigoPreferenceCategory category = (AmigoPreferenceCategory) findPreference("test2");
//        category.setWidgetLayoutResource(R.layout.gn_preference_category_widget);
        
        AmigoListPreference listP2 = (AmigoListPreference)findPreference("pref_key_sms_save_location2");
        listP2.setOnPreferenceChangeListener(this);
        
        AmigoListPreference listP = (AmigoListPreference)findPreference("pref_key_sms_save_location");
        listP.setOnPreferenceChangeListener(this);
        
        AmigoPreferenceButtonCategory ca = (AmigoPreferenceButtonCategory)findPreference("test1");
        ca.setCategoryBtnClickListener(this);
//        ca.setButtonText("test1");
        AmigoPreferenceButtonCategory ca2 = (AmigoPreferenceButtonCategory)findPreference("test2");
        ca2.setCategoryBtnClickListener(this);
//        ca2.setButtonText("test2");
    }
    
    private String getAmigoFrameworkVersion() {
        String version = "";
        Class<?> verClass = null;
        
        try{
            verClass = Class.forName("amigo.widget.AmigoWidgetVersion");
        }catch (Exception e){
            e.printStackTrace();
        }
        
        try {
            Method method = verClass.getMethod("getVersion");
            version = (String)method.invoke(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return version;
    }
    
    private String getAmigoFrameworkUA(Context cxt) {
        String ua = "";
        Class<?> verClass = null;
        
        try{
            verClass = Class.forName("com.amigo.utils.ProductConfiguration");
        }catch (Exception e){
            e.printStackTrace();
        }
        
        try {
            if(null == cxt) {
                Method method = verClass.getMethod("getUAString");
                ua = (String)method.invoke(this);
            } else {
                Method method = verClass.getMethod("getUAStringWithContext",Context.class);
                ua = (String)method.invoke(this,cxt);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return ua;
    }
    
    @Override
    public boolean onPreferenceChange(AmigoPreference preference,
            Object newValue) {
        
        AmigoListPreference list = (AmigoListPreference)preference;
        list.setPreferenceResult(list.getValue());
        
        return true;
    }

    @Override
    public void onCategoryBtnClickListener(AmigoPreference preference) {
        String key = preference.getKey();
        Toast.makeText(this, key, Toast.LENGTH_SHORT).show();
    }
}
