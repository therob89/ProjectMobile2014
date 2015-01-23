package com.example.robertopalamaro.projectmobile;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedOutputStream;

/**
 * Created by robertopalamaro on 13/11/14.
 */
public class PositionListViewCache {

    private View baseView;
    private TextView hotel_name;
    private TextView distance;
    private Button searchButton;
    private Button favouriteButton;

    public PositionListViewCache(View view){
        this.baseView=view;
    }

    public View getViewBase(){
        return baseView;
    }

    public TextView getHotelName (int resource) {
        if ( hotel_name == null ) {
            hotel_name = ( TextView ) baseView.findViewById(R.id.Hotel_name);
        }
        return hotel_name;
    }

    public TextView getDistance (int resource) {
        if ( distance == null ) {
            distance = ( TextView ) baseView.findViewById(R.id.Distance);
        }
        return distance;
    }

    public void setDistance(TextView distance) {
        this.distance = distance;
    }

    public void setHotel_name(TextView hotel_name) {
        this.hotel_name = hotel_name;
    }

    public void setSearchButton(Button button){this.searchButton =button;}

    public void setFavouriteButton(Button button){this.favouriteButton=button;}

    public Button getSearchButton(int resource) {
        if(searchButton ==null) {
            searchButton = (Button)baseView.findViewById(R.id.buttonSearch);
        }
        return searchButton;
    }

    public Button getFavouriteButton(int resource) {
        if(favouriteButton ==null) {
            favouriteButton = (Button)baseView.findViewById(R.id.buttonFavourite);
        }
        return favouriteButton;
    }
}
