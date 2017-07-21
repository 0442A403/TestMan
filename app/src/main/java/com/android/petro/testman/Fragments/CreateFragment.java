package com.android.petro.testman.Fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;

import com.android.petro.testman.Activities.BaseActivity;
import com.android.petro.testman.R;
import com.android.petro.testman.Support.CreateCallBack;
import com.android.petro.testman.Support.SettingsData;
import com.android.petro.testman.Support.TasksData;
import com.android.petro.testman.Support.TestClass;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Fragment for creating new tests
 */

public class CreateFragment extends Fragment implements CreateCallBack {
    FragmentManager fragmentManager = null;
    ViewPager pager = null;
    TasksFragment tasksFragment;
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
        tasksFragment = new TasksFragment();
        settingsFragment = new SettingsFragment(this);
        adapter.addFragment(tasksFragment);
        adapter.addFragment(settingsFragment);
        pager.setAdapter(adapter);
    }

    @Override
    public void onTestSave(@NonNull SettingsData settings) {
        TasksData tasksData = tasksFragment.getData();
        if (tasksData == null) {
            Snackbar.make(view, "Заполните данные", Snackbar.LENGTH_SHORT).show();
            pager.setCurrentItem(0);
            return;
        }

        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Подождите");
        dialog.show();

        TestClass test = new TestClass(settings, tasksData, getActivity());
        test.save(this);
    }

    @Override
    public boolean checkEmpty() {
        TasksData tasksData = tasksFragment.getData();
        if (tasksData == null) {
            Snackbar.make(view, "Заполните данные", Snackbar.LENGTH_SHORT).show();
            pager.setCurrentItem(0);
            return true;
        }
        return false;
    }

    @Override
    public void onSaved() {
        dialog.dismiss();
        activity.changeFragment(new SearchFragment());
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
