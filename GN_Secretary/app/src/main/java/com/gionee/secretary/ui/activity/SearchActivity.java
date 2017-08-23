package com.gionee.secretary.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gionee.secretary.R;
import com.gionee.secretary.adapter.SearchCardAdapter;
import com.gionee.secretary.adapter.SearchTextHistoryAdapter;
import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.ExpressSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.bean.HotelSchedule;
import com.gionee.secretary.bean.MovieSchedule;
import com.gionee.secretary.bean.SelfCreateSchedule;
import com.gionee.secretary.bean.TrainSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.presenter.SearchPresenter;
import com.gionee.secretary.ui.viewInterface.ISearchView;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.ui.viewInterface.ISetStateListener;
import com.gionee.secretary.ui.viewInterface.ILoadFooterViewState;

import java.lang.ref.SoftReference;
import java.util.List;

import amigoui.app.AmigoActionBar;
import amigoui.widget.AmigoButton;
import amigoui.widget.AmigoEditText;

/**
 * Created by liyy 2016-05-13
 */
public class SearchActivity extends PasswordBaseActivity implements ILoadFooterViewState, ISearchView {

    private AmigoEditText mSearchEditText;
    private ImageButton mClearEditTextButton;
    private LinearLayout mSearch_text_history_layout;
    private ListView mSearchTextHistoryListView;
    private AmigoButton mClearAllHistory;
    private SearchTextHistoryAdapter mSearchTextHistoryAdapter;
    private RecyclerView mSearchContent;
    private int mSearchContentPage = 0;
    private SearchCardAdapter mSearchContentCardAdapter;
    private LinearLayoutManager mSearchContentLinearLayoutManager;
    private LinearLayout mEmpty;
    private static final int REQUEST_CARD = 500;
    private ISetStateListener setStateListener;
    private boolean isToLoadMore = false;
    private SearchPresenter mSearchPresent;
    private static final String TAG = "SearchActivity";
    private static final int MAX_TITLE_EXT_LENGTH = 50;


    //add by zhengjl at 2017-2-15 for 优化搜索 not begin
    private SearchHandler mHandler;
    private static final int QUERY_SCHDULE_AND_NOTE = 1;
    private static final int QUERY_HISTORY = 2;

    private static class SearchHandler extends Handler {

        private final SoftReference<SearchActivity> mActivity;

        public SearchHandler(SearchActivity activity) {
            mActivity = new SoftReference<SearchActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SearchActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case QUERY_SCHDULE_AND_NOTE:

                    //Log.e("zjl","QUERY_SCHDULE_AND_NOTE...result:" + activity.mSearchPresent.getSearchResult().size());
                    if (activity.mSearchPresent.getSearchResult().size() == 0) {
                        activity.showEmpty();
                    } else {
                        activity.hideEmpty();
                        activity.mSearchContentCardAdapter.initNoteCard();
                        activity.mSearchContentCardAdapter.initSchduleCard();
                        activity.mSearchContentCardAdapter.notifyDataSetChanged();
                    }
                    break;
                case QUERY_HISTORY:
                    //Log.e("zjl","QUERY_HISTORY...result:" + activity.mSearchPresent.getSearchTextHistoryResult().size());
                    if (activity.mSearchPresent.getSearchTextHistoryResult().size() == 0) {
                        activity.hideSearchHistory();
                    } else {
                        activity.showSearchHistory(activity.mSearchPresent.getSearchTextHistoryResult());
                    }

                    break;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_main);
        initActionBar();
        initViews();

        //add by zhengjl at 2017-2-15 for 优化搜索 not begin
        mHandler = new SearchHandler(this);
        //add by zhengjl at 2017-2-15 for 优化搜索 not begin
        mSearchPresent = new SearchPresenter(this);

        //modify by zhengjl at 2017-2-15 for 优化搜索 not begin
        mSearchPresent.initHistoryData();
        /*modify by zhengjl for  GNSPR #65753 not end*/
        mSearchPresent.initSearcherAdapter();

    }

