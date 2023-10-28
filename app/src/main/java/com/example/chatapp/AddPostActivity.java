package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;
    EditText titleEt,descriptionEt;
    ImageView imageIv;
    Button uploadBtn;
    // user info
    String name,email,uid,dp;

    ProgressDialog pd;

    Uri image_rui=null ;
    private static  final int CAMERA_REQUEST_CODE =100;
    private static  final int STORAGE_REQUEST_CODE =200;

    private static  final int IMAGE_PICK_GALLERY_CODE =400;
    private static  final int IMAGE_PICK_CAMERA_CODE =300;


    //Pmermissions array
    String[] cameraPermissions;
    String[] storagePermissions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar =getSupportActionBar();
        actionBar.setTitle("Add New Post");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth =FirebaseAuth.getInstance();
        actionBar.setSubtitle(email);

        checkUserStatus();

        // get some info
        userDbRef =FirebaseDatabase.getInstance().getReference("Users");
        Query query =userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // init view
        titleEt =findViewById(R.id.pTitleEt);
        descriptionEt =findViewById(R.id.pDescriptionEt);
        imageIv =findViewById(R.id.pImageIv);

        //init permissions arrays
        cameraPermissions =new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions =new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        pd= new ProgressDialog(AddPostActivity.this);
        //upload button click listener
        uploadBtn =findViewById(R.id.pUploadBt);
        //get image from gallery or camera
        //get image from gallery or camera


        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();

            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getdata from edit text
                String title =titleEt.getText().toString().trim();
                String description =descriptionEt.getText().toString().trim();

                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();

                return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
                    return;

                }
                if(image_rui ==null){
                    uploadData(title,description,"no Image");
                }else {
                    //post with image
                    uploadData(title,description,String.valueOf(image_rui));
                }
            }
        });
}

    private void uploadData(String title, String description, String uri) {
        pd.setMessage("Publish Post...");
        pd.show();

        String timeStamp =String.valueOf(System.currentTimeMillis());
        String filePathAndName= "Posts/" +"post_" + timeStamp;

        if(!uri.equals("noImage")){
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is uploaded to firebase;
                            Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri =uriTask.getResult().toString();

                            if(uriTask.isSuccessful()){
                                HashMap <Object,String> hashMap =new HashMap<>();

                                //put post info
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTime",timeStamp);

                                //path to store post data
                                DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Posts");
                                // put data in ref
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "PostPublished", Toast.LENGTH_SHORT).show();
                                        //reset views
                                        titleEt.setText("");
                                        descriptionEt.setText("");
                                        imageIv.setImageURI(null);
                                        image_rui=null;

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                            }


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }else {
            // pots nhung ko co hinh anh
            HashMap <Object,String> hashMap =new HashMap<>();

            //put post info
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);

            //path to store post data
            DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Posts");
            // put data in ref
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                    //reset views
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_rui=null;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });


        }


    }

    private void showImagePickDialog() {
        String[] options ={"Camera","Gallery"};

        //dialog
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");

        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                if(which ==0){
                    //camera
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }

                }else if(which ==1){
                    //gallery
                    if(!checkStoragepermission()){
                        requestStoragePermission();

                    }else{
                        pickFromGallery();

                    }

                }

            }
        });

        builder.create().show();

    }

    private void pickFromGallery() {

        //intent to pick image from gallery

        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        //intent to pick image
        ContentValues cv =new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp dESCR");

        image_rui =getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);




    }


    private boolean checkStoragepermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }

    private void requestStoragePermission(){
        //request run time storage permission
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }

    private void requestCameraPermission(){
        //request run time storage permission
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }





    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus () {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if( user != null) {

//            mProfileTv.setText(user.getEmail());
            email = user.getEmail();
            uid= user.getUid();

        }else{
            startActivity(new Intent(this,MainActivity.class));
     finish();
        }


    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();
        if(id== R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();

        }

        return super.onOptionsItemSelected(item);
    }
    //handle permission result


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted =grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted =grantResults[1] ==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();

                    }else{
                        Toast.makeText(this, "Cần Quyền Máy ảnh và Lưu trữ ...", Toast.LENGTH_SHORT).show();

                    }
                }else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccept =grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccept){
                        pickFromGallery();
                    }else {
                        Toast.makeText(this, "Cần Quyền Lưu trữ ...", Toast.LENGTH_SHORT).show();
                    }

                }else {

                }

            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         if(requestCode ==RESULT_OK){
             if(requestCode == IMAGE_PICK_GALLERY_CODE){
                 image_rui =data.getData();
                 //set to image
                 imageIv.setImageURI(image_rui);

             }else if(requestCode ==IMAGE_PICK_CAMERA_CODE){
                 imageIv.setImageURI(image_rui);


             }
         }


        super.onActivityResult(requestCode, resultCode, data);
    }
}