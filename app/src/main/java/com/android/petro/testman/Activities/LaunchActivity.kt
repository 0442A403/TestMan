package com.android.petro.testman.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.android.petro.testman.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        val preferences = getSharedPreferences("AppPref", Context.MODE_PRIVATE)
        if (preferences!!.getLong("Last Opening", 0) + 1 < System.currentTimeMillis()
                && preferences.getString("Token", "").isNotEmpty()) {
            val networkThread = NetworkThread()

            val dialog = ProgressDialog(this)
            dialog.setCancelable(false)
            dialog.setMessage("Подождите")
            dialog.show()

            networkThread.start()
            networkThread.join()

            dialog.dismiss()

            Toast.makeText(this, Uri.parse(networkThread.redirectedURL.toString())
                    .getQueryParameter("access_token"), Toast.LENGTH_SHORT).show()

            val editor = getSharedPreferences("AppPref", Context.MODE_PRIVATE).edit()
            editor.putString("Token",
                    Uri.parse(networkThread.redirectedURL.toString())
                            .getQueryParameter("access_token"))
            editor.apply()
            startActivity(Intent(this, BaseActivity::class.java))
            finish()
        }
        else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private  class NetworkThread: Thread() {
        var redirectedURL: String? = null
        override fun run() {
            val url = "http://oauth.vk.com/authorize?client_id=6046576&redirect_uri=https://oauth.vk.com/blank.html&response_type=token"
            val conn = URL(url).openConnection()
            conn.connect()
            val inputS = conn.getInputStream()
            inputS.close()

            redirectedURL = conn.url.toString().replaceFirst("#", "/?")
            Log.d("Debugging", "done")

            val client = OkHttpClient()
                    .newBuilder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            redirectedURL = client.newCall(Request.Builder().url(url).build()).execute().request().url().toString()
        }
    }
}
