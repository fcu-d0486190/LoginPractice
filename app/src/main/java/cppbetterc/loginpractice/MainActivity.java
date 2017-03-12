package cppbetterc.loginpractice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=1000000;
    public static final int READ_TIMEOUT=1000000;
    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    //尋照相對應在 R Class 的元件
        etUsername=(EditText)findViewById(R.id.username);
        etPassword=(EditText)findViewById(R.id.password);

    }
    //當Button被點擊後的選項
    public void checkLogin(View argc0){
    //將EditText輸入的文字得到定轉乘String型態
        final String username=etUsername.getText().toString();
        final String password=etPassword.getText().toString();

    //初始化 AsyncLogin() class with username and password
    //下方的 AsyncLogin() class
        new AsyncLogin().execute(username,password);
    }

    private class AsyncLogin extends AsyncTask<String,String ,String>
    {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url=null;
        private static  final String TAG="MainActivity";

        @Override
        protected  void onPreExecute(){
            super.onPreExecute();
            //this method will be running on UIthread
            pdLoading.setMessage("Loading");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                //Enter URL address where your php file resides
                url=new URL("http://192.168.1.101/loginc.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
            }
            //Log.v("TAG","checkpoint");
            try{
                //Set HttpURLConnection class to send and receive data from php and mysql
                conn=(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                //set DoInput and DoOutput method depict(描繪) handing of both send and service
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //Append(附加) parameters to URL(使用Uri)
                Uri.Builder builder=new Uri.Builder()
                        .appendQueryParameter("username",params[0])
                        .appendQueryParameter("password",params[1]);
                String query = builder.build().getEncodedQuery();

                //Open connection for sending data
                OutputStream os=conn.getOutputStream();
                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOException", "exception", e);
                return "exception";
            }
            try{
                int response_code = conn.getResponseCode();
                Log.e("exception", conn.toString());
                //Check if successful connection made
                if(response_code == HttpURLConnection.HTTP_OK){
                    //read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    StringBuilder result = new StringBuilder();
                    String line;

                    while((line = reader.readLine())!=null){
                        result.append(line);
                    }
                    Log.d("result", result.toString());
                    //pass data to onPostExecute method
                    return result.toString();
                }
                else{
                    return ("unsuccessful");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOException", "exception", e);
                return "exception";
            }
            finally{
                conn.disconnect();
            }
        }
        //包在inner class裡
        @Override
        protected  void onPostExecute(String result){
            //this method will be running on UI thread
            pdLoading.dismiss();
            if(result.equalsIgnoreCase("true")){
            /*
            Here launching another activity when login successful. If you persist login state
            use sharedPreferences of Android. and logout button to clear sharedPreferences.
             */
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //Log.e("IOException", "exception", e);
                //MainActivity.this.finish();
            }
            else if(result.equalsIgnoreCase("false")){
                // If username and password does not match display a error message
                Toast.makeText(MainActivity.this,"Invalid username or password",Toast.LENGTH_LONG).show();
            }
            else if(result.equalsIgnoreCase("exception")||result.equalsIgnoreCase("unsuccessful")){
                Toast.makeText(MainActivity.this,"OOPs! Something went wrong . Connection Problem.exception3",Toast.LENGTH_LONG).show();
            }
        }
    }
}
