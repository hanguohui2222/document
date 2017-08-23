package com.gionee.secretary.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.gionee.secretary.utils.LogUtils;

/**
 * Created by liyh on 1/22/17.
 * 自定义RecyclerView，支持无数据时显示EmptyView
 */

public class CalendarRecyclerView extends RecyclerView {

    private static final String LOG_TAG = CalendarRecyclerView.class.getSimpleName();
    private View emptyView;

    public CalendarRecyclerView(Context context) {
        super(context);
    }

    public CalendarRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CalendarRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            LogUtils.d("CalendarObserver", "onChanged()");
            checkEmpty();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            LogUtils.d("CalendarObserver", "onItemRangeChanged()");
            super.onItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            LogUtils.d("CalendarObserver", "onItemRangeInserted()");
            checkEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            LogUtils.d("CalendarObserver", "onItemRangeRemoved()");
            checkEmpty();
        }
    };

    public void setEmptyView(View empty) {
        this.emptyView = empty;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        LogUtils.d(LOG_TAG, "setAdapter()");
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        LogUtils.d(LOG_TAG, "setAdapter(), after super.");
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
            LogUtils.d(LOG_TAG, "registerAdapterDataObserver()");
        }
        checkEmpty();
    }

    private void checkEmpty() {
        LogUtils.d(LOG_TAG, "checkEmpty(). ");
        if (emptyView != null && getAdapter() != null) {
            Adapter adapter = getAdapter();
            LogUtils.d(LOG_TAG, "checkEmpty(). getItemCount=" + adapter.getItemCount());
            Boolean isEmptyViewVisible = adapter.getItemCount() == 0;
            emptyView.setVisibility(isEmptyViewVisible ? VISIBLE : GONE);
            setVisibility(isEmptyViewVisible ? GONE : VISIBLE);
        }
    }

}
