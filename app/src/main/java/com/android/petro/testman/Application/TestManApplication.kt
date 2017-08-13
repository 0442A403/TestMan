package com.android.petro.testman.Application

import android.app.Application
import android.content.Intent
import android.util.Log
import com.android.petro.testman.Activities.LaunchActivity
import com.vk.sdk.VKSdk
import com.vk.sdk.util.VKUtil
import java.util.*

/**
 *Extension of Application for VK initializing
 * Created by petro on 05.08.2017.
 */
class TestManApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("lol", Arrays.asList(*VKUtil.getCertificateFingerprint(this, this.packageName)).toString())
        VKSdk.initialize(this)
        startActivity(
                Intent(this, LaunchActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))

    }
}