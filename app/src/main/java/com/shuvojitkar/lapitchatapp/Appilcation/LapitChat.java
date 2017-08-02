package com.shuvojitkar.lapitchatapp.Appilcation;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;
import com.shuvojitkar.lapitchatapp.GetFirebaseRef;

/**
 * Created by SHOBOJIT on 8/3/2017.
 */

public class LapitChat extends Application {
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        super.onCreate();
        new Instabug.Builder(this, "bd796240086a293ce9ffb057bbb19fe8")
                .setInvocationEvent(InstabugInvocationEvent.SHAKE)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = GetFirebaseRef.GetDbIns()
                .getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid());
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                        mUserDatabase.child("online").onDisconnect().setValue(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
