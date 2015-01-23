package com.example.robertopalamaro.projectmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

/**
 * Created by robertopalamaro on 14/11/14.
 */
public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater mInflater;

    public CustomInfoWindow(LayoutInflater inflater){
        this.mInflater=inflater;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View popup = mInflater.inflate(R.layout.infoview,null);
        TextView text1 = (TextView)popup.findViewById(R.id.intestation);
        text1.setText(marker.getTitle());
        TextView text2 = (TextView)popup.findViewById(R.id.subtitle);
        text2.setText(marker.getSnippet());
        ImageView imageView =(ImageView)popup.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_directions_black);
        return popup;

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
