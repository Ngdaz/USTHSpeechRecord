package vn.edu.usth.usthspeechrecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.text.method.Touch;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;


public class RecordFragment extends Fragment {
    private static final String main_url = "https://voiceviet.itrithuc.vn/api/v1";

    RequestQueue mQueue;
    Button btnGetText;
    ImageButton btnRetry;
    StateButton btnStartRecord;
    MediaPlayButton btnPlay;
    Spinner btnDialog;
    TextView mTextView;
    String pathSave = "";
    ProgressBar circleBar;

    MediaPlayer mMediaPlayer;
    String mText = "";
    String mId = "";
    String mToken = null;
    Boolean checkToken = false;
    int mCatId = 0;
    ArrayList<Category> mCategories = new ArrayList<Category>();
    CategoryAdapter categoryAdapter;

    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = "_audio_record.wav";
    private static final String AUDIO_RECORDER_FOLDER = "Audio";
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

            mToken = getArguments().getString("TOKEN");
            if (mToken!=null) {
                checkToken = true;
            } else {
                checkToken = false;
            }
//        Log.d("RESP2", mToken);

        bufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

//        Category init = new Category("Please choose one category", 0);
//        mCategories.add(init);

        categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), R.layout.categories_item, mCategories);
        getCategory();

        btnDialog = view.findViewById(R.id.btn_dialog);
//        btnDialog.setOnItemClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
//
//
//                builder.setSingleChoiceItems(categoryAdapter, 0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        mCatId = mCategories.get(which).getCatNum();
//                        String strname = mCategories.get(which).getCatName();
////                        btnDialog.setText(strname);
//                        if (which!=0) btnGetText.setEnabled(true);
//                        dialog.dismiss();
//                    }
//                });
//                builder.show();
//            }
//        });
        btnDialog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCatId = mCategories.get(position).getCatNum();
                Log.d("cat1:", mCategories.get(position).getCatName());
                jsonParse();
                btnStartRecord.setEnabled(true);
                btnStartRecord.setBackgroundResource(R.drawable.recordshape);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                mCatId = mCategories.get(0).getCatNum();
