package com.example.roomi.roomi;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SecurityRoomSettings extends AppCompatActivity {

    private EditText nameInput;
    private EditText accessLevelInput;
    private Bundle extras;
    private String nameVal;
    private Button submitButton;
    private String key;
    private Button cancelButton;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    RoomDatastructure data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_room_settings);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);

        extras = getIntent().getExtras();
        nameVal = extras.getString("name");

        nameVal = extras.getString("name");
        key = extras.getString("key");
        Log.d("KeyTest", key);

        setTitle(nameVal);
        findViews();
        getDatabase();

        nameInput.setHint("Current: " + nameVal);
        accessLevelInput.setHint("Current: " + extras.getInt("accessLevel"));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_delete);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    String name = nameInput.getText().toString();
                    int accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
                    dbRef.child(key).setValue(new RoomDatastructure(name, 0, 0, true, false, accessLevel));
                    Toast toast = Toast.makeText(getApplicationContext(), "Updated " + name, Toast.LENGTH_LONG);
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
                dbRef.child(key).removeValue();
                Toast toast = Toast.makeText(getApplicationContext(), "Deleted " + nameVal, Toast.LENGTH_LONG);
                toast.show();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDatabase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/" + fbUser.getUid() + "/rooms");
    }

    private void findViews() {
        accessLevelInput = findViewById(R.id.update_access_level_input);
        nameInput = findViewById(R.id.update_security_name_input);
        submitButton = findViewById(R.id.update_security_room_button);
        cancelButton = findViewById(R.id.cancel_update_security_room_button);
    }

    private boolean validateData() {
        int accessLevel = -1;
        int nameLen = nameInput.getText().toString().length();

        if (!accessLevelInput.getText().toString().equals("")) {
            try {
                accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
            } catch (Exception e) {
                Log.d("IntParse", e.toString());
            }
        }

        if (nameLen < 0 || nameLen > 25) {
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