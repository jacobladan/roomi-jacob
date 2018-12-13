package com.example.roomi.roomi;


import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RoomSettings extends AppCompatActivity {

    private TextView nameView;
    private EditText temperatureInput;
    private EditText brightnessInput;
    private EditText nameInput;
    private Button submitButton;
    private Button cancelButton;

    private String key;
    private String nameVal;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_settings);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle extras;

        extras = getIntent().getExtras();
        nameVal = extras.getString("name");
        key = extras.getString("key");

        setTitle("Home: " + nameVal);
        findViews();
        getDatabase();

        nameView.setText(getString(R.string.update_room_settings));
        nameInput.setHint("Current: " + nameVal);
        temperatureInput.setHint("Current: " + extras.getInt("temperature") + getString(R.string.degrees_cel));
        brightnessInput.setHint("Current: " + extras.getInt("brightness") + getString(R.string.percent));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_delete);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    String name = nameInput.getText().toString();
                    int temperature = Integer.parseInt(temperatureInput.getText().toString());
                    int brightness = Integer.parseInt(brightnessInput.getText().toString());
                    dbRef.child(key).setValue(new HomeRoomDataStructure(name, temperature, brightness));
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
        dbRef = database.getReference("users/" + fbUser.getUid() + "/rooms/home");
    }

    private void findViews() {
        nameInput = findViewById(R.id.update_name_input);
        temperatureInput = findViewById(R.id.update_temperature_input);
        brightnessInput = findViewById(R.id.update_brightness_input);
        nameView = findViewById(R.id.room_name);
        submitButton = findViewById(R.id.update_room_button);
        cancelButton = findViewById(R.id.cancel_update_room_button);
    }

    private boolean validateData() {
        int temperature = 0;
        int brightness = -1;
        int nameLen = nameInput.getText().toString().length();

        if (!temperatureInput.getText().toString().equals("")) {
            try {
                temperature = Integer.parseInt(temperatureInput.getText().toString());
            } catch (Exception e) {
                Log.d("IntParse1", e.toString());
            }
        }

        if (!brightnessInput.getText().toString().equals("")) {
            try {
                brightness = Integer.parseInt(brightnessInput.getText().toString());
            } catch (Exception e) {
                Log.d("IntParse2", e.toString());
            }
        }

        if (nameLen <= 0 || nameLen > 25) {
            nameInput.setError("Please enter a name between 1 and 25 characters");
            return false;
        }
        if (temperature < 15 || temperature > 25) {
            temperatureInput.setError("Please enter a valid temperature");
            return false;
        }
        if (brightness < 0 || brightness > 100) {
            brightnessInput.setError("Please enter a valid brightness");
            return false;
        }
        return true;
    }
}