package vn.edu.usth.usthspeechrecord;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RecordFragment extends Fragment {
    private static final String main_url = "https://voiceviet.itrithuc.vn/api/v1";

    RequestQueue mQueue;
    Button btnPlay, btnGetText, btnRetry;
    StateButton btnStartRecord;
    Spinner listCategories;
    TextView mTextView;

    MediaPlayer mMediaPlayer;
    public String mText = "";
    public String mId = "";
    public String mToken = "";
    public int mCatId = 0;
    public ArrayList<Category> mCategories = new ArrayList<Category>();
    CategoryAdapter categoryAdapter;

    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = "_audio_record.wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    public RecordFragment() {
    }

    public static RecordFragment newInstance(String token) {
        RecordFragment recordFragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putString("TOKEN", token);
        recordFragment.setArguments(args);
        return recordFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        mToken = getArguments().getString("TOKEN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        listCategories = view.findViewById(R.id.spnCategory);
        Category init = new Category("Please choose one category", 0);
        mCategories.add(init);

        categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), R.layout.categories_item, mCategories);

        getCategory();

        listCategories.setAdapter(categoryAdapter);
        listCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    btnGetText.setEnabled(true);
                    mCatId = mCategories.get(position).getCatNum();
                } else {
                    btnGetText.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        mTextView = view.findViewById(R.id.get_text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        btnGetText = view.findViewById(R.id.btn_get_text);
        btnStartRecord = view.findViewById(R.id.btnStartRecord);
        btnPlay = view.findViewById(R.id.btnPlayRecord);
        btnRetry = view.findViewById(R.id.btn_retry);

        btnGetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonParse();
            }
        });


        mTextView.setText(mToken);
        Log.d("RESP2", mToken);
        Toast.makeText(getActivity().getApplicationContext(), mToken, Toast.LENGTH_SHORT).show();

        return view;
    }


    private void jsonParse() {
        String url = main_url + "/text/category/" + mCatId + "/random";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("resp");
                    mText = jsonObject.getString("text");
                    mId = jsonObject.getString("id");
                    mTextView.setText(mText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        mQueue.add(request);
    }

    private void getCategory() {
        String url = main_url + "/domain/8";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray categories = response.getJSONArray("resp").getJSONObject(0).getJSONArray("categories");
                    for (int i=0; i < categories.length(); i++) {
                        JSONObject cate = categories.getJSONObject(i);
                        String catName = cate.getString("name");
                        String id = cate.getString("id");
                        Category newCat = new Category(catName, Integer.valueOf(id));
                        mCategories.add(newCat);
                        categoryAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        mQueue.add(request);
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