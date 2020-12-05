package com.itesm.devxican_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

public class ProfileSettingsActivity extends AppCompatActivity {

    private TextView email;
    private TextView username;
    private TextView password;
    private TextView noPosts,
                     noComments,
                     noFollows,
                     noBranches;

    private ImageView profilePicture;

    private String uid;
    private User auxUser;

    private Button saveChangesButton;
    private ProgressBar loadingProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        loadingProgressBar = findViewById(R.id.progressBar);
        loadingProgressBar.setVisibility(View.VISIBLE);

        noPosts = findViewById(R.id.postNo);
        noComments = findViewById(R.id.noComments);
        noFollows = findViewById(R.id.noFollows);
        noBranches = findViewById(R.id.noBranches);

        email = findViewById(R.id.emailSettingsTextView);
        username = findViewById(R.id.usernameSettingsTextView);
        password = findViewById(R.id.passwordSettingsTextView);
        profilePicture = findViewById(R.id.profileImage);
        saveChangesButton = findViewById(R.id.saveSettings);

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });


        uid = "q6egcEP11JbUeCKfb7Nz";

        FirebaseAuth mAuth = FirebaseAuth.getInstance();



        User.LOAD_USER(uid);



        //User.LOAD_USER(mAuth.getCurrentUser().getUid());



        User.getAuxUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {

                auxUser = user;
                updateUI(auxUser);
            }
        });




    }


    private void updateUI(User user) {

        email.setText(user.email);
        username.setText(user.name);
        password.setText("********");

        noPosts.setText(user.posts.size() + "");
        noComments.setText(user.comments.size()  + "");
        noFollows.setText(user.follows.size()  + "");
        noBranches.setText(user.branches.size()  + "");


        Picasso.get()
                .load(user.photoURL)
                .fit()
                .centerInside()
                .into(profilePicture);


        loadingProgressBar.setVisibility(View.GONE);

    }


    private void saveChanges() {

        auxUser.name = username.getText().toString();
        String auxPassword = password.getText().toString();

        if (password.getText().toString() !=  "********") {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.updatePassword(auxPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("HERE", "User password updated.");
                            }
                        }
                    });
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.set(auxUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("HERE", "User password updated.");
                Toast.makeText(getApplicationContext(), "User Updated", Toast.LENGTH_LONG).show();
            }
        });
    }
}