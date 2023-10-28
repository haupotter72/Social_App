package com.example.chatapp.adpters;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.AddPostActivity;
import com.example.chatapp.R;
import com.example.chatapp.ThereProfileActivity;
import com.example.chatapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    //view hodler class
    Context context;
    List<ModelPost> postList;
    String myUid;


    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myholder, int i) {


        //get data
        String uid = postList.get(i).getUid();
        String uEmail = postList.get(i).getUEmail();
        String uName = postList.get(i).getuName();
        String uDp = postList.get(i).getuDp();
        final  String pId = postList.get(i).getpId();
        String pTitle = postList.get(i).getpTitle();
        String pDescription = postList.get(i).getpDescr();
        String pImage = postList.get(i).getpImage();
        String pTimestamp = postList.get(i).getpTime();
        //conver time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimestamp));
        String pTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        //set data
        myholder.uNameTv.setText(uName);
        myholder.pTimeTv.setText(pTime);
        myholder.pTitleTv.setText(pTitle);
        myholder.pDescriptionTv.setText(pDescription);

        //setup dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myholder.uPictureIv);


        } catch (Exception e) {

        }

        //set posts image

        try {
            Picasso.get().load(pImage).into(myholder.pImageTv);
        } catch (Exception e) {

        }

        if (pImage.equals("noImage")) {
            //an imaveview
            myholder.pImageTv.setVisibility(View.GONE);

        } else {
          //show image view
            myholder.pImageTv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(myholder.pImageTv);
            } catch (Exception e) {

            }
        }

        //handle button click
        myholder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            showMoreOptions(myholder.moreBtn,uid,myUid,pId,pImage);

            }
        });
        myholder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Like...", Toast.LENGTH_SHORT).show();

            }
        });
        myholder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Comment...", Toast.LENGTH_SHORT).show();

            }
        });
        myholder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Share...", Toast.LENGTH_SHORT).show();

            }
        });
        myholder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);

            }
        });


    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {

        //create ... menu co nut delete
        PopupMenu popupMenu =new PopupMenu(context,moreBtn, Gravity.END);

        //item click listener
        if(uid.equals(myUid)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,0,0,"Edit Posts");

        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int id= menuItem.getItemId();
                if(id==0){
                    //delete clicked
                    beginDelete(pId,pImage);
                } else if(id==1){
                 // edit posts clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        
        // show menu
        popupMenu.show();


    }

    private void beginDelete(String pId, String pImage) {
        // posts ko co image
        if(pImage.equals("noImage")){
            deleteWithoutImage(pId);

        }else {

           deleteWithImage(pId,pImage);

        }
    }

   private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd = new ProgressDialog(context);
      pd.setMessage("Delete...");


       StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
       picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
           public void onSuccess(Void avoid) {
            Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
             fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                      for(DataSnapshot ds:datasnapshot.getChildren()){
                          ds.getRef().removeValue();
                     }
                      Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();

                  }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {
                 }
            });

}
      }).addOnFailureListener(new OnFailureListener() {
          @Override
         public void onFailure(@NonNull Exception e) {
               // loi
               pd.dismiss();


           }
     });

    }


    private void deleteWithoutImage(String pId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Delete...");


        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for(DataSnapshot ds:datasnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Delete Successfully", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
       return  postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //view from row_posts.xml

        ImageView uPictureIv,pImageTv;
        TextView uNameTv, pTimeTv,pDescriptionTv,pLikesTv,pTitleTv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            uPictureIv =itemView.findViewById(R.id.uPictureIv);
            pImageTv =itemView.findViewById(R.id.pImageIv);
            uNameTv =itemView.findViewById(R.id.uNameTv);
            pTimeTv =itemView.findViewById(R.id.pTimeTv);
            pTitleTv =itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv =itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv =itemView.findViewById(R.id.pLikeTv);
            moreBtn =itemView.findViewById(R.id.moreBtn);
            likeBtn =itemView.findViewById(R.id.likeBtn);
            commentBtn =itemView.findViewById(R.id.commentBtn);
            shareBtn =itemView.findViewById(R.id.shareBtn);
            profileLayout =itemView.findViewById(R.id.profileLayout);

        }
    }
}
