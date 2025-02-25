package com.example.sptm_systerm;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerUserRole;
    private String selectedRole = "User";

    private  String emails,passwords,firstname,lastnames,addresses,nics,mobiles ;
    private String regNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_user);
        EditText Name = findViewById(R.id.Name);
        EditText lastname = findViewById(R.id.lastName);
        EditText address = findViewById(R.id.Address);
        EditText email = findViewById(R.id.Email);
        EditText password = findViewById(R.id.password);
        EditText nic= findViewById(R.id.NIC);
        EditText reg= findViewById(R.id.regNo);
        EditText mobile= findViewById(R.id.MobileNumber);
        Button  register = findViewById(R.id.Register);
        spinnerUserRole = findViewById(R.id.spinnerUserRole);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        List<String> roles = Arrays.asList("Admin", "User", "Manager");

        // Populate Spinner with user roles
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerUserRole.setAdapter(adapter);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emails = email.getText().toString();
                passwords = password.getText().toString();
                firstname = Name.getText().toString();
                lastnames = lastname.getText().toString();
                addresses = address.getText().toString();
                nics =nic.getText().toString();
                mobiles = mobile.getText().toString();
                regNo = reg.getText().toString();
                selectedRole = spinnerUserRole.getSelectedItem().toString();
                registerUser();
            }
        });

    }


    public void registerUser(){
        mAuth.createUserWithEmailAndPassword(emails, passwords)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            addUserToFirestore(user.getUid(), firstname,lastnames,addresses,nics,emails,mobiles,regNo, selectedRole);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUser.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });


    }

    private void addUserToFirestore(String uid, String firstname,String lastnames,String addresses, String nics,String emails,String mobiles,String regNo, String selectedRole) {


        // Create a user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstname", firstname);
        userData.put("lastname",lastnames);
        userData.put("email", emails);
        userData.put("addresses", addresses);
        userData.put("nic", nics);
        userData.put("mobile", mobiles);
        userData.put("role", selectedRole);
        userData.put("subscriptionNo", regNo);


        // Add data to Firestore
        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("RegisterActivity", "User added to Firestore.");
                        Intent emailPass = new Intent(RegisterUser.this,SignUp.class);
                        emailPass.putExtra("emailAddress",emails);
                        emailPass.putExtra("password",passwords);
                        startActivity(emailPass);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("RegisterActivity", "Error adding user to Firestore: ", e);
                    }
                });
    }
}