package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class ChatListFragment extends Fragment {
    FirebaseAuth firebaseAuth;


    public ChatListFragment() {

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth =FirebaseAuth.getInstance();
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat_list, container, false);
        return view;
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