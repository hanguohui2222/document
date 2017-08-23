package com.gionee.secretary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gionee.secretary.R;

import java.util.List;

/**
 * Created by liyy 2016-05-13
 */
public class SearchTextHistoryAdapter extends BaseAdapter {
    private List<String> mFileList;
    private Context mContext;

    public SearchTextHistoryAdapter(Context context, List<String> list) {
        this.mFileList = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList == null ? null : mFileList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchTextHistoryHolder imageViewHolder;

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.search_history_text, null);

            imageViewHolder = new SearchTextHistoryHolder(convertView);

            convertView.setTag(imageViewHolder);
        } else {
            imageViewHolder = (SearchTextHistoryHolder) convertView.getTag();
        }

        String searText = mFileList.get(position);

        imageViewHolder.searchHistoryText.setText(searText);

        return convertView;
    }


    class SearchTextHistoryHolder {
        TextView searchHistoryText;

        public SearchTextHistoryHolder(View view) {
            searchHistoryText = (TextView) view.findViewById(R.id.history_text);
        }
    }

}
