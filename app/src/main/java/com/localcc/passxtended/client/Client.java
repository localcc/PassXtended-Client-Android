package com.localcc.passxtended.client;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.localcc.passxtended.Constants;
import com.localcc.passxtended.protos.file;
import com.localcc.passxtended.protos.files;
import com.localcc.passxtended.security.SslHelper;
import com.localcc.passxtended.utils.Tuple;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

    public static Client CLIENT_INSTANCE = null;

    private Socket socket;
    private SSLSocket sslSocket;
    private InputStream is;
    private OutputStream os;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
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
        this.bis = new BufferedInputStream(is);
        this.bos = new BufferedOutputStream(os);

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

    public void WriteToSocket(byte[] data) throws IOException {
        os.write(data);
    }

    public void WriteToSocket(int b) throws IOException {
        os.write(b);
    }



    public int ReadFromSocket(byte[] arr, int length, int offset) throws IOException {
        return is.read(arr, offset, length);
    }

    public List<Tuple<String, byte[]>> ReadAllFiles() throws IOException {
        List<Tuple<String, byte[]>> filesList = new ArrayList<>();
        WriteToSocket(Constants.Commands.file_fetch);
        int data_size = ReadIntFromSockfd();
        byte[] flatbuf_arr = new byte[data_size];
        System.out.println(data_size);
        int received = bis.read(flatbuf_arr, 0, data_size);
        while(received != data_size) {
            received += bis.read(flatbuf_arr, received, data_size - received);
        }
        ByteBuffer files_bytebuf = ByteBuffer.wrap(flatbuf_arr);
        files all_files = files.getRootAsfiles(files_bytebuf);
        for(int i = 0; i < all_files.allFilesLength(); i++) {
            byte[] data_arr = all_files.allFiles(i).dataAsByteBuffer().array();
            String filename = all_files.allFiles(i).filename();
            filesList.add(new Tuple<>(filename, data_arr));
        }
        return filesList;
    }

    private int ReadIntFromSockfd() throws IOException {
        byte[] size_arr = new byte[4];
        ReadFromSocket(size_arr, 4, 0);

        return size_arr[0] & 0xFF |
                (size_arr[1] & 0xFF) << 8 |
                (size_arr[2] & 0xFF) << 16 |
                (size_arr[3] & 0xFF) << 24;
    }

    private void WriteIntToSockfd(int i) throws IOException {
        byte[] size_arr = new byte[4];
        size_arr[3] = (byte)((i >> 24) & 0xFF);
        size_arr[2] = (byte)((i >> 16) & 0xFF);
        size_arr[1] = (byte)((i >> 8) & 0xFF);
        size_arr[0] = (byte)(i & 0xFF);
        WriteToSocket(size_arr, 0, 4);
    }



    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }
}
