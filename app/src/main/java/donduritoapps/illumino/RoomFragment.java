package donduritoapps.illumino;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomFragment extends Fragment {
    private static final String DEBUG_TAG = "*** RoomFragment";
    private static final String ARG_ROOM_INDEX = "0";


//    private static final String ARG_ROOM_NAME = "Room";
//    private static final String ARG_ROOM_IP = "255.255.255.255";

    private View fragment_view;
    private MyRoom room;
    private List<MyRoom> roomList = new ArrayList<MyRoom>();
    private List<String> stripeList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private int radioButtonSelection;
    private boolean pir_enable = true;
    private List<Boolean> send_enable = new ArrayList<>();

    public RoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomIndex Parameter 1.
     *
     * @return A new instance of fragment RoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoomFragment newInstance(Integer roomIndex) {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ROOM_INDEX, roomIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String roomNumber = String.valueOf(getArguments().getInt(ARG_ROOM_INDEX));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String roomName = sharedPref.getString("ROOM_NAME_" + roomNumber, "Error");
        String roomIP = sharedPref.getString("ROOM_IP_" + roomNumber, "Error");
        int roomIcon = sharedPref.getInt("ROOM_ICON_" + roomNumber, 0);
        String roomStripeNames = sharedPref.getString("ROOM_STRIPE_NAMES_" + roomNumber, "Error");
        boolean roomDht = sharedPref.getBoolean("ROOM_DHT_" + roomNumber, false);
        boolean roomPir = sharedPref.getBoolean("ROOM_PIR_" + roomNumber, false);

        room = new MyRoom(roomName, roomIP, roomIcon, roomStripeNames, roomDht, roomPir);
//        roomList.add(room);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment_view = inflater.inflate(R.layout.fragment_room, container, false);

        ((MainActivity) getActivity()).setActionBarTitle(room.getName());

        for (int i = 0; i < room.getStripes_num(); i++) {
            stripeList.add(room.getStripeNames().split(",")[i]);
            send_enable.add(i, true);
        }

        populateListView();

        swipeRefreshLayout = (SwipeRefreshLayout) fragment_view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshFragment();
                    }
                }
        );

        return fragment_view;
    }

    private void populateListView() {
        ArrayAdapter<String> adapter = new MyListAdapter();
        ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
        if (list == null) return;
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<String> {
        public MyListAdapter() {
            super(getActivity(), R.layout.item_room, stripeList);
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)

            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.item_room, parent, false);
            }


            for (int i = 0; i< room.getStripes_num(); i++) {
                startRequest(room.getIp(), "C" + ((i * 100)  + 10) + "_");
            }
            // Find the room to work with.
//            final MyRoom currentRoom = roomList.get(position);
            // Stripe Name:
            String stripeName = stripeList.get(position);
            TextView nameText = (TextView) itemView.findViewById(R.id.textView_StripeName);
            nameText.setText(stripeName);

            handleOnOffSwitch(itemView, position);
            handleStripeAction(itemView, position);
            handleStripeInterval(itemView, position);
