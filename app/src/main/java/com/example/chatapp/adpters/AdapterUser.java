package com.example.chatapp.adpters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.ThereProfileActivity;
import com.example.chatapp.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser  extends  RecyclerView.Adapter<AdapterUser.MyHolder>{
    Context context;
    List<ModelUser> userList;

    public AdapterUser(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_user,viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myholder, int i) {
        //get data
        String hisUID= userList.get(i).getUid();
        String userImage =userList.get(i).getImage();
        String userName =userList.get(i).getName();
        String userEmail =userList.get(i).getEmail();

        // set data

        myholder.mNameTv.setText(userName);
        myholder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(myholder.mAvatarIv);
        }catch (Exception e){

        }
        //handle item click
        myholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which ==0){
                            //profile click

                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);

                        }if(which ==1){
                            //chat click

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid",hisUID);
                            context.startActivity(intent);

                        }

                    }
                });
                builder.create().show();
            }

        });



    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView mAvatarIv;
        TextView mNameTv,mEmailTv;
         public  MyHolder(@NonNull View itemView){
             super(itemView);

             //init views;
             mAvatarIv =itemView.findViewById(R.id.avatarIv);
             mNameTv = itemView.findViewById(R.id.nameTv);
             mEmailTv =itemView.findViewById(R.id.emailTv);
         }

    }
}
