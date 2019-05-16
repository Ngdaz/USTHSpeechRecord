package vn.edu.usth.usthspeechrecord;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String main_url =  "https://voiceviet.itrithuc.vn/api/v1";
    final int REQUEST_PERMISSION_CODE = 1000;
    public String mToken = "";
    RequestQueue mQueue;

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        IntentFilter filter = new IntentFilter("TOKEN");


        mQueue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

        if (!checkPermissionFromDevice()) {
            requestPermission();
        }

        Login();


        navigation = findViewById(R.id.btm_nav_bar);
        getSupportFragmentManager().beginTransaction().add(R.id.container, RecordFragment.newInstance(mToken)).commit();
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment newFragment;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_record:
                        newFragment = RecordFragment.newInstance(mToken);
                        break;
                    case R.id.navigation_vote:
                        newFragment = new VoteFragment();
                        break;
                    case R.id.navigation_edit:
                        newFragment = new EditFragment();
                    default:
                        newFragment = new Fragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container, newFragment).commit();
                return true;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public boolean checkPermissionFromDevice() {
        int write_exteral_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int internet_result = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        return write_exteral_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED && internet_result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET
        },REQUEST_PERMISSION_CODE);
    }
    private void Login() {
        String username = getString(R.string.user_name);
        String password = getString(R.string.password);
        String url = main_url + "/user/login" + "/" + username + "/" + password;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("resp");
                    mToken = jsonObject.getString("token");
                    Log.d("RESP", mToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public HashMap<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        mQueue.add(request);
    }

}
