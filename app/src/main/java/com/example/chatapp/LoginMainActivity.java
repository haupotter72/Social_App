package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginMainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    //views
    EditText mEmailEt,mPasswordEt;
    TextView notHaveAccountTV,mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;


    // firebaseauth
    private FirebaseAuth mAuth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login Please");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //before mAuth
// Configure Google Sign In
GoogleSignInOptions gso = new GoogleSignInOptions. Builder (GoogleSignInOptions. DEFAULT_SIGN_IN)
        .requestIdToken(getString (R.string.default_web_client_id))
        .requestEmail().build();


        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth =FirebaseAuth.getInstance();

        // init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccountTV = findViewById(R.id.nothave_accountTv);
        mLoginBtn = findViewById(R.id.login_btn);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        mGoogleLoginBtn = findViewById(R.id.googleLogin_btn);


        // Login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // input data
                String email=mEmailEt.getText().toString();
                String passw=mPasswordEt.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);

                }else {
                    loginUser(email,passw);

                }


            }
        });

        // not have acc textview click

        notHaveAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginMainActivity.this,RegisterMainActivity.class));
                finish();

            }
        });

        // recover password

        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();

            }
        });

        //handle google login
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin gg sign in
//                Intent signInIntent=mGoogleSignInClient.getSignInIntent();


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!= null){
                    Intent intent = mGoogleSignInClient.getSignInIntent();
                    startActivity(intent);
                }



            }
        });


        //init progress dialog

        pd =new ProgressDialog(this);

    }


    private void showRecoverPasswordDialog() {
//alert dialog
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout =new LinearLayout(this);


        //view to set dialog
         final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buton recover

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

           String email= emailEt.getText().toString().trim();
           beginRecovery(email);
            }
        });

        //buton cancle

        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

           dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();





    }


    private void beginRecovery (String email){

        pd.setMessage("Sending email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginMainActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginMainActivity.this,"Fail...",Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    private  void loginUser(String email,String passw){
        //show progress dialog
        pd.setMessage("Logging In...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            pd.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //start Login
                            startActivity(new Intent(LoginMainActivity.this, Dashboardctivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.

                            pd.dismiss();
                            Toast.makeText(LoginMainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        //error get and show erroe message
                        Toast.makeText(LoginMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });


    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return super.onSupportNavigateUp();

    }
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {

                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle (GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential (acct.getIdToken(),  null);
        mAuth.signInWithCredential (credential)
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user=mAuth.getCurrentUser();
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                // get email and uid from auth
                                String email=user.getEmail();
                                String uid =user.getUid();

                                //using hashmap
                                HashMap<Object,String> hashMap =new HashMap<>();

                                // thong tin hashmap
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name","");
                                hashMap.put("phone","");
                                hashMap.put("image","");
                                hashMap.put("cover","");
                                hashMap.put("TypingTo","noOne");

                                //firebase database isntance

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(uid).setValue(hashMap);
                            }


                            Toast.makeText(LoginMainActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginMainActivity.this,RegisterMainActivity.class));
                            finish();

                        } else {


                            Toast.makeText(LoginMainActivity.this, "Failed....", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginMainActivity.this, "+getMessage()", Toast.LENGTH_SHORT).show();

                    }
                });

    }
}