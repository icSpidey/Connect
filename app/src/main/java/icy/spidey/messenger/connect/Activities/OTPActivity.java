package icy.spidey.messenger.connect.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import icy.spidey.messenger.connect.R;
import icy.spidey.messenger.connect.databinding.ActivityOtpactivityBinding;

public class OTPActivity extends AppCompatActivity {

    ActivityOtpactivityBinding binding;

    PinView pinView;

    FirebaseAuth auth;
    FirebaseDatabase database;

    String verificationId;
    ProgressDialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying OTP ...");
        dialog.setCancelable(false);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();


        pinView = findViewById(R.id.pin_view);
        pinView.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        

        auth = FirebaseAuth.getInstance();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        String country = getIntent().getStringExtra("country");
        String verifyId = getIntent().getStringExtra("verifyId");

        binding.phoneLbl.setText("Verify " + country + phoneNumber);
        String phone = country + phoneNumber ;

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence et, int start, int before, int count) {

                if (et.toString().trim().length() == 6){
                    String code = et.toString().trim();
                    dialog.show();
                    if (verifyId != null){
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifyId, code);
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        dialog.dismiss();
                                        if (task.isSuccessful()){
                                            Intent i = new Intent(OTPActivity.this, ProfileActivity.class);
                                            startActivity(i);
                                            finishAffinity();
                                        } else {
                                            Toast.makeText(OTPActivity.this,"OTP is Not Valid", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


      /*  PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);

                        dialog.dismiss();

                        verificationId = verifyId;
                    }
                }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence otp, int start, int before, int count) {
                if (otp.toString().length()==6){
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, String.valueOf(otp));
                    
                    auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())   {
                                Intent i = new Intent(OTPActivity.this, ProfileActivity.class);
                                startActivity(i);
                                finishAffinity();

                            } else {
                                Toast.makeText(OTPActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    
                }
            }

            @Override
            public void afterTextChanged(Editable otp) {
               
            }
        }); */

    }


}