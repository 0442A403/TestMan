package com.android.petro.testman.Fragments

import android.app.ProgressDialog
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.android.petro.testman.R
import com.android.petro.testman.Support.DataBase
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.test_item.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*

/**
 * Created by petro on 11.06.2017.
 * Fragment for view available tests.
 */

class SearchFragment : Fragment() {

    private val testData: ArrayList<Test> = ArrayList()
    private val relevantTestsData: ArrayList<Test> = ArrayList()
    private var adapter: TestAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_search, container, false)
        adapter = TestAdapter()

        view.search_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                relevantTestsData.clear()
                if (s!!.isNotEmpty()) {
                    for (word in s.split(" "))
                        for (test in testData)
                            if (!relevantTestsData.contains(test)
                                    && (test.author.contains(word, true)
                                    || test.name.contains(word, true)))
                                relevantTestsData.add(test)
                }
                else {
                    for (test in testData)
                        relevantTestsData.add(0, test)
                }
                adapter!!.notifyDataSetChanged()
            }
        })

//        val dataBase: SQLiteDatabase = DataBase(context).readableDatabase
//        val cursor: Cursor = dataBase.rawQuery("select * from tests", null)
//        if (cursor.moveToFirst()) {
//            do {
//                adapter.testData.add(0, Test(
//                        cursor.getString(
//                                cursor.getColumnIndex("name")),
//                        cursor.getString(
//                                cursor.getColumnIndex("author")),
//                        null))
//                adapter.relevantTestsData.add(0, Test(
//                        cursor.getString(
//                                cursor.getColumnIndex("name")),
//                        cursor.getString(
//                                cursor.getColumnIndex("author")),
//                        null))
//            } while (cursor.moveToNext())
//        }
//        cursor.close()

        view.task_recycle_view.adapter = adapter
        view.task_recycle_view.layoutManager = LinearLayoutManager(activity)

        updateData()

        return view
    }

    private fun updateData() {
        GetData(activity).execute()
    }

    private fun onUpdatedData(array: JSONArray) {
        testData.clear()
        relevantTestsData.clear()
        for (i in 0..array.length()-1) {
            val jsonTest = array.getJSONObject(i)
            val test = Test(jsonTest.getInt("id"),
                    jsonTest.getString("name"),
                    jsonTest.getString("author"),
                    null)
            testData.add(0, test)
            relevantTestsData.add(0, test)
            adapter!!.notifyDataSetChanged()
        }
    }

    private class TestHolder(view: View) : RecyclerView.ViewHolder(view) {
        val testName = view.test_item_name
        val testAuthor = view.test_item_description
        val testIcon = view.test_item_icon
    }


    inner private class TestAdapter: RecyclerView.Adapter<TestHolder>() {
        override fun onBindViewHolder(holder: TestHolder?, position: Int) {
            holder!!.testName.text = relevantTestsData.get(position).name
            holder.testAuthor.text = relevantTestsData.get(position).author
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TestHolder {
            return TestHolder(
                    LayoutInflater.from(parent!!.context)
                            .inflate(R.layout.test_item, parent, false))
        }

        override fun getItemCount(): Int {
            return relevantTestsData.size
        }

    }

    class Test(val id: Int,
               val name: String,
               val author: String,
               val icon: Bitmap?)

    private inner class GetData(context: Context) : AsyncTask<Void, Void, Void>() {
        private var answer: JSONArray? = null
        private var dialog = ProgressDialog(context)
        override fun onPreExecute() {
            super.onPreExecute()
            dialog.setCancelable(false)
            dialog.setMessage("Подождите")
            dialog.show()
        }

        override fun doInBackground(vararg params: Void): Void? {
            try {
                answer = JSONArray(
                        OkHttpClient()
                                .newCall(Request.Builder()
                                        .url("https://testman-o442a4o3.c9users.io/get_all_tests")
                                        .get()
                                        .build())
                                .execute().body().string())
                Log.v("ServerResponse", answer.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            dialog.dismiss()
            onUpdatedData(answer!!)
        }
    }
}