    protected void onResume() {
        super.onResume();
        String tempName = mSearchEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(tempName)) {
            mSearchContentPage = 0;
            mSearchContentCardAdapter.setTitlehighLightCompile(tempName);
            mSearch_text_history_layout.setVisibility(View.GONE);
            mClearEditTextButton.setVisibility(View.VISIBLE);
            mSearchPresent.onActivityResume(tempName, mSearchContentPage);

            //modify by zhengjl at 2017-2-15 for 优化搜索 not begin
            /*
            mSearchContentCardAdapter.notifyDataSetChanged();
            mSearchContentCardAdapter.initNoteCard();
            mSearchContentCardAdapter.initSchduleCard();
            */
            LogUtils.i(TAG, "-----------onResume.....");
        }
    }


    private void initViews() {
        mSearchTextHistoryListView = (ListView) findViewById(R.id.search_text_history);
        mSearchContent = (RecyclerView) findViewById(R.id.search_content);
        mEmpty = (LinearLayout) findViewById(R.id.empty_stub);

        mSearch_text_history_layout = (LinearLayout) findViewById(R.id.search_text_history_layout);
        mClearAllHistory = (AmigoButton) findViewById(R.id.clear_all_history);

        mClearAllHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchPresent.clearAllSearchTextHistory();
                mSearchTextHistoryAdapter.notifyDataSetChanged();
                mSearch_text_history_layout.setVisibility(View.GONE);
            }
        });
    }

    private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View view = getLayoutInflater().inflate(R.layout.actionbar_search_activity, null);
        ImageView btn_back = (ImageView) view.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        mSearchEditText = (AmigoEditText) view.findViewById(R.id.search_edittext);
        mClearEditTextButton = (ImageButton) view.findViewById(R.id.clearButton);
        mSearchEditText.addTextChangedListener(new SearchTextWatcher(mSearchEditText, MAX_TITLE_EXT_LENGTH));
        mClearEditTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditText.setText("");
            }
        });
//        mSearchEditText.setOnKeyListener(new SearchTextOnKeyListener());

        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(AmigoActionBar.LayoutParams.WRAP_CONTENT,
                AmigoActionBar.LayoutParams.WRAP_CONTENT);
        mActionBar.setCustomView(view, param);
        mActionBar.show();

    }

//    private void querySearchText(String searchText) {
//        //更新list显示
//        mSearchTextHistoryListDatas.clear();
//        //查找 所有历史包含输入内容并且显示
//        mSearchTextHistoryListDatas.addAll(mSearchTextHistoryDao.querySearchTextByKeyWord(searchText));
//        mSearchTextHistoryAdapter.notifyDataSetChanged();
//    }


    public void setSetStateListener(ISetStateListener setStateListener) {
        this.setStateListener = setStateListener;
    }


    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            /*Gionee zhengyt delete for scroll*/
