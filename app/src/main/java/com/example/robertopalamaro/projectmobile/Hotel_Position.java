package com.example.robertopalamaro.projectmobile;

/**
 * Created by robertopalamaro on 13/11/14.
 */
public class Hotel_Position {

    private String hotel_name;
    private String distance;

    public Hotel_Position(String a,String b){
        hotel_name=a;
        distance=b;
    }
    public String getDistance() {
        return distance;
    }

    public String getHotel_name() {
        return hotel_name;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }
}
