package com.example.roomi.roomi;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeEmail extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, newEmailEditText, reEnterEmailEditText;
    private Button submitButton;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_change_email);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setTitle("Change Email");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        findViews();
        user = FirebaseAuth.getInstance().getCurrentUser();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.hideKeyboard(ChangeEmail.this);
                String oldEmail = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                final String newEmail = newEmailEditText.getText().toString().trim();
                String confirmEmail = reEnterEmailEditText.getText().toString().trim();

                if (oldEmail.isEmpty()) {
                    emailEditText.setError("Enter your email");
                    return;
                }

                if (password.isEmpty()) {
                    passwordEditText.setError("Enter your password");
                    return;
                }

                if (newEmail.isEmpty()) {
                    newEmailEditText.setError("Enter a new email");
                    return;
                }

                if (confirmEmail.isEmpty()) {
                    reEnterEmailEditText.setError("Please confirm your email");
                    return;
                }

                if (oldEmail.equals(newEmail)) {
                    Toast.makeText(getApplicationContext(), "Old email and new email must not match!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!newEmail.equals(confirmEmail)) {
                    Toast.makeText(getApplicationContext(), "Emails must match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                credential = EmailAuthProvider.getCredential(oldEmail, password);

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()) {
                            user.updateEmail(newEmail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                updateEmail(newEmail);
                                                Toast.makeText(getApplicationContext(), "Email address was updated.", Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong!\nCheck your credentials.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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

    private void updateEmail(String email) {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        String userId = user.getUid();
        mReference.child("users").child(userId).child("email").setValue(email);

    }


    private void findViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        newEmailEditText = findViewById(R.id.newEmailEditText);
        reEnterEmailEditText = findViewById(R.id.reEnterEmailEditText);
        submitButton = findViewById(R.id.submit);
        progressBar = findViewById(R.id.progressBar);
    }
}
