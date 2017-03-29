package com.example.falcato.wfdrouting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProviderActivity extends AppCompatActivity{

    IntentFilter mIntentFilter;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    WifiManager wifiManager;
    boolean groupCreated = false, peerDiscoveryInit = false;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);

        if(((MyApplication) ProviderActivity.this.getApplication()).getHasNet()){
            //Discover Peers
            discoverPeers();
        }else{
            //Create Group
            createGroupAsOwner();
        }
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
            String peerInfo = "Available peers: \n";
            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);
                for (WifiP2pDevice peer : peers) {
                    peerInfo += "\nMAC: " + peer.deviceAddress + " - Name: " + peer.deviceName;
                }
                TextView peerDisplay = (TextView) findViewById(R.id.peerListText);
                peerDisplay.setText(peerInfo);
            }
            if (peers.size() == 0) {
                Toast.makeText(ProviderActivity.this, "No peers found!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void discoverPeers(){
        if(mManager != null) {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    peerDiscoveryInit = true;
                    Toast.makeText(ProviderActivity.this, "Success on discovering new peers!",
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(ProviderActivity.this, "Failure on discovering new peers!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void createGroupAsOwner(){
        if(mManager != null) {
            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    groupCreated = true;
                    Toast.makeText(ProviderActivity.this, "P2P group creation successful.",
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reason) {
                    Toast.makeText(ProviderActivity.this, "P2P group creation failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, peerListListener);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(groupCreated) {
            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProviderActivity.this, "P2P group destroyed.",
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reason) {
                    Toast.makeText(ProviderActivity.this, "Failed to destroy P2P group.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(peerDiscoveryInit){
            mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProviderActivity.this, "Peer discovery stopped!",
                            Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(ProviderActivity.this, "Failed to stop peer discovery!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
