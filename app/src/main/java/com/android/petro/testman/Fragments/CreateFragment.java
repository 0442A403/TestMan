package com.android.petro.testman.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.petro.testman.R;
import com.android.petro.testman.Support.OnTestSaveListener;
import com.android.petro.testman.Support.OnTestSavedListener;
import com.android.petro.testman.Support.OnTestUpdateListener;
import com.android.petro.testman.Support.OnTestUpdatedListener;
import com.android.petro.testman.Support.SettingsData;
import com.android.petro.testman.Support.TaskData;
import com.android.petro.testman.Support.Test;

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
