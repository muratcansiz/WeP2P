package com.example.murat.wep2p;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServerAsyncTask extends AsyncTask {

    private Context context;
    private TextView statusText;
    private MainActivity activity;

    public FileServerAsyncTask(Context context, TextView messageView, MainActivity mActivity) {
        this.context = context;
        this.statusText = (TextView) messageView;
        this.activity = mActivity;
    }



    @Override
    protected Object doInBackground(Object[] objects) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();
            String clientIp = client.getInetAddress().toString();
            activity.addIpAddress(clientIp);

            InputStream inputstream = client.getInputStream();
            String res = inputstream.toString();
            this.statusText.setText(res);
            serverSocket.close();
        } catch (IOException e) {
            Log.e("", e.getMessage());
            return null;
        }
        return null;
    }
}