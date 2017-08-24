package com.amigo.widgetdemol;

import amigoui.app.AmigoListActivity;
import amigoui.widget.AmigoListView;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;

public class GnListView2Window extends AmigoListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] wuxiaStrings = {"笑傲江湖", "天龙八部", "射雕英雄传", "神雕侠侣", "碧血剑"};
        setListAdapter(new SimpleListAdapter(this, wuxiaStrings, true));
    }
    
    @Override
    protected void onListItemClick(AmigoListView listView, View view, int position, long id) {
        View listItemView = (View)((SimpleListAdapter)getListAdapter()).getView(position, view, null);
        if (listItemView != null) {
            CheckBox checkBox = (CheckBox)listItemView.findViewById(R.id.list_checkbox);
            if (checkBox != null && (View.VISIBLE == checkBox.getVisibility()) && checkBox.isEnabled()) {
                checkBox.setChecked(!checkBox.isChecked());
            }
            
            RadioButton radioButton = (RadioButton)listItemView.findViewById(R.id.list_radiobutton);
            if (radioButton != null && (View.VISIBLE == radioButton.getVisibility()) && radioButton.isEnabled()) {
                radioButton.setChecked(true);
            }
        }
    }
}
