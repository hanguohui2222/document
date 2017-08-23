package com.gionee.secretary.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;

import java.io.File;


public class PictureClickableSpan extends ClickableSpan {
    private Context mContext;
    private String path;

    public PictureClickableSpan(Context mContext, String path) {
        super();
        this.mContext = mContext;
        this.path = path;
    }

    @Override
    public void onClick(View arg0) {
        try {
            String imagepath = null;
            if(path.startsWith(Constants.FILE_SCHEME + "/storage/emulated")){
                imagepath = path.substring(Constants.FILE_SCHEME.length());
                if(imagepath == null)
                    return;
                File file = new File(imagepath);
                Uri imageUri= FileProvider.getUriForFile(mContext,"com.gionee.secretary.fileprovider",file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(imageUri, "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
            } else {
                Uri uri = Uri.parse(path);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*");
                mContext.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
