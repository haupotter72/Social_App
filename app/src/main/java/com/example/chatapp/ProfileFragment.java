package com.example.chatapp;

import static android.app.Activity.RESULT_OK;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;


import com.example.chatapp.adpters.AdapterPosts;
import com.example.chatapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth ;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    String storagepath ="Users_Profile_Cover_Imgs/";

    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv;
    FloatingActionButton fab;

    RecyclerView postsRecyclerView;

    // progress dialog

    ProgressDialog pd;

    //uri of picked image
    Uri image_uri;

    String profileOrCoverPhoto;


    //permissions dialog



    private static  final int CAMERA_REQUEST_CODE =100;
    private static  final int STORAGE_REQUEST_CODE =200;
    private static  final int IMAGE_PICK_GALLERY_CODE =300;
    private static  final int IMAGE_PICK_CAMERA_CODE =400;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;


    /// array of permissions to be requested

    String cameraPermissions[];
    String storagePermissions[];





    public ProfileFragment() {



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_profile,container,false);

        //init firebase
        firebaseAuth =FirebaseAuth.getInstance();
        user =firebaseAuth.getCurrentUser();
        firebaseDatabase =FirebaseDatabase.getInstance();
        databaseReference =firebaseDatabase.getReference("Users");
        storageReference =getInstance().getReference();

        //init views
        coverIv =view.findViewById(R.id.coverIv);
        fab =view.findViewById(R.id.fab);
        avatarIv =view.findViewById(R.id.avatarIv);
        nameTv =view.findViewById(R.id.nameTv);
        emailTv =view.findViewById(R.id.emailTv);
        phoneTv =view.findViewById(R.id.phoneTv);

        pd=new ProgressDialog(getActivity());

        postsRecyclerView =view.findViewById(R.id.recyclerview_posts);


        //init array of permission


        cameraPermissions =new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions =new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Query query =databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until dataget

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone= "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();


                    //set data

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try {
                        Picasso.get().load(image).into(avatarIv);

                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default).into(avatarIv);

                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);

                    }
                    catch (Exception e){
//                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //fab button click

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();

            }
        });
        postList = new ArrayList<>();
        checkUserStatus();
        loadMyPosts();

        return view;


    }

    private void loadMyPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show npost moi nhat
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //querry to load post
        Query query = ref.orderByChild("uid").equalTo(uid);


        //get  all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for(DataSnapshot ds :datasnapshot.getChildren()){
                    ModelPost myPosts =ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);
                    //adpter

                    adapterPosts =new AdapterPosts(getActivity(),postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter((adapterPosts));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {
                Toast.makeText(getActivity(), ""+databaseerror.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void searchMyPosts(String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //show npost moi nhat
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //querry to load post
        Query query = ref.orderByChild("uid").equalTo(uid);


        //get  all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for(DataSnapshot ds :datasnapshot.getChildren()){
                    ModelPost myPosts =ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){


                        //add to list
                        postList.add(myPosts);
                    }

                    //adpter

                    adapterPosts =new AdapterPosts(getActivity(),postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter((adapterPosts));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {
                Toast.makeText(getActivity(), ""+databaseerror.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private boolean checkStoragePermission (){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }

    private void requestStoragePermission(){
      requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission (){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }

    private void requestCameraPermission(){
      requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }




    private void showEditProfileDialog () {

        String options[] ={"Edit profile Picture","Edit Cover Photo",
        "Edit Name","Edit Phone"};

        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");

        // set items to dialog

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                //handle dialog item click

                if(which ==0){
                    //Edit profile clicked

                    pd.setMessage("upload profile Picture");
                    profileOrCoverPhoto ="image";
                    showImagePicDialog();

                }else if(which == 1){
                    //Edit Cover clicked
                    pd.setMessage("upload profile Photo");

                    profileOrCoverPhoto ="Updating Cover Photo";
                    showImagePicDialog();



                }else if(which==2){
                    //Edit Name clicked

                    pd.setMessage("upload profile Name");
                    showNamePhoneUpdateDialog("name");

                }else if(which==3){
                    //Edit Phone clicked

                    pd.setMessage("upload profile Phone");
                    showNamePhoneUpdateDialog("phone");

                }

            }
        });

        //create and show dialog
        builder.create().show();




    }

    private void showNamePhoneUpdateDialog(String key) {
        //custon dialog
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " +key);

        //set Layout of dialog
        LinearLayout  linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation((LinearLayout.VERTICAL));
        linearLayout.setPadding(10,10,10,10);

        //add edit text

        EditText editText =new EditText(getActivity());
        editText.setHint("Enter " +key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        // add button in dialog

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                //input text from edit text
                String value= editText.getText().toString().trim();


                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result =new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void avoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Update...", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                    // nếu người dùng chỉnh sửa tên của mình, cũng thay đổi nó từ bài viết
                    if(key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query =ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                for(DataSnapshot ds:datasnapshot.getChildren()){
                                    String child = ds.getKey();
                                    datasnapshot.getRef().child(child).child("uName").setValue(value);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }


                }else{
                    Toast.makeText(getActivity(), "Pleasr Enter", Toast.LENGTH_SHORT).show();
                }


            }
        });

        //add button in dialof to cancle
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        //create and show dialog
        builder.create().show();




    }

    private void showImagePicDialog(){
        String options[] ={"Camera","Gallery"};

        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");

        // set items to dialog


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item click

                if(which ==0){
                    //camera click
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }

                }else if(which == 1){
                    // gallery clicked

                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }

                }

            }
        });





        //create and show dialog
        builder.create().show();




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length >0){
                    boolean cameraAccepted =grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted =grantResults[1] ==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "VUI LÒNG BẬT QUYỀN LƯU TRỮ MÁY ẢNH  ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                if(grantResults.length >0){

                    boolean writeStorageAccepted =grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted) {
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(), "VUI LÒNG BẬT QUYỀN LƯU TRỮ  ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){

                //image is picked from gallery,get uri of image

                image_uri =data.getData();

                uploadProfileCoverPhoto(image_uri);

            }if (requestCode == IMAGE_PICK_CAMERA_CODE){

                //  image is picked from camera,get uri of image
                uploadProfileCoverPhoto(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {

        pd.show();
        String filePathAndName =storagepath + ""+profileOrCoverPhoto+"_"+user.getUid();

        StorageReference storageReference2nd= storageReference.child(filePathAndName);

        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnSuccessListener(new OnSuccessListener<Uri>(){
                    @Override
                    public void onSuccess(Uri uri) {
                        // Tác vụ tải xuống hoàn thành thành công
                        Uri downladUri = uri;
                        // Thực hiện các hoạt động tiếp theo với downloadUri ở đây
                        HashMap<String, Object> results = new HashMap<>();
                        results.put(profileOrCoverPhoto, downladUri.toString());

                        databaseReference.child(user.getUid()).updateChildren(results)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void avoid) {

                                        //dissmiss progress bar
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Image update...", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Erro Updating Image...", Toast.LENGTH_SHORT).show();

                                    }
                                });
                        // nếu người dùng chỉnh sửa tên của mình, cũng thay đổi nó từ bài viết


                        if(profileOrCoverPhoto.equals("image")){
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            Query query =ref.orderByChild("uid").equalTo(uid);
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                    for(DataSnapshot ds:datasnapshot.getChildren()){
                                        String child = ds.getKey();
                                        datasnapshot.getRef().child(child).child("uDp").setValue(downladUri.toString());

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }



                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Xử lý lỗi xảy ra khi tải xuống thất bại
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Some Error action", Toast.LENGTH_SHORT).show();
                    }
                });
                }








        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void pickFromGallery() {

        // picking image from device camera

        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Tem Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Ten Description");

        // put image uri

        image_uri =getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera

        Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);



    }

    private void pickFromCamera(){

        // pick from gallery

        Intent galleryIntent =new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);


    }




    private void checkUserStatus () {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if( user != null) {

//            mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        }else{
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu
        super.onCreate(savedInstanceState);

    }
    //inflate optio s menu


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // được gọi khi người dùng nhấn nút tìm kiếm
                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //được gọi khi người dùng gõ bất kỳ ký tự nào
                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else {
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    public boolean onOptionsItemSelected (MenuItem item) {

        int id=item.getItemId();
        if(id== R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();

        }
        if(id== R.id.action_add_post){

            startActivity(new Intent(getActivity(),AddPostActivity.class));

        }

        return  super.onOptionsItemSelected(item);
    }



}