package com.gionee.secretary.ui.fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.BankSchedule;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.CardDetailsUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.ui.activity.CardDetailsActivity;
import com.ted.android.core.SmsEntityLoader;
import com.ted.android.data.SmsEntity;
import com.ted.android.data.bubbleAction.ActionBase;

import java.util.List;
import java.util.ArrayList;

import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoTextView;

/**
 * Created by liu on 5/17/16.
 */
public class CreditCardDetailsFragment extends Fragment {
    private static final String TAG = "CreditCardDetailsFragment";

    LinearLayout mAccountPrompt;
    LinearLayout mBillsMonthPrompt;
    LinearLayout mMoneyPrompt;
    LinearLayout mRepayTimePrompt;
    AmigoTextView mTitle;
    AmigoTextView mMoney;
    AmigoTextView mAccounts;
    AmigoTextView mAccount;
    AmigoTextView mBillsMonth;
    AmigoTextView tv_bills_month;
    AmigoTextView mRepayTime;
    AmigoTextView tv_repay_date;
    AmigoTextView mAlipay;
    ImageView iv_line;
    BankSchedule schedule;
    Context mContext;

    public CreditCardDetailsFragment() {
        // Required empty public constructor
    }

    public static CreditCardDetailsFragment newInstance() {
        CreditCardDetailsFragment fragment = new CreditCardDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardDetailsActivity activity = (CardDetailsActivity) getActivity();
        mContext = activity;
        schedule = (BankSchedule) activity.getSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_credit_card_details, container, false);
        initView(root);
        updateScheduleInfo();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getEntryForPayAction();
    }

    private void initView(View root) {
        mTitle = (AmigoTextView) root.findViewById(R.id.title);
        mMoney = (AmigoTextView) root.findViewById(R.id.money);
        mAccounts = (AmigoTextView) root.findViewById(R.id.accounts);
        mAccount = (AmigoTextView) root.findViewById(R.id.account);
        mBillsMonth = (AmigoTextView) root.findViewById(R.id.bills_month);
        tv_bills_month = (AmigoTextView) root.findViewById(R.id.tv_bills_month);
        mRepayTime = (AmigoTextView) root.findViewById(R.id.repay_time);
        tv_repay_date = (AmigoTextView) root.findViewById(R.id.tv_repay_date);
        mAlipay = (AmigoTextView) root.findViewById(R.id.alipay);
        mAccountPrompt = (LinearLayout) root.findViewById(R.id.account_prompt);
        mBillsMonthPrompt = (LinearLayout) root.findViewById(R.id.bills_month_prompt);
        mRepayTimePrompt = (LinearLayout) root.findViewById(R.id.repay_time_prompt);
        mMoneyPrompt = (LinearLayout) root.findViewById(R.id.money_prompt);
        iv_line = (ImageView) root.findViewById(R.id.iv_line);
    }

    private void updateScheduleInfo() {
        if(schedule == null)
            return;
        if (schedule.getBankName() == null || schedule.getBankName().equals("null")) {
            mTitle.setText(schedule.getTitle());
        } else {
            mTitle.setText(schedule.getTitle() + "-" + schedule.getBankName());
        }
        if (TextUtils.isEmpty(schedule.getRepaymentAmount()) || schedule.getRepaymentAmount().equals("null")) {
            mMoney.setText(R.string.null_money);
        } else {
            if (schedule.getRepaymentAmount().contains("美元")) {
                int index = schedule.getRepaymentAmount().indexOf("美");
                String rmb = schedule.getRepaymentAmount().substring(0, index);
                String doll = schedule.getRepaymentAmount().substring(index, schedule.getRepaymentAmount().length());
                String money = rmb + "\n" + doll;
                mMoney.setText(money);
            } else
                mMoney.setText(schedule.getRepaymentAmount());
        }
        if (!TextUtils.isEmpty(schedule.getCardNum()) && !schedule.getCardNum().contains("null")) {
            mAccounts.setText(schedule.getCardNum());
        } else {
            mAccounts.setVisibility(View.GONE);
            mAccount.setVisibility(View.GONE);
        }
        if (schedule.getBillMonth() == null || schedule.getBillMonth().contains("null")) {
            mBillsMonth.setVisibility(View.GONE);
            tv_bills_month.setVisibility(View.GONE);
        }
        mBillsMonth.setText(schedule.getBillMonth());
        if (TextUtils.isEmpty(schedule.getRepaymentAmount()) || schedule.getRepaymentAmount().trim().equals("null")) {
            mRepayTime.setVisibility(View.GONE);
            tv_repay_date.setVisibility(View.GONE);
        } else {
            if (schedule.getRepaymentAmount().trim().equals("null")) {
                mRepayTime.setText(getResources().getText(R.string.null_value));
            } else {
                mRepayTime.setText(schedule.getRepaymentMonth());
            }
        }
        mAlipay.setText(getResources().getString(R.string.alipay));

        //当无值时隐藏相应内容
        CardDetailsUtils.setShowStatus(mAccountPrompt, schedule.getCardNum());
        CardDetailsUtils.setShowStatus(mBillsMonthPrompt, schedule.getBillMonth());
        CardDetailsUtils.setShowStatus(mRepayTimePrompt, schedule.getRepaymentMonth());

    }


    private void getEntryForPayAction() {
        if(schedule == null)
            return;
        LogUtils.i(TAG, "getEntryForPayAction...getSmsContent:" + schedule.getSmsContent() + "  getSmsSender:" + schedule.getSmsSender());
        long key = System.currentTimeMillis();
        SmsEntityLoader entityLoader = SmsEntityLoader.getInstance(mContext.getApplicationContext());

        SmsEntity entity = entityLoader.loadSmsEntity(key, schedule.getSmsContent(), schedule.getSmsSender(),
                System.currentTimeMillis(),
                new SmsEntityLoader.SmsEntityLoaderCallback() {
                    @Override
                    public void onSmsEntityLoaded(Long aLong,
                                                  SmsEntity smsEntity) {
                        List<ActionBase> showActions = getActions(smsEntity);
                        filterActionForPayLink(showActions);

                    }
                });
        if (entity != null) {
            LogUtils.e(TAG, entity.toString());
        }
    }

    private void filterActionForPayLink(List<ActionBase> showActions) {
        if (null != showActions && showActions.size() >= 0) {
            LogUtils.i(TAG, "getEntryForPayAction...showActions.size:" + showActions.size());
            for (ActionBase ab : showActions) {
                //modified by luorw for GNSPR #72083 2017-03-15 begin
//                if (ab.action == ClickType.CLICKTYPE_APP && Constants.APP_PAY_PACKAGENAME.equals(ab.packageName)) {
                LogUtils.i(TAG, "getEntryForPayAction....actionBase:" + ab.toString());

                if (showPayLink()) {
                    setPayLink(ab);
                } else {
                    mAlipay.setVisibility(View.GONE);
                }
                break;
//                }
                //modified by luorw for GNSPR #72083 2017-03-15 end
            }
        }
    }

    private List<ActionBase> getActions(SmsEntity entity) {
        List<ActionBase> showActions = new ArrayList<ActionBase>();
        List<ActionBase> actionBases = entity.getAllActions();
        LogUtils.i(TAG, "getActions...entity:" + entity.toString());
        if (entity != null) {
            if (entity.hasBusinessADAction()) {
                for (ActionBase actionBase : actionBases) {
                    if (actionBase.businessType == ActionBase.BUSINESS_TYPE_AD) {
                        LogUtils.i(TAG, "getActions....businessType == ActionBase.BUSINESS_TYPE_AD.......actionBase:" + actionBase.toString());
                        showActions.add(actionBase);
                    } else {
                        LogUtils.i(TAG, "getActions....businessType != ActionBase.BUSINESS_TYPE_AD.......actionBase:" + actionBase.toString());
                        showActions.add(actionBase);
                    }
                }
            } else {
                LogUtils.i(TAG, "getActions....!entity.hasBusinessADAction.......actionBase");
                showActions = actionBases;
            }
        }
        return showActions;
    }

    private void setPayLink(final ActionBase actionBase) {
        mAlipay.setVisibility(View.VISIBLE);
        final boolean isInstall = appAvailable(mContext,Constants.APP_PAY_PACKAGENAME);
        if(!isInstall){
            mAlipay.setText(getResources().getString(R.string.alipay_uninstall));
        } else {
            mAlipay.setText(getResources().getString(R.string.alipay));
        }
        mAlipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i(TAG, "creditcard...setPayLink....click...action:" + actionBase.action);
                if(!isInstall){
                    try {
                        Intent mainIntent = new Intent(Constants.MARKET_SEARCH_ACTION, null);
                        mainIntent.putExtra(Constants.MARKET_PAY_EXTRA_KEY1, Constants.MARKET_EXTRA_PAY_VALUE);
                        mainIntent.putExtra(Constants.MARKET_PAY_EXTRA_KEY2, Constants.MARKET_EXTRA_PAY_VALUE);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                        startActivity(mainIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        Intent intent = new Intent();
                        ComponentName cn = new ComponentName(Constants.APP_PAY_PACKAGENAME,Constants.APP_PAY_CLASSNAME);
                        intent.setComponent(cn);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext,"系统未检测到可正常使用的支付宝应用!",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean showPayLink(){
        boolean alipay_available = appAvailable(mContext,Constants.APP_PAY_PACKAGENAME);
        boolean market_available = appAvailable(mContext,Constants.APP_MARKET_PACKAGENAME);

        if (!alipay_available && !market_available) {
            return false;
        } else {
            return true;
        }
    }


    private boolean  appAvailable(Context context, String packagename){
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        }catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if(packageInfo ==null){
            return false;
        }else{
            return true;
        }
    }

    private void showDialogForDownLoad() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.not_found_app_message_title));
        builder.setMessage(getActivity().getApplicationContext().getString(R.string.not_found_pay));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(getActivity().getApplicationContext().getString(R.string.download), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent mainIntent = new Intent(Constants.MARKET_SEARCH_ACTION, null);
                mainIntent.putExtra(Constants.MARKET_PAY_EXTRA_KEY1, Constants.MARKET_EXTRA_PAY_VALUE);
                mainIntent.putExtra(Constants.MARKET_PAY_EXTRA_KEY2, Constants.MARKET_EXTRA_PAY_VALUE);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(mainIntent);
            }
        });
        builder.show();
    }

}
