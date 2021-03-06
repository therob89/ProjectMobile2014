package com.example.robertopalamaro.projectmobile;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by robertopalamaro on 18/11/14.
 */
public class RegisterFragment extends Fragment {


    private ImageView imageView;
    private Button button;
    private TextView username;
    private TextView password;
    private TextView email;
    private Button loginButton;
    private ProgressBar progressBar;
    private final static int SELECT_IMAGE =110;
    onLoginListener onLoginListner;
    private RegisterTask registerTask;
    private LoginAsyncTask loginAsyncTask;
    public RegisterFragment(){}
    //Timer timer = new Timer();

    public interface onLoginListener{
        public void onLoginDone(String user,String password);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onLoginListner = (onLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }

    }

    @Override
    public void onDestroy() {
        if(registerTask!=null){
            registerTask.cancel(true);

        }
        if(loginAsyncTask!=null){
            loginAsyncTask.cancel(true);
        }
        super.onDestroy();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.register_layout,container,false);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
            }
        });
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        button =(Button)rootView.findViewById(R.id.loginButton);
        loginButton = (Button)rootView.findViewById(R.id.buttonLogin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()){
                    loginButton.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);
                    if(username.getText().toString().isEmpty() || password.getText().toString().isEmpty() || email.getText().toString().isEmpty()){
                        Toast.makeText(getActivity(),"Insert username,password,email to register",Toast.LENGTH_LONG).show();
                        return;
                    }
                    //v.setClickable(false);
                    registerTask= new RegisterTask();
                    registerTask.execute("http://robsite.altervista.org/mobile/sendData.php");
                    loginButton.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    button.setVisibility(View.INVISIBLE);
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(username.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Insert username and password to login",Toast.LENGTH_LONG).show();
                    return;
                }
                loginAsyncTask = new LoginAsyncTask();
                loginAsyncTask.execute("http://robsite.altervista.org/mobile/login.php");
                hideSoftKeyboard();
                loginButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                button.setVisibility(View.INVISIBLE);



            }
        });

        imageView =(ImageView)rootView.findViewById(R.id.imageViewRegister);
        username = (TextView) rootView.findViewById(R.id.username);
        password = (TextView) rootView.findViewById(R.id.password);
        email = (TextView)rootView.findViewById(R.id.email);
        //imageView.setBackgroundColor(Color.GRAY);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.INFO,"REGISTER","Pressed on image View");
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), SELECT_IMAGE);

            }
        });

    return rootView;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView.setBackgroundColor(0x00000000);
        if (requestCode == SELECT_IMAGE)
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                // TODO Do something with the select image URI
                imageView.setImageURI(selectedImage);

            }
    }
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }



    private class RegisterTask extends AsyncTask<String,Void,Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Log.println(Log.DEBUG,"REGISTER-TASK","Doing Request");
            InputStream inputStream;
            //String result = "";
            final int COMPRESSION_QUALITY = 80;
            try {


                // 1. create HttpClient

                //HttpParams httpParameters = new BasicHttpParams();
                //HttpConnectionParams.setConnectionTimeout(httpParameters, 100000);
                //HttpConnectionParams.setSoTimeout(httpParameters, 10000+12000);
                //DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                // 2. make POST request to the given URL
                DefaultHttpClient httpClient = new DefaultHttpClient();
                workAroundReverseDnsBugInHoneycombAndEarlier(httpClient);
                HttpPost httpPost = new HttpPost("http://robsite.altervista.org/mobile/sendData.php");
                //httpPost.setParams(httpParameters);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username.getText().toString());
                    jsonObject.put("password", password.getText().toString());
                    jsonObject.put("email", email.getText().toString());
                }catch (JSONException e){
                    Log.println(Log.DEBUG, "REGISTER-TASK", "error with json"+e.getLocalizedMessage());
                }

                Log.println(Log.DEBUG, "REGISTER-TASK", "Create Request");
                //HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                if(imageView.getDrawable()!=null) {
                    String encodedImage;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY,
                            byteArrayBitmapStream);
                    byte[] b = byteArrayBitmapStream.toByteArray();
                    encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                    jsonObject.put("image", encodedImage);
                }


                httpPost.setEntity(new ByteArrayEntity(jsonObject.toString().getBytes(
                        "UTF8")));
                //httpPost.setHeader("json", jsonObject.toString());
                httpPost.setHeader("Content-Type","application/json");
                httpPost.setEntity(new StringEntity(jsonObject.toString()));
                // 8. Execute POST request to the given URL

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpClient.execute(httpPost, responseHandler);
                Log.println(Log.DEBUG,"REGISTER-TASK","executed post with this json"+jsonObject.toString());
                Log.println(Log.DEBUG, "REGISTER-TASK", "This is the server reply "+response);
                String[]tokens = response.split("<br/>");
                if(Integer.parseInt(tokens[tokens.length-1])==0){
                    this.publishProgress();
                    Log.println(Log.DEBUG, "REGISTER", "Correct");
                    return 0;
                }





            } catch (UnsupportedEncodingException e) {
                Log.println(Log.DEBUG, "REGISTER-TASK", "Error with encoding"+e.getLocalizedMessage());

                e.printStackTrace();
            } catch (ClientProtocolException e) {
                Log.println(Log.DEBUG, "REGISTER-TASK", "Error with protocol "+e.getLocalizedMessage());

                e.printStackTrace();
            } catch (IOException e) {
                Log.println(Log.DEBUG, "REGISTER-TASK", "Error of io "+e.getLocalizedMessage());
                e.printStackTrace();
            }catch (Exception e) {
                Log.println(Log.DEBUG, "REGISTER-TASK", "Error"+e.getLocalizedMessage());

            }
            return -1;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //button.setBackgroundColor(Color.GREEN);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer==0) {
                onLoginListner.onLoginDone(username.getText().toString(), password.getText().toString());
                Toast.makeText(getActivity(),"Correct Sign-in",Toast.LENGTH_SHORT).show();
                username.setText("");
                password.setText("");
                email.setText("");



            }
            else{
                Toast.makeText(getActivity(),"username|password already taken",Toast.LENGTH_SHORT).show();
            }
            button.setClickable(true);
            loginButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }


    private class LoginAsyncTask extends AsyncTask<String,Void,Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            try {

                HttpPost httpPost = new HttpPost(params[0]);
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
                HttpConnectionParams.setSoTimeout(httpParameters, 10000 + 12000);
                HttpClient httpClient = new DefaultHttpClient();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username.getText().toString());
                jsonObject.put("password", password.getText().toString());
                httpPost.setEntity(new ByteArrayEntity(jsonObject.toString().getBytes(
                        "UTF8")));
                httpPost.setEntity(new StringEntity(jsonObject.toString()));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpClient.execute(httpPost, responseHandler);
                String[]tokens = response.split("<br/>");

                Log.println(Log.INFO, "LOGIN-TASK", "executed post with this json" + jsonObject.toString());
                Log.println(Log.INFO, "LOGIN-TASK", "This is the server reply "+response);
                Log.println(Log.INFO, "LOGIN-TASK", "This is the server answer with code"+tokens[tokens.length-1]);
                if(Integer.parseInt(tokens[tokens.length-1])==0){
                    Log.println(Log.INFO, "LOGIN-TASK", "Correct");
                    return 0;
                }

            } catch (JSONException e) {
                Log.println(Log.INFO, "LOGIN-TASK", "error with json" + e.getLocalizedMessage());

            } catch (UnsupportedEncodingException e) {
                Log.println(Log.INFO, "LOGIN-TASK", "Error with encoding" + e.getLocalizedMessage());

                e.printStackTrace();

            } catch (IOException e) {
                Log.println(Log.INFO, "LOGIN-TASK", "Error of io " + e.getLocalizedMessage());
                e.printStackTrace();
            } catch (Exception e) {
                Log.println(Log.INFO, "LOGIN-TASK", "Error" + e.getLocalizedMessage());
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer==0) {
                onLoginListner.onLoginDone(username.getText().toString(), password.getText().toString());
                Toast.makeText(getActivity(),"Correct login",Toast.LENGTH_SHORT).show();
                username.setText("");
                password.setText("");
            }
            else{
                Toast.makeText(getActivity(),"Username or password incorrect",Toast.LENGTH_SHORT).show();
            }
            loginButton.setClickable(true);
            loginButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);

        }
    }


    public void hideSoftKeyboard() {
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }


    private void workAroundReverseDnsBugInHoneycombAndEarlier(HttpClient client) {
        // Android had a bug where HTTPS made reverse DNS lookups (fixed in Ice Cream Sandwich)
        // http://code.google.com/p/android/issues/detail?id=13117
        SocketFactory socketFactory = new LayeredSocketFactory() {
            SSLSocketFactory delegate = SSLSocketFactory.getSocketFactory();
            @Override public Socket createSocket() throws IOException {
                return delegate.createSocket();
            }
            @Override public Socket connectSocket(Socket sock, String host, int port,
                                                  InetAddress localAddress, int localPort, HttpParams params) throws IOException {
                return delegate.connectSocket(sock, host, port, localAddress, localPort, params);
            }
            @Override public boolean isSecure(Socket sock) throws IllegalArgumentException {
                return delegate.isSecure(sock);
            }
            @Override public Socket createSocket(Socket socket, String host, int port,
                                                 boolean autoClose) throws IOException {
                injectHostname(socket, host);
                return delegate.createSocket(socket, host, port, autoClose);
            }
            private void injectHostname(Socket socket, String host) {
                try {
                    Field field = InetAddress.class.getDeclaredField("hostName");
                    field.setAccessible(true);
                    field.set(socket.getInetAddress(), host);
                } catch (Exception ignored) {
                }
            }
        };
        client.getConnectionManager().getSchemeRegistry()
                .register(new Scheme("https", socketFactory, 443));
    }


}
