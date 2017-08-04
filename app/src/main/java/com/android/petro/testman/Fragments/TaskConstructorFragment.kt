package com.android.petro.testman.Fragments

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import com.android.petro.testman.R
import com.android.petro.testman.Support.TaskClass
import com.android.petro.testman.Support.TaskType
import com.android.petro.testman.Support.TasksData
import kotlinx.android.synthetic.main.creating_answer_pattern.view.*
import kotlinx.android.synthetic.main.creating_task_pattern.view.*

class TaskConstructorFragment : Fragment() {
    private var taskAdapter : TaskAdapter? = null
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

        taskAdapter = TaskAdapter(floatButton)
        taskRecycle.adapter = taskAdapter
        taskRecycle.layoutManager = LinearLayoutManager(activity)

        return view
    }

    private fun addTask() {
        taskAdapter!!.addTask()
    }

    fun getData() : TasksData? {
        val data = taskAdapter!!.getData()
        if (data.isFilled())
            return taskAdapter!!.getData()
        else
            return null
    }

    private class TaskHolder(view : View,
                             private val removeTaskCallback: removeTaskCallback,
                             floatButton: FloatingActionButton) :
            RecyclerView.ViewHolder(view),
            View.OnCreateContextMenuListener {
        private val answerAdapter : AnswerAdapter
        private var type = TaskType.RADIO_BOX
        private val question = view.question
        var taskPosition = -1
        init {
            answerAdapter = AnswerAdapter(floatButton)
            val recyclerView = view.answer_recycle_view
            recyclerView.adapter = answerAdapter
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            itemView.setOnCreateContextMenuListener(this)
            view.add_answer_button.setOnClickListener { addAnswer() }
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
        }

        fun getData(): TaskClass {
            return TaskClass(
                    question.text.toString(),
                    answerAdapter.getData(),
                    type.code,
                    answerAdapter.getRights(type),
                    Object())
        }

        private fun removeTask() {
            removeTaskCallback.removeTask(taskPosition)
        }

        private class AnswerHolder(view: View,
                                   private val removeAnswerCallback: removeAnswerCallback,
                                   onCheckChangedCallback: checkChangedCallback) :
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
                        checkBox.visibility = View.GONE
                    }
                    else -> {
                        radioButton.visibility = View.GONE
                        checkBox.visibility = View.VISIBLE
                    }
                }
            }

            fun setData(data: String, type : Int, checked : Boolean) {
                answer.setText(data)
                if (type == TaskType.RADIO_BOX.code)
                    radioButton.isChecked = checked
                else if (type == TaskType.CHECK_BOX.code)
                    checkBox.isChecked = checked
            }

            fun getData(): String {
                return answer.text.toString()
            }

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
                removeAnswerCallback,
                checkChangedCallback {
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
                    answerHolders.set(position, holder)
            }

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AnswerHolder {
                return AnswerHolder(LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.creating_answer_pattern, parent, false), this, this)
            }

            override fun getItemCount(): Int {
                return size
            }

            fun addAnswer() {
                size++
                notifyItemInserted(size - 1)
            }

            fun changeInput(newType : TaskType) {
                for (holder in answerHolders)
                    holder.changeInput(newType)
            }

            fun setData(answers: ArrayList<String>, taskType : Int, taskRights: Any) {
                data = answers
                type = taskType
                rights = taskRights
                size = answers.size
                notifyDataSetChanged()
            }

            fun getRights(type : TaskType): Any {
                if (type == TaskType.RADIO_BOX) {
                    for (i in 0 .. answerHolders.size - 1)
                        if (answerHolders.get(i).isChecked(type))
                            return i
                    return -1
                }
                else {
                    val rights = ArrayList<Int>()
                    for (i in 0 .. answerHolders.size - 1)
                        if (answerHolders.get(i).isChecked(type))
                            rights.add(i)
                    return rights
                }
            }

            fun getData(): ArrayList<String> {
                val answers = ArrayList<String>()
                for (holder in answerHolders)
                    answers.add(holder.getData())
                return answers
            }

            override fun removeAnswer(position: Int) {
                size--
                answerHolders.removeAt(position)
                data = getData()
                for (i in position .. answerHolders.size - 1)
                    answerHolders.get(i).answerPosition = i
                notifyItemRemoved(position)
                floatButton.show()
                if (size == 1)
                    addAnswer()
            }

            override fun onCheckChanged(summonHolder: AnswerHolder) {
                for (holder in answerHolders)
                    if (holder != summonHolder)
                        holder.removeChecked()
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

        private interface removeAnswerCallback {
            fun removeAnswer(position: Int)
        }
        private interface checkChangedCallback {
            fun onCheckChanged(summonHolder: AnswerHolder)
        }
    }


    private class TaskAdapter(val floatButton : FloatingActionButton) : RecyclerView.Adapter<TaskHolder>(), removeTaskCallback{
        private var size = 1
        private val taskHolders = ArrayList<TaskHolder>()
        private var data = TasksData()
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TaskHolder {
            return TaskHolder(
                    LayoutInflater.from(parent!!.context)
                            .inflate(R.layout.creating_task_pattern, parent, false),
                    this,
                    floatButton)
        }

        override fun getItemCount(): Int {
            return size
        }

        override fun onBindViewHolder(holder: TaskHolder?, position: Int) {
            holder!!.taskPosition = position
            if (position < data.tasks.size)
                holder.setData(data.tasks.get(position))
            else
                holder.setData(TaskClass())
            if (position < taskHolders.size)
                taskHolders.set(position, holder)
            else
                taskHolders.add(holder)
        }

        fun addTask() {
            size++
            notifyItemInserted(size - 1)
        }

        fun getData(): TasksData {
            val dataList = ArrayList<TaskClass>()
            for (holder in taskHolders)
                dataList.add(holder.getData())
            return TasksData(dataList)
        }

        override fun removeTask(position: Int) {
            size--
            taskHolders.removeAt(position)
            data = getData()
            for (i in position .. taskHolders.size - 1)
                taskHolders.get(i).taskPosition = i
            notifyItemRemoved(position)
            floatButton.show()
            if (size == 0)
                addTask()
        }
    }

    private interface removeTaskCallback {
        fun removeTask(position : Int)
    }
}