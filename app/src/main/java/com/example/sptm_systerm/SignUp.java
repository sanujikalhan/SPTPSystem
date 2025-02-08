package com.example.sptm_systerm;
import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.scene.api.IResultCallback;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {
    private String firstname,lastname,mobile_number,addresses,emails,nics,locations,verifications,passwords;
    EditText Fname,Lname ,mobile ,address, email,nic,passward , Location ,verification;
    Button verify,register;
    private String receivedEmail,passwordFrom;
    private int progress = 5;private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        Intent emailAddress = getIntent();
         receivedEmail = emailAddress.getStringExtra("emailAddress");
         passwordFrom=emailAddress.getStringExtra("password");
         progressBar = findViewById(R.id.progressBar);
         Fname = findViewById(R.id.firstName);
         Lname = findViewById(R.id.lastName);
         mobile = findViewById(R.id.MobileNumber);
         address = findViewById(R.id.Address);
         email = findViewById(R.id.Email);
         nic = findViewById(R.id.NIC);
         passward = findViewById(R.id.password);
        Location = findViewById(R.id.Location);
         verification = findViewById(R.id.myVerification);
         verify = findViewById(R.id.Submit);
         register = findViewById(R.id.verify);

         Fname.setVisibility(View.INVISIBLE);
         Lname.setVisibility(View.INVISIBLE);
         mobile.setVisibility(View.INVISIBLE);
         address.setVisibility(View.INVISIBLE);
         nic.setVisibility(View.INVISIBLE);
         passward.setVisibility(View.INVISIBLE);
         Location.setVisibility(View.INVISIBLE);
         register.setVisibility(View.INVISIBLE);
         verification.setVisibility(View.INVISIBLE);
         email.setText(receivedEmail);
         register.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 //firstname= Fname.getText().toString();
                 //lastname= Lname.getText().toString();
                // mobile_number=mobile.getText().toString();
                 //addresses=address.getText().toString();
                 emails =email.getText().toString();
                 passwords=passward.getText().toString();
                 //nics =nic.getText().toString();
                 //locations=Location.getText().toString();
                 verifications=verification.getText().toString();
                 progressBar.setVisibility(View.VISIBLE);
                 ThingHomeSdk.getUserInstance().registerAccountWithEmail("86",emails,passwords,verifications,Registercallback);

             }
         });

         verify.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                  //email.getText().toString()
                 progressBar.setVisibility(View.VISIBLE);
                 getValidationCode(receivedEmail);
             }
         });

    }

    private IRegisterCallback Registercallback = new IRegisterCallback(){

        @Override
        public void onSuccess(User user) {

            Log.d("ValidationCode", "Verification code sent successfully.");
            startActivity(new Intent(SignUp.this,LoginActivity.class));
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onError(@Nullable String errorCode, @Nullable String errorMessage) {
            progressBar.setVisibility(View.INVISIBLE);
            Log.e("ValidationCodeError", "Error code: " + errorCode + ", message: " + errorMessage);
        }
    };
    private void getValidationCode(String email) {
        // Pass the country code correctly; if you don't need to pass anything in the second parameter, you can use null.
       try{
           ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                   email,          // Email address
                   "",           // Country code as per documentation
                   "86",           // Region code or additional country code
                   1,              // Type of verification (e.g., 1 for registration)
                   new com.thingclips.smart.sdk.api.IResultCallback() {
                       @Override
                       public void onError(String code, String error) {

                               Log.e("ValidationCodeError", "Error code: " + code + ", message: " + error);
                                if (code.equals("IS_EXISTS")){
                                    Toast.makeText(SignUp.this, "Account Already Exists", Toast.LENGTH_LONG).show();
                                }
                       }

                       @Override
                       public void onSuccess() {
                           Log.d("ValidationCode", "Verification code sent successfully.");
                           //Fname.setVisibility(View.VISIBLE);
                           //Lname.setVisibility(View.VISIBLE);
                          // mobile.setVisibility(View.VISIBLE);
                          // address.setVisibility(View.VISIBLE);
                          // nic.setVisibility(View.VISIBLE);
                           passward.setVisibility(View.VISIBLE);
                           passward.setText(passwordFrom);
                          // Location.setVisibility(View.VISIBLE);
                            register.setVisibility(View.VISIBLE);
                            verification.setVisibility(View.VISIBLE);
                            verify.setVisibility(View.INVISIBLE);
                           progressBar.setVisibility(View.INVISIBLE);
                       }
                   } // Callback to handle the result
           );
       }
       catch (Exception e)
       {
           Log.e("ValidationCodeError", "Error during sending validation code: " + e.getMessage());

       }

    }



}