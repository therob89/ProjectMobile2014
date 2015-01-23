package com.example.robertopalamaro.projectmobile;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by robertopalamaro on 25/11/14.
 */



public class LocationService extends Service{

    //private final static String TAG ="Broadcast message";
    //public static final String BROADCAST_ACTION = "com.example.robertopalamaro.projectmobile.updateUI";
    private ArrayList<FriendMarkers>usersPosition;
    static final int MSG_REGISTER_CLIENT = 1;
    private HashMap<String,String> maps = null;
    private Stack<String> pendingPositions;
    private String usernameForGetTask = null;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    static final int MSG_SET_VALUE = 3;

    private final Handler handler = new Handler();
    //private final Handler handler2 = new Handler();

    Messenger mClients;
    Timer timer;
    Integer value;
    //private Intent intent;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private String username=null;
    private double latitude_to_send =0;
    private double longitude_to_send=0;
    //private TimerTask taskToSend;
    //private TimerTask taskToAcquire;
    private Bundle mex_attachment_bundle;
    private boolean sending_flag = false;
    private boolean alreadyAcquiringPosition = false;
    private sendMyPositionTask sendTask = null;

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.println(Log.DEBUG,"Service.GetPosition-Task","Get Data Async started");
                            if(usernameForGetTask==null){
                                Log.println(Log.DEBUG,"Service.GetPosition-Task","No user is set to getting data from server");
                                return;
                            }
                            getPositionsTask performBackgroundTask = new getPositionsTask();
                            performBackgroundTask.execute("http://robsite.altervista.org/mobile/getAllPositions.php");

                            // PerformBackgroundTask this class is the class that extends AsyncTask
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.println(Log.DEBUG,"Service.GetPosition-Task","Error Async Get");

                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 120000); //execute in every 50000 ms
    }


    public void sendPosinAsyncMode() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.println(Log.DEBUG,"LocService.SendAsync","Sending data Async Started!");
                            if(sending_flag==false){
                                if (username==null && latitude_to_send==0 && longitude_to_send==0){
                                    if (pendingPositions.size()!=0){
                                        Log.println(Log.DEBUG,"LocService.SendAsync","Sending data from the stack");
                                        if(usernameForGetTask==null || !usernameForGetTask.equals(username)){
                                            usernameForGetTask=username;
                                        }
                                        sending_flag = true;
                                        String[] temp = pendingPositions.pop().split("::");
                                        username = temp[0];
                                        latitude_to_send = Double.valueOf(temp[1]);
                                        longitude_to_send = Double.valueOf(temp[2]);
                                        sendTask.execute("http://robsite.altervista.org/mobile/sendPositions.php");
                                        pendingPositions.clear();
                                    }
                                }
                            }

                            // PerformBackgroundTask this class is the class that extends AsynchTask
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.println(Log.DEBUG,"LocService.SendAsync","Error send Async");

                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 30000, 180000); //execute in every 50000 ms
    }


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.println(Log.DEBUG,"Service-Mex-Handler","Register client");
                    mClients=msg.replyTo;
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients=null;
                    usersPosition.clear();
                    usersPosition=null;
                    break;

                case MSG_SET_VALUE:
                    Log.println(Log.DEBUG,"Service-Mex-Handler","Updating position mex");
                    value= msg.arg1;
                    username =(String) msg.getData().get("username");
                    latitude_to_send = (Double)msg.getData().get("latitude");
                    longitude_to_send = (Double)msg.getData().get("longitude");
                    if(usernameForGetTask==null || !usernameForGetTask.equals(username)){
                        usernameForGetTask=username;
                    }
                    if(sendTask==null){
                        sendTask = new sendMyPositionTask();
                    }
                    if(!sending_flag){
                        Log.println(Log.DEBUG,"Service-Mex-Handler","Sending data");
                        sending_flag=true;
                        //sendTask.execute("http://robsite.altervista.org/mobile/sendPositions.php");
                        new sendMyPositionTask().execute("http://robsite.altervista.org/mobile/sendPositions.php");
                    }
                    else{
                        Log.println(Log.DEBUG,"Service-Mex-Handler","We are already sending data....put in the stack");
                        String temp = username+"::"+String.valueOf(latitude_to_send)+"::"+String.valueOf(longitude_to_send);
                        pendingPositions.push(temp);


                    }
                    /*
                    if(!alreadyAcquiringPosition) {
                        Log.println(Log.DEBUG,"Service-Mex-Handler","***Starting to get position");
                        alreadyAcquiringPosition = true;
                        callAsynchronousTask();
                    }
                    */
                    //removeMessages(LocationService.MSG_SET_VALUE);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //intent = new Intent(BROADCAST_ACTION);


    }
