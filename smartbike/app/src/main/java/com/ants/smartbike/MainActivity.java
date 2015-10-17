package com.ants.smartbike;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
//import android.bluetooth.device.extra;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ants.smartbike.bluetooth.AutoConnectActivity;
import com.ants.smartbike.bluetooth.BluetoothLeService;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private FriendsFragment friendsFragment;
//    private BluetoothSPP bluetoothSPP;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.randomUUID();
    private BluetoothAdapter myBluetoothAdapter;

    private BluetoothLeService mBluetoothLeService;

    public enum connectionStateEnum{isNull, isScanning, isToScan, isConnecting , isConnected, isDisconnecting};
    public connectionStateEnum mConnectionState = connectionStateEnum.isNull;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                ((Activity) getBaseContext()).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    private int readRSSIInterval = 1000; // 1 seconds
    private Handler mHandler;

    Runnable rssiReader = new Runnable() {
        @Override
        public void run() {
            if (mConnectionState == connectionStateEnum.isConnected) {
                mBluetoothLeService.readRemoteRSSI(); //this function can change value of mInterval.
//                changeMayowanUI(mBluetoothLeService.currentRSSI);
//                curre
                Toast.makeText(getApplicationContext(), mBluetoothLeService.currentRSSI,
                        Toast.LENGTH_LONG).show();
            }
            else if (mBluetoothLeService != null) {
                mBluetoothLeService.currentRSSI = 0;
            }

            mHandler.postDelayed(rssiReader, readRSSIInterval);
        }
    };

    void startRepeatingTask() {
        rssiReader.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(rssiReader);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        friendsFragment = FriendsFragment.newInstance("", "");

        // send friends request
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newMyFriendsRequest(
                accessToken,
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(
                            JSONArray jsonArray,
                            GraphResponse response) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            JSONObject friend = null;
                            String id = null;
                            String name = null;
                            try {
                                friend = jsonArray.getJSONObject(i);
                                id = friend.getString("id");
                                name = friend.getString("name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            friendsFragment.addItem(new FriendsFragment.FriendItem(id, name));
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name");
        request.setParameters(parameters);
        request.executeAsync();

        // Image loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        // Bluetooth
//        bluetoothSPP = new BluetoothSPP(getApplicationContext());
//
//        if(!bluetoothSPP.isBluetoothAvailable()) {
//            System.out.println("Bluetooth not available");
//        }
//        bluetoothSPP.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
//            public void onDeviceConnected(String name, String address) {
//                Toast.makeText(getApplicationContext()
//                        , "Connected to " + name
//                        , Toast.LENGTH_SHORT).show();
//            }
//
//            public void onDeviceDisconnected() {
//                Toast.makeText(getApplicationContext()
//                        , "Connection lost"
//                        , Toast.LENGTH_SHORT).show();
//            }
//
//            public void onDeviceConnectionFailed() {
//                Log.i("Check", "Unable to connect");
//            }
//        });
        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
//            onBtn.setEnabled(false);
//            offBtn.setEnabled(false);
//            listBtn.setEnabled(false);
//            findBtn.setEnabled(false);
//            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
//            text = (TextView) findViewById(R.id.text);
//            onBtn = (Button)findViewById(R.id.turnOn);
//            onBtn.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    // TODO Auto-generated method stub
//                    on(v);
//                }
//            });
//
//            offBtn = (Button)findViewById(R.id.turnOff);
//            offBtn.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    // TODO Auto-generated method stub
//                    off(v);
//                }
//            });

//            listBtn = (Button)findViewById(R.id.paired);
//            listBtn.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    // TODO Auto-generated method stub
//                    list(v);
//                }
//            });
//
//            findBtn = (Button)findViewById(R.id.search);
//            findBtn.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    // TODO Auto-generated method stub
//                    find(v);
//                }
//            });

//            myListView = (ListView)findViewById(R.id.listView1);
//
//            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
//            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
//            myListView.setAdapter(BTArrayAdapter);
            on();
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

            mHandler = new Handler();
            startRepeatingTask();
        }
    }

    public void on() {
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void onClick(View v) {
        int id = v.getId();
//        Intent intent = null;
        switch (id) {
            case R.id.fab:
//                intent = new Intent(getApplicationContext(), AutoConnectActivity.class);
//                startActivity(intent);
//                bluetoothSPP.connect("98:4F:EE:04:51:21");
//                bluetoothSPP.connect("98:4F:EE:04:51:21");
//                talkOnBluetooth();
//                on();
                find();
                break;
        }
    }

    @Override
    public void onDestroy() {
        this.unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
//                text.setText("Status: Enabled");
                Toast.makeText(getApplicationContext(),"Status enabled" ,
                        Toast.LENGTH_LONG).show();

            } else {
//                text.setText("Status: Disabled");
                Toast.makeText(getApplicationContext(),"Status disabled" ,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(getApplicationContext(),device.getAddress() ,
                        Toast.LENGTH_LONG).show();

                myBluetoothAdapter.cancelDiscovery(); // better performance
//                BluetoothSocket currentBluetoothSocket = myBluetoothAdapter.listenUsingRfcommWithServiceRecord();
//                currentBluetoothSocket.connect(device.getAddress());
//                currentBluetoothSocket.send

                // add the name and the MAC address of the object to the arrayAdapter
//                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//                BTArrayAdapter.notifyDataSetChanged();
//                mBluetoothLeService.connect(device.getAddress());
//                myBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_UUID);
//                BluetoothSocket bluetoothSocket = null;
//                try {
//                    bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//                    bluetoothSocket.connect();
//                    ConnectedThread thread = new ConnectedThread(bluetoothSocket);
//                    thread.start();
//                    thread.write("aa".getBytes());
//                    Toast.makeText(getApplicationContext(), "Connected and sent",
//                            Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                RSSI rssi = intent.getParcelableExtra(BluetoothDevice.EXTRA_RSSI);
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Toast.makeText(getApplicationContext(),"  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
                mBluetoothLeService.connect(device.getAddress());
            } else {
                Toast.makeText(getApplicationContext(), action.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
//                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public void find() {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "canceled discovering",
                    Toast.LENGTH_LONG).show();
        }
        else {
//            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
            Toast.makeText(getApplicationContext(), "started discovering",
                    Toast.LENGTH_LONG).show();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }


    @Override
    public void onStart() {
        super.onStart();
//        if(!bluetoothSPP.isBluetoothEnabled()) {
//            // Do somthing if bluetooth is disable
//            System.out.println("Bluetooth not enabled");
//        } else {
//            // Do something if bluetooth is already enable
//            bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
//            bluetoothSPP.send("Message", true);
//            System.out.println("Message sent");
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 1) {
                return friendsFragment;
            }
            return PlaceholderFragment.newInstance(position + 1);
//            switch (position) {
//                case 0: return FirstFragment.newInstance("FirstFragment, Instance 1");
//                case 1: return SecondFragment.newInstance("SecondFragment, Instance 1");
//                case 2: return ThirdFragment.newInstance("ThirdFragment, Instance 1");
//                default: return FriendsFragment.newInstance("ThirdFragment", "Default");
//            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Bikes";
                case 1:
                    return "Friends";
                case 2:
                    return "Profile";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

//    public interface OnFragmentInteractionListener {
//        public void onNavFragmentInteraction(String string);
//    }
}
