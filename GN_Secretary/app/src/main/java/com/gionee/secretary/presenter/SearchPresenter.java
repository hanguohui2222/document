package com.gionee.secretary.presenter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.gionee.secretary.SecretaryApplication;
import com.gionee.secretary.bean.BaseNoteSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.dao.SearchHistoryDao;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.ui.viewInterface.ISearchView;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;

public class SearchPresenter extends BasePresenterImpl<ISearchView>{

    private ScheduleInfoDao mScheduleInfoDao;
    private SearchHistoryDao mSearchTextHistoryDao;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private boolean isShowExpress;
    private List<String> mSearchTextHistoryListDatas;
    private List<BaseSchedule> mSearchContentList = new ArrayList<>();
    private List<ExpressSchedule> mExpressSchedules = new ArrayList<ExpressSchedule>();

    private VoiceNoteDao mVoiceNoteInfoDao;

    public SearchPresenter(ISearchView searchView) {
        attachView(searchView);
        mContext = (Context) searchView;

        intDao();

        //modify by zhengjl at 2017-2-15 for 优化搜索
//		initHistoryData();
        /*modify by zhengjl for  GNSPR #65753 begin
		initSearcherAdapter应该在SearchPresent new之后执行
		*/
        //initSearcherAdapter();
    }

    public SearchPresenter(Context context, int type) {
        mContext = context;
        intDao();
    }


    private void intDao() {
        mScheduleInfoDao = ScheduleInfoDao.getInstance(mContext);
        mSearchTextHistoryDao = new SearchHistoryDao(mContext);

        mVoiceNoteInfoDao = VoiceNoteDao.getInstance(mContext);
    }

    public void initHistoryData() {
        //根据设置开关选择先不显示快递
        mSharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        isShowExpress = mSharedPreferences.getBoolean(Constants.EXPRESS_SWITCH_PREFERENCE_KEY, true);

        //搜索关键字历史
        showHistory();

//        LogUtils.i("liyy", "searchTextHistory.size:" + mSearchTextHistoryListDatas.size());
    }

