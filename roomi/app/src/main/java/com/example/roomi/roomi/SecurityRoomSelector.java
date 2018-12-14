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

public class SecurityRoomSelector extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private int i;
    private String[] keyList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_room_selector);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        setTitle(R.string.security_rooms);

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
        dbRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/rooms/security");
    }


    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fetchRooms(dataSnapshot);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("roomi", "Data retrieval error...", databaseError.toException());
            }
        });
    }

    private void fetchRooms(DataSnapshot dataSnapshot) {
        List<SecurityRoomDataStructure> roomList = new ArrayList<>();
        keyList = new String[(int) dataSnapshot.getChildrenCount()];
        i = 0;
        roomList.clear();
        for (DataSnapshot roomSnapShot: dataSnapshot.getChildren()) {
            SecurityRoomDataStructure room = roomSnapShot.getValue(SecurityRoomDataStructure.class);
            keyList[i] = roomSnapShot.getKey();
            roomList.add(room);
            i++;
        }
        generateRoomButtons(roomList, keyList);
    }

    private void generateRoomButtons(List<SecurityRoomDataStructure> roomList, final String[] keyList) {
        i = 0;
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(900, 300);
        buttonParams.setMargins(0, 0, 0, 50);

        for (SecurityRoomDataStructure room: roomList) {
            final String key = keyList[i];
            final String name = room.getName();
            final int accessLevel = room.getAccessLevel();
            final String accessInfo = "Access Level: " + accessLevel;

            LinearLayout roomContainer = new LinearLayout(this);
            roomContainer.setBackgroundResource(R.drawable.element_background_dark);
            roomContainer.setLayoutParams(buttonParams);
            roomContainer.setOrientation(LinearLayout.VERTICAL);

            roomContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SecurityRoomSelector.this, SecurityRoomSettings.class);
                    intent.putExtra("key", key);
                    intent.putExtra("name", name);
                    intent.putExtra("accessLevel", accessLevel);
                    startActivity(intent);
                }
            });

            Button button = new Button(this);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setText(room.getName());
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

            TextView accessView = new TextView(this);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, 0, 25, 0);

            accessView.setLayoutParams(textParams);
            accessView.setText(accessInfo);
            accessView.setTextColor(Color.WHITE);
            accessView.setTextSize(16);


            infoContainer.addView(accessView);

            roomContainer.addView(button);
            roomContainer.addView(infoContainer);

            buttonContainer.addView(roomContainer);

            i++;
        }

        Button addSecurityRoomButton = new Button(this);
        addSecurityRoomButton.setTag("add_room");
        addSecurityRoomButton.setBackgroundResource(R.drawable.element_background_dark);
        addSecurityRoomButton.setLayoutParams(buttonParams);
        addSecurityRoomButton.setText("+");
        addSecurityRoomButton.setTextSize(40);
        addSecurityRoomButton.setTextColor(Color.WHITE);
        addSecurityRoomButton.setTransformationMethod(null);

        addSecurityRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityRoomSelector.this, SecurityAddRoom.class);
                startActivity(intent);
            }
        });

        buttonContainer.addView(addSecurityRoomButton);
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
