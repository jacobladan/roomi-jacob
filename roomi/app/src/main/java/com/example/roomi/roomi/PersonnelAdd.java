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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonnelAdd extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    private EditText nameInput;
    private EditText accessLevelInput;
    private Button submitButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_add);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
                    int accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
                    DatabaseReference newPersonnel = dbRef.push();
                    newPersonnel.setValue(new PersonnelDatastructure(name, accessLevel));
                    Toast toast = Toast.makeText(getApplicationContext(), name + " with access level " + accessLevel +  " created!", Toast.LENGTH_LONG);
                    toast.show();
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
        int nameLen = nameInput.getText().toString().length();
        Log.d("nameInput" , nameInput.getText().toString());
        int accessLevel = -1;

        if (!accessLevelInput.getText().toString().equals("")) {
            try {
                accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
            } catch (Exception e) {
                Log.d("IntParse1", e.toString());
            }
        }

        if (nameLen <= 0 || nameLen > 25) {
            nameInput.setError("Please enter a name between 1 and 25 characters");
            return false;
        }
        if (accessLevel < 0 || accessLevel > 5) {
            accessLevelInput.setError("Please enter a valid access level");
            return false;
        }
        return true;
    }
}
