package com.example.murat.wep2p;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pConfig;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends Activity {
    private Boolean isTheOwner;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private Button discoverButton;
    private Button connectButton;
    private Button sendButton;
    private TextView messageView;
    private EditText messageEdit;
    private List<WifiP2pDevice> knownDevices;
    private FileServerAsyncTask server;
    private Boolean serverStarted;
    private ArrayList<String> knownIpAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isTheOwner = false;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        knownIpAddresses = new ArrayList<String>();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        messageView = (TextView) findViewById(R.id.textView);
        messageEdit = (EditText) findViewById(R.id.editText);
        server = new FileServerAsyncTask(getApplicationContext(), messageView, this);
        this.serverStarted = false;

        discoverButton = (Button) findViewById(R.id.button);
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("WifiManager", "Discover succeeded.");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Log.d("WifiManager", "Discover failed.");
                    }
                });
            }
        });
        connectButton = (Button) findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToTheFirst();
            }
        });
        this.knownDevices = new ArrayList<WifiP2pDevice>();

        sendButton = (Button) findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                propagateMessage();
            }
        });
        this.knownDevices = new ArrayList<WifiP2pDevice>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void discoveredPeers(Collection<WifiP2pDevice> devices) {
        Iterator it= devices.iterator();
        while (it.hasNext()) {
            WifiP2pDevice device = (WifiP2pDevice) it.next();
            Log.d("One Device","Device Address::" + device.deviceAddress);

        }
        this.knownDevices.clear();
        this.knownDevices.addAll(devices);
    }

    public void setIsTheOwner(Boolean bool) {
        this.isTheOwner = bool;
    }

    private void connectToTheFirst() {
        if (this.knownDevices.size() == 0) return;

        WifiP2pDevice device = this.knownDevices.get(0);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        Log.d("Devices", "Connection to: "+config.deviceAddress);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("Devices", "Connected !");
            }

            @Override
            public void onFailure(int reason) {
                Log.d("Devices", "Connection Failed !");
            }
        });
    }

    public void startServer() {
        if (!this.serverStarted) {
            this.serverStarted = true;
            server.execute();
        }
    }

    public void addIpAddress(String ip) {
        Log.d("P2P", "Adding ip: " + ip);
        if (!this.knownIpAddresses.contains(ip)) this.knownIpAddresses.add(ip);
    }

    public void propagateMessageIfOwner() {
        if (this.isTheOwner) this.propagateMessage();
    }

    public void propagateMessage() {
        Iterator<String> ite = this.knownIpAddresses.iterator();
        Log.d("propagateMessage", "propagating intialized.");
        while(ite.hasNext()) {
            String ipAddress = ite.next();
            String host = ipAddress;
            Log.d("propagateMessage", "HOST: " + ipAddress);
            int port = 8888;
            Socket socket = new Socket();

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
                String message = this.messageEdit.getText().toString();
                Log.d("Sending", "Sending:" + message + " to: " + ipAddress);
                outputStream.write(message.getBytes());

                outputStream.close();
            } catch (IOException e) {
                Log.e("propagateMessage", "error:" + e.getMessage());
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
                            Log.e("propagateMessage", "error socket.close :" + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}