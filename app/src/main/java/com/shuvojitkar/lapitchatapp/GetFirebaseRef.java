package com.shuvojitkar.lapitchatapp;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Created by SHOBOJIT on 7/6/2017.
 */

public class GetFirebaseRef {
    private static  FirebaseDatabase mFirebaseDatabase;
    private  static FirebaseStorage mFirebaseStorage;

    public static FirebaseDatabase GetDbIns(){
        if (mFirebaseDatabase ==null){
            mFirebaseDatabase =FirebaseDatabase.getInstance();
            mFirebaseDatabase.setPersistenceEnabled(true);
            return mFirebaseDatabase;
        }
        return mFirebaseDatabase;
    }


}
