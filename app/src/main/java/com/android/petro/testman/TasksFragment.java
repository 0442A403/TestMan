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

import java.util.ArrayList;

/**
 * Fragment with tasks
 */

public class TasksFragment extends Fragment {

    private TaskAdapter taskAdapter;
    private FloatingActionButton floatButton;
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
        taskAdapter.addTask();
        taskAdapter.notifyItemChanged(taskAdapter.getItemCount()-1);
    }

    private void saveData() {
        questions.clear();
        answersArray.clear();
        for (TaskHolder holder : tasks) {
            questions.add(holder.getQuestion());
            answersArray.add(holder.getAnswers());
        }
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
                holder.setPosition(position);
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

        private EditText question;
        private RecyclerView answerRecyclerView;
        private AnswerAdapter answerAdapter;
        private ArrayList<String> answerStrings = new ArrayList<>();
        private ArrayList<AnswerHolder> answers = new ArrayList<>();
        private int position;

        TaskHolder(View itemView) {
            super(itemView);
            question = (EditText) itemView.findViewById(R.id.question);
            answerRecyclerView = (RecyclerView) itemView.findViewById(R.id.answer_recycle_view);
            answerAdapter = new AnswerAdapter();
            answerRecyclerView.setAdapter(answerAdapter);
            answerRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            for (int i = 0; i < 2; i++)
                addAnswer();

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

        ArrayList<String> getAnswers() {
            saveData();
            return answerStrings;
        }

        private void saveData() {
            answerStrings.clear();
            for (AnswerHolder answer : answers) {
                answerStrings.add(answer.getData());
                Log.v("saveData", answer.getData());
            }
        }

        void addAnswer() {
            answerAdapter.addAnswer();
            answerAdapter.notifyItemChanged(answerAdapter.getItemCount() - 1);
        }

        class AnswerAdapter extends RecyclerView.Adapter<AnswerHolder> {

            private int answerSize = 0;

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
                if (position >= answerStrings.size()) {
                    answers.add(holder);
                    holder.setData("");
                }
                else {
                    holder.setData(answerStrings.get(position));
                    answers.set(position, holder);
                }
                holder.setPosition(position);
            }

            @Override
            public int getItemCount() { return answerSize; }

            void addAnswer() { answerSize++; }

            void removeAnswer(int position) {
                answers.remove(position);
                saveData();
                answerSize--;
                notifyDataSetChanged();
            }
        }

        class AnswerHolder extends RecyclerView.ViewHolder {

            private EditText answer;
            private int position;

            AnswerHolder(View viewItem) {
                super(viewItem);
                answer = (EditText) itemView.findViewById(R.id.patterns_answer);
                itemView.findViewById(R.id.remove_answer).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        answerAdapter.removeAnswer(position);
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
        }
    }
}
