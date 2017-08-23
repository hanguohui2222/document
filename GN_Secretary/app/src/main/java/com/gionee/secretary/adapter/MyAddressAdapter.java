package com.gionee.secretary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.AddressBean;

import java.util.List;

/**
 * Created by rongdd on 16-5-5.
 */
public class MyAddressAdapter extends BaseAdapter {

    private Context mContext;
    private List<AddressBean> addressList;

    public MyAddressAdapter(Context context, List<AddressBean> addressList) {
        mContext = context;
        this.addressList = addressList;
    }

    @Override
    public int getCount() {
        return addressList.size();
    }

    @Override
    public Object getItem(int position) {
        return addressList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_address, null);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.text_address);
            viewHolder.tvAddressDesc = (TextView) convertView.findViewById(R.id.text_address_desc);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvAddress.setText(addressList.get(position).getName());
        viewHolder.tvAddressDesc.setText(addressList.get(position).getDesc());
        return convertView;
    }

    class ViewHolder {
        TextView tvAddress;
        TextView tvAddressDesc;
    }

    /**
     * 刷新新的数据
     *
     * @param addressList
     */
    public void setNewDate(List<AddressBean> addressList) {
        if (null != addressList) {
            this.addressList = addressList;
            notifyDataSetChanged();
        }
    }

    public void clearData() {
        if (addressList != null) {
            addressList.clear();
            notifyDataSetChanged();
        }
    }
}
