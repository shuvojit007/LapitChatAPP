package com.shuvojitkar.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.path;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private Context cn;
    private static int GALLERY_PICK = 1;
    private static ProgressBar imgpd;
    private FirebaseUser firebaseUser;
    private TextView mName, mStatus;
    private StorageReference mImageStorage;
    private Button mImagebtn, mStatusbtn;
    private CircleImageView mCircleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        cn = this;
        intit();
        //get the id of the Current Users
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentId = firebaseUser.getUid();

        mDatabase = GetDatabaseRef.GetDbIns().getReference().child("Users").child(currentId);

        //Set up Storage Ref Instance
        mImageStorage =FirebaseStorage.getInstance().getReference();

        /*To read data at a path and listen for changes,
        use the addValueEventListener() oraddListenerForSingleValueEvent()
        method to add a ValueEventListener to a DatabaseReference.*/

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                mName.setText(name);
                mStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             ChangeStatus();
            }
        });

        mImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           /*   Intent gallaryIntent = new Intent().setType("image*//**//*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallaryIntent,"SELECT IMAGE"),GALLERY_PICK);
                */
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity().setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
            }
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imgpd.setVisibility(View.VISIBLE);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                StorageReference filepath = mImageStorage.child("profile_images").child("profile_image.jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete( Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            imgpd.setVisibility(View.GONE);
                            Toast.makeText(cn, "Image Upload Succesfully", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(cn, "Error in Image Uploading", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                imgpd.setVisibility(View.GONE);
            }
        }
    }

    private void ChangeStatus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.status_dialog,null);
        final Button savebtn = (Button) dialogView.findViewById(R.id.dialog_status_btn);
        final TextInputLayout statusEdittext = (TextInputLayout) dialogView.findViewById(R.id.dialog_status);
        statusEdittext.getEditText().setText(mStatus.getText().toString());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String status = statusEdittext.getEditText().getText().toString();
                savebtn.setVisibility(View.GONE);
                statusEdittext.setVisibility(View.GONE);
                final ProgressBar pd = (ProgressBar) dialog.findViewById(R.id.pd);
                pd.setVisibility(View.VISIBLE);
                 if(!TextUtils.isEmpty(status)){
                    mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                pd.setVisibility(View.GONE);
                                dialog.dismiss();
                            }else {
                                Toast.makeText(cn, "Please Try Again Letter", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });

                }else {
                    Toast.makeText(cn, "Please Try Again Letter", Toast.LENGTH_SHORT).show();
                    dialog.hide();
                }
            }
        });
    }

    private void intit() {
        imgpd = (ProgressBar) findViewById(R.id.settings_img_pd);
        mName = (TextView) findViewById(R.id.settings_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mCircleImageView = (CircleImageView) findViewById(R.id.settings_image);
        mImagebtn = (Button) findViewById(R.id.settings_image_btn);
        mStatusbtn = (Button) findViewById(R.id.settings_status_btn);
    }

    //Android random string generator
    //Link 
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
