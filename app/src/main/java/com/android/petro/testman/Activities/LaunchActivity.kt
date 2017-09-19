package com.android.petro.testman.Activities

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.petro.testman.R
import com.pawegio.kandroid.startActivity
import com.vk.sdk.VKSdk


class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        if (isNetworkAvailable() && !VKSdk.isLoggedIn())
            startActivity<LoginActivity>()
        else
            startActivity<BaseActivity>()
        finish()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
    }
}