/*
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            handler.postDelayed(this, 2000); // 20 seconds

            sendDataToActivitity();
        }
    };

    private void sendDataToActivitity(){
        Log.println(Log.INFO,"Service","Sending data to activity");
        intent.putExtra("Random","1234567");
        sendBroadcast(intent);
    }
    */

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Service stopped", Toast.LENGTH_SHORT).show();
        timer.cancel();
        //taskToAcquire=null;
        //taskToSend=null;
        alreadyAcquiringPosition = false;
        handler.removeCallbacksAndMessages(null);
    }


    private class getPositionsTask extends AsyncTask<String,Void,Integer>{
        @Override
        protected Integer doInBackground(String... params) {
            Log.println(Log.DEBUG,"Service.GetPosition-Task","Getting position for user: "+usernameForGetTask);
            String url = params[0];
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000+12000);
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url);
            JSONArray jsonArray;
            try {
                JSONObject jsonObject2= new JSONObject();
                jsonObject2.put("username",usernameForGetTask);
                httpPost.setEntity(new ByteArrayEntity(jsonObject2.toString().getBytes(
                        "UTF8")));
                httpPost.setHeader("json", jsonObject2.toString());
                httpPost.setEntity(new StringEntity(jsonObject2.toString()));
                //List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                //nameValuePairs.add(new BasicNameValuePair("username",username));


                //httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                //httpPost.setHeader("username",username);
                Log.println(Log.DEBUG,"Service.GetPosition-Task","Request:  "+jsonObject2.toString());


                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpClient.execute(httpPost, responseHandler);
                Log.println(Log.DEBUG,"Service.GetPosition-Task","Server's reply: "+response);

                String [] tokens = response.split("<br/>");
                jsonArray = new JSONArray(tokens[tokens.length-1]);
                JSONObject jsonObject;
                if(maps==null){
                    maps = new HashMap<String, String>();
                }
                String user;
                String lat_long_avatar;
                for (int j=0;j<jsonArray.length();j++){
                    user = lat_long_avatar = "";
                    for (int i=0;i<jsonArray.getJSONArray(j).length();i++) {
                        jsonObject = jsonArray.getJSONArray(j).getJSONObject(i);
                        switch (i) {
                            case 0:
                                user = jsonObject.getString("user");
                                break;
                            case 1:
                                lat_long_avatar = jsonObject.getString("latitude");
                                break;
                            case 2:
                                lat_long_avatar = lat_long_avatar + "::" + jsonObject.getString("longitude");
                                break;
                            case 3:
                                lat_long_avatar = lat_long_avatar + "::" + jsonObject.getString("avatar");
                        }
                    }
                    maps.put(user,lat_long_avatar);
                }
                Log.println(Log.DEBUG,"Service.GetPosition-Task","Map_SIZE:  "+maps.size());
                return 0;

            }catch (UnsupportedEncodingException e){
                Log.println(Log.DEBUG, "Service.GetPosition-Task", "error with encoding"+e.getLocalizedMessage());
            }catch (JSONException e){
                Log.println(Log.DEBUG, "Service.GetPosition-Task", "Error of json" + e.getLocalizedMessage());
                e.printStackTrace();

            }catch (IOException e) {
                Log.println(Log.DEBUG, "Service.GetPosition-Task", "Error of io " + e.getLocalizedMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.println(Log.DEBUG, "Service.GetPosition-Task", "Error" + e.getLocalizedMessage());
            }
            return -1;

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer==-1){
                Log.println(Log.DEBUG, "Service.GetPosition-Task","Get request: returns -1");
                return;
            }
            if (maps==null){
                Log.println(Log.DEBUG, "Service.GetPosition-Task","Maps is null...nothing to send");
                return;
            }
            Log.println(Log.DEBUG, "Service.GetPosition-Task","Replying to activity with # "+maps.size()+" new user");
            try {
                if(mex_attachment_bundle==null){
                    mex_attachment_bundle = new Bundle();
                }
                Message mex =(Message.obtain(null, MSG_SET_VALUE, value, 0));
                Bundle bundle = new Bundle();
                bundle.putSerializable("maps",maps);
                mex.setData(bundle);
                mClients.send(mex);
                //maps.clear();
            }catch (RemoteException e ){
                mClients=null;
                Log.println(Log.DEBUG, "Service.GetPosition-Task", "Remote not respond:" + e.getLocalizedMessage());

            }

        }
    }

    private class sendMyPositionTask extends AsyncTask <String,Void,Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (username==null || latitude_to_send == 0 || longitude_to_send == 0){
                Log.println(Log.DEBUG,"SERVICE-Task-Send-Pos","No new position to send");
                cancel(true);
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == 0){
                Log.println(Log.DEBUG, "Service-Task-Send-Pos", "Correct sending data");
                username = null;
                latitude_to_send = 0;
                longitude_to_send = 0;
            }
            else{
                // put in the stack
                Log.println(Log.DEBUG, "Service-Task-Send-Pos", "Error when sending data,insert into stack");
                String temp = username+"::"+String.valueOf(latitude_to_send)+"::"+String.valueOf(longitude_to_send);
                pendingPositions.push(temp);

            }
            sending_flag=false;
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.println(Log.DEBUG,"SERVICE-Task-SendPos","Sending Position");
            String url = params[0];
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
            HttpConnectionParams.setSoTimeout(httpParameters, 10000+12000);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username",username);
                jsonObject.put("latitude",latitude_to_send);
                jsonObject.put("longitude",longitude_to_send);
                httpPost.setEntity(new ByteArrayEntity(jsonObject.toString().getBytes(
                        "UTF8")));
                httpPost.setEntity(new StringEntity(jsonObject.toString()));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpClient.execute(httpPost, responseHandler);
                Log.println(Log.DEBUG,"SERVICE-Task-SendPos","Reply of server "+response);
                String[]tokens = response.split("<br/>");
                if(Integer.parseInt(tokens[tokens.length-1])==0){
                    return 0;
                }



            }catch (JSONException e){
                Log.println(Log.DEBUG, "SERVICE-Task-SendPos", "error with json"+e.getLocalizedMessage());
            }catch (UnsupportedEncodingException e){
                Log.println(Log.DEBUG, "SERVICE-Task-SendPos", "error with encoding"+e.getLocalizedMessage());
            } catch (IOException e) {
                Log.println(Log.DEBUG, "SERVICE-Task-SendPos", "Error of io " + e.getLocalizedMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.println(Log.DEBUG, "SERVICE-Task-SendPos", "Error" + e.getLocalizedMessage());
            }
            return -1;

        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Service Binding", Toast.LENGTH_SHORT).show();
        Log.println(Log.DEBUG, "LocationService.bind", "Service bind");
        timer=new Timer();
        pendingPositions = new Stack<String>();
        sendPosinAsyncMode();
        callAsynchronousTask();
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(), "Location-Service Unbind", Toast.LENGTH_SHORT).show();
        Log.println(Log.DEBUG, "LocationService.unbind", "Service unbind");
        handler.removeCallbacksAndMessages(null);
        alreadyAcquiringPosition=false;
        pendingPositions.clear();
        return super.onUnbind(intent);

    }
}
