package com.example.sptm_systerm;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private ProgressBar progressBar;
    private  String countrycode = "86";
    private static
    boolean Logged = false;
    private String email, password;
    private static final String TAG1 = "FirestoreTest";
    private FirebaseFirestore db;
    private int progress = 0;
    private LoginStorage loginStorage;
    private FirebaseAuth mAuth;
    private FirebaseService firebaseService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        EditText login = findViewById(R.id.userName);
        EditText passwordd = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.Login);
        Button signup = findViewById(R.id.SignUp);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseService = new FirebaseService();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && ThingHomeSdk.getUserInstance().getUser() != null) {
            firebaseService.getUserRole(
                    role -> {
                        // Role successfully retrieved; you can use the role if needed
                        redirectToHome();
                    },
                    e -> {
                        Toast.makeText(this, "Failed to retrieve user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            );
        }


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = 0;
                progressBar.setProgress(progress);
                progressBar.setVisibility(View.VISIBLE);
                // Retrieve user inputs
                email = login.getText().toString();
                password = passwordd.getText().toString();
                signIn(email,password);
                // Validate inputs
                if (!Logged) {
                    //signIn(email,password);

                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterUser.class));
            }
        });


    }

    private void redirectToHome() {
        Intent intent = new Intent(LoginActivity.this, Home.class);
        startActivity(intent);
        finish(); // Prevents user from returning to LoginActivity
    }


    public void smartHomeLogin(Context context, String email, String password) {
        progress = 75;
        progressBar.setProgress(progress);
        ThingHomeSdk.getUserInstance().loginWithEmail(countrycode, email, password, new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                progress = 100;
                progressBar.setProgress(progress);
                Toast.makeText(LoginActivity.this, "Success:", Toast.LENGTH_SHORT).show();
                Intent mainScreen = new Intent(LoginActivity.this, Home.class);
                startActivity(mainScreen);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String code, String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Not Success", Toast.LENGTH_SHORT).show();
            }
        });

    }



    private void signIn(String email, String password) {
        progress = 5;
        progressBar.setProgress(progress);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            progress = 25;
                            progressBar.setProgress(progress);
                            smartHomeLogin(LoginActivity.this, email, password);
                            firebaseService.getUserRole(
                                    role -> {
                                        // Successfully retrieved role
                                        GlobalVariable.userRole = role;
                                        Log.d(TAG, "User Role: " + role);
                                    },
                                    e -> {
                                        // Failed to retrieve role
                                        Log.e(TAG, "Error getting user role", e);
                                        Toast.makeText(LoginActivity.this, "Failed to get user role", Toast.LENGTH_SHORT).show();
                                    }
                            );
                        } else {
                            progressBar.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

}



