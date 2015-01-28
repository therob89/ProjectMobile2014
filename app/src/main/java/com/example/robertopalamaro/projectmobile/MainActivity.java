package com.example.robertopalamaro.projectmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.Inflater;


public class MainActivity extends Activity implements RegisterFragment.onLoginListener,MyMapFragment.onUpdatePositionListener {

    private String[] menuList;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CharSequence title;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private final static int MAP_FRAGMENT = 0;
    private final static int LIST_FRAGMENT = 1;
    private final static int FAVOURITE_FRAGMENT = 2;
    private final static int REGISTER_FRAGMENT = 3;

    private final static String TAG ="Broadcast message";
    private final static String MARKERS_STRING = "MARKERS_ON_MAP";
    private TreeMap<Double, MarkerOptions> markersOnMap;
    HashMap<String,String> position_received_from_service;
    private Intent service;
    private String username = "";
    private String password = "";
    private Messenger mService;
    private Handler handler = new IncomingHandler();
    final Messenger mMessenger = new Messenger(handler);


    private Bundle bundle_of_positions;
    private boolean mBound = false;
    private Location currentLocation;


    protected boolean isBinded(){
        return mBound;
    }

    protected HashMap<String,String> attualUserPositionsFromService(){
        return (HashMap<String,String> )position_received_from_service.clone();
    }
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LocationService.MSG_SET_VALUE:
                    Log.println(Log.DEBUG,"Activity-Handler-Mex","Received mex from service");
                    //Toast.makeText(getApplicationContext(),"Received from service: " + msg.arg1,Toast.LENGTH_SHORT).show();
                    bundle_of_positions = msg.getData();
                    if (position_received_from_service==null){
                        position_received_from_service = new HashMap<String, String>();
                    }
                    HashMap<String,String> bundle_map = (HashMap<String,String>)bundle_of_positions.getSerializable("maps");
                    Log.println(Log.DEBUG,"Activity-Handler-Mex","bundle map received from service has size "+bundle_map.size());
                    for (String key : bundle_map.keySet()){
                        if (!position_received_from_service.containsKey(key)){
                            Log.println(Log.DEBUG,"Activity-Handler-Mex","Adding new user");
                            position_received_from_service.put(key,bundle_map.get(key));
                        }
                        else{
                            if (!position_received_from_service.get(key).equals(bundle_map.get(key))){
                                Log.println(Log.DEBUG,"Activity-Handler-Mex","Update position of user");
                                position_received_from_service.remove(key);
                                position_received_from_service.put(key,bundle_map.get(key));
                            }
                        }
                    }
                    //this.removeMessages(LocationService.MSG_SET_VALUE);
                    //removeCallbacksAndMessages(null);
                    break;
                default:
                    super.handleMessage(msg);
                    break;

            }
        }
    }




    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            Log.println(Log.DEBUG,"Activity.Connection","Start connection with service");
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        LocationService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                // Give it some value as an example.

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.println(Log.DEBUG,"Activity.Connection","Service doesn't respond");

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        markersOnMap = new TreeMap<Double, MarkerOptions>();
        setContentView(R.layout.activity_main);
        title = getTitle();
        menuList = getResources().getStringArray(R.array.drawer_list);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuList));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.ic_drawer,R.string.drawer_open
        ,R.string.drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                try {
                    getActionBar().setTitle("Select Options");
                }catch (NullPointerException e){
                    Log.println(Log.DEBUG,"Activity","Action bar is null");
                }
                //invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setTitle(title);
                //invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        FragmentTransaction fragmentTransaction=getFragmentManager().beginTransaction();
        Fragment fragment = new RegisterFragment();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        service = new Intent(this,LocationService.class);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    @Override
    public void onLoginDone(String user, String password) {
        Log.println(Log.DEBUG,"Activity.Login","Correct username and password ->"+user+" "+password);
        this.username=user;
        this.password=password;

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        /*
            set hidden all the button if is open
            menu.findItem(R.id.action_websearch).setVisible(!drawOpen);
         */
        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        service = new Intent(this,LocationService.class);
        switch (id){
            case R.id.spotting_toggle:
                if(!this.username.isEmpty() && !this.password.isEmpty() && !mBound) {
                    Log.println(Log.DEBUG, "Activity_ActionBar", "Binding Service");
                    item.setIcon(R.drawable.ic_action_location_found);
                    bindService(service,mConnection,Context.BIND_AUTO_CREATE);
                    mBound = true;

                }
                else{
                    item.setIcon(R.drawable.ic_action_location_off);
                    Toast.makeText(getApplication(),"Spotting disabled",Toast.LENGTH_SHORT).show();
                    Message msg = Message.obtain(null,
                            LocationService.MSG_UNREGISTER_CLIENT);
                    try {
                        mService.send(msg);
                    }catch (RemoteException e){
                        Log.println(Log.DEBUG,"Activity.Unbinding","Service doesn't respond");
                    }
                    //unregisterReceiver(broadcastReceiver);
                    if (mBound) {
                        Log.println(Log.DEBUG, "Activity_ActionBar", "Unbinding Service");
                        unbindService(mConnection);
                        mBound = false;
                    }
                }
                break;


        }
        //noinspection SimplifiableIfStatement
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        Fragment fragment;
        switch (position){
            case MAP_FRAGMENT:
                if(getFragmentManager().findFragmentById(R.id.map)!=null){
                    break;
                }
                fragment = new MyMapFragment();
                Bundle bundle_pos = new Bundle();
                bundle_pos.putSerializable("maps",position_received_from_service);
                fragment.setArguments(bundle_pos);
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mDrawerList.setItemChecked(position,true);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case LIST_FRAGMENT:
                Bundle bundle = new Bundle();
                ArrayList<String> passingArray = new ArrayList<String>();
                for (Map.Entry<Double,MarkerOptions> entry:markersOnMap.entrySet()){
                    passingArray.add(entry.getKey()+"::"+entry.getValue().getTitle());
                }
                bundle.putStringArrayList(MARKERS_STRING,passingArray);
                fragment = new MyListFragment();
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.content_frame,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mDrawerList.setItemChecked(position,true);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case REGISTER_FRAGMENT:
                fragment = new RegisterFragment();
                fragmentTransaction.replace(R.id.content_frame,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mDrawerList.setItemChecked(position,true);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;
            case FAVOURITE_FRAGMENT:
                fragment = new FavouriteFragment();
                fragmentTransaction.replace(R.id.content_frame,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mDrawerList.setItemChecked(position,true);
                mDrawerLayout.closeDrawer(mDrawerList);
                break;



        }


    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        Log.println(Log.DEBUG,"Activity.Configuration","Config changed");

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    public void addMarker(Double d, MarkerOptions mk){
        this.markersOnMap.put(d,mk);
    }







    @Override
    public void userPositionUpdate(Location location) {
        if (currentLocation!=null){
            if(!isBetterLocation(currentLocation,location)){
                return;
            }
        }
        Log.println(Log.DEBUG,"Activity.Position_Update","Position update from map...send mex to service");
        currentLocation = location;
        if (mService == null){
            Log.println(Log.DEBUG,"Activity.Position_Update","You must start a service to send mex");
            return;
        }
        Message msg = Message.obtain(null,
                LocationService.MSG_SET_VALUE);
        Bundle bundle = new Bundle();
        bundle.putString("username",this.username);
        bundle.putDouble("latitude",location.getLatitude());
        bundle.putDouble("longitude",location.getLongitude());
        Log.println(Log.DEBUG,"Activity.Position_Update","mex is "+username+"with latitude "+location.getLatitude()+" and long"
        +location.getLongitude());
        msg.setData(bundle);
        try {
            mService.send(msg);
        }catch (RemoteException e){
            Log.println(Log.DEBUG,"Activity.Position_Update","Service doesn't respond");

        }

    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }




}

