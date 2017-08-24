package com.demo.amigoscrollbar;


import java.util.ArrayList;

import com.amigo.widgetdemol.R;
import com.amigo.widgetdemol.SimpleListAdapter;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoListView;
import android.os.Bundle;

public class GnScrollbarActivity extends AmigoActivity {

    AmigoListView mListView = null;
    private ArrayList<String> mMultiViewArray;
    
    String[] mMultiViewStrings = {
            "text view",
            "edit text",
            "list view",
            "checkbox and radiobutton in ListView",
            "button",
            "progress bar",
            "Switch",
            "dialog",
            "preference",
            "smart guide",
            "expandablelistview",
            "action bar",
            "multiselect",
            "context menu",
            "fragment and viewpager",
            "spiner demo",
            "SearchView",
            "Test AppTheme",
            "text view",
            "edit text",
            "list view",
            "checkbox and radiobutton in ListView",
            "button",
            "progress bar",
            "Switch",
            "dialog",
            "preference",
            "smart guide",
            "expandablelistview",
            "action bar",
            "multiselect",
            "context menu",
            "fragment and viewpager",
            "spiner demo",
            "SearchView",
            "Test AppTheme",
            "text view",
            "edit text",
            "list view",
            "checkbox and radiobutton in ListView",
            "button",
            "progress bar",
            "Switch",
            "dialog",
            "preference",
            "smart guide",
            "expandablelistview",
            "action bar",
            "multiselect",
            "context menu",
            "fragment and viewpager",
            "spiner demo",
            "SearchView",
            "Test AppTheme",
          };
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_scrollbar_layout);
        initArrayList();
        mListView = (AmigoListView)findViewById(R.id.list);
        mListView.setAdapter(new SimpleListAdapter(this, mMultiViewArray));        
    }
    
    private void initArrayList() {
        mMultiViewArray = new ArrayList<String>();
        for (int i = 0; i < mMultiViewStrings.length; i++) {
            mMultiViewArray.add(mMultiViewStrings[i]);
        }
    }
}
