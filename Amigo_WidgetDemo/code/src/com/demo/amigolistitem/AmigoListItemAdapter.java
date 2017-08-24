package com.demo.amigolistitem;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amigo.widgetdemol.R;

public class AmigoListItemAdapter extends BaseAdapter {
    
    public static final int TYPE_10 = 10;
    public static final int TYPE_11 = 11;
    public static final int TYPE_12 = 12;
    public static final int TYPE_20 = 20;
    public static final int TYPE_21 = 21;
    public static final int TYPE_22 = 22;
    public static final int TYPE_23 = 23;
    public static final int TYPE_30 = 30;
    public static final int TYPE_31 = 31;
    public static final int TYPE_32 = 32;
    public static final int TYPE_1 = 0;
    public static final int TYPE_2 = 1;
    public static final int TYPE_3 = 2;
    private static final int ITEM_TYPE_NUM = TYPE_3 + 1;
    
    private Context mCxt;
    private ArrayList<ItemData> mListData;

    public AmigoListItemAdapter(Context cxt, ArrayList<ItemData> data) {
        mCxt = cxt;
        mListData = data;
    }

    @Override
    public int getItemViewType(int position) {
        int type = mListData.get(position).mType;
        switch (type) {
            case TYPE_10:
            case TYPE_11:
            case TYPE_12:
            default:
                return TYPE_1;
            case TYPE_20:
            case TYPE_21:
            case TYPE_22:
            case TYPE_23:
                return TYPE_2;
            case TYPE_30:
            case TYPE_31:
            case TYPE_32:
                return TYPE_3;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_TYPE_NUM;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int index) {
        return mListData.get(index);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_1:
                convertView = constructItemView1(position,convertView,parent);
                break;
            case TYPE_2:
                convertView = constructItemView2(position,convertView,parent);
                break;
            case TYPE_3:
                convertView = constructItemView3(position,convertView,parent);
                break;
            default:
                break;
        }

        return convertView;
    }

    private View constructItemView1(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mCxt).inflate(R.layout.common_list_item_type_single_line, parent, false);
            holder.mHeadIcon = (ImageView) convertView.findViewById(R.id.headicon);
            holder.mTailIcon = (ImageView) convertView.findViewById(R.id.tailicon);
            holder.textLevel1View = (TextView) convertView.findViewById(R.id.textlevel1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ItemData data = mListData.get(position);
        holder.textLevel1View.setText(data.mFirstSummary);
        int type = mListData.get(position).mType;
        switch (type) {
            case TYPE_10:
            default:
                break;
            case TYPE_11:
                holder.mTailIcon.setVisibility(View.GONE);
                break;
            case TYPE_12:
                holder.mTailIcon.setVisibility(View.GONE);
                holder.mHeadIcon.setVisibility(View.GONE);
                break;
        }
        
        return convertView;
    }
    
    private View constructItemView2(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mCxt).inflate(R.layout.common_list_item_type_two_line, parent, false);
            holder.mHeadIcon = (ImageView) convertView.findViewById(R.id.headicon);
            holder.mTailIcon = (ImageView) convertView.findViewById(R.id.tailicon);
            holder.textLevel1View = (TextView) convertView.findViewById(R.id.textlevel1);
            holder.textLevel2View = (TextView) convertView.findViewById(R.id.textlevel2);
            holder.textTail = (TextView) convertView.findViewById(R.id.textTail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ItemData data = mListData.get(position);
        holder.textLevel1View.setText(data.mFirstSummary);
        int type = mListData.get(position).mType;
        switch (type) {
            case TYPE_20:
            default:
                break;
            case TYPE_21:
                holder.mTailIcon.setVisibility(View.GONE);
                break;
            case TYPE_22:
                holder.mTailIcon.setVisibility(View.GONE);
                holder.textTail.setVisibility(View.GONE);
                break;
            case TYPE_23:
                holder.mTailIcon.setVisibility(View.GONE);
                holder.textTail.setVisibility(View.GONE);
                holder.mHeadIcon.setVisibility(View.GONE);
                break;
        }
        
        return convertView;
    }
    
    private View constructItemView3(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mCxt).inflate(R.layout.common_list_item_type_three_line, parent, false);
            holder.mHeadIcon = (ImageView) convertView.findViewById(R.id.headicon);
            holder.mTailIcon = (ImageView) convertView.findViewById(R.id.tailicon);
            holder.textLevel1View = (TextView) convertView.findViewById(R.id.textlevel1);
            holder.textLevel2View = (TextView) convertView.findViewById(R.id.textlevel2);
            holder.textLevel3View = (TextView) convertView.findViewById(R.id.textlevel3);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ItemData data = mListData.get(position);
        holder.textLevel1View.setText(data.mFirstSummary);
        int type = mListData.get(position).mType;
        switch (type) {
            case TYPE_30:
            default:
                break;
            case TYPE_31:
                holder.mTailIcon.setVisibility(View.GONE);
                break;
            case TYPE_32:
                holder.mTailIcon.setVisibility(View.GONE);
                holder.mHeadIcon.setVisibility(View.GONE);
                break;
        }
        
        return convertView;
    }
    
    class ViewHolder {
        public TextView textLevel1View;
        public TextView textLevel2View;
        public TextView textLevel3View;
        public TextView textTail;
        public ImageView mHeadIcon;
        public ImageView mTailIcon;
    }
}
