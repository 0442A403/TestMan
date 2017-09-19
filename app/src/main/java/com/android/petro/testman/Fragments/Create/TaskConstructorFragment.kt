package com.android.petro.testman.Fragments.Create

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import com.android.petro.testman.R
import com.android.petro.testman.Support.TestData.TaskClass
import com.android.petro.testman.Support.TestData.TaskData
import com.android.petro.testman.Support.TestData.TaskType
import kotlinx.android.synthetic.main.creating_answer_pattern.view.*
import kotlinx.android.synthetic.main.creating_task_pattern.view.*
import kotlinx.android.synthetic.main.task_settings_layout.view.*

class TaskConstructorFragment() : Fragment() {
    private var taskAdapter : TaskAdapter? = null
    private var fillTaskData: TaskData? = null
    constructor(fillTaskData: TaskData): this() {
        val tasks = ArrayList<TaskClass>()
        for (task in fillTaskData.tasks) {
            val correctRights: Any
            if (task.rights is Double)
                correctRights = task.rights.toInt()
            else {
                val rightArray = ArrayList<Int>()
                (task.rights as ArrayList<*>).mapTo(rightArray) { (it as Double).toInt() }
                correctRights = rightArray
            }
            tasks.add(TaskClass(task.question,
                    task.answers,
                    task.type,
                    correctRights,
                    task.scores,
                    task.photo))
        }
        this.fillTaskData = TaskData(tasks)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_tasks, container, false)

        val taskRecycle = view.findViewById(R.id.create_recycle_view) as RecyclerView

