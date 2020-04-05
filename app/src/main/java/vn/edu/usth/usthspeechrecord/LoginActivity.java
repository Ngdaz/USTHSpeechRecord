package vn.edu.usth.usthspeechrecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    WebView mWebView;
    private static final String TAG = "LoginAcivity";
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mWebView = findViewById(R.id.login_web);
        clearCache(this, 0);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false);

        clearCookies(this);

        mWebView.clearCache(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();//ignore
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.i("url",url);
                if (url.contains("api/v1")) {
                    int begin = url.indexOf("access_token");
                    int end = url.indexOf("&token_type");
                    String token = url.substring(begin+13, end);
                    Log.e(TAG, "shouldOverrideUrlLoading: Token "+token );
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("TOKEN", token);
                    Log.d("token", token);
                    startActivity(intent);
                    Toast.makeText(getApplication().getApplicationContext(), "Login successfully", Toast.LENGTH_SHORT).show();
                }
                return false;

            }

        });


        Double nonce = Math.random();
        String convert  = Long.toString(Double.doubleToLongBits(nonce), 36).substring(7);

        //this is the URL
        String url = "https://eid.itrithuc.vn/auth/realms/eid/protocol/openid-connect/auth?response_type=id_token%20token&redirect_uri=https://voiceviet.itrithuc.vn/eid&scope=openid%20profile%20email%20api&client_id=voiceviet&nonce="+ convert +"kc_locale=vi";
        mWebView.loadUrl(url);// its loaded here to the webview(browser) ok
    }

    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {

                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            }
            catch(Exception e) {
                Log.e(TAG, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    public static void clearCache(final Context context, final int numDays) {
        Log.i(TAG, String.format("Starting cache prune, deleting files older than %d days", numDays));
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        Log.i(TAG, String.format("Cache pruning completed, %d files deleted", numDeletedFiles));
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }
}
