package com.amigo.widgetdemol;

import com.demo.amigoactionbar.GnActionBarNormalDemo;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoTextView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GnContextMenuDemo extends AmigoActivity {
    private AmigoListView listView;
    private String[] datas = new String[] {"item1", "item2", "item3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_context_menu_demo);

        listView = (AmigoListView) findViewById(R.id.listView1);

        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(GnContextMenuDemo.this, "OnItemLongClickListener", Toast.LENGTH_SHORT).show();

                // 1、长按事件处理完，不会弹出contextmenu。
                // return true;

                // 2、长按事件未处理完，会弹出contextmenu。
                return false;
            }
        });

        listView.setAdapter(new MyAdapter());

        // **********************************
        // *ListView实现长按弹ContextMenu的方法*
        // **********************************

        // 1、AmigoListView弹出ContextMenu方式一
        listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                menu.setHeaderTitle(R.string.action_settings);
                menu.add("AAA");
                menu.add("BBB");
                menu.add("CCC");
                menu.add("DDD");
                menu.add("EEE");
                menu.add("FFF");
                menu.add("GGG");
            }
        });

        //2、AmigoListView弹出ContextMenu方式二，需要AmigoActiviy中复写onCreateContextMenu()
//        listView.setOnCreateContextMenuListener(this);

        // 3、AmigoListView弹出ContextMenu方式三，需要AmigoActiviy中复写onCreateContextMenu()
        // registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add("aaa");
        menu.add("bbb").setIntent(new Intent(GnContextMenuDemo.this, GnActionBarNormalDemo.class));
        menu.add("ccc").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(GnContextMenuDemo.this, "cccc__", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        menu.add("ddd").setVisible(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onContextItemSelected(item);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.length;
        }

        @Override
        public Object getItem(int position) {
            return datas[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
//                TextView textView = new TextView(GnContextMenuDemo.this);
                AmigoTextView textView = new AmigoTextView(GnContextMenuDemo.this);
                textView.setText(datas[position]);
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setHeight(100);
                convertView = textView;
            }
            return convertView;
        }

    }
}