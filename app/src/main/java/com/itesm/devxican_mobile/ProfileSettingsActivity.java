package com.itesm.devxican_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ProfileSettingsActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 71;

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
    private Uri filePath;
    private Boolean photoChange = false;


    FirebaseStorage storage;
    StorageReference storageReference;




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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();


        User.LOAD_USER(uid);



        //User.LOAD_USER(mAuth.getCurrentUser().getUid());



        User.getAuxUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {

                auxUser = user;
                updateUI(auxUser);
            }
        });


        //ActionBar toolbar = getActionBar();
        //toolbar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //this.finish();
        return true;
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
                                Toast.makeText(getApplicationContext(), "Password updated", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

        if (photoChange) {
            uploadImage();
        }

    }

    private void pushChangestoDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.set(auxUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("HERE", "User password updated.");
                Toast.makeText(getApplicationContext(), "User Updated!", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();

            Picasso.get()
                    .load(filePath)
                    .fit()
                    .centerInside()
                    .into(profilePicture);

            photoChange = true;

        }
    }


    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("users/"+ uid);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();


                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for 'users/me/profile.png'

                                    auxUser.photoURL = uri.toString();
                                    pushChangestoDB();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors

                                    Toast.makeText(getApplicationContext(), "An error ocurred, try again later", Toast.LENGTH_LONG).show();
                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}