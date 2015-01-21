package com.example.murat.wep2p;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pConfig;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends Activity {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private Button discoverButton;
    private Button connectButton;
    private TextView messageView;
    private List<WifiP2pDevice> knownDevices;
    private FileServerAsyncTask server;
    private ArrayList<String> knownIpAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        server.execute();
    }

    public void addIpAddress(String ip) {
        this.knownIpAddresses.add(ip);
    }
}