package com.demo.amigoactionbar;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoMultiChoiceCursorAdapter;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.amigo.widgetdemol.R;
import com.demo.adapter.GnTestActionModeCursorAdapter;

public class GnTestCursorActionModeActivity extends AmigoActivity implements OnItemClickListener {

    private final static String TAG = "GnTestActionModeActivity";
    private AmigoMultiChoiceCursorAdapter mAdapter;
    private AsyncQueryHandler mAsyncQuery; // 异步查询通话记录handler
    private Bundle mSaveInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_action_mode_demo);
        mSaveInstance = savedInstanceState;
        initData();
    }

    private void initData() {
        mAdapter = new GnTestActionModeCursorAdapter(mSaveInstance, null, this);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setAdapterView(getListView());
        mAsyncQuery = new MyAsyncQueryHandler(getContentResolver());
        String[] sel = {};
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = {CallLog.Calls.DATE, CallLog.Calls.NUMBER, CallLog.Calls.TYPE,
                CallLog.Calls.CACHED_NAME, CallLog.Calls._ID}; // 查询的列
        mAsyncQuery.startQuery(0, null, uri, sel, "", sel, CallLog.Calls.DEFAULT_SORT_ORDER);
    }

    private AmigoListView getListView() {
        AmigoListView view = (AmigoListView) findViewById(android.R.id.list);
        Log.e(TAG, "getListView view=" + view);
        return view;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mAdapter.save(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Toast.makeText(this, "Item click: " + mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gn_actionmenu_demo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Toast.makeText(this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        mAdapter.enterMultiChoiceMode();
        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * 查询通话记录异步任务处理
     * */
    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * 通话记录查询完成消息处理
         * */
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.e("Demo","onQueryComplete cursor="+cursor.getCount());
            mAdapter.changeCursor(cursor);
        }
    }
}
