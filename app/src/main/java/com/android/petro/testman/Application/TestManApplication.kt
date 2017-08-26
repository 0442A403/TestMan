package com.android.petro.testman.Application

import android.app.Application
import com.vk.sdk.VKSdk

/**
 *Extension of Application for VK initializing
 * Created by petro on 05.08.2017.
 */
class TestManApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        VKSdk.initialize(this)
//        startActivity(
//                Intent(this, LaunchActivity::class.java)
//                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }
}