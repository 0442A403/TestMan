package com.android.petro.testman.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.petro.testman.R;
import com.android.petro.testman.Support.CreateCallBack;
import com.android.petro.testman.Support.SettingsData;
import com.android.petro.testman.Support.TasksData;
import com.android.petro.testman.Support.TestClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for creating new tests
 */

public class CreateFragment extends Fragment implements CreateCallBack {
    FragmentManager fragmentManager = null;
    ViewPager pager = null;
    TasksFragment tasksFragment;
    SettingsFragment settingsFragment;

    public CreateFragment(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);
        pager = (ViewPager) view.findViewById(R.id.create_pager);

        //Constructor occurs earlier than onCreateView
        setUpViaAdapter();
        return view;
    }

    private void setUpViaAdapter() {
        Adapter adapter = new Adapter(fragmentManager);
        tasksFragment = new TasksFragment();
        settingsFragment = new SettingsFragment(this);
        adapter.addFragment(tasksFragment);
        adapter.addFragment(settingsFragment);
        pager.setAdapter(adapter);
    }

    @Override
    public void onTestSave(@NonNull SettingsData settings) {
        TasksData tasksData = tasksFragment.getData();
        TestClass test = new TestClass(settings, tasksData);
        test.save();
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
