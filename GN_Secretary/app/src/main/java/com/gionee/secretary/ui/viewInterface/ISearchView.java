package com.gionee.secretary.ui.viewInterface;

import java.util.List;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;

public interface ISearchView {
    void hideSearchHistory();

    void showSearchHistory(List<String> searchTextHistoryDatas);

    void initSearcherAdapter(List<BaseSchedule> searchContentList, List<ExpressSchedule> expressSchedules);

    void showEmpty();

    void hideEmpty();

    void isToLoadMore(boolean isToLoadMore);

    void showScheduleAndNote();

    void showHistory();
}
