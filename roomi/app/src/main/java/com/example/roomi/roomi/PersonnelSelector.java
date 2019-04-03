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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private int i;
    private String[] keyList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private ProgressBar progressBar;
    private TextView header;


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_selector);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        setTitle(R.string.personnel);
        header = findViewById(R.id.personnel_selector_header);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            getDatabase();
            retrieveData();
            logoutListener();
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
        dbRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/personnel");
    }

    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fetchPersonnel(dataSnapshot);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("roomi", "Data retrieval error...", databaseError.toException());
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
        if (personnelList.isEmpty()) {
            header.setText("No personnel have been created.");
        }

        i = 0;
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(900, 300);
        buttonParams.setMargins(0, 0, 0, 50);

        for (PersonnelDatastructure personnel: personnelList) {
            final String key = keyList[i];
            final String name = personnel.getName();
            final int accessLevel = personnel.getaccessLevel();
            final String accessInfo = "Access Level: " + accessLevel;

            LinearLayout personnelContainer = new LinearLayout(this);
            personnelContainer.setBackgroundResource(R.drawable.element_background_dark);
            personnelContainer.setLayoutParams(buttonParams);
            personnelContainer.setOrientation(LinearLayout.VERTICAL);

            personnelContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PersonnelSelector.this, PersonnelSettings.class);
                    intent.putExtra("name", name);
                    intent.putExtra("accessLevel", accessLevel);
                    intent.putExtra("key", key);
                    startActivity(intent);
                }
            });

            Button button = new Button(this);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setText(name);
            button.setTextSize(20);
            button.setTextColor(Color.WHITE);
            button.setClickable(false);
            button.setTransformationMethod(null);


            LinearLayout infoContainer = new LinearLayout(this);
            infoContainer.setHorizontalGravity(Gravity.CENTER);
            infoContainer.setVerticalGravity(Gravity.CENTER);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(900, 100);
            layoutParams.setMargins(0, 0, 0, 50);

            infoContainer.setLayoutParams(layoutParams);

            TextView accessInfoView = new TextView(this);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, 0, 25, 0);

            accessInfoView.setLayoutParams(textParams);
            accessInfoView.setText(accessInfo);
            accessInfoView.setTextColor(Color.WHITE);
            accessInfoView.setTextSize(16);

            infoContainer.addView(accessInfoView);

            personnelContainer.addView(button);
            personnelContainer.addView(infoContainer);

            buttonContainer.addView(personnelContainer);
            i++;
        }



//        Button addPersonnelButton = new Button(this);
//        addPersonnelButton.setTag("add_personnel");
//        addPersonnelButton.setBackgroundResource(R.drawable.element_background_dark);
//        addPersonnelButton.setLayoutParams(buttonParams);
//        addPersonnelButton.setText("+");
//        addPersonnelButton.setTextSize(40);
//        addPersonnelButton.setTextColor(Color.WHITE);
//        addPersonnelButton.setTransformationMethod(null);
//
//        addPersonnelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(PersonnelSelector.this, PersonnelAdd.class);
//                startActivity(intent);
//            }
//        });
//
//        buttonContainer.addView(addPersonnelButton);
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
