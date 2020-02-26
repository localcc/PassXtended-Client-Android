package com.localcc.passxtended.ui.setup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.localcc.passxtended.Constants;
import com.localcc.passxtended.client.Client;
import com.localcc.passxtended.protos.file;
import com.localcc.passxtended.protos.files;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.localcc.passxtended.R;
import com.localcc.passxtended.security.SslHelper;
import com.localcc.passxtended.ui.dialogs.WarningDialog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;


public class setup_wizard extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wizard);


        findViewById(R.id.checkButton).setOnClickListener(new CheckButtonListener());
        findViewById(R.id.nextButton).setOnClickListener(new NextButtonListener());
    }

    private class CheckButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            TextInputLayout ipLayout = findViewById(R.id.ipLayout);
            TextInputLayout portLayout = findViewById(R.id.portLayout);
            try {
                String ip = ipLayout.getEditText().getText().toString();
                int port = Integer.parseInt(portLayout.getEditText().getText().toString());

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Client client = new Client(setup_wizard.this);
                            client.connect(ip, port);
                            runOnUiThread(() -> {
                                TextView view = findViewById(R.id.resultMark);
                                view.setText(R.string.iv_mark);
                                view.setVisibility(View.VISIBLE);
                            });

                        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
                            runOnUiThread(() -> {
                                TextView view = findViewById(R.id.resultMark);
                                view.setText(R.string.x_mark);
                                view.setVisibility(View.VISIBLE);
                            });
                            e.printStackTrace();
                        }
                    }
                }.start();
            }catch(NullPointerException | NumberFormatException e) {
                DialogFragment dialogFragment = new WarningDialog(R.string.setupwizard_empty,
                        R.string.ok);
                FragmentManager manager = getSupportFragmentManager();
                dialogFragment.show(manager, "wrong");
            }
        }
    }

    private class NextButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(setup_wizard.this.getApplicationContext());
            SharedPreferences.Editor editor = settings.edit();
            TextInputLayout ipLayout = setup_wizard.this.findViewById(R.id.ipLayout);
            TextInputLayout portLayout = setup_wizard.this.findViewById(R.id.portLayout);
            try {
                editor.putString(Constants.IP_ALIAS, ipLayout.getEditText().getText().toString());
                editor.putInt(Constants.PORT_ALIAS, Integer.parseInt(portLayout.getEditText().getText().toString()));
                editor.putBoolean(Constants.SETUP_FINISHED_ALIAS, true);

                editor.apply();
            }catch(NullPointerException e) {
                DialogFragment dialogFragment = new WarningDialog(R.string.setupwizard_empty,
                        R.string.ok);
                FragmentManager manager = getSupportFragmentManager();
                dialogFragment.show(manager, "wrong");
            }
        }
    }
}