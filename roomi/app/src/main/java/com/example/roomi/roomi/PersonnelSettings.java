package com.example.roomi.roomi;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

public class PersonnelSettings extends AppCompatActivity {

    private EditText nameInput;
    private EditText accessLevelInput;

    private Bundle extras;
    private String nameVal;
    private Button submitButton;
    private String key;
    private Button cancelButton;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_settings);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);

        extras = getIntent().getExtras();
        nameVal = extras.getString("name");
        key = extras.getString("key");
        setTitle(extras.getString("name"));
        findViews();
        getDatabase();

        nameInput.setHint("Current: "  + nameVal);
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
                    dbRef.child(key).setValue(new PersonnelDatastructure(name, accessLevel));
                    Toast toast = Toast.makeText(getApplicationContext(), "Updated " + nameVal, Toast.LENGTH_LONG);
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
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                dbRef.child(key).removeValue();
                                Toast toast = Toast.makeText(getApplicationContext(), "Deleted " + nameVal, Toast.LENGTH_LONG);
                                toast.show();
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure? This cannot be undone").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDatabase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/" + fbUser.getUid() + "/personnel");
    }

    private void findViews() {
        accessLevelInput = findViewById(R.id.update_personnel_access_level_input);
        nameInput = findViewById(R.id.update_personnel_name_input);
        submitButton = findViewById(R.id.update_personnel_button);
        cancelButton = findViewById(R.id.cancel_update_personnel_button);
    }

    private boolean validateData() {
        int accessLevel = 0;
        String name = nameInput.getText().toString();


        try {
            accessLevel = Integer.parseInt(accessLevelInput.getText().toString());
        } catch (Exception e) {
            Log.d("IntParse", e.toString());
        }

        if (name.length() < 0 || name.length() > 25) return false;

        if (accessLevel < 0 || accessLevel > 5) return false;
        return true;
    }
}