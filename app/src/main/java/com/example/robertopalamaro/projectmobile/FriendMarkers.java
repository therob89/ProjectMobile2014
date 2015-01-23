package com.example.robertopalamaro.projectmobile;

import android.graphics.Bitmap;


/**
 * Created by robertopalamaro on 03/12/14.
 */
public class FriendMarkers {

    String text;
    Double latitude;
    Double longitude;
    Bitmap icon;

    public FriendMarkers(String a, double b,double c, Bitmap d){
        text=a;
        latitude = new Double(b);
        longitude = new Double(c);
        icon = d;
    }
    public void setText(String text) {
        this.text = text;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getText() {
        return text;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
