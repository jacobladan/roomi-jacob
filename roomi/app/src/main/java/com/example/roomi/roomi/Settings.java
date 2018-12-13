package com.example.roomi.roomi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class Settings extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser fbUser;
    private DatabaseReference dbRef;
    private FirebaseDatabase database;
    private FirebaseAuth.AuthStateListener authListener;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private View headerView;
    private User user;
    private TextView fullName, fullNameMenu, emailMenu;
    private Button emailChange, passwordChange, deleteAccount, save;
    private RadioButton english, french;
    private String languageCode;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        mAuth = FirebaseAuth.getInstance();
        fbUser = mAuth.getCurrentUser();

        getFromSharedPreference();

        setContentView(R.layout.activity_settings);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);
        setTitle(R.string.settings);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);



        // No user logged in
        if (fbUser == null) {
            startActivity(new Intent(Settings.this, MainActivity.class));
            finish();
        }

        findViews();
        getDatabase();
        logoutListener();
        retrieveData();

        if(languageCode.equalsIgnoreCase("en")) {
            english.setChecked(true);
        } else if (languageCode.equalsIgnoreCase("fr")) {
            french.setChecked(true);
        }

        emailChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent email = new Intent(Settings.this, ChangeEmail.class);
                startActivity(email);
            }
        });

        passwordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent password = new Intent(Settings.this, ChangePassword.class);
                startActivity(password);
            }
        });

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageCode = "en";
            }
        });

        french.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageCode = "fr";
            }
        });

        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Locale locale = new Locale(languageCode);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
                saveToSharedPreference();

                Intent intent = new Intent(getApplicationContext(), Settings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deleteAccount = new Intent(Settings.this, DeleteAccount.class);
                startActivity(deleteAccount);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();

                        if (id == R.id.nav_home) {
                            Intent myIntent = new Intent(getApplicationContext(), RoomSelector.class);
                            startActivity(myIntent);
                        } else if (id == R.id.nav_security) {
                            // Goes to Security Activity
                            Intent security = new Intent(getApplicationContext(), SecuritySelector.class);
                            startActivity(security);
                        } else if (id == R.id.nav_settings) {
                            // Already at settings do nothing
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

    public void saveToSharedPreference() {
        SharedPreferences.Editor editor = getSharedPreferences("language", MODE_PRIVATE).edit();
        editor.putString(fbUser.getEmail(), languageCode);
        editor.apply();
    }

    public void getFromSharedPreference() {
        SharedPreferences prefs = getSharedPreferences("language", MODE_PRIVATE);
        languageCode = prefs.getString(fbUser.getEmail(), "en");
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

    public void findViews() {
        emailChange = findViewById(R.id.emailChange);
        passwordChange = findViewById(R.id.passwordChange);
        deleteAccount = findViewById(R.id.deleteAccount);
        fullName = findViewById(R.id.fullName);
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        fullNameMenu = headerView.findViewById(R.id.fullNameUser);
        emailMenu = headerView.findViewById(R.id.emailUser);
        save = findViewById(R.id.save);
        english = findViewById(R.id.english);
        french = findViewById(R.id.french);
    }

    private void getDatabase() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/"+ fbUser.getUid());
    }

    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    fullName.setText(user.getFirstName() + " " + user.getLastName());
                    fullNameMenu.setText(user.getFirstName() + " " + user.getLastName());
                    emailMenu.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Database access error!", Toast.LENGTH_SHORT).show();
            }
        });
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
