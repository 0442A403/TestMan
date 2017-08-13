package com.android.petro.testman.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;

import com.android.petro.testman.Fragments.CreateFragment;
import com.android.petro.testman.Fragments.MyTestsFragment;
import com.android.petro.testman.Fragments.SearchFragment;
import com.android.petro.testman.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by petro on 11.06.2017.
 * Base Activity for all my fragments
 */

public class BaseActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private NavigationView upperNavigationView;
    private ActualFragment actualFragment = ActualFragment.SEARCH_FRAGMENT;
    public static int TEST_NOT_STARTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        upperNavigationView = (NavigationView) findViewById(R.id.upper_nav_view);
        NavigationView lowerNavigationView = (NavigationView) findViewById(R.id.exit_nav_view);

        upperNavigationView.setCheckedItem(R.id.search_test_item);
        upperNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                        if (actualFragment == ActualFragment.CREATE_FRAGMENT) {
                            if (item.getItemId() == R.id.create_test_item) {
                                drawerLayout.closeDrawer(Gravity.START);
                                return true;
                            }
                            new AlertDialog.Builder(BaseActivity.this)
                                    .setTitle("Данные будут потеряны. Выйти?")
                                    .setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            changeFragment(
                                                    item.getItemId() == R.id.search_test_item?
                                                            new SearchFragment()
                                                            : new MyTestsFragment());
                                            drawerLayout.closeDrawer(Gravity.START);
                                        }
                                    })
                                    .setNegativeButton("Отмена", null)
                                    .show();
                            return false;
                        }
                        switch (item.getItemId()) {
                            case R.id.search_test_item:
                                changeFragment(new SearchFragment());
                                drawerLayout.closeDrawer(Gravity.START);
                                break;
                            case R.id.create_test_item:
                                changeFragment(new CreateFragment(getSupportFragmentManager(), BaseActivity.this));
                                drawerLayout.closeDrawer(Gravity.START);
                                break;
                            case R.id.my_tests_item:
                                startActivity(new Intent(BaseActivity.this, SolveActivity.class));
                                break;
                        }
                        return true;
                    }
                });
        lowerNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        VKSdk.logout();
                        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        return false;
                    }
                });

        changeFragment(new SearchFragment());
        Log.i("Token", VKAccessToken.ACCESS_TOKEN);
    }

    public void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
        if (fragment.getClass() == SearchFragment.class) {
            setTitle("Найти тест");
            upperNavigationView.setCheckedItem(R.id.search_test_item);
            actualFragment = ActualFragment.SEARCH_FRAGMENT;
        }
        else if (fragment.getClass() == CreateFragment.class) {
            setTitle("Создать тест");
            upperNavigationView.setCheckedItem(R.id.create_test_item);
            actualFragment = ActualFragment.CREATE_FRAGMENT;
        }
        else {
            setTitle("Мои тесты");
            upperNavigationView.setCheckedItem(R.id.my_tests_item);
            actualFragment = ActualFragment.MY_TESTS_FRAGMENT;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }


    private enum ActualFragment {
        SEARCH_FRAGMENT,
        CREATE_FRAGMENT,
        MY_TESTS_FRAGMENT
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TEST_NOT_STARTED) {
            new AsyncTask<Void, Void, Void>() {
                ProgressDialog dialog;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog = new ProgressDialog(BaseActivity.this);
                    dialog.setTitle("Подождите");
                    dialog.setCancelable(false);
                    dialog.show();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    FormBody body = new FormBody.Builder()
                            .add("id", String.valueOf(data.getIntExtra("id", -1)))
                            .build();
                    Log.i("InterruptedAnswerId", String.valueOf(data.getIntExtra("id", -89)));

                    Request request = new Request.Builder()
                            .url("https://testman-o442a4o3.c9users.io/remove_answer/")
                            .post(body)
                            .build();

                    try {
                        String responseString = new OkHttpClient().newCall(request).execute().body().string();
                        Log.i("AnswerInterrupted", responseString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    dialog.dismiss();
                }
            }.execute();
        }
    }
}
