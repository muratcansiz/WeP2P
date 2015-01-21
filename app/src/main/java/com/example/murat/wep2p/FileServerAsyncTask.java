package com.example.murat.wep2p;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

            while (true) {
                Socket client = serverSocket.accept();
                String clientIp = client.getInetAddress().toString();
                activity.addIpAddress(clientIp);

                InputStream inputstream = client.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputstream));
                String inputLine;
                inputLine = in.readLine();
                in.close();

                this.statusText.setText(inputLine);
                activity.propagateMessageIfOwner();
            }

            // serverSocket.close();
        } catch (IOException e) {
            Log.e("doInBackground", e.getMessage());
            return null;
        }
    }
}