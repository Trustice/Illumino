package donduritoapps.illumino;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.incrementExact;
import static java.lang.Math.subtractExact;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private static final String DEBUG_TAG = "*** MainFragment";
    private SharedPreferences sharedPref;

    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    View fragment_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Boolean send_main_switch_enable = true;
    private List<Boolean> send_stripe_switch_enable = new ArrayList<>();
    private List<Boolean> pir_enable = new ArrayList<>();
    private SwitchCompat switch_Toolbar;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item_switch_Toolbar = menu.findItem(R.id.action_switch);
        switch_Toolbar = (SwitchCompat) item_switch_Toolbar.getActionView().findViewById(R.id.switchForActionBar);
        switch_Toolbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (send_main_switch_enable) {
                    if (isChecked) {
                        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
                            MyRoom room = roomList.get(i);
                            startRequest(room.getIp(), "P" + (room.getStripes_num() * 100 + 1));
                        }
                    } else {
                        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
                            MyRoom room = roomList.get(i);
                            startRequest(room.getIp(), "P" + (room.getStripes_num() * 100));
                        }
                    }
                    refreshActivity();
                }
                send_main_switch_enable = true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if (id == R.id.action_switch) {
//            Log.d(DEBUG_TAG, "switc Toolbar pressed");
//            if (switch_Toolbar.isChecked()) {
//                for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
//                    MyRoom room = roomList.get(i);
//                    startRequest(room.getIp(), "P" + room.getStripes_num() * 100);
//                }
//            } else {
//                for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
//                    MyRoom room = roomList.get(i);
//                    startRequest(room.getIp(), "P" + (room.getStripes_num() * 100) + 1);
//                }
//            }
//        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getContext(), PrefRoomActivity.class);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragment_main, container, false);

        ((MainActivity) getActivity()).setActionBarTitle("Illumino");

        populateRoomList();
        populateListView();

        swipeRefreshLayout = (SwipeRefreshLayout) fragment_view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshActivity();
                    }
                }
        );

        return fragment_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateRoomList();
        populateListView();
        refreshActivity();
    }


    private void populateRoomList() {
        roomList.clear();
        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            roomList.add(new MyRoom(getContext(), i));
            send_stripe_switch_enable.add(i, true);
            pir_enable.add(i, false);
        }
    }

    private void populateListView() {
        ArrayAdapter<MyRoom> adapter = new MyListAdapter();
        ListView list = (ListView) fragment_view.findViewById(R.id.roomsListView);
        if (list == null) return;
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<MyRoom> {
        public MyListAdapter() {
            super(getActivity(), R.layout.item_view, roomList);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.item_view, parent, false);
            }

            // Find the room to work with.
            final MyRoom currentRoom = roomList.get(position);

            RelativeLayout roomAction = (RelativeLayout) itemView.findViewById(R.id.roomAction);
            roomAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity(), RoomActivity.class);
