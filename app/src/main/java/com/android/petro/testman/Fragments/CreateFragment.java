package com.android.petro.testman.Fragments;

import android.app.ProgressDialog;
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

import com.android.petro.testman.Activities.BaseActivity;
import com.android.petro.testman.R;
import com.android.petro.testman.Support.SettingsData;
import com.android.petro.testman.Support.TasksData;
import com.android.petro.testman.Support.TestClass;
import com.android.petro.testman.Support.onTestSave;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for creating new tests
 */

public class CreateFragment extends Fragment implements onTestSave {
    FragmentManager fragmentManager = null;
    ViewPager pager = null;
    TaskConstructorFragment constructorFragment;
    SettingsFragment settingsFragment;
    BaseActivity activity;
    ProgressDialog dialog;
    View view;

    public CreateFragment(FragmentManager fragmentManager, BaseActivity activity) {
        this.fragmentManager = fragmentManager;
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create, container, false);
        pager = (ViewPager) view.findViewById(R.id.create_pager);

        //Constructor occurs earlier than onCreateView
        setUpViaAdapter();
        return view;
    }

    private void setUpViaAdapter() {
        Adapter adapter = new Adapter(fragmentManager);
        constructorFragment = new TaskConstructorFragment();
        settingsFragment = new SettingsFragment(this);
        adapter.addFragment(constructorFragment);
        adapter.addFragment(settingsFragment);
        pager.setAdapter(adapter);
    }

    @Override
    public void onTestSaving(@NonNull SettingsData settings) {
        TasksData tasksData = constructorFragment.getData();

        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Подождите");
        dialog.show();

        assert tasksData != null;
        TestClass test = new TestClass(settings, tasksData, getActivity());
        test.save(this);
    }

    @Override
    public void onTestSaved() {
        dialog.dismiss();
        activity.changeFragment(new SearchFragment());
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
