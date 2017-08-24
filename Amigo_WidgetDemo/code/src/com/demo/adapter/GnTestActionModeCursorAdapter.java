package com.demo.adapter;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoMultiChoiceCursorAdapter;
import amigoui.widget.AmigoTextView;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amigo.widgetdemol.R;

public class GnTestActionModeCursorAdapter extends AmigoMultiChoiceCursorAdapter {

    private AmigoActivity mActivity;

    public GnTestActionModeCursorAdapter(Bundle savedInstanceState, Cursor cursor, AmigoActivity activity) {
        super(savedInstanceState,activity,cursor);
        this.mActivity = activity;
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
    public void bindViewImpl(View view, Context cxt, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
        AmigoTextView itemTitle = (AmigoTextView) view.findViewById(R.id.list_name);
        itemTitle.setText(title);
    }

    @Override
    public View newViewImpl(Context cxt, Cursor cursor, ViewGroup parent) {
        LayoutInflater factory = LayoutInflater.from(mActivity);
        return factory.inflate(R.layout.multiselect_list_item, parent, false);
    }
}
