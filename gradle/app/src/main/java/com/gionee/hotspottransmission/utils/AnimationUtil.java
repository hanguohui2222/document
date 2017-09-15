package com.gionee.hotspottransmission.utils;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {
    public final static int ANIMATION_IN_TIME = 500;
    public final static int ANIMATION_OUT_TIME = 500;

    public static Animation createInAnimation(Context context, int fromYDelta) {
        AnimationSet set = new AnimationSet(context, null);
        set.setFillAfter(true);
        TranslateAnimation animation = new TranslateAnimation(0, 0, fromYDelta, 0);
        set.addAnimation(animation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        set.addAnimation(alphaAnimation);
        set.setDuration(ANIMATION_OUT_TIME);
        return set;
    }

    public static Animation createOutAnimation(Context context, int toYDelta) {
        AnimationSet set = new AnimationSet(context, null);
        set.setFillAfter(true);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, toYDelta);
        set.addAnimation(animation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        set.addAnimation(alphaAnimation);
        set.setDuration(ANIMATION_OUT_TIME);
        return set;
    }
}
