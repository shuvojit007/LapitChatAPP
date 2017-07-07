package com.shuvojitkar.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register_Activity extends AppCompatActivity {
    TextInputLayout mDisplayName, mEmail, mPassword;
    Button mCreateButton;
    private ProgressDialog mDialog;
    private DatabaseReference mDatabase;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init(); //initialize the component

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true
        );


        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email =mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                if(display_name.equals("")||email.equals("")||password.equals("")){
                    Toast.makeText(Register_Activity.this, "Please enter a valid data ", Toast.LENGTH_SHORT).show();
                }else {
                    mDialog.setTitle("Registering User");
                    mDialog.setMessage("Please wait while we create your account !");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    register_user(display_name,email,password);
                }
            }
        });
    }

   /* Step 4 :
    Create a new account by passing the
    new user's email address and password to createUserWithEmailAndPassword */
    private void register_user(final String display_name, String email, String password) {
      mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(Task<AuthResult> task) {
              if(task.isSuccessful()) {
                  FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
                  String Uid = currentuser.getUid();
                  mDatabase = GetDatabaseRef.GetDbIns().getReference().child("Users").child(Uid);
                  HashMap<String, String> UserMap = new HashMap<String, String>();
                  UserMap.put("name", display_name);
                  UserMap.put("status", "Hi there, i'm using chat app");
                  UserMap.put("image", "Default");
                  UserMap.put("thumb_image", "deafult");

                    /*public interface OnCompleteListener
                    Listener called when a Task complete*/

                  mDatabase.setValue(UserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          if (task.isSuccessful()){
                              mDialog.dismiss();
                              startActivity(new Intent(Register_Activity.this, MainActivity.class));
                              finish();
                          }
                      }
                  });


              }
              else {
                  mDialog.hide();
                      String error = "";
                      try {
                          throw task.getException();
                      } catch (FirebaseAuthWeakPasswordException e) {
                          error = "Weak Password!";
                      } catch (FirebaseAuthInvalidCredentialsException e) {
                          error = "Invalid Email";
                      } catch (FirebaseAuthUserCollisionException e) {
                          error = "Existing account!";
                      } catch (Exception e) {
                          error = "Unknow error!";
                          e.printStackTrace();
                      }
                      Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
              }
          }
      });
    }

    private void init() {
        mCreateButton = (Button) findViewById(R.id.reg_create_btn);
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail= (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mToolbar = (Toolbar) findViewById(R.id.reg_toolbar);
        mDialog=new ProgressDialog(this);
        // Step 1
        // get the shared instance of the FirebaseAuth object:
        mAuth = FirebaseAuth.getInstance();
    }
}
