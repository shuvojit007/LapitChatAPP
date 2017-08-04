package com.shuvojitkar.lapitchatapp.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shuvojitkar.lapitchatapp.ChatActivity;
import com.shuvojitkar.lapitchatapp.Data.Friends;
import com.shuvojitkar.lapitchatapp.GetFirebaseRef;
import com.shuvojitkar.lapitchatapp.ProfileActivity;
import com.shuvojitkar.lapitchatapp.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private View v;
    private RecyclerView mFriendList;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUserDatabase;


    String mCurrent_user_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        v= inflater.inflate(R.layout.fragment_friends,container,false);
        mFriendList = (RecyclerView) v.findViewById(R.id.friend_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendDatabase = GetFirebaseRef.GetDbIns().getReference().child("Friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);

        mUserDatabase = GetFirebaseRef.GetDbIns().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        init(v);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(v.getContext()));

        // Inflate the layout for this fragment
        return v;
    }

    private void init(View v) {
        mFriendList = (RecyclerView) v.findViewById(R.id.friend_list);

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter <Friends ,FriendsViewHolder> firebaseRecylerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                        Friends.class,
                        R.layout.users_single_layout,
                        FriendsViewHolder.class,
                        mFriendDatabase
                ) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                        viewHolder.setDate(model.getDate());
                        final String list_user_id = getRef(position).getKey();

                        mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String name = dataSnapshot.child("name").getValue().toString();
                                String thum_image = dataSnapshot.child("thumb_image").getValue().toString();

                               if (dataSnapshot.hasChild("online")){
                                   String online =  dataSnapshot.child("online").getValue().toString();
                                   viewHolder.setOnline(online);
                               }

                                viewHolder.setName(name);
                                viewHolder.setImage(thum_image);

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CharSequence [] options = new CharSequence[]{"Open Profile","Send Message"};

                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                            builder.setTitle("Selcet Options");
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which ==0){
                                                        startActivity(new Intent(getContext(), ProfileActivity.class)
                                                                .putExtra("user_id",list_user_id));
                                                    }
                                                    if (which==1){
                                                        Intent in = new Intent(getContext(), ChatActivity.class);
                                                        in.putExtra("user_name",name);
                                                        in.putExtra("user_id",list_user_id);
                                                        startActivity(in);

                                                    }

                                                }
                                            });
                                        builder.show();
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };

                mFriendList.setAdapter(firebaseRecylerAdapter);

    }




    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView userDate = (TextView) mView.findViewById(R.id.user_single_status);
            userDate.setText(date);
        }

        public void setName(String name) {
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name);
            userName.setText(name);
        }

        public void setImage(String image) {
            CircleImageView userImage = (CircleImageView) mView.findViewById(R.id.user_single_image);
            if (!image.equals("")){
                Picasso.with(mView.getContext()).load(image).into(userImage);
            }
        }


        public void setOnline(String status) {
            ImageView img = (ImageView) mView.findViewById(R.id.user_single_online_icon);
            if (status.equals("true")){
                img.setVisibility(View.VISIBLE);
            }else {
                img.setVisibility(View.GONE);
            }
        }
    }
}


