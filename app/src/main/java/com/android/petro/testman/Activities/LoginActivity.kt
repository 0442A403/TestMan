package com.android.petro.testman.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.petro.testman.R
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException

class LoginActivity : AppCompatActivity() {
    private var authPerforming = false
    private val scopes = arrayOf(VKScope.PHOTOS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_vk_icon.setOnClickListener {
            loginToVK()
        }
        login_vk_text.setOnClickListener {
            loginToVK()
        }
    }

    fun loginToVK() {
        if (!authPerforming) {
            VKSdk.login(this, *scopes)
            authPerforming = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken) {
                VKApi.users().get().executeWithListener(object : VKRequest.VKRequestListener() {
                    override fun onComplete(response: VKResponse?) {
                        // Пользователь успешно авторизовался
                        super.onComplete(response)
                        if (authPerforming) {
                            authPerforming = false
                            try {
                                Log.i("VKResponse", response!!.json.toString())
                                val id = response.json.getJSONArray("response").getJSONObject(0).getInt("id")
                                Log.i("VKId", id.toString())
                                getSharedPreferences("AppPref", Context.MODE_PRIVATE)
                                        .edit()
                                        .putInt("VKId", id)
                                        .apply()
                                val intent = Intent(this@LoginActivity, BaseActivity::class.java)
                                startActivity(intent)
                                finish()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
            }

            override fun onError(error: VKError) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                authPerforming = false
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
