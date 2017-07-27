package com.android.petro.testman.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.webkit.CookieManager;

import com.android.petro.testman.Fragments.CreateFragment;
import com.android.petro.testman.Fragments.MyTestsFragment;
import com.android.petro.testman.Fragments.SearchFragment;
import com.android.petro.testman.R;

/**
 * Created by petro on 11.06.2017.
 * Base Activity for all my fragments
 */

public class BaseActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView upperNavigationView;
    ActualFragment actualFragment = ActualFragment.SEARCH_FRAGMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

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
                        SharedPreferences.Editor editor = getSharedPreferences("AppPref", MODE_PRIVATE).edit();
                        editor.putString("Token", null);
                        editor.putLong("Last Opening", 0L);
                        editor.apply();
                        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                        CookieManager.getInstance().removeAllCookie();
                        startActivity(intent);
                        finish();
                        return false;
                    }
                });

        changeFragment(new SearchFragment());

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


    enum ActualFragment {
        SEARCH_FRAGMENT,
        CREATE_FRAGMENT,
        MY_TESTS_FRAGMENT
    }
}
