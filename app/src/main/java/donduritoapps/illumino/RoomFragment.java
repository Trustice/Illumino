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
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

            // Find the room to work with.
//            final MyRoom currentRoom = roomList.get(position);
            // Stripe Name:
            String stripeName = stripeList.get(position);
            TextView nameText = (TextView) itemView.findViewById(R.id.textView_StripeName);
            nameText.setText(stripeName);

            handleOnOffSwitch(itemView, position);
            createPatternSelection(itemView);
            createAnimationSwitch(itemView);
            createColorButtons(itemView);
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

        private void createPatternSelection(final View itemView) {
            // Pattern Selection
            View actionPattern = itemView.findViewById(R.id.action_Pattern);
            if (actionPattern != null) {
                actionPattern.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AppCompatDialog dialog = new AppCompatDialog(getActivity());
                        dialog.setContentView(R.layout.dialog_pattern);
                        dialog.setTitle("Pattern");
                        dialog.show();

                        RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                        if (radioGroup == null) { return; }
                        radioGroup.check(radioButtonSelection);
                        Button buttonOK = (Button) dialog.findViewById(R.id.button_okay);
                        if (buttonOK == null) { return; }
                        buttonOK.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radio_group);
                                int selectedId = radioGroup.getCheckedRadioButtonId();
                                radioButtonSelection = selectedId;
                                // find the radiobutton by returned id
                                RadioButton radioButton = (RadioButton) dialog.findViewById(selectedId);
                                TextView txtViewPattern = (TextView) itemView.findViewById(R.id.textView_Pattern);
                                String pattern = radioButton.getText().toString();
                                txtViewPattern.setText(pattern);
                                switch (pattern) {
                                    case "Waves":
                                        startRequest(room.getIp(), "P2");
                                        break;
                                    case "Rainbow Short":
                                        startRequest(room.getIp(), "P3");
                                        break;
                                    case "Rainbow Long":
                                        startRequest(room.getIp(), "P4");
                                        break;
                                    case "Theater":
                                        startRequest(room.getIp(), "P6");
                                        break;
                                    case "Scanner":
                                        startRequest(room.getIp(), "P8");
                                        break;
                                    default:
                                        break;
                                }
                                //Snackbar.make(coordinatorLayout, radioButton.getText(), Snackbar.LENGTH_SHORT)
                                //        .show();
                                dialog.dismiss();
                            }
                        });

                        Button buttonCancel = (Button) dialog.findViewById(R.id.button_cancel);
                        if (buttonCancel != null) {
                            buttonCancel.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });

            }
        }

        private void createAnimationSwitch(View itemView) {
            //Switch Button for Animation
            SwitchCompat switchAnimation = (SwitchCompat) itemView.findViewById(R.id.switch_animation);
            if (switchAnimation == null) { return; }
            switchAnimation.setEnabled(true);
            switchAnimation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String pattern = room.getPattern();
                    if (isChecked) {
                        switch (pattern) {
                            case "1":
                            case "5":
                            case "7":
                            case "98":
                                startRequest(room.getIp(), "P2");
                                break;
                            default:
                                break;
                        }
                    } else { // not checked
                        switch (pattern) {
                            case "1":
                            case "5":
                            case "7":
                            case "98":
                            case "97":
                            case "99":
                                break;
                            default:
                                startRequest(room.getIp(), "P5");
                                break;
                        }
                    }
                }
            });
        }

        private void createColorButtons(View itemView) {
            // Button Color 1
            Button button_color1 = (Button) itemView.findViewById(R.id.button_color1);
            colorButtonListener(1, button_color1);
            Button button_color2 = (Button) itemView.findViewById(R.id.button_color2);
            colorButtonListener(2, button_color2);

            Button button_colorSwap = (Button) itemView.findViewById(R.id.button_color_swap);
            if (button_colorSwap == null) return;
            button_colorSwap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startRequest(room.getIp(), "P7");
                }
            });
        }

        private void colorButtonListener(final int color_number, final Button button_color) {
            button_color.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ColorActivity.class);
                    intent.putExtra("ROOM_IP", room.getIp());
                    intent.putExtra("COLOR_NUMBER", color_number);
                    intent.putExtra("ROOM_NAME", room.getName());
                    startActivity(intent);
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
        // resend request send if failed
        if (!request.contains("_") && !response.equals(request)) {
            //startRequest(serverIP, request);
            Snackbar.make(fragment_view, "Request Error: " + request + "\nresponse: " + response, Snackbar.LENGTH_LONG).show();
        } else {
            if (!room.getIp().equals(serverIP)) {
                return;
            }

            char state = response.charAt(0);
            String value = response.substring(1);

            switch (state) {
                case 'M':    // PIR
//                    ImageView imageView_pir = (ImageView) listChild.findViewById(R.id.item_imageIconPir);
                    switch (value) {
                        case "0":
                            pir_enable = false;
//                            imageView_pir.setVisibility(View.INVISIBLE);
                            break;
                        case "10":
                            pir_enable = false;
//                            imageView_pir.setVisibility(View.VISIBLE);
//                            imageView_pir.setColorFilter(Color.GRAY);
                            break;
                        case "11":
                            pir_enable = true;
//                            imageView_pir.setVisibility(View.VISIBLE);
//                            imageView_pir.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorAccent));
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
    }

    private void processPattern(String value) {
        room.setPattern(value);
        int value_int = Integer.parseInt(value);
        int stripeNumber =  value_int / 100;
        int pattern = value_int % 100;

        ListView list = (ListView) fragment_view.findViewById(R.id.roomListView);
        View listChild = list.getChildAt(stripeNumber);

        SwitchCompat switchCompatOnOff = (SwitchCompat) listChild.findViewById(R.id.switch_on_off);
        switchCompatOnOff.setEnabled(true);
        switch (pattern) {
            case 0:
                if (pir_enable) {
                    if (switchCompatOnOff.isChecked()) {
                        send_enable.set(stripeNumber, false);
                        switchCompatOnOff.setChecked(false);
                    }
                }
                break;
            default:
                if (pir_enable) {
                    if (!switchCompatOnOff.isChecked()) {
                        send_enable.set(stripeNumber, false);
                        switchCompatOnOff.setChecked(true);
                    }
                }
                break;
        }

//        TextView txtViewPattern = (TextView) itemView.findViewById(R.id.textView_Pattern);
//        String pattern;
//        switch (value) {
//            case "2":
//                pattern = "Waves";
//                break;
//            case "3":
//                pattern = "Rainbow Short";
//                break;
//            case "4":
//                pattern = "Rainbow Long";
//                break;
//            case "6":
//                pattern = "Theater";
//                break;
//            case "8":
//                pattern = "Scanner";
//                break;
//            default:
//                pattern = "-";
//                break;
//        }
//        txtViewPattern.setText(pattern);
    }

    private void processColor(String value) {
        if (value.length() == 10) {
            char colorNumber = value.charAt(0);
            int red = Integer.parseInt(value.substring(1, 4));
            int green = Integer.parseInt(value.substring(4, 7));
            int blue = Integer.parseInt(value.substring(7, 10));

            int color = Color.rgb(red, green, blue);


//            switch (colorNumber) {
//                case '1':
//                    // get color view from card to change tint
//                    Button button_color1 = (Button) itemView.findViewById(R.id.button_color1);
//                    room.setColor1(color);
//                    button_color1.setBackgroundColor(color);
//                    break;
//                case '2':
//                    // get color view from card to change tint
//                    Button button_color2 = (Button) itemView.findViewById(R.id.button_color2);
//                    room.setColor2(color);
//                    button_color2.setBackgroundColor(color);
//                    break;
//                default:
//                    //Snackbar.make(coordinatorLayout, "C_ERR_number: " + colorNumber, Snackbar.LENGTH_LONG).show();
//            }
        } else {
           // Snackbar.make(coordinatorLayout, "C_ERR_value: " + value, Snackbar.LENGTH_LONG).show();
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
