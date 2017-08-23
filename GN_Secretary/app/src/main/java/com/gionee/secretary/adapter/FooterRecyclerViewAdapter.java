package com.gionee.secretary.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.viewInterface.ISetStateListener;
import com.gionee.secretary.ui.viewInterface.ILoadFooterViewState;

/**
 * Created by zhuboqin on 12/05/16.
 */
public class FooterRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER = 10;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mInnerAdapter;
    private View mFooterView;

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            notifyItemRangeChanged(fromPosition, toPosition + itemCount);
        }
    };
    private Context mContext;
    private int mFooterState;

    public FooterRecyclerViewAdapter() {
    }

    public FooterRecyclerViewAdapter(Context context, RecyclerView.Adapter mInnerAdapter) {
        setAdapter(mInnerAdapter);
        this.mContext = context;
    }

    public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {

        if (adapter != null) {
            if (!(adapter instanceof RecyclerView.Adapter))
                throw new RuntimeException("your adapter must be a RecyclerView.Adapter");
        }
        if (mInnerAdapter != null) {
            notifyItemRangeRemoved(0, mInnerAdapter.getItemCount());
            mInnerAdapter.unregisterAdapterDataObserver(mDataObserver);
        }
        this.mInnerAdapter = adapter;
        mInnerAdapter.registerAdapterDataObserver(mDataObserver);
        notifyItemRangeInserted(0, mInnerAdapter.getItemCount());
    }

    public RecyclerView.Adapter getInnerAdapter() {
        return mInnerAdapter;
    }

    @Override
    public int getItemCount() {
        return mInnerAdapter.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        int innerCount = mInnerAdapter.getItemCount();
        if (position == innerCount) {
            return FOOTER;
        } else {
            int innerItemViewType = mInnerAdapter.getItemViewType(position);
            return innerItemViewType;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER) {
            mFooterView = LayoutInflater.from(mContext).inflate(R.layout.item_footer, parent, false);
            return new FooterViewHolder(mFooterView);
        } else {
            return mInnerAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (position >= 0 && position < mInnerAdapter.getItemCount()) {
            mInnerAdapter.onBindViewHolder(holder, position);
        } else {
            ((ILoadFooterViewState) mContext).setSetStateListener(new ISetStateListener() {
                @Override
                public void setState(int state) {
                    setFooterViewState(holder.itemView, state);
                }
            });
        }
    }

    private ClickToLoadListener clickToLoadListener;

    public void setClickToLoadListener(ClickToLoadListener clickToLoadListener) {
        this.clickToLoadListener = clickToLoadListener;
    }

    public interface ClickToLoadListener {
        void clickToLoad();
    }

    public void setFooterViewState(View footerView, int footerViewState) {
        setFooterViewState(footerView, footerViewState, null);
    }

    public int getmFooterState() {
        return mFooterState;
    }

    public void setFooterViewState(View footerView, int footerViewState, View.OnClickListener onClickListener) {
        this.mFooterState = footerViewState;
        FooterViewHolder footerViewHolder = (FooterViewHolder) footerView.getTag();
        if (footerViewState == Constants.FOOTER_LOADING) {
            footerViewHolder.rl_footer_loading.setVisibility(View.VISIBLE);
            footerViewHolder.tv_footer_normal.setVisibility(View.GONE);
            footerViewHolder.tv_footer_failed.setVisibility(View.GONE);
            footerViewHolder.tv_footer_end.setVisibility(View.GONE);
        } else if (footerViewState == Constants.FOOTER_END) {
            footerViewHolder.tv_footer_end.setVisibility(View.VISIBLE);
            footerViewHolder.rl_footer_loading.setVisibility(View.GONE);
            footerViewHolder.tv_footer_normal.setVisibility(View.GONE);
            footerViewHolder.tv_footer_failed.setVisibility(View.GONE);
        } else if (footerViewState == Constants.FOOTER_FAILED) {
            footerViewHolder.tv_footer_failed.setVisibility(View.VISIBLE);
            footerViewHolder.tv_footer_end.setVisibility(View.GONE);
            footerViewHolder.rl_footer_loading.setVisibility(View.GONE);
            footerViewHolder.tv_footer_normal.setVisibility(View.GONE);
        } else {
            footerViewHolder.tv_footer_normal.setVisibility(View.VISIBLE);
            footerViewHolder.tv_footer_failed.setVisibility(View.GONE);
            footerViewHolder.tv_footer_end.setVisibility(View.GONE);
            footerViewHolder.rl_footer_loading.setVisibility(View.GONE);
        }
        if (onClickListener != null && mFooterState != Constants.FOOTER_END) {
            footerView.setOnClickListener(onClickListener);
        } else {
            footerView.setClickable(false);
        }
    }

    public View getmFooterView() {
        return mFooterView;
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_footer_normal;
        private RelativeLayout rl_footer_loading;
        private TextView tv_footer_end;
        private TextView tv_footer_failed;

        public FooterViewHolder(final View footerView) {
            super(footerView);
            tv_footer_normal = (TextView) footerView.findViewById(R.id.tv_footer_normal);
            rl_footer_loading = (RelativeLayout) footerView.findViewById(R.id.rl_footer_loading);
            tv_footer_end = (TextView) footerView.findViewById(R.id.tv_footer_end);
            tv_footer_failed = (TextView) footerView.findViewById(R.id.tv_footer_failed);
            footerView.setTag(this);
            setFooterViewState(footerView, Constants.FOOTER_NORMAL, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickToLoadListener != null) {
                        setFooterViewState(footerView, Constants.FOOTER_LOADING);
                        clickToLoadListener.clickToLoad();
                    }
                }
            });
        }
    }
}
