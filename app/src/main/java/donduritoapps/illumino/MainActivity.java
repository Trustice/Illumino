package donduritoapps.illumino;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** Illumino Main";

    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    NavigationView navigationView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.AUF, R.string.ZU);
        drawerLayout.setDrawerListener(drawerToggle);

        populateRoomList();
        navigationView = (NavigationView) findViewById(R.id.navView);
        Menu menu = navigationView.getMenu();
        menu.add("Übersicht");
        for (int i = 0; i < roomList.size(); i++) {
            menu.add(roomList.get(i).getName());
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Log.d(DEBUG_TAG, item.toString());
                FragmentManager fm = getSupportFragmentManager();

                Log.d(DEBUG_TAG, "### Backstack: " + fm.getBackStackEntryCount());

                if (item.toString().equals("Übersicht")) {
                    MainFragment mainFragment = new MainFragment().newInstance();

                    if (fm.getBackStackEntryCount() > 0)
                        fm.popBackStackImmediate();

                    fm.beginTransaction()
                            .add(R.id.fragment_container, mainFragment)
                            .commit();
                }
                else {
                    for (int i = 0; i < roomList.size(); i++) {
                        String room_name = roomList.get(i).getName();
                        if (item.toString().equals(room_name)) {
                            RoomFragment roomFragment = new RoomFragment().newInstance(i);
                            fm.beginTransaction()
                                    .replace(R.id.fragment_container, roomFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                }

                drawerLayout.closeDrawers();
                item.setChecked(true);

                return false;
            }
        });

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        drawerToggle.syncState();

        MainFragment mainFragment = new MainFragment().newInstance();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainFragment)
                .commit();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(DEBUG_TAG, "onResume");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle drawerToggle
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateRoomList() {
        roomList.clear();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Log.d(DEBUG_TAG, String.valueOf(sharedPref.getInt("ROOM_COUNT", 0)));
        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            String roomNumber = String.valueOf(i);
            String roomName = sharedPref.getString("ROOM_NAME_" + roomNumber, "Error");
            String roomIP = sharedPref.getString("ROOM_IP_" + roomNumber, "Error");
            int roomIcon = sharedPref.getInt("ROOM_ICON_" + roomNumber, 0);
            String roomStripeNames = sharedPref.getString("ROOM_STRIPE_NAMES_" + roomNumber, "Error");
            boolean roomDht = sharedPref.getBoolean("ROOM_DHT_", false);
            boolean roomPir = sharedPref.getBoolean("ROOM_PIR_", false);
            roomList.add(new MyRoom(this, i));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://donduritoapps.illumino/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://donduritoapps.illumino/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

}
