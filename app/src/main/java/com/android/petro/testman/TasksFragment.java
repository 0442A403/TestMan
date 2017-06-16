package com.android.petro.testman;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
    static FragmentActivity activity;
    static LayoutInflater layoutInflater;
    static TaskAdapter taskAdapter;
    FloatingActionButton floatButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        activity = getActivity();
        layoutInflater = inflater;

        RecyclerView taskRecycle = (RecyclerView) view.findViewById(R.id.create_recycle_view);
        taskAdapter = new TaskAdapter();
        taskRecycle.setAdapter(taskAdapter);
        taskRecycle.setLayoutManager(new LinearLayoutManager(activity));
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

    private static void addTask() {
        taskAdapter.addTask();
        taskAdapter.notifyDataSetChanged();
    }


    private static class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {

        ArrayList<TaskHolder> tasks = new ArrayList<>();
        static int taskSize = 0;

        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.creating_task_pattern, parent, false);
            TaskHolder holder = new TaskHolder(v);
            tasks.add(holder);
            return holder;
        }

        @Override
        public void onBindViewHolder(final TaskHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return taskSize;
        }

        void addTask() {
            taskSize++;
        }
    }

    static class TaskHolder extends RecyclerView.ViewHolder {

        EditText question;
        RecyclerView answerRecyclerView;
        AnswerAdapter answerAdapter;
        ImageView addTaskIcon;
        static ArrayList<String> answerStrings = new ArrayList<>();

        TaskHolder(View itemView) {
            super(itemView);
            question = (EditText) itemView.findViewById(R.id.question);
            answerRecyclerView = (RecyclerView) itemView.findViewById(R.id.answer_recycle_view);
            answerRecyclerView.setHasFixedSize(true);
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

        void setData() {
            answerStrings.clear();
            for (AnswerHolder holder : answerAdapter.answers)
                answerStrings.add(holder.answer.getText().toString());
        }

        void addAnswer() {
            Log.i("UserInformation", "Checkpoint 1");
            setData();
            Log.i("UserInformation", "Checkpoint 2");
            answerAdapter.addAnswer();
            Log.i("UserInformation", "Checkpoint 3");
            answerAdapter.notifyDataSetChanged();
//            answerAdapter.notify();
        }

        static class AnswerAdapter extends RecyclerView.Adapter<AnswerHolder> {

            ArrayList<AnswerHolder> answers = new ArrayList<>();
            int answerSize = 0;

            @Override
            public void onViewRecycled(AnswerHolder holder) {
                super.onViewRecycled(holder);
            }

            @Override
            public AnswerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.creating_answer_pattern, parent, false);
                AnswerHolder holder = new AnswerHolder(v);
                answers.add(holder);
                Log.i("UserInformation",
                        "Answer Holders: " + answers.size() + "\nAnswer Size: " + answerSize
                                + "\nAnswer Strings: " + answerStrings.size());
                return holder;
            }

            @Override
            public void onBindViewHolder(AnswerHolder holder, int position) {
                if (position != answerStrings.size()) {
                    holder.answer.setText(answerStrings.get(position));
                    Log.i("onBindViewHolder", "Holder number " + position + " with " +
                            answerStrings.get(position));
                }
                else {
                    Log.i("onBindViewHolder", "empty answerStrings");
                }
            }

            @Override
            public int getItemCount() {
                return answerSize;
            }

            void addAnswer() {
                answerSize++;
                Log.i("UserInformation", "FROM ADDANSWER \n" +
                        "Answer Holders: " + answers.size() + "\nAnswer Size: " + answerSize
                        + "\nAnswer Strings: " + answerStrings.size());
            }
        }

        static class AnswerHolder extends RecyclerView.ViewHolder {
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
