package com.android.petro.testman;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginActivity extends AppCompatActivity {
    private final static String CLIENT_ID = "6046576";
    private final static String OAUTH_URL = "http://oauth.vk.com/authorize";
    private final static String RESPONSE_TYPE = "token";
    private final static String METHOD_URL = "https://api.vk.com/method/";
    private final static String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    private final static String GET_PROFILE_INFO = "account.getProfileInfo";

    Dialog dialog;
    WebView web;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    boolean authComplete = false;
    boolean firstAuth = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        preferences = getSharedPreferences("AppPref", MODE_PRIVATE);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.auth_dialog);
        dialog.setCancelable(true);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Подождите");
        progressDialog.setCancelable(false);

        web = (WebView) dialog.findViewById(R.id.webview);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setUserAgentString("AndroidWebView");
        web.clearCache(true);
        web.setWebViewClient(new WebViewClient() {
            String authCode;
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!progressDialog.isShowing() && !firstAuth)
                    progressDialog.show();
                firstAuth = false;
                Log.i("firstAuth", String.valueOf(progressDialog.isShowing()));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                url = url.replaceFirst("#", "/?");
                Log.d("Url", url);
                if (url.contains("access_token=") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("access_token");
                    authComplete = true;

                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("Token", authCode);
                    edit.apply();
                    if (dialog.isShowing())
                        dialog.dismiss();
                    new TokenGet().execute();
                    Toast.makeText(getApplicationContext(), "Authorization Token is: "
                            + authCode, Toast.LENGTH_SHORT).show();
                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    authComplete = true;
                    Toast.makeText(getApplicationContext(),
                            "Error Occured", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            }
        });
        web.loadUrl(OAUTH_URL + "?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=" + RESPONSE_TYPE);
    }

    public void showDialog(View view) {
        web.reload();
        if (!firstAuth)
            dialog.show();
    }

    private class TokenGet extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        private String token;
        private DefaultHttpClient httpClient;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("debugging", "onPreExecute");
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Connecting VK ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            token = preferences.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            StringBuilder object = null;
            try {

                DefaultHttpClient client = new DefaultHttpClient();
                String userRequest = METHOD_URL + GET_PROFILE_INFO + "?access_token=" + preferences.getString("Token", "");
                Log.d("userRequest", userRequest);
                HttpGet get = new HttpGet(userRequest);

//                get.setHeader("Content-Type", "application/x-www-form-urlencoded");
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();

                InputStream stream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"), 8);
                String line;
                object = new StringBuilder();
                while ((line = reader.readLine()) != null)
                    object.append(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert object != null;
            return object.toString();
        }

        @Override
        protected void onPostExecute(String str) {
            authComplete = false;
            Log.d("JSONObject", str);
            Toast.makeText(LoginActivity.this, str, Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
            Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
            intent.putExtra("JSONObject", str);
            startActivity(intent);
            finish();
        }
    }
}
