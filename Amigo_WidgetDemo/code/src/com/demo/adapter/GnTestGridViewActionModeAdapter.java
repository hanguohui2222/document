package com.demo.adapter;

import java.util.List;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoMultiChoiceAdapter;
import amigoui.widget.AmigoMultiChoiceBaseAdapter;
import amigoui.widget.AmigoTextView;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.amigo.widgetdemol.R;
import com.demo.adapter.GnTestActionModeAdapter.ViewHolder;

public class GnTestGridViewActionModeAdapter extends AmigoMultiChoiceBaseAdapter {

    private AmigoActivity mActivity;
    private List<String> mItems;
    List<Integer> mIcons;
    
    public GnTestGridViewActionModeAdapter(Bundle savedInstanceState, List<String> items, List<Integer> icons, AmigoActivity activity) {
        super(savedInstanceState);
        this.mActivity = activity;
        this.mItems = items;
        this.mIcons = icons;
    }

    
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Toast.makeText(mActivity, item.getTitle(), Toast.LENGTH_SHORT).show();
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
    public int getMode() {
        // TODO Auto-generated method stub
        return AmigoMultiChoiceAdapter.MODE_GRID_VIEW;
    }


    @Override
    protected View getViewImpl(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder mHolder;
        if (view == null) {
            LayoutInflater factory = LayoutInflater.from(mActivity);
            view = factory.inflate(R.layout.multiselect_list_item_grid_view, parent, false);
            mHolder = new ViewHolder();
            mHolder.mTxtView = (AmigoTextView) view.findViewById(R.id.text);
            mHolder.mImageView = (ImageView) view.findViewById(R.id.icon);
            view.setTag(mHolder);
        }else {
            mHolder = (ViewHolder)view.getTag();
        }
        
        mHolder.mTxtView.setText(mItems.get(position));
        Drawable drawable = mActivity.getDrawable(mIcons.get(position));
        if (isChecked(position)) {
            drawable.setAlpha(80);
        }else {
            drawable.setAlpha(255);
        }
        mHolder.mImageView.setImageDrawable(drawable);
        return view;
    }
    
    static class ViewHolder {
        AmigoTextView mTxtView;
        ImageView mImageView;
    }
}
