package com.itesm.devxican_mobile.ui.branch;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;

import java.util.ArrayList;

public class BranchFragment extends Fragment {

    RecyclerView rv_branches;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    CollectionReference branches_ref;
    ArrayList<Branch> branches;
    ArrayList<DocumentReference> branches_doc_ref;

    public BranchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        branches_ref = db.collection("branches");
        user = mAuth.getCurrentUser();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_branches, container, false);
        rv_branches = v.findViewById(R.id.rv_branches);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.branches = new ArrayList<>();
        this.branches_doc_ref = new ArrayList<>();

        branches_ref.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                branches.add(documentSnapshot.toObject(Branch.class));
                                branches_doc_ref.add(documentSnapshot.getReference());
                            }
                            constructRecycler(); // build recycler after reading all info
                        }
                    }
                });
    }

    private void constructRecycler() {


        rv_branches.setLayoutManager(new LinearLayoutManager(this.getContext()));
        BranchAdapter adapter = new BranchAdapter(this.getContext(), branches);
        rv_branches.setAdapter(adapter);

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intBranch = new Intent(getContext(), BranchActivity.class);
                intBranch.putExtra("branch_id", branches_doc_ref.get(rv_branches.getChildAdapterPosition(view)).getId());
                Toast.makeText(getContext(), intBranch.getStringExtra("branch_id"), Toast.LENGTH_SHORT).show();
                startActivity(intBranch);
            }
        });
    }

}