    public void showHistory() {

        //modify by zhengjl at 2017-2-15 for 优化搜索 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
//				LogUtils.e("zjl","query search history thread run...");
                qureryAllSearchTextHistory();
                SecretaryApplication.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mView.showHistory();
                    }
                });
            }
        }).start();
		/*
        if (mSearchTextHistoryListDatas.size() == 0) {
        	mSearchView.hideSearchHistory();
        }else{
        	mSearchView.showSearchHistory(mSearchTextHistoryListDatas);
        }
        */
        //modify by zhengjl at 2017-2-15 for 优化搜索 end

    }

    public List<String> getSearchTextHistoryResult() {
        return mSearchTextHistoryListDatas;
    }

    public void initSearcherAdapter() {
        mView.initSearcherAdapter(mSearchContentList, mExpressSchedules);
    }

    private void qureryAllSearchTextHistory() {
        if (mSearchTextHistoryListDatas != null) {
            mSearchTextHistoryListDatas.clear();
        } else {
            mSearchTextHistoryListDatas = new ArrayList<>();
        }

        List queryData = mSearchTextHistoryDao.querySearchHistory();
        mSearchTextHistoryListDatas.addAll(queryData);
    }

    private void insertHistoryData(String searchText) {
        mSearchTextHistoryDao.saveSearchHistoryToDB(searchText);
    }

    private boolean hasHistoryData(String option) {
        List<String> textHistory = mSearchTextHistoryDao.querySearchHistory(option);

        if (textHistory.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void saveSearchTextToDB(final String tempName) {
        //modify by zhengjl at 2017-2-15 for 优化搜索 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
//				LogUtils.e("zjl","save search history thread run...");
                if (!TextUtils.isEmpty(tempName)) {
                    boolean hasData = hasHistoryData(tempName);
                    if (!hasData) {
                        insertHistoryData(tempName);
                    }
                }
            }
        }).start();
        //modify by zhengjl at 2017-2-15 for 优化搜索 end

    }

    public void updateExpressSchedule(String time, String info, int state, int id) {
        mScheduleInfoDao.updateExpressSchedule(time, info, state, id);
    }

    public void clearAllSearchTextHistory() {
        mSearchTextHistoryDao.clearSearchHistory();
        mSearchTextHistoryListDatas.clear();
    }

    public void onActivityResume(String tempName, int searchContentPage) {

        showSchedules(tempName, searchContentPage);
    }

    public void showSchedules(final String tempName, final int searchContentPage) {
		 /*Gionee zhengyt 2016-12-20 add for search not Begin*/
//		 querySchedules(tempName,searchContentPage);

        //modify by zhengjl at 2017-2-15 for 优化搜索 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
//				 LogUtils.e("zjl","thread...run...");
                querySchedulesAndNote(tempName, searchContentPage);
                SecretaryApplication.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mView.showScheduleAndNote();
                    }
                });
            }
        }).start();

		 /*Gionee zhengyt 2016-12-20 add for search not End*/
		 /*
         if (mSearchContentList.size() == 0) {
        	 mSearchView.showEmpty();
         } else {
        	 mSearchView.hideEmpty();
         }
         */

        //modify by zhengjl at 2017-2-15 for 优化搜索 end
    }

    public List<BaseSchedule> getSearchResult() {
        return mSearchContentList;
    }

    /*Gionee zhengyt 2016-12-20 add for search not Begin*/
    public void querySchedulesAndNote(String option, int searchContentPage) {
        mSearchContentList.clear();

        if (option == null || TextUtils.isEmpty(option.trim())) {
            return;
        }

        // Gionee sunyang modify for GNSPR #65801 at 2017-02-04 begin
//		 List<BaseSchedule> selfCreateSchedules = mScheduleInfoDao.queryScheduleByKeyWord(option, searchContentPage, isShowExpress);
        List<BaseSchedule> selfCreateSchedules = mScheduleInfoDao.queryByKeyWord(option, searchContentPage, isShowExpress);
        // Gionee sunyang modify for GNSPR #65801 at 2017-02-04 end
        LogUtils.i("liyy", "selfCreateSchedules....:" + selfCreateSchedules.size() + "  ,mSearchContentPage:" + searchContentPage);
        List<BaseNoteSchedule> note = mVoiceNoteInfoDao.queryNoteByKeyWord(option, searchContentPage);
        LogUtils.i("liyy22", "note....:" + note.size() + "  ,note:" + searchContentPage);

        if (selfCreateSchedules.size() <= 0 && note.size() <= 0) {
            mView.isToLoadMore(false);
        }

        filterExcludeExpress(selfCreateSchedules);
        for (BaseNoteSchedule e : note) {
            mSearchContentList.add(e);
        }


    }
	 
	 /*Gionee zhengyt 2016-12-20 add for search not End*/

    public void querySchedules(String option, int searchContentPage) {
        mSearchContentList.clear();
        if (option == null || TextUtils.isEmpty(option.trim())) {
            return;
        }

        List<BaseSchedule> selfCreateSchedules = mScheduleInfoDao.queryScheduleByKeyWord(option, searchContentPage, isShowExpress);
        LogUtils.i("liyy", "selfCreateSchedules....:" + selfCreateSchedules.size() + "  ,mSearchContentPage:" + searchContentPage);

        if (selfCreateSchedules.size() <= 0) {
            mView.isToLoadMore(false);
        } else {

            if (isShowExpress) {
                filterExpress(selfCreateSchedules);//快递要在最上面显示 ,包括 今天签收的，以及所有未签收状态的，
                if (null != mExpressSchedules && mExpressSchedules.size() > 0) {
                    ExpressSchedule expressSchedule = mExpressSchedules.get(0);
                    mSearchContentList.add(0, expressSchedule);
                }
            }
            filterExcludeExpress(selfCreateSchedules);
        }
    }


    private void filterExcludeExpress(List<BaseSchedule> selfCreateSchedules) {
        for (BaseSchedule e : selfCreateSchedules) {
            boolean isExpress = e.getType() == Constants.EXPRESS_TYPE;
            //if (!isExpress) {
            mSearchContentList.add(e);
            //}
        }
    }

    private void filterExpress(List<BaseSchedule> selfCreateSchedules) {
        mExpressSchedules.clear();
        Date today = Calendar.getInstance().getTime();
        for (BaseSchedule e : selfCreateSchedules) {
            boolean isExpress = e.getType() == Constants.EXPRESS_TYPE;
            if (isExpress) {
                if (((ExpressSchedule) e).getState() == 3 && !DateUtils.date2String(((ExpressSchedule) e).getTrace_date()).equals(DateUtils.date2String(today))) {
                    continue;
                } else {
                    e.setDate(today);
                    final ExpressSchedule express = (ExpressSchedule) e;
                    mExpressSchedules.add(express);
                }
            }
        }
    }

}
