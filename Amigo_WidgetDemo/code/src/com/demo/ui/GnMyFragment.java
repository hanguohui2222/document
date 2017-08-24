package com.demo.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amigo.widgetdemol.R;

public class GnMyFragment extends Fragment {

    private static final String TEXT_CHAT = "CHAT";
    private static Context mContext ;
    

    public static GnMyFragment newInstance(String chat) {
        final GnMyFragment f = new GnMyFragment();
        final Bundle args = new Bundle();
        args.putString(TEXT_CHAT, chat);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gn_myfragment, container, false);
        TextView tv = (TextView) view.findViewById(R.id.tv_fragment_text);
        String str = getArguments() != null ? getArguments().getString(TEXT_CHAT) : null;
        if(str != null){
            tv.setText(str);
        }else{
            tv.setText("Get String Error !! Help Me!!!");
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getArguments().getString(TEXT_CHAT).equalsIgnoreCase("TAB1")) {
            inflater.inflate(R.menu.menu1, menu);
        } else if (getArguments().getString(TEXT_CHAT).equalsIgnoreCase("TAB2")) {
            inflater.inflate(R.menu.menu2, menu);
        }else {
            inflater.inflate(R.menu.menu3, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("Dazme", "onOptionsItemSelected-FragMent1");
        switch (item.getItemId()) {
        case R.id.title8:
            Intent intent = new Intent("com.demo.amigoactionbar.GNBroadcastReceiver");                 
            getActivity().sendBroadcast(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.i("Dazme", "onPrepareOptionsMenu-FragMent1");
        // TODO Auto-generated method stub
        getActivity().onPrepareOptionsMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }
    
    
}
