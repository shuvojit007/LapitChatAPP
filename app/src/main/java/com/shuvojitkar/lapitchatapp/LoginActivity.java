package com.shuvojitkar.lapitchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextInputLayout mLoginEmail;
    private ProgressDialog mProgress;
    private TextInputLayout mLoginPass;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mLoginEmail.getEditText().getText().toString();
                String pass = mLoginPass.getEditText().getText().toString();
                if (!TextUtils.isEmpty(email)||!TextUtils.isEmpty(pass)){
                    mProgress.setTitle("Logging in");
                    mProgress.setMessage("Please wait while we check your credentials");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();
                    LoginUser(email,pass);
                }
            }
        });
    }

    private void LoginUser(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete( Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mProgress.dismiss();
                    startActivity(new Intent(LoginActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }else {
                    mProgress.hide();
                    Toast.makeText(LoginActivity.this, "Cannot Sign in. Please check the form and try again.", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
    mProgress = new ProgressDialog(this);
    mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
    mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
    mLoginPass = (TextInputLayout) findViewById(R.id.login_password);
    }
}
