package com.gionee.secretary.widget;

import com.gionee.secretary.module.SoundPlayer;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.ui.activity.NoteDetailActivity;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.TextUtilTools;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

public class SoundsClickableSpan extends ClickableSpan {
    private Context mContext;
    private String path;
    private SoundPlayer mSoundPlayer;
    Activity mActivity;
    public static final int MIN_CLICK_DELAY_TIME = 500;
    private long lastClickTime = 0;

    public SoundsClickableSpan(Activity activity, String path) {
        super();
        this.mContext = (Context) activity;
        mActivity = activity;
        this.path = path;
    }

    @Override
    public void onClick(View arg0) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick();
        }
    }

    private void onNoDoubleClick(){
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(path));
        if (mediaPlayer == null) {
            Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        int time = VoiceNoteDao.getInstance(mContext.getApplicationContext()).getRecordBean(path).getTime();
        // int time = mediaPlayer.getDuration()/1000;
        String timestr = TextUtilTools.formatTime(time, ":");
        mSoundPlayer = new SoundPlayer(mContext, path);
        mSoundPlayer.showDialog(timestr);
        mSoundPlayer.startPlay();
        if(mActivity instanceof NoteDetailActivity){
            NoteDetailActivity noteDetailActivity = (NoteDetailActivity)mActivity;
            noteDetailActivity.setOnActivityStateListener(new NoteDetailActivity.OnActivityStateListener() {
                @Override
                public void onDestroy() {
                    LogUtils.i("SoundPlayer" , "NoteDetailActivity , onDestroy");
                    mSoundPlayer.completePlayer();
                }
            });
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        LogUtils.d("liyu", "updateDrawState ");
        LogUtils.d("liyu", "ds.bgColor = " + ds.bgColor);
        LogUtils.d("liyu", "ds.getColor() = " + ds.getColor());
        ds.setUnderlineText(false);
        ds.clearShadowLayer();
    }
}
