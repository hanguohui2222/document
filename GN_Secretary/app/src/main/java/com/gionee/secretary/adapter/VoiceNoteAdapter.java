package com.gionee.secretary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.LoggingEventHandler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.secretary.R;

import com.gionee.secretary.bean.VoiceNoteBean;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.TextUtilTools;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amigoui.widget.AmigoTextView;

/**
 * Created by hangh on 6/4/16.
 */
public class VoiceNoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<VoiceNoteBean> voiceNoteList;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    public static final String JPG = "jpg";
    public static final String PNG = "png";
    public static final String AMR = "amr";

    public VoiceNoteAdapter(Context context, List<VoiceNoteBean> list) {
        mContext = context;
        voiceNoteList = list;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, final int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_voice_note_item, viewGroup, false);
        ItemViewHolder vh = new ItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            VoiceNoteBean bean = voiceNoteList.get(position);
            if (bean == null) {
                return;
            }
            /*
            modify by zhengjl at 2017-1-17
            修改备忘列表显示出路径的问题
             */
            Date date = new Date(bean.getCreateTime());
            String creatTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
            ((ItemViewHolder) viewHolder).tvDate.setText(creatTime);
            ((ItemViewHolder) viewHolder).tvTitle.setText(bean.getTitle());
            String content = bean.getContent();
            if (content != null) {
                int type = bean.getAttachmentType();
                switch (type) {
                    case 0:
                        ((ItemViewHolder) viewHolder).imgAttach.setVisibility(View.GONE);
                        break;
                    case 1:
                        ((ItemViewHolder) viewHolder).imgAttach.setVisibility(View.VISIBLE);
                        ((ItemViewHolder) viewHolder).imgAttach.setImageDrawable(mContext.getResources().getDrawable(R.drawable.attachment_icon));
                        break;
                    case 2:
                        ((ItemViewHolder) viewHolder).imgAttach.setVisibility(View.VISIBLE);
                        ((ItemViewHolder) viewHolder).imgAttach.setImageDrawable(mContext.getResources().getDrawable(R.drawable.picture_icon));
                        break;
                    case 3:
                        ((ItemViewHolder) viewHolder).imgAttach.setVisibility(View.VISIBLE);
                        ((ItemViewHolder) viewHolder).imgAttach.setImageDrawable(mContext.getResources().getDrawable(R.drawable.note_record_icon));
                        break;
                }
            } else {
                //只有提醒的备忘
                ((ItemViewHolder) viewHolder).tvContent.setVisibility(View.GONE);
                ((ItemViewHolder) viewHolder).imgAttach.setVisibility(View.GONE);
            }
            //add by zhengjl at 2017-1-20 for  GNSPR #65717 begin
            if (bean.getRemindDate() > 0) {
                Drawable right_draw = mContext.getResources().getDrawable(R.drawable.remind_icon_gray);
                right_draw.setBounds(0, 0, right_draw.getMinimumWidth(), right_draw.getMinimumHeight());
                ((ItemViewHolder) viewHolder).tvTitle.setCompoundDrawables(null, null, right_draw, null);
            } else {
                ((ItemViewHolder) viewHolder).tvTitle.setCompoundDrawables(null, null, null, null);
            }
            //add by zhengjl at 2017-1-20 for  GNSPR #65717 end


        }
    }

    /**
     * add by zhengjl at 2017-1-18
     * 提取内容
     * 有换行，取换行后面的为内容
     * 没有换行，20个字后面的为内容
     *
     * @param content
     * @return
     */
    private String getContentFromContent(String content) {
//        LogUtils.e("zjl","content:" + content);
        if (!TextUtils.isEmpty(content)) {

            int index = 0;
            int end = content.length() - 1;
            while (index < content.length() && content.charAt(index) == '\n') {
                index++;
            }
            //去除文本后的换行符
            while (end > 0 && end <= content.length() - 1 && content.charAt(end) == '\n') {
                end--;
            }
            if (index >= end) return null;
            content = content.substring(index, end + 1);
//            LogUtils.e("zjl","去掉换行符--content:" + content);
            index = content.indexOf("\n");

            if (index > 0) {
                if (index < content.length() - 1) {
//                    LogUtils.e("zjl","...after..." + content.substring(index + 1, content.length()));
                    return content.substring(index + 1, content.length());
                } else {
                    return content.substring(0, index + 1).length() > 20 ? content.substring(21, content.length()) : null;
                }
            } else {
                return content.length() > 20 ? content.substring(21, content.length()) : null;
            }
        } else
            return null;
    }

    @Override
    public int getItemCount() {
        if (voiceNoteList == null) {
            return 0;
        }
        return voiceNoteList.size();
    }

    public VoiceNoteBean getItem(int position) {
        if (voiceNoteList != null && position < voiceNoteList.size()) {
            return voiceNoteList.get(position);
        } else {
            return null;
        }
    }

    public void setVoiceNoteList(List<VoiceNoteBean> voiceNoteList) {
        if (null != voiceNoteList) {
            this.voiceNoteList = voiceNoteList;
            notifyDataSetChanged();
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AmigoTextView tvDate;
        AmigoTextView tvTitle;
        AmigoTextView tvContent;
        ImageView imgAttach;
        ImageView imgRemind;

        public ItemViewHolder(View v) {
            super(v);
            tvDate = (AmigoTextView) v.findViewById(R.id.note_date);
            tvTitle = (AmigoTextView) v.findViewById(R.id.note_title);
            tvContent = (AmigoTextView) v.findViewById(R.id.note_content);
            imgAttach = (ImageView) v.findViewById(R.id.attach_img);
            imgRemind = (ImageView) v.findViewById(R.id.remind_img);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, this.getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(v, this.getPosition());
            }
            return true;
        }
    }

}
