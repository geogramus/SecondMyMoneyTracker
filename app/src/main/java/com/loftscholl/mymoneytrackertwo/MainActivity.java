package com.loftscholl.mymoneytrackertwo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabs;
    private ViewPager pages;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!((LSApp) getApplication()).isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
        } else {
            tabs = (TabLayout) findViewById(R.id.tabs);
            pages = (ViewPager) findViewById(R.id.pages);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            pages.setAdapter(new MainPagerAdapter());
            tabs.setupWithViewPager(pages);
        }
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {
        private final String[] types = {"expense",
                "income"};
        private final String[] titles;

        MainPagerAdapter() {
            super(getSupportFragmentManager());
            titles = getResources().getStringArray(R.array.main_pager_titles);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == getCount() - 1)
                return new BalanceFragment();

            final ItemsFragment fragment = new ItemsFragment();
            Bundle args = new Bundle();
            if (position == getCount() - 2) {
                args.putString(ItemsFragment.ARG_TYPE, Item.TYPE_EXPENSE);
            } else {
                args.putString(ItemsFragment.ARG_TYPE, Item.TYPE_INCOME);
            }
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public CharSequence getPageTitle(int position) {

            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;

        }
    }
}
