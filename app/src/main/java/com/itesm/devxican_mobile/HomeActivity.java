package com.itesm.devxican_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itesm.devxican_mobile.data.model.LoggedInUser;
import com.itesm.devxican_mobile.data.model.User;
import com.itesm.devxican_mobile.data.tools.DataGenerator;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static final String USER_TAG = "LoggedInUser";
    public static final String TAG = "HomeActivity";
    public static final String ERROR = "ERROR";
    private AppBarConfiguration mAppBarConfiguration;
    FirebaseUser user;
    FirebaseAuth mAuth;
    DocumentReference user_ref;
    FirebaseFirestore db;
    CollectionReference users_ref;
    User userdata;

    NavigationView navigationView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        users_ref = db.collection("users");
        userdata = new User();


        //Toast.makeText(getApplicationContext(), user.getEmail(), Toast.LENGTH_LONG).show();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_branches, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        loadUser();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void loadUser() {
        users_ref.document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.wtf(TAG, "DocumentSnapshot data: " + document.getData());

                        userdata.name = document.getData().get("name").toString();
                        userdata.photoURL = document.getData().get("photoURL").toString();
                        userdata.phone = document.getData().get("phone").toString();
                        userdata.email = document.getData().get("email").toString();

                        userdata.branches = (List<DocumentReference>) document.getData().get("branches");
                        userdata.comments = (List<DocumentReference>) document.getData().get("comments");
                        userdata.follows = (List<DocumentReference>) document.getData().get("follows");
                        userdata.posts = (List<DocumentReference>) document.getData().get("posts");
                        populateNavHeader(navigationView.getHeaderView(0));
                    } else {
                        Log.wtf(TAG, "No such document");
                    }
                } else {
                    Log.wtf(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void populateNavHeader(View v) {
        TextView email =  (TextView) v.findViewById(R.id.emailNavHeader);
        TextView username = (TextView) v.findViewById(R.id.usernameNavHeader);
        email.setText(userdata.email);
        username.setText(userdata.name);

        // populate photo
        ImageView image = (ImageView) v.findViewById(R.id.profileImageView);


        Picasso.get()
                .load(userdata.photoURL)
                .fit()
                .centerInside()
                .into(image);




    }



}