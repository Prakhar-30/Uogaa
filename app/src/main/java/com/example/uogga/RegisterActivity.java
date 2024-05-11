package com.example.uogga;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText editTextUsername;
    private EditText editTextRegisterEmail;
    private EditText editTextAge;
    private EditText editTextRegisterMobile;
    private EditText editTextRegisterPwd;
    private EditText editTextRegisterConfirmPwd;

    private static final String TAG = "RegisterActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        progressBar = findViewById(R.id.progressBar);
        editTextUsername = findViewById(R.id.etName);
        editTextRegisterEmail = findViewById(R.id.etEmail);
        editTextAge = findViewById(R.id.etAge);
        editTextRegisterMobile = findViewById(R.id.etMobile);
        editTextRegisterPwd = findViewById(R.id.etPassword);
        editTextRegisterConfirmPwd = findViewById(R.id.etConfirmPassword);

        findViewById(R.id.btnSignUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textUsername = editTextUsername.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textAge = editTextAge.getText().toString();
                String textMobile = editTextRegisterMobile.getText().toString();
                String textPwd = editTextRegisterPwd.getText().toString();
                String textConfirmPwd = editTextRegisterConfirmPwd.getText().toString();

                if (TextUtils.isEmpty(textUsername)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your username", Toast.LENGTH_LONG).show();
                    editTextUsername.setError("Username is required");
                    editTextUsername.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your valid email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textAge)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your age", Toast.LENGTH_LONG).show();
                    editTextAge.setError("Age is required");
                    editTextAge.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your mobile number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile number is required");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(RegisterActivity.this, "Please re-enter your valid mobile number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile number should be 10 digits");
                    editTextRegisterMobile.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password is required");
                    editTextRegisterPwd.requestFocus();
                } else if (textPwd.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 6 digits", Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password too weak");
                    editTextRegisterPwd.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Please confirm your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPwd.setError("Confirm Password is required");
                    editTextRegisterConfirmPwd.requestFocus();
                } else if (!textPwd.equals(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "Confirm Password does not match the Password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPwd.setError("Confirmation is required");
                    editTextRegisterConfirmPwd.requestFocus();
                    editTextRegisterPwd.clearComposingText();
                    editTextRegisterConfirmPwd.clearComposingText();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textUsername, textEmail, textAge, textPwd, textMobile);

                }
            }
        });
    }

    private void registerUser(String textUsername, String textEmail, String textAge, String textPwd, String textMobile) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;

                    // Here you can use textMobile
                    ReadWriteDetails writeUserDetails = new ReadWriteDetails(textMobile, textUsername, textAge);

                    // Get reference to the "Registered Users" node in the Firebase database
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                    // Set value in the database using setValue() method
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(RegisterActivity.this, "User registered successfully.Please verify your email", Toast.LENGTH_LONG).show();

                              /*    Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                  startActivity(intent);
                                   finish(); */
                            } else {
                                Toast.makeText(RegisterActivity.this, "User registration Failed.Please try Again", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.GONE);

                        }
                    });


                } else {
                    // Handle registration failure
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthWeakPasswordException e) {
                        editTextRegisterPwd.setError("Your password is too weak. Kindly use a mix of alphabets, numbers, and special characters");
                        editTextRegisterPwd.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        editTextRegisterEmail.setError("Your email is invalid or already in use");
                        editTextRegisterEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        editTextRegisterEmail.setError("Your email is already registered. Use another email");
                        editTextRegisterEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage(), e);
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}