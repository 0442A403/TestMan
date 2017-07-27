package com.android.petro.testman.Activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.android.petro.testman.R
import com.android.petro.testman.Support.*
import com.google.gson.Gson
import com.transitionseverywhere.*
import com.transitionseverywhere.extra.Scale
import kotlinx.android.synthetic.main.activity_solve.*
import kotlinx.android.synthetic.main.solve_answer_layout.view.*
import kotlinx.android.synthetic.main.solve_task_card.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class SolveActivity : AppCompatActivity(), OnAnswerSave, OnTestReceive {
    private var timer : Timer? = null
    private var test : TestClass? = null
    private var adapter : TaskAdapter? = null
    private var testId : Int = -1
    private var startView = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solve)
        setSupportActionBar(toolbar_solve)
        setTitle(intent.getStringExtra("name"))
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true);
        test_name__entry.text = intent.getStringExtra("name")
        val time = intent.getIntExtra("time", -1)
        if (time > 0)
            solve_timer.text = Formatter().format("%02d:%02d", time / 60, time % 60).toString()
        else
            solve_timer.visibility = View.GONE
        this.start_solving.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                if (startView) {
                    solve_content_layout.visibility = View.VISIBLE

                    val set = TransitionSet()
                            .addTransition(Scale(0.7f))
                            .addTransition(Fade())
                            .setInterpolator(LinearOutSlowInInterpolator())

                    //removing test name
                    TransitionManager.beginDelayedTransition(test_name__entry__wrapper, set)
                    test_name__entry.setVisibility(View.GONE)

                    //removing button start
                    TransitionManager.beginDelayedTransition(start_solving__wrapper, set)
                    start_solving.setVisibility(View.GONE)

                    //moving timer to right bottom angle
                    TransitionManager.beginDelayedTransition(solve_frame_layout,
                            ChangeBounds().setPathMotion(ArcMotion()).setDuration(1000))
                    val params = solve_entry_wrapper.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.BOTTOM or Gravity.END
                    solve_entry_wrapper.setLayoutParams(params)

                    //changing phone
                    TransitionManager.beginDelayedTransition(primary_color_container,
                            Recolor().setDuration(1000))
                    primary_color_container.setBackgroundDrawable(
                            ColorDrawable(resources.getColor(R.color.transparent)))

                    //changing timer's text color
                    TransitionManager.beginDelayedTransition(solve_timer__wrapper,
                            Recolor().setDuration(1000))
                    solve_timer.setTextColor(resources.getColor(R.color.colorPrimary))

                    object : AsyncTask<Void, Void, Void>() {
                        override fun doInBackground(vararg mParams: Void?): Void? {
                            Thread.sleep(500)
                            return null
                        }

                        override fun onPostExecute(result: Void?) {
                            super.onPostExecute(result)
                            solve_frame_layout.removeView(primary_color_container)
                        }
                    }.execute()
                    timer?.start()
                    startView = false
                }
            }
        })
        testId = intent.getIntExtra("id", -1)
        TestClass.get(testId, this, this)
    }

    override fun onAnswerSaved() {
        finish()
    }

    override fun onTestReceived(test: TestClass) {
        this.test = test
        if (test.settings!!.time > 0)
            timer = Timer(test.settings!!.time * 1000L, 1000)
        val recyclerView = task_recycler_view
        adapter = TaskAdapter(test.tasks!!.tasks.size)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        finish_test.setOnClickListener {
            _->
            showSaveDialog()
        }
    }

    private fun finishTest() {
        getAnswers().sendData(testId, this, this)
        finish()
    }

    private fun getAnswers(): Answer {
        return Answer(getSharedPreferences("AppPref", Context.MODE_PRIVATE).getString("author", "error"),
                adapter!!.getAnswers(),
                if (timer != null) (timer!!.time / 1000).toInt() else 0)
    }

    inner private class TaskHolder(view: View, tasks: ArrayList<TaskHolder>): RecyclerView.ViewHolder(view) {
        private val question = view.question__solve
        private val recyclerView = view.answer_recycler_view__solve
        private var task: TaskClass? = null
        private var adapter: AnswerAdapter? = null
        init {
            tasks.add(this)
        }

        fun setData(position: Int) {
            task = test!!.tasks!!.tasks.get(position)
            Log.v("Answers size", task!!.answers.size.toString())
            adapter = AnswerAdapter(task!!.answers.size)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this@SolveActivity)
            question.text = task!!.question
            recyclerView.setHasFixedSize(true)
        }

        inner private class AnswerHolder(view: View, answers: ArrayList<AnswerHolder>): RecyclerView.ViewHolder(view) {
            val radioButton = view.radio_button__solve
            val checkBox = view.check_box__solve
            val answer = view.answer__solve
            init {
                answers.add(this)
                answer.setOnClickListener {
                    _ ->
                    radioButton.isChecked = true
                }
                radioButton.setOnCheckedChangeListener {
                    radioButton, isChecked ->
                    if (isChecked)
                        for (answer in answers)
                            if (answer.radioButton != radioButton)
                                radioButton.isChecked = false
                }
            }
        }

        inner private class AnswerAdapter(val count: Int): RecyclerView.Adapter<AnswerHolder>() {
            val answers = ArrayList<AnswerHolder>()
            override fun onBindViewHolder(holder: AnswerHolder?, position: Int) {
                holder!!.answer.text = task!!.answers.get(position)
                if (task!!.type == TaskType.CHECK_BOX.code) {
                    holder.radioButton.visibility = View.GONE
                    holder.checkBox.visibility =  View.VISIBLE
                }
                Log.v("AnswerOnBindViewHolder", "woops! $position")
            }

            override fun getItemCount(): Int {
                return count
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AnswerHolder {
                return AnswerHolder(layoutInflater.inflate(R.layout.solve_answer_layout, parent, false), answers)
            }

            fun getAnswer(): Any {
                if (task!!.type == TaskType.RADIO_BOX.code) {
                    for (i in 0 .. answers.size - 1)
                        if (answers.get(i).radioButton.isChecked)
                            return i
                    return -1
                }
                else /*if (task!!.type == TaskType.CHECK_BOX.code)*/ {
                    val answerList = ArrayList<Int>()
                    for (i in 0 .. answers.size - 1)
                        if (answers.get(i).checkBox.isChecked)
                            answerList.add(i)
                    return answerList
                }
            }

        }

        fun getAnswer(): Any {
            return adapter!!.getAnswer()
        }
    }

    inner private class TaskAdapter(val count: Int): RecyclerView.Adapter<TaskHolder>() {
        val tasks = ArrayList<TaskHolder>()
        override fun onBindViewHolder(holder: TaskHolder?, position: Int) {
            holder!!.setData(position)
        }

        override fun getItemCount(): Int {
            return count
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskHolder {
            return TaskHolder(layoutInflater.inflate(R.layout.solve_task_card, parent, false), tasks)
        }

        fun getAnswers(): ArrayList<Any> {
            val list = ArrayList<Any>()
            for (task in tasks)
                list.add(task.getAnswer())
            return list
        }
    }

    inner private class Timer(time: Long, periodicity: Long): CountDownTimer(time, periodicity) {
        private val timer = solve_timer
        var time: Long = -1
        override fun onTick(millisUntilFinished: Long) {
            time = millisUntilFinished
            timer.text = Formatter().format("%02d:%02d", millisUntilFinished / 60000, millisUntilFinished % 60000 / 1000    ).toString()
        }
        override fun onFinish() {

        }
    }

    private fun showSaveDialog() {
        AlertDialog.Builder(this)
                .setTitle("Ваш ответ будет сохранён. Выйти?")
                .setPositiveButton("Сохранить") {
                    _, _ ->
                    finishTest()
                }
                .setNegativeButton("Отмена", null)
                .show()
    }

    override fun onBackPressed() {
        if (!startView)
            showSaveDialog()
        else
            finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!startView)
            showSaveDialog()
        else
            finish()
        return super.onOptionsItemSelected(item)
    }
}