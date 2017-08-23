package com.gionee.secretary.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.R;
import com.gionee.secretary.module.settings.SettingModel;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.LogUtils;

import java.util.Arrays;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoEditText;


/**
 * Created by luorw on 12/17/16.
 */
public class AddressRemarkActivity extends PasswordBaseActivity {
    private EditText mAddressText;
    private String[] mTravelModeAdapter;
    private int mTravelType = 0;
    private TextView mTravel;
    private AmigoEditText mAddressRemark;
    private RelativeLayout mTravelModeLayout;
    private ImageButton mClearEditTextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_remark);
        initActionBar();
        initView();
        initData();
        parseIntent();
    }

    private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
            }
        });
        View actionBarLayout = getLayoutInflater().inflate(
                R.layout.actionbar_address_remark, null);
        ImageView btn_back = (ImageView) actionBarLayout.findViewById(R.id.btn_back);
        DisplayUtils.setBackIcon(btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
            }
        });
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.MATCH_PARENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mActionBar.setCustomView(actionBarLayout, param);
        mActionBar.show();
    }

    private void saveAddress() {
        saveTravel();
        try {
            Intent intent = new Intent();
            intent.putExtra(Constants.ADDRESS, mAddressText.getText().toString());
            intent.putExtra(Constants.ADDRESS_REMARK, mAddressRemark.getText().toString());
            intent.putExtra(Constants.TRAVEL_MODE, mTravel.getText().toString());
            intent.setAction(Constants.LAUNCH_FROM_ADDRESS_ACTION);
            intent.setClass(this, SelfCreateScheduleActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("AddressRemarkActivity", "e = " + e.getMessage().toString());
        }
        finish();
    }

    private void saveTravel() {
        SettingModel.getInstance(this).saveSelfScheduleTravel(mTravelType);
    }

    private void deleteAddress() {
        Intent intent = new Intent();
        intent.putExtra(Constants.DELETE_ADDRESS, true);
        intent.setAction(Constants.LAUNCH_FROM_ADDRESS_ACTION);
        intent.setClass(this, SelfCreateScheduleActivity.class);
        startActivity(intent);
        finish();
    }

    private void modifyAddress() {
        Intent intent = new Intent();
        intent.putExtra(Constants.MODIFY_ADDRESS, true);
        intent.setAction(Constants.LAUNCH_FROM_ADDRESS_REMARK_ACTION);
        intent.setClass(this, AddAddressActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        mAddressText = (EditText) findViewById(R.id.address_text);
        mTravel = (TextView) findViewById(R.id.travel_type);
        mTravelModeLayout = (RelativeLayout) findViewById(R.id.travel_mode_layout);
        mTravelModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTravelModeDialog();
            }
        });
        mAddressRemark = (AmigoEditText) findViewById(R.id.address_remark);
        //added by luorw for #72112 2017-03-14 begin
        mAddressRemark.addTextChangedListener(new MyTextWatcher(mAddressRemark, 200));
        //added by luorw for #72112 2017-03-14 end
        mAddressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyAddress();
            }
        });
        mAddressText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    mClearEditTextButton.setVisibility(View.VISIBLE);
                } else {
                    mClearEditTextButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mClearEditTextButton = (ImageButton) findViewById(R.id.clearButton);
        mClearEditTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddressText.setText("");
            }
        });
    }

    //added by luorw for GNSPR #72856 2017-03-17 begin
    private class MyTextWatcher implements TextWatcher {
        private int mMaxLength = 0;
        private AmigoEditText mEditText = null;
        private int start;
        private int count;

        public MyTextWatcher(AmigoEditText editText, int maxLength) {
            mEditText = editText;
            mMaxLength = maxLength;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            this.start = start;
            this.count = count;
            LogUtils.i("AddressRemark", "onTextChanged....." + s.toString() + ",length:" + s.toString().length() + "  ,start:" + start + ",   before:" + before + "  ,count:" + count);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            LogUtils.i("AddressRemark", "beforeTextChanged:" + s.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            int length = editable.toString().length();
            int beforeLength = length - count;
            int leftInput = mMaxLength - beforeLength;
            int delStart = start + leftInput;
            int delEnd = start + count;
            if (length > mMaxLength) {
                Toast.makeText(AddressRemarkActivity.this, getResources().getString(R.string.texttoolong), Toast.LENGTH_SHORT).show();
                editable.delete(delStart, delEnd);
                mEditText.setText(editable);
                mEditText.setSelection(delStart);
            }
        }
    }

    //added by luorw for GNSPR #72856 2017-03-17 end
    private void initData() {
        mTravelModeAdapter = getResources().getStringArray(R.array.travel_method_entry);
    }

    private void parseIntent() {
        Bundle bundle = getIntent().getExtras();
        String address = bundle.getString(Constants.ADDRESS);
        String addressRemark = bundle.getString(Constants.ADDRESS_REMARK);
        String tripMode = bundle.getString(Constants.TRAVEL_MODE);
        mAddressText.setText(address);
        mAddressRemark.setText(addressRemark);
        if (!TextUtils.isEmpty(tripMode)) {
            mTravelType = Arrays.asList(mTravelModeAdapter).indexOf(tripMode);
        } else {
            mTravelType = Integer.parseInt(SettingModel.getInstance(this).getDefaultTravelMethod());
        }
        mTravel.setText(mTravelModeAdapter[mTravelType]);
    }

    private void showTravelModeDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.travel_type));
        builder.setSingleChoiceItems(mTravelModeAdapter, mTravelType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTravel.setText(mTravelModeAdapter[which]);
                mTravelType = which;
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveAddress();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_address_menu, menu);
        MenuItem menuDelete = menu.findItem(R.id.delete_address);
        if(DisplayUtils.isFullScreen()){
            //全面屏
            menuDelete.setIcon(R.drawable.gn_private_delete_enable_full);
        }else {
            menuDelete.setIcon(R.drawable.gn_delete_enable);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_address:
                deleteAddress();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
