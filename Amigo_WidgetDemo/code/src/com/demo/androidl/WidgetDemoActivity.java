package com.demo.androidl;

import java.util.ArrayList;

import com.amigo.widgetdemol.R;
import com.amigo.widgetdemol.SimpleListAdapter;
import com.amigo.widgetdemol.R.id;
import com.amigo.widgetdemol.R.menu;
import com.demo.amigoactionbar.GnActionBarWindow;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class WidgetDemoActivity extends ListActivity {

    ArrayList<String> mMultiViewArray = null;
    String[] mMultiViewStrings = { "action bar", "text view", "edit text",
            "list view", "checkbox and radiobutton in ListView", "button",
            "progress bar", "Switch", "dialog", "preference", "smart guide",
            "expandablelistview", "multiselect", "context menu",
            "fragment and viewpager", "spiner demo", "SearchView", };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArrayList();
        setListAdapter(new SimpleListAdapter(this, mMultiViewArray));
    }

    private void initArrayList() {
        mMultiViewArray = new ArrayList<String>();
        for (int i = 0; i < mMultiViewStrings.length; i++) {
            mMultiViewArray.add(mMultiViewStrings[i]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.widget_demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
        case 0:
            Intent textIntent = new Intent();
            textIntent.setClass(this, GnActionBarWindow.class);
            startActivity(textIntent);
            break;
        default:
            break;
        }
        super.onListItemClick(l, v, position, id);
    }

}
