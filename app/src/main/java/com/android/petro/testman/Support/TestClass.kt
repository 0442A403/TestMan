package com.android.petro.testman.Support

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import org.json.JSONArray
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


/**
 * General class for holding information about test
 */

class TestClass(
        val settings: SettingsData,
        val tasks: TasksData,
        context: Context) {

    val SERVER_URL = "https://testman-o442a4o3.c9users.io/"
    val ADD_TEST = "add_test/"
    val GET_TEST = "get_test/"
    val author = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE).getString("author", null)

    fun save(callBack: CreateCallBack) {
        object: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                /*val json : JSONObject = JSONObject()
                json.put("name", settings.name)
                json.put("fiveBegins", settings.fiveBegins)
                json.put("fourBegins", settings.fourBegins)
                json.put("threeBegins", settings.threeBegins)
                json.put("showWrongs", settings.showWrongs)
                json.put("time", settings.time)
                json.put("author",
                        context.getSharedPreferences("AppPref", Context.MODE_PRIVATE)
                                .getString("author", null))*/

                val list: ArrayList<String> = ArrayList()
                for (task in tasks.tasks) {
                    val obj: String? = Gson().toJson(task)
                    list.add(obj!!)
                }
                Log.v("jsonStrings", JSONArray(list).toString())

                /*json.put("tasks", JSONArray(list))
                val content: ContentValues = ContentValues()
                content.put("name", settings.name)
                content.put("fiveBegins", settings.fiveBegins)
                content.put("fourBegins", settings.fourBegins)
                content.put("threeBegins", settings.threeBegins)
                content.put("showWrongs", settings.showWrongs)
                content.put("time", settings.time)
                content.put("tasks", JSONArray(list).toString())
                content.put("author",
                        context.getSharedPreferences("AppPref", Context.MODE_PRIVATE)
                                .getString("author", null))

                val writableDataBase = DataBase(context).writableDatabase
                writableDataBase.insert("tests", null, content)
                writableDataBase.close()*/

                val formBody = FormBody.Builder()
                        .add("name", settings.name)
                        .add("author", author)
                        .add("fiveBegins", settings.fiveBegins.toString())
                        .add("fourBegins", settings.fourBegins.toString())
                        .add("threeBegins", settings.threeBegins.toString())
                        .add("showWrongs", settings.showWrongs.toString())
                        .add("time", settings.time.toString())
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
                callBack.onSaved()
            }
        }.execute();
    }

    fun get(id: Int) {

    }

//    fun edit() {}

}