//                    intent.putExtra("ROOM_INDEX", position);
//                    startActivity(intent);

                    RoomFragment roomFragment = new RoomFragment().newInstance(position);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, roomFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

            // Fill the view
            final ImageView imageView = (ImageView) itemView.findViewById(R.id.item_icon);
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
                    if (send_stripe_switch_enable.get(position)) {
                        if (isChecked) {
                            for (int i = 0; i < currentRoom.getStripes_num(); i++) {
                                Log.d(DEBUG_TAG, "Pattern:" + currentRoom.getPattern(i));
                                if ((currentRoom.getPattern(i) == 0) || pir_enable.get(position))
                                    startRequest(currentRoom.getIp(), "P" + (i*100 + 1));
                            }
                        } else {
                            for (int i = 0; i < currentRoom.getStripes_num(); i++) {
                                Log.d(DEBUG_TAG, "Pattern:" + currentRoom.getPattern(i));
                                if (!(currentRoom.getPattern(i) == 0))
                                    startRequest(currentRoom.getIp(), "P" + (i * 100));
                            }
                        }
                        refreshActivity();
                    }
                    send_stripe_switch_enable.set(position, true);

                }
            });

            // Stripe Icons
            LinearLayout stripeIcons_ll = (LinearLayout) itemView.findViewById(R.id.item_linearLayout_stripes_icons);
            stripeIcons_ll.removeAllViews();
            for (int i = 0; i < currentRoom.getStripes_num(); i++) {
                ImageView icon = new ImageView(getContext());
                icon.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp);
                icon.setColorFilter(Color.GRAY);

                if (icon.getParent()!=null)
                    ((ViewGroup)icon.getParent()).removeView(icon);
                stripeIcons_ll.addView(icon);
            }

            return itemView;
        }
    }

    private void refreshActivity() {
        for (int i = 0; i < roomList.size(); i++) {
            MyRoom room = roomList.get(i);
            String ip = room.getIp();

            if (room.getPirState()) {
                startRequest(ip, "M_");
            } else
                startRequest(ip, "P" + room.getStripes_num() + "_");

            if (room.getDhtState()) {
                startRequest("192.168.178.27", "middleware.php/data/" + room.getTemperatureUuid() + ".json?from=1+hour+ago");
                startRequest("192.168.178.27", "middleware.php/data/" + room.getHumidityUuid() + ".json?from=1+hour+ago");
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    public void startRequest(final String ip, final String message) {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(getActivity(), "No network access", Toast.LENGTH_LONG).show();
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
                        Log.d(DEBUG_TAG, "Response PASS!!!" + url + ": " + response.replace("!",""));
                        if (ip.equals("192.168.178.27")) {
                            processVolkszaehlerResponse(response);
                        }
                        processResponse(ip, message, response.replace("!",""));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(DEBUG_TAG, "Response FAIL!!!: " + url);
            }
        });

        // Add a request (in this example, called stringRequest) to your RequestQueue.
        MyWiFi.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    // extract information from result of a WebRequest
    private void processResponse(String serverIP, String request, String response) {
        MyRoom room = roomList.get(0);
        int i;
        for (i = 0; i < roomList.size(); i++) {
            room = roomList.get(i);
            if (room.getIp().equals(serverIP))
                break;
        }
        ListView list = (ListView) fragment_view.findViewById(R.id.roomsListView);
        View listChild = list.getChildAt(i);
        char state = response.charAt(0);
        String value = response.substring(1);
        switch (state) {
            case 'P':   // process as Pattern
                processPattern(listChild, i, value);
                break;
            case 'C':   // Color
                processColor(listChild, room, value);
                break;
            case 'M':    // PIR
                ImageView imageView_pir = (ImageView) listChild.findViewById(R.id.item_imageIconPir);
                switch (value) {
                    case "0":
                        pir_enable.set(i, false);
                        imageView_pir.setVisibility(View.INVISIBLE);
                        break;
                    case "10":
                        pir_enable.set(i, false);
                        imageView_pir.setVisibility(View.VISIBLE);
                        imageView_pir.setColorFilter(Color.GRAY);
                        break;
                    case "11":
                        pir_enable.set(i, true);
                        imageView_pir.setVisibility(View.VISIBLE);
                        imageView_pir.clearColorFilter();
//                            imageView_pir.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                        break;
                    default:
                        return;

                }

                startRequest(room.getIp(), "P" + room.getStripes_num() + "_");
                break;

            default:
//                Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
        }
    }

    void processPattern(View listChild, int roomIndex, String value){
        MyRoom room = roomList.get(roomIndex);

        String pattern_string = value;
        if (value.contains(":")) {
            pattern_string = value.split(":")[0];
            // ignore the interval [Iyxxxx] part of the value --> not required in current fragment
        }
        int value_int = Integer.parseInt(pattern_string);
        int stripeIndex = value_int / 100;
        int patternIndex = value_int % 100;


        // get general room state
        if (stripeIndex == room.getStripes_num()) {
            processGeneralRoomState(listChild, roomIndex, patternIndex);
        }
        else {
            LinearLayout stripe_icons_ll = (LinearLayout) listChild.findViewById(R.id.item_linearLayout_stripes_icons);
            ImageView stripe_icon = (ImageView) stripe_icons_ll.getChildAt(stripeIndex);

            if ( (patternIndex >= 0) && (patternIndex <= 9) ) {
                stripe_icon.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp);
                startRequest(room.getIp(), "C" + ((stripeIndex * 100) + patternIndex) + "_");
            }
            else if ( (patternIndex >= 20) && (patternIndex <= 29) ) {
                stripe_icon.clearColorFilter();
                stripe_icon.setImageResource(R.drawable.ic_lightbulb_outline_rainbow_24dp);
            }
            else if ( (patternIndex >= 30) && (patternIndex <= 33) ) {
                stripe_icon.setImageResource(R.drawable.ic_fire_white_24dp);
                stripe_icon.setColorFilter(Color.RED);
            }
            room.setPattern(stripeIndex, patternIndex);
        }
    }

    private void processGeneralRoomState(View listChild, int roomIndex, int patternIndex) {
        MyRoom room = roomList.get(roomIndex);
        room.setRoomState(patternIndex);
        SwitchCompat switch_Stripe = (SwitchCompat) listChild.findViewById(R.id.item_switch);
        switch_Stripe.setEnabled(true);

        if  (pir_enable.get(roomIndex) || (patternIndex == 0)) {
            if ( switch_Stripe.isChecked() ) {
                send_stripe_switch_enable.set(roomIndex, false);
                switch_Stripe.setChecked(false);
            }
            if (allLedsOff() && switch_Toolbar.isChecked()) {
                send_main_switch_enable = false;
                switch_Toolbar.setChecked(false);
            }
        }
        else if (patternIndex == 1) {
            if ( !switch_Stripe.isChecked() ) {
                send_stripe_switch_enable.set(roomIndex, false);
                switch_Stripe.setChecked(true);
            }
            if ( !switch_Toolbar.isChecked() ) {
                send_main_switch_enable = false;
                switch_Toolbar.setChecked(true);
            }
        } else {
            return;
        }

        for (int j = 0; j < room.getStripes_num(); j++) {
            startRequest(room.getIp(), "P" + j + "_");
        }
    }

    private Boolean allLedsOff() {
        for (int i=0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            MyRoom room = roomList.get(i);
            if (room.getRoomState() != 0)
                return false;
        }
        return true;
    }

    private void processColor(View listChild, MyRoom room, String value) {
        if (value.contains(":")) {
            int value_info = Integer.parseInt(value.split(":")[0]);
            int stripeNumber = value_info / 100;
            int colorNumber = value_info % 100;
            String color_string = value.split(":")[1];

            int red, green, blue;
            int color = 0;
            if (color_string.contains("[")) {
                try {
                    JSONArray jsonArray_color = new JSONArray(color_string);
                    red = jsonArray_color.getInt(0);
                    green = jsonArray_color.getInt(1);
                    blue = jsonArray_color.getInt(2);
                    color = Color.rgb(red, green, blue);
                } catch (JSONException e) {
                    Log.e(DEBUG_TAG, "unexpected JSON exception", e);
                }
            } else {
                red = Integer.parseInt(color_string.split(",")[0]);
                green = Integer.parseInt(color_string.split(",")[1]);
                blue = Integer.parseInt(color_string.split(",")[2]);
                color = Color.rgb(red, green, blue);
            }

            // get color view from card to change tint
            LinearLayout stripe_icons_ll = (LinearLayout) listChild.findViewById(R.id.item_linearLayout_stripes_icons);
            ImageView lightbulb = (ImageView) stripe_icons_ll.getChildAt(stripeNumber);
            lightbulb.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp);
            if (colorNumber == 0)
                lightbulb.setColorFilter(Color.DKGRAY);
            else
                lightbulb.setColorFilter(color);
        }
    }

    private void processVolkszaehlerResponse(String response) {
        /* response:
            {   "version":"0.3",
                "data":
                {   "tuples":[	[1481486879723,21.1,1], ... , [1481490514018,21.4,1]    ],
                    "uuid":"1641b790-4799-11e6-9031-f7bb55dde6e3",
                    "from":1481486875984,
                    "to":1481490514018,
                    "min":[1481487747661,20.9],
                    "max":[1481488964128,21.5],
                    "average":21.215,"rows":25
                }
            }
        */
        Log.d(DEBUG_TAG, response);
        try {
            JSONObject jObj = new JSONObject(response);
            JSONObject jObjData = jObj.getJSONObject("data");
            String uuid = jObjData.getString("uuid");
            JSONArray tuples_arr = jObjData.getJSONArray("tuples");
            JSONArray tuple_arr = tuples_arr.getJSONArray(tuples_arr.length() - 1);
            double value = tuple_arr.getDouble(1);
            MyRoom room;
            int i;
            for (i = 0; i < roomList.size(); i++) {
                room = roomList.get(i);
                if (room.getTemperatureUuid().equals(uuid)) {
                    updateTemperature(room, i, value);
                    break;
                }
                if (room.getHumidityUuid().equals(uuid)) {
                    updateHumidity(room, i, value);
                    break;
                }
            }

            Log.d(DEBUG_TAG, String.valueOf(value));
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, "unexpected JSON exception", e);
        }
    }

    private void updateTemperature(MyRoom room, int room_number, double value) {
        ListView list = (ListView) fragment_view.findViewById(R.id.roomsListView);
        View listChild = list.getChildAt(room_number);

        room.setTemperature(String.valueOf(value));
        TextView textView_temperature = (TextView) listChild.findViewById(R.id.item_txtTemp);
        textView_temperature.setText(room.getTemperature() + "°C / ");
        textView_temperature.setVisibility(View.VISIBLE);
    }

    private void updateHumidity(MyRoom room, int room_number, double value) {
        ListView list = (ListView) fragment_view.findViewById(R.id.roomsListView);
        View listChild = list.getChildAt(room_number);

        room.setHumidity(String.valueOf(value));
        TextView textView_humidity = (TextView) listChild.findViewById(R.id.item_txtHumid);
        textView_humidity.setText(room.getHumidity() + " % r.h.");
        textView_humidity.setVisibility(View.VISIBLE);
    }
}
