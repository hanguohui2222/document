package com.demo.adapter;

import java.util.List;
import java.util.Set;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoMultiChoiceBaseAdapter;
import amigoui.widget.AmigoTextView;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amigo.widgetdemol.R;

public class GnTestActionModeAdapter extends AmigoMultiChoiceBaseAdapter {

    private final static String LOGTAG = "GnTestActionModeAdapter";
    private AmigoActivity mActivity;
    private List<String> mItems;

    public GnTestActionModeAdapter(Bundle savedInstanceState, List<String> items, AmigoActivity activity) {
        super(savedInstanceState);
        this.mActivity = activity;
        this.mItems = items;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Set<Long> items = getCheckedItems();
        if(items.size() <= 0) {
            return false;
        }
        Long[] arrays = new Long[items.size()];
        
        Toast.makeText(mActivity, items.toArray(arrays)[0]+"", Toast.LENGTH_SHORT).show();
        
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mActivity.getMenuInflater();
        inflater.inflate(R.menu.menu_multi_sel, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int cnt = getCheckedItemCount();
        Set<Long> checks = getCheckedItems();
        for (Long pos : checks) {
            String strVal = (String)getItem(pos.intValue());
            Log.e(LOGTAG, "onPrepareActionMode strVal="+strVal+"pos="+pos);
        }
        
        if (cnt > 0) {
            menu.findItem(R.id.title1).setEnabled(true);
            menu.findItem(R.id.title2).setEnabled(true);
        } else {
            menu.findItem(R.id.title1).setEnabled(false);
            menu.findItem(R.id.title2).setEnabled(false);
        }
        mActivity.getAmigoActionBar().updateActionMode();
        
        return true;
    }

    @Override
    public int getCount() {
        if (mItems != null) {
            return mItems.size();
        } else {
            return 0;
        }
    }

    
    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return 0;
        
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isItemCheckable(int position) {
        if(position == 0) {
            return false;
        }
        
        return super.isItemCheckable(position);
    };
    
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder mHolder;
        if (view == null) {
            LayoutInflater factory = LayoutInflater.from(mActivity);
            view = factory.inflate(R.layout.multiselect_list_item, parent, false);
            mHolder = new ViewHolder();
            mHolder.mTxtView = (AmigoTextView) view.findViewById(R.id.list_name);
            view.setTag(mHolder);
        } else {
            mHolder = (ViewHolder)view.getTag();
        }
        mHolder.mTxtView.setText(mItems.get(position));
        
        return view;
    }
    
    static class ViewHolder {
        AmigoTextView mTxtView;
    }

}
