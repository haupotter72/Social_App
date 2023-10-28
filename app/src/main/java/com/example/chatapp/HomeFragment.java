package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapp.adpters.AdapterPosts;
import com.example.chatapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;





    public HomeFragment() {

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);


        recyclerView =view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager((getActivity()));
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        //set layout to recylerview
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        postList = new ArrayList<>();
        loadPosts();
        //show new post

        return  view;
    }

    private void loadPosts() {
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelPost modelPost =ds.getValue(ModelPost.class);

                    postList.add(modelPost);
                    //adapter
                    adapterPosts =new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // truong hop bi loi
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void searchPost(String searchQuery){
        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                postList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelPost modelPost =ds.getValue(ModelPost.class);

                    if(modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){


                        postList.add(modelPost);
                    }



                    //adapter
                    adapterPosts =new AdapterPosts(getActivity(),postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // truong hop bi loi
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void checkUserStatus () {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if( user != null) {

//            mProfileTv.setText(user.getEmail());

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
        MenuItem  item=menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // khi nhấn nút tìm kiếm
                if(!TextUtils.isEmpty(s)){
                    searchPost(s);
                }else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // khi  tìm kiếm bất kì chữ cái
                if(!TextUtils.isEmpty(s)){
                    searchPost(s);
                }else {
                    loadPosts();
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