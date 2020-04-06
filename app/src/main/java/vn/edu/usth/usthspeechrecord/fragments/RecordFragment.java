package vn.edu.usth.usthspeechrecord.fragments;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import vn.edu.usth.usthspeechrecord.adapters.MainCategoryAdapter;
import vn.edu.usth.usthspeechrecord.models.MainCategory;
import vn.edu.usth.usthspeechrecord.views.MediaPlayButton;
import vn.edu.usth.usthspeechrecord.R;
import vn.edu.usth.usthspeechrecord.views.StateButton;
import vn.edu.usth.usthspeechrecord.adapters.CategoryAdapter;
import vn.edu.usth.usthspeechrecord.models.Category;
import vn.edu.usth.usthspeechrecord.utils.VolleySingleton;

import static android.content.ContentValues.TAG;


public class RecordFragment extends Fragment {
    private static final String main_url = "https://voiceviet.itrithuc.vn/api/v1";

    private RequestQueue mQueue;
    private Button btnGetText;
    private ImageButton btnRetry;
    private StateButton btnStartRecord;
    private MediaPlayButton btnPlay;
    private Spinner btnDialog,btnMainCategory;
    private TextView mTextView;
    private String pathSave = "";
    private ProgressBar circleBar;

    private MediaPlayer mMediaPlayer;
    private String mText = "";
    private String mId = "";
    private String mToken = null;
    private Boolean checkToken = false;
    private int mainCatId = 1;
    private int mCatId = 0;
    private ArrayList<Category> mCategories = new ArrayList<>();
    private ArrayList<MainCategory> mainListCategories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private MainCategoryAdapter mainCategoryAdapter;

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
        mQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();
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

        mainCategoryAdapter = new MainCategoryAdapter(getActivity(), R.layout.categories_item, mainListCategories );
        categoryAdapter = new CategoryAdapter(getActivity(), R.layout.categories_item, mCategories);
//        getCategory();
        getAllCategory();

        btnDialog = view.findViewById(R.id.btn_dialog);
        btnMainCategory = view.findViewById(R.id.btn_dialog_main);
        btnDialog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCatId = mCategories.get(position).getCatNum();
                Log.d("cat1:", mCatId+mCategories.get(position).getCatName());
                jsonParse();
                btnStartRecord.setEnabled(true);
                btnStartRecord.setBackgroundResource(R.drawable.circle_gradient);
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
        btnMainCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mainCatId = mainListCategories.get(position).getCatNum();
                Log.e(TAG, "onItemSelected: "+mainCatId );
                Log.d("cat1:", position+mainListCategories.get(position).getName());
                getCategory();
                btnStartRecord.setEnabled(true);
                btnStartRecord.setBackgroundResource(R.drawable.circle_gradient);
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
        btnMainCategory.setAdapter(mainCategoryAdapter);

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
//                btnStartRecord.setBackgroundResource(R.drawable.recordshape);
            }
        });

        btnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (btnStartRecord.getState()) {
                    case 0:
                        pathSave = getFilename();
                        startRecording();
                        Toast.makeText(getActivity(), getString(R.string.recording_now), Toast.LENGTH_SHORT).show();
                        btnStartRecord.setImageResource(R.drawable.ic_mic_off_black_24dp);

                        btnRetry.setEnabled(false);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);
                        btnStartRecord.changeState();
                        break;
                    case 1:
                        stopRecording();
                        Toast.makeText(getActivity(), getString(R.string.stop_recording), Toast.LENGTH_SHORT).show();
                        btnStartRecord.setImageResource(R.drawable.ic_file_upload_black_24dp);
                        btnGetText.setEnabled(false);

                        btnPlay.setEnabled(true);
                        btnPlay.setBackgroundResource(R.drawable.circle_gradient);

                        btnRetry.setEnabled(true);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_bg);
                        btnStartRecord.changeState();
                        if (!checkToken) {
                            btnStartRecord.setEnabled(false);
//                            btnStartRecord.setBackgroundResource(R.drawable.record_shape_disable);
                            Toast.makeText(getActivity(), getString(R.string.need_to_be_looged_in), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        uploadVoice(pathSave);
                        btnStartRecord.setImageResource(R.drawable.ic_mic_black);

                        btnRetry.setEnabled(false);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);

                        btnPlay.setEnabled(false);
                        btnPlay.setBackgroundResource(R.drawable.circle_gradient);
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
                        //add audio player Listener
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if (mMediaPlayer != null) {
                                    mMediaPlayer.stop();
                                    mMediaPlayer.release();
                                }
                                Toast.makeText(getActivity(), getString(R.string.has_stopped), Toast.LENGTH_SHORT).show();
                                btnPlay.setImageResource(R.drawable.ic_play);

                                if (mToken!=null) {
                                    btnStartRecord.setEnabled(true);
                                    btnStartRecord.setBackgroundResource(R.drawable.circle_gradient);
                                }
                                btnGetText.setEnabled(true);

                                btnRetry.setEnabled(true);
                                btnRetry.setBackgroundResource(R.drawable.play_retry_bg);
                                mbp.changeState();
                            }
                        });
                        try {
                            Log.d("path", pathSave);
                            mMediaPlayer.setDataSource(pathSave);
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mMediaPlayer.start();
                        Toast.makeText(getActivity(), getString(R.string.running_audio_recording), Toast.LENGTH_SHORT).show();

                        btnStartRecord.setEnabled(false);
                        btnStartRecord.setBackgroundResource(R.drawable.play_retry_disable);

                        btnRetry.setEnabled(false);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);

                        btnGetText.setEnabled(false);

                        btnPlay.setImageResource(R.drawable.ic_pause);
                        mbp.changeState();
                        break;
                    case 1:
                        if (mMediaPlayer != null) {
                            mMediaPlayer.stop();
                            mMediaPlayer.release();
                        }
                        Toast.makeText(getActivity(), getString(R.string.has_stopped), Toast.LENGTH_SHORT).show();
                        btnPlay.setImageResource(R.drawable.ic_play);

                        if (mToken!=null) {
                            btnStartRecord.setEnabled(true);
                            btnStartRecord.setBackgroundResource(R.drawable.circle_gradient);
                        }
                        btnGetText.setEnabled(true);

                        btnRetry.setEnabled(true);
                        btnRetry.setBackgroundResource(R.drawable.play_retry_bg);
                        mbp.changeState();
                        break;
                }
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartRecord.changeState();
                btnStartRecord.setEnabled(true);
                btnStartRecord.setImageResource(R.drawable.ic_mic_black);
                btnStartRecord.setBackgroundResource(R.drawable.circle_gradient);
                btnRetry.setEnabled(false);
                btnRetry.setBackgroundResource(R.drawable.play_retry_disable);
                btnPlay.setEnabled(false);
                btnPlay.setBackgroundResource(R.drawable.circle_gradient);
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
        Log.e(TAG, "jsonParse: "+url );
        mTextView.setText("");
        circleBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: "+response.toString() );
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
                Log.e(TAG, "onErrorResponse: "+url );
                Toast.makeText(getActivity(), "An error occured while sending request", Toast.LENGTH_SHORT).show();
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
        mCategories=new ArrayList<>();

        if (mainListCategories.size()>0){
            List<Category> mainCategory =getCurrentSubcategories();
            if (mainCategory.size()>0){
                for (int i = 0; i< mainCategory.size(); i++){
                    mCategories.add(mainCategory.get(i));
                    Log.e(TAG, "getSubCategory: name: "+mainCategory.get(i).getCatNum()+" "+mainCategory.get(i).getCatName() );
                    categoryAdapter.setArrayList(mCategories);
                    categoryAdapter.notifyDataSetChanged();
                    mCatId=1;
                }
                jsonParse();
            }
        }
