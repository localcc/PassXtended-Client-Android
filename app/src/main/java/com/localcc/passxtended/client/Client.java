package com.localcc.passxtended.client;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.localcc.passxtended.Constants;
import com.localcc.passxtended.security.SslHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Semaphore;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {
    private Socket socket;
    private SSLSocket sslSocket;
    private InputStream is;
    private OutputStream os;
    private Context ctx;
    public Client(Context ctx){
        socket = new Socket();
        this.ctx = ctx;
    }

    private void secure_socket(Socket socket, String ip, int port) throws NoSuchAlgorithmException, KeyManagementException, IOException{
        SSLContext sslContext = SslHelper.CreateContext(ctx);
        SSLSocketFactory factory = sslContext.getSocketFactory();
        sslSocket = (SSLSocket)factory.createSocket(socket, ip, port, true);
        SslHelper.ConfigureSocket(sslSocket);
        SocketHandshakeCompleted handshakeCompleted = new SocketHandshakeCompleted();
        sslSocket.addHandshakeCompletedListener(handshakeCompleted);
        sslSocket.startHandshake();
        handshakeCompleted.WaitForHandshakeCompletion();
        this.is = sslSocket.getInputStream();
        this.os = sslSocket.getOutputStream();

    }

    private class SocketHandshakeCompleted implements HandshakeCompletedListener {

        private volatile boolean handshakeCompleted = false;
        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            handshakeCompleted = true;
        }

        public void WaitForHandshakeCompletion() {
            while(!handshakeCompleted) {}
        }

    }

    public int connect(String ip, int port) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        socket.connect(new InetSocketAddress(ip, port), Constants.DEFAULT_SOCKET_TIMEOUT);
        secure_socket(socket, ip, port);

        return 0;
    }

    public int connect(SharedPreferences preferences) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        String ip = preferences.getString(Constants.IP_ALIAS, "");
        int port = preferences.getInt(Constants.PORT_ALIAS, -1);
        if(port == -1 || ip.equals("")) return -1;
        return connect(ip, port);
    }

    public int connect() throws NoSuchAlgorithmException, IOException, KeyManagementException {
        return connect(PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    public void WriteToSocket(byte[] data, int offset, int length) throws IOException {
        os.write(data, offset, length);
    }

    public int ReadFromSocket(byte[] arr, int length, int offset) throws IOException {
        return is.read(arr, offset, length);
    }


    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }
}
