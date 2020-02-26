package com.localcc.passxtended.ui.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.localcc.passxtended.Constants;
import com.localcc.passxtended.R;
import com.localcc.passxtended.client.Client;
import com.localcc.passxtended.ui.dialogs.WarningDialog;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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
        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(listener -> {
            EditText password_textedit = findViewById(R.id.password_textedit);

            String received_password = password_textedit.getText().toString();
            System.out.println(received_password);
            if(received_password.equals("")) {
                DialogFragment dialogFragment = new WarningDialog(R.string.authentication_input_password_empty,
                        R.string.ok);
                FragmentManager fragmentManager = getSupportFragmentManager();
                dialogFragment.show(fragmentManager, "authentication_empty");
                return;
            }
            Client.CLIENT_INSTANCE = new Client(this);

            try {
                Client.CLIENT_INSTANCE.connect(preferences.getString(Constants.IP_ALIAS, ""),
                        preferences.getInt(Constants.PORT_ALIAS, -1));
            } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                DialogFragment dialogFragment = new WarningDialog(R.string.connection_error,
                        R.string.ok);
                FragmentManager fragmentManager = getSupportFragmentManager();
                dialogFragment.show(fragmentManager, "server_connection_error");
                return;
            }

        });



    }
}