//            createColorButtons(itemView);
            createSliders(itemView);

            refreshFragment();

            return itemView;
        }

        private void handleOnOffSwitch(final View itemView, final int position) {
            //Switch Button for ON - OFF
            SwitchCompat switchOnOff = (SwitchCompat) itemView.findViewById(R.id.switch_on_off);
            switchOnOff.setEnabled(false);
            switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(DEBUG_TAG, "send_enable_" + position + ": " + send_enable.get(position));
                    if (send_enable.get(position)) {
                        String pattern = room.getPattern();
                        if (isChecked) {
                            if ((room.getPattern(position) == 0))
                                startRequest(room.getIp(), "P" + (position * 100 + 1));
                        } else {
                            if (!(room.getPattern(position) == 0))
                                startRequest(room.getIp(), "P" + (position * 100));
                        }
                        refreshFragment();
                    }
                    send_enable.set(position, true);
                }
            });
        }

        private void handleStripeAction(final View itemView, final int position) {
            // Pattern Selection
            View stripeActionView = itemView.findViewById(R.id.stripeAction);
            stripeActionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AppCompatDialog dialog = new AppCompatDialog(getActivity());
                    dialog.setContentView(R.layout.dialog_template);
                    dialog.setTitle(stripeList.get(position));

                    LinearLayout linearLayout_dialog = (LinearLayout) dialog.findViewById(R.id.linearLayout_dialog);

                    // GridView for setting Static Pattern
                    GridView gV_staticButtons = new GridView(getContext());
                    gV_staticButtons.setNumColumns(4);
                    gV_staticButtons.setColumnWidth(GridView.AUTO_FIT);
                    gV_staticButtons.setAdapter(new GridAdapter_pattern(getActivity(), position, "STATIC"));
                    gV_staticButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startRequest(room.getIp(), String.format("P%d%02d", position, i));
                            dialog.dismiss();
                        }
                    });
                    linearLayout_dialog.addView(gV_staticButtons);

                    // GridView for setting Rainbow Pattern
                    GridView gV_rainbowButtons = new GridView(getContext());
                    gV_rainbowButtons.setNumColumns(4);
                    gV_rainbowButtons.setColumnWidth(GridView.AUTO_FIT);
                    gV_rainbowButtons.setAdapter(new GridAdapter_pattern(getActivity(), position, "RAINBOW"));
                    gV_rainbowButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startRequest(room.getIp(), String.format("P%d%02d", position, 20 + i));
                            dialog.dismiss();
                        }
                    });
                    linearLayout_dialog.addView(gV_rainbowButtons);

                    // GridView for setting Fire Pattern
                    GridView gV_fireButtons = new GridView(getContext());
                    gV_fireButtons.setNumColumns(4);
                    gV_fireButtons.setColumnWidth(GridView.AUTO_FIT);
                    gV_fireButtons.setAdapter(new GridAdapter_pattern(getActivity(), position, "FIRE"));
                    gV_fireButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            startRequest(room.getIp(), String.format("P%d%02d", position, 30 + i));
                            dialog.dismiss();
                        }
                    });
                    linearLayout_dialog.addView(gV_fireButtons);

                    dialog.show();
                }
            });
        }

        private class GridAdapter_pattern extends BaseAdapter {
            private Context mContext;
            private int stripeNumber;
            private int patternMax;
            private String mode;

            public GridAdapter_pattern(Context c, int stripeNr, String patternMode) {
                this.stripeNumber = stripeNr;
                this.mode = patternMode;
                switch (mode) {
                    case "STATIC":
                    case "RAINBOW":
                        this.patternMax = 10;
                        break;
                    case "FIRE":
                        this.patternMax = 4;
                    default:
                        break;
                }
                mContext = c;
            }

            public int getCount() {
                return patternMax;
            }

            public Object getItem(int position) {
                return null;
            }

            public long getItemId(int position) {
                return 0;
            }

            // create a new ImageView for each item referenced by the Adapter
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView;
                if (convertView == null) {
                    // if it's not recycled, initialize some attributes
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setPadding(8, 8, 8, 8);
                } else {
                    imageView = (ImageView) convertView;
                }

                switch (mode) {
                    case "STATIC":
                        imageView.setImageResource(R.drawable.ic_brightness_1_white_36dp);
                        imageView.setColorFilter(room.getColor(this.stripeNumber, position));
                        break;
                    case "RAINBOW":
                        imageView.setImageResource(R.drawable.ic_lightbulb_outline_rainbow_24dp);
                        break;
                    case "FIRE":
                        imageView.setImageResource(R.drawable.ic_fire_white_24dp);
                    default:
                        break;
                }

                return imageView;
            }
        }

        private void handleStripeInterval(final View itemView, final int position) {
            ImageButton imageButton_Interval = (ImageButton) itemView.findViewById(R.id.imageButton_interval);
            imageButton_Interval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AppCompatDialog dialog = new AppCompatDialog(getActivity());
                    dialog.setContentView(R.layout.dialog_template);

                    TextView dialogTitle = (TextView) dialog.findViewById(R.id.textView_dialogTitle);
                    dialogTitle.setText(stripeList.get(position) + " Interval");

                    LinearLayout ll_dialog = (LinearLayout) dialog.findViewById(R.id.linearLayout_dialog);

                    final TextView textView_currentInterval = new TextView(getContext());
                    textView_currentInterval.setText("display Intervall");
                    ll_dialog.addView(textView_currentInterval);

                    SeekBar seekBar_Interval = new SeekBar(getActivity());
                    seekBar_Interval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            double interval = Math.exp(0.08517 * progress);
                            textView_currentInterval.setText(String.format("%.0fms", interval));
                            Log.d(DEBUG_TAG, String.valueOf(interval));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            int progress = seekBar.getProgress();
                            double interval = Math.exp(0.08517 * progress);
                            String message = String.format("I%d%04.0f", position, interval);
                            startRequest(room.getIp(), message);
                        }
                    });
                    ll_dialog.addView(seekBar_Interval);

                    dialog.show();
                }
            });
        }

        private void createSliders(final View itemView) {
            SeekBar slider_interval = (SeekBar) itemView.findViewById(R.id.seekBar_Interval);
            slider_interval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                TextView textView_interval = (TextView) itemView.findViewById(R.id.textView_interval);
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    double interval = Math.exp(0.08517 * progress);
                    textView_interval.setText(String.format("%.0fms", interval));
                    Log.d(DEBUG_TAG, String.valueOf(interval));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    double interval = Math.exp(0.08517 * progress);
                    String message = String.format("I%.0f", interval);
                    startRequest(room.getIp(), message);
                }
            });
        }
    }

    private void refreshFragment() {
        if (room.getPirState()) {
            startRequest(room.getIp(), "M_");
        }
        else {
            for (int i = 0; i < room.getStripes_num(); i++)
                startRequest(room.getIp(), "P" + i + "_");
        }

        // turn off Spinner
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
        if (!room.getIp().equals(serverIP)) {
            return;
        }

        char state = response.charAt(0);
        String value = response.substring(1);

        switch (state) {
            case 'M':    // PIR
                switch (value) {
                    case "0":
                        pir_enable = false;
                        break;
                    case "10":
                        pir_enable = false;
                        break;
                    case "11":
                        pir_enable = true;
                        break;
                    default:
                        return;
                }
                for (int i = 0; i < room.getStripes_num(); i++)
                    startRequest(room.getIp(), "P" + i + "_");
                break;
            case 'P':   // process as Pattern
                processPattern(value);
                break;
            case 'C':   // Color
                processColor(value);
                break;
            case 'I':
                double progress = 11.74096 * Math.log(Integer.parseInt(value));
                room.setInterval((int) progress);
                Log.d(DEBUG_TAG, "progress: " + progress);
//                    SeekBar slider_interval = (SeekBar) itemView.findViewById(R.id.seekBar_Interval);
//                    slider_interval.setProgress((int) progress);
                break;
            default:
                //Snackbar.make(coordinatorLayout, "Invalid response from " + serverIP + "\n" + response, Snackbar.LENGTH_LONG).show();
        }
    }

    private void processPattern(String value) {
        int value_int = Integer.parseInt(value);
        int stripeNumber =  value_int / 100;
        int pattern = value_int % 100;
        room.setPattern(stripeNumber, pattern);

        ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
        View listChild = list.getChildAt(stripeNumber);

        SwitchCompat switchCompatOnOff = (SwitchCompat) listChild.findViewById(R.id.switch_on_off);
        switchCompatOnOff.setEnabled(true);

        // set Main Switch of the Stripe
        if ( (pattern == 0) && (switchCompatOnOff.isChecked()) ) {
            send_enable.set(stripeNumber, false);
            switchCompatOnOff.setChecked(false);
        }
        else if ( !(pattern == 0) && !(switchCompatOnOff.isChecked()) ) {
            send_enable.set(stripeNumber, false);
            switchCompatOnOff.setChecked(true);
        }

        // set Icon of the Stripe
        ImageView imageView_icon = (ImageView) listChild.findViewById(R.id.imageView_stripeIcon);
        if ( (pattern >= 0) && (pattern <= 9) ) {
            imageView_icon.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp);
            startRequest(room.getIp(), "C" + ((stripeNumber *100) + pattern) + "_");
        }
        else if ( (pattern >= 20) && (pattern <= 29) ) {
            imageView_icon.clearColorFilter();
            imageView_icon.setImageResource(R.drawable.ic_lightbulb_outline_rainbow_24dp);

        }
        else if ( (pattern >= 30) && (pattern <= 33) ) {
            imageView_icon.setImageResource(R.drawable.ic_fire_white_24dp);
            imageView_icon.setColorFilter(Color.RED);
        }
        room.setPattern(stripeNumber, pattern);
    }

    private void processColor(String value) {
        if (value.contains(":")) {
            int value_info = Integer.parseInt(value.split(":")[0]);
            int stripeNumber = value_info / 100;
            int colorNumber = value_info % 100;
            String color_string = value.split(":")[1];
            int color;
            int red;
            int green;
            int blue;

            if ( color_string.contains(";")) {
                if (colorNumber == 10) {
                    for (int i = 0; i <= 9; i++) {
                        red = Integer.parseInt(color_string.split(";")[i].split(",")[0]);
                        green = Integer.parseInt(color_string.split(";")[i].split(",")[1]);
                        blue = Integer.parseInt(color_string.split(";")[i].split(",")[2]);
                        color = Color.rgb(red, green, blue);
                        room.setColor(stripeNumber, i, color);
                    }
                }
            }
            else {
                red = Integer.parseInt(color_string.split(",")[0]);
                green = Integer.parseInt(color_string.split(",")[1]);
                blue = Integer.parseInt(color_string.split(",")[2]);

                color = Color.rgb(red, green, blue);
                room.setColor(stripeNumber, colorNumber, color);

                // get color view from card to change tint
                ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
                View listChild = list.getChildAt(stripeNumber);
                ImageView imageView_icon = (ImageView) listChild.findViewById(R.id.imageView_stripeIcon);
                if (colorNumber == 0)
                    imageView_icon.setColorFilter(Color.DKGRAY);
                else
                    imageView_icon.setColorFilter(color);

            }
        }
    }

    private void getVolkszaehlerData() {
        String url = "http://192.168.178.27/middleware.php/data/1641b790-4799-11e6-9031-f75b35dde6e3.json?from=7+days+ago&tuples=14";
        Log.d(DEBUG_TAG, "Request: " + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            long time_start = data.getLong("from");
                            long time_end = data.getLong("to");

                            JSONArray array_min = data.getJSONArray("min");
                            long time_min = array_min.getLong(0);
                            double min_value = array_min.getDouble(1);

                            JSONArray array_max = data.getJSONArray("max");
                            long time_max = array_max.getLong(0);
                            double max_value = array_max.getDouble(1);

                            double average_value = data.getDouble("average");
                            int rows = data.getInt("rows");

                            Log.d(DEBUG_TAG, String.valueOf(min_value));

                            JSONArray array_tuples = data.getJSONArray("tuples");
                            for (int i = 0; i < rows; i++) {
                                //array_tuples
                            }
                        } catch (JSONException e) {
                            // Oops
                        }
                        Log.d(DEBUG_TAG, "Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        // Access the RequestQueue through your singleton class.
        MyWiFi.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }
}
