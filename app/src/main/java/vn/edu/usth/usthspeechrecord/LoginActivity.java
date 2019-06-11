package vn.edu.usth.usthspeechrecord;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class LoginActivity extends AppCompatActivity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mWebView = findViewById(R.id.login_web);

        mWebView.setWebViewClient(new WebViewClient() {
            boolean flag = false;
            @Override
            public WebResourceRequest shouldInterceptUrlLoading(WebView view, String url) {
                if (flag) {
                    URL aURL = new URL(url);
                    try {
                        URLConnection conn = aURL.openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        String input = is.toString();
                        Log.d("Webview token", input);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return  true;
                }
                return false;
            }

            @Override
            public void onPageFinished (WebView view, String url) {
                if (url.contains("login")) {
                    flag = true;
                }
            }
        });


        String url = "https://eid.itrithuc.vn/auth/realms/eid/protocol/openid-connect/auth?response_type=id_token%20token&redirect_uri=https://voiceviet.itrithuc.vn/eid&scope=openid%20profile%20email%20api&client_id=voiceviet&nonce=9jqwa&kc_locale=vi";
        mWebView.loadUrl(url);
    }
}
