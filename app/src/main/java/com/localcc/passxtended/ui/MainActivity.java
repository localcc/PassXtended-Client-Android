package com.localcc.passxtended.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.localcc.passxtended.R;
import com.localcc.passxtended.ui.authentication.AuthenticationActivity;
import com.localcc.passxtended.ui.setup.setup_wizard;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(settings.getBoolean("setup_finished", false)) {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, setup_wizard.class);
            startActivity(intent);
        }
    }
}