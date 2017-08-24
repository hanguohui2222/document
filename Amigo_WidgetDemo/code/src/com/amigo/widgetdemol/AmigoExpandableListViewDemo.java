package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoExpandableListView;
import amigoui.widget.AmigoExpandableListView.OnChildClickListener;
import amigoui.widget.AmigoExpandableMultiChoiceBaseAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AmigoExpandableListViewDemo extends AmigoActivity implements OnChildClickListener{

    private AmigoActivity mActivity;
    private AmigoExpandableListView mListView;
    private MyExpandableListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setTheme(R.style.DemoLightTheme);
        super.onCreate(savedInstanceState);
        initView();
        initAdapter(savedInstanceState);
    }
    
    private void initView() {
        mActivity = this;
        setContentView(R.layout.expandablelistview);
        mListView = (AmigoExpandableListView) findViewById(R.id.expandable_list);
    }
    
    private void initAdapter(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (mAdapter == null) {
            mAdapter = new MyExpandableListAdapter(savedInstanceState, this);
            mListView.setOnCreateContextMenuListener(this);
            mAdapter.setAdapterView(mListView);
            mAdapter.setOnChildClickListener(this);
        }
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mListView.expandGroup(3);
    }
    
    
    
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        mAdapter.finishActionMode();
        super.onBackPressed();
        
    }



    public class MyExpandableListAdapter extends AmigoExpandableMultiChoiceBaseAdapter {
        private AmigoActivity mContext;
        
        public MyExpandableListAdapter(Bundle savedInstanceState, AmigoActivity context) {
            super(savedInstanceState);
            this.mContext = context;
        }
        
        private String[] generalsTypes = new String[] { "魏", "蜀", "吴", "群雄" };
        //子视图显示文字
        private String[][] generals = new String[][] {
                { "夏侯惇", "甄姬", "许褚体系部于2013年11月13日已发布金立通信设备有限公司研发管理PQA组C类文件整机LCD效果确认流程请各位同事阅读文件浏览链接地址点击浏览此邮件由体系资料通过金立办公系统发送不需回复", "郭嘉", "司马懿", "杨修" },
                { "马超", "张飞", "刘备", "诸葛亮", "黄月英", "赵云" },
                { "吕蒙哦费劲地附近的金额近哥几个将根据第三方阿飞阿飞阿飞阿飞呃呃份额娥和任何人如何如何如何如何然后忽然不和任何人和任何人和任何人会如何然后还感到饥饿价格结构个哥哥搞供电所高低贵贱多个"},
                { "凹凸曼", "漩涡鸣人", "卡卡西","我爱罗","波风水门" }
        };
        
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return generals[groupPosition][childPosition];
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return childPosition;
        }

        @Override
        public View getChildViewImpl(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View retView = convertView;
            if (convertView == null) {
                retView = LinearLayout
                        .inflate(mContext, R.layout.list_child_item, null);
            }
            TextView text = (TextView) retView.findViewById(R.id.childName);
            text.setText(getChild(groupPosition, childPosition)
                    .toString());
            return retView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            // TODO Auto-generated method stub
            return generals[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return generalsTypes[groupPosition];
        }

        @Override
        public int getGroupCount() {
            // TODO Auto-generated method stub
            return generalsTypes.length;
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return groupPosition;
        }

        @Override
        public View getGroupViewImpl(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View retView = convertView;
            if (convertView == null) {
                retView = LinearLayout
                        .inflate(mContext, R.layout.list_group_item, null);
            }
            TextView title = (TextView) retView.findViewById(R.id.groupName);
            TextView count = (TextView) retView.findViewById(R.id.groupCount);
            title.setText(getGroup(groupPosition).toString());
            if (groupPosition == 0) {
                count.setText("[" + (getChildrenCount(groupPosition) -1 ) + "]");
            }else {
                count.setText("[" + getChildrenCount(groupPosition) + "]");
            }
            
            return retView;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return true;
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
            boolean isselected = hasItemSelected();
            if (isselected) {
                menu.findItem(R.id.title1).setEnabled(true);
                menu.findItem(R.id.title2).setEnabled(true);
            } else {
                menu.findItem(R.id.title1).setEnabled(false);
                menu.findItem(R.id.title2).setEnabled(false);
            }
            mActivity.getAmigoActionBar().updateActionMode();

            return true;
        }
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

    @Override
    public boolean onChildClick(AmigoExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        // TODO Auto-generated method stub
        Toast.makeText(mActivity,"你点击了" + mAdapter.getChild(groupPosition, childPosition),
                Toast.LENGTH_SHORT).show();

        return false;
    }
}
