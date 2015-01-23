package com.example.robertopalamaro.projectmobile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by robertopalamaro on 13/11/14.
 */
public class CacheListWithAdapters extends ArrayAdapter {

    private int resource;
    private LayoutInflater inflater;
    private Context context;
    public final static String FILE_NAME = "favourite.txt";
    public final static String separator = System.getProperty("line.separator");
    private File file;



    public CacheListWithAdapters ( Context ctx, int resourceId, List objects) {
        super( ctx, resourceId, objects );
        resource = resourceId;
        inflater = LayoutInflater.from( ctx );
        context=ctx;
        file = new File(ctx.getFilesDir(), FILE_NAME);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Hotel_Position hotel_position = (Hotel_Position)getItem(position);
        PositionListViewCache positionListViewCache;
        if (convertView == null){
            convertView = (RelativeLayout)inflater.inflate(resource,null);
            positionListViewCache = new PositionListViewCache(convertView);
            convertView.setTag(positionListViewCache);

        }
        else{
            convertView = (RelativeLayout)inflater.inflate(resource,null);
            positionListViewCache = new PositionListViewCache(convertView);
        }
        final TextView txtName = positionListViewCache.getHotelName(resource);
        TextView txtDistance = positionListViewCache.getDistance(resource);
        Button searchButton = positionListViewCache.getSearchButton(resource);
        final Button favouriteButton = positionListViewCache.getFavouriteButton(resource);
        txtName.setText("Hotel Name: "+hotel_position.getHotel_name());
        txtDistance.setText("Distance: "+new BigDecimal(hotel_position.getDistance()).setScale(4,BigDecimal.ROUND_HALF_UP)+" Mt");
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                PackageManager manager = getContext().getPackageManager();
                try {
                    i = manager.getLaunchIntentForPackage("com.android.chrome");
                    if (i == null)
                        throw new PackageManager.NameNotFoundException();
                    //i.addCategory(Intent.CATEGORY_LAUNCHER);
                    //i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(Uri.parse("http://www.google.it/?gws_rd=ssl#q=hotel+maikol"));
                    getContext().startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.println(Log.INFO,"PACKAGE","Not found bookinh");
                }
            }
        });
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO,"Row element","You have clicked favourite for this "+txtName.getText());
                favouriteButton.setBackgroundColor(Color.RED);
                try {
                    FileWriter fileWriter = new FileWriter(file,true);
                    //FileOutputStream outputStream;
                    //outputStream = context.openFileOutput(FILE_NAME,Context.MODE_PRIVATE);//getAppopenFileOutput(filename, Context.MODE_PRIVATE);
                    //OutputStreamWriter out = new OutputStreamWriter(fileWriter);
                    //out.append(txtName.getText());
                    //out.append(separator);
                    //out.flush();
                    //out.close();
                    //outputStream.close();
                    fileWriter.write(txtName.getText()+"\n");
                    fileWriter.close();
                } catch (Exception e) {
                    Log.println(Log.INFO,"List_Adapters","Error when write "+e.getLocalizedMessage());
                }


            }
        });
        return convertView;
    }
}
