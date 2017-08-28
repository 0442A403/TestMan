package com.android.petro.testman.Fragments.MyAnswers

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.petro.testman.Activities.ResultActivity
import com.android.petro.testman.R
import com.android.petro.testman.Support.Other.Dictionary
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.vk.sdk.api.*
import com.vk.sdk.api.methods.VKApiUsers
import com.vk.sdk.api.model.VKList
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.item_with_image.view.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Created by petro on 27.08.2017.
 * Fragment for viewing user's marks and if it possible user's wrongs
 */

class MyAnswersFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflatedView = inflater!!.inflate(R.layout.fragment_search, container, false)
        val recyclerView = inflatedView.recycler_view__search
        recyclerView.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        GetData(recyclerView, activity).execute(context.getSharedPreferences("AppPref", Context.MODE_PRIVATE).getInt("VKId", -1))
        return inflatedView
    }

    private class AnswerHolder(private val holderView: View, private val context: Context): RecyclerView.ViewHolder(holderView) {
        private val mark = holderView.mark__item_with_image
        private val testName = holderView.first_field__item_with_image
        private val date = holderView.second_field__item_with_image
        private val icon = holderView.test_item_icon
        init {
            mark.visibility = View.VISIBLE
        }
        fun setData(data: AnswerItem, user: UserWithImage) {
            mark.text = data.mark.toString()
            testName.text = data.test_name
            val dv = data.date * 1000
            val df = Date(dv)
            date.text = SimpleDateFormat("MM dd, yyyy hh:mma", Locale.getDefault()).format(df)
            icon.setImageBitmap(user.image)
            holderView.setOnClickListener {
                if (data.show_wrongs == "t")
                    context.startActivity(
                            Intent(context, ResultActivity::class.java)
                                    .putExtra("Answer id", data.answerId)
                                    .putExtra("Title", data.test_name)
                    )
                else
                    Snackbar
                            .make(holderView, "Результат закрыт для просмотра", Snackbar.LENGTH_SHORT)
                            .show()
            }
        }
    }

    private class AnswerAdapter(private val data: ArrayList<AnswerItem>,
                                private val usersImages: HashMap<Int, UserWithImage>,
                                private val context: Context): RecyclerView.Adapter<AnswerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerHolder
                = AnswerHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_with_image, parent, false), context)


        override fun onBindViewHolder(holder: AnswerHolder, position: Int) {
            holder.setData(data[position], usersImages[data[position].test_author]!!)
        }

        override fun getItemCount(): Int = data.size

    }

    private class GetData(private val recyclerView: RecyclerView,
                          private val activity: Activity): AsyncTask<Int, Void, Void>() {
        private val dialog: ProgressDialog = ProgressDialog(activity)
        private val author =
                activity
                        .getSharedPreferences("AppPref", Context.MODE_PRIVATE)
                        .getInt("VKId", -1).toString()
        override fun onPreExecute() {
            super.onPreExecute()
            dialog.setTitle("TestMan")
            dialog.setMessage("Загружаем ответы")
            dialog.setCancelable(false)
            dialog.show()
        }

        override fun doInBackground(vararg params: Int?): Void? {
            val formBody = FormBody.Builder()
                    .add("author", author)
                    .build()

            val request = Request.Builder()
                    .url("https://testman-o442a4o3.c9users.io/get_answers_by_author/")
                    .post(formBody)
                    .build()

            val responseString = OkHttpClient().newCall(request).execute().body().string()
            Log.v("TestManNetwork", responseString)

            try {
                val jsonArray = JSONArray(responseString)
                val data = ArrayList<AnswerItem>()
                val gson = Gson()
                (0 until jsonArray.length())
                        .mapTo(data) {
                            gson.fromJson(jsonArray.getJSONObject(it).toString(), AnswerItem::class.java)
                        }
                val usersList = ArrayList<Int>()
                data
                        .filterNot { usersList.contains(it.test_author) }
                        .forEach { usersList.add(it.test_author) }
                val idRequest = StringBuilder()
                for (userId in usersList)
                    idRequest.append("$userId, ")
                if (idRequest.isNotEmpty())
                    idRequest.delete(idRequest.length - 3, idRequest.length - 1)
                val vkParams: HashMap<String, Any> = HashMap()
                val users = HashMap<Int, UserWithImage>()
                vkParams.put(VKApiConst.USER_IDS, idRequest.toString())
                vkParams.put(VKApiConst.FIELDS, VKApiConst.PHOTO)
                VKApiUsers()
                        .get(VKParameters(vkParams))
                        .executeWithListener(
                                object : VKRequest.VKRequestListener() {
                                    override fun onComplete(response: VKResponse?) {
                                        Log.v("TestManNetwork", response!!.responseString)
                                        val vkUsers = response.parsedModel as VKList<*>
                                        val dictionary = Dictionary().dictionary
                                        val usersImageLinks = HashMap<Int, UserWithImageLink>()
                                        for (user in vkUsers) {
                                            var author = user.fields.getString("first_name") + " " + user.fields.getString("last_name")
                                            for ((key, value) in dictionary)
                                                if (author.contains(key))
                                                    author = author.replace(key, value, false)
                                            usersImageLinks.put(
                                                    user.fields.getInt("id"),
                                                    UserWithImageLink(author,
                                                            user.fields.getString("photo"))
                                            )
                                        }
                                        object: AsyncTask<Void, Void, Void>() {
                                            override fun doInBackground(vararg params: Void?): Void? {
                                                for ((key, value) in usersImageLinks)
                                                    users.put(key,
                                                            UserWithImage(
                                                                    value.name,
                                                                    Glide
                                                                            .with(activity)
                                                                            .load(value.imageLink)
                                                                            .asBitmap()
                                                                            .into(-1, -1)
                                                                            .get()
                                                            ))
                                                return null
                                            }

                                            override fun onPostExecute(result: Void?) {
                                                super.onPostExecute(result)
                                                dialog.hide()
                                                recyclerView.adapter = AnswerAdapter(data, users, activity)
                                                recyclerView.adapter.notifyDataSetChanged()
                                            }
                                        }.execute()
                                    }
                                    override fun onError(error: VKError?) {
                                        super.onError(error)
                                        Log.v("TestManNetworkError", error?.errorMessage)
                                    }


                                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }

    private class AnswerItem(val answerId: Int,
                             val test_name: String,
                             val test_author: Int,
                             val date: Long,
                             val mark: Int,
                             val show_wrongs: String)

    private class UserWithImageLink(val name: String,
                                    val imageLink: String)

    private class UserWithImage(val name: String,
                                val image: Bitmap)
}
