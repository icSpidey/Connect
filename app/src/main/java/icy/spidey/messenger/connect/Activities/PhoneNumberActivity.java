package icy.spidey.messenger.connect.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import icy.spidey.messenger.connect.databinding.ActivityPhoneNumber2Binding;

public class PhoneNumberActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityPhoneNumber2Binding binding;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    ProgressDialog dialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For Darkmode off start here just one line below
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        CountryCodePicker countryCodeHolder;

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP ...");
        dialog.setCancelable(false);



        
        binding = ActivityPhoneNumber2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.phoneBox.requestFocus();
       
        // toolbar hide copy 3lines below
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        countryCodeHolder = findViewById(com.hbb20.R.id.countryCodeHolder);

        auth = FirebaseAuth.getInstance();


        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.phoneBox.getText().toString().isEmpty()){
                    binding.phoneBox.setError("Phone Number Can't be Empty");
                    return;
                } else {
                    if (binding.phoneBox.getText().toString().length()!=10){
                        binding.phoneBox.setError("Please Enter A Valid Phone Number");
                        return;
                    } else {
                        String countryCode = countryCodeHolder.getSelectedCountryCodeWithPlus().toString();
                        String num = binding.phoneBox.getText().toString().trim();
                        String phoneNumber = countryCode + num;
                        dialog.show();
                        binding.continueBtn.setVisibility(View.INVISIBLE);
                        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(PhoneNumberActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                binding.continueBtn.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                                intent.putExtra("phoneNumber",binding.phoneBox.getText().toString());
                                intent.putExtra("verifyId", s);
                                intent.putExtra("country",countryCodeHolder.getSelectedCountryCodeWithPlus().toString());
                                startActivity(intent);
                            }
                        };
                        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(phoneNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(PhoneNumberActivity.this)
                                .setCallbacks(callbacks)
                                .build();
                        PhoneAuthProvider.verifyPhoneNumber(options);

                    }
                }

            }
        });



    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String country = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}

