package com.itesm.devxican_mobile;

import android.app.Activity;
import android.content.Context;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
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
import com.itesm.devxican_mobile.ui.login.LoginActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {

    public static final String USER_TAG = "LoggedInUser";
    public static final String ERROR = "ERROR";
    private AppBarConfiguration mAppBarConfiguration;
    FirebaseUser user;
    FirebaseAuth mAuth;
    DocumentReference user_ref;
    FirebaseFirestore db;
    CollectionReference users_ref;
    User userdata;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        users_ref = db.collection("users");

        users_ref.document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().equals(null)){
                        user_ref = task.getResult().getReference();
                        userdata = task.getResult().toObject(User.class);
                    } else {
                        Log.w(ERROR, "No user with such uid.");
                    }
                } else {
                    Log.w(ERROR, "Error getting documents.", task.getException());
                }
            }
        });

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
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_branches, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            //Log Out button
            case R.id.log_out:
                logOut();
                return true;
            case R.id.action_settings:
                startSettingsActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void logOut() {
        Intent auxIntent = new Intent(this, LoginActivity.class);
        startActivity(auxIntent);
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void startSettingsActivity() {
        Intent auxIntent = new Intent(this, ProfileSettingsActivity.class);
        startActivity(auxIntent);
        setResult(Activity.RESULT_OK);
        finish();
    }






}