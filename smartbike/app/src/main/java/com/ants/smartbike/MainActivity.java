package com.ants.smartbike;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
    private BikesFragment bikesFragment;
    private ProfileFragment profileFragment;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RSSI_LOCK_THRESHOLD = -72;
    private static final int RSSI_UNLOCK_THRESHOLD = -70;

    public static final String DEFAULT_DEVICE_ADDRESS = "98:4F:EE:04:51:21";

    public boolean currentlyLocked = true;

    private BluetoothAdapter myBluetoothAdapter;

    private int readRSSIInterval = 2000; // 2 seconds
    private Handler mHandler;

    private Button lockButton;
    private Button unlockButton;

    Runnable rssiReader = new Runnable() {
        @Override
        public void run() {
            findBluetoothDevices();
            mHandler.postDelayed(rssiReader, readRSSIInterval);
        }
    };

    void startRepeatingTask() {
        rssiReader.run();
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
        bikesFragment = new BikesFragment();
        profileFragment = new ProfileFragment();

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
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            turnBluetoothOn();
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

            mHandler = new Handler();
            startRepeatingTask();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

    private void turnBluetoothOn() {
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is on",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void findBluetoothDevices() {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
//            myBluetoothAdapter.cancelDiscovery();
//            Toast.makeText(getApplicationContext(), "canceled discovering",
//                    Toast.LENGTH_LONG).show();
        }
        else {
            myBluetoothAdapter.startDiscovery();
            Toast.makeText(getApplicationContext(), "started discovering",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                Toast.makeText(getApplicationContext(),"Fab clicked" ,
                        Toast.LENGTH_LONG).show();
                findBluetoothDevices();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(),"Status enabled" ,
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(),"Status disabled" ,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private static boolean isDeviceElock(String deviceAddress) {
        if (deviceAddress.equals(DEFAULT_DEVICE_ADDRESS)) {
            return true;
        } else {
            return false;
        }
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//                Toast.makeText(getApplicationContext(),device.getAddress()  + ": " + rssi + "dBm" ,
//                        Toast.LENGTH_SHORT).show();

                if (isDeviceElock(device.getAddress())) {
                    System.out.println(rssi + "dBm, currentlyLocked: " + currentlyLocked);
//                    myBluetoothAdapter.cancelDiscovery();
                    if (currentlyLocked && rssi > RSSI_UNLOCK_THRESHOLD) {
                        unlockBike();
                    } else if (!currentlyLocked && rssi <= RSSI_LOCK_THRESHOLD) {
                        lockBike();
                    }
                }
                // do not try to connect
            } else {
                Toast.makeText(getApplicationContext(), action.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    private Map<String, String> buildPostData(String deviceAddress) {
        Map<String, String> data = new HashMap<>();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        data.put("userFbid", accessToken.getUserId());
        data.put("bikeUUID", deviceAddress);
        return data;
    }

    public void lockBike() {
        Toast.makeText(getApplicationContext(),"Lock request sent" ,
                Toast.LENGTH_LONG).show();
        NetworkHelper.sendPostRequest("lock", this, buildPostData(DEFAULT_DEVICE_ADDRESS));
        manageLockStatus(true);
    }

    public void unlockBike() {
        Toast.makeText(getApplicationContext(),"Unlock request sent" ,
                Toast.LENGTH_LONG).show();
        NetworkHelper.sendPostRequest("unlock", this, buildPostData(DEFAULT_DEVICE_ADDRESS));
        manageLockStatus(false);
    }

    private void manageLockStatus(boolean locked) {
        currentlyLocked = locked;
        if (lockButton == null) {
            lockButton = (Button) findViewById(R.id.lock_button);
        }
        if (unlockButton == null) {
            unlockButton = (Button) findViewById(R.id.unlock_button);
        }
        lockButton.setVisibility(locked ? View.GONE : View.VISIBLE);
        unlockButton.setVisibility((!locked) ? View.GONE : View.VISIBLE);
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
            if (position == 0) {
                return bikesFragment;
            } else if (position == 1) {
                return friendsFragment;
            } else if (position == 2) {
                return profileFragment;
            }
            return PlaceholderFragment.newInstance(position + 1);
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
