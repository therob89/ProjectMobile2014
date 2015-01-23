package com.example.robertopalamaro.projectmobile;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertopalamaro on 14/11/14.
 */
public class MyListFragment extends Fragment{

    private final static String MARKERS_STRING = "MARKERS_ON_MAP";
    private File file;
    private FileOutputStream fileOutputStream;
    public MyListFragment(){}



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View rootView = inflater.inflate(R.layout.my_list_main,container,false);
        ListView listView =(ListView)rootView.findViewById(R.id.listView);
        ArrayList<String> markerOnMap = getArguments().getStringArrayList(MARKERS_STRING);
        if (markerOnMap==null){
            Log.println(Log.INFO,"ListFragment","null passing array");
            return rootView;
        }
        List list = new ArrayList();
        for (String s: markerOnMap){
            String []tokens = s.split("::");
            list.add(new Hotel_Position(tokens[1],tokens[0]));
        }
        listView.setAdapter(new CacheListWithAdapters(getActivity(), R.layout.row,list));
        return rootView;
    }

}
