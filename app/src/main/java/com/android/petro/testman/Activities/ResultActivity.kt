package com.android.petro.testman.Activities

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.petro.testman.R
import com.android.petro.testman.Support.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.solve_answer_layout.view.*
import kotlinx.android.synthetic.main.task_card.view.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

class ResultActivity : AppCompatActivity(), OnAnswerReceivedListener, OnTestReceivedListener {
    private var adapter: TaskAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        setSupportActionBar(toolbar_result)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        title = intent.getStringExtra("Title")

        val recyclerView = recycler_view__result
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(this)
        recyclerView.adapter = adapter
        if (intent.getIntExtra("Answer id", -1) != -1)
            Answer.get(intent.getIntExtra("Answer id", -1), this, this)
        else {
            val answer = Gson().fromJson(intent.getStringExtra("Answer"), Answer::class.java)
            val test = Gson().fromJson(intent.getStringExtra("Test"), Test::class.java)
            adapter!!.setData(answer, test.tasks.tasks)
            adapter!!.notifyDataSetChanged()
            val time = answer.time
            if (test.settings.time > 0)
                timer__result.text = Formatter().format("%02d:%02d", time / 60, time % 60).toString()
        }
    }

    private class TaskHolder(view: View, val context: Context): RecyclerView.ViewHolder(view) {
        private val recyclerView: RecyclerView = view.answer_recycler_view__solve
        private val question: TextView = view.question__solve

        init {
            recyclerView.layoutManager = LinearLayoutManager(context)
        }

        fun setData(data: TaskClass, answer: Any) {
            question.text = data.question
            val adapter = AnswerAdapter(
                    data.answers,
                    data.rights,
                    answer,
                    TaskType.getTypeByCode(data.type),
                    context
            )
            recyclerView.adapter = adapter
        }

        fun notifyDataSetChanged() {
            recyclerView.adapter.notifyDataSetChanged()
        }

        private class AnswerAdapter(private val answerStrings: ArrayList<String>,
                                    private val right: Any,
                                    private val studentAnswer: Any,
                                    private val type: TaskType,
                                    private val context: Context): RecyclerView.Adapter<AnswerHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AnswerHolder =
                    AnswerHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.solve_answer_layout, parent, false))

            override fun onBindViewHolder(holder: AnswerHolder?, position: Int) {
                holder!!.answer.text = answerStrings[position]
                when (type) {
                    TaskType.RADIO_BOX -> {
                        if ((right as Double == position.toDouble()) == (((studentAnswer as? Double)?.toInt() ?: studentAnswer as Int) == position))
                            holder.radioButton.supportButtonTintList = ContextCompat.getColorStateList(context, R.color.green)
                        else
                            holder.radioButton.supportButtonTintList = ContextCompat.getColorStateList(context, R.color.red)
                        if (position == (studentAnswer as? Double)?.toInt() ?: studentAnswer as Int)
                            holder.radioButton.isChecked = true
                    }
                    else -> {
                        holder.checkBox.visibility = View.VISIBLE
                        holder.radioButton.visibility = View.GONE
                        try {
                            if ((right as ArrayList<Double>).contains(position.toDouble()) == (studentAnswer as ArrayList<Double>).contains(position.toDouble()))
                                holder.checkBox.supportButtonTintList = ContextCompat.getColorStateList(context, R.color.green)
                            else
                                holder.checkBox.supportButtonTintList = ContextCompat.getColorStateList(context, R.color.red)
                            if (studentAnswer.contains(position.toDouble()))
                                holder.checkBox.isChecked = true
                        } catch (e: ClassCastException) {
                            val array = ArrayList<Int>()
                            val jsonArray = studentAnswer as JSONArray
                            (0 until jsonArray.length()).mapTo(array) { jsonArray.getInt(it) }
                            if ((right as ArrayList<Double>).contains(position.toDouble()) == array.contains(position))
                                holder.checkBox.supportButtonTintList = ContextCompat.getColorStateList(context, R.color.green)
                            else
                                holder.checkBox.supportButtonTintList = ContextCompat.getColorStateList(context, R.color.red)
                            if (array.contains(position))
                                holder.checkBox.isChecked = true
                        }
                    }
                }
            }

            override fun getItemCount(): Int = answerStrings.size

        }

        private class AnswerHolder(view: View): RecyclerView.ViewHolder(view) {
            val radioButton: AppCompatRadioButton = view.radio_button__solve
            val checkBox: AppCompatCheckBox = view.check_box__solve
            val answer: TextView = view.answer__solve
        }
    }

    private class TaskAdapter(val context: Context): RecyclerView.Adapter<TaskHolder>() {

        private var answer: Answer? = null
        private var tasks: ArrayList<TaskClass> = ArrayList()

        override fun onBindViewHolder(holder: TaskHolder, position: Int) {
            try {
                holder.setData(tasks[position], answer!!.answer[position])
            } catch (e: IndexOutOfBoundsException) {
                try {
                    tasks[position].rights as Double
                    holder.setData(tasks[position], -1)
                } catch (er: ClassCastException) {
                    holder.setData(tasks[position], ArrayList<Double>())
                }
            }
            holder.notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskHolder =
                TaskHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.task_card, parent, false), context)

        override fun getItemCount(): Int = tasks.size

        fun setData(answer: Answer, taskData: ArrayList<TaskClass>) {
            tasks = taskData
            this.answer = answer
        }
    }

    override fun onTestReceived(test: Test, receivedId: Int) {
        finish()
    }

    override fun onTestReceived(test: Test, answer: Answer?) {
        adapter!!.setData(answer!!, test.tasks.tasks)
        adapter!!.notifyDataSetChanged()
        val time = answer.time
        if (test.settings.time > 0)
            timer__result.text = Formatter().format("%02d:%02d", time / 60, time % 60).toString()
    }

    override fun onTestReceived(test: Test) {
        finish()
    }

    override fun onAnswerReceived(answer: Answer) {
        Test.get(this, this, answer)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}
