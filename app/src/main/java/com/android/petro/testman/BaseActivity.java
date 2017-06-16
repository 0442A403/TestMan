package com.android.petro.testman;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.webkit.CookieManager;

/**
 * Created by petro on 11.06.2017.
 * Base Activity for all my fragments
 */

public class BaseActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;

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

        changeFragment(new SearchFragment());

        NavigationView upperNavigationView = (NavigationView) findViewById(R.id.upper_nav_view);
        NavigationView lowerNavigationView = (NavigationView) findViewById(R.id.exit_nav_view);

        upperNavigationView.setCheckedItem(R.id.search_test_item);
        upperNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.search_test_item:
                                changeFragment(new SearchFragment());
                                drawerLayout.closeDrawer(Gravity.START);
                                setTitle("Найти тест");
                                break;
                            case R.id.create_test_item:
                                changeFragment(new CreateFragment(getSupportFragmentManager()));
                                drawerLayout.closeDrawer(Gravity.START);
                                setTitle("Создать тест");
                                break;
                            case R.id.my_tests_item:
                                changeFragment(new MyTestsFragment());
                                drawerLayout.closeDrawer(Gravity.START);
                                setTitle("Мои тесты");
                                break;
                        }
                        return true;
                    }
                });
        lowerNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                        CookieManager.getInstance().removeAllCookie();
                        startActivity(intent);
                        finish();
                        return false;
                    }
                });

    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment).commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

}
