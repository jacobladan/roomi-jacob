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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RoomSelector extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference dbRef, dbUserRef;
    private int i;
    private String[] keyList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser fbUser;
    private ProgressBar progressBar;

    private User user;
    private NavigationView navigationView;
    private View headerView;
    private TextView fullNameMenu, emailMenu;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selector);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        setTitle(R.string.home);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            fbUser = mAuth.getCurrentUser();
            findViews();
            getDatabase();
            logoutListener();
            retrieveData();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();

                        if (id == R.id.nav_security) {
                            // Goes to Security Activity
                            Intent security = new Intent(getApplicationContext(), SecuritySelector.class);
                            startActivity(security);
                        } else if (id == R.id.nav_settings) {
                            // Goes to Settings Page
                            Intent settings = new Intent(getApplicationContext(), Settings.class);
                            startActivity(settings);
                        } else if (id == R.id.nav_aboutus) {
                            Intent mAboutUs = new Intent(getApplicationContext(), AboutUs.class);
                            startActivity(mAboutUs);
                        } else if (id == R.id.nav_logout) {
                            // Logs out and displays the Log In Screen

                            mAuth.signOut();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else if (id == R.id.nav_exit) {
                            finishAffinity();
                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        fullNameMenu = headerView.findViewById(R.id.fullNameUser);
        emailMenu = headerView.findViewById(R.id.emailUser);
    }

    private void getDatabase() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/" + fbUser.getUid() + "/rooms/home");
        dbUserRef = database.getReference("users/" + fbUser.getUid());
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

    private void fetchRooms(DataSnapshot dataSnapshot) {
        List<HomeRoomDataStructure> roomList = new ArrayList<>();
        keyList = new String[(int) dataSnapshot.getChildrenCount()];
        i = 0;
        roomList.clear();
        for (DataSnapshot roomSnapShot: dataSnapshot.getChildren()) {
            HomeRoomDataStructure room = roomSnapShot.getValue(HomeRoomDataStructure.class);
            keyList[i] = roomSnapShot.getKey();
            roomList.add(room);
            i++;
        }
        generateRoomButtons(roomList, keyList);
    }

    private void generateRoomButtons(List<HomeRoomDataStructure> roomList, final String[] keyList) {
        i = 0;
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(900, 300);
        buttonParams.setMargins(0, 0, 0, 50);

        for (HomeRoomDataStructure room: roomList) {
            final String key = keyList[i];
            final String name = room.getName();
            final int temperature = room.getTemperature();
            final int brightness = room.getBrightness();
            final String brightnessInfo = "Brightness: " + brightness + "%";
            final String temperatureInfo = "Temp: " + temperature + "°C";

            LinearLayout roomContainer = new LinearLayout(this);
            roomContainer.setBackgroundResource(R.drawable.element_background_dark);
            roomContainer.setLayoutParams(buttonParams);
            roomContainer.setOrientation(LinearLayout.VERTICAL);

            roomContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RoomSelector.this, RoomSettings.class);
                    intent.putExtra("key", key);
                    intent.putExtra("name", name);
                    intent.putExtra("temperature", temperature);
                    intent.putExtra("brightness", brightness);
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

            TextView brightnessView = new TextView(this);
            TextView temperatureView = new TextView(this);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, 0, 25, 0);

            brightnessView.setLayoutParams(textParams);
            brightnessView.setText(brightnessInfo);
            brightnessView.setTextColor(Color.WHITE);
            brightnessView.setTextSize(16);

            temperatureView.setText(temperatureInfo);
            temperatureView.setTextColor(Color.WHITE);
            temperatureView.setTextSize(16);

            infoContainer.addView(brightnessView);
            infoContainer.addView(temperatureView);

            roomContainer.addView(button);
            roomContainer.addView(infoContainer);

            buttonContainer.addView(roomContainer);

            i++;
        }

        Button addRoomButton = new Button(this);
        addRoomButton.setTag("add_room");
        addRoomButton.setBackgroundResource(R.drawable.element_background_dark);
        addRoomButton.setLayoutParams(buttonParams);
        addRoomButton.setText("+");
        addRoomButton.setTextSize(40);
        addRoomButton.setTextColor(Color.WHITE);
        addRoomButton.setTransformationMethod(null);

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddRoom.class);
                startActivity(intent);
            }
        });

        buttonContainer.addView(addRoomButton);
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
