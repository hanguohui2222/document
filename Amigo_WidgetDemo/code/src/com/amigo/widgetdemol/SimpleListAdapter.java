package com.amigo.widgetdemol;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

public class SimpleListAdapter extends BaseAdapter {

    Context mContext = null;
    ArrayList<String> mArrayList = null;
    boolean mIsShowExtra = false;
    
    public SimpleListAdapter(Context context, ArrayList<String> strArray) {
        mContext = context;
        mArrayList = strArray;
    }
    
    public SimpleListAdapter(Context context, String[] strArray) {
        mContext = context;
        mArrayList = new ArrayList<String>();
        for (int i = 0; i < strArray.length; i++) {
            mArrayList.add(strArray[i]);
        }
    }
    
    public SimpleListAdapter(Context context, String[] strArray, boolean isShowExtra) {
        mIsShowExtra = isShowExtra;
        mContext = context;
        mArrayList = new ArrayList<String>();
        for (int i = 0; i < strArray.length; i++) {
            mArrayList.add(strArray[i]);
        }
    }

    @Override
    public int getCount() {
        if (mArrayList != null) {
            return mArrayList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return getView(position, null, null);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            view = factory.inflate(R.layout.list_item, parent, false);
        }
        TextView list_nameText = (TextView) view.findViewById(R.id.list_name);
        CheckBox list_checkBox = (CheckBox) view.findViewById(R.id.list_checkbox);
        RadioButton list_radiobtn = (RadioButton) view.findViewById(R.id.list_radiobutton);
        list_nameText.setText(mArrayList.get(position));
        int itemCount = getCount();
        if (list_checkBox != null) {
            if (mIsShowExtra) {
                if (position == itemCount - 1) {
                    list_checkBox.setVisibility(View.VISIBLE);
                    list_checkBox.setEnabled(false);
                } else if (position == itemCount -2) {
                    list_checkBox.setVisibility(View.VISIBLE);
                } else {
                    list_checkBox.setVisibility(View.GONE);
                }
            } else {
                list_checkBox.setVisibility(View.GONE);
            }
        }
        
        if (list_radiobtn != null) {
            if (mIsShowExtra) {
                if (position == itemCount - 3) {
                    list_radiobtn.setVisibility(View.VISIBLE);
                    list_radiobtn.setEnabled(false);
                } else if (position == itemCount - 4) {
                    list_radiobtn.setVisibility(View.VISIBLE);
                } else {
                    list_radiobtn.setVisibility(View.GONE);
                }
            } else {
                list_radiobtn.setVisibility(View.GONE);
            }
        }
        return view;
    }
}
