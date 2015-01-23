package com.example.robertopalamaro.projectmobile;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class MyMapFragment extends Fragment implements  GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener,GoogleMap.OnInfoWindowClickListener{

    private LocationClient mLocationClient;
    private GoogleMap googleMap;
    private Location mcurrentLocation;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int TOP_N_ELEMENTS = 30;
    private LocationListener locationListener;
    private static final String TAG = "Broadcast_Updating";
    private Intent intent;
    private LocationManager locationManager;
    onUpdatePositionListener onUpdatePositionListener;
    private Timer timer = null;
    private TaskToGetActivityPos taskToGetActivityPos;
    private HashMap<String,Marker> markerOnMap = new HashMap<String, Marker>();



    //final Messenger mMessenger = new Messenger(new IncomingHandler());

    public MyMapFragment(){}



    public interface onUpdatePositionListener{
        public void userPositionUpdate(Location location);
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(getActivity(),LocationService.class);
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                onUpdatePositionListener.userPositionUpdate(location);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater,container,savedInstanceState);
        View rootView = inflater.inflate(R.layout.map_layout,container,false);
        googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setInfoWindowAdapter(new CustomInfoWindow(inflater));
        //getActivity().startService(intent);
        //Bundle bundle_of_positions = getArguments();
        if(!isNetworkOnline())
        {
            Toast.makeText(getActivity(),"Set on network and position",Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        mLocationClient.connect();
        HashMap<String,String>pos = (HashMap<String,String>)getArguments().get("maps");
        Log.println(Log.INFO,"MAP_FRAGMENT","Taking started position and adding to map");
        if (pos!=null) {
            for (String k : pos.keySet()) {
                MarkerOptions markerOptions = stringToMarker(k, pos.get(k));
                markerOnMap.put(k,(googleMap.addMarker(markerOptions)));
            }
        }
        Log.println(Log.INFO,"MAP_FRAGMENT","Start timer and timer task");
        if (timer == null){
            timer = new Timer();
        }
        return rootView;

    }

    @Override
    public void onDestroy() {
        Log.println(Log.INFO,"MAP_FRAGMENT","Called on destroy");
        if(mLocationClient.isConnected()){
            mLocationClient.disconnect();

        }

        Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();

        super.onDestroy();


    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //getActivity().stopService(intent);
    }
    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
        Log.println(Log.INFO,"Map ","Connected");
        mcurrentLocation = mLocationClient.getLastLocation();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mcurrentLocation.getLatitude(), mcurrentLocation.getLongitude()),16);
        googleMap.animateCamera(cameraUpdate);
        googleMap.setOnInfoWindowClickListener(this);
        new LoadDataAsyncThread(getActivity()).execute(getResources().openRawResource(R.raw.strutturericettivewithcoordinate1),getResources().openRawResource(R.raw.strutturericettivewithcoordinate2)
                ,getResources().openRawResource(R.raw.strutturericettivewithcoordinate3));
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        if(timer != null){
            Log.println(Log.INFO,"Map ","Start position acquiring from activity");
            // add timer
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    // Get new user periodically
                    taskToGetActivityPos = new TaskToGetActivityPos();
                    taskToGetActivityPos.execute(markerOnMap);
                }
            },30000,120000L);
        }
    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getActivity(),"Clicked on info marker"+marker.getTitle(),Toast.LENGTH_SHORT).show();
        Log.println(Log.INFO,"onInfoWindow","***************** On info window");
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr="+mcurrentLocation.getLatitude()+","+mcurrentLocation.getLongitude()+"&daddr="+marker.getPosition().latitude+","+marker.getPosition().longitude));
        startActivity(intent);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.println(Log.DEBUG, "MyApp", String.valueOf(connectionResult.getErrorCode()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.println(Log.DEBUG,"Fragment-Lifecycle","onPause");
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        MapFragment fragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
        if (timer !=null) {
            timer.cancel();
        }
        if (taskToGetActivityPos!=null) {
            taskToGetActivityPos.cancel(true);
        }

    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        Log.println(Log.DEBUG,"Fragment-Lifecycle","onStop");
        mLocationClient.disconnect();
        locationManager.removeUpdates(locationListener);
        super.onStop();


    }

    @Override
    public void onStart() {
        super.onStart();

    }
    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLocationClient = new LocationClient(getActivity(), this, this);
        mLocationClient.connect();
        try {
            onUpdatePositionListener = (onUpdatePositionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onUpdatePosition");
        }


    }

    public class LoadDataAsyncThread extends AsyncTask<InputStream,Integer,TreeMap<Double,Address>> {

        private TreeMap<Double,Address> hotel_coordinates;
        Context mContext;

        public LoadDataAsyncThread(Context c){
            super();
            mContext=c;
            hotel_coordinates = new TreeMap<Double, Address>();
        }
        @Override
        protected TreeMap<Double, Address> doInBackground(InputStream... params) {
            for (InputStream input : params) {
                BufferedReader stream = new BufferedReader(new InputStreamReader(input));
                mcurrentLocation = mLocationClient.getLastLocation();
                Location temp = new Location("myLocation");
                String[] tokens;
                String line;
                Location loc;
                double tempDistance = 0;
                Address myAddress;
                int z = 0;
                try {
                    while ((line = stream.readLine()) != null) {
                        tokens = line.split(":");
                        String name = tokens[0];
                        String category = tokens[2];
                        double latitude = Double.valueOf(tokens[3]);
                        double longitude = Double.valueOf(tokens[4]);
                        temp.setLatitude(latitude);
                        temp.setLongitude(longitude);
                        z++;
                        tempDistance = mcurrentLocation.distanceTo(temp);
                        if (tempDistance < 30000) {
                            myAddress = new Address(Locale.getDefault());
                            myAddress.setLatitude(latitude);
                            myAddress.setLongitude(longitude);
                            myAddress.setFeatureName(name + ":" + category);
                            hotel_coordinates.put(tempDistance, myAddress);
                        }

                    }

                } catch (FileNotFoundException e) {
                    Log.println(Log.DEBUG, "THREAD", "FILe not found!!");
                    cancel(true);

                } catch (IOException e) {
                    Log.println(Log.DEBUG, "THREAD", "IO exception!!");
                    cancel(true);
                }
            }
            return hotel_coordinates;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.println(Log.DEBUG,"ASYNC","***************** 50 done");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hotel_coordinates = new TreeMap<Double, Address>();

        }

        @Override
        protected void onPostExecute(TreeMap<Double, Address> locationFloatTreeMap) {
            super.onPostExecute(locationFloatTreeMap);
            MainActivity ma = (MainActivity)getActivity();
            Log.println(Log.INFO,"ASYNC"," On Post Execute");
            int count =0;
            for (Map.Entry<Double,Address> entry:hotel_coordinates.entrySet()){
                if (count>=TOP_N_ELEMENTS){
                    break;
                }
                MarkerOptions mko = new MarkerOptions();
                Address t = (Address)entry.getValue();
                String []tks =t.getFeatureName().split(":");
                mko.title(tks[0]);
                mko.position(new LatLng(t.getLatitude(),t.getLongitude()));
                mko.snippet("Category:"+tks[1]+" stars");
                googleMap.addMarker(mko);
                ma.addMarker(entry.getKey(),mko);
                count++;
            }

        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        Log.println(Log.INFO,"PACKAGE","Config changed");

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT).show();
        }


    }


    private class TaskToGetActivityPos extends AsyncTask<HashMap<String,Marker>,MarkerOptions,HashMap<String,MarkerOptions>>{
        @Override
        protected HashMap<String,MarkerOptions> doInBackground(HashMap<String,Marker>...params) {
            Log.println(Log.INFO, "Map_Task ", "Task Get Pos started!!!");
            HashMap<String,Marker>markerOnMap = (HashMap<String,Marker>)params[0].clone();
            HashMap<String,MarkerOptions>markerToReturn = new HashMap<String, MarkerOptions>();
            MainActivity mainActivity = (MainActivity)getActivity();
            if (mainActivity.position_received_from_service==null){
                Log.println(Log.INFO, "Map_Task ", "No user on map!!!");
                return null;
            }
            Log.println(Log.INFO, "Map_Task ", "size of position of activity is!!!"+mainActivity.attualUserDownloaded().size());
            HashMap<String,String> positionFromActivity = mainActivity.attualUserDownloaded();
            for (String k : positionFromActivity.keySet()){
                if (markerOnMap.containsKey(k)){
                    String [] elements = positionFromActivity.get(k).split("::");
                    LatLng latLng = new LatLng(Double.parseDouble(elements[0]),Double.parseDouble(elements[1]));
                    this.publishProgress(stringToMarker(k,positionFromActivity.get(k)));
                }
                else{
                    markerToReturn.put(k,stringToMarker(k,positionFromActivity.get(k)));
                }
            }

            Log.println(Log.INFO, "Map_Task ", "Task Get Pos ended!!!");
            return markerToReturn;
        }

        @Override
        protected void onProgressUpdate(MarkerOptions... values) {
            Log.println(Log.INFO, "Map_Task ", "Update position!!!");
            super.onProgressUpdate(values);
            MarkerOptions mo = values[0];
            Marker temp = markerOnMap.get(mo.getTitle());
            if (mo.getPosition().latitude!=temp.getPosition().latitude ||mo.getPosition().longitude!=temp.getPosition().longitude) {
                temp.remove();

            }
            markerOnMap.put(mo.getTitle(),googleMap.addMarker(mo));
        }

        @Override
        protected void onPostExecute(HashMap<String, MarkerOptions> stringMarkerOptionsHashMap) {
            super.onPostExecute(stringMarkerOptionsHashMap);
            if (stringMarkerOptionsHashMap==null){
                return;
            }
            Log.println(Log.INFO, "Map_Task ", "We have "+stringMarkerOptionsHashMap.size()+" new users");
            for (String k : stringMarkerOptionsHashMap.keySet()){
               markerOnMap.put(k,googleMap.addMarker(stringMarkerOptionsHashMap.get(k)));
            }
        }
    }

    private MarkerOptions stringToMarker(String user,String lat_lng_avatar){

        Log.println(Log.INFO,"Map_Method ","Draw a new position");
        String [] elements = lat_lng_avatar.split("::");
        LatLng latLng = new LatLng(Double.parseDouble(elements[0]),Double.parseDouble(elements[1]));
        byte[] decodedString = Base64.decode(elements[2], Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(60, 80, conf);
        Canvas canvas1 = new Canvas(bmp);
        // paint defines the text color,
        // stroke width, size
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.marker_on_map,null);
        layout.setDrawingCacheEnabled(true);
        ((ImageView) layout.findViewById(R.id.imageMarker)).setImageBitmap(decodedByte);
        //((TextView) layout.findViewById(R.id.textMarker)).setText(user);

        layout.measure(View.MeasureSpec.makeMeasureSpec(canvas1.getWidth(),View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(canvas1.getHeight(),View.MeasureSpec.EXACTLY));
        layout.layout(0, 0, layout.getMeasuredWidth(), layout.getMeasuredHeight());
        //color.setTextSize(35);
        //modify canvas
        canvas1.drawBitmap(layout.getDrawingCache(),0,0, new Paint());
        //add marker to Mar
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
                        // Specifies the anchor to be at a particular point in the marker image.
         markerOptions.anchor(0.5f, 1);
         markerOptions.title(user);
        return markerOptions;

    }

}
