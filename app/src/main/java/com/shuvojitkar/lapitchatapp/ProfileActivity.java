package com.shuvojitkar.lapitchatapp;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfilename,mProfilestatus,mProfiletotalfriends;
    private Button mProfileSendReqBtn,mProfileDeclinereqBtn;
    private ImageView mProfileImage;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendsReqDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog pd;
    private String mCurrentState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String id = getIntent().getStringExtra("user_id");
        init();

        mCurrentState ="not_friends";


        pd.setTitle("Loading User Data");
        pd.setMessage("Please wait while er load the user data");
        pd.setCanceledOnTouchOutside(false);
        if (haveNetworkConnection()==true){
            pd.show();
        }

        //Firebase Initialization
        mFriendsReqDatabase = GetFirebaseRef.GetDbIns().getReference().child("Frnds_req");
        mFriendDatabase = GetFirebaseRef.GetDbIns().getReference().child("Friends");
        mUserDatabase = GetFirebaseRef.GetDbIns().getReference().child("Users").child(id);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Read Data From Firebase
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Name = dataSnapshot.child("name").getValue().toString();
                String Status = dataSnapshot.child("status").getValue().toString();
                String Image = dataSnapshot.child("image").getValue().toString();
                mProfilename.setText(Name);
                mProfilestatus.setText(Status);

                if (!Image.equals("default")){
                    Picasso.with(getApplicationContext()).load(Image).placeholder(R.drawable.person2).into(mProfileImage);}
                
                mFriendsReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //------Friends List /Request Features
                        mFriendsReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(id)){
                                    String req_type = dataSnapshot.child(id).child("req_type").getValue().toString();
                                    if (req_type.equals("received")){
                                        mCurrentState = "req_received";
                                        mProfileSendReqBtn.setText("Accept Friend Request");

                                    }else if(req_type.equals("sent")){
                                        mCurrentState = "req_sent";
                                        mProfileSendReqBtn.setText("Cancel Friend Request");

                                    }
                                }else {
                                    mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)){
                                                mCurrentState = "friends";
                                                mProfileSendReqBtn.setText("UnFriend this Person");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            pd.dismiss();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                pd.dismiss();
                            }
                        });

                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendReqBtn.setEnabled(false);

              //  ------Not Friend State-----

                if (mCurrentState.equals("not_friends")){
                    mFriendsReqDatabase
                            .child(mCurrentUser.getUid())
                            .child(id)
                            .child("req_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendsReqDatabase.child(id).child(mCurrentUser.getUid()).child("req_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mCurrentState = "req_sent";
                                        mProfileSendReqBtn.setText("Cancel Friend Request");
                                     //   Toast.makeText(ProfileActivity.this, "Request Send Succesfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }

                //  ------Cancel Request State-----
                if (mCurrentState.equals("req_sent")){
                    mFriendsReqDatabase.child(mCurrentUser.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendsReqDatabase.child(id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mProfileSendReqBtn.setText("Sent Friend Request");
                                }
                            });
                        }
                    });
                }

                // ------REQ RECEIVED STATE
                if (mCurrentState.equals("req_received")){
                    final String CurrentDate  = DateFormat.getDateInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(id).setValue(CurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(id).child(mCurrentUser.getUid()).setValue(CurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendsReqDatabase.child(mCurrentUser.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendsReqDatabase.child(id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrentState = "friends";
                                                    mProfileSendReqBtn.setText("UnFriend this Person");
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    });

                }


                // ------FRIENDS STATE
                if (mCurrentState.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mProfileSendReqBtn.setText("Sent Friend Request");
                                }
                            }) ;
                        }
                    });
                }


            }
        });

    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void init() {
        pd =new ProgressDialog(this);
        mProfilename = (TextView) findViewById(R.id.profile_displayName);
        mProfilestatus = (TextView) findViewById(R.id.profile_status);
        mProfiletotalfriends = (TextView) findViewById(R.id.profile_totalfriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_sendRequest);
        mProfileDeclinereqBtn = (Button) findViewById(R.id.profile_declinereq);
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
    }
}
