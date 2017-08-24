package com.amigo.widgetdemol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import amigoui.app.AmigoActivity;


public class GnActionMenuViewDemo extends AmigoActivity {
		private List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();
		private String mWidgetArray[]  = {"TestData1","TestData2","TestData3"};//"TestData4","TestData5","TestData6","TestData7","TestData8","TestData9","TestData10","TestData11","TestData12","TestData13",};
		private boolean mFlag = true;
@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gn_actionmenuview_demo);
		ListView listview = (ListView)findViewById(R.id.listview);
		TextView screen_width = (TextView)findViewById(R.id.screen_width);
		TextView screen_height = (TextView)findViewById(R.id.screen_height);
		TextView screen_density = (TextView)findViewById(R.id.screen_density);
		int screenWidth  = getWindowManager().getDefaultDisplay().getWidth();       
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		DisplayMetrics dm = new DisplayMetrics();  
		dm = getResources().getDisplayMetrics(); 
		
		screen_width.setText("Screen Width :"+String.valueOf(screenWidth));
		screen_height.setText("Screen Height :"+String.valueOf(screenHeight));
		screen_density.setText("Screen Density :"+String.valueOf(dm.density));
		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
              if(mFlag){
                  setOptionsMenuHideMode(true);
                  mFlag = false ;
              }else {
                  setOptionsMenuHideMode(false);
                  mFlag = true ;
              }
                
            }
        });
		 initData() ;
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, (List<Map<String, Object>>)myData, android.R.layout.simple_list_item_1,
				new String[] { "title" }, new int[] { android.R.id.text1 });
		listview.setAdapter(simpleAdapter);		
	}
	
	private void initData() {
		for(int i =0 ; i<mWidgetArray.length;i++) {
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.put("title", mWidgetArray[i]);
			myData.add(temp);
		}
		
	}	
    MenuItem mMenuItem = null;
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gn_actionmenu_demo, menu);
		mMenuItem = menu.findItem(R.id.title3);
		return super.onCreateOptionsMenu(menu);    
    }


    @Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
         boolean ischecked1 = false;
         boolean ischecked2 = false ;
		switch (menuItem.getItemId()) {
		case R.id.title1:
			//Intent edittextIntent = new Intent();
            //edittextIntent.setClass(this, GnEditTextWindow.class);
            // startActivity(edittextIntent);   
			Toast.makeText(GnActionMenuViewDemo.this,"you press menu 1",Toast.LENGTH_SHORT).show();
			break;
		case R.id.title2:
			Toast.makeText(GnActionMenuViewDemo.this,"hide magic bar",Toast.LENGTH_SHORT).show();
			setOptionsMenuHideMode(true);
			break;
		case R.id.title3:
			Toast.makeText(GnActionMenuViewDemo.this,"you press menu 3",Toast.LENGTH_SHORT).show();
			mMenuItem.setIcon(getResources().getDrawable(R.drawable.gn_magic_menu_cut));
			mMenuItem.setVisible(false);
			break;
		case R.id.title4:
			Intent intent=new Intent(Intent.ACTION_SEND);   
			intent.setType("image/*");   
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享");   
			intent.putExtra(Intent.EXTRA_TEXT, "终于可以了!!!");    
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
			startActivity(Intent.createChooser(intent, getTitle())); 
			break;
		case R.id.title5:
	          ischecked2 = !ischecked2;
	          menuItem.setChecked(ischecked2);
			Toast.makeText(GnActionMenuViewDemo.this,"you press menu 5",Toast.LENGTH_SHORT).show();
			break;
		case R.id.title6:
		    ischecked1 = !ischecked1;
		    menuItem.setChecked(ischecked1);
			Toast.makeText(GnActionMenuViewDemo.this,"you press menu 6",Toast.LENGTH_SHORT).show();
			break;
	    case R.id.title7:
	            Toast.makeText(GnActionMenuViewDemo.this,"you press menu 7",Toast.LENGTH_SHORT).show();
	            break;
	    case R.id.title8:
	            Toast.makeText(GnActionMenuViewDemo.this,"you press menu 8",Toast.LENGTH_SHORT).show();
	            break;
		default:
			break;
		}
		return super.onOptionsItemSelected(menuItem);

	}
	
@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub 
		//menu.removeItem(R.id.title1);
		//setOptionsMenuHideMode(false);
        //menu.getItem(0).setVisible(false);
        //menu.getItem(1).setVisible(false);
        //menu.getItem(2).setVisible(false);
        //menu.getItem(3).setEnabled(false);
        //menu.getItem(4).setEnabled(false);
		return super.onPrepareOptionsMenu(menu);
	}


}
