package com.demo.amigolistitem;

import java.util.ArrayList;

import amigoui.app.AmigoListActivity;
import amigoui.widget.AmigoListView;
import android.os.Bundle;
import android.view.View;

import com.amigo.widgetdemol.R;

public class ItemListDemo extends AmigoListActivity {

    private ArrayList<ItemData> mListData = new ArrayList<ItemData>();

    @Override
    protected void onListItemClick(AmigoListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListData();
        setListAdapter(new AmigoListItemAdapter(this, mListData));
    }

    private void initListData() {
        ItemData data10 = new ItemData();
        data10.mType = AmigoListItemAdapter.TYPE_10;
        data10.mFirstSummary = getString(R.string.type10_level1);
        mListData.add(data10);

        ItemData data11 = new ItemData();
        data11.mType = AmigoListItemAdapter.TYPE_11;
        data11.mFirstSummary = getString(R.string.type11_level1);
        mListData.add(data11);

        ItemData data12 = new ItemData();
        data12.mType = AmigoListItemAdapter.TYPE_12;
        data12.mFirstSummary = getString(R.string.type12_level1);
        mListData.add(data12);

        ItemData data20 = new ItemData();
        data20.mType = AmigoListItemAdapter.TYPE_20;
        data20.mFirstSummary = getString(R.string.type20_level1);
        mListData.add(data20);

        ItemData data21 = new ItemData();
        data21.mType = AmigoListItemAdapter.TYPE_21;
        data21.mFirstSummary = getString(R.string.type21_level1);
        mListData.add(data21);

        ItemData data22 = new ItemData();
        data22.mType = AmigoListItemAdapter.TYPE_22;
        data22.mFirstSummary = getString(R.string.type22_level1);
        mListData.add(data22);

        ItemData data23 = new ItemData();
        data23.mType = AmigoListItemAdapter.TYPE_23;
        data23.mFirstSummary = getString(R.string.type23_level1);
        mListData.add(data23);

        ItemData data30 = new ItemData();
        data30.mType = AmigoListItemAdapter.TYPE_30;
        data30.mFirstSummary = getString(R.string.type30_level1);
        mListData.add(data30);

        ItemData data31 = new ItemData();
        data31.mType = AmigoListItemAdapter.TYPE_31;
        data31.mFirstSummary = getString(R.string.type31_level1);
        mListData.add(data31);

        ItemData data32 = new ItemData();
        data32.mType = AmigoListItemAdapter.TYPE_32;
        data32.mFirstSummary = getString(R.string.type32_level1);
        mListData.add(data32);
    }

}
