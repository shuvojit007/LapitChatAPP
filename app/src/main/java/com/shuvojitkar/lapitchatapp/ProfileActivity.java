package com.shuvojitkar.lapitchatapp;

import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfilename,mProfilestatus,mProfiletotalfriends;
    private Button mProfileSendReqBtn;
    private ImageView mProfileImage;
    private DatabaseReference mDatabase;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        String id = getIntent().getStringExtra("user_id");
        init();

        pd.setTitle("Loading User Data");
        pd.setMessage("Please wait while er load the user data");
        pd.setCanceledOnTouchOutside(false);




        mDatabase  = GetFirebaseRef.GetDbIns().getReference().child("Users").child(id);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Name = dataSnapshot.child("name").getValue().toString();
                String Status = dataSnapshot.child("status").getValue().toString();
                String Image = dataSnapshot.child("image").getValue().toString();

                mProfilename.setText(Name);
                mProfilestatus.setText(Status);

                if (!Image.equals("default")){
                    Picasso.with(getApplicationContext())
                            .load(Image)
                            .placeholder(R.drawable.person2)
                            .into(mProfileImage);
                }
                
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
    }
}
