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
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.android.petro.testman.Support.Listeners.OnAnswerSavedListener
import com.android.petro.testman.Support.Listeners.OnTestReceivedListener
import com.android.petro.testman.Support.TestData.*
import com.google.gson.Gson
import com.transitionseverywhere.*
import kotlinx.android.synthetic.main.activity_solve.*
import kotlinx.android.synthetic.main.solve_answer_layout.view.*
import kotlinx.android.synthetic.main.task_card.view.*
import java.util.*
import kotlin.collections.ArrayList

class SolveActivity : AppCompatActivity(), OnAnswerSavedListener, OnTestReceivedListener {
    private var timer : Timer? = null
    private var adapter : TaskAdapter? = null
    private var startView = true
    private var receivedId = -1
    private var test: Test? = null
    private var testId: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.android.petro.testman.R.layout.activity_solve)
        setSupportActionBar(toolbar_solve)
        title = intent.getStringExtra("name")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        test_name__entry.text = intent.getStringExtra("name")
        val time = intent.getIntExtra("time", -1)
        testId = intent.getIntExtra("id", -1)
        if (time > 0)
            solve_timer.text = Formatter().format("%02d:%02d", time / 60, time % 60).toString()
        else
            solve_timer.visibility = View.GONE
        this.start_solving.setOnClickListener {
            if (startView) {
                startView = false
                Test.get(testId, this, this)
            }
        }
    }

    private fun startTest() {
        solve_content_layout.visibility = View.VISIBLE


        val set = TransitionSet()
                .addTransition(com.transitionseverywhere.extra.Scale(0.7f))
                .addTransition(Fade())
                .setInterpolator(LinearOutSlowInInterpolator())

        //removing test name
        com.transitionseverywhere.TransitionManager.beginDelayedTransition(test_name__entry__wrapper, set)
        test_name__entry.visibility = View.GONE

        //removing button start
        com.transitionseverywhere.TransitionManager.beginDelayedTransition(start_solving__wrapper, set)
        start_solving.visibility = View.GONE

        //moving timer to right bottom angle
        com.transitionseverywhere.TransitionManager.beginDelayedTransition(solve_frame_layout,
                ChangeBounds().setPathMotion(ArcMotion()).setDuration(1000))
        val params = solve_entry_wrapper.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM or Gravity.END
        solve_entry_wrapper.layoutParams = params

        //changing phone
        com.transitionseverywhere.TransitionManager.beginDelayedTransition(primary_color_container,
                Recolor().setDuration(1000))
        primary_color_container.background =
                ColorDrawable(ContextCompat.getColor(this, com.android.petro.testman.R.color.transparent))

        //changing timer's text color
        com.transitionseverywhere.TransitionManager.beginDelayedTransition(solve_timer__wrapper,
                Recolor().setDuration(1000))
        solve_timer.setTextColor(ContextCompat.getColor(this, com.android.petro.testman.R.color.colorPrimary))

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
    }

    override fun onAnswerSaved(answer: Answer, mark: Int) {
        if (test!!.settings.showwrongs == "t")
            startActivity(
                    Intent(this, ResultActivity::class.java)
                            .putExtra(
                                    "Answer",
                                    Gson().toJson(answer)
                            )
                            .putExtra(
                                    "Test",
                                    Gson().toJson(test)
                            )
                            .putExtra("Title",
                                    "Результат: $mark")
            )
        else {
            setResult(BaseActivity.RESULT_MARK, Intent().putExtra("Mark", mark))
        }
        finish()
    }

    override fun onTestReceived(test: Test, receivedId: Int) {
        this.test = test
        this.receivedId = receivedId
        if (test.settings.time > 0)
            timer = Timer(test.settings.time * 1000L, 1000, this.solve_timer)
        val recyclerView = task_recycler_view
        adapter = TaskAdapter(test.tasks, this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        finish_test.setOnClickListener {
            showSaveDialog()
        }
        startTest()
    }

    override fun onTestReceived(test: Test) {
        finish()
    }

    override fun onTestReceived(test: Test, answer: Answer?) {
        finish()
    }

    private fun finishTest() {
        Log.i("TestManInformation", "FinishingTest")
        timer?.cancel()
        getAnswer().save(this, this, test!!)
    }

    private fun getAnswer(): Answer {
        return Answer(receivedId,
                adapter!!.getAnswers(),
                if (timer != null) (timer!!.time / 1000).toInt() else 0,
                testId)
    }

    private class TaskHolder(view: View): RecyclerView.ViewHolder(view) {
        private val question = view.question__solve
        private val recyclerView = view.answer_recycler_view__solve
        private var adapter: AnswerAdapter? = null

        fun getAnswer() = adapter!!.getAnswer()

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
                return if (radioButton.visibility == View.VISIBLE)
                    radioButton.isChecked
                else
                    checkBox.isChecked
            }
        }

        private class AnswerAdapter(val data: ArrayList<String>,
                                    val type: Int): RecyclerView.Adapter<AnswerHolder>(), CheckedChangeCallback{
            private val count = data.size
            private val answers = ArrayList<AnswerHolder>()
            private val random = IntArray(count)

            init {
                val array = ArrayList<Int>()
                for (i in 0 until count)
                    array.add(i)
                val randomObject = Random()
                for (i in 0 until count) {
                    val j = randomObject.nextInt(count - i)
                    random[i] = array[j]
                    array.removeAt(j)
                }
            }

            override fun onBindViewHolder(holder: AnswerHolder, position: Int) {
                holder.setData(data[random[position]], type)
            }

            override fun getItemCount() = count

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerHolder {
                val layout = LayoutInflater.from(parent.context).inflate(com.android.petro.testman.R.layout.solve_answer_layout, parent, false)
                layout.radio_button__solve.isClickable = true
                layout.check_box__solve.isClickable = true
                val holder = AnswerHolder(layout, type, this)
                answers.add(holder)
                return holder
            }

            fun getAnswer(): Any {
                if (type == TaskType.RADIO_BOX.code) {
                    for (i in 0 until answers.size)
                        if (answers.get(i).isChecked())
                            return random[i]
                    return -1
                }
                else /*if (task!!.type == TaskType.CHECK_BOX.code)*/ {
                    val answerList = ArrayList<Int>()
                    (0 until answers.size)
                            .filter { answers[it].isChecked() }
                            .mapTo(answerList) { random[it] }
                    return answerList
                }
            }

            override fun onCheckChanged(summonHolder: AnswerHolder) {
                answers
                        .filter { it != summonHolder }
                        .forEach { it.removeChecked() }
            }
        }

        interface CheckedChangeCallback {
            fun onCheckChanged(summonHolder: AnswerHolder)
        }
    }

    private class TaskAdapter(val data: TaskData,
                              val context: Context): RecyclerView.Adapter<TaskHolder>() {
        private val count = data.tasks.size
        private val taskHolders = ArrayList<TaskHolder>()
        override fun onBindViewHolder(holder: TaskHolder, position: Int) {
            holder.setData(data.tasks[position], context)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
            val holder = TaskHolder(LayoutInflater.from(parent.context).inflate(com.android.petro.testman.R.layout.task_card, parent, false))
            taskHolders.add(holder)
            return holder
        }

        fun getAnswers(): ArrayList<Any> {
            val list = ArrayList<Any>()
            for (task in taskHolders)
                list.add(task.getAnswer())
            return list
        }

        override fun getItemCount() = count
    }

    private inner class Timer(time: Long, periodicity: Long, private val timer: TextView): CountDownTimer(time, periodicity) {
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