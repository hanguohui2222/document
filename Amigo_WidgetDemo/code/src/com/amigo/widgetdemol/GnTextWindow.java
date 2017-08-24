package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class GnTextWindow extends AmigoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_text_window);
        initWhiteTextView();
        initBlackTextView();
        initHintTextView();
        
        initAnimationView();
    }
    
    private void initWhiteTextView() {
        TextView firstTextView = (TextView) this.findViewById(R.id.first_white_text);
        firstTextView.setText("第一种字体");
        TextView secondTextView = (TextView) this.findViewById(R.id.second_white_text);
        secondTextView.setText("第二种字体");
        TextView threeTextView = (TextView) this.findViewById(R.id.three_white_text);
        threeTextView.setText("第三种字体");
        TextView fourTextView = (TextView) this.findViewById(R.id.four_white_text);
        fourTextView.setText("提示字体");
        
        TextView firstTextView2 = (TextView) this.findViewById(R.id.first_white_text2);
        firstTextView2.setText("第一种字体");
        TextView secondTextView2 = (TextView) this.findViewById(R.id.second_white_text2);
        secondTextView2.setText("第二种字体");
        TextView threeTextView2 = (TextView) this.findViewById(R.id.three_white_text2);
        threeTextView2.setText("第三种字体");
        TextView fourTextView2 = (TextView) this.findViewById(R.id.four_white_text2);
        fourTextView2.setText("提示字体");        
    }
    
    private void initBlackTextView() {
        TextView firstTextView = (TextView) this.findViewById(R.id.first_black_text);
        firstTextView.setText("第一种字体");
        TextView secondTextView = (TextView) this.findViewById(R.id.second_black_text);
        secondTextView.setText("第二种字体");
        TextView threeTextView = (TextView) this.findViewById(R.id.three_black_text);
        threeTextView.setText("第三种字体");
        TextView fourTextView = (TextView) this.findViewById(R.id.four_black_text);
        fourTextView.setText("提示字体");
        
        TextView firstTextView2 = (TextView) this.findViewById(R.id.first_black_text2);
        firstTextView2.setText("第一种字体");
        TextView secondTextView2 = (TextView) this.findViewById(R.id.second_black_text2);
        secondTextView2.setText("第二种字体");
        TextView threeTextView2 = (TextView) this.findViewById(R.id.three_black_text2);
        threeTextView2.setText("第三种字体");
        TextView fourTextView2 = (TextView) this.findViewById(R.id.four_black_text2);
        fourTextView2.setText("提示字体");        
    }
    
    private void initHintTextView() {
        TextView hintTextView = (TextView) this.findViewById(R.id.hint_text_1);
        hintTextView.setText("1. 系统中用到的字体大小及颜色梯度见上");
        
        TextView hintTextView2 = (TextView) this.findViewById(R.id.hint_text_2);
        hintTextView2.setText("2. 文字选中反色可参考Demo以及手机中任何文字选中的地方，比如Demo中的列表以及对话框的Button");        
    }
    
    private void initAnimationView() {
        final TextView unread = (TextView) this.findViewById(R.id.gn_unread_view);
        final TextView unread2 = (TextView) this.findViewById(R.id.gn_unread_view2);

        unread.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                addAnimation(unread);
                unread.startAnimation(mAnimationSet);
            }
        });
        
        unread2.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                addAnimation(unread2);
                unread2.startAnimation(mAnimationSet);
            }
        });
    }
    
    private AnimationSet mAnimationSet;
    private void addAnimation(final View view) {
        mAnimationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAnimationSet.addAnimation(scaleAnimation);
        mAnimationSet.addAnimation(alphaAnimation);
        mAnimationSet.setDuration(400);
        mAnimationSet.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                view.setVisibility(View.GONE);
            }
        });
    }
}
