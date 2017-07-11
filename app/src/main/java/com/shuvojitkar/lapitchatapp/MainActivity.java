package com.shuvojitkar.lapitchatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Toolbar toolbar;
    private TabLayout tb;
    private ViewPager vp;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //set up the toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lapit Chat App");

        //set up the viewpager
        vp.setAdapter(mSectionsPagerAdapter);
        tb.setupWithViewPager(vp);



    }

    private void init() {
        tb = (TabLayout) findViewById(R.id.main_tabs);
        vp = (ViewPager) findViewById(R.id.main_tabpager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mAuth = FirebaseAuth.getInstance();
        toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //otherwise it return null method
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
           sendToStart();
        }
    }

    private void sendToStart() {
        startActivity(new Intent(MainActivity.this,StartActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();}
        else if(item.getItemId()==R.id.main_setting_btn){
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
        } else if(item.getItemId()==R.id.main_all_user_btn){
            startActivity(new Intent(MainActivity.this,UsersActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
