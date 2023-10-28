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

import com.example.chatapp.adpters.AdapterUser;
import com.example.chatapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<ModelUser> userList;

    FirebaseAuth firebaseAuth;




    public UsersFragment() {

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_users, container, false);

        // init recycleView

        recyclerView =view.findViewById(R.id.users_recyclerView);

        //init firebase
        firebaseAuth =FirebaseAuth.getInstance();

        //set it is properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        //init user list
        userList =new ArrayList<>();
        
        //get all users
        getAllUser();
        


        return  view;
    }

    private void getAllUser() {

        // get curent user
        FirebaseUser fuser= FirebaseAuth.getInstance().getCurrentUser();

        // get path of database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        // get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                userList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelUser modelUser =ds.getValue(ModelUser.class);


                   // nhận tất cả người dùng ngoại trừ người dùng hiện đang đăng nhập

                    if(!modelUser.getUid().equals(fuser.getUid())){
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUser =new AdapterUser(getActivity(),userList);

                    //set adapter to recycler view

                    recyclerView.setAdapter(adapterUser);



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void searchUser(String query) {


        // get curent user
        FirebaseUser fuser= FirebaseAuth.getInstance().getCurrentUser();

        // get path of database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        // get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                userList.clear();
                for(DataSnapshot ds: datasnapshot.getChildren()){
                    ModelUser modelUser =ds.getValue(ModelUser.class);


                    // tim kiem  tất cả người dùng ngoại trừ người dùng hiện đang đăng nhập

                    if(!modelUser.getUid().equals(fuser.getUid())){
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);

                        }
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUser =new AdapterUser(getActivity(),userList);

                    //refresh adapter
                    adapterUser.notifyDataSetChanged();

                    //set adapter to recycler view

                    recyclerView.setAdapter(adapterUser);



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
     // hide add post
        menu.findItem(R.id.action_add_post).setVisible(false);


     //Search View
         MenuItem item =menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // neu tim kiem khong trong

                if(!TextUtils.isEmpty(s.trim())){
                   // tim kiem user
                    searchUser(s);
                }else {
                    // neu tim kiem trong thi lay tat ca user
                    getAllUser();

                }
                return false;
            }

          
            @Override
            public boolean onQueryTextChange(String s) {

                // neu tim kiem khong trong

                if(!TextUtils.isEmpty(s.trim())){
                    // tim kiem user
                    searchUser(s);
                }else {
                    // neu tim kiem trong thi lay tat ca user
                    getAllUser();

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

        return  super.onOptionsItemSelected(item);
    }

}


