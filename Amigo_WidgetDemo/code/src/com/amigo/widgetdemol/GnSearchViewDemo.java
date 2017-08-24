package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.widget.AmigoSearchView;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class GnSearchViewDemo extends AmigoActivity {
    LinearLayout mLayout;
    AmigoSearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_search_view_demo);

        mLayout = (LinearLayout) findViewById(R.id.layout);
        mLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AmigoSearchView searchView = new AmigoSearchView(GnSearchViewDemo.this);
                mLayout.addView(searchView, new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                searchView.requestFocus();
            }
        });
    }
}
