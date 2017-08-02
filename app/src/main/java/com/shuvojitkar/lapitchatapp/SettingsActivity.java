package com.shuvojitkar.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.R.attr.bitmap;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private Context cn;
    private static int GALLERY_PICK = 1;
    private FirebaseUser firebaseUser;
    private ProgressDialog img_up_pd;
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
        final String currentId = firebaseUser.getUid();

       mDatabase = GetFirebaseRef.GetDbIns().getReference().child("Users").child(currentId);

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
                String image = dataSnapshot.child("image").getValue().toString();
                //String thumb_image= dataSnapshot.child("thumb_image").getValue().toString();

                   mName.setText(name);
                   mStatus.setText(status);




              if (!image.equals("default")|| !image.equals("")||!image.equals(null)) {
                   Picasso.with(cn)
                           .load(image)
                           .placeholder(R.drawable.person2)
                           .into(mCircleImageView);
               }


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
                        .setMinCropWindowSize(500,500)
                        .start(SettingsActivity.this);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabase.child("online").setValue(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                img_up_pd = new ProgressDialog(cn);
                img_up_pd.setTitle("Uploading Image....");
                img_up_pd.setMessage("Please wait while we upload and process the image");
                img_up_pd.setCanceledOnTouchOutside(false);
                img_up_pd.show();

                //upload the file to storage
                //get the Uri of selected Image
                Uri resultUri = result.getUri();

                //Now Compress it for thumbline
                File thumb_image = new File(resultUri.getPath());
                Bitmap thumb_bitmap = null;
                try {
                     thumb_bitmap = new Compressor(this)
                            .setQuality(75)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .compressToBitmap(thumb_image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*The putBytes() method is the simplest way to upload a file to Cloud Storage.
                putBytes() takes a byte[] and returns an UploadTask that you can use to
                manage and monitor the status of the upload.*/
                //Process the bitmap for Firebase
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                //Storage Reference for main image
                StorageReference filepath = mImageStorage.child("profile_images").child(firebaseUser.getUid()+".jpg");

                //Storage Reference for thumb image
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(firebaseUser.getUid()+".jpg");

                //Now first upload the main image
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete( Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String download_url = task.getResult().getDownloadUrl().toString();


                            //after that now upload the thumb image
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete( Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_donwload_url = thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()){

                                    //After the Upload of both file now we
                                    //update the database
                                    //Error found --> Hashmap not update the value its set the value
                                        //thats why now use map for just update the value
                                    Map map = new HashMap();
                                    map.put("image",download_url);
                                    map.put("thumb_image",thumb_donwload_url);
                                        //for map we use updatechildren
                                    mDatabase.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete( Task<Void> task) {
                                            if (task.isSuccessful()){
                                                img_up_pd.dismiss();
                                                Toast.makeText(cn, "thumbline Upload Succesfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else {
                                    img_up_pd.dismiss();
                                    Toast.makeText(cn, "Error in Uploading thumbline", Toast.LENGTH_SHORT).show();
                                }
                                }
                            });


                        /*    //update the database for Main Image
                            mDatabase.child("image").setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete( Task<Void> task) {
                                if (task.isSuccessful()){
                                    img_up_pd.dismiss();
                                    Toast.makeText(cn, "Image Upload Succesfully", Toast.LENGTH_SHORT).show();
                                 }
                              }
                            });*/
                        }else {
                            img_up_pd.dismiss();
                            Toast.makeText(cn, "Error in Image Uploading", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

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
                        public void onComplete(Task<Void> task) {
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
        mName = (TextView) findViewById(R.id.settings_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mCircleImageView = (CircleImageView) findViewById(R.id.settings_image);
        mImagebtn = (Button) findViewById(R.id.settings_image_btn);
        mStatusbtn = (Button) findViewById(R.id.settings_status_btn);
    }

    //Android random string generator
    //Link --> https://stackoverflow.com/questions/12116092/android-random-string-generator
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
