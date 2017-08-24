package com.demo.amigoactionbar;

import amigoui.app.AmigoActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amigo.widgetdemol.R;

public class GnOptionsMenuAsUpDemo extends AmigoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.gn_options_menu_as_up_demo);
        
        getAmigoActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gn_actionmenu_demo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Toast.makeText(GnOptionsMenuAsUpDemo.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();        
        return super.onOptionsItemSelected(menuItem);
    }

	@Override
	public boolean onOptionsItemLongClick(MenuItem menuItem) {
		Toast.makeText(GnOptionsMenuAsUpDemo.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();        
		return super.onOptionsItemLongClick(menuItem);
	}
    
    

}
