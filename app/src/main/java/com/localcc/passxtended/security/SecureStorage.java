package com.localcc.passxtended.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.preference.PreferenceManager;

import com.localcc.passxtended.Constants;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

public class SecureStorage {

    private static byte[] IV = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private static void GenerateKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                Constants.ANDROID_KEYSTORE_NAME);
        generator.init(new KeyGenParameterSpec.Builder(
                Constants.SECURE_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(false)
        .build());
        generator.generateKey();
    }

    private static Key GetKey(KeyStore ks) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return ks.getKey(Constants.SECURE_KEY_ALIAS, null);
    }

    private static KeyStore GetKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE_NAME);
        ks.load(null);
        return ks;
    }

    private static Cipher GetCipher(KeyStore ks, int cipher_mode) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher c = Cipher.getInstance(Constants.AES_PARAMS);
        c.init(cipher_mode, GetKey(ks), new GCMParameterSpec(128, IV));
        return c;
    }

    public static void Write(Context ctx, String alias, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Cipher c = GetCipher(GetKeyStore(), Cipher.ENCRYPT_MODE);
        byte[] encrypted = c.doFinal(data);
        String base64_encoded = Base64.encodeToString(encrypted, Base64.DEFAULT);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(alias, base64_encoded);
        editor.apply();
    }

    public static byte[] Read(Context ctx, String alias) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        Cipher c = GetCipher(GetKeyStore(), Cipher.DECRYPT_MODE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        byte[] decoded = Base64.decode(preferences.getString(alias, ""), Base64.DEFAULT);
        return c.doFinal(decoded);
    }

}
