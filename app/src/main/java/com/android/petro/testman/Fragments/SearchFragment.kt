package com.android.petro.testman.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
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
import android.widget.EditText
import com.android.petro.testman.Activities.SolveActivity
import com.android.petro.testman.R
import com.android.petro.testman.Support.Dictionary
import com.android.petro.testman.Support.OnUpdatedDataListener
import com.bumptech.glide.Glide
import com.vk.sdk.api.*
import com.vk.sdk.api.methods.VKApiUsers
import com.vk.sdk.api.model.VKList
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.item_with_image.view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

/**
 * Created by petro on 11.06.2017.
 * Fragment for view available tests.
 */

class SearchFragment : Fragment(), OnUpdatedDataListener {
    private val testData: ArrayList<Test> = ArrayList()
    private val relevantTestData: ArrayList<Test> = ArrayList()
    private var adapter: TestAdapter? = null
    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_search, container, false)
        adapter = TestAdapter(relevantTestData,
                testData,
                activity,
                view.edit_text__search!!)

        val recycleView = view.recycler_view__search
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(activity)
        updateData()
        return view
    }

    private fun updateData() {
        GetData(activity, this).execute()
    }

    override fun onUpdatedData(array: JSONArray?) {
        if (array != null) {
            testData.clear()
            relevantTestData.clear()
            for (i in 0 until array.length()) {
                val jsonTest = array.getJSONObject(i)
                val test = Test(jsonTest.getInt("id"),
                        jsonTest.getString("name"),
                        jsonTest.getInt("author"),
                        jsonTest.getInt("time"))
                testData.add(0, test)
                relevantTestData.add(0, test)
            }
            adapter!!.updateData()
        }
    }

    private class TestHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val testName = view.first_field__item_with_image!!
        val testAuthor = view.second_field__item_with_image!!
        val testIcon = view.test_item_icon!!
    }


    private class TestAdapter(private val relevantTestData: ArrayList<Test>,
                              private val testData: ArrayList<Test>,
                              private val activity: Activity,
                              searchField: EditText): RecyclerView.Adapter<TestHolder>() {
        private val users: HashMap<Int, User> = HashMap()

        init {
            searchField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    relevantTestData.clear()
                    if (s!!.trim().isNotEmpty()) {
                        for (word in s.split(" "))
                            for (test in testData)
                                if (!relevantTestData.contains(test)
                                        && (users[test.authorId]!!.name.contains(word, true)
                                        || test.name.contains(word, true)))
                                    relevantTestData.add(test)
                    }
                    else {
                        for (test in testData)
                            relevantTestData.add(0, test)
                    }
                    notifyDataSetChanged()
                }
            })
        }

        override fun onBindViewHolder(holder: TestHolder?, position: Int) {
            holder!!.testName.text = relevantTestData[position].name
            holder.testAuthor.text =
                    users[relevantTestData[position].authorId]!!.name
            Glide.with(activity)
                    .load(users[relevantTestData[position].authorId]!!.photo)
                    .into(holder.testIcon)
            holder.view.setOnClickListener {
                activity.startActivityForResult(
                        Intent(activity, SolveActivity::class.java)
                                .putExtra("id", relevantTestData.get(position).testId)
                                .putExtra("time", relevantTestData.get(position).time)
                                .putExtra("name", relevantTestData.get(position).name),
                        0)
                Log.v("TestId", relevantTestData[position].testId.toString())
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TestHolder {
            return TestHolder(
                    LayoutInflater.from(parent!!.context)
                            .inflate(R.layout.item_with_image, parent, false))
        }

        override fun getItemCount(): Int = relevantTestData.size

        fun updateData() {
            val idString = StringBuilder()
            for (test in testData)
                if (!idString.contains(test.authorId.toString()))
                    idString.append("${test.authorId}, ")
            if (idString.isNotEmpty())
                idString.removeRange(idString.length - 3 until idString.length)
            val params: HashMap<String, Any> = HashMap()
            params.put(VKApiConst.USER_IDS, idString.toString())
            params.put(VKApiConst.FIELDS, VKApiConst.PHOTO)
            VKApiUsers()
                    .get(VKParameters(params))
                    .executeWithListener(
                            object : VKRequest.VKRequestListener() {
                                override fun onComplete(response: VKResponse?) {
                                    Log.v("TestManNetwork", response!!.responseString)
                                    val vkUsers = response.parsedModel as VKList<*>
                                    val dictionary = Dictionary().dictionary
                                    for (user in vkUsers) {
                                        var author = user.fields.getString("first_name") + " " + user.fields.getString("last_name")
                                        for ((key, value) in dictionary)
                                            if (author.contains(key))
                                                author = author.replace(key, value, false)
                                        users.put(user.fields.getInt("id"), User(author, user.fields.getString("photo")))
                                    }
                                    notifyDataSetChanged()
                                }
                                override fun onError(error: VKError?) {
                                    super.onError(error)
                                    Log.v("TestManNetworkError", error?.errorMessage)
                                }
                            })
        }

        private class User(val name: String,
                           val photo: String)
    }

    private class Test(val testId: Int,
                       val name: String,
                       val authorId: Int,
                       val time: Int)

    private class GetData(val context: Context, val callback: OnUpdatedDataListener) : AsyncTask<Void, Void, Void>() {
        private var tests: JSONArray? = null
        private val dialog = ProgressDialog(context)
        override fun onPreExecute() {
            super.onPreExecute()
            dialog.setCancelable(false)
            dialog.setTitle("TestMan")
            dialog.setMessage("Загружаем тесты")
            dialog.show()
        }

        override fun doInBackground(vararg params: Void): Void? {
            try {
                val str = OkHttpClient()
                        .newCall(Request.Builder()
                                .url(context.resources.getString(R.string.server_url)
                                        + context.resources.getString(R.string.get_all_tests))
                                .get()
                                .build())
                        .execute().body().string()
                Log.v("TestManNetwork", str)
                try {
                    tests = JSONArray(str)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            dialog.dismiss()
            callback.onUpdatedData(tests)
        }
    }
}