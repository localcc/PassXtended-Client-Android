package com.localcc.passxtended;

public class Constants {
    public static int KEY_LENGTH = 4096;
    public static String ANDROID_KEYSTORE_NAME = "AndroidKeyStore";
    public static String CERT_STORAGE_PREFIX = "px_cert_";
    public static String SECURE_KEY_ALIAS = "secure_storage_key";
    public static String AES_PARAMS = "AES/GCM/NoPadding";
    public static String FINGERPRINT_ENABLED_ALIAS = "fingerprint_enabled";

    public static String IP_ALIAS = "ip";
    public static String PORT_ALIAS = "port";
    public static String SETUP_FINISHED_ALIAS = "setup_finished";

    public static int DEFAULT_SOCKET_TIMEOUT = 5000;
    public static class Commands {
        public static int file_fetch = 0;
        public static int file_write = 1;
        public static int cert_fetch = 2;
        public static int totp_req = 3;


    }
}
