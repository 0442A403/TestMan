package com.android.petro.testman.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.petro.testman.R;
import com.android.petro.testman.Support.Listeners.OnTestSaveListener;
import com.android.petro.testman.Support.Listeners.OnTestSavedListener;
import com.android.petro.testman.Support.Listeners.OnTestUpdateListener;
import com.android.petro.testman.Support.Listeners.OnTestUpdatedListener;
import com.android.petro.testman.Support.TestData.SettingsData;
import com.android.petro.testman.Support.TestData.TaskClass;
import com.android.petro.testman.Support.TestData.TaskData;
import com.android.petro.testman.Support.TestData.TaskType;
import com.android.petro.testman.Support.TestData.Test;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for creating new tests
 */

public class CreateFragment extends Fragment implements OnTestSaveListener, OnTestUpdateListener {
    private FragmentManager fragmentManager = null;
    private ViewPager pager = null;
    private TaskConstructorFragment constructorFragment = null;
    private SettingsFragment settingsFragment = null;
    private OnTestSavedListener onTestSavedListener = null;
    private OnTestUpdatedListener onTestUpdatedListener = null;
    private Test test = null;
    private View view = null;
    private int id = -1;
    private TaskData oldData;

    public CreateFragment(FragmentManager fragmentManager, OnTestSavedListener onTestSavedListener) {
        this.fragmentManager = fragmentManager;
        this.onTestSavedListener = onTestSavedListener;
    }

    public CreateFragment(FragmentManager fragmentManager,
                          OnTestUpdatedListener onTestUpdatedListener,
                          Test test,
                          int id) {
        this.fragmentManager = fragmentManager;
        this.onTestUpdatedListener = onTestUpdatedListener;
        this.test = test;
        this.id = id;
        this.oldData = test.getTasks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create, container, false);
        pager = (ViewPager) view.findViewById(R.id.create_pager);
        setUpViaAdapter();
        return view;
    }

    private void setUpViaAdapter() {
        Adapter adapter = new Adapter(fragmentManager);
        if (test == null) {
            constructorFragment = new TaskConstructorFragment();
            settingsFragment = new SettingsFragment(this, this, false);
        }
        else {
            constructorFragment = new TaskConstructorFragment(test.getTasks());
            settingsFragment = new SettingsFragment(this, this, test.getSettings());
        }
        adapter.addFragment(constructorFragment);
        adapter.addFragment(settingsFragment);
        pager.setAdapter(adapter);
    }

    @Override
    public void onTestSaving(@NonNull SettingsData settings) {
        TaskData taskData = constructorFragment.getData();
        assert taskData != null;
        Test test = new Test(settings, taskData, getActivity());
        test.save(this, getContext());
    }

    @Override
    public void onTestSaved() {
        onTestSavedListener.onTestSaved();
    }

    @Override
    public boolean hasEmpty() {
        if (constructorFragment.getData() == null) {
            Snackbar.make(view, "Заполните данные", Snackbar.LENGTH_SHORT).show();
            pager.setCurrentItem(0);
            return true;
        }
        else
            return false;
    }

    @Override
    public void OnTestUpdate(@NonNull SettingsData settings) {
        TaskData taskData = constructorFragment.getData();
        assert taskData != null;
        Test test = new Test(settings, taskData, getActivity());
        test.update(id, getContext(), this);
    }

    @Override
    public void onTestUpdated() {
        onTestUpdatedListener.onTestUpdated();
    }

    @Override
    public boolean checkTasksHasBeenChanged() {
        Gson gson = new Gson();
        Log.d("TestManDebug", gson.toJson(constructorFragment.getData()) + " " + gson.toJson(oldData));
        ArrayList<TaskClass> oldTasks = oldData.getTasks();
        ArrayList<TaskClass> newTasks = constructorFragment.getData().getTasks();
        if (oldTasks.size() == newTasks.size()) {
            for (int i = 0; i < oldTasks.size(); i++) {
                TaskClass oldTask = oldTasks.get(i);
                TaskClass newTask = newTasks.get(i);
                if (!oldTask.getQuestion().equals(newTask.getQuestion())
                        || oldTask.getType() != newTask.getType()
                        || oldTask.getScores() != newTask.getScores()
                        || !gson.toJson(oldTask.getAnswers()).equals(gson.toJson(newTask.getAnswers())))
                    return true;
                if (oldTask.getType() == TaskType.RADIO_BOX.getCode()
                        && ((int) newTask.getRights()) != ((int) ((double) oldTask.getRights())))
                    return true;
                else {
                    ArrayList<Object> newRights = (ArrayList<Object>) newTask.getRights();
                    ArrayList<Object> oldRights = (ArrayList<Object>) oldTask.getRights();
                    if (newRights.size() != oldRights.size())
                        return true;
                    for (int j = 0; j < newRights.size(); j++)
                        if (((Integer) newRights.get(j)) != ((Double) oldRights.get(j)).intValue())
                            return true;
                }
            }
        }
        else {
            return true;
        }
        return false;
    }

    private class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        Adapter(FragmentManager manager) { super(manager); }

        @Override
        public Fragment getItem(int position) { return mFragmentList.get(position); }

        @Override
        public int getCount() { return mFragmentList.size(); }

        void addFragment(Fragment fragment) { mFragmentList.add(fragment); }
    }

}
