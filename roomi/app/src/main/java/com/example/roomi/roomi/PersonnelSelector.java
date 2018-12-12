package com.example.roomi.roomi;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PersonnelSelector extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private int i;
    private String[] keyList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private TextView fullNameMenu, emailMenu;
    private NavigationView navigationView;
    private View headerView;

    private DatabaseReference dbUserRef;
    private User user;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_selector);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mAuth = FirebaseAuth.getInstance();

        setTitle(R.string.personnel);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            getDatabase();
            retrieveData();
            logoutListener();
            findViews();
        }


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDatabase() {
        database = FirebaseDatabase.getInstance();
        dbUserRef = database.getReference("users/" + mAuth.getUid());
        dbRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/personnel");
    }

    private void findViews() {
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        fullNameMenu = headerView.findViewById(R.id.fullNameUser);
        emailMenu = headerView.findViewById(R.id.emailUser);
    }

    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fetchPersonnel(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("roomi", "Data retrieval error...", databaseError.toException());
            }
        });

        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    fullNameMenu.setText(user.getFirstName() + " " + user.getLastName());
                    emailMenu.setText(user.getEmail());
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Database access error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPersonnel(DataSnapshot dataSnapshot) {
        List<PersonnelDatastructure> personnelList = new ArrayList<>();
        personnelList.clear();
        keyList = new String[(int) dataSnapshot.getChildrenCount()];
        i = 0;
        for (DataSnapshot personnelSnapShot: dataSnapshot.getChildren()) {
            PersonnelDatastructure personnel = personnelSnapShot.getValue(PersonnelDatastructure.class);
            personnelList.add(personnel);
            keyList[i] = personnelSnapShot.getKey();
            i++;
        }
        generatePersonnelButtons(personnelList, keyList);
    }

    private void generatePersonnelButtons(List<PersonnelDatastructure> personnelList, final String[] keyList) {
        i = 0;
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(900, 200);
        params.setMargins(0, 0, 0, 65);

        for (PersonnelDatastructure personnel: personnelList) {
            final String key = keyList[i];
            final String name = personnel.getName();
            final String avatarColour = personnel.getAvatarColour();
            final int accessLevel = personnel.getaccessLevel();
            Button button = new Button(this);
            button.setTag(personnel.getName());
            button.setBackgroundResource(R.drawable.element_background_dark);
            button.setLayoutParams(params);
            button.setText(personnel.getName());
            button.setTextSize(20);
            button.setTextColor(Color.WHITE);
            button.setTransformationMethod(null);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonnelSelector.this, PersonnelSettings.class);
                    intent.putExtra("name", name);
                    intent.putExtra("avatarColour", avatarColour);
                    intent.putExtra("accessLevel", accessLevel);
                    intent.putExtra("key", key);
                    startActivity(intent);
                }
            });
            i++;
            buttonContainer.addView(button);
        }

        Button addPersonnelButton = new Button(this);
        addPersonnelButton.setTag("add_personnel");
        addPersonnelButton.setBackgroundResource(R.drawable.element_background_dark);
        addPersonnelButton.setLayoutParams(params);
        addPersonnelButton.setText("+");
        addPersonnelButton.setTextSize(20);
        addPersonnelButton.setTextColor(Color.WHITE);
        addPersonnelButton.setTransformationMethod(null);

        addPersonnelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonnelSelector.this, PersonnelAdd.class);
                startActivity(intent);
            }
        });

        buttonContainer.addView(addPersonnelButton);
    }

    private void logoutListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        };
    }
}
