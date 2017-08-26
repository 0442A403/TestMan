package com.android.petro.testman.Activities;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.android.petro.testman.Fragments.MyTestsControlFragment;
import com.android.petro.testman.Fragments.SearchFragment;
import com.android.petro.testman.R;
import com.android.petro.testman.Support.OnBackPressedListener;
import com.android.petro.testman.Support.OnTestSavedListener;
import com.vk.sdk.VKSdk;

/**
 * Created by petro on 11.06.2017.
 * Base Activity for all my fragments
 */

public class BaseActivity extends AppCompatActivity implements OnTestSavedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView upperNavigationView;
    private ActualFragment actualFragment = ActualFragment.SEARCH_FRAGMENT;
    private OnBackPressedListener onBackPressedListener;
    private final OnTestSavedListener onTestSavedListener = this;
    public static final int RESULT_TEST_UPDATED = -22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                                                            ActualFragment.SEARCH_FRAGMENT
                                                    : ActualFragment.MY_TESTS_FRAGMENT,
                                                    item.getItemId() == R.id.search_test_item?
                                                            new SearchFragment()
                                                            : new MyTestsControlFragment());
                                            drawerLayout.closeDrawer(Gravity.START);
                                        }
                                    })
                                    .setNegativeButton("Отмена", null)
                                    .show();
                            return false;
                        }
                        switch (item.getItemId()) {
                            case R.id.search_test_item:
                                SearchFragment sFragment = new SearchFragment();
                                onBackPressedListener = null;
                                changeFragment(ActualFragment.SEARCH_FRAGMENT, sFragment);
                                drawerLayout.closeDrawer(Gravity.START);
                                break;
                            case R.id.create_test_item:
                                CreateFragment cFragment = new CreateFragment(getSupportFragmentManager(), onTestSavedListener);
                                onBackPressedListener = null;
                                changeFragment(ActualFragment.CREATE_FRAGMENT, cFragment);
                                drawerLayout.closeDrawer(Gravity.START);
                                break;
                            case R.id.my_tests_item:
                                MyTestsControlFragment mtcFragment = new MyTestsControlFragment();
                                onBackPressedListener = mtcFragment;
                                changeFragment(ActualFragment.MY_TESTS_FRAGMENT, mtcFragment);
                                drawerLayout.closeDrawer(Gravity.START);
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

        changeFragment(ActualFragment.SEARCH_FRAGMENT, new SearchFragment());
    }

    public void changeFragment(ActualFragment actualFragment, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
        this.actualFragment = actualFragment;
        switch (actualFragment) {
            case SEARCH_FRAGMENT:
                setTitle("Найти тест");
                upperNavigationView.setCheckedItem(R.id.search_test_item);
                break;
            case CREATE_FRAGMENT:
                setTitle("Создать тест");
                upperNavigationView.setCheckedItem(R.id.create_test_item);
                break;
            case MY_TESTS_FRAGMENT:
                setTitle("Мои тесты");
                upperNavigationView.setCheckedItem(R.id.my_tests_item);
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedListener == null || onBackPressedListener.onBackPressed())
            super.onBackPressed();
    }

    @Override
    public void onTestSaved() {
        changeFragment(ActualFragment.SEARCH_FRAGMENT, new SearchFragment());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TestManDebug", "12313");
        if (resultCode == RESULT_TEST_UPDATED)
            changeFragment(ActualFragment.SEARCH_FRAGMENT, new SearchFragment());
    }

    private enum ActualFragment {
        SEARCH_FRAGMENT,
        CREATE_FRAGMENT,
        MY_TESTS_FRAGMENT
    }
}
