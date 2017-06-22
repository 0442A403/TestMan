package com.android.petro.testman;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Fragment with tasks
 */

public class TasksFragment extends Fragment {

    TaskAdapter taskAdapter;
    FloatingActionButton floatButton;
    ArrayList<String> questions = new ArrayList<>();
    ArrayList<ArrayList<String>> answersArray = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        RecyclerView taskRecycle = (RecyclerView) view.findViewById(R.id.create_recycle_view);
        taskAdapter = new TaskAdapter();
        taskRecycle.setAdapter(taskAdapter);
        taskRecycle.setLayoutManager(new LinearLayoutManager(getActivity()));
        addTask();

        floatButton = (FloatingActionButton) view.findViewById(R.id.add_task_button);
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
        taskAdapter.notifyDataSetChanged();
    }

    private void saveData() {
        questions.clear();
        answersArray.clear();
        Log.i("saveData", "start");
        for (TaskHolder holder : taskAdapter.tasks) {
            questions.add(holder.question.getText().toString());
            Log.i("saveData", holder.question.getText().toString());
            holder.saveData();
            answersArray.add(holder.answerStrings);
            for (String str : answersArray.get(answersArray.size() - 1))
                Log.i("saveData", str);
        }
    }


    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

        ArrayList<TaskHolder> tasks = new ArrayList<>();
        int taskSize = 0;

        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.creating_task_pattern, parent, false);
            TaskHolder holder = new TaskHolder(v);
            tasks.add(holder);
            return holder;
        }

        @Override
        public void onBindViewHolder(TaskHolder holder, int position) {
            if (position < questions.size()) {
//                tasks.set(position, holder);
//                holder.answerStrings.clear();
//                holder.question.setText(questions.get(position));
//                holder.answerAdapter.answerSize = questions.size();
//                holder.answerAdapter.notifyDataSetChanged();
//                holder.answerStrings = answersArray.get(position);
//                holder.answerAdapter.notifyDataSetChanged();

//                for (int i = 0; i < 2; i++)
//                    holder.addAnswer();
                tasks.set(position, holder);
                holder.answerAdapter.answers.clear();
                for (int i = 0; i < answersArray.get(position).size(); i++)
                    holder.addAnswer();
                holder.answerStrings = answersArray.get(position);
                holder.answerAdapter.notifyDataSetChanged();
            }
            else {
                tasks.add(holder);
                holder.question.setText("");
                holder.answerAdapter.answers.clear();
                holder.answerAdapter.answerSize = 2;
                holder.answerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return taskSize;
        }

        void addTask() {
            taskSize++;
        }
    }

    class TaskHolder extends RecyclerView.ViewHolder {

        EditText question;
        RecyclerView answerRecyclerView;
        AnswerAdapter answerAdapter;
        ImageView addTaskIcon;
        ArrayList<String> answerStrings = new ArrayList<>();

        TaskHolder(View itemView) {
            super(itemView);
            question = (EditText) itemView.findViewById(R.id.question);
            answerRecyclerView = (RecyclerView) itemView.findViewById(R.id.answer_recycle_view);
            answerAdapter = new AnswerAdapter();
            answerRecyclerView.setAdapter(answerAdapter);
            answerRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            for (int i = 0; i < 2; i++)
                addAnswer();

            addTaskIcon = (ImageView) itemView.findViewById(R.id.add_answer_button);
            addTaskIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAnswer();
                }
            });
        }

        void saveData() {
            answerStrings.clear();
            for (EditText holder : answerAdapter.answers)
                answerStrings.add(holder.getText().toString());
        }

        void addAnswer() {
            saveData();
            answerAdapter.addAnswer();
            answerAdapter.notifyDataSetChanged();
        }

        class AnswerAdapter extends RecyclerView.Adapter<AnswerHolder> {

            int answerSize = 0;
            ArrayList<EditText> answers = new ArrayList<>();

            @Override
            public void onViewRecycled(AnswerHolder holder) {
                super.onViewRecycled(holder);
            }

            @Override
            public AnswerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.creating_answer_pattern, parent, false);
                return new AnswerHolder(v);
            }

            @Override
            public void onBindViewHolder(AnswerHolder holder, int position) {
                if (position < answerStrings.size()) {
                    holder.answer.setText(answerStrings.get(position));
                    answers.set(position, holder.answer);
                }
                else {
                    answers.add(holder.answer);
                    holder.answer.setText("");
                }
            }

            @Override
            public int getItemCount() { return answerSize; }

            void addAnswer() { answerSize++; }
        }

        class AnswerHolder extends RecyclerView.ViewHolder {
            EditText answer;
            View removeIcon;
            AnswerHolder(View viewItem) {
                super(viewItem);
                answer = (EditText) itemView.findViewById(R.id.patterns_answer);
                removeIcon = itemView.findViewById(R.id.remove_answer);
            }
        }
    }

}
