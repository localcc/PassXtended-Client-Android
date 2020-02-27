package com.localcc.passxtended.security;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.localcc.passxtended.Constants;
import com.localcc.passxtended.R;
import com.localcc.passxtended.pointers.BoolPtr;
import com.localcc.passxtended.ui.dialogs.CertificateDialog;
import com.localcc.passxtended.ui.dialogs.WarningDialog;
import com.localcc.passxtended.ui.setup.setup_wizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SslHelper {
    static X509Certificate cert;


    private static X509Certificate getCertificateFromString(String s) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(s, 0));
        return (X509Certificate)certificateFactory.generateCertificate(is);
    }

    private static String getStringFrromCertificate(X509Certificate cert) throws CertificateEncodingException {
        return Base64.encodeToString(cert.getEncoded(), 0);
    }


    public static SSLContext CreateContext(Context ctx) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManager[] trust_with_ask = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        AppCompatActivity setup_wizard_activity = (AppCompatActivity)ctx;

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
                        SharedPreferences.Editor preferenceEditor = preferences.edit();
                        Semaphore mutex = new Semaphore(1);
                        for(X509Certificate cert : chain) {
                            String storage_cert_k = preferences.getString(
                                    Constants.CERT_STORAGE_PREFIX + cert.getSerialNumber(),
                                    "");
                            if(!storage_cert_k.equals("")) {
                                X509Certificate keyStore_certificate =
                                        getCertificateFromString(storage_cert_k);
                                if (cert.equals((X509Certificate) keyStore_certificate)) continue;
                            }
                            BoolPtr accepted = new BoolPtr();
                            DialogFragment dialogFragment = new CertificateDialog(
                                    cert.getIssuerDN().toString(),
                                    cert.getSigAlgName(),
                                    cert.getSigAlgOID(),
                                    Arrays.toString(cert.getSigAlgParams()),
                                    //Positive
                                    (dialog, which) -> {
                                        accepted.val = true;
                                        mutex.release();
                                    },
                                    //Negative
                                    (dialog, which) -> {
                                        accepted.val = false;
                                        mutex.release();
                                    }
                            );
                            FragmentManager manager = setup_wizard_activity.getSupportFragmentManager();
                            try {
                                mutex.acquire(); // waiting for user to click the button
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            dialogFragment.show(manager, "cert_ask" + cert.getSerialNumber());
                            try {
                                mutex.acquire(); // waiting for user to click the button
                                mutex.release();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(!accepted.val) throw new CertificateException();
                            else preferenceEditor.putString(Constants.CERT_STORAGE_PREFIX + cert.getSerialNumber(),
                                    getStringFrromCertificate(cert));


                        }
                        preferenceEditor.apply();


                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        /*
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
                        Map<String, ?> filtered = preferences.getAll().entrySet().stream().filter(entry ->
                            entry.getKey().startsWith(Constants.CERT_STORAGE_PREFIX))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        List<X509Certificate> certificateList = new ArrayList<>();
                        filtered.forEach((k, v) -> {
                            try {
                                X509Certificate cert = getCertificateFromString((String)v);
                                certificateList.add(cert);
                            } catch (CertificateException e) {
                                e.printStackTrace();
                            }
                        });
                        return certificateList.toArray(new X509Certificate[0]);*/
                        return new X509Certificate[0];

                    }
                }
        };
        sslContext.init(null, trust_with_ask, new SecureRandom());
        return sslContext;
    }

    public static void ConfigureSocket(SSLSocket sslSocket) throws SocketException {
        ArrayList<String> supportedCiphers = new ArrayList<>();
        supportedCiphers.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        sslSocket.setEnabledCipherSuites(supportedCiphers.toArray(new String[0]));
        sslSocket.setSoTimeout(5000);
    }


}
