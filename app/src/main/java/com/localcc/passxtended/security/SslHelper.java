package com.localcc.passxtended.security;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;

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
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SslHelper {
    static X509Certificate cert;


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

                        try {
                            KeyStore keyStore = KeyStore.getInstance(Constants.ANDROID_KEYSTORE_NAME);
                            keyStore.load(null);
                            Semaphore mutex = new Semaphore(1);
                            for(X509Certificate cert : chain) {
                                Certificate keyStore_certificate = keyStore.getCertificate("cert_" + cert.getSerialNumber());
                                if(keyStore_certificate != null) {
                                    if(cert.equals((X509Certificate)keyStore_certificate)) continue;
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
                                else keyStore.setCertificateEntry(Constants.CERT_STORAGE_PREFIX + cert.getSerialNumber(), cert);

                            }

                        } catch (NoSuchAlgorithmException | KeyStoreException | IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        try {
                            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE_NAME);
                            ks.load(null);
                            List<X509Certificate> certificateList = new ArrayList<>();
                            while(ks.aliases().hasMoreElements()) {
                                Certificate keyStoreCert = ks.getCertificate(ks.aliases().nextElement());
                                if(keyStoreCert != null) certificateList.add((X509Certificate)keyStoreCert);
                            }
                            return certificateList.toArray(new X509Certificate[0]);
                        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
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
