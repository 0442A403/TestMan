package com.android.petro.testman.Fragments.MyTests

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.android.petro.testman.Activities.ChangingActivity
import com.android.petro.testman.Activities.ResultActivity
import com.android.petro.testman.R
import com.android.petro.testman.Support.Listeners.OnTestDeletedListener
import com.android.petro.testman.Support.Other.AnswerItem
import com.android.petro.testman.Support.Other.Dictionary
import com.android.petro.testman.Support.TestData.Test
import com.bumptech.glide.Glide
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.methods.VKApiUsers
import com.vk.sdk.api.model.VKList
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.item_with_image.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by petro on 23.08.2017.
 * Fragment for viewing user's current test's answers
 */
class AnswersFragment(private val data: ArrayList<AnswerItem>,
                      private val testId: Int,
                      private val callback: OnTestDeletedListener): Fragment(), OnTestDeletedListener {
    private var menu: Menu? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        data.reverse()
        val view = inflater!!.inflate(R.layout.fragment_search, container, false)
        val recyclerView = view.recycler_view__search
        recyclerView.layoutManager = LinearLayoutManager(context)

        val users: HashMap<Int, User> = HashMap()
        val userIds = ArrayList<Int>()
        data
                .filterNot { userIds.contains(it.author) }
                .forEach { userIds.add(it.author) }
        val ids = StringBuilder()
        for (id in userIds)
            ids.append("$id, ")
        if (ids.isNotEmpty())
            ids.removeRange(ids.length - 3 until ids.length)
        val params = HashMap<String, Any>()
        params.put(VKApiConst.USER_IDS, ids.toString())
        params.put(VKApiConst.FIELDS, VKApiConst.PHOTO)
        VKApiUsers()
                .get(VKParameters(params))
                .executeWithListener(object: VKRequest.VKRequestListener() {
                    override fun onComplete(response: VKResponse) {
                        super.onComplete(response)
                        val vkUsers = response.parsedModel as VKList<*>
                        object: AsyncTask<Void, Void, HashMap<Int, User>>() {
                            override fun doInBackground(vararg params: Void?): HashMap<Int, User> {
                                for (user in vkUsers) {
                                    var author = user.fields.getString("first_name") + " " + user.fields.getString("last_name")
                                    val dictionary = Dictionary().dictionary
                                    for ((key, value) in dictionary)
                                        if (author.contains(key))
                                            author = author.replace(key, value, false)
                                    Log.i("TestManInformation", "User photo: ${user.fields.getString("photo")}")
                                    users.put(
                                            user.fields.getInt("id"),
                                            User(
                                                    author,
                                                    Glide
                                                            .with(activity)
                                                            .load(user.fields.getString("photo"))
                                                            .asBitmap()
                                                            .into(-1, -1)
                                                            .get()
                                            )
                                    )
                                }
                                return users
                            }

                            override fun onPostExecute(users: HashMap<Int, User>) {
                                super.onPostExecute(users)
                                recyclerView.adapter = AnswerAdapter(data, users, context, view.edit_text__search)
                                recyclerView.adapter.notifyDataSetChanged()
                            }
                        }.execute()
                    }
                })
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.test_cotrol_menu, menu)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.change_test__menu)
            startActivityForResult(Intent(context, ChangingActivity::class.java).putExtra("Test id", testId), 0)
        else
            AlertDialog.Builder(activity)
                    .setMessage("Удалить тест?")
                    .setPositiveButton("ОК") { _, _ -> Test.delete(testId, context, this) }
                    .setNegativeButton("Отмена") { _, _ -> }
                    .create()
                    .show()
        return super.onOptionsItemSelected(item)
    }

    private class AnswerAdapter(private val data: ArrayList<AnswerItem>,
                                private val users: HashMap<Int, User>,
                                private val context: Context,
                                searchField: EditText): RecyclerView.Adapter<AnswerHolder>() {
        private val relevantData: ArrayList<AnswerItem> = data.clone() as ArrayList<AnswerItem>
        init {
            searchField.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    relevantData.clear()
                    if (s!!.trim().isNotEmpty()) {
                        for (word in s.split(" "))
                            for (answer in data)
                                if (!relevantData.contains(answer)
                                        && (users[answer.author]!!.name.contains(word, true)))
                                    relevantData.add(answer)
                    }
                    else {
                        for (test in data)
                            relevantData.add(test)
                    }
                    notifyDataSetChanged()
                }

            })
        }

        override fun onBindViewHolder(holder: AnswerHolder?, position: Int) {
            val user = users[relevantData[position].author]
            holder!!.image.setImageBitmap(user!!.bitmap)
            holder.author.text = users[relevantData[position].author]!!.name
            holder.date.text = SimpleDateFormat("MM dd, yyyy hh:mma", Locale.getDefault()).format(relevantData[position].date * 1000)
            holder.view.setOnClickListener {
                context.startActivity(
                        Intent(context, ResultActivity::class.java)
                                .putExtra("Answer id", relevantData[position].id)
                                .putExtra("Title", users[relevantData[position].author]!!.name))
            }
            holder.mark.text = relevantData[position].mark.toString()
            holder.mark.visibility = View.VISIBLE
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AnswerHolder =
                AnswerHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.item_with_image, parent, false))

        override fun getItemCount(): Int = relevantData.size
    }

    private class AnswerHolder(val view: View): RecyclerView.ViewHolder(view) {
        val image: ImageView = view.test_item_icon
        val author: TextView = view.first_field__item_with_image
        val date: TextView = view.second_field__item_with_image
        val mark: TextView = view.mark__item_with_image
    }

    private class User(val name: String,
                       val bitmap: Bitmap)

    override fun onDetach() {
        super.onDetach()
        menu!!.clear()
    }

    override fun onTestDeleted() {
        callback.onTestDeleted()
    }
}