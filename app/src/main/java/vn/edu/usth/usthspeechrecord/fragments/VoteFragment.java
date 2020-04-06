package vn.edu.usth.usthspeechrecord.fragments;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
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
import java.util.HashMap;

import vn.edu.usth.usthspeechrecord.R;
import vn.edu.usth.usthspeechrecord.utils.VolleySingleton;
import vn.edu.usth.usthspeechrecord.views.MediaButton;

import static com.android.volley.VolleyLog.TAG;

public class VoteFragment extends Fragment {

    private static final String main_url =  "https://voiceviet.itrithuc.vn/api/v1";
    private RequestQueue mQueue;
    private MediaPlayer mMediaPlayer;

    private TextView mTextView;
    private MediaButton btnPlay;
    private ImageButton btnLike, btnDislike;
    private Button btnGet;
    private ProgressBar circleBar;

    private DownloadManager downloadManager;
    private String mToken = "";
    private String download_url;
    private Uri uri;
    private String subpath, prev_subpath;
    private String pathToFile, mtext, mId, prevId;
    Context context;

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
        mQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        context=getActivity();
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
                btnGet.setText(getString(R.string.next));
                btnGet.setText(getString(R.string.next));
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
                        String src = pathSave + "/" + mId + subpath;
                        Log.d("SRC: ", src);
//                            mMediaPlayer.setDataSource(src);

                        mMediaPlayer.start();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if (mMediaPlayer != null) {
                                    mMediaPlayer = new MediaPlayer();
                                    mMediaPlayer.stop();
                                    mMediaPlayer.release();
                                }
                                Toast.makeText(getActivity(), getString(R.string.has_stopped), Toast.LENGTH_SHORT).show();

                                btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                                mb.changeState();
                            }
                        });
                        btnPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                        Toast.makeText(getActivity(), getString(R.string.playing_record), Toast.LENGTH_SHORT).show();
                        mb.changeState();
                        break;
                    case 1:
                        if (mMediaPlayer != null) {
                            mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                        Toast.makeText(getActivity(), getString(R.string.has_stopped), Toast.LENGTH_SHORT).show();

                        btnPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        mb.changeState();
                        break;
                }
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
        Log.e(TAG, "DownloadVoice: start" );
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
                Log.e(TAG, "onResponse: "+response.toString() );
                try {
                    mMediaPlayer = new MediaPlayer();
                    JSONObject jsonObject = response.getJSONObject("resp");
                    pathToFile = jsonObject.getString("pathToFile");
                    subpath = pathToFile.substring(pathToFile.length()-4);
                    Log.d("subpath: ", subpath);
                    mtext = jsonObject.getString("text");
                    mId = jsonObject.getString("id");
                    //added https
                    download_url = "https://voiceviet.itrithuc.vn" + pathToFile;
                    mMediaPlayer.setDataSource(context,Uri.parse(download_url));
                    mMediaPlayer.prepare();
                    Log.e(TAG, "onResponse: audio"+download_url );
                    uri = Uri.parse(download_url);
                    DownloadVoice(uri);
                    mTextView.setText(mtext);
                    circleBar.setVisibility(View.INVISIBLE);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "onErrorResponse: error" );
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
                    Toast.makeText(getActivity(), getString(R.string.voted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.voting_failed), Toast.LENGTH_SHORT).show();
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
        btnPlay.setBackgroundResource(R.drawable.circle_gradient);
        btnLike.setEnabled(false);
        btnLike.setBackgroundResource(R.drawable.play_retry_disable);
        btnDislike.setEnabled(false);
        btnDislike.setBackgroundResource(R.drawable.play_retry_disable);
    }

    private void enableButton() {
        btnPlay.setEnabled(true);
        btnPlay.setBackgroundResource(R.drawable.circle_gradient);
        btnLike.setEnabled(true);
        btnLike.setBackgroundResource(R.drawable.play_retry_bg);
        btnDislike.setEnabled(true);
        btnDislike.setBackgroundResource(R.drawable.play_retry_bg);
    }
}