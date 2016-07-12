package donduritoapps.illumino;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "*** Illumino Main";
    //private WebRequest webRequest;
    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
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
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshActivity();
                    }
                }
        );

        //webRequest = new WebRequest();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume() {
        super.onResume();

        populateRoomList();
        populateListView();
        registerClickCallback();

        refreshActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_save);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, PrefRoomActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateRoomList() {
//        roomList.add(new MyRoom("Küche", "192.168.178.26", R.drawable.ic_restaurant_menu_white_24dp));
//        roomList.add(new MyRoom("Test", "192.168.178.34", R.drawable.ic_build_white_24dp));

//        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        roomList.clear();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(DEBUG_TAG, String.valueOf(sharedPref.getInt("ROOM_COUNT", 0)));
        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            String roomNumber = String.valueOf(i);
            String roomName = sharedPref.getString("ROOM_NAME_" + roomNumber, "Error");
            String roomIP = sharedPref.getString("ROOM_IP_" + roomNumber, "Error");
            int roomIcon = sharedPref.getInt("ROOM_ICON_" + roomNumber, 0);
            roomList.add(new MyRoom(roomName, roomIP, roomIcon));
        }
    }

    private void populateListView() {
        ArrayAdapter<MyRoom> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.roomsListView);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.roomsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyRoom clickedRoom = roomList.get(position);
                String message = "You clicked position " + position
                        + " Which is car make " + clickedRoom.getName();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
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

    private class MyListAdapter extends ArrayAdapter<MyRoom> {
        public MyListAdapter() {
            super(MainActivity.this, R.layout.item_view, roomList);
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
            }

            // Find the room to work with.
            final MyRoom currentRoom = roomList.get(position);

            RelativeLayout roomAction = (RelativeLayout) itemView.findViewById(R.id.roomAction);
            roomAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(MainActivity.this, "OKKKKK", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                    intent.putExtra("ROOM_IP", currentRoom.getIp());
                    intent.putExtra("ROOM_NAME", currentRoom.getName());
                    startActivity(intent);
                }
            });

            // Fill the view
            ImageView imageView = (ImageView) itemView.findViewById(R.id.item_icon);
            imageView.setImageResource(currentRoom.getIconID());

            // Name:
            TextView nameText = (TextView) itemView.findViewById(R.id.item_txtName);
            nameText.setText(currentRoom.getName());

            //Switch Button for ON - OFF
            SwitchCompat switchCompat = (SwitchCompat) itemView.findViewById(R.id.item_switch);
            switchCompat.setEnabled(false);
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String pattern = currentRoom.getPattern();
                    if (isChecked) {
                        if (pattern.equals("99") || pattern.equals("97")) {
                            startRequest(currentRoom.getIp(), "P98");
                        }
                    } else {
                        if (!pattern.equals("99") || !pattern.equals("97")) {
                            startRequest(currentRoom.getIp(), "P99");
                        }
                    }
                }
            });


            return itemView;
        }
    }

    private void refreshActivity() {
        for (int i = 0; i < roomList.size(); i++) {
            String ip = roomList.get(i).getIp();
            startRequest(ip, "P_");
            startRequest(ip, "C1_");
            startRequest(ip, "T_");
            startRequest(ip, "H_");
        }
        swipeRefreshLayout.setRefreshing(false);
    }


    public void startRequest(final String ip, final String message) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(MainActivity.this, "No network access", Toast.LENGTH_LONG).show();
            return;
        }

        final String url = "http://" + ip + "/" + message;
        Log.d(DEBUG_TAG, "Request: " + url);
        //webRequest.sendGetRequest(url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(DEBUG_TAG, "Response PASS!!!" + url);
                        //Snackbar.make(coordinatorLayout, response, Snackbar.LENGTH_LONG).show();
                        Log.d(DEBUG_TAG, "Response: " + response.replace("\r\n",""));
                        processResponse(ip, message, response.replace("!\r\n",""));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(DEBUG_TAG, "Response FAIL!!!: " + url);
            }
        });

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        MyWiFi.getInstance(this).addToRequestQueue(stringRequest);
    }

    // extract information from result of a WebRequest
    private void processResponse(String serverIP, String request, String response) {
        // resend request send if failed
        if (!request.contains("_") && !response.equals(request)) {
            startRequest(serverIP, request);
        } else {
            MyRoom room = roomList.get(0);
            int i;
            for (i = 0; i < roomList.size(); i++) {
                room = roomList.get(i);
                if (room.getIp().equals(serverIP)) {
                    break;
                }
            }
            ListView list = (ListView) findViewById(R.id.roomsListView);
            View listChild = list.getChildAt(i);
            char state = response.charAt(0);
            String value = response.substring(1);
            switch (state) {
                case 'P':   // process as Pattern
                    room.setPattern(value);

                    // get button view from card to change tint

                    SwitchCompat switchCompat = (SwitchCompat) listChild.findViewById(R.id.item_switch);
                    switchCompat.setEnabled(true);
                    switch (value) {
                        case "97":
                        case "99":
                            switchCompat.setChecked(false);
                            break;
                        default:
                            switchCompat.setChecked(true);
                            break;
                    }
                    break;
                case 'C':   // Color
                    if (value.length() == 10) {
                        char colorNumber = value.charAt(0);
                        int red = Integer.parseInt(value.substring(1, 4));
                        int green = Integer.parseInt(value.substring(4, 7));
                        int blue = Integer.parseInt(value.substring(7, 10));

                        int color = Color.rgb(red, green, blue);

                        // get color view from card to change tint
                        ImageView colorView = (ImageView) listChild.findViewById(R.id.item_colorIcon);

                        switch (colorNumber) {
                            case '1':
                                room.setColor1(color);
                                colorView.setColorFilter(color);
                                break;
                            case '2':
                                room.setColor2(color);
                                break;
                            default:
                                Snackbar.make(coordinatorLayout, "C_ERR_number: " + colorNumber, Snackbar.LENGTH_LONG).show();
                        }
                        //Color.rgb(red, green, blue);
                    } else {
                        Snackbar.make(coordinatorLayout, "C_ERR_value: " + value, Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case 'T':   // Temperature
                    float t = Float.parseFloat(value);
                    TextView textView_temperature = (TextView) listChild.findViewById(R.id.item_txtTemp);
                    textView_temperature.setText(value + " °C / ");
                    break;
                case 'H':   // Humidity
                    TextView textView_humidity = (TextView) listChild.findViewById(R.id.item_txtHumid);
                    textView_humidity.setText(value + " % r.h.");
                    break;
                default:
                    Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
            }
        }
    }


}
