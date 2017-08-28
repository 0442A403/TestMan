package com.android.petro.testman.Fragments.MyTests

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.android.petro.testman.R
import com.android.petro.testman.Support.Listeners.OnTestDeletedListener
import com.android.petro.testman.Support.Listeners.OnTestSelectedListener
import com.android.petro.testman.Support.Other.TestItem
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.my_test_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by petro on 23.08.2017.
 * Fragment for viewing user's tests
 */
class MyTestsFragment(private val callback: OnTestSelectedListener,
                      private val onTestDeletedListener: OnTestDeletedListener): Fragment() {
    private var adapter: TaskAdapter? = null
    private var menu: Menu? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_search, container, false)
        adapter = TaskAdapter(callback, view.edit_text__search, onTestDeletedListener)
        val recyclerView = view.recycler_view__search
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    fun updateData(receivedData: ArrayList<TestItem>) {
        adapter!!.data = receivedData
        adapter!!.notifyDataSetChanged()
    }

    private class TaskAdapter(private val callback: OnTestSelectedListener,
                              searchField: EditText,
                              private val onTestDeletedListener: OnTestDeletedListener): RecyclerView.Adapter<TestHolder>() {
        private var relevantData: ArrayList<TestItem> = ArrayList()
        var data: ArrayList<TestItem> = ArrayList()
            set(value) {
                field = value
                relevantData = value.clone() as ArrayList<TestItem>
            }

        init {
            searchField.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    relevantData.clear()
                    if (s!!.trim().isNotEmpty()) {
                        for (word in s.split(" "))
                            for (test in data)
                                if (!relevantData.contains(test)
                                        && (test.testName.contains(word, true)))
                                    relevantData.add(test)
                    }
                    else {
                        for (test in data)
                            relevantData.add(test)
                    }
                    notifyDataSetChanged()
                }

            })
        }

        override fun onBindViewHolder(holder: TestHolder?, position: Int) {
            holder!!.name.text = relevantData[position].testName
            val dv = relevantData[position].date * 1000
            val df = Date(dv)
            holder.date.text = SimpleDateFormat("MM dd, yyyy hh:mma", Locale.getDefault()).format(df)
            val markSum = relevantData[position].answers.sumBy { it.mark }
            val mark = (markSum * 1f / relevantData[position].answers.size).toString().take(3)
            holder.mark.text =
                    if (mark != "NaN")
                        mark
                    else
                        "0.0"
            holder.view.setOnClickListener {
                callback.onTestSelected(AnswersFragment(relevantData[position].answers, relevantData[position].testId, onTestDeletedListener))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TestHolder =
                TestHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.my_test_item, parent, false))

        override fun getItemCount(): Int = relevantData.size
    }

    private class TestHolder(val view: View): RecyclerView.ViewHolder(view) {
        val mark: TextView = view.mark__my_tests_item
        val date: TextView = view.test_date__my_tests_item
        val name: TextView = view.test_name__my_tests_item
    }
}