package com.localcc.passxtended.ui.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.localcc.passxtended.Constants;
import com.localcc.passxtended.R;

public class AuthenticationActivity extends AppCompatActivity {


    private void login() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        BiometricManager manager = BiometricManager.from(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(manager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS && preferences.getBoolean(Constants.FINGERPRINT_ENABLED_ALIAS, false)) {
            BiometricPrompt prompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                    new BiometricPrompt.AuthenticationCallback() {

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            login();
                        }
                    });
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.authentication_fingerprint_title))
                    .setNegativeButtonText(getString(R.string.authentication_fingerprint_cancel))
                    .build();
            prompt.authenticate(promptInfo);
        }
        Button login_button = (Button)findViewById(R.id.login_button);
        login_button.setOnClickListener(listener -> {
            
        });



    }
}