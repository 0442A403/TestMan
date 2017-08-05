package com.android.petro.testman.Support

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by petro on 27.07.2017.
 * General class for storing answer's information
 */

class Answer(val author: String,
             val answer: ArrayList<Any>,
             val time: Int) {

    fun sendData(testId : Int, context : Context, callback: OnAnswerSave) {
        val dialog = ProgressDialog(context)
        object : AsyncTask<Void, Void, String>() {
            override fun onPreExecute() {
                super.onPreExecute()
                dialog.setMessage("Сохраняем ответ")
                dialog.setCancelable(false)
                dialog.show()
            }

            override fun doInBackground(vararg params: Void): String {
                val body = FormBody.Builder()
                        .add("test", testId.toString())
                        .add("author", author)
                        .add("answer", answer.toString())
                        .add("time", time.toString())
                        .build()

                Log.d("Answer on test", answer.toString())

                val request = Request.Builder()
                        .url("https://testman-o442a4o3.c9users.io/add_answer/")
                        .post(body)
                        .build()

                return OkHttpClient().newCall(request).execute().body().string()
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.v("HttpResponse", result)
                dialog.hide()
                callback.onAnswerSaved()
            }
        }.execute()
    }
}
