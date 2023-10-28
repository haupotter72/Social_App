package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterMainActivity extends AppCompatActivity {
    EditText mEmailEt,mPasswordEt;
    Button mRegisterBtn;
    ProgressDialog progressDialog;

    TextView mHaveAccountTv;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        mEmailEt =findViewById(R.id.emailEt);
        mPasswordEt=findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mAuth=FirebaseAuth.getInstance();
        mHaveAccountTv=findViewById(R.id.have_accountTv);



        progressDialog =new ProgressDialog(this);
        progressDialog.setMessage("Resgistering user...");


        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                String email=mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }else if(password.length()<6){
                    mPasswordEt.setError("password length at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }else {
                    registerUser(email,password);
                }

            }

        });

        //handle Login textview click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterMainActivity.this,LoginMainActivity.class));
                finish();

            }
        });



    }
    private void registerUser(String email,String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();

                    FirebaseUser user = mAuth.getCurrentUser();

                    // get email and uid from auth
                    String email=user.getEmail();
                    String uid =user.getUid();

                    //using hashmap
                    HashMap<Object,String> hashMap =new HashMap<>();

                    // thong tin hashmap
                    hashMap.put("email",email);
                    hashMap.put("uid",uid);
                    hashMap.put("name","");
                    hashMap.put("TypingTo","noOne");
                    hashMap.put("phone","");
                    hashMap.put("image","");
                    hashMap.put("cover","");


                    //firebase database isntance

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Users");
                    reference.child(uid).setValue(hashMap);


                    Toast.makeText(RegisterMainActivity.this, "Register... \n" +user.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterMainActivity.this, Dashboardctivity.class));
                    finish();

                } else {
                    progressDialog.dismiss();

                    Toast.makeText(RegisterMainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();

                }
            }
        } ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });



    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return super.onSupportNavigateUp();

    }
}