//            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                //获取最后一个完全显示的itemposition
//                int lastVisiblePos = mSearchContentLinearLayoutManager.findLastCompletelyVisibleItemPosition();
//                int itemCount = mSearchContentLinearLayoutManager.getItemCount();
//                if (lastVisiblePos == (itemCount - 1) && isToLoadMore) {
//                    mSearchContentPage++;
//                    mSearchPresent.querySchedules(mSearchEditText.getText().toString(), mSearchContentPage);
//                }
//            }
            /*Gionee zhengyt delete for scroll*/
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        	/*Gionee zhengyt delete for scroll*/
//            if (dy > 0) {
//                isToLoadMore = true;
//            } else {
//                isToLoadMore = false;
//            }
        	/*Gionee zhengyt delete for scroll*/
        }
    };


    private void enterScheduleDetails(BaseSchedule schedule) {

        int type = schedule.getType();
        Intent intent = new Intent();
        switch (type) {
            case Constants.SELF_CREATE_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                SelfCreateSchedule selfCreateSchedule = (SelfCreateSchedule) schedule;
                intent.putExtra("schedule", selfCreateSchedule);
                break;
            case Constants.BANK_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                BankSchedule bankSchedule = (BankSchedule) schedule;
                intent.putExtra("schedule", bankSchedule);
                break;
            case Constants.TRAIN_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                TrainSchedule trainSchedule = (TrainSchedule) schedule;
                intent.putExtra("schedule", trainSchedule);
                break;
            case Constants.FLIGHT_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                FlightSchedule flightSchedule = (FlightSchedule) schedule;
                intent.putExtra("schedule", flightSchedule);
                break;
            case Constants.MOVIE_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                MovieSchedule movieSchedule = (MovieSchedule) schedule;
                intent.putExtra("schedule", movieSchedule);
                break;
            case Constants.HOTEL_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                HotelSchedule hotelSchedule = (HotelSchedule) schedule;
                intent.putExtra("schedule", hotelSchedule);
                break;
            case Constants.EXPRESS_TYPE:
                intent.setClass(this, CardDetailsActivity.class);
                ExpressSchedule expressSchedule = (ExpressSchedule) schedule;
                intent.putExtra("schedule", expressSchedule);
                break;
            case Constants.WEATHER_TYPE:
                PackageManager packageManager = this.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage(Constants.PACKAGE_WEATHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }

        /*如果是备忘点击，就进入备忘详情卡片*/
        if (schedule.date == null) {
            Intent noteIntent = new Intent();
            noteIntent.setClass(this, NoteDetailActivity.class);
            noteIntent.putExtra("noteid", schedule.id);
            noteIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            startActivity(noteIntent);
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivity(intent);

    }

//    @Override
//    public void onClick(View view) {
//        if(view.getId() == R.id.clear_search_text_history_button){
//
//        }
//    }


    private class SearchTextWatcher implements TextWatcher {
        private int mMaxLength = 0;
        private AmigoEditText mEditText = null;
        private int start;
        private int count;

        public SearchTextWatcher(AmigoEditText editText, int maxLength) {
            mEditText = editText;
            mMaxLength = maxLength;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            this.start = start;
            this.count = count;
        }

        private void controlTextLengh(Editable editable) {
            if (TextUtils.isEmpty(editable.toString().trim())) {
                return;
            }
            LogUtils.i(TAG, "afterTextChanged....." + "  ,start:" + start + "  ,count:" + count);
            int length = editable.toString().length();
            int beforeLength = length - count;
            int leftInput = mMaxLength - beforeLength;
            int delStart = start + leftInput;
            int delEnd = start + count;
            if (length > mMaxLength) {
                LogUtils.i(TAG, "afterTextChanged...>lenght");
                Toast.makeText(SearchActivity.this, getResources().getString(R.string.texttoolong), Toast.LENGTH_SHORT).show();
                editable.delete(delStart, delEnd);
                mEditText.setText(editable);
                mEditText.setSelection(delStart);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            controlTextLengh(editable);
            String tempName = mSearchEditText.getText().toString().trim();
            mSearchContentPage = 0;
            // 根据tempName去模糊查询数据库中有没有数据
            if (TextUtils.isEmpty(tempName)) {

                mSearchPresent.showHistory();

                mClearEditTextButton.setVisibility(View.GONE);
                mEmpty.setVisibility(View.GONE);
                mSearchContent.setVisibility(View.GONE);

                //add by zhengjl at 2017-2-15 for 优化搜索 not begin
                /*
                if (mSearchTextHistoryAdapter != null){
                    mSearchTextHistoryAdapter.notifyDataSetChanged();
                }
                */

            } else {

                //add by zhengjl at 2017-2-15 for 优化搜索 not begin
                /*
            	if (mSearchContentCardAdapter != null){
            		mSearchContentCardAdapter.notifyDataSetChanged();
                    /*add by zhengjl at 2017-2-7 for search
                      搜索界面显示效果

                    mSearchContentCardAdapter.initNoteCard();
                    mSearchContentCardAdapter.initSchduleCard();
                }
            */
                mSearchContentCardAdapter.setTitlehighLightCompile(tempName);
                mSearch_text_history_layout.setVisibility(View.GONE);
                mClearEditTextButton.setVisibility(View.VISIBLE);
                mSearchPresent.showSchedules(tempName, mSearchContentPage);

            }
        }
    }


    /**
     * 根据type参数，将查询之后的结果更新到视图
     *
     * @param type 1/2 查询日程和备忘/查询历史记录
     */
    @Override
    public void updateView(int type) {
        if (type == 1) {
            Message message = new Message();
            message.what = QUERY_SCHDULE_AND_NOTE;
            mHandler.sendMessage(message);
        } else {
            Message message = new Message();
            message.what = QUERY_HISTORY;
            mHandler.sendMessage(message);
        }

    }

    @Override
    public void hideSearchHistory() {
        mSearch_text_history_layout.setVisibility(View.GONE);
    }

    @Override
    public void showSearchHistory(final List<String> searchTextHistoryDatas) {
        if (mSearchTextHistoryAdapter == null) {
            mSearchTextHistoryAdapter = new SearchTextHistoryAdapter(this, searchTextHistoryDatas);
            mSearchTextHistoryListView.setAdapter(mSearchTextHistoryAdapter);
        }
        mSearch_text_history_layout.setVisibility(View.VISIBLE);

        //add by zhengjl at 2017-2-15 for 优化搜索
        mSearchTextHistoryAdapter.notifyDataSetChanged();


        mSearchTextHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String searchText = searchTextHistoryDatas.get(position);
                mSearchEditText.append(searchText);
            }
        });
    }

    @Override
    public void initSearcherAdapter(List<BaseSchedule> searchContentList, List<ExpressSchedule> expressSchedules) {
        mSearchContentLinearLayoutManager = new LinearLayoutManager(this);
        mSearchContent.setLayoutManager(mSearchContentLinearLayoutManager);

        mSearchContentCardAdapter = new SearchCardAdapter(this, mSearchContent, searchContentList, expressSchedules, mSearchPresent);
        mSearchContentCardAdapter.setNeedHighLight(true);
        mSearchContent.setAdapter(mSearchContentCardAdapter);

        mSearchContentCardAdapter.setClickItemListener(new SearchCardAdapter.ClickItemListener() {
            @Override
            public void onClick(BaseSchedule event) {
                enterScheduleDetails(event);
                String tempName = mSearchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(tempName)) {
                    mSearchPresent.saveSearchTextToDB(tempName);
                }
            }
        });

