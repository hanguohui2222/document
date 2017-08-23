package com.gionee.secretary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gionee.secretary.bean.AddressBean;

import java.util.ArrayList;
import java.util.List;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by liyingheng on 2016/12/24.
 */

public class SelectAddressAdapter extends BaseAdapter {


    private final String LOG_TAG = SelectAddressAdapter.class.getSimpleName();
    private Context mContext;
    private List<AddressBean> addressBeenList;

    public SelectAddressAdapter(Context context, List<AddressBean> data) {
        addressBeenList = new ArrayList<>();
        mContext = context;
        if (data != null) {
            addressBeenList = data;
        }
    }

    public void setDatasource(List<AddressBean> addressData) {
        if (addressData != null) {
            addressBeenList = addressData;
        }
    }

    @Override
    public int getCount() {
        return addressBeenList.size();
    }

    @Override
    public Object getItem(int i) {
        return addressBeenList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(mContext, R.layout.item_address, null);
        }
        ViewHolder holder = new ViewHolder(view);
        if (addressBeenList != null) {
            AddressBean address = addressBeenList.get(i);
            String addressTitle = address.getName();
            String addressDesc = address.getDesc();
            LogUtils.d(LOG_TAG, "getView(): addressTitle=" + addressTitle + ";addressDesc=" + addressDesc + ";index=" + i);
            holder.tvTitle.setText(addressTitle);
            holder.tvDesc.setText(addressDesc);
        }
        return view;
    }

    private class ViewHolder {
        TextView tvTitle;
        TextView tvDesc;

        public ViewHolder(View view) {
            tvTitle = (TextView) view.findViewById(R.id.text_address);
            tvDesc = (TextView) view.findViewById(R.id.text_address_desc);
        }
    }
}
