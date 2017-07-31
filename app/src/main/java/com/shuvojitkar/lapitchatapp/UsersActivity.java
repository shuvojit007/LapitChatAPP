package com.shuvojitkar.lapitchatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;
import com.shuvojitkar.lapitchatapp.Data.Users;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private Context cn;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        cn=this;
        init();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = GetFirebaseRef.GetDbIns().getReference().child("Users");

        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));

    }


    private void init() {
        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        mUserList = (RecyclerView) findViewById(R.id.users_list);

    }

    @Override
    protected void onStart() {
        super.onStart();
       /* Basically we use Different class for Recyler adapter and extend it Recycler Adapter
        after that create a innerclass extends the view holder class
            but now we use predefined FirebaseRecyclerAdapter for this*/

       //Now pass the Model class , Layout , and viewholder
        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UserViewHolder.class,mUserDatabase
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setImage(model.getThumb_image());
                viewHolder.setStatus(model.getStatus());


                final String user_id = getRef(position).getKey();

               viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                     //  Toast.makeText(cn, user_id, Toast.LENGTH_SHORT).show();
                       startActivity(new Intent(UsersActivity.this,ProfileActivity.class).putExtra("user_id",user_id));
                   }
               });
            }
        };

    mUserList.setAdapter(firebaseRecyclerAdapter);
    }
    

    //Here we create the viewHolder
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setImage(String image){
            CircleImageView mUserImage = (CircleImageView) mView.findViewById(R.id.user_single_image);

            if (!image.equals("default")|| !image.equals("")||!image.equals(null)) {
            Picasso.with(mView.getContext())
                    .load(image)
                    .placeholder(R.drawable.person2)
                    .into(mUserImage);
            }

        }

        public void setStatus(String status){
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }


    }
}
