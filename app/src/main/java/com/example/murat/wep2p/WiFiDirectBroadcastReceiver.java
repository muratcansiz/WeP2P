package com.example.murat.wep2p;

/**
 * Created by Murat on 21/01/2015.
 */

import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Debug;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pDevice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    private WifiP2pManager.PeerListListener myPeerListListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;

        myPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList list) {
                Collection<WifiP2pDevice> res = list.getDeviceList();
                mActivity.discoveredPeers(res);
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d("", "Victoire");
            } else {
                Log.d("", "Oupssss");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Log.d("", "Peers Changed");
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                Log.d("", "Connected to a device.");
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {

                        // InetAddress from WifiP2pInfo struct.
                        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
                        mActivity.startServer();
                        // After the group negotiation, we can determine the group owner.
                        if (info.groupFormed && info.isGroupOwner) {
                            // Do whatever tasks are specific to the group owner.
                            // One common case is creating a server thread and accepting
                            // incoming connections.
                            Log.d("", "We are the owner. Waiting for connections.");

                            Log.d("", "IP ADRESS::: " + groupOwnerAddress);
                        } else if (info.groupFormed) {
                            // The other device acts as the client. In this case,
                            // you'll want to create a client thread that connects to the group
                            // owner.
                            Log.d("", "We are not the owner. Initiating the connection.");
                            Log.d("", "IP ADRESS::: " + groupOwnerAddress);


                            String host = groupOwnerAddress;
                            int port = 888;
                            int len;
                            Socket socket = new Socket();
                            byte buf[]  = new byte[1024];

                            try {
                                /**
                                 * Create a client socket with the host,
                                 * port, and timeout information.
                                 */
                                socket.bind(null);
                                socket.connect((new InetSocketAddress(host, port)), 500);

                                /**
                                 * Create a byte stream from a JPEG file and pipe it to the output stream
                                 * of the socket. This data will be retrieved by the server device.
                                 */
                                OutputStream outputStream = socket.getOutputStream();

                                outputStream.write(new String("hello").getBytes());

                                outputStream.close();
                                mActivity.addIpAddress(groupOwnerAddress);
                            } catch (IOException e) {
                                //catch logic
                            }

                            /**
                             * Clean up any open sockets when done
                             * transferring or if an exception occurred.
                             */
                            finally {
                                if (socket != null) {
                                    if (socket.isConnected()) {
                                        try {
                                            socket.close();
                                        } catch (IOException e) {
                                            //catch logic
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
            Log.d("", "Connection Changed");
        }
    }

}