//                Log.d("cat: ", mCategories.get(0).getCatName());
//                jsonParse();
//                btnStartRecord.setEnabled(true);
//                btnStartRecord.setBackgroundResource(R.drawable.recordshape);
            }
        });
        btnDialog.setAdapter(categoryAdapter);

        mTextView = view.findViewById(R.id.get_text);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

        btnGetText = view.findViewById(R.id.btn_get_text);
        btnStartRecord = view.findViewById(R.id.btnStartRecord);
        btnPlay = view.findViewById(R.id.btnPlayRecord);
        btnRetry = view.findViewById(R.id.btn_retry);
        circleBar = view.findViewById(R.id.record_progress_bar);
        circleBar.setVisibility(View.INVISIBLE);

        disableButton();

        btnGetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonParse();
                btnStartRecord.setEnabled(true);
                btnStartRecord.setBackgroundResource(R.drawable.recordshape);
            }
        });

        btnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (btnStartRecord.getState()) {
                    case 0:
                        pathSave = getFilename();
                        startRecording();
                        Toast.makeText(getActivity().getApplicationContext(), "Recording...", Toast.LENGTH_SHORT).show();
                        btnStartRecord.setImageResource(R.drawable.ic_mic_off_black_24dp);

                        btnRetry.setEnabled(false);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);
                        btnStartRecord.changeState();
                        break;
                    case 1:
                        stopRecording();
                        Toast.makeText(getActivity().getApplicationContext(), "Stop recording", Toast.LENGTH_SHORT).show();
                        btnStartRecord.setImageResource(R.drawable.ic_file_upload_black_24dp);
                        btnGetText.setEnabled(false);

                        btnPlay.setEnabled(true);
                        btnPlay.setBackgroundResource(R.drawable.play_retry_bg);

                        btnRetry.setEnabled(true);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_bg);
                        btnStartRecord.changeState();
                        if (!checkToken) {
                            btnStartRecord.setEnabled(false);
                            btnStartRecord.setBackgroundResource(R.drawable.record_shape_disable);
                            Toast.makeText(getActivity().getApplicationContext(), "You need to login before upload voice", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        uploadVoice(pathSave);
                        btnStartRecord.setImageResource(R.drawable.ic_mic_black);

                        btnRetry.setEnabled(false);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);

                        btnPlay.setEnabled(false);
                        btnPlay.setBackgroundResource(R.drawable.play_retry_disable);
                        btnStartRecord.changeState();
                        break;
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayButton mbp = (MediaPlayButton) v;
                switch (mbp.getState()) {
                    case 0:
                        mMediaPlayer = new MediaPlayer();
                        try {
                            Log.d("path", pathSave);
                            mMediaPlayer.setDataSource(pathSave);
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mMediaPlayer.start();
                        Toast.makeText(getActivity().getApplicationContext(), "Playing...", Toast.LENGTH_SHORT).show();

                        btnStartRecord.setEnabled(false);
                        btnStartRecord.setBackgroundResource(R.drawable.record_shape_disable);

                        btnRetry.setEnabled(false);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);

                        btnGetText.setEnabled(false);

                        btnPlay.setImageResource(R.drawable.ic_pause);
                        break;
                    case 1:
                        if (mMediaPlayer != null) {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                        Toast.makeText(getActivity().getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
                        btnPlay.setImageResource(R.drawable.ic_play);

                        if (mToken!=null) {
                            btnStartRecord.setEnabled(true);
                            btnStartRecord.setBackgroundResource(R.drawable.recordshape);
                        }
                        btnGetText.setEnabled(true);

                        btnRetry.setEnabled(true);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_bg);
                        break;
                }
                mbp.changeState();
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartRecord.changeState();
                btnStartRecord.setEnabled(true);
                btnStartRecord.setImageResource(R.drawable.ic_mic_black);
                btnStartRecord.setBackgroundResource(R.drawable.recordshape);
                btnRetry.setEnabled(false);
                btnRetry.setBackgroundResource(R.drawable.play_retry_disable);
                btnPlay.setEnabled(false);
                btnPlay.setBackgroundResource(R.drawable.play_retry_disable);
            }
        });

        return view;
    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + mId + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdir();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
        int i = recorder.getState();
        if (i==1)
            recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecord Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile() {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        if (null != os) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;

            int i = recorder.getState();
            if (i==1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(), getFilename());
        deleteTempFile();
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.d("File size: ", ""+ totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private void jsonParse() {
        String url = main_url + "/text/category/" + mCatId + "/random";
        mTextView.setText("");
        circleBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("resp");
                    mText = jsonObject.getString("text");
                    mId = jsonObject.getString("id");
                    mTextView.setText(mText);
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

    private void uploadVoice(final String voicePath) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String url = main_url + "/upload/voice" + "/" + mId;
                File file = new File(voicePath);
                Log.d("Upload --","File name: " + file.getName());

                RequestBody file_body = RequestBody.create(MediaType.parse("data"),file);
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("data", file.getName(), file_body)
                        .build();
                Log.d("Upload --", file.getName());
                Log.d("Upload --","Request body generated");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .addHeader("Authorization-Key", "812f2448624c42899fbf794f54f591f9")
                        .addHeader( "Authorization", "Bearer " + mToken)
                        .post(body)
                        .build();
                try {
                    String code = "";
                    okhttp3.Response response = client.newCall(request).execute();
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        code = jsonObject.getString("code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (code.equals("200")) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity().getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d("Upload --","Successful ");
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                        Log.d("Upload --", "Fail");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void disableButton() {
        btnGetText.setEnabled(true);
        btnStartRecord.setEnabled(false);
        btnStartRecord.setBackgroundResource(R.drawable.record_shape_disable);
        btnPlay.setEnabled(false);
        btnPlay.setBackgroundResource(R.drawable.play_retry_disable);
        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);
        btnRetry.setEnabled(false);
    }
}