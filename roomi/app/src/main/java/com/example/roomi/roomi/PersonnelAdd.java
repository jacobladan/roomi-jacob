package com.example.roomi.roomi;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.HomeSp;
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
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonnelAdd extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    private EditText nameInput;
    private EditText accessLevelInput;
    private EditText avatarColourInput;
    private Button submitButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_add);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);

        setTitle(R.string.add_a_person);
        getDatabase();
        getElements();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    String name = nameInput.getText().toString();
                    String avatarColour = avatarColourInput.getText().toString();
                    int accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
                    DatabaseReference newPersonnel = dbRef.push();
                    newPersonnel.setValue(new PersonnelDatastructure(name, avatarColour, accessLevel));
                    finish();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

    private void getElements() {
        nameInput = findViewById(R.id.add_room_name_input);
        accessLevelInput = findViewById(R.id.add_access_level_input);
        avatarColourInput = findViewById(R.id.add_avatar_colour_input);
        submitButton = findViewById(R.id.add_personnel_button);
        cancelButton = findViewById(R.id.cancel_add_personnel_button);
    }

    private void getDatabase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/" + fbUser.getUid() + "/personnel");
    }

    private boolean validateData() {
        String name = nameInput.getText().toString();

        String avatarColour = nameInput.getText().toString();
        int accessLevel = 0;

        try {
            accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
        } catch (Exception e) {
            Log.d("IntParse", e.toString());
        }

        if (name.length() < 3 || name.length() > 25) return false;

        if (accessLevel < 0 || accessLevel > 5) return false;
//        TODO: Add validity for avatar colour
        return true;
    }
}
