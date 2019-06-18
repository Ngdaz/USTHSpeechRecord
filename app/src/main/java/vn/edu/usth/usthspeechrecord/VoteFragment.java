package vn.edu.usth.usthspeechrecord;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

public class VoteFragment extends Fragment {

    private static final String main_url =  "https://voiceviet.itrithuc.vn/api/v1";
    RequestQueue mQueue;
    MediaPlayer mMediaPlayer;

    TextView mTextView;
    MediaButton btnPlay;
    ImageButton btnLike, btnDislike;
    Button btnGet;
    ProgressBar circleBar;

    DownloadManager downloadManager;
    public String mToken = "";
    String download_url;
    Uri uri;
    String subpath, prev_subpath;
    String pathToFile, mtext, mId, prevId;

    public VoteFragment() {
    }

    public static VoteFragment newInstance(String token) {
        VoteFragment voteFragment = new VoteFragment();
        Bundle args = new Bundle();
        args.putString("TOKEN", token);
        voteFragment.setArguments(args);
        return voteFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = VolleySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        mToken = getArguments().getString("TOKEN");
        View view = inflater.inflate(R.layout.fragment_vote, container, false);
        mTextView = view.findViewById(R.id.voteTextview);
        btnPlay = view.findViewById(R.id.btn_circle_play);
        btnLike = view.findViewById(R.id.btn_like);
        btnDislike = view.findViewById(R.id.btn_dislike);
        btnGet = view.findViewById(R.id.btn_get);
        circleBar = view.findViewById(R.id.vote_progress_bar);
        circleBar.setVisibility(View.INVISIBLE);

        disableButton();
        init();
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevId = mId;
                prev_subpath = subpath;
                getVoiceRef();
                deleteFile();
                btnGet.setText("Tiếp theo");
                enableButton();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathSave = getFilename();
                Log.d("Local", pathSave);
                MediaButton mb = (MediaButton) v;
                switch (mb.getState()) {
                    case 0:
                        mMediaPlayer = new MediaPlayer();
                        try {
                            String src = pathSave + "/" + mId + subpath;
                            Log.d("SRC: ", src);
                            mMediaPlayer.setDataSource(src);
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mMediaPlayer.start();
                        btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                        Toast.makeText(getActivity().getApplicationContext(), "Đang chơi bản ghi", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        if (mMediaPlayer != null) {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                        Toast.makeText(getActivity().getApplicationContext(), "Đã dừng", Toast.LENGTH_SHORT).show();

                        btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        break;
                }
                mb.changeState();
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevId = mId;
                prev_subpath = subpath;
                putVote(mId, "1");
                deleteFile();
                getVoiceRef();
            }
        });

        btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevId = mId;
                prev_subpath = subpath;
                putVote(mId, "0");
                deleteFile();
                getVoiceRef();
            }
        });

        return view;
    }

    private void DownloadVoice(Uri uri) {
        downloadManager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setTitle("Download Voice");
        request.setDescription("Download voice mp3 file");
        request.setDestinationInExternalPublicDir("/VoiceDownload", mId + subpath);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);
    }

    private void init() {
        getVoiceRef();
        enableButton();
    }

    private void getVoiceRef() {
        String url = main_url + "/voice/random";
        mTextView.setText("");
        circleBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("resp");
                    pathToFile = jsonObject.getString("pathToFile");
                    subpath = pathToFile.substring(pathToFile.length()-4);
                    Log.d("subpath: ", subpath);
                    mtext = jsonObject.getString("text");
                    mId = jsonObject.getString("id");
                    download_url = "http://voiceviet.itrithuc.vn" + pathToFile;
                    uri = Uri.parse(download_url);
                    DownloadVoice(uri);
                    mTextView.setText(mtext);
                    circleBar.setVisibility(View.INVISIBLE);
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
                headers.put("Authorization", "Bearer " + mToken);
                return headers;
            }
        };
        mQueue.add(request);
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"VoiceDownload");

        if(!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath());
    }

    private void deleteFile() {
        File file = new File(getFilename() +"/"+ prevId + prev_subpath);
        file.delete();
    }

    private void putVote(String id, String vote) {
        String url = main_url + "/voice/vote/" + id + "/" + vote;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String code = "";
                try {
                    code = response.getString("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Code", code);
                if (code.equals("200")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Đã bình chọn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Bình chọn không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public HashMap<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
                headers.put("accept", "application/json");
                headers.put("Authorization", "Bearer " + mToken);
                return headers;
            }
        };
        mQueue.add(request);
    }

    private void disableButton() {
        btnPlay.setEnabled(false);
        btnPlay.setBackgroundResource(R.drawable.record_shape_disable);
        btnLike.setEnabled(false);
        btnLike.setBackgroundResource(R.drawable.play_retry_disable);
        btnDislike.setEnabled(false);
        btnDislike.setBackgroundResource(R.drawable.play_retry_disable);
    }

    private void enableButton() {
        btnPlay.setEnabled(true);
        btnPlay.setBackgroundResource(R.drawable.recordshape);
        btnLike.setEnabled(true);
        btnLike.setBackgroundResource(R.drawable.play_retry_bg);
        btnDislike.setEnabled(true);
        btnDislike.setBackgroundResource(R.drawable.play_retry_bg);
    }
}
