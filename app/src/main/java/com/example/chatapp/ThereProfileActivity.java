package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.adpters.AdapterPosts;
import com.example.chatapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postsRecyclerView;
    FirebaseAuth firebaseAuth;
    ImageView avatarIv,coverIv;
    TextView nameTv,emailTv,phoneTv;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //get uid





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        ActionBar actionBar =getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        postsRecyclerView =findViewById(R.id.recyclerview_posts);
        firebaseAuth =FirebaseAuth.getInstance();

        coverIv =findViewById(R.id.coverIv);
        avatarIv =findViewById(R.id.avatarIv);
        nameTv =findViewById(R.id.nameTv);
        emailTv =findViewById(R.id.emailTv);
        phoneTv =findViewById(R.id.phoneTv);




        Query query =FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
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
        postList = new ArrayList<>();

        Intent intent =getIntent();
        uid = intent.getStringExtra("uid");
        loadHisPosts();
        checkUserStatus();

    }

    private void loadHisPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

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

                    adapterPosts =new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter((adapterPosts));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseerror.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void  searchHisPosts (final String searchQuery){
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);

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

                    adapterPosts =new AdapterPosts(ThereProfileActivity.this,postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter((adapterPosts));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseerror) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseerror.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void checkUserStatus () {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if( user != null) {

//            mProfileTv.setText(user.getEmail());


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

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // được gọi khi người dùng nhấn nút tìm kiếm
                if(!TextUtils.isEmpty(s)){
                    searchHisPosts(s);
                }else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //được gọi khi người dùng gõ bất kỳ ký tự nào
                if(!TextUtils.isEmpty(s)){
                    searchHisPosts(s);
                }else {
                    loadHisPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if(id== R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();

        }

        return super.onOptionsItemSelected(item);
    }
}