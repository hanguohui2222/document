package com.amigo.widgetdemol;

import com.demo.amigoactionbar.GnActionBarOvlayDemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GNBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent overlayIntent = new Intent(context, GnActionBarOvlayDemo.class);
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(overlayIntent);
    }

}
