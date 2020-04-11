package com.muhammad.minhaz.viodeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private CountryCodePicker countryCodePicker;
    private EditText phoneText, codeText;
    private Button continueButton;
    private String checker = "", phoneNumber = "";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth auth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        continueButton = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneText);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueButton.getText().equals("Submit") || checker.equals("Code Sent")){
                    String verificationCode = codeText.getText().toString();
                    if (verificationCode.equals("")){
                        Toast.makeText(RegisterActivity.this, "Please give the verification code first", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        progressDialog.setTitle("Code Verification");
                        progressDialog.setMessage("Please wait");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else {
                    phoneNumber = countryCodePicker.getFullNumberWithPlus();
                    if (!phoneNumber.equals(""))
                    {
                        progressDialog.setTitle("Phone Number Verification");
                        progressDialog.setMessage("Please wait");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, RegisterActivity.this, callbacks);
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Please give valid phone number :(", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(RegisterActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);

                continueButton.setText("Continue");
                codeText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;
                resendingToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueButton.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Code has been sent!", Toast.LENGTH_SHORT).show();

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            Intent homeIntent = new Intent(RegisterActivity.this, ContactsActivity.class);
            startActivity(homeIntent);
        }
    }

    private void signInWithPhoneAuthCredential (PhoneAuthCredential credential){
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Thanks for registering!", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else
                        {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                progressDialog.dismiss();
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(RegisterActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}
