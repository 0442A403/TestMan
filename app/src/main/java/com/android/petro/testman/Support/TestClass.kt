package com.android.petro.testman.Support

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


/**
 * General class for holding information about test
 */

class TestClass constructor(settings: SettingsData,
                            tasks: TasksData,
                            context: Context) {

    val SERVER_URL = "https://testman-o442a4o3.c9users.io/"
    val ADD_TEST = "add_test/"
    var author: Int = -1
    var settings: SettingsData? = null
    var tasks: TasksData? = null

    init {
        author = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE).getInt("VKId", -1)
        this.settings = settings
        this.tasks = tasks
    }

    fun save(callBack: onTestSave) {
        object: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val list: ArrayList<String> = ArrayList()
                for (task in tasks!!.tasks) {
                    val obj: String? = Gson().toJson(task)
                    list.add(obj!!)
                }
                Log.v("jsonStrings", JSONArray(list).toString())
                val formBody = FormBody.Builder()
                        .add("name", settings!!.name)
                        .add("author", author.toString())
                        .add("fiveBegins", settings!!.fiveBegins.toString())
                        .add("fourBegins", settings!!.fourBegins.toString())
                        .add("threeBegins", settings!!.threeBegins.toString())
                        .add("showWrongs", settings!!.showWrongs.toString())
                        .add("time", settings!!.time.toString())
                        .add("tasks", JSONArray(list).toString())
                        .build()

                val request = Request.Builder()
                        .url(SERVER_URL + ADD_TEST)
                        .post(formBody)
                        .build()

                val response = OkHttpClient().newCall(request).execute()
                Log.v("HttpResponse", response.body().string())

                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                callBack.onTestSaved()
            }
        }.execute()
    }

    companion object {
        fun get(id: Int, context: Context, callback: OnTestReceive) {
            object : AsyncTask<Void, Void, ServerResponse>() {
                val dialog = ProgressDialog(context)
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setMessage("Загружаем тест")
                    dialog.setCancelable(false)
                    dialog.show()
                }

                override fun doInBackground(vararg params: Void?): ServerResponse {
                    val formBody = FormBody.Builder()
                            .add("id", id.toString())
                            .add("user",
                                    context.getSharedPreferences("AppPref", Context.MODE_PRIVATE)
                                            .getInt("VKId", -1).toString())
                            .build()

                    val request = Request.Builder()
                            .url("https://testman-o442a4o3.c9users.io/get_test_by_id/")
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("ReceivedTest", responseString)
                    val jsonObject = JSONObject(responseString)
                    val response = jsonObject.getJSONArray("test").getJSONObject(0).toString()
                    val receivedId = jsonObject.getInt("id")
                    return ServerResponse(response, receivedId)
                }

                override fun onPostExecute(result: ServerResponse) {
                    super.onPostExecute(result)
                    dialog.dismiss()
                    val gson = Gson()
                    val settings = gson.fromJson(result.response, SettingsData::class.java)
                    val tasksJSONArray = JSONArray(JSONObject(result.response).getString("tasks"))
                    val tasksArray = ArrayList<TaskClass>()
                    for (i in 0 .. tasksJSONArray.length() - 1)
                        tasksArray.add(gson.fromJson(tasksJSONArray.getString(i), TaskClass::class.java))
                    val tasks = TasksData(tasksArray)
                    callback.onTestReceived(TestClass(settings, tasks, context), result.id)
                }
            }.execute()
        }

        private class ServerResponse(val response: String,
                                     val id: Int)
    }


}