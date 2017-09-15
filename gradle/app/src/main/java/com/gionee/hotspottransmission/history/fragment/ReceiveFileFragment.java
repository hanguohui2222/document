package com.gionee.hotspottransmission.history.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.adapter.ReceiveFileGridAdapter;
import com.gionee.hotspottransmission.history.view.FileRecordActivity;
import com.gionee.hotspottransmission.history.view.FileRecordActivity.FileReceiveListener;
import com.gionee.hotspottransmission.history.view.SpecReceivedFileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 收到的文件
 */
public class ReceiveFileFragment extends Fragment{

    private final String TAG = "ReceiveFileFragment";
    private List<Integer> countList = new ArrayList<>();

    public static ReceiveFileFragment newInstance() {
        ReceiveFileFragment pageFragment = new ReceiveFileFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receivefile, container, false);
        final GridView gridView = (GridView) view.findViewById(R.id.grid_record);

        ((FileRecordActivity)getActivity()).setFileReceiveListener(new FileReceiveListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void setFileCount(List<Map<String, List<FileInfo>>> list) {
                countList = setCountList(list);
                gridView.setAdapter(new ReceiveFileGridAdapter(getContext(),countList));
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), SpecReceivedFileActivity.class);
                switch (position) {
                    case 0:
                        intent.putExtra("type", Constants.TYPE_FILE);
                        startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("type", Constants.TYPE_APPS);
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("type", Constants.TYPE_IMAGE);
                        startActivity(intent);
                        break;
                    case 3:
                        intent.putExtra("type", Constants.TYPE_MUSIC);
                        startActivity(intent);
                        break;
                    case 4:
                        intent.putExtra("type", Constants.TYPE_VIDEO);
                        startActivity(intent);
                        break;
                }
            }
        });
        return view;
    }

    public List<Integer> setCountList(List<Map<String,List<FileInfo>>> list){
        List<Integer> countList = new ArrayList<>();
        countList.clear();
        for(Map<String,List<FileInfo>> fileInfoMap : list){
            int count = 0;
            for (String key : fileInfoMap.keySet()) {
                List<FileInfo> files = fileInfoMap.get(key);
                count += files.size();
            }
            countList.add(count);
        }
        return countList;
    }

}
