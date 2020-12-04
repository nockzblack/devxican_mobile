package com.itesm.devxican_mobile.ui.branch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<com.itesm.devxican_mobile.ui.branch.BranchAdapter.BranchViewHolder> implements View.OnClickListener {


    public StorageReference mStorageRef;
    public FirebaseFirestore db;
    public FirebaseAuth mAuth;
    public CollectionReference branches_ref, users_ref;
    public FirebaseUser curr_user;
    public DocumentReference user;
    public User userdata;

    public static final String ERROR = "ERROR";
    public static final String INFO = "INFO";

    DocumentReference branch_ref; // will be constantly changing
    Branch branchdata; // also constantly changing

    public View.OnClickListener listener;
    public Context context;
    public ArrayList<Branch> branches;

    public BranchAdapter(Context context, ArrayList<Branch> branches) {

        this.context = context;
        this.branches = branches;

        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        branches_ref = db.collection("branches");
        users_ref = db.collection("users");
        mStorageRef = FirebaseStorage.getInstance().getReference("branches");

        user = users_ref.document(curr_user.getUid());
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    userdata = task.getResult().toObject(User.class);
                    //Log.i(INFO, );
                } else {
                    Log.w(ERROR, "Error getting user document.", task.getException());
                }
            }
        });

    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.branch_row, parent, false);

        view.setOnClickListener(this);

        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {

        // downloading image from firebase storage
        String url = branches.get(position).backURL;
        Picasso.get()
                .load(url)
                .fit()
                .centerInside()
                .into(holder.branch_img);

        branches_ref.whereEqualTo("name", branches.get(position).name) // obtaining current branch_ref
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            branch_ref = task.getResult().getDocuments().get(0).getReference();
                            branchdata = task.getResult().getDocuments().get(0).toObject(Branch.class);
                        }
                    }
                });

        holder.post_b.setText(String.valueOf(branches.get(position).posts.size()));
        holder.foll_b.setText(String.valueOf(branches.get(position).followers.size()));
        holder.name_b.setText("r/".concat(String.valueOf(branches.get(position).name)));
        holder.bt_follow.setVisibility(View.INVISIBLE);
       /*holder.bt_follow.setOnClickListener(new View.OnClickListener() { // follow / unfollow behaviour
            @Override
            public void onClick(View view) {
                user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { // update userdata info
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            userdata = doc.toObject(User.class);
                            Log.i("INFO", doc.toString());

                            String totoast = "Succsessfully ";

                            if (userdata.branches.size() > 0) {
                                if (userdata.branches.contains(branch_ref)) {
                                    holder.bt_follow.setText("unfollow");
                                }
                            }
                            if (holder.bt_follow.getText().toString().equals("unfollow")) {
                                // unfollow
                                userdata.branches.remove(branch_ref);
                                branchdata.followers.remove(user);
                                totoast.concat("unfollowed! :$");
                                holder.bt_follow.setText("follow");
                            } else if (holder.bt_follow.getText().toString().equals("unfollow")){
                                // follow
                                userdata.branches.add(branch_ref);
                                branchdata.followers.add(user);
                                totoast.concat("followed! :D");
                                holder.bt_follow.setText("unfollow");
                            }

                            user.update("branches", userdata.branches).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        branch_ref.update("followers", branchdata.followers).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, totoast, Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Log.w(ERROR, "Error updating branch document.", task.getException());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.w(ERROR, "Error updating user document.", task.getException());
                                    }
                                }
                            });

                        }
                    }
                });
            }
        });*/
    }

    @Override
    public int getItemCount() { return this.branches.size(); }

    public void setOnClickListener(View.OnClickListener listener) { this.listener = listener; }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    public class BranchViewHolder extends RecyclerView.ViewHolder {

        ImageView branch_img;
        TextView name_b, foll_b, post_b;
        Button bt_follow;
        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);

            branch_img = itemView.findViewById(R.id.branch_img);
            name_b = itemView.findViewById(R.id.branch_name);
            foll_b = itemView.findViewById(R.id.branch_followers);
            post_b = itemView.findViewById(R.id.branch_posts);
            bt_follow = itemView.findViewById(R.id.follow);
        }


    }

}
