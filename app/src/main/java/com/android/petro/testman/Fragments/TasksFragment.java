package com.android.petro.testman.Fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.android.petro.testman.R;
import com.android.petro.testman.Support.TaskType;
import com.android.petro.testman.Support.TasksData;

import java.util.ArrayList;

/**
 * Fragment with tasks
 */

public class TasksFragment extends Fragment {

    private TaskAdapter taskAdapter;
    private ArrayList<String> questions = new ArrayList<>();
    private ArrayList<ArrayList<String>> answersArray = new ArrayList<>();
    private ArrayList<TaskHolder> tasks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        RecyclerView taskRecycle = (RecyclerView) view.findViewById(R.id.create_recycle_view);
        taskAdapter = new TaskAdapter();
        taskRecycle.setAdapter(taskAdapter);
        taskRecycle.setLayoutManager(new LinearLayoutManager(getActivity()));
        addTask();

        FloatingActionButton floatButton = (FloatingActionButton) view.findViewById(R.id.add_task_button);
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        return view;
    }

    private void addTask() {
        saveData();
        taskAdapter.addTask();
        taskAdapter.notifyItemChanged(taskAdapter.getItemCount()-1);
    }

    private void logInformation(String notice) {
        StringBuilder str = new StringBuilder(notice ).append("\n");
        int i = 0;
        for (TaskHolder holder : tasks) {
            str.append("Question ").append(i).append(" ").append(holder.getQuestion()).append("\n");
            for (TaskHolder.AnswerHolder answer : holder.answers)
                str.append("Answer - ").append(answer.getData()).append("\n");
            i++;
        }
        Log.i("Application Information", str.toString());
    }

    public TasksData getData() {
        saveData();
        ArrayList<TaskType> types = new ArrayList<>();
        ArrayList<Object> rights = new ArrayList<>();
        ArrayList<Object> photo = new ArrayList<>();
        for (TaskHolder holder : tasks) {
            types.add(holder.getType());
            rights.add(holder.getRights());
        }
        return new TasksData(questions, answersArray, types, rights, photo);
    }

    private void saveData() {
        questions.clear();
        answersArray.clear();
        StringBuilder str = new StringBuilder();
        int i = 0;
        for (TaskHolder holder : tasks) {
            questions.add(holder.getQuestion());
            answersArray.add(holder.getAnswers());

//            Log.i("Application Information", holder.answers.size() + " " + holder.answerAdapter.answerSize);
        }
        Log.i("Application Information", str.toString());
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

        private int taskSize = 0;

        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.creating_task_pattern, parent, false);
            return new TaskHolder(v);
        }

        @Override
        public void onBindViewHolder(TaskHolder holder, int position) {
            if (position >= questions.size()) {
                tasks.add(holder);
                holder.clearData();
            }
            else {
                holder.setData(position);
                tasks.set(position, holder);
            }
            holder.setPosition(position);
        }

        @Override
        public int getItemCount() {
            return taskSize;
        }

        void addTask() {
            taskSize++;
        }

        void removeTask(int position) {
//            tasks.remove(position);
//
//            StringBuilder strb = new StringBuilder("Before saveData()\n");
//            int i = 0;
//            for (TaskHolder holder : tasks) {
//                questions.add(holder.getQuestion());
//                answersArray.add(holder.getAnswers());
//                strb.append("Question ").append(i).append(" ").append(questions.get(i)).append("\n");
//                for (String string : answersArray.get(i))
//                    strb.append("Answer - ").append(string).append("\n");
//            }
//            Log.i("Application Information", strb.toString());
//
//
//            saveData();
//
//
//            StringBuilder stringBuffer = new StringBuilder();
//            i = 0;
//            for (TaskHolder holder : tasks) {
//                stringBuffer.append("TaskHolder number ").append(i).append(":\n");
//                stringBuffer.append("answers.size() - ").append(holder.answers.size()).append("\n");
//                stringBuffer.append("answerSize - ").append(holder.answerAdapter.answerSize).append("\n");
//                stringBuffer.append("answerStrings.size() - ").append(holder.answerStrings.size()).append("\n");
//                stringBuffer.append("position - ").append(holder.position).append("\n");
//                stringBuffer.append("Question - ").append(holder.getQuestion()).append("\n");
//                int j = 0;
//                for (String str : holder.getAnswers()) {
//                    stringBuffer.append("Answer ").append(j).append(" - ").append(str).append("\n");
//                    j++;
//                }
//                i++;
//            }
//            Log.i("Application Information", stringBuffer.toString());
//
//
//            taskSize--;
//            taskAdapter.notifyDataSetChanged();
            tasks.remove(position);
            taskSize--;
            if (taskSize == 0)
                addTask();
            notifyItemRemoved(position);
            for (int i = 0; i < tasks.size(); i++)
                tasks.get(i).setPosition(i);
        }
    }

    class TaskHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private EditText question;
        private RecyclerView answerRecyclerView;
        private AnswerAdapter answerAdapter;
        private ArrayList<String> answerStrings = new ArrayList<>();
        private ArrayList<AnswerHolder> answers = new ArrayList<>();
        private int position;
        private TaskType type = TaskType.RADIO_BOX;

        TaskHolder(View itemView) {
            super(itemView);
            question = (EditText) itemView.findViewById(R.id.question);
            answerRecyclerView = (RecyclerView) itemView.findViewById(R.id.answer_recycle_view);
            answerAdapter = new AnswerAdapter();
            answerRecyclerView.setAdapter(answerAdapter);
            answerRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            itemView.setOnCreateContextMenuListener(this);

            itemView.findViewById(R.id.add_answer_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAnswer();
                }
            });
        }

        void setPosition(int position) {
            this.position = position;
        }

        String getQuestion() {
            return question.getText().toString();
        }

        void setData(int position) {
            clearData();
            question.setText(questions.get(position));
            answerStrings = answersArray.get(position);
            answerAdapter.setSize(answerStrings.size());
            answerAdapter.notifyDataSetChanged();
        }

        ArrayList<String> getAnswers() {
            saveData();
            return answerStrings;
        }

        private void saveData() {
            answerStrings.clear();
            Log.d("debugging", String.valueOf(answers.size()));
            for (AnswerHolder answer : answers) {
                answerStrings.add(answer.getData());
                Log.d("debugging", "a - " + answer.getData());
            }
            Log.d("debugging", "q - " + getQuestion());
        }

        void addAnswer() {
            answerAdapter.addAnswer();
            answerAdapter.notifyItemChanged(answerAdapter.getItemCount() - 1);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem delete = menu.add(Menu.NONE, 0, 0, "Удалить");
            final MenuItem changeInput = menu.add(Menu.NONE, 1, 1, "Изменить ввод");
            MenuItem addImage = menu.add(Menu.NONE, 2, 2, "Добавить изображение");

            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    taskAdapter.removeTask(position);
                    return false;
                }
            });

            changeInput.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (type == TaskType.RADIO_BOX)
                        changeInput(TaskType.CHECK_BOX);
                    else if (type == TaskType.CHECK_BOX)
                        changeInput(TaskType.RADIO_BOX);
                    return false;
                }
            });
        }

        void changeInput(TaskType type) {
            this.type = type;
            for (AnswerHolder holder : answers)
                holder.changeInput(type);
        }

        void clearData() {
            answerStrings.clear();
            answers.clear();
            question.setText("");
            answerAdapter.setDefaultSize();
            answerAdapter.notifyDataSetChanged();
            changeInput(TaskType.RADIO_BOX);
        }

        TaskType getType() {
            return type;
        }

        Object getRights() {
            if (type == TaskType.RADIO_BOX) {
                for (int i = 0; i < answers.size(); i++)
                    if (answers.get(i).isChecked())
                        return i;
            }
            else if (type == TaskType.CHECK_BOX) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int i = 0; i < answers.size(); i++)
                    if (answers.get(i).isChecked())
                        list.add(i);
                if (list.size() > 0)
                    return list;
            }
            return null;
        }

        class AnswerAdapter extends RecyclerView.Adapter<AnswerHolder> {

            private int answerSize = 0;

            @Override
            public void onViewRecycled(AnswerHolder holder) { super.onViewRecycled(holder); }

            @Override
            public AnswerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.creating_answer_pattern, parent, false);
                return new AnswerHolder(v);
            }

            @Override
            public void onBindViewHolder(AnswerHolder holder, int position) {
                if (position >= answerStrings.size()) {
                    Log.w("ONBINDVIEWHOLDER", position + " LOLOLOLOLOLO");
                    answers.add(holder);
                    holder.setData("");
                }
                else {
                    holder.setData(answerStrings.get(position));
                    if (position < answers.size())
                        answers.set(position, holder);
                    else
                        answers.add(holder);
                }
                holder.setPosition(position);
            }

            @Override
            public int getItemCount() { return answerSize; }

            void addAnswer() { answerSize++; }

            void setSize(int size) { answerSize = size; }

            void setDefaultSize() { answerSize = 2; }

            void removeAnswer(int position) {
                answers.remove(position);
                saveData();
                answerSize--;
                if (answerSize == 1)
                    addAnswer();
                notifyDataSetChanged();
            }
        }

        class AnswerHolder extends RecyclerView.ViewHolder {

            private EditText answer;
            private int position;
            private RadioButton radioButton;
            private CheckBox checkBox;

            AnswerHolder(View viewItem) {
                super(viewItem);
                answer = (EditText) itemView.findViewById(R.id.patterns_answer);
                itemView.findViewById(R.id.remove_answer).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        answerAdapter.removeAnswer(position);
                    }
                });
                radioButton = (RadioButton) itemView.findViewById(R.id.radio_button__answer_pattern);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkbox__answer_pattern);

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            for (AnswerHolder answer : answers)
                                if (AnswerHolder.this != answer)
                                    answer.makeUnchecked();
                    }
                });
            }

            void setPosition(int position) {
                this.position = position;
            }

            String getData() {
                return answer.getText().toString();
            }

            void setData(String str) {
                answer.setText(str);
            }

            void changeInput(TaskType type) {
                if (type == TaskType.RADIO_BOX) {
                    radioButton.setVisibility(View.VISIBLE);
                    checkBox.setVisibility(View.INVISIBLE);
                }
                else if (type == TaskType.CHECK_BOX) {
                    radioButton.setVisibility(View.INVISIBLE);
                    checkBox.setVisibility(View.VISIBLE);
                }
            }

            Boolean isChecked() {
                if (type == TaskType.RADIO_BOX)
                    return radioButton.isChecked();
                else if (type == TaskType.CHECK_BOX)
                    return checkBox.isChecked();
                return null;
            }

            void makeUnchecked() {
                radioButton.setChecked(false);
            }
        }
    }
}
