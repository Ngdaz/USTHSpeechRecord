package vn.edu.usth.usthspeechrecord;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String main_url =  "https://voiceviet.itrithuc.vn/api/v1";
    final int REQUEST_PERMISSION_CODE = 1000;
    public String mToken = "";
    RequestQueue mQueue;

    TabLayout mTabLayout;
    ViewPager viewPager;

    private int[] tabIcons = {
            R.drawable.ic_mic_black_24dp,
            R.drawable.ic_thumbs_up_down_black_24dp,
            R.drawable.ic_mode_edit_black_24dp
    };

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLayout = findViewById(R.id.btm_nav_bar);
        viewPager = findViewById(R.id.container);
        mQueue = VolleySingleton.getInstance(getApplicationContext()).getRequestQueue();

        getSupportActionBar().setTitle("Record");

        if (!checkPermissionFromDevice()) {
            requestPermission();
        }

        deleteFolder();

        Login();

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(RecordFragment.newInstance(mToken), "Record");
        adapter.addFragment(VoteFragment.newInstance(mToken), "Vote");
        adapter.addFragment(EditFragment.newInstance(mToken), "Edit");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("Record");
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Vote");
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Edit");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }
    private void setupTabIcons() {
        if (mTabLayout!=null){
            if (mTabLayout.getTabAt(0)!=null)
                mTabLayout.getTabAt(0).setIcon(tabIcons[0]);
            if (mTabLayout.getTabAt(1)!=null)
                mTabLayout.getTabAt(1).setIcon(tabIcons[1]);
            if (mTabLayout.getTabAt(2)!=null)
                mTabLayout.getTabAt(2).setIcon(tabIcons[2]);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    public boolean checkPermissionFromDevice() {
        int write_exteral_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int internet_result = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        return write_exteral_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED && internet_result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aciton_bar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                LoginOnWeb();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void LoginOnWeb() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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
                    setupViewPager(viewPager);
                    mTabLayout.setupWithViewPager(viewPager);
                    setupTabIcons();
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

    private void deleteFolder() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"VoiceDownload");
        file.delete();
        file = new File(filepath,"Audio");
        file.delete();
    }
}
