package com.android.petro.testman.Activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.android.petro.testman.R
import com.android.petro.testman.Support.*
import com.transitionseverywhere.*
import com.transitionseverywhere.extra.Scale
import kotlinx.android.synthetic.main.activity_solve.*
import kotlinx.android.synthetic.main.solve_answer_layout.view.*
import kotlinx.android.synthetic.main.solve_task_card.view.*
import java.util.*
import kotlin.collections.ArrayList

class SolveActivity : AppCompatActivity(), OnAnswerSave, OnTestReceive {
    private var timer : Timer? = null
    private var adapter : TaskAdapter? = null
    private var startView = true
    private var receivedId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solve)
        setSupportActionBar(toolbar_solve)
        title = intent.getStringExtra("name")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        test_name__entry.text = intent.getStringExtra("name")
        val time = intent.getIntExtra("time", -1)
        if (time > 0)
            solve_timer.text = Formatter().format("%02d:%02d", time / 60, time % 60).toString()
        else
            solve_timer.visibility = View.GONE
        this.start_solving.setOnClickListener {
            if (startView) {
                solve_content_layout.visibility = View.VISIBLE

                val set = TransitionSet()
                        .addTransition(Scale(0.7f))
                        .addTransition(Fade())
                        .setInterpolator(LinearOutSlowInInterpolator())

                //removing test name
                TransitionManager.beginDelayedTransition(test_name__entry__wrapper, set)
                test_name__entry.visibility = View.GONE

                //removing button start
                TransitionManager.beginDelayedTransition(start_solving__wrapper, set)
                start_solving.visibility = View.GONE

                //moving timer to right bottom angle
                TransitionManager.beginDelayedTransition(solve_frame_layout,
                        ChangeBounds().setPathMotion(ArcMotion()).setDuration(1000))
                val params = solve_entry_wrapper.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.BOTTOM or Gravity.END
                solve_entry_wrapper.layoutParams = params

                //changing phone
                TransitionManager.beginDelayedTransition(primary_color_container,
                        Recolor().setDuration(1000))
                primary_color_container.background =
                        ColorDrawable(ContextCompat.getColor(this, R.color.transparent))

                //changing timer's text color
                TransitionManager.beginDelayedTransition(solve_timer__wrapper,
                        Recolor().setDuration(1000))
                solve_timer.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))

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
        TestClass.get(intent.getIntExtra("id", -1), this, this)
    }

    override fun onAnswerSaved() {
        finish()
    }

    override fun onTestReceived(test: TestClass, receivedId: Int) {
        this.receivedId = receivedId
        if (test.settings!!.time > 0)
            timer = Timer(test.settings!!.time * 1000L, 1000, this.solve_timer)
        val recyclerView = task_recycler_view
        adapter = TaskAdapter(test.tasks!!, this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        finish_test.setOnClickListener {
            _->
            showSaveDialog()
        }
    }

    private fun finishTest() {
        getAnswers().sendData(this, this)
    }

    private fun getAnswers(): Answer {
        return Answer(receivedId,
                adapter!!.getAnswers(),
                if (timer != null) (timer!!.time / 1000).toInt() else 0)
    }

    private class TaskHolder(view: View): RecyclerView.ViewHolder(view) {
        private val question = view.question__solve
        private val recyclerView = view.answer_recycler_view__solve
        private var adapter: AnswerAdapter? = null

        fun getAnswer(): Any {
            return adapter!!.getAnswer()
        }

        fun setData(data: TaskClass, context: Context) {
            adapter = AnswerAdapter(data.answers, data.type)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(context)
            question.text = data.question
            recyclerView.setHasFixedSize(true)
        }

        private class AnswerHolder(view: View,
                                   type: Int,
                                   checkedChangeCallback: CheckedChangeCallback): RecyclerView.ViewHolder(view) {
            private val radioButton = view.radio_button__solve!!
            private val checkBox = view.check_box__solve!!
            private val answer = view.answer__solve!!
            init {
                answer.setOnClickListener {
                    if (type == TaskType.RADIO_BOX.code)
                        radioButton.isChecked = true
                    else
                        checkBox.isChecked = true
                }
                radioButton.setOnCheckedChangeListener {
                    _, isChecked ->
                    if (isChecked)
                        checkedChangeCallback.onCheckChanged(this)
                }
            }

            fun removeChecked() {
                radioButton.isChecked = false
            }

            fun setData(answerData: String, type: Int) {
                answer.text = answerData
                if (type == TaskType.CHECK_BOX.code) {
                    radioButton.visibility = View.GONE
                    checkBox.visibility =  View.VISIBLE
                    radioButton.isChecked = false
                }
            }

            fun isChecked(): Boolean {
                if (radioButton.visibility == View.VISIBLE)
                    return radioButton.isChecked
                else
                    return checkBox.isChecked
            }
        }

        private class AnswerAdapter(val data: ArrayList<String>,
                                    val type: Int): RecyclerView.Adapter<AnswerHolder>(), CheckedChangeCallback{
            private val count = data.size
            private val answers = ArrayList<AnswerHolder>()
            private val random = IntArray(count)

            init {
                val array = ArrayList<Int>()
                for (i in 0 .. count - 1)
                    array.add(i)
                val randomObject = Random()
                for (i in 0 .. count - 1) {
                    val j = randomObject.nextInt(count - i)
                    random[i] = array[j]
                    array.removeAt(j)
                }
            }

            override fun onBindViewHolder(holder: AnswerHolder?, position: Int) {
                holder!!.setData(data[random[position]], type)
            }

            override fun getItemCount(): Int {
                return count
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AnswerHolder {
                val holder = AnswerHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.solve_answer_layout, parent, false), type, this)
                answers.add(holder)
                return holder
            }

            fun getAnswer(): Any {
                if (type == TaskType.RADIO_BOX.code) {
                    for (i in 0 .. answers.size - 1)
                        if (answers.get(i).isChecked())
                            return random[i]
                    return -1
                }
                else /*if (task!!.type == TaskType.CHECK_BOX.code)*/ {
                    val answerList = ArrayList<Int>()
                    for (i in 0 .. answers.size - 1)
                        if (answers.get(i).isChecked())
                            answerList.add(random[i])
                    return answerList
                }
            }

            override fun onCheckChanged(summonHolder: AnswerHolder) {
                for (holder in answers)
                    if (holder != summonHolder)
                        holder.removeChecked()
            }
        }

        interface CheckedChangeCallback {
            fun onCheckChanged(summonHolder: AnswerHolder)
        }
    }

    private class TaskAdapter(val data: TasksData,
                              val context: Context): RecyclerView.Adapter<TaskHolder>() {
        val count = data.tasks.size
        val taskHolders = ArrayList<TaskHolder>()
        override fun onBindViewHolder(holder: TaskHolder?, position: Int) {
            holder!!.setData(data.tasks[position], context)
        }

        override fun getItemCount(): Int {
            return count
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskHolder {
            val holder = TaskHolder(LayoutInflater.from(parent!!.context).inflate(R.layout.solve_task_card, parent, false))
            taskHolders.add(holder)
            return holder
        }

        fun getAnswers(): ArrayList<Any> {
            val list = ArrayList<Any>()
            for (task in taskHolders)
                list.add(task.getAnswer())
            return list
        }
    }

    inner private class Timer(time: Long, periodicity: Long, private val timer: TextView): CountDownTimer(time, periodicity) {
        var time: Long = -1
        override fun onTick(millisUntilFinished: Long) {
            time = millisUntilFinished
            timer.text = Formatter().format("%02d:%02d", millisUntilFinished / 60000, millisUntilFinished % 60000 / 1000    ).toString()
        }
        override fun onFinish() {
            finishTest()
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
        else {
            setResult(BaseActivity.TEST_NOT_STARTED,
                    Intent().putExtra("id", receivedId))
            timer?.cancel()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!startView)
            showSaveDialog()
        else {
            setResult(BaseActivity.TEST_NOT_STARTED,
                    Intent().putExtra("id", receivedId))
            timer?.cancel()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}