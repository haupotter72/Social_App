package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.adpters.AdapterChat;
import com.example.chatapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatActivity extends AppCompatActivity {

    //view from xlm
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;


    String hisUid;
    String myUid;
    String hisImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);

        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);


        //layput recyclerview
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);;


        Intent intent =getIntent();
        hisUid = intent.getStringExtra("hisUid");



        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase =FirebaseDatabase.getInstance();
        usersDbRef =firebaseDatabase.getReference("Users");

         Query userQuery =usersDbRef.orderByChild("uid").equalTo(hisUid);

         // getuser  picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for(DataSnapshot ds: dataSnapshot.getChildren()){
                  //get data
                  String name =""+ds.child("name").getValue();
                   hisImage =""+ds.child("image").getValue();




                  //set data
                  nameTv.setText(name);
                  try {
                     // hình ảnh trên toolbar
                      Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img_white).into(profileIv);

                  }catch (Exception e){
                      Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv);

                  }
              }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click button send mess
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get text from edit text
                String message =messageEt.getText().toString().trim();
                
                //check if text is empty or not
                if(TextUtils.isEmpty(message)){
                    //tin nhắn trống
                    Toast.makeText(ChatActivity.this, "VUI LÒNG SOẠN TIN NHẮN", Toast.LENGTH_SHORT).show();

                   
                }else {
                    //  tin nhắn không trống
                    sendMessage(message);
                    
                }
            }

        });


        readMessages();
        seenMessage();



    }
    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen();
    }
    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        seen=isSeen();
    }

    private void seenMessage() {
        userRefForSeen =FirebaseDatabase.getInstance().getReference("Chats");
        seenListener =userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat =ds.getValue(ModelChat.class);

                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenMap = new HashMap<>();
                        hasSeenMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList =new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                chatList.clear();
                for(DataSnapshot ds:datasnapshot.getChildren()){
                    ModelChat chat= ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                    chat.getReceiver().equals(hisImage) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    // adapter
                    adapterChat=new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();

        String timestamp =String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);



        databaseReference.child("Chats").push().setValue(hashMap);

        //reset edittext adter sending message
        messageEt.setText("");





    }

    // check user

    private void checkUserStatus() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            myUid =user.getUid();// lay tai khoan dang nhap hien tai

//            mProfileTv.setText(user.getEmail());

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }


    }



    @Override
    protected void onStart() {
        checkUserStatus();

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();




        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        // ẩn tìm kiếm
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
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
}