//        Log.e(TAG, "getCategory: " );
//        String url = main_url + "/domain/"+mainCatId;
//        Log.e(TAG, "getCategory: "+url );
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Log.e(TAG, "onResponse: getCategory "+url );
//                Log.e(TAG, "onResponse: getCategory Response "+response.toString() );
//                try {
//                    JSONArray categories = response.getJSONArray("resp").getJSONObject(0).getJSONArray("categories");
//                    for (int i=0; i < categories.length(); i++) {
//                        JSONObject cate = categories.getJSONObject(i);
//                        String catName = cate.getString("name");
//                        String id = cate.getString("id");
//                        Category newCat = new Category(catName, Integer.valueOf(id));
//                        mCategories.add(newCat);
//                        categoryAdapter.notifyDataSetChanged();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                Log.e(TAG, "onResponseError: getCategory "+error.getMessage() );
//                Log.e(TAG, "onResponseError: getCategory "+url );
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization-Key", "812f2448624c42899fbf794f54f591f9");
//                headers.put("accept", "application/json");
//                return headers;
//            }
//        };
//        mQueue.add(request);
    }
    private List<Category> getCurrentSubcategories(){
        List<Category> mainCategory =new ArrayList<>();
        for (int i=0;i<mainListCategories.size();i++){
            if (mainListCategories.get(i).getCatNum()==mainCatId){
                mainCategory= mainListCategories.get(i).getCategoryList();
            }
        }
        return mainCategory;

    }
    private void getAllCategory() {
        Log.e(TAG, "getAllCategory: " );
        String url = main_url + "/domain/list/15/1";


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: getAllCategory "+url );
                Log.e(TAG, "onResponse: getAllCategory Response "+response.toString() );
                try {
                    JSONArray mainCategories = response.getJSONArray("resp");
                    for (int i=0; i < mainCategories.length(); i++) {
                        JSONObject cate = mainCategories.getJSONObject(i);
                        String catName = cate.getString("domainName");
                        String id = cate.getString("id");
                        JSONArray categories2 = cate.getJSONArray("categories");

                        mCategories= new ArrayList<>();
                        for (int i2=0; i2 < categories2.length(); i2++) {
                            JSONObject cate2 = categories2.getJSONObject(i2);
                            String catName2 = cate2.getString("name");
                            String id2 = cate2.getString("id");
                            String domainId = cate2.getString("domainId");
                            Category newCat2 = new Category(catName2, domainId, Integer.valueOf(id2));
                            mCategories.add(newCat2);
                        }
                        if (isAccessible(Integer.valueOf(id))){
                            MainCategory mainCategory=new MainCategory(catName,Integer.valueOf(id),mCategories);
                            mainListCategories.add(mainCategory);//mainListCategories
                            mainCategoryAdapter.notifyDataSetChanged();
                        }
                    }
                    getCategory();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "onResponseError: getAllCategory "+error.getMessage() );
                Log.e(TAG, "onResponseError: getAllCategory "+url );
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
    private boolean isAccessible(Integer valueOf) {
        int[] noAccessible={2,4,5,6,7,12};
        for (int value : noAccessible) {
            if (valueOf == value) {
                return false;
            }
        }
        return true;
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
                                Toast.makeText(getActivity(), getString(R.string.upload_successfull), Toast.LENGTH_SHORT).show();
                            }
                        });
                        Log.d("Upload --","Successful ");
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
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
        btnStartRecord.setBackgroundResource(R.drawable.circle_gradient);
        btnPlay.setEnabled(false);
        btnPlay.setBackgroundResource(R.drawable.play_retry_disable);
        btnRetry.setBackgroundResource(R.drawable.play_retry_disable);
        btnRetry.setEnabled(false);
    }
}