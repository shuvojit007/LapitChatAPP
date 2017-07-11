package com.shuvojitkar.lapitchatapp;

import android.app.Application;

import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

/**
 * Created by SHOBOJIT on 7/10/2017.
 */

public class InstaBug extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new Instabug.Builder(this, "bd796240086a293ce9ffb057bbb19fe8")
                .setInvocationEvent(InstabugInvocationEvent.SHAKE)
                .build();
    }
}