//        mSearchContent.addOnScrollListener(onScrollListener);
        mSearchContent.setOnScrollListener(onScrollListener);

    }

    @Override
    public void showEmpty() {
        mEmpty.setVisibility(View.VISIBLE);
        mSearchContent.setVisibility(View.GONE);
    }

    @Override
    public void hideEmpty() {
        mEmpty.setVisibility(View.GONE);
        mSearchContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void isToLoadMore(boolean isToLoadMore) {
        this.isToLoadMore = isToLoadMore;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    //    private class SearchTextOnKeyListener implements View.OnKeyListener{
//
//        @Override
//        public boolean onKey(View v, int keyCode, KeyEvent event) {
//            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
//                // 先隐藏键盘
//                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
//                        getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                // 按完搜索键后将当前查询的关键字保存起来,如果该关键字已经存在就不执行保存
//                String tempName = mSearchEditText.getText().toString().trim();
//                if(!TextUtils.isEmpty(tempName)){
//                    boolean hasData = hasHistoryData(tempName);
//                    if (!hasData) {
//                        insertHistoryData(tempName);
//                    }
//
//                    // TODO 根据输入的内容模糊查询商品，并跳转到另一个界面
//                    querySchedules(tempName);
//
//                }
//            }
//            return false;
//        }
//    }
}