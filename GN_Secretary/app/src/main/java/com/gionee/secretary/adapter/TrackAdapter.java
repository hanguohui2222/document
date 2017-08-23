package com.gionee.secretary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gionee.secretary.bean.ExpressSchedule;

import java.util.List;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.DateUtils;

public class TrackAdapter extends BaseAdapter {
    private Context mContext;
    private List<ExpressSchedule.Trace> mTraces;

    public TrackAdapter(Context context, List<ExpressSchedule.Trace> traces) {
        mContext = context;
        mTraces = traces;
    }

    @Override
    public int getCount() {
        return mTraces.size();
    }

    @Override
    public Object getItem(int position) {
        return mTraces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_express_trace, null);
            holder = new ViewHolder();
            holder.mTvTrackDate = (TextView) convertView.findViewById(R.id.track_accepttime);
            holder.mTvTrackStation = (TextView) convertView.findViewById(R.id.track_acceptstation);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ExpressSchedule.Trace trace = (ExpressSchedule.Trace) getItem(position);
        holder.mTvTrackDate.setText(DateUtils.formatDate2String2(DateUtils.formatDate(trace.getAcceptTime())));
        holder.mTvTrackStation.setText(trace.getAcceptStation());
        return convertView;
    }

    private class ViewHolder {
        TextView mTvTrackDate;
        TextView mTvTrackStation;
    }
}