        val floatButton = view.findViewById(R.id.add_task_button) as FloatingActionButton
        floatButton.setOnClickListener { addTask() }
        taskRecycle.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0)
                    floatButton.hide()
                else if (dy != 0)
                    floatButton.show()
            }
        })

        taskAdapter = if (fillTaskData != null)
            TaskAdapter(floatButton, activity, fillTaskData!!)
        else
            TaskAdapter(floatButton, activity)
        taskRecycle.adapter = taskAdapter
        taskRecycle.layoutManager = LinearLayoutManager(activity)


        return view
    }

    private fun addTask() {
        taskAdapter!!.addTask()
    }

    fun getData() : TaskData? {
        val data = taskAdapter!!.getData()
        return if (data.isFilled())
            taskAdapter!!.getData()
        else
            null
    }

    private class TaskHolder(view: View,
                             private val removeTaskCallback: RemoveTaskCallback,
                             floatButton: FloatingActionButton,
                             context: Context) :
            RecyclerView.ViewHolder(view),
            View.OnCreateContextMenuListener {
        private val answerAdapter : AnswerAdapter
        private var type = TaskType.RADIO_BOX
        private val question = view.question
        private val scoreView: TextView
        var scores = 5
            set(value) {
                field = value
                scoreView.text = value.toString()
            }
        var taskPosition = -1
        init {
            answerAdapter = AnswerAdapter(floatButton)
            val recyclerView = view.answer_recycle_view
            recyclerView.adapter = answerAdapter
            recyclerView.layoutManager = LinearLayoutManager(view.context)
//            itemView.setOnCreateContextMenuListener(this)
            view.add_answer_button.setOnClickListener { addAnswer() }
            val dialog= Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.task_settings_layout, null, false)
            dialogView.increase_scores.setOnClickListener {
                scores++
            }
            dialogView.reduce_scores.setOnClickListener {
                if (scores > 1)
                    scores--
            }
            dialogView.change_input.setOnClickListener {
                changeInput(if (type == TaskType.RADIO_BOX) TaskType.CHECK_BOX else TaskType.RADIO_BOX)
                dialog.hide()
            }
            dialogView.delete.setOnClickListener {
                removeTask()
                dialog.hide()
            }
            dialog.setContentView(dialogView)
            scoreView = dialogView.scores
            view.setOnLongClickListener {
                dialog.show()
                return@setOnLongClickListener false
            }

        }

        private fun addAnswer() {
            answerAdapter.addAnswer()
        }

        private fun changeInput(newType : TaskType) {
            type = newType
            answerAdapter.changeInput(newType)
        }

        fun setData(data: TaskClass) {
            question.setText(data.question)
            answerAdapter.setData(
                    data.answers,
                    data.type,
                    data.rights)
            scores = data.scores
            changeInput(TaskType.getTypeByCode(data.type))
        }

        fun getData(): TaskClass {
            val task = TaskClass(
                    question.text.toString(),
                    answerAdapter.getData(),
                    type.code,
                    answerAdapter.getRights(type),
                    scores,
                    Object())
            Log.i("TestManInformation", "Task $taskPosition: ${task.question}\n${task.answers}\n${task.type}\n${task.rights}\n${task.scores}")
            return task
        }

        private fun removeTask() {
            removeTaskCallback.removeTask(taskPosition)
        }

        private class AnswerHolder(view: View,
                                   private val removeAnswerCallback: RemoveAnswerCallback,
                                   onCheckChangedCallback: CheckChangedCallback) :
                RecyclerView.ViewHolder(view) {
            private val checkBox : CheckBox
            private val radioButton : RadioButton
            private val answer : EditText
            var answerPosition: Int = -1
            init {
                checkBox = view.checkbox__answer_pattern
                radioButton = view.radio_button__answer_pattern
                radioButton.setOnCheckedChangeListener {
                    _, isChecked ->
                    if (isChecked)
                        onCheckChangedCallback.onCheckChanged(this)
                }
                answer = view.patterns_answer
                view.remove_answer.setOnClickListener {
                    _ ->
                    removeAnswer()
                }
            }

            private fun removeAnswer() {
                removeAnswerCallback.removeAnswer(answerPosition)
            }

            fun changeInput(type : TaskType) {
                when (type) {
                    TaskType.RADIO_BOX -> {
                        radioButton.visibility = View.VISIBLE
                        checkBox.visibility = View.INVISIBLE
                        checkBox.isChecked = false
                    }
                    else -> {
                        radioButton.visibility = View.INVISIBLE
                        checkBox.visibility = View.VISIBLE
                        radioButton.isChecked = false
                    }
                }
            }

            fun setData(data: String, type : Int, checked : Boolean) {
                answer.setText(data)
                if (type == TaskType.RADIO_BOX.code)
                    radioButton.isChecked = checked
                else if (type == TaskType.CHECK_BOX.code)
                    checkBox.isChecked = checked
                changeInput(TaskType.getTypeByCode(type))
            }

            fun getData(): String = answer.text.toString()

            fun isChecked(type: TaskType): Boolean {
                if (type == TaskType.RADIO_BOX)
                    return radioButton.isChecked
                else
                    return checkBox.isChecked
            }

            fun removeChecked() {
                radioButton.isChecked = false
            }
        }

        private class AnswerAdapter(val floatButton: FloatingActionButton) :
                RecyclerView.Adapter<AnswerHolder>(),
                RemoveAnswerCallback,
                CheckChangedCallback {
            private var size = 2
            private val answerHolders = ArrayList<AnswerHolder>()
            private var data = ArrayList<String>()
            private var type = TaskType.RADIO_BOX.code
            private var rights : Any = -1
            override fun onBindViewHolder(holder: AnswerHolder?, position: Int) {
                holder!!.answerPosition = position
                if (position < data.size)
                    holder.setData(data[position],
                            type,
                            if (type == TaskType.RADIO_BOX.code)
                                    (rights as Int) == position
                            else
                                (rights as ArrayList<*>).contains(position))
                else
                    holder.setData("", type, false)
                if (position >= answerHolders.size)
                    answerHolders.add(holder)
                else
                    answerHolders[position] = holder
                holder.changeInput(TaskType.getTypeByCode(type))
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AnswerHolder {
                return AnswerHolder(LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.creating_answer_pattern, parent, false), this, this)
            }

            override fun getItemCount(): Int = size

            fun addAnswer() {
                size++
                notifyItemInserted(size - 1)
            }

            fun changeInput(newType : TaskType) {
                type = newType.code
                for (holder in answerHolders)
                    holder.changeInput(newType)
            }

            fun setData(answers: ArrayList<String>, taskType : Int, taskRights: Any) {
                data = answers
                changeInput(TaskType.getTypeByCode(taskType))
//                Log.d("TestManDebug", "s: $taskType")
                rights = taskRights
                size = answers.size
                notifyDataSetChanged()
            }

            fun getRights(type : TaskType): Any {
                if (type == TaskType.RADIO_BOX) {
                    return (0 until answerHolders.size).firstOrNull { answerHolders[it].isChecked(type) }
                            ?: -1
                }
                else {
                    val rights = ArrayList<Int>()
                    (0 until answerHolders.size)
                            .filterTo(rights) {
                                answerHolders[it].isChecked(type)
                            }
                    return rights
                }
            }

            fun getData(): ArrayList<String> {
                val answers = ArrayList<String>()
                answerHolders.mapTo(answers) { it.getData() }
                return answers
            }

            override fun removeAnswer(position: Int) {
                size--
                answerHolders.removeAt(position)
                data = getData()
                for (i in position until answerHolders.size)
                    answerHolders[i].answerPosition = i
                notifyItemRemoved(position)
                floatButton.show()
                if (size == 1)
                    addAnswer()
            }

            override fun onCheckChanged(summonHolder: AnswerHolder) {
                answerHolders
                        .filter { it != summonHolder }
                        .forEach { it.removeChecked() }
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            val delete = menu!!.add(Menu.NONE, 0, 0, "Удалить")
            val changeInput = menu.add(Menu.NONE, 1, 1, "Изменить ввод")
//            val addImage = menu.add(Menu.NONE, 2, 2, "Добавить изображение")

            delete.setOnMenuItemClickListener {
                removeTask()
                false
            }

            changeInput.setOnMenuItemClickListener {
                if (type === TaskType.RADIO_BOX)
                    changeInput(TaskType.CHECK_BOX)
                else if (type === TaskType.CHECK_BOX)
                    changeInput(TaskType.RADIO_BOX)
                false
            }
        }

        private interface RemoveAnswerCallback {
            fun removeAnswer(position: Int)
        }
        private interface CheckChangedCallback {
            fun onCheckChanged(summonHolder: AnswerHolder)
        }
    }


    private class TaskAdapter(val floatButton : FloatingActionButton,
                              val context: Context) : RecyclerView.Adapter<TaskHolder>(), RemoveTaskCallback {
        private var size = 1
        private val taskHolders = ArrayList<TaskHolder>()
        private var data = TaskData()

        constructor(floatButton: FloatingActionButton,
                    context: Context,
                    fillTaskData: TaskData): this(floatButton, context) {
            data = fillTaskData
            size = fillTaskData.tasks.size
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskHolder {
            return TaskHolder(
                    LayoutInflater.from(parent!!.context)
                            .inflate(R.layout.creating_task_pattern, parent, false),
                    this,
                    floatButton,
                    context)
        }

        override fun getItemCount(): Int = size

        override fun onBindViewHolder(holder: TaskHolder?, position: Int) {
            holder!!.taskPosition = position
            if (position < data.tasks.size)
                holder.setData(data.tasks[position])
            else
                holder.setData(TaskClass())
            if (position < taskHolders.size)
                taskHolders[position] = holder
            else
                taskHolders.add(holder)
        }

        fun addTask() {
            size++
            notifyItemInserted(size - 1)
        }

        fun getData(): TaskData {
            val dataList = ArrayList<TaskClass>()
            taskHolders.mapTo(dataList) { it.getData() }
            Log.d("TestManDebug", "taskHolders size: ${taskHolders.size}")
            return TaskData(dataList)
        }

        override fun removeTask(position: Int) {
            size--
            taskHolders.removeAt(position)
            data = getData()
            for (i in position until taskHolders.size)
                taskHolders[i].taskPosition = i
            notifyItemRemoved(position)
            floatButton.show()
            if (size == 0)
                addTask()
        }
    }

    private interface RemoveTaskCallback {
        fun removeTask(position : Int)
    }
}