package com.android.petro.testman.Support.TestData

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.android.petro.testman.R
import com.android.petro.testman.Support.Listeners.*
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


/**
 * General class for holding information about test
 */

class Test constructor(val settings: SettingsData,
                       val tasks: TaskData,
                       context: Context) {
    var author: Int = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE).getInt("VKId", -1)
    fun save(callBack: OnTestSavedListener, context: Context) {
        val dialog = ProgressDialog(context)
        dialog.setMessage("Сохраняем тест")
        dialog.setTitle("TestMan")
        dialog.setCancelable(false)
        object : AsyncTask<Void, Void, Void>() {
            override fun onPreExecute() {
                super.onPreExecute()
                dialog.show()
            }

            override fun doInBackground(vararg params: Void?): Void? {
                context.getSharedPreferences("AppPref", Context.MODE_PRIVATE).edit()
                        .putInt("Five begins", settings.fivebegins)
                        .putInt("Four begins", settings.fourbegins)
                        .putInt("Three begins", settings.threebegins)
                        .putInt("Time", settings.time)
                        .putBoolean("Show wrongs", settings.showwrongs == "t")
                        .apply()

                val list: ArrayList<String> = ArrayList()
                for (task in tasks.tasks) {
                    val obj: String? = Gson().toJson(task)
                    list.add(obj!!)
                }
                val formBody = FormBody.Builder()
                        .add("name", settings.name)
                        .add("author", author.toString())
                        .add("fivebegins", settings.fivebegins.toString())
                        .add("fourbegins", settings.fourbegins.toString())
                        .add("threebegins", settings.threebegins.toString())
                        .add("showwrongs", settings.showwrongs)
                        .add("time", settings.time.toString())
                        .add("tasks", JSONArray(list).toString())
                        .build()

                val request = Request.Builder()
                        .url(context.getString(R.string.server_url) + context.getString(R.string.add_test))
                        .post(formBody)
                        .build()

                val response = OkHttpClient().newCall(request).execute()
                Log.v("TestManNetwork", response.body().string())

                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                dialog.hide()
                callBack.onTestSaved()
            }
        }.execute()
    }

    fun update(testId: Int, context: Context, callback: OnTestUpdatedListener) {
        val dialog = ProgressDialog(context)
        dialog.setMessage("Сохраняем тест")
        dialog.setTitle("TestMan")
        dialog.setCancelable(false)
        object : AsyncTask<Void, Void, Void>() {
            override fun onPreExecute() {
                super.onPreExecute()
                dialog.show()
            }

            override fun doInBackground(vararg params: Void?): Void? {
                val list: ArrayList<String> = ArrayList()
                tasks.tasks
                        .map { Gson().toJson(it) }
                        .mapTo(list) { it!! }
                val formBody = FormBody.Builder()
                        .add("id", testId.toString())
                        .add("name", settings.name)
                        .add("author", author.toString())
                        .add("fivebegins", settings.fivebegins.toString())
                        .add("fourbegins", settings.fourbegins.toString())
                        .add("threebegins", settings.threebegins.toString())
                        .add("showwrongs", settings.showwrongs)
                        .add("time", settings.time.toString())
                        .add("tasks", JSONArray(list).toString())
                        .build()

                val request = Request.Builder()
                        .url(context.getString(R.string.server_url) + context.getString(R.string.update_test))
                        .post(formBody)
                        .build()

                val response = OkHttpClient().newCall(request).execute()
                Log.v("TestManNetwork", response.body().string())

                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                dialog.hide()
                callback.onTestUpdated()
            }
        }.execute()
    }

    companion object {
        fun get(id: Int, context: Context, callback: OnTestReceivedListener) {
            object : AsyncTask<Void, Void, ServerResponse>() {
                val dialog = ProgressDialog(context)
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setTitle("TestMan")
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
                            .add("forsolving", true.toString())
                            .build()

                    val request = Request.Builder()
                            .url(context.getString(R.string.server_url) + context.getString(R.string.get_test_by_id))
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("TestManNetwork", "Received test: $responseString")
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
                    for (i in 0 until tasksJSONArray.length())
                        tasksArray.add(gson.fromJson(tasksJSONArray.getString(i), TaskClass::class.java))
                    val tasks = TaskData(tasksArray)
                    callback.onTestReceived(Test(settings, tasks, context), result.id)
                }
            }.execute()
        }

        fun get(context: Context, callback: OnTestReceivedListener, answer: Answer) {
            object : AsyncTask<Void, Void, Test>() {
                val dialog = ProgressDialog(context)
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setTitle("TestMan")
                    dialog.setMessage("Загружаем тест")
                    dialog.setCancelable(false)
                    dialog.show()
                }

                override fun doInBackground(vararg params: Void?): Test {
                    val formBody = FormBody.Builder()
                            .add("id", answer.test.toString())
                            .build()

                    val request = Request.Builder()
                            .url(context.getString(R.string.server_url) + context.getString(R.string.get_test_by_id))
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("TestManNetwork", "Received test: $responseString")
                    val gson = Gson()
                    val settings = gson.fromJson(responseString, SettingsData::class.java)
                    val jsonArray = JSONArray(JSONObject(responseString).getString("tasks"))
                    val array = ArrayList<TaskClass>()
                    for (i in 0 until jsonArray.length())
                        array.add(gson.fromJson(jsonArray.getString(i), TaskClass::class.java))
                    return Test(settings, TaskData(array), context)
                }

                override fun onPostExecute(result: Test) {
                    super.onPostExecute(result)
                    dialog.hide()
                    callback.onTestReceived(result, answer)
                }
            }.execute()
        }

        fun get(context: Context, callback: OnTestReceivedListener, testId: Int) {
            object : AsyncTask<Void, Void, Test>() {
                val dialog = ProgressDialog(context)
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setTitle("TestMan")
                    dialog.setMessage("Загружаем тест")
                    dialog.setCancelable(false)
                    dialog.show()
                }

                override fun doInBackground(vararg params: Void?): Test {
                    val formBody = FormBody.Builder()
                            .add("id", testId.toString())
                            .build()

                    val request = Request.Builder()
                            .url(context.getString(R.string.server_url) + context.getString(R.string.get_test_by_id))
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("TestManNetwork", "Received test: $responseString")
                    val gson = Gson()
                    val settings = gson.fromJson(responseString, SettingsData::class.java)
                    val jsonArray = JSONArray(JSONObject(responseString).getString("tasks"))
                    val array = ArrayList<TaskClass>()
                    for (i in 0 until jsonArray.length())
                        array.add(gson.fromJson(jsonArray.getString(i), TaskClass::class.java))
                    return Test(settings, TaskData(array), context)
                }

                override fun onPostExecute(result: Test) {
                    super.onPostExecute(result)
                    dialog.hide()
                    callback.onTestReceived(result)
                }
            }.execute()
        }

        fun delete(testId: Int, context: Context, callback: OnTestDeletedListener) {
            object : AsyncTask<Void, Void, Void>() {
                val dialog = ProgressDialog(context)
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setTitle("TestMan")
                    dialog.setMessage("Удаляем тест")
                    dialog.setCancelable(false)
                    dialog.show()
                }

                override fun doInBackground(vararg params: Void?): Void? {
                    val formBody = FormBody.Builder()
                            .add("id", testId.toString())
                            .build()

                    val request = Request.Builder()
                            .url(context.getString(R.string.server_url) + context.getString(R.string.delete_test))
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("TestManNetwork", "Received test: $responseString")
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    dialog.hide()
                    callback.onTestDeleted()
                }
            }.execute()
        }

        fun clearAnswers(testId: Int, callback: OnAnswerClearedListener, context: Context) {
            object: AsyncTask<Void, Void, Void>() {
                val dialog = ProgressDialog(context)
                override fun onPreExecute() {
                    super.onPreExecute()
                    dialog.setTitle("TestMan")
                    dialog.setMessage("Удаляем ответы")
                    dialog.setCancelable(false)
                    dialog.show()
                }

                override fun doInBackground(vararg params: Void?): Void? {
                    val formBody = FormBody.Builder()
                            .add("id", testId.toString())
                            .build()

                    val request = Request.Builder()
                            .url(context.getString(R.string.server_url) + context.getString(R.string.clear_answers_by_id))
                            .post(formBody)
                            .build()

                    val responseString = OkHttpClient().newCall(request).execute().body().string()
                    Log.i("TestManNetwork", "Received test: $responseString")
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    dialog.hide()
                    callback.onAnswerCleared()
                }
            }.execute()
        }
    }

    private class ServerResponse(val response: String,
                                 val id: Int)
}