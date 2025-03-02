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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterUser extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerUserRole;
    private String selectedRole = "User";

    private String emails, passwords, firstname, lastnames, addresses, nics, mobiles;
    private String regNo;
    private CustomProgressDialog progressDialog;
    private Button registerBtn;
    private EditText emailField, passwordField, verificationCodeField;
    private Button sendVerificationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_user);

        EditText Name = findViewById(R.id.Name);
        EditText lastname = findViewById(R.id.lastName);
        EditText address = findViewById(R.id.Address);
        emailField = findViewById(R.id.Email);
        passwordField = findViewById(R.id.password);
        EditText nic = findViewById(R.id.NIC);
        EditText reg = findViewById(R.id.regNo);
        EditText mobile = findViewById(R.id.MobileNumber);
        registerBtn = findViewById(R.id.verify);
        spinnerUserRole = findViewById(R.id.spinnerUserRole);
        sendVerificationBtn = findViewById(R.id.Submit);
        verificationCodeField = findViewById(R.id.myVerification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new CustomProgressDialog(this);

        List<String> roles = Arrays.asList("Admin", "User", "Technician", "Reader");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerUserRole.setAdapter(adapter);

        sendVerificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTuyaVerificationCode(emailField.getText().toString());
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                // Retrieve and Trim User Inputs
                emails = emailField.getText().toString().trim();
                passwords = passwordField.getText().toString().trim();
                firstname = Name.getText().toString().trim();
                lastnames = lastname.getText().toString().trim();
                addresses = address.getText().toString().trim();
                nics = nic.getText().toString().trim();
                mobiles = mobile.getText().toString().trim();
                regNo = reg.getText().toString().trim();
                selectedRole = spinnerUserRole.getSelectedItem().toString();

                // Validate Input
                if (!isValidInput(emails, passwords, firstname, lastnames, addresses, nics, mobiles, regNo)) {
                    progressDialog.dismiss();
                    return; // Stop execution if input is invalid
                }

                registerBtn.setEnabled(false); // Prevent multiple clicks
                registerUser();
            }
        });

    }

    private void registerUser() {
        mAuth.createUserWithEmailAndPassword(emails, passwords)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        registerBtn.setEnabled(true); // Re-enable button after attempt

                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            registerWithTuya(emails, passwords);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private boolean isValidInput(String email, String password, String firstname, String lastname,
                                 String address, String nic, String mobile, String regNo) {

        if (firstname.isEmpty()) {
            Toast.makeText(RegisterUser.this, "First name cannot be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lastname.isEmpty()) {
            Toast.makeText(RegisterUser.this, "Last name cannot be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (address.isEmpty()) {
            Toast.makeText(RegisterUser.this, "Address cannot be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(RegisterUser.this, "Email cannot be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterUser.this, "Enter a valid email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(RegisterUser.this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(RegisterUser.this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (nic.isEmpty() || !nic.matches("^[0-9]{9}[VvXx]$|^[0-9]{12}$")) {
            // Example: 9-digit NIC + "V" (old format) OR 12-digit new NIC
            Toast.makeText(RegisterUser.this, "Enter a valid NIC number!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mobile.isEmpty() || !mobile.matches("^[0-9]{10}$")) {
            Toast.makeText(RegisterUser.this, "Enter a valid 10-digit mobile number!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (regNo.isEmpty()) {
            Toast.makeText(RegisterUser.this, "Registration number cannot be empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void registerWithTuya(String email, String password) {
        String verificationCode = verificationCodeField.getText().toString();

        ThingHomeSdk.getUserInstance().registerAccountWithEmail("86", email, password, verificationCode, new IRegisterCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d("TuyaRegistration", "User registered successfully with Tuya.");
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    addUserToFirestore(firebaseUser.getUid(), firstname, lastnames, addresses, nics, emails, mobiles, regNo, selectedRole);
                }
            }

            @Override
            public void onError(@Nullable String errorCode, @Nullable String errorMessage) {
                progressDialog.dismiss();
                Log.e("TuyaRegistrationError", "Error code: " + errorCode + ", message: " + errorMessage);
                Toast.makeText(RegisterUser.this, "Tuya Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addUserToFirestore(String uid, String firstname, String lastnames, String addresses, String nics, String emails, String mobiles, String regNo, String selectedRole) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstname", firstname);
        userData.put("lastname", lastnames);
        userData.put("email", emails);
        userData.put("addresses", addresses);
        userData.put("nic", nics);
        userData.put("mobile", mobiles);
        userData.put("role", selectedRole);
        userData.put("subscriptionNo", regNo);

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RegisterActivity", "User added to Firestore.");
                    Intent emailPass = new Intent(RegisterUser.this, LoginActivity.class);
                    startActivity(emailPass);
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> Log.w("RegisterActivity", "Error adding user to Firestore: ", e));
    }

    private void sendTuyaVerificationCode(String email) {
        progressDialog.show();
        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                email, "", "86", 1, new com.thingclips.smart.sdk.api.IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Log.e("TuyaVerificationError", "Error code: " + code + ", message: " + error);
                        Toast.makeText(RegisterUser.this, "Verification Code Failed: " + error, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onSuccess() {
                        Log.d("TuyaVerification", "Verification code sent successfully.");
                        verificationCodeField.setVisibility(View.VISIBLE);
                        registerBtn.setVisibility(View.VISIBLE);
                        sendVerificationBtn.setVisibility(View.INVISIBLE);
                        progressDialog.dismiss();
                    }
                });
    }
}
