package donduritoapps.illumino;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import java.util.zip.Inflater;

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

    private void populateRoomList() {
        roomList.clear();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
        ListView list = (ListView) fragment_view.findViewById(R.id.roomsListView);
        if (list == null) return;
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<MyRoom> {
        public MyListAdapter() {
            super(getActivity(), R.layout.item_view, roomList);
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
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
                    //Toast.makeText(MainActivity.this, "OKKKKK", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getActivity(), RoomActivity.class);
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
            startRequest(ip, "T_");
            startRequest(ip, "H_");

            String currPatt = roomList.get(i).getPattern();
            if (currPatt != null) {
                switch (currPatt) {
                    case "0":
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                    case "8":
                    case "9":
                        startRequest(ip, "C" + currPatt + "_");
                        break;
                    default:
                        break;
                }
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
        MyWiFi.getInstance(getActivity()).addToRequestQueue(stringRequest);
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
            ListView list = (ListView) fragment_view.findViewById(R.id.roomsListView);
            View listChild = list.getChildAt(i);
            char state = response.charAt(0);
            String value = response.substring(1);
            if (value.charAt(value.length() - 1) != '!') {
                return;
            }
            value = value.substring(0, value.length() - 1);
            switch (state) {
                case 'P':   // process as Pattern
                    room.setPattern(value);

                    // get button view from card to change tint

                    SwitchCompat switchCompat = (SwitchCompat) listChild.findViewById(R.id.item_switch);
                    switchCompat.setEnabled(true);
                    switch (value) {
                        case "0":
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
                        room.setColor1(color);
                        colorView.setColorFilter(color);
                        //Color.rgb(red, green, blue);
                    } else {
//                        Snackbar.make(coordinatorLayout, "C_ERR_value: " + value, Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case 'T':   // Temperature
                    TextView textView_temperature = (TextView) listChild.findViewById(R.id.item_txtTemp);
                    textView_temperature.setText(value + " °C / ");
                    break;
                case 'H':   // Humidity
                    TextView textView_humidity = (TextView) listChild.findViewById(R.id.item_txtHumid);
                    textView_humidity.setText(value + " % r.h.");
                    break;
                default:
//                    Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
            }
        }
    }


}
