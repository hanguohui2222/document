package com.amigo.widgetdemol;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

public class GnTitleFragment extends AmigoListFragment {

    private List<String> mDatas;
    private String[] mMovieStrings = { "星球大战系列", "异次元骇客（第十三层）", "超人",
            "终结者（1、2）", "12猴子", "黑客帝国系列", "移魂都市（黑暗城市）", "超时空接触", "千钧一发",
            "2001漫游太空", "肖申克的救赎", "教父", "美国往事", "天堂电影院", "无主之城", "活着", "阿甘正传",
            "勇敢的心", "楚门的世界", "音乐之声", "辛德勒的名单", "拯救大兵瑞恩", "猎杀红色十月", "兵临城下",
            "大逃杀", "巴顿将军", "u-571", "全金属外壳", "星际舰队", "瓦尔特保卫萨拉热窝", "野战排",
            "英雄本色", "真实的谎言", "生死时速", "虎胆龙威系列", "勇闯夺命岛", "刀锋战士", "神秘的黄玫瑰系列",
            "复仇", "三步杀人曲系列", "第一滴血" };

    private BaseAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getListView().setDividerPadding(30, 100);
        
        mDatas = getDummyItems();
        mAdapter = new MyListAdapter();
        setListAdapter(mAdapter);
    }

    private ArrayList<String> getDummyItems() {
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < mMovieStrings.length; i++) {
            items.add(mMovieStrings[i]);
        }
        return items;
    }

    private LayoutInflater mInflater;

    private class MyListAdapter extends BaseAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView tv;
            if (convertView == null) {
                mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = (View) mInflater.inflate(
                        R.layout.gn_animateremoval_row, parent, false);
                tv = (CheckedTextView) convertView.findViewById(R.id.checktext);
                convertView.setTag(tv);
            } else {
                tv = (CheckedTextView) convertView.getTag();
            }
            tv.setText(mDatas.get(position));
            return convertView;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
