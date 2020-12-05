package com.itesm.devxican_mobile.ui.branch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
