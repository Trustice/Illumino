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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.incrementExact;
import static java.lang.Math.subtractExact;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoomsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomsFragment extends Fragment {
    private static final String DEBUG_TAG = "*** RoomsFragment";

    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    View fragment_view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Boolean> send_enable = new ArrayList<>();
    private List<Boolean> pir_enable = new ArrayList<>();

    public RoomsFragment() {
        // Required empty public constructor
    }

    public static RoomsFragment newInstance() {
        RoomsFragment fragment = new RoomsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragment_rooms, container, false);

        ((MainActivity) getActivity()).setActionBarTitle("Illuminoo");

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//        Log.d(DEBUG_TAG, String.valueOf(sharedPref.getInt("ROOM_COUNT", 0)));
        for (int i = 0; i < sharedPref.getInt("ROOM_COUNT", 0); i++) {
            String roomNumber = String.valueOf(i);
            String roomName = sharedPref.getString("ROOM_NAME_" + roomNumber, "Error");
            String roomIP = sharedPref.getString("ROOM_IP_" + roomNumber, "Error");
            int roomIcon = sharedPref.getInt("ROOM_ICON_" + roomNumber, 0);
            String roomStripeNames = sharedPref.getString("ROOM_STRIPE_NAMES_" + roomNumber, "Error");
            boolean roomDht = sharedPref.getBoolean("ROOM_DHT_STATE_" + roomNumber, false);
            boolean roomPir = sharedPref.getBoolean("ROOM_PIR_STATE_" + roomNumber, false);
            roomList.add(new MyRoom(roomName, roomIP, roomIcon, roomStripeNames, roomDht, roomPir));
            send_enable.add(i, true);
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
                    if (send_enable.get(position)) {
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
                    send_enable.set(position, true);

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
                startRequest(ip, "T_");
                startRequest(ip, "H_");
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
        // resend request send if failed
        if ( !request.contains("_") && !response.equals(request) ) {
            startRequest(serverIP, request);
        } else {
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
                case 'T':   // Temperature
                    TextView textView_temperature = (TextView) listChild.findViewById(R.id.item_txtTemp);
                    textView_temperature.setVisibility(View.VISIBLE);
                    textView_temperature.setText(value + "°C / ");
                    break;
                case 'H':   // Humidity
                    TextView textView_humidity = (TextView) listChild.findViewById(R.id.item_txtHumid);
                    textView_humidity.setVisibility(View.VISIBLE);
                    textView_humidity.setText(value + " % r.h.");
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
                            imageView_pir.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                            break;
                        default:
                            return;

                    }

                    startRequest(room.getIp(), "P" + room.getStripes_num() + "_");
                    break;

                default:
//                    Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    void processPattern(View listChild, int roomIndex, String value){
        MyRoom room = roomList.get(roomIndex);
        int value_int = Integer.parseInt(value);
        int stripe_number = value_int / 100;
        int color_number = value_int % 100;
        room.setPattern(value);

        SwitchCompat switchCompat = (SwitchCompat) listChild.findViewById(R.id.item_switch);
        switchCompat.setEnabled(true);

        // get general room state
        if (stripe_number == room.getStripes_num()) {
            if  (pir_enable.get(roomIndex) || (value_int % 100 == 0)) {
                if ( switchCompat.isChecked() ) {
                    send_enable.set(roomIndex, false);
                    switchCompat.setChecked(false);
                }
            }
            else if (value_int % 100 == 1) {
                if ( !switchCompat.isChecked() ) {
                    send_enable.set(roomIndex, false);
                    switchCompat.setChecked(true);
                }
            } else {
                return;
            }

            for (int j = 0; j < room.getStripes_num(); j++) {
                startRequest(room.getIp(), "P" + j + "_");
            }
        }
        else if (value.length() > 4)
            return;
        else {
            LinearLayout stripe_icons_ll = (LinearLayout) listChild.findViewById(R.id.item_linearLayout_stripes_icons);
            ImageView stripe_icon = (ImageView) stripe_icons_ll.getChildAt(stripe_number);

            if ( (color_number >= 0) && (color_number <= 9) ) {
                stripe_icon.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp);
                startRequest(room.getIp(), "C" + ((stripe_number * 100) + color_number) + "_");
            }
            else if ( (color_number >= 20) && (color_number <= 29) ) {
                stripe_icon.clearColorFilter();
                stripe_icon.setImageResource(R.drawable.ic_lightbulb_outline_rainbow_24dp);

            }
            else if ( (color_number >= 30) && (color_number <= 33) ) {
                stripe_icon.setImageResource(R.drawable.ic_fire_white_24dp);
                stripe_icon.setColorFilter(Color.RED);
            }
            room.setPattern(stripe_number, color_number);
        }
    }

    void processColor(View listChild, MyRoom room, String value) {
        if (value.contains(":")) {
            int value_info = Integer.parseInt(value.split(":")[0]);
            int stripeNumber = value_info / 100;
            int colorNumber = value_info % 100;
            String color_string = value.split(":")[1];

            int red = Integer.parseInt(color_string.split(",")[0]);
            int green = Integer.parseInt(color_string.split(",")[1]);
            int blue = Integer.parseInt(color_string.split(",")[2]);

            int color = Color.rgb(red, green, blue);

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
}
