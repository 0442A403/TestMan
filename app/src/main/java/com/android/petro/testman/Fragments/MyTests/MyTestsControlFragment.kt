package com.android.petro.testman.Fragments.MyTests

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.petro.testman.R
import com.android.petro.testman.Support.Listeners.*
import com.android.petro.testman.Support.Other.AnswerItem
import com.android.petro.testman.Support.Other.TestItem
import com.google.gson.Gson
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

/**
 * Fragment for view tests created by current user
 */

class MyTestsControlFragment:
        Fragment(),
        OnTestSelectedListener,
        OnDataReceivedListener,
        OnBackPressedListener,
        OnTestDeletedListener,
        OnAnswerClearedListener{

    private val testFragment: MyTestsFragment = MyTestsFragment(this, this, this)
    private var mActualWindow: MActualWindow? = null

    init {
        testFragment.retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        changeFragment(MActualWindow.TESTS, null)
        GetData(this, context).execute()
        return inflater!!.inflate(R.layout.fragment_my_tests, container, false)
    }

    private fun changeFragment(window: MActualWindow, fragment: Fragment?) {
        mActualWindow = window
        when (window) {
            MActualWindow.TESTS -> {
                GetData(this, context).execute()
                childFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout__my_tests, testFragment).commit()
            }
            else -> {
                childFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout__my_tests, fragment!!).commit()
            }
        }
    }

    override fun onTestSelected(fragment: Fragment) {
        changeFragment(MActualWindow.ANSWERS, fragment)
    }

    override fun onDataReceived(data: ArrayList<TestItem>) {
        testFragment.updateData(data)
    }

    override fun onBackPressed(): Boolean {
        if (mActualWindow == MActualWindow.TESTS)
            return true
        changeFragment(MActualWindow.TESTS, null)
        return false
    }

    override fun onTestDeleted() {
        changeFragment(MActualWindow.TESTS, null)
    }

    override fun onAnswerCleared() {
        changeFragment(MActualWindow.TESTS, null)
    }

    private enum class MActualWindow {
        TESTS, ANSWERS
    }

    private class GetData(private val callback: OnDataReceivedListener,
                          private val context: Context): AsyncTask<Int, Void, ArrayList<TestItem>>() {
        private val dialog: ProgressDialog = ProgressDialog(context)
        override fun onPreExecute() {
            super.onPreExecute()
            dialog.setTitle("TestMan")
            dialog.setMessage("Загружаем тесты")
            dialog.setCancelable(false)
            dialog.show()
        }

        override fun doInBackground(vararg params: Int?): ArrayList<TestItem>? {
            val formBody = FormBody.Builder()
                    .add("author",
                            context.getSharedPreferences("AppPref", Context.MODE_PRIVATE)
                                    .getInt("VKId", -1).toString())
                    .build()

            val request = Request.Builder()
                    .url(context.getString(R.string.server_url) + context.getString(R.string.get_tests_by_author))
                    .post(formBody)
                    .build()

            val responseString = OkHttpClient().newCall(request).execute().body().string()
            Log.i("TestManNetwork", responseString)

            val array = ArrayList<TestItem>()
            try {
                val jsonArray = JSONArray(responseString)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val answerArray = ArrayList<AnswerItem>()
                    try {
                        val jsonAnswerArray = jsonObject.getJSONArray("answers")
                        val gson = Gson()
                        (0 until jsonAnswerArray.length())
                                .mapTo(answerArray) {
                                    gson.fromJson(
                                            jsonAnswerArray.getJSONObject(it).toString(),
                                            AnswerItem::class.java)
                                }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    array.add(TestItem(
                            jsonObject.getJSONObject("test").getInt("id"),
                            jsonObject.getJSONObject("test").getString("name"),
                            jsonObject.getJSONObject("test").getInt("date").toLong(),
                            answerArray
                    ))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return array
        }

        override fun onPostExecute(result: ArrayList<TestItem>) {
            super.onPostExecute(result)
            dialog.hide()
            result.reverse()
            callback.onDataReceived(result)
        }
    }
}
