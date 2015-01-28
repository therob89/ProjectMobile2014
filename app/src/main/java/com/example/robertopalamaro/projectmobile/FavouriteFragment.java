package com.example.robertopalamaro.projectmobile;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by robertopalamaro on 13/12/14.
 */
public class FavouriteFragment extends Fragment  implements SwipeListView.SwipeListViewCallback {
    public final static String FILE_NAME = "favourite.txt";
    private List<String> favouriteArray =null;
    private List<String> deletingList;
    private ListView listView;
    private MyAdapter m_Adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.favourite_fragment,container,false);
        listView = (ListView)rootView.findViewById(R.id.listFavourite);
        if (favouriteArray.size()==0){
            Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","Size of array is 0");
        }
        else{
            Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","Populating list");
            /*
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    favouriteArray);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            */
            SwipeListView l = new SwipeListView(getActivity(),this);
            l.exec();
            m_Adapter = new MyAdapter();
            m_Adapter.addItemAll(favouriteArray);
            listView.setAdapter(m_Adapter);


        }
        return rootView;

    }

    @Override
    public ListView getListView() {
        return listView;
    }

    @Override
    public void onSwipeItem(boolean isRight, int position) {
        Log.println(Log.INFO,"FAVOURITE_FRAGMENT","Swiping");

        m_Adapter.onSwipeItem(isRight,position);
    }

    @Override
    public void onItemClickListener(ListAdapter adapter, int position) {
        Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","onItemClick");
        //MyAdapter m = (MyAdapter)adapter;


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(favouriteArray==null){
            favouriteArray = new ArrayList<String>();
        }
        try {
            InputStream inputStream = getActivity().openFileInput(FILE_NAME);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    if(!receiveString.equals(CacheListWithAdapters.separator)) {
                        favouriteArray.add(receiveString);
                    }
                }
                Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","Size of array read is"+favouriteArray.size());
                bufferedReader.close();
                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","File not found");
        } catch (IOException e) {
            Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","IoException");

        }
    }

    public class MyAdapter extends BaseAdapter {

        protected List<String> m_List;
        private final int INVALID = -1;
        protected int DELETE_POS = -1;

        public MyAdapter() {
            // TODO Auto-generated constructor stub
            m_List = new ArrayList<String>();
        }

        public void addItem(String item) {
            //
            m_List.add(item);
            notifyDataSetChanged();
        }

        public void addItemAll(List<String> item) {
            //
            Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","Add all item");

            m_List.addAll(item);

            notifyDataSetChanged();
        }

        public void onSwipeItem(boolean isRight, int position) {
            // TODO Auto-generated method stub
            if (!isRight) {
                DELETE_POS = position;
            } else if (DELETE_POS == position) {
                DELETE_POS = INVALID;
            }
            //
            notifyDataSetChanged();
        }

        public void deleteItem(int pos) {
            //
            Log.println(Log.DEBUG,"Favourite_Fragment","Favourite position is"+pos);
            if(deletingList==null){
                deletingList = new LinkedList<String>();
            }
            deletingList.add(m_List.get(pos));
            m_List.remove(pos);
            DELETE_POS = INVALID;
            notifyDataSetChanged();
        }
        public List<String>getItems(){
            return m_List;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return m_List.size();
        }

        @Override
        public String getItem(int position) {
            // TODO Auto-generated method stub
            return m_List.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.item, null);
            }
            TextView text = ViewHolderPattern.get(convertView, R.id.text);
            Button delete = ViewHolderPattern.get(convertView, R.id.delete);
            if(text == null){
                Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","text is null");

            }
            if(delete == null){
                Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","button");

            }
            if (DELETE_POS == position) {
                //delete
                delete.setVisibility(View.VISIBLE);
            } else
               delete.setVisibility(View.INVISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    deleteItem(position);
                }
            });

            text.setText(getItem(position));

            return convertView;
        }
    }

    public static class ViewHolderPattern {
        // I added a generic return type to reduce the casting noise in client
        // code
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(deletingList!=null && !deletingList.isEmpty()){
            Log.println(Log.DEBUG,"FAVOURITE_FRAGMENT","Deleting list is not empty");
            try {
                File file = new File(getActivity().getFilesDir(), FILE_NAME);
                FileWriter fileWriter = new FileWriter(file,false);
                for (String s:m_Adapter.getItems()){
                    if (!deletingList.contains(s)){
                        fileWriter.write(s+"\n");

                    }
                }
                fileWriter.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
